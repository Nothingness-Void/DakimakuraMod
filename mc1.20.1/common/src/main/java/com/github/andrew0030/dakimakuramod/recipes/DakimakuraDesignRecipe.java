package com.github.andrew0030.dakimakuramod.recipes;

import com.github.andrew0030.dakimakuramod.dakimakura.serialize.DakiTagSerializer;
import com.github.andrew0030.dakimakuramod.platform.Services;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class DakimakuraDesignRecipe extends CustomRecipe
{
    public DakimakuraDesignRecipe(ResourceLocation id, CraftingBookCategory category)
    {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level)
    {
        return this.matchesApplyDesign(container) || this.matchesClearDesign(container);
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess)
    {
        if (this.matchesApplyDesign(container))
        {
            ItemStack blankDakimakura = this.findBlankDakimakura(container);
            ItemStack unlockedDesign = this.findUnlockedDesign(container);
            ItemStack result = new ItemStack(Services.getPlatform().getDakimakuraBlock());
            CompoundTag tag = unlockedDesign.getTag() != null ? unlockedDesign.getTag().copy() : new CompoundTag();
            DakiTagSerializer.setFlipped(tag, DakiTagSerializer.isFlipped(blankDakimakura.getTag()));
            result.setTag(tag);
            return result;
        }
        if (this.matchesClearDesign(container))
            return new ItemStack(Services.getPlatform().getDakimakuraBlock());

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
        return Services.getPlatform().getDakimakuraDesignRecipeSerializer();
    }

    private boolean matchesApplyDesign(CraftingContainer container)
    {
        int blankDakimakuras = 0;
        int unlockedDesigns = 0;
        for (int i = 0; i < container.getContainerSize(); i++)
        {
            ItemStack stack = container.getItem(i);
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

    private boolean matchesClearDesign(CraftingContainer container)
    {
        int designedDakimakuras = 0;
        int whiteDyes = 0;
        for (int i = 0; i < container.getContainerSize(); i++)
        {
            ItemStack stack = container.getItem(i);
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

    private ItemStack findBlankDakimakura(CraftingContainer container)
    {
        for (int i = 0; i < container.getContainerSize(); i++)
        {
            ItemStack stack = container.getItem(i);
            if (this.isBlankDakimakura(stack))
                return stack;
        }
        return ItemStack.EMPTY;
    }

    private ItemStack findUnlockedDesign(CraftingContainer container)
    {
        for (int i = 0; i < container.getContainerSize(); i++)
        {
            ItemStack stack = container.getItem(i);
            if (this.isUnlockedDesign(stack))
                return stack;
        }
        return ItemStack.EMPTY;
    }

    private boolean isBlankDakimakura(ItemStack stack)
    {
        return stack.is(Services.getPlatform().getDakimakuraItem()) && DakiTagSerializer.deserializeFromStack(stack) == null;
    }

    private boolean isDesignedDakimakura(ItemStack stack)
    {
        return stack.is(Services.getPlatform().getDakimakuraItem()) && DakiTagSerializer.deserializeFromStack(stack) != null;
    }

    private boolean isUnlockedDesign(ItemStack stack)
    {
        return stack.is(Services.getPlatform().getDakimakuraDesignItem()) && DakiTagSerializer.deserializeFromStack(stack) != null;
    }
}
