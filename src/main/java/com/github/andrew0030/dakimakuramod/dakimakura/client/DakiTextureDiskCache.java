package com.github.andrew0030.dakimakuramod.dakimakura.client;

import com.github.andrew0030.dakimakuramod.DakimakuraMod;
import com.github.andrew0030.dakimakuramod.config.DMConfig;
import com.github.andrew0030.dakimakuramod.dakimakura.Daki;
import com.github.andrew0030.dakimakuramod.dakimakura.DakiDiskCacheUtil;
import com.github.andrew0030.dakimakuramod.dakimakura.DakiImageData;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Pattern;

public final class DakiTextureDiskCache
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Pattern SAFE_HASH = Pattern.compile("[0-9a-f]{64}");
    private static final String FRONT_SUFFIX = ".front";
    private static final String BACK_SUFFIX = ".back";

    private DakiTextureDiskCache() {}

    public static DakiImageData load(Daki daki)
    {
        if (daki == null || !isUsableHash(daki.getTextureHash()))
            return null;

        File frontFile = getCacheFile(daki.getTextureHash(), FRONT_SUFFIX);
        File backFile = getCacheFile(daki.getTextureHash(), BACK_SUFFIX);
        if (frontFile == null || backFile == null || !frontFile.isFile() || !backFile.isFile())
            return null;

        try
        {
            DakiImageData imageData = new DakiImageData(daki, Files.readAllBytes(frontFile.toPath()), Files.readAllBytes(backFile.toPath()));
            // Touch lastModified so LRU keeps the hot entries around.
            long now = System.currentTimeMillis();
            if (!frontFile.setLastModified(now)) { /* non-fatal */ }
            if (!backFile.setLastModified(now)) { /* non-fatal */ }
            return imageData;
        }
        catch (IOException e)
        {
            LOGGER.warn(String.format("Failed reading cached Dakimakura texture '%s'.", daki.getTextureHash()), e);
            return null;
        }
    }

    public static void save(DakiImageData imageData)
    {
        Daki daki = imageData.getDaki();
        if (daki == null || !isUsableHash(daki.getTextureHash()) || imageData.getTextureFront() == null || imageData.getTextureBack() == null)
            return;

        File cacheFolder = getCacheFolder();
        if (cacheFolder == null)
            return;

        File frontFile = getCacheFile(daki.getTextureHash(), FRONT_SUFFIX);
        File backFile = getCacheFile(daki.getTextureHash(), BACK_SUFFIX);
        if (frontFile == null || backFile == null)
            return;

        boolean frontWritten = DakiDiskCacheUtil.writeAtomic(frontFile, imageData.getTextureFront());
        boolean backWritten = DakiDiskCacheUtil.writeAtomic(backFile, imageData.getTextureBack());
        if (!frontWritten || !backWritten)
            return;

        enforceSizeLimit();
    }

    /** Called once at client startup to bound the on-disk cache size. */
    public static void initialise()
    {
        File cacheFolder = getCacheFolder();
        if (cacheFolder == null || !cacheFolder.isDirectory())
            return;
        // Remove any stale .tmp left over from a previous crash.
        File[] files = cacheFolder.listFiles();
        if (files != null)
        {
            for (File file : files)
                if (file.isFile() && file.getName().endsWith(".tmp"))
                    if (!file.delete())
                        LOGGER.warn("Failed removing stale temp file '{}'.", file.getAbsolutePath());
        }
        enforceSizeLimit();
    }

    private static void enforceSizeLimit()
    {
        if (!DMConfig.CLIENT_SPEC.isLoaded())
            return;
        File cacheFolder = getCacheFolder();
        if (cacheFolder == null)
            return;
        long maxBytes = (long) DMConfig.CLIENT.diskCacheMegabytes.get() * 1024L * 1024L;
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
        if (DakimakuraMod.getDakimakuraManager() == null)
            return null;
        File packFolder = DakimakuraMod.getDakimakuraManager().getPackFolder();
        if (packFolder == null)
            return null;
        File parent = packFolder.getParentFile();
        return parent != null ? new File(parent, "dakimakura-mod-cache") : null;
    }
}
