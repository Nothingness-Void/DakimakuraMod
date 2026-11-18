package com.github.andrew0030.dakimakuramod.config;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Lightweight TOML config reader/writer used by Fabric (and any loader without ForgeConfigSpec).
 * Produces files identical in layout to {@code dakimakuramod-common.toml} and {@code dakimakuramod-client.toml}.
 */
public final class DMTomlConfig
{
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String COMMON_FILE_NAME = "dakimakuramod-common.toml";
    public static final String CLIENT_FILE_NAME = "dakimakuramod-client.toml";

    private DMTomlConfig() {}

    public static void loadCommon(Path configDir)
    {
        if (configDir == null)
            return;
        Path file = configDir.resolve(COMMON_FILE_NAME);
        if (!Files.exists(file))
            writeDefault(file, defaultCommonToml());

        Map<String, String> values = parseSection(file, "server");
        DMConfigValues.setMaxTransferHeight(parseInt(values.get("maxTransferHeight"), DMConfigValues.DEFAULT_MAX_TRANSFER_HEIGHT));
        DMConfigValues.setJpegQuality(parseDouble(values.get("jpegQuality"), DMConfigValues.DEFAULT_JPEG_QUALITY));
        DMConfigValues.setMaxSourceMegabytes(parseInt(values.get("maxSourceMegabytes"), DMConfigValues.DEFAULT_MAX_SOURCE_MEGABYTES));
        DMConfigValues.setServerMemoryCacheMinutes(parseInt(values.get("memoryCacheMinutes"), DMConfigValues.DEFAULT_SERVER_MEMORY_CACHE_MINUTES));
        DMConfigValues.setServerDiskCacheEnabled(parseBoolean(values.get("diskCacheEnabled"), DMConfigValues.DEFAULT_SERVER_DISK_CACHE_ENABLED));
        DMConfigValues.setServerDiskCacheMegabytes(parseInt(values.get("diskCacheMegabytes"), DMConfigValues.DEFAULT_SERVER_DISK_CACHE_MEGABYTES));
    }

    public static void loadClient(Path configDir)
    {
        if (configDir == null)
            return;
        Path file = configDir.resolve(CLIENT_FILE_NAME);
        if (!Files.exists(file))
            writeDefault(file, defaultClientToml());

        Map<String, String> values = parseSection(file, "client");
        DMConfigValues.setClientMemoryCacheMinutes(parseInt(values.get("memoryCacheMinutes"), DMConfigValues.DEFAULT_CLIENT_MEMORY_CACHE_MINUTES));
        DMConfigValues.setClientDiskCacheMegabytes(parseInt(values.get("diskCacheMegabytes"), DMConfigValues.DEFAULT_CLIENT_DISK_CACHE_MEGABYTES));
    }

    private static void writeDefault(Path file, String content)
    {
        try
        {
            Path parent = file.getParent();
            if (parent != null)
                Files.createDirectories(parent);
            Files.writeString(file, content, StandardCharsets.UTF_8);
        }
        catch (IOException e)
        {
            LOGGER.warn("Failed writing default config '{}'.", file, e);
        }
    }

