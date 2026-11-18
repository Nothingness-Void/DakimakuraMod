package com.github.andrew0030.dakimakuramod.network.client;

import com.github.andrew0030.dakimakuramod.dakimakura.BatchPackListHelper;
import com.github.andrew0030.dakimakuramod.dakimakura.Daki;
import com.github.andrew0030.dakimakuramod.dakimakura.pack.DakiPackFolder;
import com.github.andrew0030.dakimakuramod.dakimakura.pack.IDakiPack;
import com.github.andrew0030.dakimakuramod.dakimakura.serialize.DakiJsonSerializer;
import com.github.andrew0030.dakimakuramod.network.client.util.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.function.Supplier;

public class SendDakiListClientMessage
{
    public final boolean firstBatch;
    public final boolean lastBatch;
    public final ArrayList<IDakiPack> packs;

    public SendDakiListClientMessage(boolean firstBatch, boolean lastBatch, ArrayList<IDakiPack> packs)
    {
        this.firstBatch = firstBatch;
        this.lastBatch = lastBatch;
        this.packs = packs;
    }

    public void serialize(FriendlyByteBuf buf)
    {
        buf.writeBoolean(this.firstBatch);
        buf.writeBoolean(this.lastBatch);
        buf.writeInt(this.packs.size());
        for (IDakiPack dakiPack : this.packs)
        {
            buf.writeBoolean(dakiPack instanceof DakiPackFolder);
            buf.writeUtf(dakiPack.getResourceName());
            ArrayList<Daki> dakis = dakiPack.getDakisInPack();
            buf.writeInt(dakis.size());
            for (Daki daki : dakis)
            {
                buf.writeUtf(daki.getDakiDirectoryName());
                buf.writeUtf(DakiJsonSerializer.serialize(daki).toString());
            }
        }
    }

    public static SendDakiListClientMessage deserialize(FriendlyByteBuf buf)
    {
        boolean firstBatch = buf.readBoolean();
        boolean lastBatch = buf.readBoolean();
        ArrayList<IDakiPack> packs = new ArrayList<>();
        int packCount = buf.readInt();
        for (int i = 0; i < packCount; i++)
        {
            boolean folder = buf.readBoolean();
            String packName = buf.readUtf(Short.MAX_VALUE);
            IDakiPack dakiPack = BatchPackListHelper.createEmptyPack(folder, packName);
            int dakiCount = buf.readInt();
            for (int j = 0; j < dakiCount; j++)
            {
                String path = buf.readUtf(Short.MAX_VALUE);
                String dakiJson = buf.readUtf(Short.MAX_VALUE);
                Daki daki = DakiJsonSerializer.deserialize(dakiJson, dakiPack.getResourceName(), path);
                dakiPack.addDaki(daki);
            }
            packs.add(dakiPack);
        }
        return new SendDakiListClientMessage(firstBatch, lastBatch, packs);
    }

    public static void handle(SendDakiListClientMessage message, Supplier<NetworkEvent.Context> ctx)
    {
        NetworkEvent.Context context = ctx.get();
        if (context.getDirection().getReceptionSide() == LogicalSide.CLIENT)
        {
            context.enqueueWork(() ->
                    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handleSendDakiList(message))
            );
            context.setPacketHandled(true);
        }
    }
}
