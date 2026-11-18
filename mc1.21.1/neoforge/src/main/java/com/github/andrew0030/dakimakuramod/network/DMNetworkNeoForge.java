package com.github.andrew0030.dakimakuramod.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class DMNetworkNeoForge
{
    private DMNetworkNeoForge() {}

    public static void registerPayloads(RegisterPayloadHandlersEvent event)
    {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(CommandClientPayload.TYPE, CommandClientPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(payload::handleClient));

        registrar.playToClient(SendDakiListPayload.TYPE, SendDakiListPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(payload::handleClient));

        registrar.playToClient(SendTexturesPayload.TYPE, SendTexturesPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(payload::handleClient));

        registrar.playToServer(RequestTexturesPayload.TYPE, RequestTexturesPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer serverPlayer)
                        payload.handleServer(serverPlayer);
                }));
    }
}
