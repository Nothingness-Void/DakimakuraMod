package com.github.andrew0030.dakimakuramod.registries;

import com.github.andrew0030.dakimakuramod.DakimakuraModCommon;
import com.github.andrew0030.dakimakuramod.dakimakura.Daki;
import com.github.andrew0030.dakimakuramod.dakimakura.serialize.DakiTagSerializer;
import com.github.andrew0030.dakimakuramod.mixins.CreativeModeTabsAccessor;
import com.github.andrew0030.dakimakuramod.platform.Services;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;

public final class CreativeTabHelper
{
    private CreativeTabHelper() {}

    public static void populateCreativeTab(CreativeModeTab.Output output)
    {
        output.accept(Services.getPlatform().getDakimakuraDesignItem());
        output.accept(Services.getPlatform().getDakimakuraBlock());
        if (DakimakuraModCommon.getDakimakuraManager() == null)
            return;

        ArrayList<Daki> dakiList = DakimakuraModCommon.getDakimakuraManager().getDakiList();
        Collections.sort(dakiList);
        for (Daki daki : dakiList)
        {
            ItemStack itemStack = new ItemStack(Services.getPlatform().getDakimakuraBlock());
            DakiTagSerializer.serializeToStack(daki, itemStack);
            DakiTagSerializer.setStackFlipped(itemStack, false);
            output.accept(itemStack);
        }
    }

    public static void reloadTabContents()
    {
        CreativeModeTabsAccessor.setCachedParameters(null);
    }
}
