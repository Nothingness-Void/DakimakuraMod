package com.github.andrew0030.dakimakuramod.netwok;

import com.github.andrew0030.dakimakuramod.DakimakuraMod;
import com.github.andrew0030.dakimakuramod.netwok.client.CommandClientMessage;
import com.github.andrew0030.dakimakuramod.netwok.client.SendDakiListClientMessage;
import com.github.andrew0030.dakimakuramod.netwok.client.SendTexturesClientMessage;
import com.github.andrew0030.dakimakuramod.netwok.server.RequestTexturesServerMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;

public class DMNetwork
{
    public static final String NETWORK_PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = ChannelBuilder.named(ResourceLocation.fromNamespaceAndPath(DakimakuraMod.MODID, "net"))
            .networkProtocolVersion(Integer.parseInt(NETWORK_PROTOCOL))
            .simpleChannel();

    /**
     * Used to set up all the Messages
     */
    public static void registerMessages()
    {
        int id = 0;
        //Client Messages
        CHANNEL.messageBuilder(CommandClientMessage.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(CommandClientMessage::serialize)
                .decoder(CommandClientMessage::deserialize)
                .consumerMainThread(CommandClientMessage::handle)
                .add();
        CHANNEL.messageBuilder(SendTexturesClientMessage.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SendTexturesClientMessage::serialize)
                .decoder(SendTexturesClientMessage::deserialize)
                .consumerMainThread(SendTexturesClientMessage::handle)
                .add();
        CHANNEL.messageBuilder(SendDakiListClientMessage.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SendDakiListClientMessage::serialize)
                .decoder(SendDakiListClientMessage::deserialize)
                .consumerMainThread(SendDakiListClientMessage::handle)
                .add();
        //Server Messages
        CHANNEL.messageBuilder(RequestTexturesServerMessage.class, id, NetworkDirection.PLAY_TO_SERVER)
                .encoder(RequestTexturesServerMessage::serialize)
                .decoder(RequestTexturesServerMessage::deserialize)
                .consumerMainThread(RequestTexturesServerMessage::handle)
                .add();
        CHANNEL.build();
    }
}
