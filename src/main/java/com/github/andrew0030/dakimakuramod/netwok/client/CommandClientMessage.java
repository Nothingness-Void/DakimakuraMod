package com.github.andrew0030.dakimakuramod.netwok.client;

import com.github.andrew0030.dakimakuramod.DakimakuraMod;
import com.github.andrew0030.dakimakuramod.netwok.client.util.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CommandClientMessage(CommandType commandType) implements CustomPacketPayload
{
    public static final Type<CommandClientMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DakimakuraMod.MODID, "command_client"));
    public static final StreamCodec<FriendlyByteBuf, CommandClientMessage> STREAM_CODEC = StreamCodec.of((buf, message) -> message.serialize(buf), CommandClientMessage::deserialize);

    @Override
    public Type<CommandClientMessage> type()
    {
        return TYPE;
    }

    public void serialize(FriendlyByteBuf buf)
    {
        buf.writeByte(this.commandType().ordinal());
    }

    public static CommandClientMessage deserialize(FriendlyByteBuf buf)
    {
        return new CommandClientMessage(CommandType.values()[buf.readByte()]);
    }

    public static void handle(CommandClientMessage message, IPayloadContext context)
    {
        ClientPacketHandler.handleCommandClient(message);
    }

    public enum CommandType
    {
        OPEN_PACK_FOLDER;
    }
}
