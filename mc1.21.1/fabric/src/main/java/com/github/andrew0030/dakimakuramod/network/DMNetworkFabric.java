package com.github.andrew0030.dakimakuramod.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class DMNetworkFabric
{
    private DMNetworkFabric() {}

    public static void registerPayloads()
    {
        PayloadTypeRegistry.playS2C().register(CommandClientPayload.TYPE, CommandClientPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SendDakiListPayload.TYPE, SendDakiListPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SendTexturesPayload.TYPE, SendTexturesPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(RequestTexturesPayload.TYPE, RequestTexturesPayload.STREAM_CODEC);
    }

    public static void registerServerReceivers()
    {
        ServerPlayNetworking.registerGlobalReceiver(RequestTexturesPayload.TYPE, (payload, context) ->
                context.server().execute(() -> payload.handleServer(context.player())));
    }

    public static void registerClientReceivers()
    {
        ClientPlayNetworking.registerGlobalReceiver(CommandClientPayload.TYPE, (payload, context) ->
                context.client().execute(payload::handleClient));

        ClientPlayNetworking.registerGlobalReceiver(SendDakiListPayload.TYPE, (payload, context) ->
                context.client().execute(payload::handleClient));

        ClientPlayNetworking.registerGlobalReceiver(SendTexturesPayload.TYPE, (payload, context) ->
                context.client().execute(payload::handleClient));
    }
}
