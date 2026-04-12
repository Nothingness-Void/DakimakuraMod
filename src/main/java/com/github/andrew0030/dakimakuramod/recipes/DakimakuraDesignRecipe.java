package com.github.andrew0030.dakimakuramod.recipes;

import com.github.andrew0030.dakimakuramod.dakimakura.serialize.DakiTagSerializer;
import com.github.andrew0030.dakimakuramod.registries.DMBlocks;
import com.github.andrew0030.dakimakuramod.registries.DMItems;
import com.github.andrew0030.dakimakuramod.registries.DMRecipeSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class DakimakuraDesignRecipe extends CustomRecipe
{
    public DakimakuraDesignRecipe(CraftingBookCategory category)
    {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level)
    {
        return this.matchesApplyDesign(input) || this.matchesClearDesign(input);
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries)
    {
        if (this.matchesApplyDesign(input))
        {
            ItemStack blankDakimakura = this.findBlankDakimakura(input);
            ItemStack unlockedDesign = this.findUnlockedDesign(input);
            ItemStack result = new ItemStack(DMBlocks.DAKIMAKURA.get());
            CompoundTag tag = DakiTagSerializer.getTag(unlockedDesign);
            if (tag == null)
                tag = new CompoundTag();
            DakiTagSerializer.setFlipped(tag, DakiTagSerializer.isFlipped(blankDakimakura));
            DakiTagSerializer.setTag(result, tag);
            return result;
        }
        if (this.matchesClearDesign(input))
            return new ItemStack(DMBlocks.DAKIMAKURA.get());

        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height)
    {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return DMRecipeSerializers.DAKIMAKURA_DESIGN.get();
    }

    private boolean matchesApplyDesign(CraftingInput input)
    {
        int blankDakimakuras = 0;
        int unlockedDesigns = 0;
        for (int i = 0; i < input.size(); i++)
        {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty())
                continue;
            if (this.isBlankDakimakura(stack))
            {
                blankDakimakuras++;
                continue;
            }
            if (this.isUnlockedDesign(stack))
            {
                unlockedDesigns++;
                continue;
            }
            return false;
        }
        return blankDakimakuras == 1 && unlockedDesigns == 1;
    }

    private boolean matchesClearDesign(CraftingInput input)
    {
        int designedDakimakuras = 0;
        int whiteDyes = 0;
        for (int i = 0; i < input.size(); i++)
        {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty())
                continue;
            if (this.isDesignedDakimakura(stack))
            {
                designedDakimakuras++;
                continue;
            }
            if (stack.is(Items.WHITE_DYE))
            {
                whiteDyes++;
                continue;
            }
            return false;
        }
        return designedDakimakuras == 1 && whiteDyes == 1;
    }

    private ItemStack findBlankDakimakura(CraftingInput input)
    {
        for (int i = 0; i < input.size(); i++)
        {
            ItemStack stack = input.getItem(i);
            if (this.isBlankDakimakura(stack))
                return stack;
        }
        return ItemStack.EMPTY;
    }

    private ItemStack findUnlockedDesign(CraftingInput input)
    {
        for (int i = 0; i < input.size(); i++)
        {
            ItemStack stack = input.getItem(i);
            if (this.isUnlockedDesign(stack))
                return stack;
        }
        return ItemStack.EMPTY;
    }

    private boolean isBlankDakimakura(ItemStack stack)
    {
        return stack.is(DMBlocks.DAKIMAKURA.get().asItem()) && DakiTagSerializer.deserialize(stack) == null;
    }

    private boolean isDesignedDakimakura(ItemStack stack)
    {
        return stack.is(DMBlocks.DAKIMAKURA.get().asItem()) && DakiTagSerializer.deserialize(stack) != null;
    }

    private boolean isUnlockedDesign(ItemStack stack)
    {
        return stack.is(DMItems.DAKIMAKURA_DESIGN.get()) && DakiTagSerializer.deserialize(stack) != null;
    }
}
