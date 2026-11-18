package com.github.andrew0030.dakimakuramod.dakimakura.client;

import com.github.andrew0030.dakimakuramod.DakimakuraModClientCommon;
import com.github.andrew0030.dakimakuramod.dakimakura.Daki;
import com.github.andrew0030.dakimakuramod.dakimakura.DakiImageData;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Client-only chunked texture reassembly handler.
 */
public final class ClientTextureReceiver
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final HashMap<Daki, PendingTextureTransfer> UNFINISHED_SKINS = new HashMap<>();
    private static final long TRANSFER_TIMEOUT_MS = 120_000L;

    private ClientTextureReceiver() {}

    public static void gotDakiTexturePartFromServer(String path, Daki daki, int sizeFront, int sizeBack, int packetsNeeded, int idx, byte[] data)
    {
        DakiTextureManagerClient clientManager = DakimakuraModClientCommon.getDakiTextureManager();
        if (daki == null)
        {
            if (clientManager != null)
                clientManager.serverSentTextures(path, null);
            return;
        }
        if (packetsNeeded <= 0 || idx < 0 || idx >= packetsNeeded)
            return;

        cleanupTransfers();

        if (packetsNeeded == 1)
        {
            try
            {
                finishTransfer(path, daki, sizeFront, sizeBack, data != null ? data : new byte[0]);
            }
            catch (RuntimeException e)
            {
                LOGGER.warn(String.format("Failed reading Dakimakura texture transfer for '%s'.", daki), e);
                if (clientManager != null)
                    clientManager.serverSentTextures(path, new DakiImageData(daki, null, null));
            }
            return;
        }

        PendingTextureTransfer transfer = UNFINISHED_SKINS.get(daki);
        if (transfer == null || !transfer.matches(sizeFront, sizeBack, packetsNeeded))
        {
            transfer = new PendingTextureTransfer(sizeFront, sizeBack, packetsNeeded);
            UNFINISHED_SKINS.put(daki, transfer);
        }

        transfer.add(idx, data != null ? data : new byte[0]);
        if (!transfer.isComplete())
            return;

        UNFINISHED_SKINS.remove(daki);
        try
        {
            finishTransfer(path, daki, sizeFront, sizeBack, transfer.assemble());
        }
        catch (RuntimeException e)
        {
            LOGGER.warn(String.format("Failed assembling Dakimakura texture transfer for '%s'.", daki), e);
            if (clientManager != null)
                clientManager.serverSentTextures(path, new DakiImageData(daki, null, null));
        }
    }

    private static void finishTransfer(String path, Daki daki, int sizeFront, int sizeBack, byte[] totalData)
    {
        if (totalData.length != sizeFront + sizeBack)
            throw new IllegalStateException("Dakimakura texture transfer size did not match header.");
        byte[] dataFront = (sizeFront > 0) ? Arrays.copyOfRange(totalData, 0, sizeFront) : null;
        byte[] dataBack = (sizeBack > 0) ? Arrays.copyOfRange(totalData, sizeFront, sizeFront + sizeBack) : null;

        DakiImageData imageData = new DakiImageData(daki, dataFront, dataBack);
        DakiTextureManagerClient clientManager = DakimakuraModClientCommon.getDakiTextureManager();
        if (clientManager != null)
            clientManager.serverSentTextures(path, imageData);
    }

    public static void clearPendingTransfers()
    {
        UNFINISHED_SKINS.clear();
    }

    private static void cleanupTransfers()
    {
        long now = System.currentTimeMillis();
        UNFINISHED_SKINS.entrySet().removeIf(entry -> now - entry.getValue().lastUpdate() > TRANSFER_TIMEOUT_MS);
    }

    private static final class PendingTextureTransfer
    {
        private final int sizeFront;
        private final int sizeBack;
        private final int packetsNeeded;
        private final byte[][] chunks;
        private int receivedCount;
        private long lastUpdate;

        private PendingTextureTransfer(int sizeFront, int sizeBack, int packetsNeeded)
        {
            this.sizeFront = sizeFront;
            this.sizeBack = sizeBack;
            this.packetsNeeded = packetsNeeded;
            this.chunks = new byte[packetsNeeded][];
            this.lastUpdate = System.currentTimeMillis();
        }

        private boolean matches(int sizeFront, int sizeBack, int packetsNeeded)
        {
            return this.sizeFront == sizeFront && this.sizeBack == sizeBack && this.packetsNeeded == packetsNeeded;
        }

        private void add(int idx, byte[] data)
        {
            this.lastUpdate = System.currentTimeMillis();
            if (this.chunks[idx] == null)
                this.receivedCount++;
            this.chunks[idx] = data;
        }

        private boolean isComplete()
        {
            return this.receivedCount == this.packetsNeeded;
        }

        private byte[] assemble()
        {
            byte[] totalData = new byte[this.sizeFront + this.sizeBack];
            int index = 0;
            for (byte[] chunk : this.chunks)
            {
                if (chunk == null)
                    throw new IllegalStateException("Attempted to assemble incomplete Dakimakura texture transfer.");
                if (index + chunk.length > totalData.length)
                    throw new IllegalStateException("Attempted to assemble oversized Dakimakura texture transfer.");
                System.arraycopy(chunk, 0, totalData, index, chunk.length);
                index += chunk.length;
            }
            return totalData;
        }

        private long lastUpdate()
        {
            return this.lastUpdate;
        }
    }
}
