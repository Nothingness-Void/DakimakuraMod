package com.github.andrew0030.dakimakuramod.events;

import com.github.andrew0030.dakimakuramod.platform.Services;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

/**
 * Shared mob-drop logic for {@link com.github.andrew0030.dakimakuramod.platform.IPlatformHelper}.
 */
public final class MobDropHandler
{
    private static final float DAKIMAKURA_DESIGN_DROP_CHANCE = 0.05F;

    private MobDropHandler() {}

    public static void onLivingDrops(LivingEntity entity, Entity killer, Consumer<ItemEntity> dropConsumer)
    {
        if (dropConsumer == null || !(killer instanceof Player) || !(entity instanceof Enemy))
            return;
        if (entity.getRandom().nextFloat() >= DAKIMAKURA_DESIGN_DROP_CHANCE)
            return;

        dropConsumer.accept(new ItemEntity(
                entity.level(),
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                new ItemStack(Services.getPlatform().getDakimakuraDesignItem())
        ));
    }
}
