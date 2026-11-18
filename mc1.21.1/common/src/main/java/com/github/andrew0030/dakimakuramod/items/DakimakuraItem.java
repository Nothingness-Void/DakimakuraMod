package com.github.andrew0030.dakimakuramod.items;

import com.github.andrew0030.dakimakuramod.dakimakura.Daki;
import com.github.andrew0030.dakimakuramod.dakimakura.serialize.DakiTagSerializer;
import com.github.andrew0030.dakimakuramod.entities.dakimakura.Dakimakura;
import com.github.andrew0030.dakimakuramod.platform.Services;
import com.github.andrew0030.dakimakuramod.util.TranslationHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public class DakimakuraItem extends BlockItem
{
    public DakimakuraItem(Block block, Properties properties)
    {
        super(block, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        ItemStack itemStack = player.getItemInHand(hand);
        if (player.isShiftKeyDown())
        {
            boolean flipped = isFlipped(itemStack);
            itemStack = setFlipped(itemStack, !flipped);
            return InteractionResultHolder.consume(itemStack);
        }
        return InteractionResultHolder.pass(itemStack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        InteractionResult bedResult = tryPlaceOnBed(context);
        if (bedResult != null)
            return bedResult;
        return super.useOn(context);
    }

    public static InteractionResult tryPlaceOnBed(UseOnContext context)
    {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        ItemStack stack = context.getItemInHand();

        if (!Services.getPlatform().isBed(level, pos, state))
            return null;

        if (isDakiEntityOnBlock(level, pos))
            return InteractionResult.FAIL;

        if (!level.isClientSide())
        {
            Daki daki = DakiTagSerializer.deserialize(stack);
            @SuppressWarnings("unchecked")
            EntityType<Dakimakura> entityType = (EntityType<Dakimakura>) Services.getPlatform().getDakimakuraEntityType();
            Dakimakura dakimakura = new Dakimakura(entityType, level);
            dakimakura.setPos(pos.getX() + 0.5D, pos.getY() + 0.5625D, pos.getZ() + 0.5D);
            dakimakura.setDaki(daki);
            dakimakura.setFlipped(isFlipped(stack));
            dakimakura.setRotation(context.getHorizontalDirection());
            level.addFreshEntity(dakimakura);
            level.playSound(null, pos, SoundType.WOOL.getPlaceSound(), SoundSource.BLOCKS, (SoundType.WOOL.getVolume() + 1.0F) / 2.0F, SoundType.WOOL.getPitch() * 0.8F);
            if (player != null && !player.isCreative())
                stack.shrink(1);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static boolean isDakiEntityOnBlock(Level level, BlockPos pos)
    {
        List<Dakimakura> list = level.getEntitiesOfClass(Dakimakura.class, new AABB(pos));
        for (Dakimakura dakimakura : list)
        {
            if (dakimakura.distanceToSqr(pos.getCenter()) <= 1)
                return true;
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag)
    {
        super.appendHoverText(stack, context, tooltip, flag);
        List<Component> myToolTips = new ArrayList<>();
        myToolTips.add(Component.translatable("tooltip.dakimakuramod.dakimakura"));
        Daki daki = DakiTagSerializer.deserialize(stack);
        if (daki != null)
        {
            myToolTips.add(Component.translatable("tooltip.dakimakuramod.dakimakura.flip"));
            daki.addInformation(stack, myToolTips);
        }
        else
        {
            myToolTips.add(Component.translatable("tooltip.dakimakuramod.dakimakura.blank"));
        }

        TranslationHelper.formatTooltips(myToolTips);
        tooltip.addAll(myToolTips);
    }

    public static boolean isFlipped(ItemStack itemStack)
    {
        if (itemStack == null || !DakiTagSerializer.hasTag(itemStack))
            return false;
        return DakiTagSerializer.isFlipped(itemStack);
    }

    public static ItemStack setFlipped(ItemStack itemStack, boolean flipped)
    {
        if (itemStack == null)
            return null;
        DakiTagSerializer.setFlipped(itemStack, flipped);
        return itemStack;
    }
}
