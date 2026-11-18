package com.github.andrew0030.dakimakuramod.platform;

import com.github.andrew0030.dakimakuramod.dakimakura.Daki;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.nio.file.Path;

public interface IPlatformHelper
{
    Path getGameDirectory();

    ResourceLocation id(String namespace, String path);

    Block getDakimakuraBlock();

    Item getDakimakuraItem();

    Item getDakimakuraDesignItem();

    BlockEntityType<?> getDakimakuraBlockEntityType();

    EntityType<?> getDakimakuraEntityType();

    RecipeSerializer<?> getDakimakuraDesignRecipeSerializer();

    void serializeDakiToStack(Daki daki, ItemStack stack);

    Daki deserializeDakiFromStack(ItemStack stack);

    boolean isStackFlipped(ItemStack stack);

    ItemStack setStackFlipped(ItemStack stack, boolean flipped);

    void sendDakiListToAll();

    void sendDakiListToPlayer(ServerPlayer player);

    void sendTexturesChunk(ServerPlayer player, String path, Daki daki, int sizeFront, int sizeBack, int packetsNeeded, int idx, byte[] data);

    void sendOpenPackFolder(ServerPlayer player);

    void clientRequestTextures(Daki daki);

    default boolean isBed(Level level, BlockPos pos, BlockState state)
    {
        return state.is(BlockTags.BEDS) || state.getBlock() instanceof BedBlock;
    }

    void reloadCreativeTabContents();
}
