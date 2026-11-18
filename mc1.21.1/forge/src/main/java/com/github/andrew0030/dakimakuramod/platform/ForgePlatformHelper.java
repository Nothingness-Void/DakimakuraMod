package com.github.andrew0030.dakimakuramod.platform;

import com.github.andrew0030.dakimakuramod.DakimakuraModCommon;
import com.github.andrew0030.dakimakuramod.dakimakura.BatchPackListHelper;
import com.github.andrew0030.dakimakuramod.dakimakura.Daki;
import com.github.andrew0030.dakimakuramod.dakimakura.serialize.DakiTagSerializer;
import com.github.andrew0030.dakimakuramod.network.CommandClientPayload;
import com.github.andrew0030.dakimakuramod.network.DMNetworkForge;
import com.github.andrew0030.dakimakuramod.network.RequestTexturesPayload;
import com.github.andrew0030.dakimakuramod.network.SendDakiListPayload;
import com.github.andrew0030.dakimakuramod.network.SendTexturesPayload;
import com.github.andrew0030.dakimakuramod.registries.CreativeTabHelper;
import com.github.andrew0030.dakimakuramod.registries.DMRegistriesForge;
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
import net.minecraftforge.network.PacketDistributor;

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
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    @Override
    public Block getDakimakuraBlock()
    {
        return DMRegistriesForge.DAKIMAKURA_BLOCK.get();
    }

    @Override
    public Item getDakimakuraItem()
    {
        return DMRegistriesForge.DAKIMAKURA_ITEM.get();
    }

    @Override
    public Item getDakimakuraDesignItem()
    {
        return DMRegistriesForge.DAKIMAKURA_DESIGN.get();
    }

    @Override
    public BlockEntityType<?> getDakimakuraBlockEntityType()
    {
        return DMRegistriesForge.DAKIMAKURA_BLOCK_ENTITY.get();
    }

    @Override
    public EntityType<?> getDakimakuraEntityType()
    {
        return DMRegistriesForge.DAKIMAKURA_ENTITY.get();
    }

    @Override
    public RecipeSerializer<?> getDakimakuraDesignRecipeSerializer()
    {
        return DMRegistriesForge.DAKIMAKURA_DESIGN_RECIPE.get();
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
        for (BatchPackListHelper.PackListBatch batch : BatchPackListHelper.createBatches(DakimakuraModCommon.getDakimakuraManager().getDakiPacksList()))
            DMNetworkForge.CHANNEL.send(SendDakiListPayload.fromBatch(batch), PacketDistributor.ALL.noArg());
    }

    @Override
    public void sendDakiListToPlayer(ServerPlayer player)
    {
        for (BatchPackListHelper.PackListBatch batch : BatchPackListHelper.createBatches(DakimakuraModCommon.getDakimakuraManager().getDakiPacksList()))
            DMNetworkForge.CHANNEL.send(SendDakiListPayload.fromBatch(batch), PacketDistributor.PLAYER.with(player));
    }

    @Override
    public void sendTexturesChunk(ServerPlayer player, String path, Daki daki, int sizeFront, int sizeBack, int packetsNeeded, int idx, byte[] data)
    {
        DMNetworkForge.CHANNEL.send(new SendTexturesPayload(path, daki, sizeFront, sizeBack, packetsNeeded, idx, data), PacketDistributor.PLAYER.with(player));
    }

    @Override
    public void sendOpenPackFolder(ServerPlayer player)
    {
        DMNetworkForge.CHANNEL.send(new CommandClientPayload(CommandClientPayload.CommandType.OPEN_PACK_FOLDER), PacketDistributor.PLAYER.with(player));
    }

    @Override
    public void clientRequestTextures(Daki daki)
    {
        DMNetworkForge.CHANNEL.send(new RequestTexturesPayload(daki), PacketDistributor.SERVER.noArg());
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
