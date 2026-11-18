package com.github.andrew0030.dakimakuramod.dakimakura;

import com.github.andrew0030.dakimakuramod.config.DMConfigValues;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class DakiTextureManagerCommon implements Runnable
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final long MAX_CACHE_WEIGHT_BYTES = 256L * 1024L * 1024L;
    private static final int WORKER_THREAD_COUNT = 2;

    private final Cache<Daki, DakiImageData> dakiImageCache;
    private final ArrayList<WaitingClient> waitingClients;
    private final LinkedBlockingQueue<Daki> dakiLoadQueue;
    private final Set<Daki> queuedSet;
    private final List<Thread> workerThreads;
    private volatile boolean running = false;

    public DakiTextureManagerCommon()
    {
        long cacheMinutes = DMConfigValues.getServerMemoryCacheMinutes();
        this.dakiImageCache = CacheBuilder.newBuilder()
                .expireAfterAccess(cacheMinutes, TimeUnit.MINUTES)
                .maximumWeight(MAX_CACHE_WEIGHT_BYTES)
                .weigher((Daki key, DakiImageData value) -> {
                    int front = value.getTextureFront() != null ? value.getTextureFront().length : 0;
                    int back = value.getTextureBack() != null ? value.getTextureBack().length : 0;
                    long weight = 64L + front + back;
                    return (int) Math.min(Integer.MAX_VALUE, weight);
                })
                .build();
        this.waitingClients = new ArrayList<>();
        this.dakiLoadQueue = new LinkedBlockingQueue<>();
        this.queuedSet = ConcurrentHashMap.newKeySet();
        this.workerThreads = new ArrayList<>(WORKER_THREAD_COUNT);
    }

    /** Starts the Common Texture Manager worker threads */
    public synchronized void serverStarted()
    {
        if (this.running)
            return;
        this.running = true;
        this.workerThreads.clear();
        for (int i = 1; i <= WORKER_THREAD_COUNT; i++)
        {
            Thread thread = new Thread(this, "Dakimakura Mod texture manager thread-" + i);
            thread.setDaemon(true);
            thread.start();
            this.workerThreads.add(thread);
        }
    }

    /** Ends the Common Texture Manager worker threads and clears queues */
    public synchronized void serverStopped()
    {
        this.running = false;
        for (Thread thread : this.workerThreads)
            thread.interrupt();
        this.workerThreads.clear();
        this.clear();
    }

    @Override
    public void run()
    {
        LOGGER.info("Starting texture manager thread '{}'.", Thread.currentThread().getName());
        while (this.running)
        {
            try
            {
                Daki loadDaki = this.dakiLoadQueue.take();
                this.loadDakiTextures(loadDaki);
            }
            catch (InterruptedException e)
            {
                if (!this.running)
                    break;
            }
            catch (Throwable t)
            {
                LOGGER.error("Failed processing Dakimakura texture queue.", t);
            }
        }
        LOGGER.info("Stopped texture manager thread '{}'.", Thread.currentThread().getName());
    }

    private void loadDakiTextures(Daki daki)
    {
        if (daki == null)
            return;

        DakiImageData imageData;
        try
        {
            imageData = new DakiImageData(daki);
        }
        catch (Throwable t)
        {
            LOGGER.error(String.format("Failed loading Dakimakura texture for '%s'.", daki), t);
            imageData = new DakiImageData(daki, null, null);
        }
        finally
        {
            this.queuedSet.remove(daki);
        }
        this.dakiImageCache.put(daki, imageData);
        this.checkAndSendToClients();
    }

    public void onClientRequestTexture(ServerPlayer serverPlayer, Daki daki)
    {
        if (serverPlayer == null || daki == null || serverPlayer.hasDisconnected())
            return;

        DakiImageData cached = this.dakiImageCache.getIfPresent(daki);
        if (cached != null)
        {
            this.sendTextureToClient(serverPlayer, daki, cached);
            return;
        }

        UUID playerUuid = serverPlayer.getUUID();
        synchronized (this.waitingClients)
        {
            boolean alreadyWaiting = false;
            for (WaitingClient wc : this.waitingClients)
            {
                if (wc.playerUuid().equals(playerUuid) && wc.daki().equals(daki))
                {
                    alreadyWaiting = true;
                    break;
                }
            }
            if (!alreadyWaiting)
                this.waitingClients.add(new WaitingClient(playerUuid, serverPlayer, daki));
        }

        if (this.queuedSet.add(daki))
            this.dakiLoadQueue.offer(daki);
    }

    private void checkAndSendToClients()
    {
        List<PendingSend> toSend = new ArrayList<>();
        synchronized (this.waitingClients)
        {
            Iterator<WaitingClient> it = this.waitingClients.iterator();
            while (it.hasNext())
            {
                WaitingClient waitingClient = it.next();
                ServerPlayer player = waitingClient.getPlayer();
                if (player == null || player.hasDisconnected())
                {
                    it.remove();
                    continue;
                }
                DakiImageData imageData = this.dakiImageCache.getIfPresent(waitingClient.getDaki());
                if (imageData != null)
                {
                    toSend.add(new PendingSend(player, waitingClient.getDaki(), imageData));
                    it.remove();
                }
            }
        }

        for (PendingSend send : toSend)
        {
            if (!send.player().hasDisconnected())
                this.sendTextureToClient(send.player(), send.daki(), send.imageData());
        }
    }

    private void sendTextureToClient(ServerPlayer serverPlayer, Daki daki, DakiImageData imageData)
    {
        DakiSendHelper.sendDakiTexturesToClient(serverPlayer, daki, imageData);
    }

    public int size()
    {
        return (int) this.dakiImageCache.size();
    }

    public void clear()
    {
        this.dakiImageCache.invalidateAll();
        this.dakiLoadQueue.clear();
        this.queuedSet.clear();
        synchronized (this.waitingClients)
        {
            this.waitingClients.clear();
        }
    }

    private record PendingSend(ServerPlayer player, Daki daki, DakiImageData imageData) {}

    public record WaitingClient(UUID playerUuid, ServerPlayer serverPlayer, Daki daki)
    {
        public ServerPlayer getPlayer()
        {
            return this.serverPlayer();
        }

        public Daki getDaki()
        {
            return this.daki();
        }
    }
}
