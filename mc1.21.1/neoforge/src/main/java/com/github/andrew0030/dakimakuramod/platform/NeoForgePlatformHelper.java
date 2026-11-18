package com.github.andrew0030.dakimakuramod.platform;

import com.github.andrew0030.dakimakuramod.dakimakura.BatchPackListHelper;
import com.github.andrew0030.dakimakuramod.dakimakura.Daki;
import com.github.andrew0030.dakimakuramod.dakimakura.serialize.DakiTagSerializer;
import com.github.andrew0030.dakimakuramod.network.CommandClientPayload;
import com.github.andrew0030.dakimakuramod.network.RequestTexturesPayload;
import com.github.andrew0030.dakimakuramod.network.SendDakiListPayload;
import com.github.andrew0030.dakimakuramod.network.SendTexturesPayload;
import com.github.andrew0030.dakimakuramod.registries.CreativeTabHelper;
import com.github.andrew0030.dakimakuramod.registries.DMRegistriesNeoForge;
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
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.network.PacketDistributor;

import java.nio.file.Path;

public class NeoForgePlatformHelper implements IPlatformHelper
{
    @Override
    public Path getGameDirectory()
    {
        return FMLPaths.GAMEDIR.get();
    }

    @Override
    public ResourceLocation id(String namespace, String path)
    {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    @Override
    public Block getDakimakuraBlock()
    {
        return DMRegistriesNeoForge.DAKIMAKURA_BLOCK.get();
    }

    @Override
    public Item getDakimakuraItem()
    {
        return DMRegistriesNeoForge.DAKIMAKURA_ITEM.get();
    }

    @Override
    public Item getDakimakuraDesignItem()
    {
        return DMRegistriesNeoForge.DAKIMAKURA_DESIGN.get();
    }

    @Override
    public BlockEntityType<?> getDakimakuraBlockEntityType()
    {
        return DMRegistriesNeoForge.DAKIMAKURA_BLOCK_ENTITY.get();
    }

    @Override
    public EntityType<?> getDakimakuraEntityType()
    {
        return DMRegistriesNeoForge.DAKIMAKURA_ENTITY.get();
    }

    @Override
    public RecipeSerializer<?> getDakimakuraDesignRecipeSerializer()
    {
        return DMRegistriesNeoForge.DAKIMAKURA_DESIGN_RECIPE.get();
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
        for (BatchPackListHelper.PackListBatch batch : BatchPackListHelper.createBatches(com.github.andrew0030.dakimakuramod.DakimakuraModCommon.getDakimakuraManager().getDakiPacksList()))
            PacketDistributor.sendToAllPlayers(SendDakiListPayload.fromBatch(batch));
    }

    @Override
    public void sendDakiListToPlayer(ServerPlayer player)
    {
        for (BatchPackListHelper.PackListBatch batch : BatchPackListHelper.createBatches(com.github.andrew0030.dakimakuramod.DakimakuraModCommon.getDakimakuraManager().getDakiPacksList()))
            PacketDistributor.sendToPlayer(player, SendDakiListPayload.fromBatch(batch));
    }

    @Override
    public void sendTexturesChunk(ServerPlayer player, String path, Daki daki, int sizeFront, int sizeBack, int packetsNeeded, int idx, byte[] data)
    {
        PacketDistributor.sendToPlayer(player, new SendTexturesPayload(path, daki, sizeFront, sizeBack, packetsNeeded, idx, data));
    }

    @Override
    public void sendOpenPackFolder(ServerPlayer player)
    {
        PacketDistributor.sendToPlayer(player, new CommandClientPayload(CommandClientPayload.CommandType.OPEN_PACK_FOLDER));
    }

    @Override
    public void clientRequestTextures(Daki daki)
    {
        PacketDistributor.sendToServer(new RequestTexturesPayload(daki));
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
