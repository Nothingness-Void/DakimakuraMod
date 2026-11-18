package com.github.andrew0030.dakimakuramod.registries;

import com.github.andrew0030.dakimakuramod.DakimakuraModCommon;
import com.github.andrew0030.dakimakuramod.entities.dakimakura.Dakimakura;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class DMEntities
{
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, DakimakuraModCommon.MODID);

    public static final RegistryObject<EntityType<Dakimakura>> DAKIMAKURA = ENTITIES.register("dakimakura",
            () -> EntityType.Builder.of(Dakimakura::new, MobCategory.MISC)
                    .sized(0.0F, 0.0F)
                    .clientTrackingRange(10)
                    .build(DakimakuraModCommon.MODID + ":dakimakura"));
}
