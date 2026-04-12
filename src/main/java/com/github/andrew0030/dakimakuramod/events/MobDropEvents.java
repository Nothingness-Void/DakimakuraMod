package com.github.andrew0030.dakimakuramod.events;

import com.github.andrew0030.dakimakuramod.registries.DMItems;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MobDropEvents
{
    private static final float DAKIMAKURA_DESIGN_DROP_CHANCE = 0.05F;

    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent event)
    {
        if (!(event.getSource().getEntity() instanceof Player))
            return;
        if (!(event.getEntity() instanceof Enemy))
            return;
        if (event.getEntity().getRandom().nextFloat() >= DAKIMAKURA_DESIGN_DROP_CHANCE)
            return;

        event.getDrops().add(new ItemEntity(
                event.getEntity().level(),
                event.getEntity().getX(),
                event.getEntity().getY(),
                event.getEntity().getZ(),
                new ItemStack(DMItems.DAKIMAKURA_DESIGN.get())
        ));
    }
}
