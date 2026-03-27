package com.github.andrew0030.dakimakuramod.netwok;

import com.github.andrew0030.dakimakuramod.dakimakura.Daki;
import com.github.andrew0030.dakimakuramod.netwok.client.CommandClientMessage;
import com.github.andrew0030.dakimakuramod.netwok.client.SendDakiListClientMessage;
import com.github.andrew0030.dakimakuramod.netwok.client.SendTexturesClientMessage;
import com.github.andrew0030.dakimakuramod.netwok.server.RequestTexturesServerMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

public class NetworkUtil
{
    public static void openDakiFolder(ServerPlayer serverPlayer)
    {
        DMNetwork.CHANNEL.send(new CommandClientMessage(CommandClientMessage.CommandType.OPEN_PACK_FOLDER), PacketDistributor.PLAYER.with(serverPlayer));
    }

    public static void sendTextures(ServerPlayer serverPlayer, Daki daki, int sizeFront, int sizeBack, int packetsNeeded, int idx, byte[] data)
    {
        DMNetwork.CHANNEL.send(new SendTexturesClientMessage(daki, sizeFront, sizeBack, packetsNeeded, idx, data), PacketDistributor.PLAYER.with(serverPlayer));
    }

    public static void sendDakiList()
    {
        DMNetwork.CHANNEL.send(new SendDakiListClientMessage(), PacketDistributor.ALL.noArg());
    }

    public static void sendDakiList(ServerPlayer serverPlayer)
    {
        DMNetwork.CHANNEL.send(new SendDakiListClientMessage(), PacketDistributor.PLAYER.with(serverPlayer));
    }

    public static void clientRequestTextures(Daki daki)
    {
        DMNetwork.CHANNEL.send(new RequestTexturesServerMessage(daki), PacketDistributor.SERVER.noArg());
    }
}
