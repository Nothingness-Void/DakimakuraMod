package com.github.andrew0030.dakimakuramod.network;

import com.github.andrew0030.dakimakuramod.DakimakuraModCommon;
import com.github.andrew0030.dakimakuramod.dakimakura.BatchPackListHelper;
import com.github.andrew0030.dakimakuramod.dakimakura.Daki;
import com.github.andrew0030.dakimakuramod.dakimakura.client.ClientPackListReceiver;
import com.github.andrew0030.dakimakuramod.dakimakura.client.ClientTextureReceiver;
import com.github.andrew0030.dakimakuramod.dakimakura.pack.DakiPackFolder;
import com.github.andrew0030.dakimakuramod.dakimakura.pack.IDakiPack;
import com.github.andrew0030.dakimakuramod.dakimakura.serialize.DakiJsonSerializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public final class FabricNetwork
{
    public static final ResourceLocation COMMAND_CHANNEL = new ResourceLocation(DakimakuraModCommon.MODID, "command");
    public static final ResourceLocation SEND_TEXTURES_CHANNEL = new ResourceLocation(DakimakuraModCommon.MODID, "send_textures");
    public static final ResourceLocation SEND_DAKI_LIST_CHANNEL = new ResourceLocation(DakimakuraModCommon.MODID, "send_daki_list");
    public static final ResourceLocation REQUEST_TEXTURES_CHANNEL = new ResourceLocation(DakimakuraModCommon.MODID, "request_textures");

    private static volatile MinecraftServer activeServer;

    private FabricNetwork() {}

    public static void setActiveServer(MinecraftServer server)
    {
        activeServer = server;
    }

    public static void registerServerReceivers()
    {
        ServerPlayNetworking.registerGlobalReceiver(REQUEST_TEXTURES_CHANNEL, (server, player, handler, buf, responseSender) -> {
            String path = buf.readUtf(Short.MAX_VALUE);
            server.execute(() -> {
                String[] split = path.split(":", 2);
                Daki daki = (split.length == 2 && DakimakuraModCommon.getDakimakuraManager() != null)
                        ? DakimakuraModCommon.getDakimakuraManager().getDakiFromMap(split[0], split[1])
                        : null;
                if (daki != null)
                    DakimakuraModCommon.getTextureManagerCommon().onClientRequestTexture(player, daki);
                else
                    sendTextures(player, path, 0, 0, 1, 0, null);
            });
        });
    }

    public static void registerClientReceivers()
    {
        ClientPlayNetworking.registerGlobalReceiver(COMMAND_CHANNEL, (client, handler, buf, responseSender) -> {
            byte cmdOrdinal = buf.readByte();
            client.execute(() -> {
                if (cmdOrdinal == 0 && DakimakuraModCommon.getDakimakuraManager() != null)
                    Util.getPlatform().openUri(DakimakuraModCommon.getDakimakuraManager().getPackFolder().toURI());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(SEND_TEXTURES_CHANNEL, (client, handler, buf, responseSender) -> {
            String path = buf.readUtf(Short.MAX_VALUE);
            int sizeFront = buf.readInt();
            int sizeBack = buf.readInt();
            int packetsNeeded = buf.readInt();
            int idx = buf.readInt();
            byte[] data = new byte[0];
            if (buf.readBoolean())
            {
                int size = buf.readInt();
                if (size < 0 || size > Short.MAX_VALUE)
                    throw new IllegalArgumentException("Invalid Dakimakura texture packet size: " + size);
                data = new byte[size];
                buf.readBytes(data);
            }
            byte[] finalData = data;
            client.execute(() -> {
                String[] split = path.split(":", 2);
                Daki daki = (split.length == 2 && DakimakuraModCommon.getDakimakuraManager() != null)
                        ? DakimakuraModCommon.getDakimakuraManager().getDakiFromMap(split[0], split[1])
                        : null;
                ClientTextureReceiver.gotDakiTexturePartFromServer(path, daki, sizeFront, sizeBack, packetsNeeded, idx, finalData);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(SEND_DAKI_LIST_CHANNEL, (client, handler, buf, responseSender) -> {
            boolean firstBatch = buf.readBoolean();
            boolean lastBatch = buf.readBoolean();
            int packCount = buf.readInt();
            ArrayList<IDakiPack> packs = new ArrayList<>(packCount);
            for (int i = 0; i < packCount; i++)
            {
                boolean folder = buf.readBoolean();
                String packName = buf.readUtf(Short.MAX_VALUE);
                IDakiPack dakiPack = BatchPackListHelper.createEmptyPack(folder, packName);
                int dakiCount = buf.readInt();
                for (int j = 0; j < dakiCount; j++)
                {
                    String path = buf.readUtf(Short.MAX_VALUE);
                    String dakiJson = buf.readUtf(Short.MAX_VALUE);
                    Daki daki = DakiJsonSerializer.deserialize(dakiJson, dakiPack.getResourceName(), path);
                    dakiPack.addDaki(daki);
                }
                packs.add(dakiPack);
            }
            client.execute(() -> ClientPackListReceiver.handleClientBatch(firstBatch, lastBatch, packs));
        });
    }

    public static void openDakiFolder(ServerPlayer player)
    {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeByte(0);
        ServerPlayNetworking.send(player, COMMAND_CHANNEL, buf);
    }

    public static void sendTextures(ServerPlayer player, String path, int sizeFront, int sizeBack, int packetsNeeded, int idx, byte[] data)
    {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeUtf(path != null ? path : "");
        buf.writeInt(sizeFront);
        buf.writeInt(sizeBack);
        buf.writeInt(packetsNeeded);
        buf.writeInt(idx);
        buf.writeBoolean(data != null);
        if (data != null)
        {
            buf.writeInt(data.length);
            buf.writeBytes(data);
        }
        ServerPlayNetworking.send(player, SEND_TEXTURES_CHANNEL, buf);
    }

    public static void sendDakiListToAll()
    {
        MinecraftServer server = activeServer;
        if (server == null || DakimakuraModCommon.getDakimakuraManager() == null)
            return;
        List<BatchPackListHelper.PackListBatch> batches = BatchPackListHelper.createBatches(DakimakuraModCommon.getDakimakuraManager().getDakiPacksList());
        for (ServerPlayer player : PlayerLookup.all(server))
            for (BatchPackListHelper.PackListBatch batch : batches)
                sendDakiListBatch(player, batch);
    }

    public static void sendDakiList(ServerPlayer player)
    {
        if (DakimakuraModCommon.getDakimakuraManager() == null)
            return;
        List<BatchPackListHelper.PackListBatch> batches = BatchPackListHelper.createBatches(DakimakuraModCommon.getDakimakuraManager().getDakiPacksList());
        for (BatchPackListHelper.PackListBatch batch : batches)
            sendDakiListBatch(player, batch);
    }

    private static void sendDakiListBatch(ServerPlayer player, BatchPackListHelper.PackListBatch batch)
    {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(batch.firstBatch());
        buf.writeBoolean(batch.lastBatch());
        buf.writeInt(batch.packs().size());
        for (IDakiPack dakiPack : batch.packs())
        {
            buf.writeBoolean(dakiPack instanceof DakiPackFolder);
            buf.writeUtf(dakiPack.getResourceName());
            ArrayList<Daki> dakis = dakiPack.getDakisInPack();
            buf.writeInt(dakis.size());
            for (Daki daki : dakis)
            {
                buf.writeUtf(daki.getDakiDirectoryName());
                buf.writeUtf(DakiJsonSerializer.serialize(daki).toString());
            }
        }
        ServerPlayNetworking.send(player, SEND_DAKI_LIST_CHANNEL, buf);
    }

    public static void clientRequestTextures(Daki daki)
    {
        if (daki == null)
            return;
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeUtf(daki.getPackDirectoryName() + ":" + daki.getDakiDirectoryName());
        ClientPlayNetworking.send(REQUEST_TEXTURES_CHANNEL, buf);
    }
}
