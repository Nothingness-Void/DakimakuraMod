package com.github.andrew0030.dakimakuramod.netwok;

import com.github.andrew0030.dakimakuramod.DakimakuraMod;
import com.github.andrew0030.dakimakuramod.netwok.client.CommandClientMessage;
import com.github.andrew0030.dakimakuramod.netwok.client.SendDakiListClientMessage;
import com.github.andrew0030.dakimakuramod.netwok.client.SendTexturesClientMessage;
import com.github.andrew0030.dakimakuramod.netwok.server.RequestTexturesServerMessage;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class DMNetwork
{
    public static final String NETWORK_PROTOCOL = "1";

    /**
     * Used to set up all the Messages
     */
    public static void registerMessages(RegisterPayloadHandlersEvent event)
    {
        PayloadRegistrar registrar = event.registrar(DakimakuraMod.MODID + "-" + NETWORK_PROTOCOL);
        registrar.playToClient(CommandClientMessage.TYPE, CommandClientMessage.STREAM_CODEC, CommandClientMessage::handle);
        registrar.playToClient(SendTexturesClientMessage.TYPE, SendTexturesClientMessage.STREAM_CODEC, SendTexturesClientMessage::handle);
        registrar.playToClient(SendDakiListClientMessage.TYPE, SendDakiListClientMessage.STREAM_CODEC, SendDakiListClientMessage::handle);
        registrar.playToServer(RequestTexturesServerMessage.TYPE, RequestTexturesServerMessage.STREAM_CODEC, RequestTexturesServerMessage::handle);
    }
}
