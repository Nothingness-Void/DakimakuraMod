package com.github.andrew0030.dakimakuramod;

import com.github.andrew0030.dakimakuramod.block_entities.dakimakura.DakimakuraBlockEntityRenderer;
import com.github.andrew0030.dakimakuramod.dakimakura.serialize.DakiTagSerializer;
import com.github.andrew0030.dakimakuramod.entities.dakimakura.DakimakuraRenderer;
import com.github.andrew0030.dakimakuramod.platform.Services;
import com.github.andrew0030.dakimakuramod.registries.DMRegistriesForge;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public final class DakimakuraModForgeClient
{
    private DakimakuraModForgeClient() {}

    public static void init(IEventBus modEventBus)
    {
        DakimakuraModClientCommon.initClient();
        modEventBus.addListener(DakimakuraModForgeClient::setupClient);
        modEventBus.addListener(DakimakuraModForgeClient::registerEntityRenderers);

        IEventBus eventBus = MinecraftForge.EVENT_BUS;
        eventBus.addListener(DakimakuraModForgeClient::onClientTick);
        eventBus.addListener(DakimakuraModForgeClient::onLoggingOut);
    }

    private static void setupClient(FMLClientSetupEvent event)
    {
        event.enqueueWork(() -> {
            ItemProperties.register(
                    DMRegistriesForge.DAKIMAKURA_DESIGN.get(),
                    Services.getPlatform().id(DakimakuraModCommon.MODID, "unlocked"),
                    (stack, level, entity, seed) -> DakiTagSerializer.deserialize(stack) == null ? 0.0F : 1.0F
            );
            BlockEntityRenderers.register(DMRegistriesForge.DAKIMAKURA_BLOCK_ENTITY.get(), DakimakuraBlockEntityRenderer::new);
            DakimakuraModClientCommon.setupClientGlAndCache();
        });
    }

    private static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerEntityRenderer(DMRegistriesForge.DAKIMAKURA_ENTITY.get(), DakimakuraRenderer::new);
    }

    private static void onClientTick(TickEvent.ClientTickEvent.Post event)
    {
        if (event.side == LogicalSide.CLIENT)
            DakimakuraModClientCommon.onClientTick();
    }

    private static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event)
    {
        DakimakuraModClientCommon.onClientDisconnect();
    }
}
