package com.github.andrew0030.dakimakuramod;

import com.github.andrew0030.dakimakuramod.commands.DakiCommand;
import com.github.andrew0030.dakimakuramod.config.DMTomlConfig;
import com.github.andrew0030.dakimakuramod.events.BedInteractionHandler;
import com.github.andrew0030.dakimakuramod.events.MobDropHandler;
import com.github.andrew0030.dakimakuramod.items.DakimakuraItem;
import com.github.andrew0030.dakimakuramod.network.FabricNetwork;
import com.github.andrew0030.dakimakuramod.platform.FabricPlatformHelper;
import com.github.andrew0030.dakimakuramod.platform.Services;
import com.github.andrew0030.dakimakuramod.registries.DMRegistryFabric;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public class DakimakuraModFabric implements ModInitializer
{
    @Override
    public void onInitialize()
    {
        Services.register(new FabricPlatformHelper());
        DMTomlConfig.loadCommon(FabricLoader.getInstance().getConfigDir());

        DMRegistryFabric.register();
        DakimakuraModCommon.initCommon();
        FabricNetwork.registerServerReceivers();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                DakiCommand.createCommand(dispatcher, registryAccess));

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            FabricNetwork.setActiveServer(server);
            DakimakuraModCommon.onServerStarting();
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            DakimakuraModCommon.onServerStopping();
            FabricNetwork.setActiveServer(null);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                FabricNetwork.sendDakiList(handler.getPlayer()));

        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, entity, killedEntity) ->
                MobDropHandler.onLivingDrops(killedEntity, entity, world::addFreshEntity));

        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (BedInteractionHandler.handleLeftClickBed(world, player, pos))
                return InteractionResult.FAIL;
            return InteractionResult.PASS;
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (BedInteractionHandler.handleRightClickBed(world, player, hand, hitResult.getBlockPos()))
                return InteractionResult.SUCCESS;

            ItemStack held = player.getItemInHand(hand);
            if (!player.isShiftKeyDown()
                    && held.getItem() instanceof DakimakuraItem dakiItem
                    && Services.getPlatform().isBed(world, hitResult.getBlockPos(), world.getBlockState(hitResult.getBlockPos())))
            {
                InteractionResult placeResult = dakiItem.useOn(new UseOnContext(player, hand, hitResult));
                if (placeResult.consumesAction())
                    return placeResult;
            }
            return InteractionResult.PASS;
        });
    }
}
