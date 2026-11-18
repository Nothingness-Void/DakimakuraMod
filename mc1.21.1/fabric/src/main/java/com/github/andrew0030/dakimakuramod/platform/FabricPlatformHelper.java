package com.github.andrew0030.dakimakuramod.platform;

import com.github.andrew0030.dakimakuramod.DakimakuraModCommon;
import com.github.andrew0030.dakimakuramod.dakimakura.BatchPackListHelper;
import com.github.andrew0030.dakimakuramod.dakimakura.Daki;
import com.github.andrew0030.dakimakuramod.dakimakura.serialize.DakiTagSerializer;
import com.github.andrew0030.dakimakuramod.network.CommandClientPayload;
import com.github.andrew0030.dakimakuramod.network.RequestTexturesPayload;
import com.github.andrew0030.dakimakuramod.network.SendDakiListPayload;
import com.github.andrew0030.dakimakuramod.network.SendTexturesPayload;
import com.github.andrew0030.dakimakuramod.registries.CreativeTabHelper;
import com.github.andrew0030.dakimakuramod.registries.DMRegistriesFabric;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.nio.file.Path;

public class FabricPlatformHelper implements IPlatformHelper
{
    public static volatile MinecraftServer currentServer;

    @Override
    public Path getGameDirectory()
    {
        return FabricLoader.getInstance().getGameDir();
    }

    @Override
    public ResourceLocation id(String namespace, String path)
    {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    @Override
    public Block getDakimakuraBlock()
    {
        return DMRegistriesFabric.DAKIMAKURA_BLOCK;
    }

    @Override
    public Item getDakimakuraItem()
    {
        return DMRegistriesFabric.DAKIMAKURA_ITEM;
    }

    @Override
    public Item getDakimakuraDesignItem()
    {
        return DMRegistriesFabric.DAKIMAKURA_DESIGN;
    }

    @Override
    public BlockEntityType<?> getDakimakuraBlockEntityType()
    {
        return DMRegistriesFabric.DAKIMAKURA_BLOCK_ENTITY;
    }

    @Override
    public EntityType<?> getDakimakuraEntityType()
    {
        return DMRegistriesFabric.DAKIMAKURA_ENTITY;
    }

    @Override
    public RecipeSerializer<?> getDakimakuraDesignRecipeSerializer()
    {
        return DMRegistriesFabric.DAKIMAKURA_DESIGN_RECIPE;
    }

    @Override
    public void serializeDakiToStack(Daki daki, ItemStack stack)
    {
        DakiTagSerializer.serialize(daki, stack);
    }

    @Override
    public Daki deserializeDakiFromStack(ItemStack stack)
    {
        return DakiTagSerializer.deserialize(stack);
    }

    @Override
    public boolean isStackFlipped(ItemStack stack)
    {
        return DakiTagSerializer.isFlipped(stack);
    }

    @Override
    public ItemStack setStackFlipped(ItemStack stack, boolean flipped)
    {
        DakiTagSerializer.setFlipped(stack, flipped);
        return stack;
    }

    @Override
    public void sendDakiListToAll()
    {
        MinecraftServer server = currentServer;
        if (server == null)
            return;
        for (BatchPackListHelper.PackListBatch batch : BatchPackListHelper.createBatches(DakimakuraModCommon.getDakimakuraManager().getDakiPacksList()))
        {
            SendDakiListPayload payload = SendDakiListPayload.fromBatch(batch);
            for (ServerPlayer player : server.getPlayerList().getPlayers())
                ServerPlayNetworking.send(player, payload);
        }
    }

    @Override
    public void sendDakiListToPlayer(ServerPlayer player)
    {
        for (BatchPackListHelper.PackListBatch batch : BatchPackListHelper.createBatches(DakimakuraModCommon.getDakimakuraManager().getDakiPacksList()))
            ServerPlayNetworking.send(player, SendDakiListPayload.fromBatch(batch));
    }

    @Override
    public void sendTexturesChunk(ServerPlayer player, String path, Daki daki, int sizeFront, int sizeBack, int packetsNeeded, int idx, byte[] data)
    {
        ServerPlayNetworking.send(player, new SendTexturesPayload(path, daki, sizeFront, sizeBack, packetsNeeded, idx, data));
    }

    @Override
    public void sendOpenPackFolder(ServerPlayer player)
    {
        ServerPlayNetworking.send(player, new CommandClientPayload(CommandClientPayload.CommandType.OPEN_PACK_FOLDER));
    }

    @Override
    public void clientRequestTextures(Daki daki)
    {
        ClientPlayNetworking.send(new RequestTexturesPayload(daki));
    }

    @Override
    public void reloadCreativeTabContents()
    {
        CreativeTabHelper.reloadTabContents();
    }
}
