package com.github.andrew0030.dakimakuramod.mixins;

import com.github.andrew0030.dakimakuramod.DakimakuraModClientCommon;
import com.github.andrew0030.dakimakuramod.dakimakura.Daki;
import com.github.andrew0030.dakimakuramod.dakimakura.client.DakiTextureManagerClient;
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
    private static final long CACHE_REFRESH_MS = 1000L;
    private static ArrayList<Daki> cachedSortedDakis = new ArrayList<>();
    private static long lastCacheUpdateMs = 0L;

    @ModifyArg(method = "renderTabButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderItem(Lnet/minecraft/world/item/ItemStack;II)V"), index = 0)
    public ItemStack modifyVarRenderTabButton(ItemStack stack)
    {
        if (stack.is(Services.getPlatform().getDakimakuraItem()))
        {
            DakiTextureManagerClient manager = DakimakuraModClientCommon.getDakiTextureManager();
            if (manager == null)
                return stack;

            long now = System.currentTimeMillis();
            if (now - lastCacheUpdateMs >= CACHE_REFRESH_MS)
            {
                ArrayList<Daki> fresh = manager.getCachedDakis();
                Collections.sort(fresh);
                cachedSortedDakis = fresh;
                lastCacheUpdateMs = now;
            }
            if (!cachedSortedDakis.isEmpty() && Minecraft.getInstance().player != null)
            {
                ItemStack newStack = new ItemStack(Services.getPlatform().getDakimakuraBlock());
                Daki daki = cachedSortedDakis.get(Minecraft.getInstance().player.tickCount / 30 % cachedSortedDakis.size());
                DakiTagSerializer.serializeToStack(daki, newStack);
                return newStack;
            }
        }
        return stack;
    }
}
