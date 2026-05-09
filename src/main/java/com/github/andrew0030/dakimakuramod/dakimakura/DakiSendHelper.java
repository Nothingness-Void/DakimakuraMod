package com.github.andrew0030.dakimakuramod.dakimakura;

import com.github.andrew0030.dakimakuramod.DakimakuraModClient;
import com.github.andrew0030.dakimakuramod.dakimakura.client.DakiTextureDiskCache;
import com.github.andrew0030.dakimakuramod.netwok.NetworkUtil;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.HashMap;

public class DakiSendHelper
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final HashMap<Daki, PendingTextureTransfer> unfinishedSkins = new HashMap<>();
    private static final int MAX_PACKET_SIZE = Short.MAX_VALUE;
    private static final long TRANSFER_TIMEOUT_MS = 120_000L;

    /**
     * Called on <strong>Server Side</strong> to send {@link Daki} texture parts.
     * @param serverPlayer The player that will receive the data
     * @param daki The {@link Daki} the texture part belongs to
     * @param imageData The {@link DakiImageData} that will be used to load the texture data
     */
    public static void sendDakiTexturesToClient(ServerPlayer serverPlayer, Daki daki, DakiImageData imageData)
    {
        int sizeFront = imageData.getTextureFront() != null ? imageData.getTextureFront().length : 0;
        int sizeBack = imageData.getTextureBack() != null ? imageData.getTextureBack().length : 0;

        if (sizeFront == 0 && sizeBack == 0)
        {
            NetworkUtil.sendTextures(serverPlayer, daki, sizeFront, sizeBack, 1, 0, null);
            return;
        }

        byte[] totalBytes = new byte[sizeFront + sizeBack];
        if (sizeFront > 0)
            System.arraycopy(imageData.getTextureFront(), 0, totalBytes, 0, sizeFront);
        if (sizeBack > 0)
            System.arraycopy(imageData.getTextureBack(), 0, totalBytes, sizeFront, sizeBack);


        int packetsNeeded = (int) Math.ceil((double) totalBytes.length / MAX_PACKET_SIZE);
        int bytesSent = 0;

        for (int i = 0; i < packetsNeeded; i++)
        {
            boolean lastPacket = (i == packetsNeeded - 1);
            int packetSize = lastPacket ? (totalBytes.length - bytesSent) : MAX_PACKET_SIZE;
            byte[] messageData = new byte[packetSize];

            System.arraycopy(totalBytes, bytesSent, messageData, 0, packetSize);
            NetworkUtil.sendTextures(serverPlayer, daki, sizeFront, sizeBack, packetsNeeded, i, messageData);

            bytesSent += packetSize;
        }
    }

    /**
     * Called on <strong>Client Side</strong> to receive {@link Daki} texture parts.
     * @param daki The {@link Daki} the texture part belongs to
     * @param sizeFront The size of the Front Texture byte array
     * @param sizeBack The size of the Back texture byte array
     * @param packetsNeeded The total amount of packets required to create the {@link DakiImageData}
     * @param idx The index of the current packet, used to ensure data is processed in the correct order
     * @param data The received data in bytes
     */
    public static void gotDakiTexturePartFromServer(Daki daki, int sizeFront, int sizeBack, int packetsNeeded, int idx, byte[] data)
    {
        if (daki == null || packetsNeeded <= 0 || idx < 0 || idx >= packetsNeeded)
            return;

        DakiSendHelper.cleanupTransfers();

        if (packetsNeeded == 1)
        {
            try
            {
                DakiSendHelper.finishTransfer(daki, sizeFront, sizeBack, data != null ? data : new byte[0]);
            }
            catch (RuntimeException e)
            {
                LOGGER.warn(String.format("Failed reading Dakimakura texture transfer for '%s'.", daki), e);
                DakimakuraModClient.getDakiTextureManager().serverSentTextures(new DakiImageData(daki, null, null));
            }
            return;
        }

        PendingTextureTransfer transfer = DakiSendHelper.unfinishedSkins.get(daki);
        if (transfer == null || !transfer.matches(sizeFront, sizeBack, packetsNeeded))
        {
            transfer = new PendingTextureTransfer(sizeFront, sizeBack, packetsNeeded);
            DakiSendHelper.unfinishedSkins.put(daki, transfer);
        }

        transfer.add(idx, data != null ? data : new byte[0]);
        if (!transfer.isComplete())
            return;

        DakiSendHelper.unfinishedSkins.remove(daki);
        try
        {
            DakiSendHelper.finishTransfer(daki, sizeFront, sizeBack, transfer.assemble());
        }
        catch (RuntimeException e)
        {
            LOGGER.warn(String.format("Failed assembling Dakimakura texture transfer for '%s'.", daki), e);
            DakimakuraModClient.getDakiTextureManager().serverSentTextures(new DakiImageData(daki, null, null));
        }
    }

    private static void finishTransfer(Daki daki, int sizeFront, int sizeBack, byte[] totalData)
    {
        if (totalData.length != sizeFront + sizeBack)
            throw new IllegalStateException("Dakimakura texture transfer size did not match header.");
        byte[] dataFront = (sizeFront > 0) ? Arrays.copyOfRange(totalData, 0, sizeFront) : null;
        byte[] dataBack = (sizeBack > 0) ? Arrays.copyOfRange(totalData, sizeFront, sizeFront + sizeBack) : null;

        DakiImageData imageData = new DakiImageData(daki, dataFront, dataBack);
        DakiTextureDiskCache.save(imageData);
        DakimakuraModClient.getDakiTextureManager().serverSentTextures(imageData);
    }

    private static void cleanupTransfers()
    {
        long now = System.currentTimeMillis();
        DakiSendHelper.unfinishedSkins.entrySet().removeIf(entry -> now - entry.getValue().lastUpdate() > TRANSFER_TIMEOUT_MS);
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
