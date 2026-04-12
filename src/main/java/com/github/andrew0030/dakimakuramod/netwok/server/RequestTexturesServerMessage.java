package com.github.andrew0030.dakimakuramod.netwok.server;

import com.github.andrew0030.dakimakuramod.DakimakuraMod;
import com.github.andrew0030.dakimakuramod.dakimakura.Daki;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RequestTexturesServerMessage(Daki daki) implements CustomPacketPayload
{
    public static final Type<RequestTexturesServerMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DakimakuraMod.MODID, "request_textures_server"));
    public static final StreamCodec<FriendlyByteBuf, RequestTexturesServerMessage> STREAM_CODEC = StreamCodec.of((buf, message) -> message.serialize(buf), RequestTexturesServerMessage::deserialize);

    @Override
    public Type<RequestTexturesServerMessage> type()
    {
        return TYPE;
    }

    public void serialize(FriendlyByteBuf buf)
    {
        buf.writeUtf(daki.getPackDirectoryName() + ":" + daki.getDakiDirectoryName());
    }

    public static RequestTexturesServerMessage deserialize(FriendlyByteBuf buf)
    {
        String path = buf.readUtf(Short.MAX_VALUE);
        String[] pathSplit = path.split(":");
        Daki daki = DakimakuraMod.getDakimakuraManager().getDakiFromMap(pathSplit[0], pathSplit[1]);
        return new RequestTexturesServerMessage(daki);
    }

    public static void handle(RequestTexturesServerMessage message, IPayloadContext context)
    {
        ServerPlayer serverPlayer = (ServerPlayer) context.player();
        Daki daki = message.daki();

        if (serverPlayer != null)
        {
            DakimakuraMod.getTextureManagerCommon().onClientRequestTexture(serverPlayer, daki);
        }
    }
}
