package com.github.andrew0030.dakimakuramod.registries;

import com.github.andrew0030.dakimakuramod.DakimakuraModCommon;
import com.github.andrew0030.dakimakuramod.block_entities.dakimakura.DakimakuraBlockEntity;
import com.github.andrew0030.dakimakuramod.blocks.DakimakuraBlock;
import com.github.andrew0030.dakimakuramod.entities.dakimakura.Dakimakura;
import com.github.andrew0030.dakimakuramod.items.DakimakuraDesignItem;
import com.github.andrew0030.dakimakuramod.items.DakimakuraItem;
import com.github.andrew0030.dakimakuramod.recipes.DakimakuraDesignRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Set;

public final class DMRegistriesNeoForge
{
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, DakimakuraModCommon.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, DakimakuraModCommon.MODID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, DakimakuraModCommon.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, DakimakuraModCommon.MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DakimakuraModCommon.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, DakimakuraModCommon.MODID);

    public static final DeferredHolder<Block, DakimakuraBlock> DAKIMAKURA_BLOCK = BLOCKS.register("dakimakura",
            () -> new DakimakuraBlock(BlockBehaviour.Properties.of().mapColor(DyeColor.WHITE).sound(SoundType.WOOL).strength(0.2F).noOcclusion()));

    public static final DeferredHolder<Item, DakimakuraItem> DAKIMAKURA_ITEM = ITEMS.register("dakimakura",
            () -> new DakimakuraItem(DAKIMAKURA_BLOCK.get(), new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, DakimakuraDesignItem> DAKIMAKURA_DESIGN = ITEMS.register("dakimakura_design",
            () -> new DakimakuraDesignItem(new Item.Properties().stacksTo(16)));

    public static final DeferredHolder<EntityType<?>, EntityType<Dakimakura>> DAKIMAKURA_ENTITY = ENTITIES.register("dakimakura",
            () -> EntityType.Builder.of(Dakimakura::new, MobCategory.MISC).sized(3.0F, 0.25F).clientTrackingRange(10).build("dakimakura"));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DakimakuraBlockEntity>> DAKIMAKURA_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("dakimakura",
            () -> new BlockEntityType<>(DakimakuraBlockEntity::new, Set.of(DAKIMAKURA_BLOCK.get()), null));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> DAKIMAKURA_TAB = TABS.register("tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + DakimakuraModCommon.MODID + ".tab"))
            .icon(() -> new ItemStack(DAKIMAKURA_BLOCK.get()))
            .displayItems(CreativeTabHelper::populateTab)
            .build());

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DakimakuraDesignRecipe>> DAKIMAKURA_DESIGN_RECIPE = RECIPE_SERIALIZERS.register("dakimakura_design",
            () -> new SimpleCraftingRecipeSerializer<>(DakimakuraDesignRecipe::new));

    private DMRegistriesNeoForge() {}

    public static void register(IEventBus modEventBus)
    {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        ENTITIES.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        TABS.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
    }
}
