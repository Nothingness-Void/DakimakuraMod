package com.github.andrew0030.dakimakuramod.platform;

import com.github.andrew0030.dakimakuramod.dakimakura.Daki;
import com.github.andrew0030.dakimakuramod.dakimakura.serialize.DakiTagSerializer;
import com.github.andrew0030.dakimakuramod.network.NetworkUtil;
import com.github.andrew0030.dakimakuramod.registries.CreativeTabHelper;
import com.github.andrew0030.dakimakuramod.registries.DMBlockEntities;
import com.github.andrew0030.dakimakuramod.registries.DMBlocks;
import com.github.andrew0030.dakimakuramod.registries.DMEntities;
import com.github.andrew0030.dakimakuramod.registries.DMItems;
import com.github.andrew0030.dakimakuramod.registries.DMRecipeSerializers;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class ForgePlatformHelper implements IPlatformHelper
{
    @Override
    public Path getGameDirectory()
    {
        return FMLPaths.GAMEDIR.get();
    }

    @Override
    public ResourceLocation id(String namespace, String path)
    {
        return new ResourceLocation(namespace, path);
    }

    @Override
    public Block getDakimakuraBlock()
    {
        return DMBlocks.DAKIMAKURA.get();
    }

    @Override
    public Item getDakimakuraItem()
    {
        return DMItems.DAKIMAKURA.get();
    }

    @Override
    public Item getDakimakuraDesignItem()
    {
        return DMItems.DAKIMAKURA_DESIGN.get();
    }

    @Override
    public BlockEntityType<?> getDakimakuraBlockEntityType()
    {
        return DMBlockEntities.DAKIMAKURA.get();
    }

    @Override
    public EntityType<?> getDakimakuraEntityType()
    {
        return DMEntities.DAKIMAKURA.get();
    }

    @Override
    public RecipeSerializer<?> getDakimakuraDesignRecipeSerializer()
    {
        return DMRecipeSerializers.DAKIMAKURA_DESIGN.get();
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
        NetworkUtil.sendDakiList();
    }

    @Override
    public void sendDakiListToPlayer(ServerPlayer player)
    {
        NetworkUtil.sendDakiList(player);
    }

    @Override
    public void sendTexturesChunk(ServerPlayer player, String path, Daki daki, int sizeFront, int sizeBack, int packetsNeeded, int idx, byte[] data)
    {
        NetworkUtil.sendTextures(player, path, daki, sizeFront, sizeBack, packetsNeeded, idx, data);
    }

    @Override
    public void sendOpenPackFolder(ServerPlayer player)
    {
        NetworkUtil.openDakiFolder(player);
    }

    @Override
    public void clientRequestTextures(Daki daki)
    {
        NetworkUtil.clientRequestTextures(daki);
    }

    @Override
    public boolean isBed(Level level, BlockPos pos, BlockState state)
    {
        return state.isBed(level, pos, null);
    }

    @Override
    public void reloadCreativeTabContents()
    {
        CreativeTabHelper.reloadTabContents();
    }
}
