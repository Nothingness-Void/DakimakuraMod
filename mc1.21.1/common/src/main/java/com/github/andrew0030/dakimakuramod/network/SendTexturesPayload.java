package com.github.andrew0030.dakimakuramod.network;

import com.github.andrew0030.dakimakuramod.DakimakuraModCommon;
import com.github.andrew0030.dakimakuramod.dakimakura.Daki;
import com.github.andrew0030.dakimakuramod.dakimakura.client.ClientTextureReceiver;
import com.github.andrew0030.dakimakuramod.platform.Services;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SendTexturesPayload(String path, Daki daki, int sizeFront, int sizeBack, int packetsNeeded, int idx, byte[] data) implements CustomPacketPayload
{
    public static final Type<SendTexturesPayload> TYPE = new Type<>(Services.getPlatform().id(DakimakuraModCommon.MODID, "send_textures"));
    public static final StreamCodec<FriendlyByteBuf, SendTexturesPayload> STREAM_CODEC = StreamCodec.of(
            (buf, msg) -> msg.write(buf),
            SendTexturesPayload::read
    );

    public void write(FriendlyByteBuf buf)
    {
        String resolvedPath = this.path != null && !this.path.isEmpty()
                ? this.path
                : (this.daki != null ? this.daki.getPackDirectoryName() + ":" + this.daki.getDakiDirectoryName() : "");
        buf.writeUtf(resolvedPath);
        buf.writeInt(this.sizeFront);
        buf.writeInt(this.sizeBack);
        buf.writeInt(this.packetsNeeded);
        buf.writeInt(this.idx);
        buf.writeBoolean(this.data != null);
        if (this.data != null)
        {
            buf.writeInt(this.data.length);
            buf.writeBytes(this.data);
        }
    }

    public static SendTexturesPayload read(FriendlyByteBuf buf)
    {
        String path = buf.readUtf(Short.MAX_VALUE);
        String[] pathSplit = path.split(":", 2);
        Daki daki = pathSplit.length == 2 ? DakimakuraModCommon.getDakimakuraManager().getDakiFromMap(pathSplit[0], pathSplit[1]) : null;
        int sizeFront = buf.readInt();
        int sizeBack = buf.readInt();
        int packetsNeeded = buf.readInt();
        int idx = buf.readInt();
        byte[] data = new byte[0];
        if (buf.readBoolean())
        {
            int size = buf.readInt();
            if (size < 0 || size > Short.MAX_VALUE)
                throw new IllegalArgumentException("Invalid Dakimakura texture packet size: " + size);
            data = new byte[size];
            buf.readBytes(data);
        }
        return new SendTexturesPayload(path, daki, sizeFront, sizeBack, packetsNeeded, idx, data);
    }

    public void handleClient()
    {
        ClientTextureReceiver.gotDakiTexturePartFromServer(this.path, this.daki, this.sizeFront, this.sizeBack, this.packetsNeeded, this.idx, this.data);
    }

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}
