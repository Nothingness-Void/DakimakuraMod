package com.github.andrew0030.dakimakuramod;

import com.github.andrew0030.dakimakuramod.block_entities.dakimakura.DakimakuraBlockEntityRenderer;
import com.github.andrew0030.dakimakuramod.block_entities.util.DMBlockEntityWithoutLevelRenderer;
import com.github.andrew0030.dakimakuramod.config.DMTomlConfig;
import com.github.andrew0030.dakimakuramod.dakimakura.serialize.DakiTagSerializer;
import com.github.andrew0030.dakimakuramod.entities.dakimakura.DakimakuraRenderer;
import com.github.andrew0030.dakimakuramod.network.DMNetworkFabric;
import com.github.andrew0030.dakimakuramod.platform.Services;
import com.github.andrew0030.dakimakuramod.registries.DMRegistriesFabric;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;

public class DakimakuraModFabricClient implements ClientModInitializer
{
    private static DMBlockEntityWithoutLevelRenderer itemRenderer;

    @Override
    public void onInitializeClient()
    {
        DMTomlConfig.loadClient(FabricLoader.getInstance().getConfigDir());
        DakimakuraModClientCommon.initClient();
        DMNetworkFabric.registerClientReceivers();

        ItemProperties.register(
                DMRegistriesFabric.DAKIMAKURA_DESIGN,
                Services.getPlatform().id(DakimakuraModCommon.MODID, "unlocked"),
                (stack, level, entity, seed) -> DakiTagSerializer.deserialize(stack) == null ? 0.0F : 1.0F
        );

        EntityRendererRegistry.register(DMRegistriesFabric.DAKIMAKURA_ENTITY, DakimakuraRenderer::new);
        BlockEntityRenderers.register(DMRegistriesFabric.DAKIMAKURA_BLOCK_ENTITY, DakimakuraBlockEntityRenderer::new);

        BuiltinItemRendererRegistry.INSTANCE.register(DMRegistriesFabric.DAKIMAKURA_ITEM, (stack, mode, matrices, vertexConsumers, light, overlay) -> {
            if (itemRenderer == null)
                itemRenderer = new DMBlockEntityWithoutLevelRenderer();
            itemRenderer.renderByItem(stack, mode, matrices, vertexConsumers, light, overlay);
        });

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> DakimakuraModClientCommon.setupClientGlAndCache());
        ClientTickEvents.END_CLIENT_TICK.register(client -> DakimakuraModClientCommon.onClientTick());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> DakimakuraModClientCommon.onClientDisconnect());
    }
}
