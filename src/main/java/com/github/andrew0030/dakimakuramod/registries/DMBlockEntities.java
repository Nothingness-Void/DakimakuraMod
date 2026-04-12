package com.github.andrew0030.dakimakuramod.registries;

import com.github.andrew0030.dakimakuramod.DakimakuraMod;
import com.github.andrew0030.dakimakuramod.block_entities.dakimakura.DakimakuraBlockEntity;
import com.github.andrew0030.dakimakuramod.block_entities.dakimakura.DakimakuraBlockEntityRenderer;
import com.google.common.collect.Sets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DMBlockEntities
{
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, DakimakuraMod.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DakimakuraBlockEntity>> DAKIMAKURA = BLOCK_ENTITY_TYPES.register("dakimakura", () -> new BlockEntityType<>(DakimakuraBlockEntity::new, Sets.newHashSet(DMBlocks.DAKIMAKURA.get()), null));

    public static void registerBlockEntityRenderers()
    {
        BlockEntityRenderers.register(DAKIMAKURA.get(), DakimakuraBlockEntityRenderer::new);
    }
}
