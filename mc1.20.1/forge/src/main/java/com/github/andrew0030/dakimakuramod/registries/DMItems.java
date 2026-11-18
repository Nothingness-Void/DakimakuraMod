package com.github.andrew0030.dakimakuramod.registries;

import com.github.andrew0030.dakimakuramod.DakimakuraModCommon;
import com.github.andrew0030.dakimakuramod.items.DakimakuraDesignItem;
import com.github.andrew0030.dakimakuramod.items.ForgeDakimakuraItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class DMItems
{
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, DakimakuraModCommon.MODID);

    public static final RegistryObject<Item> DAKIMAKURA = ITEMS.register("dakimakura",
            () -> new ForgeDakimakuraItem(DMBlocks.DAKIMAKURA.get(), new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> DAKIMAKURA_DESIGN = ITEMS.register("dakimakura_design",
            () -> new DakimakuraDesignItem(new Item.Properties().stacksTo(16)));
}
