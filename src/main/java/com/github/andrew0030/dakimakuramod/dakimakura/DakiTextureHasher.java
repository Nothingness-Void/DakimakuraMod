package com.github.andrew0030.dakimakuramod.dakimakura;

import com.github.andrew0030.dakimakuramod.dakimakura.pack.IDakiPack;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class DakiTextureHasher
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String HASH_VERSION = "daki-texture-v3";
    private static final String[] VALID_FILE_EXT = {"png", "jpg", "jpeg"};
    private static final String DEFAULT_NAME_FRONT = "front";
    private static final String DEFAULT_NAME_BACK = "back";

    private DakiTextureHasher() {}

    public static String createHash(IDakiPack dakiPack, Daki daki)
    {
        String pathFront = resolveImagePath(dakiPack, daki, daki.getImageFront(), DEFAULT_NAME_FRONT);
        String pathBack = resolveImagePath(dakiPack, daki, daki.getImageBack(), DEFAULT_NAME_BACK);
        if (pathFront == null || pathBack == null)
            return null;

        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            updateString(digest, HASH_VERSION);
            updateString(digest, "smooth=" + daki.isSmooth());
            updateString(digest, "maxHeight=" + DakiImageOptimizer.getMaxTransferHeight());
            updateString(digest, "maxSourceBytes=" + DakiImageOptimizer.getMaxSourceBytes());
            updateString(digest, "jpegQuality=" + DakiImageOptimizer.getJpegQuality());
            updateString(digest, "front=" + pathFront);
            if (!updateResource(digest, dakiPack, pathFront))
                return null;
            updateString(digest, "back=" + pathBack);
            if (!pathFront.equals(pathBack) && !updateResource(digest, dakiPack, pathBack))
                return null;
            return toHex(digest.digest());
        }
        catch (NoSuchAlgorithmException e)
        {
            LOGGER.warn("Failed creating Dakimakura texture hash.", e);
            return null;
        }
    }

    private static boolean updateResource(MessageDigest digest, IDakiPack dakiPack, String path)
    {
        long resourceSize = dakiPack.getResourceSize(path);
        if (!DakiImageOptimizer.isSourceSizeAllowed(resourceSize, path))
            return false;

        byte[] data = dakiPack.getResource(path);
        if (data == null)
            return false;
        if (!DakiImageOptimizer.isSourceSizeAllowed(data.length, path))
            return false;
        digest.update(intToBytes(data.length));
        digest.update(data);
        return true;
    }

    /**
     * Mirrors {@code DakiImageData#findImagePath}: if the Daki explicitly specifies
     * an image filename we use it; otherwise we probe {@code front.{png,jpg,jpeg}}
     * or {@code back.{png,jpg,jpeg}} so that Dakis loaded without a
     * {@code daki-info.json} still get a stable texture hash (required for the
     * cross-session disk caches to work).
     */
    private static String resolveImagePath(IDakiPack dakiPack, Daki daki, String imageName, String defaultImageName)
    {
        if (imageName != null)
            return daki.getDakiDirectoryName() + "/" + imageName;
        if (dakiPack == null)
            return null;
        for (String ext : VALID_FILE_EXT)
        {
            String candidate = daki.getDakiDirectoryName() + "/" + defaultImageName + "." + ext;
            if (dakiPack.resourceExists(candidate))
                return candidate;
        }
        return null;
    }

    private static void updateString(MessageDigest digest, String value)
    {
        digest.update(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        digest.update((byte) 0);
    }

    private static byte[] intToBytes(int value)
    {
        return new byte[] {
                (byte) (value >>> 24),
                (byte) (value >>> 16),
                (byte) (value >>> 8),
                (byte) value
        };
    }

    private static String toHex(byte[] bytes)
    {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte value : bytes)
            builder.append(String.format("%02x", value));
        return builder.toString();
    }
}
