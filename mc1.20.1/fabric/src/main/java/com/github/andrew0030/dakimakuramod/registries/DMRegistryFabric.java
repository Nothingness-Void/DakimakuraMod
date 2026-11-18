package com.github.andrew0030.dakimakuramod.registries;

import com.github.andrew0030.dakimakuramod.DakimakuraModCommon;
import com.github.andrew0030.dakimakuramod.block_entities.dakimakura.DakimakuraBlockEntity;
import com.github.andrew0030.dakimakuramod.blocks.DakimakuraBlock;
import com.github.andrew0030.dakimakuramod.entities.dakimakura.Dakimakura;
import com.github.andrew0030.dakimakuramod.items.DakimakuraDesignItem;
import com.github.andrew0030.dakimakuramod.items.DakimakuraItem;
import com.github.andrew0030.dakimakuramod.recipes.DakimakuraDesignRecipe;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public final class DMRegistryFabric
{
    public static final Block DAKIMAKURA_BLOCK = new DakimakuraBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.WOOL)
            .strength(0.2F)
            .sound(SoundType.WOOL)
            .noOcclusion()
            .pushReaction(PushReaction.DESTROY));

    public static final Item DAKIMAKURA_ITEM = new DakimakuraItem(DAKIMAKURA_BLOCK, new Item.Properties().stacksTo(1));
    public static final Item DAKIMAKURA_DESIGN_ITEM = new DakimakuraDesignItem(new Item.Properties().stacksTo(16));

    public static final BlockEntityType<DakimakuraBlockEntity> DAKIMAKURA_BLOCK_ENTITY = FabricBlockEntityTypeBuilder
            .create(DakimakuraBlockEntity::new, DAKIMAKURA_BLOCK)
            .build();

    public static final EntityType<Dakimakura> DAKIMAKURA_ENTITY = FabricEntityTypeBuilder
            .<Dakimakura>create(MobCategory.MISC, Dakimakura::new)
            .dimensions(EntityDimensions.fixed(0.0F, 0.0F))
            .trackRangeChunks(10)
            .build();

    public static final RecipeSerializer<DakimakuraDesignRecipe> DAKIMAKURA_DESIGN_RECIPE = new SimpleCraftingRecipeSerializer<>(DakimakuraDesignRecipe::new);

    public static final CreativeModeTab DAKIMAKURA_TAB = FabricItemGroup.builder()
            .title(Component.translatable("itemGroup." + DakimakuraModCommon.MODID + ".tab"))
            .icon(() -> new ItemStack(DAKIMAKURA_BLOCK))
            .displayItems((params, output) -> CreativeTabHelper.populateCreativeTab(output))
            .build();

    private DMRegistryFabric() {}

    public static void register()
    {
        Registry.register(BuiltInRegistries.BLOCK, id("dakimakura"), DAKIMAKURA_BLOCK);
        Registry.register(BuiltInRegistries.ITEM, id("dakimakura"), DAKIMAKURA_ITEM);
        Registry.register(BuiltInRegistries.ITEM, id("dakimakura_design"), DAKIMAKURA_DESIGN_ITEM);
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("dakimakura"), DAKIMAKURA_BLOCK_ENTITY);
        Registry.register(BuiltInRegistries.ENTITY_TYPE, id("dakimakura"), DAKIMAKURA_ENTITY);
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id("dakimakura_design"), DAKIMAKURA_DESIGN_RECIPE);
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id("tab"), DAKIMAKURA_TAB);
    }

    private static ResourceLocation id(String path)
    {
        return new ResourceLocation(DakimakuraModCommon.MODID, path);
    }
}
