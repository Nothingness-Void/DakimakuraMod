package com.github.andrew0030.dakimakuramod.util.obj;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public final class ObjVertexEmitter
{
    private ObjVertexEmitter() {}

    public static void emit(VertexConsumer consumer, Matrix4f pose, Matrix3f normal, float x, float y, float z, float u, float v, int packedLight, float nx, float ny, float nz)
    {
        consumer.addVertex(pose, x, y, z)
                .setColor(1F, 1F, 1F, 1F)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(nx, ny, nz);
    }
}
