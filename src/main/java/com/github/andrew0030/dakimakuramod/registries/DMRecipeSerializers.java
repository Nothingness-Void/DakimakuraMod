package com.github.andrew0030.dakimakuramod.registries;

import com.github.andrew0030.dakimakuramod.DakimakuraMod;
import com.github.andrew0030.dakimakuramod.recipes.DakimakuraDesignRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DMRecipeSerializers
{
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, DakimakuraMod.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DakimakuraDesignRecipe>> DAKIMAKURA_DESIGN = RECIPE_SERIALIZERS.register("dakimakura_design", () -> new SimpleCraftingRecipeSerializer<>(DakimakuraDesignRecipe::new));
}
