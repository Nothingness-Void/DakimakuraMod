package com.github.andrew0030.dakimakuramod;

import com.github.andrew0030.dakimakuramod.commands.DakiCommand;
import com.github.andrew0030.dakimakuramod.dakimakura.DakiExtractor;
import com.github.andrew0030.dakimakuramod.dakimakura.DakiManager;
import com.github.andrew0030.dakimakuramod.dakimakura.DakiTextureManagerCommon;
import com.github.andrew0030.dakimakuramod.events.LoggedInEvent;
import com.github.andrew0030.dakimakuramod.events.MobDropEvents;
import com.github.andrew0030.dakimakuramod.netwok.DMNetwork;
import com.github.andrew0030.dakimakuramod.registries.*;
import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

@Mod(DakimakuraMod.MODID)
public class DakimakuraMod
{
    public static final String MODID = "dakimakuramod";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static DakiManager dakiManager;
    private static DakiTextureManagerCommon dakiTextureManagerCommon;

    public DakimakuraMod(IEventBus modEventBus)
    {
        IEventBus eventBus = NeoForge.EVENT_BUS;

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(DMNetwork::registerMessages);
        eventBus.addListener(this::registerCommands);
        eventBus.addListener(this::serverStarting);
        eventBus.addListener(this::serverStopping);
        eventBus.register(new LoggedInEvent());
        eventBus.register(new MobDropEvents());
        if (FMLEnvironment.dist == Dist.CLIENT)
            DakimakuraModClient.init(modEventBus);

        DMBlocks.BLOCKS.register(modEventBus);
        DMItems.ITEMS.register(modEventBus);
        DMEntities.ENTITIES.register(modEventBus);
        DMBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        DMCreativeTab.TABS.register(modEventBus);
        DMRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
    }

    private void commonSetup(FMLCommonSetupEvent event)
    {
        // Initializes the DakiManager, this should only happen once per Mod
        DakimakuraMod.dakiManager = new DakiManager(FMLPaths.GAMEDIR.get().toFile());
        // Initializes the DakiTextureManagerCommon
        DakimakuraMod.dakiTextureManagerCommon = new DakiTextureManagerCommon();
        // Loads the default DakiPack/s into the dakimakura-mod folder if needed
        DakiExtractor.extractDakis();
    }

    private void registerCommands(RegisterCommandsEvent event)
    {
        DakiCommand.createCommand(event.getDispatcher(), event.getBuildContext());
    }

    private void serverStarting(ServerStartingEvent event)
    {
        DakimakuraMod.getDakimakuraManager().loadPacks(false);
        DakimakuraMod.getTextureManagerCommon().serverStarted();
    }

    private void serverStopping(ServerStoppingEvent event)
    {
        DakimakuraMod.getTextureManagerCommon().serverStopped();
    }

    public static DakiManager getDakimakuraManager()
    {
        return DakimakuraMod.dakiManager;
    }

    public static DakiTextureManagerCommon getTextureManagerCommon()
    {
        return DakimakuraMod.dakiTextureManagerCommon;
    }
}
