package com.github.andrew0030.dakimakuramod.dakimakura;

import com.github.andrew0030.dakimakuramod.config.DMConfig;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class DakiTextureManagerCommon implements Runnable
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Cache<Daki, DakiImageData> dakiImageCache;
    private final ArrayList<WaitingClient> waitingClients;
    private final ArrayList<Daki> dakiLoadQueue;
    private volatile Thread threadTextureManager = null;

    public DakiTextureManagerCommon()
    {
        long cacheMinutes = DMConfig.COMMON_SPEC.isLoaded() ? DMConfig.COMMON.memoryCacheMinutes.get() : 10L;
        this.dakiImageCache = CacheBuilder.newBuilder().expireAfterAccess(cacheMinutes, TimeUnit.MINUTES).build();
        this.waitingClients = new ArrayList<>();
        this.dakiLoadQueue = new ArrayList<>();
    }

    /** Starts a new Common Texture Manager Thread */
    public void serverStarted()
    {
        this.threadTextureManager = new Thread(this, "Dakimakura Mod texture manager thread");
        this.threadTextureManager.start();
    }

    /** Ends the Common Texture Manager Thread */
    public void serverStopped()
    {
        this.threadTextureManager = null;
    }

    @Override
    public void run()
    {
        Thread thisThread = Thread.currentThread();
        LOGGER.info("Starting texture manager thread.");
        while (this.threadTextureManager == thisThread)
        {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                this.processDakiQueue();
            } catch (RuntimeException e) {
                LOGGER.error("Failed processing Dakimakura texture queue.", e);
            }
        }
        LOGGER.info("Stopped texture manager thread.");
    }

    private void processDakiQueue()
    {
        Daki loadDaki = null;
        synchronized (this.dakiLoadQueue)
        {
            if (!this.dakiLoadQueue.isEmpty())
                loadDaki = this.dakiLoadQueue.get(0);
        }

        if (loadDaki != null)
            this.loadDakiTextures(loadDaki);
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
        catch (RuntimeException e)
        {
            LOGGER.error(String.format("Failed loading Dakimakura texture for '%s'.", daki), e);
            imageData = new DakiImageData(daki, null, null);
        }
        finally
        {
            synchronized (this.dakiLoadQueue)
            {
                this.dakiLoadQueue.remove(daki);
            }
        }
        synchronized (this.dakiImageCache)
        {
            this.dakiImageCache.put(daki, imageData);
        }
        this.checkAndSendToClients();
    }

    public void onClientRequestTexture(ServerPlayer serverPlayer, Daki daki)
    {
        if (serverPlayer == null || daki == null)
            return;

        synchronized (this.dakiImageCache)
        {
            if (this.dakiImageCache.asMap().containsKey(daki))
            {
                this.sendTextureToClient(serverPlayer, daki, this.dakiImageCache.getIfPresent(daki));
            }
            else
            {
                synchronized (this.waitingClients)
                {
                    waitingClients.add(new WaitingClient(serverPlayer, daki));
                }
                synchronized (this.dakiLoadQueue)
                {
                    if (!this.dakiLoadQueue.contains(daki))
                        this.dakiLoadQueue.add(daki);
                }
            }
        }
    }

    private void checkAndSendToClients()
    {
        synchronized (this.dakiImageCache)
        {
            synchronized (this.waitingClients)
            {
                int i = 0;
                while (i < this.waitingClients.size())
                {
                    WaitingClient waitingClient = this.waitingClients.get(i);
                    DakiImageData imageData = this.dakiImageCache.getIfPresent(waitingClient.getDaki());
                    if (imageData != null)
                    {
                        this.sendTextureToClient(waitingClient.getPlayer(), waitingClient.getDaki(), imageData);
                        this.waitingClients.remove(i);
                    }
                    else
                    {
                        i++;
                    }
                }
            }
        }
    }

    private void sendTextureToClient(ServerPlayer serverPlayer, Daki daki, DakiImageData imageData)
    {
        //DakimakuraMod.getLogger().info("Sending daki to client " + playerEntity.getDisplayName() + " " + daki);
        DakiSendHelper.sendDakiTexturesToClient(serverPlayer, daki, imageData);
    }

    /**
     * Returns the number of elements in the daki image cache.
     * This method synchronizes access to the daki image cache to ensure thread safety.
     * @return The number of elements in the daki image cache
     */
    public int size()
    {
        synchronized (this.dakiImageCache)
        {
            return this.dakiImageCache.asMap().size();
        }
    }

    /**
     * Clears all elements from the daki image cache, daki load queue, and waiting clients list.
     * This method synchronizes access to each data structure, to ensure thread safety.
     */
    public void clear()
    {
        synchronized (this.dakiImageCache)
        {
            this.dakiImageCache.asMap().clear();
        }
        synchronized (this.dakiLoadQueue)
        {
            this.dakiLoadQueue.clear();
        }
        synchronized (this.waitingClients)
        {
            this.waitingClients.clear();
        }
    }

    public record WaitingClient(ServerPlayer serverPlayer, Daki daki)
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
