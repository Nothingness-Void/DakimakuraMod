package com.github.andrew0030.dakimakuramod.dakimakura;

import com.github.andrew0030.dakimakuramod.platform.Services;
import net.minecraft.server.level.ServerPlayer;

/**
 * Server-side chunked texture sender.
 */
public final class DakiSendHelper
{
    private static final int MAX_PACKET_SIZE = Short.MAX_VALUE;

    private DakiSendHelper() {}

    /**
     * Called on <strong>Server Side</strong> to send {@link Daki} texture parts.
     */
    public static void sendDakiTexturesToClient(ServerPlayer serverPlayer, Daki daki, DakiImageData imageData)
    {
        int sizeFront = imageData.getTextureFront() != null ? imageData.getTextureFront().length : 0;
        int sizeBack = imageData.getTextureBack() != null ? imageData.getTextureBack().length : 0;
        String path = daki != null ? daki.getPackDirectoryName() + ":" + daki.getDakiDirectoryName() : "";

        if (sizeFront == 0 && sizeBack == 0)
        {
            Services.getPlatform().sendTexturesChunk(serverPlayer, path, daki, sizeFront, sizeBack, 1, 0, null);
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
            Services.getPlatform().sendTexturesChunk(serverPlayer, path, daki, sizeFront, sizeBack, packetsNeeded, i, messageData);

            bytesSent += packetSize;
        }
    }
}
