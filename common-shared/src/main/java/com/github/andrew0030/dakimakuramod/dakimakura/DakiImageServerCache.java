package com.github.andrew0030.dakimakuramod.dakimakura;

import com.github.andrew0030.dakimakuramod.DakimakuraModCommon;
import com.github.andrew0030.dakimakuramod.config.DMConfigValues;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Pattern;

/**
 * Persistent on-disk cache of the optimized texture bytes on the server.
 */
public final class DakiImageServerCache
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String CACHE_FOLDER_NAME = "dakimakura-mod-server-cache";
    private static final String FRONT_SUFFIX = ".front";
    private static final String BACK_SUFFIX = ".back";
    private static final Pattern SAFE_HASH = Pattern.compile("[0-9a-f]{64}");

    private DakiImageServerCache() {}

    public static boolean isEnabled()
    {
        return DMConfigValues.isServerDiskCacheEnabled();
    }

    /** Attempts to load cached optimized bytes for the given Daki. */
    public static byte[][] load(Daki daki)
    {
        if (!isEnabled() || daki == null || !isUsableHash(daki.getTextureHash()))
            return null;

        File frontFile = getCacheFile(daki.getTextureHash(), FRONT_SUFFIX);
        File backFile = getCacheFile(daki.getTextureHash(), BACK_SUFFIX);
        if (frontFile == null || backFile == null || !frontFile.isFile() || !backFile.isFile())
            return null;

        try
        {
            byte[] front = Files.readAllBytes(frontFile.toPath());
            byte[] back = Files.readAllBytes(backFile.toPath());
            long now = System.currentTimeMillis();
            if (Math.abs(now - frontFile.lastModified()) > 3_600_000L)
                if (!frontFile.setLastModified(now)) { /* non-fatal */ }
            if (Math.abs(now - backFile.lastModified()) > 3_600_000L)
                if (!backFile.setLastModified(now)) { /* non-fatal */ }
            return new byte[][] { front, back };
        }
        catch (IOException e)
        {
            LOGGER.warn("Failed reading server-side cached texture '{}'.", daki.getTextureHash(), e);
            return null;
        }
    }

    /** Persists optimized bytes for the given Daki. Safe to call with nulls. */
    public static void save(Daki daki, byte[] front, byte[] back)
    {
        if (!isEnabled() || daki == null || front == null || back == null || !isUsableHash(daki.getTextureHash()))
            return;

        File cacheFolder = getCacheFolder();
        if (cacheFolder == null)
            return;

        File frontFile = getCacheFile(daki.getTextureHash(), FRONT_SUFFIX);
        File backFile = getCacheFile(daki.getTextureHash(), BACK_SUFFIX);
        if (frontFile == null || backFile == null)
            return;

        boolean frontWritten = DakiDiskCacheUtil.writeAtomic(frontFile, front);
        boolean backWritten = DakiDiskCacheUtil.writeAtomic(backFile, back);
        if (!frontWritten || !backWritten)
            return;

        long maxBytes = (long) DMConfigValues.getServerDiskCacheMegabytes() * 1024L * 1024L;
        DakiDiskCacheUtil.enforceSizeLimit(cacheFolder, maxBytes);
    }

    private static boolean isUsableHash(String textureHash)
    {
        return textureHash != null && SAFE_HASH.matcher(textureHash).matches();
    }

    private static File getCacheFile(String textureHash, String suffix)
    {
        File folder = getCacheFolder();
        return folder != null ? new File(folder, textureHash + suffix) : null;
    }

    private static File getCacheFolder()
    {
        DakiManager manager = DakimakuraModCommon.getDakimakuraManager();
        if (manager == null)
            return null;
        File packFolder = manager.getPackFolder();
        if (packFolder == null)
            return null;
        File parent = packFolder.getParentFile();
        return parent != null ? new File(parent, CACHE_FOLDER_NAME) : null;
    }
}
