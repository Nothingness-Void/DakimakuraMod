package com.github.andrew0030.dakimakuramod.registries;

import com.github.andrew0030.dakimakuramod.DakimakuraMod;
import com.github.andrew0030.dakimakuramod.blocks.DakimakuraBlock;
import com.github.andrew0030.dakimakuramod.items.DakimakuraItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class DMBlocks
{
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, DakimakuraMod.MODID);

    public static final DeferredHolder<Block, Block> DAKIMAKURA = createDakimakuraBlock("dakimakura", () -> new DakimakuraBlock(BlockBehaviour.Properties.of().mapColor(DyeColor.WHITE).sound(SoundType.WOOL).strength(0.2F).noOcclusion()), 1);

    private static <B extends Block> DeferredHolder<Block, B> createDakimakuraBlock(String name, Supplier<? extends B> supplier, int maxStackSize)
    {
        DeferredHolder<Block, B> block = createBlockNoItem(name, supplier);
        DMItems.ITEMS.register(name, () -> new DakimakuraItem(block.get(), new Item.Properties().stacksTo(maxStackSize)));
        return block;
    }

    private static <B extends Block> DeferredHolder<Block, B> createBlockNoItem(String name, Supplier<? extends B> supplier)
    {
        return BLOCKS.register(name, supplier);
    }
}
