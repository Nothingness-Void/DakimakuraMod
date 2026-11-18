package com.github.andrew0030.dakimakuramod.dakimakura;

import com.github.andrew0030.dakimakuramod.dakimakura.pack.DakiPackFolder;
import com.github.andrew0030.dakimakuramod.dakimakura.pack.DakiPackZip;
import com.github.andrew0030.dakimakuramod.dakimakura.pack.IDakiPack;
import com.github.andrew0030.dakimakuramod.dakimakura.serialize.DakiJsonSerializer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared 256 KiB pack-list batching helper.
 * Prevents S2C custom-payload 1 MiB disconnects when thousands of dakis exist.
 */
public final class BatchPackListHelper
{
    public static final int MAX_BATCH_BYTES = 256 * 1024;

    private BatchPackListHelper() {}

    public record PackListBatch(boolean firstBatch, boolean lastBatch, ArrayList<IDakiPack> packs) {}

    public static List<PackListBatch> createBatches(List<IDakiPack> allPacks)
    {
        List<ArrayList<IDakiPack>> rawBatches = new ArrayList<>();
        ArrayList<IDakiPack> currentBatch = new ArrayList<>();
        int currentBytes = 16;

        for (IDakiPack sourcePack : allPacks)
        {
            boolean isFolder = sourcePack instanceof DakiPackFolder;
            String packName = sourcePack.getResourceName();
            int packHeaderBytes = 16 + utf8Length(packName);

            ArrayList<Daki> dakis = sourcePack.getDakisInPack();
            if (dakis.isEmpty())
            {
                if (!currentBatch.isEmpty() && currentBytes + packHeaderBytes > MAX_BATCH_BYTES)
                {
                    rawBatches.add(currentBatch);
                    currentBatch = new ArrayList<>();
                    currentBytes = 16;
                }
                currentBatch.add(createEmptyPack(isFolder, packName));
                currentBytes += packHeaderBytes;
                continue;
            }

            IDakiPack activePackChunk = null;
            for (Daki daki : dakis)
            {
                String dakiDir = daki.getDakiDirectoryName();
                String dakiJson = DakiJsonSerializer.serialize(daki).toString();
                int entryBytes = 16 + utf8Length(dakiDir) + utf8Length(dakiJson);
                int neededBytes = entryBytes + (activePackChunk == null ? packHeaderBytes : 0);

                if (!currentBatch.isEmpty() && currentBytes + neededBytes > MAX_BATCH_BYTES)
                {
                    rawBatches.add(currentBatch);
                    currentBatch = new ArrayList<>();
                    currentBytes = 16;
                    activePackChunk = null;
                }

                if (activePackChunk == null)
                {
                    activePackChunk = createEmptyPack(isFolder, packName);
                    currentBatch.add(activePackChunk);
                    currentBytes += packHeaderBytes;
                }

                activePackChunk.addDaki(daki);
                currentBytes += entryBytes;
            }
        }

        if (rawBatches.isEmpty() || !currentBatch.isEmpty())
            rawBatches.add(currentBatch);

        List<PackListBatch> batches = new ArrayList<>(rawBatches.size());
        for (int i = 0; i < rawBatches.size(); i++)
        {
            boolean first = (i == 0);
            boolean last = (i == rawBatches.size() - 1);
            batches.add(new PackListBatch(first, last, rawBatches.get(i)));
        }
        return batches;
    }

    public static IDakiPack createEmptyPack(boolean folder, String packName)
    {
        return folder ? new DakiPackFolder(packName) : new DakiPackZip(packName);
    }

    private static int utf8Length(String str)
    {
        return str == null ? 0 : str.getBytes(StandardCharsets.UTF_8).length;
    }
}
