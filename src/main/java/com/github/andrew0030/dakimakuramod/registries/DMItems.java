package com.github.andrew0030.dakimakuramod.registries;

import com.github.andrew0030.dakimakuramod.DakimakuraMod;
import com.github.andrew0030.dakimakuramod.dakimakura.Daki;
import com.github.andrew0030.dakimakuramod.dakimakura.serialize.DakiTagSerializer;
import com.github.andrew0030.dakimakuramod.items.DakimakuraDesignItem;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DMItems
{
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, DakimakuraMod.MODID);

    public static final DeferredHolder<Item, Item> DAKIMAKURA_DESIGN = ITEMS.register("dakimakura_design", () -> new DakimakuraDesignItem(new Item.Properties().stacksTo(16)));

    public static void registerItemProperties()
    {
        ItemProperties.register(DAKIMAKURA_DESIGN.get(), ResourceLocation.fromNamespaceAndPath(DakimakuraMod.MODID, "unlocked"), (stack, level, entity, seed) -> DakiTagSerializer.deserialize(stack) == null ? 0.0F : 1.0F);
    }
}
