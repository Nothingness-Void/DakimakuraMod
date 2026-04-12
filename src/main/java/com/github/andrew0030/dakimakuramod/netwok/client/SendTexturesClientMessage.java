package com.github.andrew0030.dakimakuramod.netwok.client;

import com.github.andrew0030.dakimakuramod.DakimakuraMod;
import com.github.andrew0030.dakimakuramod.dakimakura.Daki;
import com.github.andrew0030.dakimakuramod.netwok.client.util.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SendTexturesClientMessage(Daki daki, int sizeFront, int sizeBack, int packetsNeeded, int idx, byte[] data) implements CustomPacketPayload
{
    public static final Type<SendTexturesClientMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DakimakuraMod.MODID, "send_textures_client"));
    public static final StreamCodec<FriendlyByteBuf, SendTexturesClientMessage> STREAM_CODEC = StreamCodec.of((buf, message) -> message.serialize(buf), SendTexturesClientMessage::deserialize);

    @Override
    public Type<SendTexturesClientMessage> type()
    {
        return TYPE;
    }

    public void serialize(FriendlyByteBuf buf)
    {
        buf.writeUtf(daki.getPackDirectoryName() + ":" + daki.getDakiDirectoryName());
        buf.writeInt(sizeFront);
        buf.writeInt(sizeBack);
        buf.writeInt(packetsNeeded);
        buf.writeInt(idx);
        buf.writeBoolean(data != null);
        if (data != null)
        {
            buf.writeInt(data.length);
            buf.writeBytes(data);
        }
    }

    public static SendTexturesClientMessage deserialize(FriendlyByteBuf buf)
    {
        String path = buf.readUtf(Short.MAX_VALUE);
        String[] pathSplit = path.split(":");
        Daki daki = DakimakuraMod.getDakimakuraManager().getDakiFromMap(pathSplit[0], pathSplit[1]);
        int sizeFront = buf.readInt();
        int sizeBack = buf.readInt();
        int packetsNeeded = buf.readInt();
        int idx = buf.readInt();
        byte[] data = new byte[0];
        if (buf.readBoolean())
        {
            int size = buf.readInt();
            data = new byte[size];
            buf.readBytes(data);
        }
        return new SendTexturesClientMessage(daki, sizeFront, sizeBack, packetsNeeded, idx, data);
    }

    public static void handle(SendTexturesClientMessage message, IPayloadContext context)
    {
        ClientPacketHandler.handleSendTextures(message);
    }
}
