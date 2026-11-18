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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.Nullable;
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
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        ItemStack stack = context.getItemInHand();

        if (Services.getPlatform().isBed(level, pos, state))
        {
            if (this.isDakiEntityOnBlock(level, pos))
                return InteractionResult.FAIL;

            if (!level.isClientSide())
            {
                Daki daki = DakiTagSerializer.deserializeFromStack(stack);
                @SuppressWarnings("unchecked")
                EntityType<Dakimakura> type = (EntityType<Dakimakura>) Services.getPlatform().getDakimakuraEntityType();
                Dakimakura dakimakura = new Dakimakura(type, level);
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
        return super.useOn(context);
    }

    private boolean isDakiEntityOnBlock(Level level, BlockPos pos)
    {
        List<Dakimakura> list = level.getEntitiesOfClass(Dakimakura.class, new AABB(pos));
        for (Dakimakura dakimakura : list)
            if (dakimakura.distanceToSqr(pos.getCenter()) <= 1)
                return true;
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag)
    {
        super.appendHoverText(stack, level, tooltip, flag);
        List<Component> myToolTips = new ArrayList<>();
        myToolTips.add(Component.translatable("tooltip.dakimakuramod.dakimakura"));
        Daki daki = DakiTagSerializer.deserializeFromStack(stack);
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
        return DakiTagSerializer.isStackFlipped(itemStack);
    }

    public static ItemStack setFlipped(ItemStack itemStack, boolean flipped)
    {
        return DakiTagSerializer.setStackFlipped(itemStack, flipped);
    }
}
