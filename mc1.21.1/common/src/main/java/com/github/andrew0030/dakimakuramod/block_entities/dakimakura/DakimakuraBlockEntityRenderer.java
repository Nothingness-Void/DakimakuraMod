package com.github.andrew0030.dakimakuramod.block_entities.dakimakura;

import com.github.andrew0030.dakimakuramod.blocks.DakimakuraBlock;
import com.github.andrew0030.dakimakuramod.util.obj.DakimakuraModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.AABB;

public class DakimakuraBlockEntityRenderer implements BlockEntityRenderer<DakimakuraBlockEntity>
{
    private final DakimakuraModel dakimakuraModel;

    public DakimakuraBlockEntityRenderer(BlockEntityRendererProvider.Context context)
    {
        this.dakimakuraModel = new DakimakuraModel();
    }

    @Override
    public void render(DakimakuraBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay)
    {
        this.render(blockEntity, partialTick, poseStack, buffer, packedLight, packedOverlay, -1);
    }

    public void render(DakimakuraBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, int lod)
    {
        Direction facing = blockEntity.getBlockState().getValue(DakimakuraBlock.FACING);
        if (blockEntity.getBlockState().getValue(DakimakuraBlock.TOP))
            return;

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        switch (facing)
        {
            default -> {}
            case SOUTH -> poseStack.mulPose(Axis.YN.rotationDegrees(180.0F));
            case WEST -> poseStack.mulPose(Axis.YN.rotationDegrees(270.0F));
            case EAST -> poseStack.mulPose(Axis.YN.rotationDegrees(90.0F));
        }

        if (blockEntity.hasLevel())
        {
            if (!blockEntity.getBlockState().getValue(DakimakuraBlock.FACE).equals(AttachFace.WALL))
                poseStack.mulPose(Axis.XN.rotationDegrees(90));
            if (blockEntity.getBlockState().getValue(DakimakuraBlock.FACE).equals(AttachFace.CEILING))
                poseStack.mulPose(Axis.YN.rotationDegrees(180));
            poseStack.translate(0.0F, 0.5F, -0.380F);
        }

        if (blockEntity.isFlipped())
            poseStack.mulPose(Axis.YN.rotationDegrees(180));

        if (lod == -1)
            this.dakimakuraModel.render(poseStack, buffer, packedLight, blockEntity.getDaki(), blockEntity.getBlockPos());
        else
            this.dakimakuraModel.render(poseStack, buffer, packedLight, blockEntity.getDaki(), lod);

        poseStack.popPose();
    }

    public AABB getRenderBoundingBox(DakimakuraBlockEntity blockEntity)
    {
        return blockEntity.getRenderBoundingBox();
    }
}
