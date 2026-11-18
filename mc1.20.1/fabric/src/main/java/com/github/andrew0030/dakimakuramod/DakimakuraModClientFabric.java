package com.github.andrew0030.dakimakuramod;

import com.github.andrew0030.dakimakuramod.block_entities.dakimakura.DakimakuraBlockEntityRenderer;
import com.github.andrew0030.dakimakuramod.block_entities.util.DMBlockEntityWithoutLevelRenderer;
import com.github.andrew0030.dakimakuramod.config.DMTomlConfig;
import com.github.andrew0030.dakimakuramod.dakimakura.serialize.DakiTagSerializer;
import com.github.andrew0030.dakimakuramod.entities.dakimakura.DakimakuraRenderer;
import com.github.andrew0030.dakimakuramod.network.FabricNetwork;
import com.github.andrew0030.dakimakuramod.platform.FabricPlatformHelper;
import com.github.andrew0030.dakimakuramod.platform.Services;
import com.github.andrew0030.dakimakuramod.registries.DMRegistryFabric;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;

public class DakimakuraModClientFabric implements ClientModInitializer
{
    private DMBlockEntityWithoutLevelRenderer itemRenderer;

    @Override
    public void onInitializeClient()
    {
        Services.register(new FabricPlatformHelper());
        DMTomlConfig.loadClient(FabricLoader.getInstance().getConfigDir());
        DakimakuraModClientCommon.initClient();
        FabricNetwork.registerClientReceivers();

        BuiltinItemRendererRegistry.INSTANCE.register(DMRegistryFabric.DAKIMAKURA_ITEM, (stack, mode, matrices, vertexConsumers, light, overlay) -> {
            if (this.itemRenderer == null)
                this.itemRenderer = new DMBlockEntityWithoutLevelRenderer();
            this.itemRenderer.renderByItem(stack, mode, matrices, vertexConsumers, light, overlay);
        });

        ItemProperties.register(DMRegistryFabric.DAKIMAKURA_DESIGN_ITEM,
                new ResourceLocation(DakimakuraModCommon.MODID, "unlocked"),
                (stack, level, entity, seed) -> DakiTagSerializer.deserializeFromStack(stack) == null ? 0.0F : 1.0F);

        EntityRendererRegistry.register(DMRegistryFabric.DAKIMAKURA_ENTITY, DakimakuraRenderer::new);
        BlockEntityRenderers.register(DMRegistryFabric.DAKIMAKURA_BLOCK_ENTITY, DakimakuraBlockEntityRenderer::new);

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> DakimakuraModClientCommon.setupClientGlAndCache());
        ClientTickEvents.END_CLIENT_TICK.register(client -> DakimakuraModClientCommon.onClientTick());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> DakimakuraModClientCommon.onClientDisconnect());
    }
}
