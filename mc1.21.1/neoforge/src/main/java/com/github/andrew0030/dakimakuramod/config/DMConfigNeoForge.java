package com.github.andrew0030.dakimakuramod.config;

import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public final class DMConfigNeoForge
{
    public static final Common COMMON;
    public static final ModConfigSpec COMMON_SPEC;
    public static final Client CLIENT;
    public static final ModConfigSpec CLIENT_SPEC;

    static
    {
        Pair<Common, ModConfigSpec> commonPair = new ModConfigSpec.Builder().configure(Common::new);
        COMMON = commonPair.getLeft();
        COMMON_SPEC = commonPair.getRight();

        Pair<Client, ModConfigSpec> clientPair = new ModConfigSpec.Builder().configure(Client::new);
        CLIENT = clientPair.getLeft();
        CLIENT_SPEC = clientPair.getRight();
    }

    private DMConfigNeoForge() {}

    public static void onConfigLoad(ModConfigEvent.Loading event)
    {
        sync(event.getConfig().getSpec());
    }

    public static void onConfigReload(ModConfigEvent.Reloading event)
    {
        sync(event.getConfig().getSpec());
    }

    private static void sync(net.neoforged.fml.config.IConfigSpec spec)
    {
        if (spec == COMMON_SPEC)
        {
            DMConfigValues.setMaxTransferHeight(COMMON.maxTransferHeight.get());
            DMConfigValues.setJpegQuality(COMMON.jpegQuality.get());
            DMConfigValues.setMaxSourceMegabytes(COMMON.maxSourceMegabytes.get());
            DMConfigValues.setServerMemoryCacheMinutes(COMMON.memoryCacheMinutes.get());
            DMConfigValues.setServerDiskCacheEnabled(COMMON.serverDiskCacheEnabled.get());
            DMConfigValues.setServerDiskCacheMegabytes(COMMON.diskCacheMegabytes.get());
        }
        else if (spec == CLIENT_SPEC)
        {
            DMConfigValues.setClientMemoryCacheMinutes(CLIENT.memoryCacheMinutes.get());
            DMConfigValues.setClientDiskCacheMegabytes(CLIENT.diskCacheMegabytes.get());
        }
    }

    public static final class Common
    {
        public final ModConfigSpec.IntValue maxTransferHeight;
        public final ModConfigSpec.DoubleValue jpegQuality;
        public final ModConfigSpec.IntValue maxSourceMegabytes;
        public final ModConfigSpec.IntValue memoryCacheMinutes;
        public final ModConfigSpec.IntValue diskCacheMegabytes;
        public final ModConfigSpec.BooleanValue serverDiskCacheEnabled;

        Common(ModConfigSpec.Builder builder)
        {
            builder.comment("Server / shared options. Applied when serving Dakimakura textures to clients.").push("server");

            maxTransferHeight = builder
                    .comment("Maximum pixel height for Dakimakura textures sent to clients.")
                    .defineInRange("maxTransferHeight", DMConfigValues.DEFAULT_MAX_TRANSFER_HEIGHT, DMConfigValues.MIN_MAX_TRANSFER_HEIGHT, DMConfigValues.MAX_MAX_TRANSFER_HEIGHT);

            jpegQuality = builder
                    .comment("JPEG re-encoding quality (0.0 - 1.0) for smoothed, opaque textures.")
                    .defineInRange("jpegQuality", DMConfigValues.DEFAULT_JPEG_QUALITY, DMConfigValues.MIN_JPEG_QUALITY, DMConfigValues.MAX_JPEG_QUALITY);

            maxSourceMegabytes = builder
                    .comment("Maximum accepted source-image file size in megabytes. Larger files are skipped.")
                    .defineInRange("maxSourceMegabytes", DMConfigValues.DEFAULT_MAX_SOURCE_MEGABYTES, DMConfigValues.MIN_MAX_SOURCE_MEGABYTES, DMConfigValues.MAX_MAX_SOURCE_MEGABYTES);

            memoryCacheMinutes = builder
                    .comment("How long (in minutes) optimized textures stay in the server in-memory cache after last access.")
                    .defineInRange("memoryCacheMinutes", DMConfigValues.DEFAULT_SERVER_MEMORY_CACHE_MINUTES, DMConfigValues.MIN_SERVER_MEMORY_CACHE_MINUTES, DMConfigValues.MAX_SERVER_MEMORY_CACHE_MINUTES);

            serverDiskCacheEnabled = builder
                    .comment("If true, the server also stores optimized texture bytes on disk and reuses them across restarts.")
                    .define("diskCacheEnabled", DMConfigValues.DEFAULT_SERVER_DISK_CACHE_ENABLED);

            diskCacheMegabytes = builder
                    .comment("Soft cap on the server disk cache total size, in megabytes. 0 means unlimited.")
                    .defineInRange("diskCacheMegabytes", DMConfigValues.DEFAULT_SERVER_DISK_CACHE_MEGABYTES, DMConfigValues.MIN_SERVER_DISK_CACHE_MEGABYTES, DMConfigValues.MAX_SERVER_DISK_CACHE_MEGABYTES);

            builder.pop();
        }
    }

    public static final class Client
    {
        public final ModConfigSpec.IntValue memoryCacheMinutes;
        public final ModConfigSpec.IntValue diskCacheMegabytes;

        Client(ModConfigSpec.Builder builder)
        {
            builder.comment("Client-only options.").push("client");

            memoryCacheMinutes = builder
                    .comment("How long (in minutes) Dakimakura GPU textures stay in the client in-memory cache after last access.")
                    .defineInRange("memoryCacheMinutes", DMConfigValues.DEFAULT_CLIENT_MEMORY_CACHE_MINUTES, DMConfigValues.MIN_CLIENT_MEMORY_CACHE_MINUTES, DMConfigValues.MAX_CLIENT_MEMORY_CACHE_MINUTES);

            diskCacheMegabytes = builder
                    .comment("Soft cap on the client disk cache total size, in megabytes. 0 means unlimited.")
                    .defineInRange("diskCacheMegabytes", DMConfigValues.DEFAULT_CLIENT_DISK_CACHE_MEGABYTES, DMConfigValues.MIN_CLIENT_DISK_CACHE_MEGABYTES, DMConfigValues.MAX_CLIENT_DISK_CACHE_MEGABYTES);

            builder.pop();
        }
    }
}
