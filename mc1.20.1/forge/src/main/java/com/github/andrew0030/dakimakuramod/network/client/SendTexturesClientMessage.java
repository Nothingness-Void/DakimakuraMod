package com.github.andrew0030.dakimakuramod.network.client;

import com.github.andrew0030.dakimakuramod.DakimakuraModCommon;
import com.github.andrew0030.dakimakuramod.dakimakura.Daki;
import com.github.andrew0030.dakimakuramod.network.client.util.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SendTexturesClientMessage(String path, Daki daki, int sizeFront, int sizeBack, int packetsNeeded, int idx, byte[] data)
{
    public void serialize(FriendlyByteBuf buf)
    {
        String resolvedPath = this.path != null ? this.path : (this.daki != null ? this.daki.getPackDirectoryName() + ":" + this.daki.getDakiDirectoryName() : "");
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

    public static SendTexturesClientMessage deserialize(FriendlyByteBuf buf)
    {
        String path = buf.readUtf(Short.MAX_VALUE);
        String[] pathSplit = path.split(":", 2);
        Daki daki = (pathSplit.length == 2 && DakimakuraModCommon.getDakimakuraManager() != null)
                ? DakimakuraModCommon.getDakimakuraManager().getDakiFromMap(pathSplit[0], pathSplit[1])
                : null;
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
        return new SendTexturesClientMessage(path, daki, sizeFront, sizeBack, packetsNeeded, idx, data);
    }

    public static void handle(SendTexturesClientMessage message, Supplier<NetworkEvent.Context> ctx)
    {
        NetworkEvent.Context context = ctx.get();
        if (context.getDirection().getReceptionSide() == LogicalSide.CLIENT)
        {
            context.enqueueWork(() ->
                    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handleSendTextures(message))
            );
            context.setPacketHandled(true);
        }
    }
}
