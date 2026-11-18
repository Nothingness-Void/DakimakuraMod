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
 * Shared bed interaction logic (breaking and sneak-flipping Dakimakura entities on beds).
 */
public final class BedInteractionHandler
{
    private BedInteractionHandler() {}

    /**
     * @return {@code true} if the left-click event should be cancelled (creative mode protection for the underlying bed)
     */
    public static boolean handleLeftClickBed(Player player, Level level, BlockPos pos)
    {
        BlockState state = level.getBlockState(pos);
        if (!Services.getPlatform().isBed(level, pos, state))
            return false;

        List<Dakimakura> list = level.getEntitiesOfClass(Dakimakura.class, new AABB(pos));
        if (list.isEmpty())
            return false;

        boolean cancel = false;
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
                BlockPos offsetPos = bottomPos.distanceToSqr(pos.getCenter()) <= 1
                        ? pos.relative(dakimakura.getRotation())
                        : pos.relative(dakimakura.getRotation().getOpposite());
                BlockState dakiState = Services.getPlatform().getDakimakuraBlock().defaultBlockState();
                level.addDestroyBlockEffect(pos.above(), dakiState.setValue(DakimakuraBlock.FACING, dakimakura.getRotation()));
                level.addDestroyBlockEffect(offsetPos.above(), dakiState.setValue(DakimakuraBlock.FACING, dakimakura.getRotation().getOpposite()));
                if (player.isCreative())
                    cancel = true;
            }
        }
        return cancel;
    }

    /**
     * @return {@code true} if a Dakimakura on the bed was sneak-flipped (and the bed click should be cancelled)
     */
    public static boolean handleRightClickBed(Player player, Level level, BlockPos pos, InteractionHand hand)
    {
        BlockState state = level.getBlockState(pos);
        if (!Services.getPlatform().isBed(level, pos, state))
            return false;

        List<Dakimakura> list = level.getEntitiesOfClass(Dakimakura.class, new AABB(pos));
        if (list.isEmpty())
            return false;

        boolean cancel = false;
        for (Dakimakura dakimakura : list)
        {
            Vec3 bottomPos = dakimakura.position();
            Vec3 topPos = bottomPos.relative(dakimakura.getRotation(), 1);
            if (bottomPos.distanceToSqr(pos.getCenter()) <= 1 || topPos.distanceToSqr(pos.getCenter()) <= 1)
            {
                if (player.isShiftKeyDown())
                {
                    player.swing(InteractionHand.MAIN_HAND);
                    if (hand == InteractionHand.MAIN_HAND && !level.isClientSide())
                        dakimakura.setFlipped(!dakimakura.isFlipped());
                    cancel = true;
                }
            }
        }
        return cancel;
    }
}
