package com.github.andrew0030.dakimakuramod.netwok;

import com.github.andrew0030.dakimakuramod.dakimakura.Daki;
import com.github.andrew0030.dakimakuramod.netwok.client.CommandClientMessage;
import com.github.andrew0030.dakimakuramod.netwok.client.SendDakiListClientMessage;
import com.github.andrew0030.dakimakuramod.netwok.client.SendTexturesClientMessage;
import com.github.andrew0030.dakimakuramod.netwok.server.RequestTexturesServerMessage;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public class NetworkUtil
{
    public static void openDakiFolder(ServerPlayer serverPlayer)
    {
        PacketDistributor.sendToPlayer(serverPlayer, new CommandClientMessage(CommandClientMessage.CommandType.OPEN_PACK_FOLDER));
    }

    public static void sendTextures(ServerPlayer serverPlayer, Daki daki, int sizeFront, int sizeBack, int packetsNeeded, int idx, byte[] data)
    {
        PacketDistributor.sendToPlayer(serverPlayer, new SendTexturesClientMessage(daki, sizeFront, sizeBack, packetsNeeded, idx, data));
    }

    public static void sendDakiList()
    {
        PacketDistributor.sendToAllPlayers(new SendDakiListClientMessage());
    }

    public static void sendDakiList(ServerPlayer serverPlayer)
    {
        PacketDistributor.sendToPlayer(serverPlayer, new SendDakiListClientMessage());
    }

    public static void clientRequestTextures(Daki daki)
    {
        PacketDistributor.sendToServer(new RequestTexturesServerMessage(daki));
    }
}
