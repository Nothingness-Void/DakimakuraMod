package com.github.andrew0030.dakimakuramod.dakimakura.client;

import com.github.andrew0030.dakimakuramod.DakimakuraModCommon;
import com.github.andrew0030.dakimakuramod.config.DMConfigValues;
import com.github.andrew0030.dakimakuramod.dakimakura.Daki;
import com.github.andrew0030.dakimakuramod.dakimakura.DakiImageData;
import com.github.andrew0030.dakimakuramod.dakimakura.DakiManager;
import com.github.andrew0030.dakimakuramod.platform.Services;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DakiTextureManagerClient implements RemovalListener<Daki, DakiTexture>
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_TEXTURE_CACHE_SIZE = 512;
    private static final int MAX_READY_BUFFERS = 16;
    private static final int MAX_IN_FLIGHT_REQUESTS = 4;
    private static final long REQUEST_TIMEOUT_MS = 30_000L;

    private final Cache<Daki, DakiTexture> textureCache;
    private final ArrayList<DakiTexture> textureCleanup;

    private final ExecutorService decodeExecutor;
    private final ExecutorService diskWriteExecutor;

    private final AtomicInteger readyUploadCount = new AtomicInteger(0);
    private final ConcurrentLinkedQueue<PendingDecode> pendingDecodeQueue = new ConcurrentLinkedQueue<>();

    private final ConcurrentHashMap<String, Long> inFlightRequests = new ConcurrentHashMap<>();
    private final ArrayDeque<Daki> pendingNetworkQueue = new ArrayDeque<>();
    private final Set<String> pendingNetworkKeys = new HashSet<>();

    public DakiTextureManagerClient()
    {
        long cacheMinutes = DMConfigValues.getClientMemoryCacheMinutes();
        this.textureCache = CacheBuilder.newBuilder()
                .maximumSize(MAX_TEXTURE_CACHE_SIZE)
                .removalListener(this)
                .expireAfterAccess(cacheMinutes, TimeUnit.MINUTES)
                .build();
        this.textureCleanup = new ArrayList<>();

        int decodeThreads = Math.max(2, Math.min(4, Runtime.getRuntime().availableProcessors() / 2));
        this.decodeExecutor = Executors.newFixedThreadPool(decodeThreads, daemonThreadFactory("Dakimakura-Decode-"));
        this.diskWriteExecutor = Executors.newSingleThreadExecutor(daemonThreadFactory("Dakimakura-DiskWrite-"));
    }

    private static ThreadFactory daemonThreadFactory(String prefix)
    {
        AtomicInteger counter = new AtomicInteger(1);
        return runnable -> {
            Thread thread = new Thread(runnable, prefix + counter.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        };
    }

    public static String getDakiKey(Daki daki)
    {
        return daki.getPackDirectoryName() + ":" + daki.getDakiDirectoryName();
    }

    public DakiTexture getTextureForDaki(Daki daki)
    {
        DakiTexture dakiTexture = this.textureCache.getIfPresent(daki);
        if (dakiTexture == null)
        {
            dakiTexture = new DakiTexture(daki);
            this.textureCache.put(daki, dakiTexture);
        }
        return dakiTexture;
    }

    public ArrayList<Daki> getCachedDakis()
    {
        return new ArrayList<>(this.textureCache.asMap().keySet());
    }

    /**
     * Enqueues an off-thread disk-cache check and decode for {@code daki}.
     * Never performs disk I/O on the render thread.
     */
    public void requestTexture(Daki daki, DakiTexture texture)
    {
        if (daki == null || texture == null || texture.isReleased())
            return;
        this.pendingDecodeQueue.add(new PendingDecode(daki, texture, null));
        this.pumpDecodeQueue();
    }

    public void onReadyBufferFreed()
    {
        this.readyUploadCount.updateAndGet(v -> Math.max(0, v - 1));
        this.pumpDecodeQueue();
    }

    private void pumpDecodeQueue()
    {
        while (this.readyUploadCount.get() < MAX_READY_BUFFERS)
        {
            PendingDecode task = this.pendingDecodeQueue.poll();
            if (task == null)
                break;
            if (task.texture.isReleased() || this.textureCache.getIfPresent(task.daki) != task.texture)
                continue;

            if (this.readyUploadCount.incrementAndGet() > MAX_READY_BUFFERS)
            {
                this.readyUploadCount.decrementAndGet();
                this.pendingDecodeQueue.add(task);
                break;
            }

            this.decodeExecutor.submit(() -> this.runDecodeTask(task));
        }
    }

    private void runDecodeTask(PendingDecode task)
    {
        boolean producedBuffer = false;
        try
        {
            if (task.texture.isReleased() || this.textureCache.getIfPresent(task.daki) != task.texture)
                return;

            DakiImageData imageData = task.imageData;
            if (imageData == null)
            {
                imageData = DakiTextureDiskCache.load(task.daki);
                if (imageData == null)
                {
                    this.enqueueNetworkRequest(task.daki);
                    return;
                }
            }

            producedBuffer = task.texture.decodeImageBuffer(imageData);
        }
        catch (Throwable t)
        {
            LOGGER.warn("Failed decoding Dakimakura texture '{}'.", task.daki, t);
        }
        finally
        {
            if (!producedBuffer)
            {
                this.readyUploadCount.updateAndGet(v -> Math.max(0, v - 1));
                this.pumpDecodeQueue();
            }
        }
    }

    private void enqueueNetworkRequest(Daki daki)
    {
        if (daki == null)
            return;
        String key = getDakiKey(daki);
        synchronized (this.pendingNetworkQueue)
        {
            if (!this.inFlightRequests.containsKey(key) && this.pendingNetworkKeys.add(key))
                this.pendingNetworkQueue.addLast(daki);
        }
        this.pumpNetworkQueue();
    }

    public void pumpNetworkQueue()
    {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, Long>> it = this.inFlightRequests.entrySet().iterator();
        DakiManager dakiManager = DakimakuraModCommon.getDakimakuraManager();
        while (it.hasNext())
        {
            Map.Entry<String, Long> entry = it.next();
            if (now - entry.getValue() > REQUEST_TIMEOUT_MS)
            {
                it.remove();
                String expiredKey = entry.getKey();
                LOGGER.warn("Timed out waiting for Dakimakura texture '{}', resetting request state.", expiredKey);
                String[] split = expiredKey.split(":", 2);
                if (split.length == 2 && dakiManager != null)
                {
                    Daki expiredDaki = dakiManager.getDakiFromMap(split[0], split[1]);
                    if (expiredDaki != null)
                    {
                        DakiTexture dakiTexture = this.textureCache.getIfPresent(expiredDaki);
                        if (dakiTexture != null)
                            dakiTexture.resetRequest();
                    }
                }
            }
        }

        while (this.inFlightRequests.size() < MAX_IN_FLIGHT_REQUESTS)
        {
            Daki nextDaki = null;
            String nextKey = null;
            synchronized (this.pendingNetworkQueue)
            {
                while (!this.pendingNetworkQueue.isEmpty())
                {
                    Daki candidate = this.pendingNetworkQueue.pollFirst();
                    String candidateKey = getDakiKey(candidate);
                    this.pendingNetworkKeys.remove(candidateKey);
                    DakiTexture cachedTexture = this.textureCache.getIfPresent(candidate);
                    if (cachedTexture != null && !cachedTexture.isReleased())
                    {
                        nextDaki = candidate;
                        nextKey = candidateKey;
                        break;
                    }
                }
            }
            if (nextDaki == null)
                break;

            if (this.inFlightRequests.putIfAbsent(nextKey, System.currentTimeMillis()) == null)
            {
                Services.getPlatform().clientRequestTextures(nextDaki);
            }
        }
    }

    public void serverSentTextures(String path, DakiImageData imageData)
    {
        if (path != null)
            this.inFlightRequests.remove(path);
        else if (imageData != null && imageData.getDaki() != null)
            this.inFlightRequests.remove(getDakiKey(imageData.getDaki()));

        this.pumpNetworkQueue();

        if (imageData == null || imageData.getDaki() == null)
            return;

        Daki daki = imageData.getDaki();
        byte[] frontBytes = imageData.getTextureFront();
        byte[] backBytes = imageData.getTextureBack();
        if (frontBytes != null && backBytes != null)
        {
            DakiImageData diskCopy = new DakiImageData(daki, frontBytes, backBytes);
            this.diskWriteExecutor.submit(() -> DakiTextureDiskCache.save(diskCopy));
        }

        DakiTexture dakiTexture = this.textureCache.getIfPresent(daki);
        if (dakiTexture != null && !dakiTexture.isReleased())
        {
            this.pendingDecodeQueue.add(new PendingDecode(daki, dakiTexture, imageData));
            this.pumpDecodeQueue();
        }
    }

    public void onClientTick()
    {
        this.textureCache.cleanUp();
        this.deleteTextures();
        this.pumpNetworkQueue();
        this.pumpDecodeQueue();
    }

    public void onClientDisconnect()
    {
        ClientPackListReceiver.clearStaging();
        this.reloadTextures();
    }

    /** Used to clear the {@link DakiTextureManagerClient} cache and pending queues. */
    public void reloadTextures()
    {
        synchronized (this.pendingNetworkQueue)
        {
            this.pendingNetworkQueue.clear();
            this.pendingNetworkKeys.clear();
        }
        this.inFlightRequests.clear();
        this.pendingDecodeQueue.clear();
        ClientTextureReceiver.clearPendingTransfers();
        this.textureCache.invalidateAll();
        this.textureCache.cleanUp();
    }

    private void deleteTextures()
    {
        synchronized (this.textureCleanup)
        {
            for (DakiTexture texture : this.textureCleanup)
                if (texture != null)
                    texture.releaseId();
            this.textureCleanup.clear();
        }
    }

    @Override
    public void onRemoval(RemovalNotification<Daki, DakiTexture> notification)
    {
        synchronized (this.textureCleanup)
        {
            this.textureCleanup.add(notification.getValue());
        }
    }

    private record PendingDecode(Daki daki, DakiTexture texture, DakiImageData imageData) {}
}
