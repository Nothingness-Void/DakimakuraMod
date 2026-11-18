package com.github.andrew0030.dakimakuramod.items;

import com.github.andrew0030.dakimakuramod.block_entities.util.DMBlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class DakimakuraItemForge extends DakimakuraItem
{
    public DakimakuraItemForge(Block block, Properties properties)
    {
        super(block, properties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context)
    {
        InteractionResult bedResult = tryPlaceOnBed(context);
        return bedResult != null ? bedResult : InteractionResult.PASS;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer)
    {
        consumer.accept(new IClientItemExtensions()
        {
            private DMBlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer()
            {
                if (this.renderer == null)
                    this.renderer = new DMBlockEntityWithoutLevelRenderer();
                return this.renderer;
            }
        });
    }
}
