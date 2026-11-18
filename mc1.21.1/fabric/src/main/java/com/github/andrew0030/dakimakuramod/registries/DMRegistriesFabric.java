package com.github.andrew0030.dakimakuramod.registries;

import com.github.andrew0030.dakimakuramod.DakimakuraModCommon;
import com.github.andrew0030.dakimakuramod.block_entities.dakimakura.DakimakuraBlockEntity;
import com.github.andrew0030.dakimakuramod.blocks.DakimakuraBlock;
import com.github.andrew0030.dakimakuramod.entities.dakimakura.Dakimakura;
import com.github.andrew0030.dakimakuramod.items.DakimakuraDesignItem;
import com.github.andrew0030.dakimakuramod.items.DakimakuraItem;
import com.github.andrew0030.dakimakuramod.platform.Services;
import com.github.andrew0030.dakimakuramod.recipes.DakimakuraDesignRecipe;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class DMRegistriesFabric
{
    public static DakimakuraBlock DAKIMAKURA_BLOCK;
    public static DakimakuraItem DAKIMAKURA_ITEM;
    public static DakimakuraDesignItem DAKIMAKURA_DESIGN;
    public static EntityType<Dakimakura> DAKIMAKURA_ENTITY;
    public static BlockEntityType<DakimakuraBlockEntity> DAKIMAKURA_BLOCK_ENTITY;
    public static CreativeModeTab DAKIMAKURA_TAB;
    public static RecipeSerializer<DakimakuraDesignRecipe> DAKIMAKURA_DESIGN_RECIPE;

    private DMRegistriesFabric() {}

    public static void register()
    {
        DAKIMAKURA_BLOCK = Registry.register(
                BuiltInRegistries.BLOCK,
                Services.getPlatform().id(DakimakuraModCommon.MODID, "dakimakura"),
                new DakimakuraBlock(BlockBehaviour.Properties.of().mapColor(DyeColor.WHITE).sound(SoundType.WOOL).strength(0.2F).noOcclusion())
        );

        DAKIMAKURA_ITEM = Registry.register(
                BuiltInRegistries.ITEM,
                Services.getPlatform().id(DakimakuraModCommon.MODID, "dakimakura"),
                new DakimakuraItem(DAKIMAKURA_BLOCK, new Item.Properties().stacksTo(1))
        );

        DAKIMAKURA_DESIGN = Registry.register(
                BuiltInRegistries.ITEM,
                Services.getPlatform().id(DakimakuraModCommon.MODID, "dakimakura_design"),
                new DakimakuraDesignItem(new Item.Properties().stacksTo(16))
        );

        DAKIMAKURA_ENTITY = Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                Services.getPlatform().id(DakimakuraModCommon.MODID, "dakimakura"),
                EntityType.Builder.of(Dakimakura::new, MobCategory.MISC).sized(3.0F, 0.25F).clientTrackingRange(10).build()
        );

        DAKIMAKURA_BLOCK_ENTITY = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                Services.getPlatform().id(DakimakuraModCommon.MODID, "dakimakura"),
                BlockEntityType.Builder.of(DakimakuraBlockEntity::new, DAKIMAKURA_BLOCK).build(null)
        );

        DAKIMAKURA_TAB = Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                Services.getPlatform().id(DakimakuraModCommon.MODID, "tab"),
                FabricItemGroup.builder()
                        .title(Component.translatable("itemGroup." + DakimakuraModCommon.MODID + ".tab"))
                        .icon(() -> new ItemStack(DAKIMAKURA_BLOCK))
                        .displayItems(CreativeTabHelper::populateTab)
                        .build()
        );

        DAKIMAKURA_DESIGN_RECIPE = Registry.register(
                BuiltInRegistries.RECIPE_SERIALIZER,
                Services.getPlatform().id(DakimakuraModCommon.MODID, "dakimakura_design"),
                new SimpleCraftingRecipeSerializer<>(DakimakuraDesignRecipe::new)
        );
    }
}
