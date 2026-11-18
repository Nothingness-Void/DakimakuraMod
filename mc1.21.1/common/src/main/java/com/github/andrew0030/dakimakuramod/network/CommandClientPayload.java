package com.github.andrew0030.dakimakuramod.network;

import com.github.andrew0030.dakimakuramod.DakimakuraModCommon;
import com.github.andrew0030.dakimakuramod.platform.Services;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record CommandClientPayload(CommandType commandType) implements CustomPacketPayload
{
    public static final Type<CommandClientPayload> TYPE = new Type<>(Services.getPlatform().id(DakimakuraModCommon.MODID, "command_client"));
    public static final StreamCodec<FriendlyByteBuf, CommandClientPayload> STREAM_CODEC = StreamCodec.of(
            (buf, msg) -> msg.write(buf),
            CommandClientPayload::read
    );

    public void write(FriendlyByteBuf buf)
    {
        buf.writeByte(this.commandType().ordinal());
    }

    public static CommandClientPayload read(FriendlyByteBuf buf)
    {
        return new CommandClientPayload(CommandType.values()[buf.readByte()]);
    }

    public void handleClient()
    {
        if (this.commandType == CommandType.OPEN_PACK_FOLDER)
            Util.getPlatform().openUri(DakimakuraModCommon.getDakimakuraManager().getPackFolder().toURI());
    }

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public enum CommandType
    {
        OPEN_PACK_FOLDER
    }
}
