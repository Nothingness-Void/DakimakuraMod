package com.github.andrew0030.dakimakuramod.network;

import com.github.andrew0030.dakimakuramod.DakimakuraModCommon;
import com.github.andrew0030.dakimakuramod.network.client.CommandClientMessage;
import com.github.andrew0030.dakimakuramod.network.client.SendDakiListClientMessage;
import com.github.andrew0030.dakimakuramod.network.client.SendTexturesClientMessage;
import com.github.andrew0030.dakimakuramod.network.server.RequestTexturesServerMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class DMNetwork
{
    public static final String NETWORK_PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(DakimakuraModCommon.MODID, "net"))
            .networkProtocolVersion(() -> NETWORK_PROTOCOL)
            .clientAcceptedVersions(NETWORK_PROTOCOL::equals)
            .serverAcceptedVersions(NETWORK_PROTOCOL::equals)
            .simpleChannel();

    public static void registerMessages()
    {
        int id = -1;
        CHANNEL.registerMessage(++id, CommandClientMessage.class, CommandClientMessage::serialize, CommandClientMessage::deserialize, CommandClientMessage::handle);
        CHANNEL.registerMessage(++id, SendTexturesClientMessage.class, SendTexturesClientMessage::serialize, SendTexturesClientMessage::deserialize, SendTexturesClientMessage::handle);
        CHANNEL.registerMessage(++id, SendDakiListClientMessage.class, SendDakiListClientMessage::serialize, SendDakiListClientMessage::deserialize, SendDakiListClientMessage::handle);
        CHANNEL.registerMessage(++id, RequestTexturesServerMessage.class, RequestTexturesServerMessage::serialize, RequestTexturesServerMessage::deserialize, RequestTexturesServerMessage::handle);
    }
}
