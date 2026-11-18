package com.github.andrew0030.dakimakuramod.mixins;

import com.github.andrew0030.dakimakuramod.DakimakuraModClientCommon;
import com.github.andrew0030.dakimakuramod.dakimakura.Daki;
import com.github.andrew0030.dakimakuramod.dakimakura.serialize.DakiTagSerializer;
import com.github.andrew0030.dakimakuramod.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.ArrayList;
import java.util.Collections;

@Mixin(CreativeModeInventoryScreen.class)
public class CreativeModeInventoryScreenMixin
{
    @ModifyArg(method = "renderTabButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderItem(Lnet/minecraft/world/item/ItemStack;II)V"), index = 0)
    public ItemStack modifyVarRenderTabButton(ItemStack stack)
    {
        if (stack.is(Services.getPlatform().getDakimakuraItem()) && DakimakuraModClientCommon.getDakiTextureManager() != null)
        {
            ArrayList<Daki> cachedDakis = DakimakuraModClientCommon.getDakiTextureManager().getCachedDakis();
            if (!cachedDakis.isEmpty() && Minecraft.getInstance().player != null)
            {
                Collections.sort(cachedDakis);
                ItemStack newStack = new ItemStack(Services.getPlatform().getDakimakuraItem());
                Daki daki = cachedDakis.get(Minecraft.getInstance().player.tickCount / 30 % cachedDakis.size());
                DakiTagSerializer.serialize(daki, newStack);
                return newStack;
            }
        }
        return stack;
    }
}
