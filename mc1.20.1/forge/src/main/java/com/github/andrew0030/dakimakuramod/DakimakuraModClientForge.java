package com.github.andrew0030.dakimakuramod;

import com.github.andrew0030.dakimakuramod.block_entities.dakimakura.DakimakuraBlockEntityRenderer;
import com.github.andrew0030.dakimakuramod.dakimakura.serialize.DakiTagSerializer;
import com.github.andrew0030.dakimakuramod.entities.dakimakura.Dakimakura;
import com.github.andrew0030.dakimakuramod.entities.dakimakura.DakimakuraRenderer;
import com.github.andrew0030.dakimakuramod.registries.DMBlockEntities;
import com.github.andrew0030.dakimakuramod.registries.DMEntities;
import com.github.andrew0030.dakimakuramod.registries.DMItems;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class DakimakuraModClientForge
{
    public static void init(IEventBus modEventBus)
    {
        DakimakuraModClientCommon.initClient();

        modEventBus.addListener(DakimakuraModClientForge::setupClient);
        modEventBus.addListener(DakimakuraModClientForge::registerEntityRenderers);

        IEventBus eventBus = MinecraftForge.EVENT_BUS;
        eventBus.addListener(DakimakuraModClientForge::onClientTick);
        eventBus.addListener(DakimakuraModClientForge::onClientDisconnect);
    }

    private static void setupClient(FMLClientSetupEvent event)
    {
        event.enqueueWork(() -> {
            ItemProperties.register(DMItems.DAKIMAKURA_DESIGN.get(), new ResourceLocation(DakimakuraModCommon.MODID, "unlocked"),
                    (stack, level, entity, seed) -> DakiTagSerializer.deserializeFromStack(stack) == null ? 0.0F : 1.0F);
            BlockEntityRenderers.register(DMBlockEntities.DAKIMAKURA.get(), DakimakuraBlockEntityRenderer::new);
            DakimakuraModClientCommon.setupClientGlAndCache();
        });
    }

    @SuppressWarnings("unchecked")
    private static void registerEntityRenderers(RegisterRenderers event)
    {
        event.registerEntityRenderer((EntityType<Dakimakura>) DMEntities.DAKIMAKURA.get(), DakimakuraRenderer::new);
    }

    private static void onClientTick(ClientTickEvent event)
    {
        if (event.side == LogicalSide.CLIENT && event.type == TickEvent.Type.CLIENT && event.phase == TickEvent.Phase.END)
            DakimakuraModClientCommon.onClientTick();
    }

    private static void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event)
    {
        DakimakuraModClientCommon.onClientDisconnect();
    }
}
