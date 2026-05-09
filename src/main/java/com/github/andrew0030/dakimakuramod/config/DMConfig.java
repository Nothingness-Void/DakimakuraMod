package com.github.andrew0030.dakimakuramod.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Centralised config for Dakimakura Mod.
 * Values are accessed lazily (via {@code .get()}) after Forge loads the config,
 * so they must not be read during mod construction.
 */
public final class DMConfig
{
    public static final Common COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Client CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;

    static
    {
        Pair<Common, ForgeConfigSpec> commonPair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON = commonPair.getLeft();
        COMMON_SPEC = commonPair.getRight();

        Pair<Client, ForgeConfigSpec> clientPair = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT = clientPair.getLeft();
        CLIENT_SPEC = clientPair.getRight();
    }

    private DMConfig() {}

    public static final class Common
    {
        public final IntValue maxTransferHeight;
        public final DoubleValue jpegQuality;
        public final IntValue maxSourceMegabytes;
        public final IntValue memoryCacheMinutes;
        public final IntValue diskCacheMegabytes;
        public final BooleanValue serverDiskCacheEnabled;

        Common(ForgeConfigSpec.Builder builder)
        {
            builder.comment("Server / shared options. Applied when serving Dakimakura textures to clients.").push("server");

            maxTransferHeight = builder
                    .comment(
                            "Maximum pixel height for Dakimakura textures sent to clients.",
                            "Source images taller than this are downscaled before transfer.",
                            "Lower values reduce bandwidth and client VRAM. Width is derived from height (1:3 ratio)."
                    )
                    .defineInRange("maxTransferHeight", 1536, 256, 8192);

            jpegQuality = builder
                    .comment(
                            "JPEG re-encoding quality (0.0 - 1.0) for smoothed, opaque textures.",
                            "Lower values shrink transfers further at the cost of visual quality.",
                            "Images with transparency are always sent as PNG."
                    )
                    .defineInRange("jpegQuality", 0.80D, 0.10D, 1.0D);

            maxSourceMegabytes = builder
                    .comment("Maximum accepted source-image file size in megabytes. Larger files are skipped.")
                    .defineInRange("maxSourceMegabytes", 32, 1, 1024);

            memoryCacheMinutes = builder
                    .comment("How long (in minutes) optimized textures stay in the server in-memory cache after last access.")
                    .defineInRange("memoryCacheMinutes", 10, 1, 1440);

            serverDiskCacheEnabled = builder
                    .comment(
                            "If true, the server also stores optimized texture bytes on disk and reuses them across restarts.",
                            "Files are stored under <gameDir>/dakimakura-mod-server-cache/, keyed by the texture hash."
                    )
                    .define("diskCacheEnabled", true);

            diskCacheMegabytes = builder
                    .comment(
                            "Soft cap on the server disk cache total size, in megabytes.",
                            "0 means unlimited. Oldest files are evicted first when the cap is exceeded."
                    )
                    .defineInRange("diskCacheMegabytes", 1024, 0, 65536);

            builder.pop();
        }
    }

    public static final class Client
    {
        public final IntValue memoryCacheMinutes;
        public final IntValue diskCacheMegabytes;

        Client(ForgeConfigSpec.Builder builder)
        {
            builder.comment("Client-only options.").push("client");

            memoryCacheMinutes = builder
                    .comment("How long (in minutes) Dakimakura GPU textures stay in the client in-memory cache after last access.")
                    .defineInRange("memoryCacheMinutes", 20, 1, 1440);

            diskCacheMegabytes = builder
                    .comment(
                            "Soft cap on the client disk cache total size, in megabytes.",
                            "0 means unlimited. Oldest files are evicted first when the cap is exceeded.",
                            "Cached textures live under <gameDir>/dakimakura-mod-cache/."
                    )
                    .defineInRange("diskCacheMegabytes", 2048, 0, 65536);

            builder.pop();
        }
    }
}
