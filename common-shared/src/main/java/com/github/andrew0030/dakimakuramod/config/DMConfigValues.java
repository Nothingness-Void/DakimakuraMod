package com.github.andrew0030.dakimakuramod.config;

/**
 * Runtime configuration holder shared across Forge, NeoForge, and Fabric.
 * Fabric populates these values via {@link DMTomlConfig}; Forge/NeoForge syncs
 * its {@code ForgeConfigSpec}/{@code ModConfigSpec} values into this holder on load/reload.
 */
public final class DMConfigValues
{
    public static final int DEFAULT_MAX_TRANSFER_HEIGHT = 1536;
    public static final int MIN_MAX_TRANSFER_HEIGHT = 256;
    public static final int MAX_MAX_TRANSFER_HEIGHT = 8192;

    public static final double DEFAULT_JPEG_QUALITY = 0.80D;
    public static final double MIN_JPEG_QUALITY = 0.10D;
    public static final double MAX_JPEG_QUALITY = 1.0D;

    public static final int DEFAULT_MAX_SOURCE_MEGABYTES = 32;
    public static final int MIN_MAX_SOURCE_MEGABYTES = 1;
    public static final int MAX_MAX_SOURCE_MEGABYTES = 1024;

    public static final int DEFAULT_SERVER_MEMORY_CACHE_MINUTES = 10;
    public static final int MIN_SERVER_MEMORY_CACHE_MINUTES = 1;
    public static final int MAX_SERVER_MEMORY_CACHE_MINUTES = 1440;

    public static final boolean DEFAULT_SERVER_DISK_CACHE_ENABLED = true;

    public static final int DEFAULT_SERVER_DISK_CACHE_MEGABYTES = 1024;
    public static final int MIN_SERVER_DISK_CACHE_MEGABYTES = 0;
    public static final int MAX_SERVER_DISK_CACHE_MEGABYTES = 65536;

    public static final int DEFAULT_CLIENT_MEMORY_CACHE_MINUTES = 20;
    public static final int MIN_CLIENT_MEMORY_CACHE_MINUTES = 1;
    public static final int MAX_CLIENT_MEMORY_CACHE_MINUTES = 1440;

    public static final int DEFAULT_CLIENT_DISK_CACHE_MEGABYTES = 2048;
    public static final int MIN_CLIENT_DISK_CACHE_MEGABYTES = 0;
    public static final int MAX_CLIENT_DISK_CACHE_MEGABYTES = 65536;

    private static volatile int maxTransferHeight = DEFAULT_MAX_TRANSFER_HEIGHT;
    private static volatile double jpegQuality = DEFAULT_JPEG_QUALITY;
    private static volatile int maxSourceMegabytes = DEFAULT_MAX_SOURCE_MEGABYTES;
    private static volatile int serverMemoryCacheMinutes = DEFAULT_SERVER_MEMORY_CACHE_MINUTES;
    private static volatile boolean serverDiskCacheEnabled = DEFAULT_SERVER_DISK_CACHE_ENABLED;
    private static volatile int serverDiskCacheMegabytes = DEFAULT_SERVER_DISK_CACHE_MEGABYTES;

    private static volatile int clientMemoryCacheMinutes = DEFAULT_CLIENT_MEMORY_CACHE_MINUTES;
    private static volatile int clientDiskCacheMegabytes = DEFAULT_CLIENT_DISK_CACHE_MEGABYTES;

    private DMConfigValues() {}

    public static int getMaxTransferHeight()
    {
        return maxTransferHeight;
    }

    public static void setMaxTransferHeight(int value)
    {
        maxTransferHeight = clampInt(value, MIN_MAX_TRANSFER_HEIGHT, MAX_MAX_TRANSFER_HEIGHT);
    }

    public static double getJpegQuality()
    {
        return jpegQuality;
    }

    public static void setJpegQuality(double value)
    {
        jpegQuality = clampDouble(value, MIN_JPEG_QUALITY, MAX_JPEG_QUALITY);
    }

    public static int getMaxSourceMegabytes()
    {
        return maxSourceMegabytes;
    }

    public static void setMaxSourceMegabytes(int value)
    {
        maxSourceMegabytes = clampInt(value, MIN_MAX_SOURCE_MEGABYTES, MAX_MAX_SOURCE_MEGABYTES);
    }

    public static int getServerMemoryCacheMinutes()
    {
        return serverMemoryCacheMinutes;
    }

    public static void setServerMemoryCacheMinutes(int value)
    {
        serverMemoryCacheMinutes = clampInt(value, MIN_SERVER_MEMORY_CACHE_MINUTES, MAX_SERVER_MEMORY_CACHE_MINUTES);
    }

    public static boolean isServerDiskCacheEnabled()
    {
        return serverDiskCacheEnabled;
    }

    public static void setServerDiskCacheEnabled(boolean value)
    {
        serverDiskCacheEnabled = value;
    }

    public static int getServerDiskCacheMegabytes()
    {
        return serverDiskCacheMegabytes;
    }

    public static void setServerDiskCacheMegabytes(int value)
    {
        serverDiskCacheMegabytes = clampInt(value, MIN_SERVER_DISK_CACHE_MEGABYTES, MAX_SERVER_DISK_CACHE_MEGABYTES);
    }

    public static int getClientMemoryCacheMinutes()
    {
        return clientMemoryCacheMinutes;
    }

    public static void setClientMemoryCacheMinutes(int value)
    {
        clientMemoryCacheMinutes = clampInt(value, MIN_CLIENT_MEMORY_CACHE_MINUTES, MAX_CLIENT_MEMORY_CACHE_MINUTES);
    }

    public static int getClientDiskCacheMegabytes()
    {
        return clientDiskCacheMegabytes;
    }

    public static void setClientDiskCacheMegabytes(int value)
    {
        clientDiskCacheMegabytes = clampInt(value, MIN_CLIENT_DISK_CACHE_MEGABYTES, MAX_CLIENT_DISK_CACHE_MEGABYTES);
    }

    private static int clampInt(int value, int min, int max)
    {
        return Math.max(min, Math.min(max, value));
    }

    private static double clampDouble(double value, double min, double max)
    {
        if (Double.isNaN(value))
            return min;
        return Math.max(min, Math.min(max, value));
    }
}
