package com.github.andrew0030.dakimakuramod.dakimakura.serialize;

import com.github.andrew0030.dakimakuramod.DakimakuraModCommon;
import com.github.andrew0030.dakimakuramod.dakimakura.Daki;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.function.Consumer;

/**
 * Helper class for storing and retrieving Daki metadata from {@link CompoundTag} and {@link ItemStack} {@link DataComponents#CUSTOM_DATA}.
 */
public final class DakiTagSerializer
{
    public static final String PACK_NAME_KEY = DakiTagConstants.PACK_NAME_KEY;
    public static final String DIR_NAME_KEY = DakiTagConstants.DIR_NAME_KEY;
    public static final String FACING_KEY = DakiTagConstants.FACING_KEY;
    public static final String FLIPPED_KEY = DakiTagConstants.FLIPPED_KEY;

    private DakiTagSerializer() {}

    public static CompoundTag serialize(Daki daki)
    {
        return serialize(daki, new CompoundTag());
    }

    public static CompoundTag serialize(Daki daki, CompoundTag compound)
    {
        if (compound == null)
            return serialize(daki);
        if (daki != null)
        {
            compound.putString(PACK_NAME_KEY, daki.getPackDirectoryName());
            compound.putString(DIR_NAME_KEY, daki.getDakiDirectoryName());
        }
        return compound;
    }

    public static Daki deserialize(CompoundTag compound)
    {
        if (compound == null)
            return null;
        if (compound.contains(PACK_NAME_KEY, Tag.TAG_STRING) && compound.contains(DIR_NAME_KEY, Tag.TAG_STRING))
        {
            String packName = compound.getString(PACK_NAME_KEY);
            String dirName = compound.getString(DIR_NAME_KEY);
            return DakimakuraModCommon.getDakimakuraManager().getDakiFromMap(packName, dirName);
        }
        return null;
    }

    public static void serialize(Daki daki, ItemStack stack)
    {
        if (stack == null || daki == null)
            return;
        updateTag(stack, compound -> serialize(daki, compound));
    }

    public static Daki deserialize(ItemStack stack)
    {
        return deserialize(getTag(stack));
    }

    public static void setFlipped(CompoundTag compound, boolean flipped)
    {
        if (compound == null)
            return;
        compound.putBoolean(FLIPPED_KEY, flipped);
    }

    public static boolean isFlipped(CompoundTag compound)
    {
        if (compound == null)
            return false;
        return compound.getBoolean(FLIPPED_KEY);
    }

    public static void setFlipped(ItemStack stack, boolean flipped)
    {
        if (stack == null)
            return;
        updateTag(stack, compound -> setFlipped(compound, flipped));
    }

    public static boolean isFlipped(ItemStack stack)
    {
        return isFlipped(getTag(stack));
    }

    public static boolean hasTag(ItemStack stack)
    {
        return stack != null && !stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).isEmpty();
    }

    public static CompoundTag getTag(ItemStack stack)
    {
        if (stack == null)
            return null;
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    public static void setTag(ItemStack stack, CompoundTag compound)
    {
        if (stack == null || compound == null)
            return;
        CustomData.set(DataComponents.CUSTOM_DATA, stack, compound);
    }

    public static void updateTag(ItemStack stack, Consumer<CompoundTag> updater)
    {
        if (stack == null)
            return;
        CustomData.update(DataComponents.CUSTOM_DATA, stack, updater);
    }
}
