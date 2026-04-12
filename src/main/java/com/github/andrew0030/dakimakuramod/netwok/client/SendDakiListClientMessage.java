package com.github.andrew0030.dakimakuramod.netwok.client;

import com.github.andrew0030.dakimakuramod.DakimakuraMod;
import com.github.andrew0030.dakimakuramod.dakimakura.Daki;
import com.github.andrew0030.dakimakuramod.dakimakura.DakiManager;
import com.github.andrew0030.dakimakuramod.dakimakura.pack.DakiPackFolder;
import com.github.andrew0030.dakimakuramod.dakimakura.pack.DakiPackZip;
import com.github.andrew0030.dakimakuramod.dakimakura.pack.IDakiPack;
import com.github.andrew0030.dakimakuramod.dakimakura.serialize.DakiJsonSerializer;
import com.github.andrew0030.dakimakuramod.netwok.client.util.ClientPacketHandler;
import com.mojang.logging.LogUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.slf4j.Logger;

import java.util.ArrayList;

public class SendDakiListClientMessage implements CustomPacketPayload
{
    public static final Type<SendDakiListClientMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DakimakuraMod.MODID, "send_daki_list_client"));
    public static final StreamCodec<FriendlyByteBuf, SendDakiListClientMessage> STREAM_CODEC = StreamCodec.of((buf, message) -> message.serialize(buf), SendDakiListClientMessage::deserialize);
    private static final Logger LOGGER = LogUtils.getLogger();
    private final DakiManager dakiManager;
    public ArrayList<IDakiPack> packs;

    @Override
    public Type<SendDakiListClientMessage> type()
    {
        return TYPE;
    }

    public SendDakiListClientMessage()
    {
        this.dakiManager = DakimakuraMod.getDakimakuraManager();
        this.packs = dakiManager.getDakiPacksList();
    }

    public SendDakiListClientMessage(ArrayList<IDakiPack> packs)
    {
        this();
        this.packs = packs;
    }

    public void serialize(FriendlyByteBuf buf)
    {
        ArrayList<IDakiPack> dakiPacks = this.dakiManager.getDakiPacksList();
        LOGGER.info("Sending " + dakiPacks.size() + " packs to client.");
        buf.writeInt(dakiPacks.size());
        for (IDakiPack dakiPack : dakiPacks)
        {
            buf.writeBoolean(dakiPack instanceof DakiPackFolder);
            buf.writeUtf(dakiPack.getResourceName());
            ArrayList<Daki> dakis = dakiPack.getDakisInPack();
            //DakimakuraMod.getLogger().info("Sending " + dakis.size() + " dakis to client.");
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
        ArrayList<IDakiPack> packs = new ArrayList<>();
        int packCount = buf.readInt();
        LOGGER.info("Getting " + packCount + " packs from server.");
        for (int i = 0; i < packCount; i++)
        {
            boolean folder = buf.readBoolean();
            String packName = buf.readUtf(Short.MAX_VALUE);
            IDakiPack dakiPack = folder ? new DakiPackFolder(packName) : new DakiPackZip(packName);
            int dakiCount = buf.readInt();
            //DakimakuraMod.getLogger().info("Getting " + dakiCount + " dakis from server.");
            for (int j = 0; j < dakiCount; j++)
            {
                String path = buf.readUtf(Short.MAX_VALUE);
                String dakiJson = buf.readUtf(Short.MAX_VALUE);
                Daki daki = DakiJsonSerializer.deserialize(dakiJson, dakiPack.getResourceName(), path);
                dakiPack.addDaki(daki);
            }
            packs.add(dakiPack);
        }
        return new SendDakiListClientMessage(packs);
    }

    public static void handle(SendDakiListClientMessage message, IPayloadContext context)
    {
        ClientPacketHandler.handleSendDakiList(message);
    }
}
