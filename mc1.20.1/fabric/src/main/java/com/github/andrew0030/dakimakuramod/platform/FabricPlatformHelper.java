package com.github.andrew0030.dakimakuramod.platform;

import com.github.andrew0030.dakimakuramod.config.DMTomlConfig;
import com.github.andrew0030.dakimakuramod.dakimakura.Daki;
import com.github.andrew0030.dakimakuramod.dakimakura.serialize.DakiTagSerializer;
import com.github.andrew0030.dakimakuramod.network.FabricNetwork;
import com.github.andrew0030.dakimakuramod.registries.CreativeTabHelper;
import com.github.andrew0030.dakimakuramod.registries.DMRegistryFabric;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
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
    @Override
    public Path getGameDirectory()
    {
        return FabricLoader.getInstance().getGameDir();
    }

    @Override
    public ResourceLocation id(String namespace, String path)
    {
        return new ResourceLocation(namespace, path);
    }

    @Override
    public Block getDakimakuraBlock()
    {
        return DMRegistryFabric.DAKIMAKURA_BLOCK;
    }

    @Override
    public Item getDakimakuraItem()
    {
        return DMRegistryFabric.DAKIMAKURA_ITEM;
    }

    @Override
    public Item getDakimakuraDesignItem()
    {
        return DMRegistryFabric.DAKIMAKURA_DESIGN_ITEM;
    }

    @Override
    public BlockEntityType<?> getDakimakuraBlockEntityType()
    {
        return DMRegistryFabric.DAKIMAKURA_BLOCK_ENTITY;
    }

    @Override
    public EntityType<?> getDakimakuraEntityType()
    {
        return DMRegistryFabric.DAKIMAKURA_ENTITY;
    }

    @Override
    public RecipeSerializer<?> getDakimakuraDesignRecipeSerializer()
    {
        return DMRegistryFabric.DAKIMAKURA_DESIGN_RECIPE;
    }

    @Override
    public void serializeDakiToStack(Daki daki, ItemStack stack)
    {
        DakiTagSerializer.serializeToStack(daki, stack);
    }

    @Override
    public Daki deserializeDakiFromStack(ItemStack stack)
    {
        return DakiTagSerializer.deserializeFromStack(stack);
    }

    @Override
    public boolean isStackFlipped(ItemStack stack)
    {
        return DakiTagSerializer.isStackFlipped(stack);
    }

    @Override
    public ItemStack setStackFlipped(ItemStack stack, boolean flipped)
    {
        return DakiTagSerializer.setStackFlipped(stack, flipped);
    }

    @Override
    public void sendDakiListToAll()
    {
        FabricNetwork.sendDakiListToAll();
    }

    @Override
    public void sendDakiListToPlayer(ServerPlayer player)
    {
        FabricNetwork.sendDakiList(player);
    }

    @Override
    public void sendTexturesChunk(ServerPlayer player, String path, Daki daki, int sizeFront, int sizeBack, int packetsNeeded, int idx, byte[] data)
    {
        FabricNetwork.sendTextures(player, path, sizeFront, sizeBack, packetsNeeded, idx, data);
    }

    @Override
    public void sendOpenPackFolder(ServerPlayer player)
    {
        FabricNetwork.openDakiFolder(player);
    }

    @Override
    public void clientRequestTextures(Daki daki)
    {
        FabricNetwork.clientRequestTextures(daki);
    }

    @Override
    public void reloadCreativeTabContents()
    {
        DMTomlConfig.loadCommon(FabricLoader.getInstance().getConfigDir());
        CreativeTabHelper.reloadTabContents();
    }
}
