package com.github.andrew0030.dakimakuramod.events;

import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class BedEvents
{
    @SubscribeEvent
    public void onBlockLeftClick(PlayerInteractEvent.LeftClickBlock event)
    {
        if (BedInteractionHandler.handleLeftClickBed(event.getLevel(), event.getEntity(), event.getPos()))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public void onBlockRightClick(PlayerInteractEvent.RightClickBlock event)
    {
        if (BedInteractionHandler.handleRightClickBed(event.getLevel(), event.getEntity(), event.getHand(), event.getPos()))
            event.setCanceled(true);
    }
}
