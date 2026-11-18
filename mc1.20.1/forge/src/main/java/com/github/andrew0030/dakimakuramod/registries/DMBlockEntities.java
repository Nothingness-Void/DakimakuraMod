package com.github.andrew0030.dakimakuramod.registries;

import com.github.andrew0030.dakimakuramod.DakimakuraModCommon;
import com.github.andrew0030.dakimakuramod.block_entities.dakimakura.DakimakuraBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class DMBlockEntities
{
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, DakimakuraModCommon.MODID);

    public static final RegistryObject<BlockEntityType<DakimakuraBlockEntity>> DAKIMAKURA = BLOCK_ENTITY_TYPES.register("dakimakura",
            () -> BlockEntityType.Builder.of(DakimakuraBlockEntity::new, DMBlocks.DAKIMAKURA.get()).build(null));
}
