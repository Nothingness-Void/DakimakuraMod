package com.github.andrew0030.dakimakuramod.registries;

import com.github.andrew0030.dakimakuramod.DakimakuraMod;
import com.github.andrew0030.dakimakuramod.entities.dakimakura.Dakimakura;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DMEntities
{
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, DakimakuraMod.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<Dakimakura>> DAKIMAKURA = ENTITIES.register("dakimakura", () -> EntityType.Builder.of(Dakimakura::new, MobCategory.MISC).sized(3.0F, 0.25F).clientTrackingRange(10).build(ResourceLocation.fromNamespaceAndPath(DakimakuraMod.MODID, "dakimakura").toString()));
}
