package com.github.andrew0030.dakimakuramod;

import com.github.andrew0030.dakimakuramod.commands.DakiCommand;
import com.github.andrew0030.dakimakuramod.config.DMTomlConfig;
import com.github.andrew0030.dakimakuramod.events.BedInteractionHandler;
import com.github.andrew0030.dakimakuramod.events.MobDropHandler;
import com.github.andrew0030.dakimakuramod.items.DakimakuraItem;
import com.github.andrew0030.dakimakuramod.network.DMNetworkFabric;
import com.github.andrew0030.dakimakuramod.platform.FabricPlatformHelper;
import com.github.andrew0030.dakimakuramod.platform.Services;
import com.github.andrew0030.dakimakuramod.registries.DMRegistriesFabric;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.UseOnContext;
import org.slf4j.Logger;

public class DakimakuraModFabric implements ModInitializer
{
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize()
    {
        Services.register(new FabricPlatformHelper());

        DMTomlConfig.loadCommon(FabricLoader.getInstance().getConfigDir());
        DMRegistriesFabric.register();
        DakimakuraModCommon.initCommon();
        DMNetworkFabric.registerPayloads();
        DMNetworkFabric.registerServerReceivers();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                DakiCommand.createCommand(dispatcher, registryAccess));

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            FabricPlatformHelper.currentServer = server;
            DMTomlConfig.loadCommon(FabricLoader.getInstance().getConfigDir());
            DakimakuraModCommon.onServerStarting();
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            DakimakuraModCommon.onServerStopping();
            FabricPlatformHelper.currentServer = null;
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            LOGGER.info("Sending daki list to {}", handler.player.getName().getString());
            Services.getPlatform().sendDakiListToPlayer(handler.player);
        });

        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, entity, killedEntity) -> {
            if (killedEntity instanceof LivingEntity living)
                MobDropHandler.onLivingDrops(living, entity, world::addFreshEntity);
        });

        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) ->
                BedInteractionHandler.handleLeftClickBed(player, world, pos) ? InteractionResult.FAIL : InteractionResult.PASS);

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (BedInteractionHandler.handleRightClickBed(player, world, hitResult.getBlockPos(), hand))
                return InteractionResult.SUCCESS;
            if (!player.isShiftKeyDown()
                    && player.getItemInHand(hand).is(DMRegistriesFabric.DAKIMAKURA_ITEM)
                    && Services.getPlatform().isBed(world, hitResult.getBlockPos(), world.getBlockState(hitResult.getBlockPos())))
            {
                InteractionResult placeResult = DakimakuraItem.tryPlaceOnBed(new UseOnContext(player, hand, hitResult));
                if (placeResult != null && placeResult.consumesAction())
                    return placeResult;
            }
            return InteractionResult.PASS;
        });
    }
}
