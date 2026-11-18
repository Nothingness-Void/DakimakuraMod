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
        float tx = Math.fma(pose.m00(), x, Math.fma(pose.m10(), y, Math.fma(pose.m20(), z, pose.m30())));
        float ty = Math.fma(pose.m01(), x, Math.fma(pose.m11(), y, Math.fma(pose.m21(), z, pose.m31())));
        float tz = Math.fma(pose.m02(), x, Math.fma(pose.m12(), y, Math.fma(pose.m22(), z, pose.m32())));

        float tnx = Math.fma(normal.m00(), nx, Math.fma(normal.m10(), ny, normal.m20() * nz));
        float tny = Math.fma(normal.m01(), nx, Math.fma(normal.m11(), ny, normal.m21() * nz));
        float tnz = Math.fma(normal.m02(), nx, Math.fma(normal.m12(), ny, normal.m22() * nz));

        consumer.vertex(tx, ty, tz);
        consumer.color(1F, 1F, 1F, 1F);
        consumer.uv(u, v);
        consumer.overlayCoords(OverlayTexture.NO_OVERLAY);
        consumer.uv2(packedLight);
        consumer.normal(tnx, tny, tnz);
        consumer.endVertex();
    }
}
