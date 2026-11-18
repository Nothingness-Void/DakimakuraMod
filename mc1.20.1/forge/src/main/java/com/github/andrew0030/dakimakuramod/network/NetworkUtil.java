package com.github.andrew0030.dakimakuramod.network;

import com.github.andrew0030.dakimakuramod.DakimakuraModCommon;
import com.github.andrew0030.dakimakuramod.dakimakura.BatchPackListHelper;
import com.github.andrew0030.dakimakuramod.dakimakura.Daki;
import com.github.andrew0030.dakimakuramod.network.client.CommandClientMessage;
import com.github.andrew0030.dakimakuramod.network.client.SendDakiListClientMessage;
import com.github.andrew0030.dakimakuramod.network.client.SendTexturesClientMessage;
import com.github.andrew0030.dakimakuramod.network.server.RequestTexturesServerMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;

public class NetworkUtil
{
    public static void openDakiFolder(ServerPlayer serverPlayer)
    {
        DMNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new CommandClientMessage(CommandClientMessage.CommandType.OPEN_PACK_FOLDER));
    }

    public static void sendTextures(ServerPlayer serverPlayer, String path, Daki daki, int sizeFront, int sizeBack, int packetsNeeded, int idx, byte[] data)
    {
        DMNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new SendTexturesClientMessage(path, daki, sizeFront, sizeBack, packetsNeeded, idx, data));
    }

    public static void sendDakiList()
    {
        List<BatchPackListHelper.PackListBatch> batches = BatchPackListHelper.createBatches(DakimakuraModCommon.getDakimakuraManager().getDakiPacksList());
        for (BatchPackListHelper.PackListBatch batch : batches)
            DMNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new SendDakiListClientMessage(batch.firstBatch(), batch.lastBatch(), batch.packs()));
    }

    public static void sendDakiList(ServerPlayer serverPlayer)
    {
        List<BatchPackListHelper.PackListBatch> batches = BatchPackListHelper.createBatches(DakimakuraModCommon.getDakimakuraManager().getDakiPacksList());
        for (BatchPackListHelper.PackListBatch batch : batches)
            DMNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new SendDakiListClientMessage(batch.firstBatch(), batch.lastBatch(), batch.packs()));
    }

    public static void clientRequestTextures(Daki daki)
    {
        DMNetwork.CHANNEL.sendToServer(new RequestTexturesServerMessage(daki));
    }
}
