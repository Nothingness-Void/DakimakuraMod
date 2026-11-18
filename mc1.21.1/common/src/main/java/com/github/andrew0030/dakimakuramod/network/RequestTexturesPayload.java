package com.github.andrew0030.dakimakuramod.network;

import com.github.andrew0030.dakimakuramod.DakimakuraModCommon;
import com.github.andrew0030.dakimakuramod.dakimakura.Daki;
import com.github.andrew0030.dakimakuramod.dakimakura.DakiSendHelper;
import com.github.andrew0030.dakimakuramod.platform.Services;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public record RequestTexturesPayload(String path, Daki daki) implements CustomPacketPayload
{
    public static final Type<RequestTexturesPayload> TYPE = new Type<>(Services.getPlatform().id(DakimakuraModCommon.MODID, "request_textures"));
    public static final StreamCodec<FriendlyByteBuf, RequestTexturesPayload> STREAM_CODEC = StreamCodec.of(
            (buf, msg) -> msg.write(buf),
            RequestTexturesPayload::read
    );

    public RequestTexturesPayload(Daki daki)
    {
        this(daki != null ? daki.getPackDirectoryName() + ":" + daki.getDakiDirectoryName() : "", daki);
    }

    public void write(FriendlyByteBuf buf)
    {
        String resolvedPath = this.path != null && !this.path.isEmpty()
                ? this.path
                : (this.daki != null ? this.daki.getPackDirectoryName() + ":" + this.daki.getDakiDirectoryName() : "");
        buf.writeUtf(resolvedPath);
    }

    public static RequestTexturesPayload read(FriendlyByteBuf buf)
    {
        String path = buf.readUtf(Short.MAX_VALUE);
        String[] pathSplit = path.split(":", 2);
        Daki daki = pathSplit.length == 2 ? DakimakuraModCommon.getDakimakuraManager().getDakiFromMap(pathSplit[0], pathSplit[1]) : null;
        return new RequestTexturesPayload(path, daki);
    }

    public void handleServer(ServerPlayer serverPlayer)
    {
        if (serverPlayer == null)
            return;
        if (this.daki != null)
            DakimakuraModCommon.getTextureManagerCommon().onClientRequestTexture(serverPlayer, this.daki);
        else
            Services.getPlatform().sendTexturesChunk(serverPlayer, this.path, null, 0, 0, 1, 0, null);
    }

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}
