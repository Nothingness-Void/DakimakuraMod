package com.github.andrew0030.dakimakuramod.network;

import com.github.andrew0030.dakimakuramod.DakimakuraModCommon;
import com.github.andrew0030.dakimakuramod.dakimakura.BatchPackListHelper;
import com.github.andrew0030.dakimakuramod.dakimakura.Daki;
import com.github.andrew0030.dakimakuramod.dakimakura.client.ClientPackListReceiver;
import com.github.andrew0030.dakimakuramod.dakimakura.pack.DakiPackFolder;
import com.github.andrew0030.dakimakuramod.dakimakura.pack.IDakiPack;
import com.github.andrew0030.dakimakuramod.dakimakura.serialize.DakiJsonSerializer;
import com.github.andrew0030.dakimakuramod.platform.Services;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ArrayList;

public record SendDakiListPayload(boolean firstBatch, boolean lastBatch, ArrayList<IDakiPack> packs) implements CustomPacketPayload
{
    public static final Type<SendDakiListPayload> TYPE = new Type<>(Services.getPlatform().id(DakimakuraModCommon.MODID, "send_daki_list"));
    public static final StreamCodec<FriendlyByteBuf, SendDakiListPayload> STREAM_CODEC = StreamCodec.of(
            (buf, msg) -> msg.write(buf),
            SendDakiListPayload::read
    );

    public static SendDakiListPayload fromBatch(BatchPackListHelper.PackListBatch batch)
    {
        return new SendDakiListPayload(batch.firstBatch(), batch.lastBatch(), batch.packs());
    }

    public void write(FriendlyByteBuf buf)
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

    public static SendDakiListPayload read(FriendlyByteBuf buf)
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
                if (daki != null)
                    dakiPack.addDaki(daki);
            }
            packs.add(dakiPack);
        }
        return new SendDakiListPayload(firstBatch, lastBatch, packs);
    }

    public void handleClient()
    {
        ClientPackListReceiver.handleClientBatch(this.firstBatch, this.lastBatch, this.packs);
    }

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}
