package com.github.andrew0030.dakimakuramod;

import com.github.andrew0030.dakimakuramod.dakimakura.DakiExtractor;
import com.github.andrew0030.dakimakuramod.dakimakura.DakiManager;
import com.github.andrew0030.dakimakuramod.dakimakura.DakiTextureManagerCommon;
import com.github.andrew0030.dakimakuramod.platform.Services;

public final class DakimakuraModCommon
{
    public static final String MODID = "dakimakuramod";
    private static DakiManager dakiManager;
    private static DakiTextureManagerCommon dakiTextureManagerCommon;

    private DakimakuraModCommon() {}

    public static void initCommon()
    {
        dakiManager = new DakiManager(Services.getPlatform().getGameDirectory().toFile());
        dakiTextureManagerCommon = new DakiTextureManagerCommon();
        DakiExtractor.extractDakis();
    }

    public static void onServerStarting()
    {
        if (dakiManager != null)
            dakiManager.loadPacks(false);
        if (dakiTextureManagerCommon != null)
            dakiTextureManagerCommon.serverStarted();
    }

    public static void onServerStopping()
    {
        if (dakiTextureManagerCommon != null)
            dakiTextureManagerCommon.serverStopped();
    }

    public static DakiManager getDakimakuraManager()
    {
        return dakiManager;
    }

    public static DakiTextureManagerCommon getTextureManagerCommon()
    {
        return dakiTextureManagerCommon;
    }
}
