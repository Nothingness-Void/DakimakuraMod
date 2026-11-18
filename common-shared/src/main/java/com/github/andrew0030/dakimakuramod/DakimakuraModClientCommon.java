package com.github.andrew0030.dakimakuramod;

import com.github.andrew0030.dakimakuramod.dakimakura.client.DakiTextureDiskCache;
import com.github.andrew0030.dakimakuramod.dakimakura.client.DakiTextureManagerClient;
import com.mojang.logging.LogUtils;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;

public final class DakimakuraModClientCommon
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static int maxGpuTextureSize;
    private static DakiTextureManagerClient dakiTextureManagerClient;

    private DakimakuraModClientCommon() {}

    public static void initClient()
    {
        dakiTextureManagerClient = new DakiTextureManagerClient();
    }

    public static void setupClientGlAndCache()
    {
        maxGpuTextureSize = GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE);
        LOGGER.info(String.format("Max GPU texture size: %d.", maxGpuTextureSize));
        DakiTextureDiskCache.initialise();
    }

    public static void onClientTick()
    {
        if (dakiTextureManagerClient != null)
            dakiTextureManagerClient.onClientTick();
    }

    public static void onClientDisconnect()
    {
        if (dakiTextureManagerClient != null)
            dakiTextureManagerClient.onClientDisconnect();
    }

    /**
     * Retrieves the maximum texture size supported by the GPU.
     * @return The maximum GPU texture size in pixels
     */
    public static int getMaxGpuTextureSize()
    {
        return maxGpuTextureSize;
    }

    public static DakiTextureManagerClient getDakiTextureManager()
    {
        return dakiTextureManagerClient;
    }
}
