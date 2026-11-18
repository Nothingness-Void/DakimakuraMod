package com.github.andrew0030.dakimakuramod;

import com.github.andrew0030.dakimakuramod.commands.DakiCommand;
import com.github.andrew0030.dakimakuramod.config.DMConfigForge;
import com.github.andrew0030.dakimakuramod.events.BedInteractionHandler;
import com.github.andrew0030.dakimakuramod.events.MobDropHandler;
import com.github.andrew0030.dakimakuramod.network.DMNetworkForge;
import com.github.andrew0030.dakimakuramod.platform.ForgePlatformHelper;
import com.github.andrew0030.dakimakuramod.platform.Services;
import com.github.andrew0030.dakimakuramod.registries.DMRegistriesForge;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(DakimakuraModCommon.MODID)
public class DakimakuraModForge
{
    private static final Logger LOGGER = LogUtils.getLogger();

    public DakimakuraModForge()
    {
        Services.register(new ForgePlatformHelper());

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus eventBus = MinecraftForge.EVENT_BUS;

        ModLoadingContext loadingContext = ModLoadingContext.get();
        loadingContext.registerConfig(ModConfig.Type.COMMON, DMConfigForge.COMMON_SPEC);
        loadingContext.registerConfig(ModConfig.Type.CLIENT, DMConfigForge.CLIENT_SPEC);
        modEventBus.addListener(DMConfigForge::onConfigLoad);
        modEventBus.addListener(DMConfigForge::onConfigReload);

        modEventBus.addListener(this::commonSetup);
        DMRegistriesForge.register(modEventBus);

        eventBus.addListener(this::registerCommands);
        eventBus.addListener(this::serverStarting);
        eventBus.addListener(this::serverStopping);
        eventBus.addListener(this::onPlayerLoggedIn);
        eventBus.addListener(this::onLivingDrops);
        eventBus.addListener(this::onLeftClickBlock);
        eventBus.addListener(this::onRightClickBlock);

        if (FMLEnvironment.dist == Dist.CLIENT)
            DakimakuraModForgeClient.init(modEventBus);
    }

    private void commonSetup(FMLCommonSetupEvent event)
    {
        event.enqueueWork(() -> {
            DakimakuraModCommon.initCommon();
            DMNetworkForge.registerMessages();
        });
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
        MobDropHandler.onLivingDrops(event.getEntity(), event.getSource().getEntity(), event.getDrops()::add);
    }

    private void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event)
    {
        if (BedInteractionHandler.handleLeftClickBed(event.getEntity(), event.getLevel(), event.getPos()))
            event.setCanceled(true);
    }

    private void onRightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        if (BedInteractionHandler.handleRightClickBed(event.getEntity(), event.getLevel(), event.getPos(), event.getHand()))
            event.setCanceled(true);
    }
}
