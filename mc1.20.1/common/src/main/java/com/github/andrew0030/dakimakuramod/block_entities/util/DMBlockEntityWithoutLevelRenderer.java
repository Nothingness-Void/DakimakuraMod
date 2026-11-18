package com.github.andrew0030.dakimakuramod.block_entities.util;

import com.github.andrew0030.dakimakuramod.block_entities.dakimakura.DakimakuraBlockEntity;
import com.github.andrew0030.dakimakuramod.block_entities.dakimakura.DakimakuraBlockEntityRenderer;
import com.github.andrew0030.dakimakuramod.dakimakura.serialize.DakiTagSerializer;
import com.github.andrew0030.dakimakuramod.platform.Services;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class DMBlockEntityWithoutLevelRenderer extends BlockEntityWithoutLevelRenderer
{
    private DakimakuraBlockEntity blockEntity;

    public DMBlockEntityWithoutLevelRenderer()
    {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay)
    {
        if (this.blockEntity == null)
            this.blockEntity = new DakimakuraBlockEntity(BlockPos.ZERO, Services.getPlatform().getDakimakuraBlock().defaultBlockState());

        this.blockEntity.setDaki(DakiTagSerializer.deserializeFromStack(stack));
        this.blockEntity.setFlipped(DakiTagSerializer.isStackFlipped(stack));
        int lod = displayContext.equals(ItemDisplayContext.GUI) ? 3 : 0;
        if (Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(this.blockEntity) instanceof DakimakuraBlockEntityRenderer renderer)
            renderer.render(this.blockEntity, 0.0F, poseStack, buffer, packedLight, packedOverlay, lod);
    }
}
