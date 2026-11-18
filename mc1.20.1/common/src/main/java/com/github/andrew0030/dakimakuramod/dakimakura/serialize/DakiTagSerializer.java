package com.github.andrew0030.dakimakuramod.dakimakura.serialize;

import com.github.andrew0030.dakimakuramod.DakimakuraModCommon;
import com.github.andrew0030.dakimakuramod.dakimakura.Daki;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

/**
 * Helper class for storing and retrieving Daki references in {@link CompoundTag} and {@link ItemStack} (MC 1.20.1).
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
        if (compound == null || DakimakuraModCommon.getDakimakuraManager() == null)
            return null;
        if (compound.contains(PACK_NAME_KEY, Tag.TAG_STRING) && compound.contains(DIR_NAME_KEY, Tag.TAG_STRING))
        {
            String packName = compound.getString(PACK_NAME_KEY);
            String dirName = compound.getString(DIR_NAME_KEY);
            return DakimakuraModCommon.getDakimakuraManager().getDakiFromMap(packName, dirName);
        }
        return null;
    }

    public static void serializeToStack(Daki daki, ItemStack stack)
    {
        if (stack == null || daki == null)
            return;
        CompoundTag tag = stack.getOrCreateTag();
        serialize(daki, tag);
    }

    public static Daki deserializeFromStack(ItemStack stack)
    {
        if (stack == null || !stack.hasTag())
            return null;
        return deserialize(stack.getTag());
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

    public static boolean isStackFlipped(ItemStack stack)
    {
        if (stack == null || !stack.hasTag())
            return false;
        return isFlipped(stack.getTag());
    }

    public static ItemStack setStackFlipped(ItemStack stack, boolean flipped)
    {
        if (stack == null)
            return null;
        CompoundTag tag = stack.getOrCreateTag();
        setFlipped(tag, flipped);
        return stack;
    }
}
