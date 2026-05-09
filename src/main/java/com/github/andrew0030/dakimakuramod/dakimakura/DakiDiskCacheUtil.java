package com.github.andrew0030.dakimakuramod.dakimakura;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Comparator;

/** Shared disk-cache helpers for both the client and server texture caches. */
public final class DakiDiskCacheUtil
{
    private static final Logger LOGGER = LogUtils.getLogger();

    private DakiDiskCacheUtil() {}

    /**
     * Writes {@code data} to {@code target} atomically by writing to a sibling
     * {@code .tmp} file and renaming. Partial/corrupt files are never left
     * behind even if the JVM crashes mid-write.
     */
    public static boolean writeAtomic(File target, byte[] data)
    {
        File parent = target.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs())
        {
            LOGGER.warn("Failed creating cache folder '{}'.", parent.getAbsolutePath());
            return false;
        }
        Path finalPath = target.toPath();
        Path tmpPath = finalPath.resolveSibling(target.getName() + ".tmp");
        try
        {
            Files.write(tmpPath, data);
            try
            {
                Files.move(tmpPath, finalPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            }
            catch (AtomicMoveNotSupportedException ignored)
            {
                Files.move(tmpPath, finalPath, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        }
        catch (IOException e)
        {
            LOGGER.warn("Failed writing cache file '{}'.", target.getAbsolutePath(), e);
            try { Files.deleteIfExists(tmpPath); } catch (IOException ignored) {}
            return false;
        }
    }

    /**
     * Enforces a soft total-size cap on the given folder.
     * When the sum of regular-file sizes (recursively) exceeds {@code maxBytes},
     * the oldest files (by {@code lastModified}) are deleted until the folder
     * fits again. Files whose names end with {@code .tmp} are skipped.
     *
     * @param maxBytes cap in bytes. Any value {@code <= 0} disables enforcement.
     */
    public static void enforceSizeLimit(File folder, long maxBytes)
    {
        if (folder == null || !folder.isDirectory() || maxBytes <= 0L)
            return;

        File[] files = listFilesRecursively(folder);
        if (files.length == 0)
            return;

        long totalBytes = 0L;
        for (File file : files)
            totalBytes += file.length();

        if (totalBytes <= maxBytes)
            return;

        Arrays.sort(files, Comparator.comparingLong(File::lastModified));
        for (File file : files)
        {
            if (totalBytes <= maxBytes)
                break;
            long size = file.length();
            if (file.delete())
                totalBytes -= size;
            else
                LOGGER.warn("Failed evicting cache file '{}'.", file.getAbsolutePath());
        }
    }

    private static File[] listFilesRecursively(File folder)
    {
        java.util.ArrayList<File> out = new java.util.ArrayList<>();
        java.util.ArrayDeque<File> stack = new java.util.ArrayDeque<>();
        stack.push(folder);
        while (!stack.isEmpty())
        {
            File current = stack.pop();
            File[] children = current.listFiles();
            if (children == null)
                continue;
            for (File child : children)
            {
                if (child.isDirectory())
                    stack.push(child);
                else if (child.isFile() && !child.getName().endsWith(".tmp"))
                    out.add(child);
            }
        }
        return out.toArray(new File[0]);
    }
}
