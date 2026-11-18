package com.github.andrew0030.dakimakuramod.events;

import com.github.andrew0030.dakimakuramod.blocks.DakimakuraBlock;
import com.github.andrew0030.dakimakuramod.entities.dakimakura.Dakimakura;
import com.github.andrew0030.dakimakuramod.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Shared bed-dakimakura interaction logic for Forge and Fabric.
 */
public final class BedInteractionHandler
{
    private BedInteractionHandler() {}

    /**
     * Handles left-clicking a bed that has a {@link Dakimakura} entity resting on it.
     *
     * @return {@code true} if the bed break should be cancelled (creative mode).
     */
    public static boolean handleLeftClickBed(Level level, Player player, BlockPos pos)
    {
        BlockState state = level.getBlockState(pos);
        if (!Services.getPlatform().isBed(level, pos, state))
            return false;

        List<Dakimakura> list = level.getEntitiesOfClass(Dakimakura.class, new AABB(pos));
        if (list.isEmpty())
            return false;

        boolean cancelBreak = false;
        for (Dakimakura dakimakura : list)
        {
            Vec3 bottomPos = dakimakura.position();
            Vec3 topPos = bottomPos.relative(dakimakura.getRotation(), 1);
            if (bottomPos.distanceToSqr(pos.getCenter()) <= 1 || topPos.distanceToSqr(pos.getCenter()) <= 1)
            {
                if (!level.isClientSide())
                {
                    if (!player.isCreative())
                        dakimakura.dropAsItem();
                    dakimakura.setRemoved(Entity.RemovalReason.DISCARDED);
                }
                level.playSound(player, pos, SoundType.WOOL.getBreakSound(), SoundSource.BLOCKS, (SoundType.WOOL.getVolume() + 1.0F) / 2.0F, SoundType.WOOL.getPitch() * 0.8F);
                BlockPos offsetPos = bottomPos.distanceToSqr(pos.getCenter()) <= 1 ?
                        pos.relative(dakimakura.getRotation()) :
                        pos.relative(dakimakura.getRotation().getOpposite());
                level.addDestroyBlockEffect(pos.above(), Services.getPlatform().getDakimakuraBlock().defaultBlockState().setValue(DakimakuraBlock.FACING, dakimakura.getRotation()));
                level.addDestroyBlockEffect(offsetPos.above(), Services.getPlatform().getDakimakuraBlock().defaultBlockState().setValue(DakimakuraBlock.FACING, dakimakura.getRotation().getOpposite()));
                if (player.isCreative())
                    cancelBreak = true;
            }
        }
        return cancelBreak;
    }

    /**
     * Handles sneak-right-clicking a bed that has a {@link Dakimakura} entity resting on it (flips the pillow).
     *
     * @return {@code true} if the interaction was handled and bed sleep should be cancelled.
     */
    public static boolean handleRightClickBed(Level level, Player player, InteractionHand hand, BlockPos pos)
    {
        if (!player.isShiftKeyDown())
            return false;

        BlockState state = level.getBlockState(pos);
        if (!Services.getPlatform().isBed(level, pos, state))
            return false;

        List<Dakimakura> list = level.getEntitiesOfClass(Dakimakura.class, new AABB(pos));
        if (list.isEmpty())
            return false;

        boolean handled = false;
        for (Dakimakura dakimakura : list)
        {
            Vec3 bottomPos = dakimakura.position();
            Vec3 topPos = bottomPos.relative(dakimakura.getRotation(), 1);
            if (bottomPos.distanceToSqr(pos.getCenter()) <= 1 || topPos.distanceToSqr(pos.getCenter()) <= 1)
            {
                player.swing(InteractionHand.MAIN_HAND);
                if (!level.isClientSide() && hand.equals(InteractionHand.MAIN_HAND))
                    dakimakura.setFlipped(!dakimakura.isFlipped());
                handled = true;
            }
        }
        return handled;
    }
}
