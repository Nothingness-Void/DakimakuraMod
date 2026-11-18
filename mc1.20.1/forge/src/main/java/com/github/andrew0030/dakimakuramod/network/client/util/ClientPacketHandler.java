package com.github.andrew0030.dakimakuramod.network.client.util;

import com.github.andrew0030.dakimakuramod.DakimakuraModCommon;
import com.github.andrew0030.dakimakuramod.dakimakura.client.ClientPackListReceiver;
import com.github.andrew0030.dakimakuramod.dakimakura.client.ClientTextureReceiver;
import com.github.andrew0030.dakimakuramod.network.client.CommandClientMessage;
import com.github.andrew0030.dakimakuramod.network.client.SendDakiListClientMessage;
import com.github.andrew0030.dakimakuramod.network.client.SendTexturesClientMessage;
import net.minecraft.Util;

public class ClientPacketHandler
{
    public static void handleCommandClient(CommandClientMessage msg)
    {
        if (msg.commandType() == CommandClientMessage.CommandType.OPEN_PACK_FOLDER)
            Util.getPlatform().openUri(DakimakuraModCommon.getDakimakuraManager().getPackFolder().toURI());
    }

    public static void handleSendTextures(SendTexturesClientMessage msg)
    {
        ClientTextureReceiver.gotDakiTexturePartFromServer(msg.path(), msg.daki(), msg.sizeFront(), msg.sizeBack(), msg.packetsNeeded(), msg.idx(), msg.data());
    }

    public static void handleSendDakiList(SendDakiListClientMessage msg)
    {
        ClientPackListReceiver.handleClientBatch(msg.firstBatch, msg.lastBatch, msg.packs);
    }
}