    private static Map<String, String> parseSection(Path file, String targetSection)
    {
        Map<String, String> result = new HashMap<>();
        if (!Files.isRegularFile(file))
            return result;

        try
        {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            String currentSection = "";
            for (String rawLine : lines)
            {
                String line = stripComment(rawLine).trim();
                if (line.isEmpty())
                    continue;
                if (line.startsWith("[") && line.endsWith("]"))
                {
                    currentSection = line.substring(1, line.length() - 1).trim();
                    continue;
                }
                if (!currentSection.equals(targetSection))
                    continue;
                int eq = line.indexOf('=');
                if (eq <= 0)
                    continue;
                String key = line.substring(0, eq).trim();
                String val = line.substring(eq + 1).trim();
                if (val.length() >= 2 && ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))))
                    val = val.substring(1, val.length() - 1);
                result.put(key, val);
            }
        }
        catch (IOException e)
        {
            LOGGER.warn("Failed reading config '{}'.", file, e);
        }
        return result;
    }

    private static String stripComment(String line)
    {
        boolean inQuotes = false;
        char quoteChar = 0;
        for (int i = 0; i < line.length(); i++)
        {
            char c = line.charAt(i);
            if (inQuotes)
            {
                if (c == quoteChar)
                    inQuotes = false;
            }
            else if (c == '"' || c == '\'')
            {
                inQuotes = true;
                quoteChar = c;
            }
            else if (c == '#')
            {
                return line.substring(0, i);
            }
        }
        return line;
    }

    private static int parseInt(String raw, int fallback)
    {
        if (raw == null || raw.isEmpty())
            return fallback;
        try
        {
            return Integer.parseInt(raw);
        }
        catch (NumberFormatException e)
        {
            return fallback;
        }
    }

    private static double parseDouble(String raw, double fallback)
    {
        if (raw == null || raw.isEmpty())
            return fallback;
        try
        {
            return Double.parseDouble(raw);
        }
        catch (NumberFormatException e)
        {
            return fallback;
        }
    }

    private static boolean parseBoolean(String raw, boolean fallback)
    {
        if (raw == null || raw.isEmpty())
            return fallback;
        String lower = raw.toLowerCase(Locale.ROOT);
        if ("true".equals(lower))
            return true;
        if ("false".equals(lower))
            return false;
        return fallback;
    }

    private static String defaultCommonToml()
    {
        return String.join("\n",
                "# Server / shared options. Applied when serving Dakimakura textures to clients.",
                "[server]",
                "    # Maximum pixel height for Dakimakura textures sent to clients.",
                "    # Source images taller than this are downscaled before transfer.",
                "    # Lower values reduce bandwidth and client VRAM. Width is derived from height (1:3 ratio).",
                "    # Range: 256 ~ 8192",
                "    maxTransferHeight = 1536",
                "    # JPEG re-encoding quality (0.0 - 1.0) for smoothed, opaque textures.",
                "    # Lower values shrink transfers further at the cost of visual quality.",
                "    # Images with transparency are always sent as PNG.",
                "    # Range: 0.10 ~ 1.0",
                "    jpegQuality = 0.8",
                "    # Maximum accepted source-image file size in megabytes. Larger files are skipped.",
                "    # Range: 1 ~ 1024",
                "    maxSourceMegabytes = 32",
                "    # How long (in minutes) optimized textures stay in the server in-memory cache after last access.",
                "    # Range: 1 ~ 1440",
                "    memoryCacheMinutes = 10",
                "    # If true, the server also stores optimized texture bytes on disk and reuses them across restarts.",
                "    # Files are stored under <gameDir>/dakimakura-mod-server-cache/, keyed by the texture hash.",
                "    diskCacheEnabled = true",
                "    # Soft cap on the server disk cache total size, in megabytes.",
                "    # 0 means unlimited. Oldest files are evicted first when the cap is exceeded.",
                "    # Range: 0 ~ 65536",
                "    diskCacheMegabytes = 1024",
                ""
        );
    }

    private static String defaultClientToml()
    {
        return String.join("\n",
                "# Client-only options.",
                "[client]",
                "    # How long (in minutes) Dakimakura GPU textures stay in the client in-memory cache after last access.",
                "    # Range: 1 ~ 1440",
                "    memoryCacheMinutes = 20",
                "    # Soft cap on the client disk cache total size, in megabytes.",
                "    # 0 means unlimited. Oldest files are evicted first when the cap is exceeded.",
                "    # Cached textures live under <gameDir>/dakimakura-mod-cache/.",
                "    # Range: 0 ~ 65536",
                "    diskCacheMegabytes = 2048",
                ""
        );
    }
}
