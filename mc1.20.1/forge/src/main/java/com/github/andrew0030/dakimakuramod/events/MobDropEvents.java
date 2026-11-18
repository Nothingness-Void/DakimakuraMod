package com.github.andrew0030.dakimakuramod.events;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MobDropEvents
{
    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent event)
    {
        MobDropHandler.onLivingDrops(event.getEntity(), event.getSource().getEntity(), drop -> event.getDrops().add(drop));
    }
}
