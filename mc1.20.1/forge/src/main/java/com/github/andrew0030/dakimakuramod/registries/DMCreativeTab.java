package com.github.andrew0030.dakimakuramod.registries;

import com.github.andrew0030.dakimakuramod.DakimakuraModCommon;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class DMCreativeTab
{
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DakimakuraModCommon.MODID);

    public static final RegistryObject<CreativeModeTab> TTC_TAB = TABS.register("tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + DakimakuraModCommon.MODID + ".tab"))
            .icon(() -> new ItemStack(DMBlocks.DAKIMAKURA.get()))
            .displayItems((params, output) -> CreativeTabHelper.populateCreativeTab(output))
            .build());
}
