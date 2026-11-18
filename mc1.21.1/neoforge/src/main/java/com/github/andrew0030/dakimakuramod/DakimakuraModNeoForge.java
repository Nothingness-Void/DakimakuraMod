package com.github.andrew0030.dakimakuramod;

import com.github.andrew0030.dakimakuramod.commands.DakiCommand;
import com.github.andrew0030.dakimakuramod.config.DMConfigNeoForge;
import com.github.andrew0030.dakimakuramod.events.BedInteractionHandler;
import com.github.andrew0030.dakimakuramod.events.MobDropHandler;
import com.github.andrew0030.dakimakuramod.items.DakimakuraItem;
import com.github.andrew0030.dakimakuramod.network.DMNetworkNeoForge;
import com.github.andrew0030.dakimakuramod.platform.NeoForgePlatformHelper;
import com.github.andrew0030.dakimakuramod.platform.Services;
import com.github.andrew0030.dakimakuramod.registries.DMRegistriesNeoForge;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.slf4j.Logger;

@Mod(DakimakuraModCommon.MODID)
public class DakimakuraModNeoForge
{
    private static final Logger LOGGER = LogUtils.getLogger();

    public DakimakuraModNeoForge(IEventBus modEventBus, ModContainer modContainer)
    {
        Services.register(new NeoForgePlatformHelper());

        modContainer.registerConfig(ModConfig.Type.COMMON, DMConfigNeoForge.COMMON_SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, DMConfigNeoForge.CLIENT_SPEC);
        modEventBus.addListener(DMConfigNeoForge::onConfigLoad);
        modEventBus.addListener(DMConfigNeoForge::onConfigReload);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(DMNetworkNeoForge::registerPayloads);

        DMRegistriesNeoForge.register(modEventBus);

        IEventBus gameBus = NeoForge.EVENT_BUS;
        gameBus.addListener(this::registerCommands);
        gameBus.addListener(this::serverStarting);
        gameBus.addListener(this::serverStopping);
        gameBus.addListener(this::onPlayerLoggedIn);
        gameBus.addListener(this::onLivingDrops);
        gameBus.addListener(this::onLeftClickBlock);
        gameBus.addListener(this::onRightClickBlock);

        if (FMLEnvironment.dist == Dist.CLIENT)
            DakimakuraModNeoForgeClient.init(modEventBus);
    }

    private void commonSetup(FMLCommonSetupEvent event)
    {
        event.enqueueWork(DakimakuraModCommon::initCommon);
    }

    private void registerCommands(RegisterCommandsEvent event)
    {
        DakiCommand.createCommand(event.getDispatcher(), event.getBuildContext());
    }

    private void serverStarting(ServerStartingEvent event)
    {
        DakimakuraModCommon.onServerStarting();
    }

    private void serverStopping(ServerStoppingEvent event)
    {
        DakimakuraModCommon.onServerStopping();
    }

    private void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer serverPlayer)
        {
            LOGGER.info("Sending daki list to {}", serverPlayer.getName().getString());
            Services.getPlatform().sendDakiListToPlayer(serverPlayer);
        }
    }

    private void onLivingDrops(LivingDropsEvent event)
    {
        MobDropHandler.onLivingDrops(event.getEntity(), event.getSource().getEntity(), drop -> event.getDrops().add(drop));
    }

    private void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event)
    {
        if (BedInteractionHandler.handleLeftClickBed(event.getEntity(), event.getLevel(), event.getPos()))
            event.setCanceled(true);
    }

    private void onRightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        if (BedInteractionHandler.handleRightClickBed(event.getEntity(), event.getLevel(), event.getPos(), event.getHand()))
        {
            event.setCanceled(true);
            return;
        }
        if (!event.getEntity().isShiftKeyDown()
                && event.getItemStack().is(DMRegistriesNeoForge.DAKIMAKURA_ITEM.get())
                && Services.getPlatform().isBed(event.getLevel(), event.getPos(), event.getLevel().getBlockState(event.getPos())))
        {
            InteractionResult placeResult = DakimakuraItem.tryPlaceOnBed(new UseOnContext(event.getEntity(), event.getHand(), event.getHitVec()));
            if (placeResult != null && placeResult.consumesAction())
            {
                event.setCanceled(true);
                event.setCancellationResult(placeResult);
            }
        }
    }
}
