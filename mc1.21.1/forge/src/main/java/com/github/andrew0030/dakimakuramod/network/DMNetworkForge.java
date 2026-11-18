package com.github.andrew0030.dakimakuramod.network;

import com.github.andrew0030.dakimakuramod.DakimakuraModCommon;
import com.github.andrew0030.dakimakuramod.platform.Services;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;

public final class DMNetworkForge
{
    public static final int NETWORK_PROTOCOL = 1;

    public static final SimpleChannel CHANNEL = ChannelBuilder.named(Services.getPlatform().id(DakimakuraModCommon.MODID, "net"))
            .networkProtocolVersion(NETWORK_PROTOCOL)
            .simpleChannel();

    private DMNetworkForge() {}

    public static void registerMessages()
    {
        int id = 0;

        CHANNEL.messageBuilder(CommandClientPayload.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(CommandClientPayload::write)
                .decoder(CommandClientPayload::read)
                .consumerMainThread(DMNetworkForge::handleCommandClient)
                .add();

        CHANNEL.messageBuilder(SendTexturesPayload.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SendTexturesPayload::write)
                .decoder(SendTexturesPayload::read)
                .consumerMainThread(DMNetworkForge::handleSendTextures)
                .add();

        CHANNEL.messageBuilder(SendDakiListPayload.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SendDakiListPayload::write)
                .decoder(SendDakiListPayload::read)
                .consumerMainThread(DMNetworkForge::handleSendDakiList)
                .add();

        CHANNEL.messageBuilder(RequestTexturesPayload.class, id, NetworkDirection.PLAY_TO_SERVER)
                .encoder(RequestTexturesPayload::write)
                .decoder(RequestTexturesPayload::read)
                .consumerMainThread(DMNetworkForge::handleRequestTextures)
                .add();

        CHANNEL.build();
    }

    private static void handleCommandClient(CommandClientPayload message, CustomPayloadEvent.Context context)
    {
        if (context.isClientSide())
        {
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> message::handleClient));
            context.setPacketHandled(true);
        }
    }

    private static void handleSendTextures(SendTexturesPayload message, CustomPayloadEvent.Context context)
    {
        if (context.isClientSide())
        {
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> message::handleClient));
            context.setPacketHandled(true);
        }
    }

    private static void handleSendDakiList(SendDakiListPayload message, CustomPayloadEvent.Context context)
    {
        if (context.isClientSide())
        {
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> message::handleClient));
            context.setPacketHandled(true);
        }
    }

    private static void handleRequestTextures(RequestTexturesPayload message, CustomPayloadEvent.Context context)
    {
        if (context.isServerSide())
        {
            ServerPlayer sender = context.getSender();
            context.enqueueWork(() -> message.handleServer(sender));
            context.setPacketHandled(true);
        }
    }
}
