package com.github.andrew0030.dakimakuramod;

import com.github.andrew0030.dakimakuramod.commands.DakiCommand;
import com.github.andrew0030.dakimakuramod.config.DMConfigForge;
import com.github.andrew0030.dakimakuramod.events.BedEvents;
import com.github.andrew0030.dakimakuramod.events.LoggedInEvent;
import com.github.andrew0030.dakimakuramod.events.MobDropEvents;
import com.github.andrew0030.dakimakuramod.network.DMNetwork;
import com.github.andrew0030.dakimakuramod.platform.ForgePlatformHelper;
import com.github.andrew0030.dakimakuramod.platform.Services;
import com.github.andrew0030.dakimakuramod.registries.DMBlockEntities;
import com.github.andrew0030.dakimakuramod.registries.DMBlocks;
import com.github.andrew0030.dakimakuramod.registries.DMCreativeTab;
import com.github.andrew0030.dakimakuramod.registries.DMEntities;
import com.github.andrew0030.dakimakuramod.registries.DMItems;
import com.github.andrew0030.dakimakuramod.registries.DMRecipeSerializers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(DakimakuraModCommon.MODID)
public class DakimakuraModForge
{
    public DakimakuraModForge()
    {
        Services.register(new ForgePlatformHelper());

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus eventBus = MinecraftForge.EVENT_BUS;

        ModLoadingContext loadingContext = ModLoadingContext.get();
        loadingContext.registerConfig(ModConfig.Type.COMMON, DMConfigForge.COMMON_SPEC);
        loadingContext.registerConfig(ModConfig.Type.CLIENT, DMConfigForge.CLIENT_SPEC);

        modEventBus.addListener(this::onConfigLoad);
        modEventBus.addListener(this::onConfigReload);
        modEventBus.addListener(this::commonSetup);

        eventBus.addListener(this::registerCommands);
        eventBus.addListener(this::serverStarting);
        eventBus.addListener(this::serverStopping);
        eventBus.register(new LoggedInEvent());
        eventBus.register(new MobDropEvents());
        eventBus.register(new BedEvents());

        if (FMLEnvironment.dist == Dist.CLIENT)
            DakimakuraModClientForge.init(modEventBus);

        DMBlocks.BLOCKS.register(modEventBus);
        DMItems.ITEMS.register(modEventBus);
        DMEntities.ENTITIES.register(modEventBus);
        DMBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        DMCreativeTab.TABS.register(modEventBus);
        DMRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
    }

    private void onConfigLoad(ModConfigEvent.Loading event)
    {
        this.syncConfig(event.getConfig());
    }

    private void onConfigReload(ModConfigEvent.Reloading event)
    {
        this.syncConfig(event.getConfig());
    }

    private void syncConfig(ModConfig config)
    {
        if (config.getSpec() == DMConfigForge.COMMON_SPEC)
            DMConfigForge.syncCommon();
        else if (config.getSpec() == DMConfigForge.CLIENT_SPEC)
            DMConfigForge.syncClient();
    }

    private void commonSetup(FMLCommonSetupEvent event)
    {
        DakimakuraModCommon.initCommon();
        DMNetwork.registerMessages();
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
}
