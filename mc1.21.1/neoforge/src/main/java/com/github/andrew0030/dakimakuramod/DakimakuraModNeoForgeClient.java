package com.github.andrew0030.dakimakuramod;

import com.github.andrew0030.dakimakuramod.block_entities.dakimakura.DakimakuraBlockEntityRenderer;
import com.github.andrew0030.dakimakuramod.block_entities.util.DMBlockEntityWithoutLevelRenderer;
import com.github.andrew0030.dakimakuramod.dakimakura.serialize.DakiTagSerializer;
import com.github.andrew0030.dakimakuramod.entities.dakimakura.DakimakuraRenderer;
import com.github.andrew0030.dakimakuramod.platform.Services;
import com.github.andrew0030.dakimakuramod.registries.DMRegistriesNeoForge;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class DakimakuraModNeoForgeClient
{
    private DakimakuraModNeoForgeClient() {}

    public static void init(IEventBus modEventBus)
    {
        DakimakuraModClientCommon.initClient();
        modEventBus.addListener(DakimakuraModNeoForgeClient::setupClient);
        modEventBus.addListener(DakimakuraModNeoForgeClient::registerRenderers);
        modEventBus.addListener(DakimakuraModNeoForgeClient::registerClientExtensions);

        IEventBus gameBus = NeoForge.EVENT_BUS;
        gameBus.addListener(DakimakuraModNeoForgeClient::onClientTick);
        gameBus.addListener(DakimakuraModNeoForgeClient::onLoggingOut);
    }

    private static void setupClient(FMLClientSetupEvent event)
    {
        event.enqueueWork(() -> {
            ItemProperties.register(
                    DMRegistriesNeoForge.DAKIMAKURA_DESIGN.get(),
                    Services.getPlatform().id(DakimakuraModCommon.MODID, "unlocked"),
                    (stack, level, entity, seed) -> DakiTagSerializer.deserialize(stack) == null ? 0.0F : 1.0F
            );
            DakimakuraModClientCommon.setupClientGlAndCache();
        });
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerEntityRenderer(DMRegistriesNeoForge.DAKIMAKURA_ENTITY.get(), DakimakuraRenderer::new);
        event.registerBlockEntityRenderer(DMRegistriesNeoForge.DAKIMAKURA_BLOCK_ENTITY.get(), DakimakuraBlockEntityRenderer::new);
    }

    private static void registerClientExtensions(RegisterClientExtensionsEvent event)
    {
        event.registerItem(new IClientItemExtensions()
        {
            private DMBlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer()
            {
                if (this.renderer == null)
                    this.renderer = new DMBlockEntityWithoutLevelRenderer();
                return this.renderer;
            }
        }, DMRegistriesNeoForge.DAKIMAKURA_ITEM.get());
    }

    private static void onClientTick(ClientTickEvent.Post event)
    {
        DakimakuraModClientCommon.onClientTick();
    }

    private static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event)
    {
        DakimakuraModClientCommon.onClientDisconnect();
    }
}
