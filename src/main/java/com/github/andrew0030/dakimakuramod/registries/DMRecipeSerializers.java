package com.github.andrew0030.dakimakuramod.registries;

import com.github.andrew0030.dakimakuramod.DakimakuraMod;
import com.github.andrew0030.dakimakuramod.recipes.DakimakuraDesignRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class DMRecipeSerializers
{
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, DakimakuraMod.MODID);

    public static final RegistryObject<RecipeSerializer<DakimakuraDesignRecipe>> DAKIMAKURA_DESIGN = RECIPE_SERIALIZERS.register("dakimakura_design", () -> new SimpleCraftingRecipeSerializer<>(DakimakuraDesignRecipe::new));
}
