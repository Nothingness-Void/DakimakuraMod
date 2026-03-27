package com.github.andrew0030.dakimakuramod.netwok.server;

import com.github.andrew0030.dakimakuramod.DakimakuraMod;
import com.github.andrew0030.dakimakuramod.dakimakura.Daki;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

public record RequestTexturesServerMessage(Daki daki)
{
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

    public static void handle(RequestTexturesServerMessage message, CustomPayloadEvent.Context context)
    {
        ServerPlayer serverPlayer = context.getSender();
        Daki daki = message.daki();

        if (context.isServerSide() && serverPlayer != null)
        {
            DakimakuraMod.getTextureManagerCommon().onClientRequestTexture(serverPlayer, daki);
        }
    }
}
