package com.github.andrew0030.dakimakuramod.netwok.client;

import com.github.andrew0030.dakimakuramod.netwok.client.util.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.DistExecutor;

public record CommandClientMessage(CommandType commandType)
{
    public void serialize(FriendlyByteBuf buf)
    {
        buf.writeByte(this.commandType().ordinal());
    }

    public static CommandClientMessage deserialize(FriendlyByteBuf buf)
    {
        return new CommandClientMessage(CommandType.values()[buf.readByte()]);
    }

    public static void handle(CommandClientMessage message, CustomPayloadEvent.Context context)
    {
        if (context.isClientSide())
        {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handleCommandClient(message));
        }
    }

    public enum CommandType
    {
        OPEN_PACK_FOLDER;
    }
}
