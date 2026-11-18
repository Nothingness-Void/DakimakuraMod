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
import java.util.concurrent.ConcurrentHashMap;

/** Shared disk-cache helpers for both the client and server texture caches. */
public final class DakiDiskCacheUtil
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int FULL_SCAN_WRITE_INTERVAL = 64;
    private static final ConcurrentHashMap<Path, CacheFolderState> FOLDER_STATES = new ConcurrentHashMap<>();

    private DakiDiskCacheUtil() {}

    private static CacheFolderState getState(File folder)
    {
        Path key = folder.toPath().toAbsolutePath().normalize();
        return FOLDER_STATES.computeIfAbsent(key, k -> new CacheFolderState());
    }

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
        long previousSize = target.isFile() ? target.length() : 0L;
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
            if (parent != null)
            {
                CacheFolderState state = getState(parent);
                synchronized (state)
                {
                    if (state.initialized)
                    {
                        state.trackedBytes = Math.max(0L, state.trackedBytes - previousSize + data.length);
                    }
                    state.writesSinceScan++;
                }
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
     * Uses lightweight per-folder byte tracking and only performs a full directory
     * scan/eviction on first access, when {@code trackedBytes > maxBytes}, or every
     * {@value #FULL_SCAN_WRITE_INTERVAL} writes.
     *
     * @param maxBytes cap in bytes. Any value {@code <= 0} disables enforcement.
     */
    public static void enforceSizeLimit(File folder, long maxBytes)
    {
        if (folder == null || !folder.isDirectory() || maxBytes <= 0L)
            return;

        CacheFolderState state = getState(folder);
        synchronized (state)
        {
            if (state.initialized && state.trackedBytes <= maxBytes && state.writesSinceScan < FULL_SCAN_WRITE_INTERVAL)
                return;

            state.writesSinceScan = 0;
            File[] files = listFilesRecursively(folder);
            long totalBytes = 0L;
            for (File file : files)
                totalBytes += file.length();

            if (totalBytes > maxBytes && files.length > 0)
            {
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

            state.trackedBytes = Math.max(0L, totalBytes);
            state.initialized = true;
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

    private static final class CacheFolderState
    {
        boolean initialized;
        long trackedBytes;
        int writesSinceScan;
    }
}
