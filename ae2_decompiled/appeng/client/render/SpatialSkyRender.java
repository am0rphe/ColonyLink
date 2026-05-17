/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.BufferBuilder
 *  com.mojang.blaze3d.vertex.BufferUploader
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.MeshData
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.Tesselator
 *  com.mojang.blaze3d.vertex.VertexBuffer
 *  com.mojang.blaze3d.vertex.VertexBuffer$Usage
 *  com.mojang.blaze3d.vertex.VertexFormat$Mode
 *  net.minecraft.client.renderer.GameRenderer
 *  net.minecraft.util.Mth
 *  net.minecraft.util.RandomSource
 *  org.joml.Matrix4f
 *  org.joml.Quaternionf
 */
package appeng.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class SpatialSkyRender {
    private static final SpatialSkyRender INSTANCE = new SpatialSkyRender();
    private final RandomSource random = RandomSource.create();
    private final VertexBuffer sparkleBuffer = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
    private long cycle = 0L;
    private static final Quaternionf[] SKYBOX_SIDE_ROTATIONS = new Quaternionf[]{new Quaternionf(), new Quaternionf().rotationX(1.5707964f), new Quaternionf().rotationX(-1.5707964f), new Quaternionf().rotationX((float)Math.PI), new Quaternionf().rotationZ(1.5707964f), new Quaternionf().rotationZ(-1.5707964f)};

    public static SpatialSkyRender getInstance() {
        return INSTANCE;
    }

    public void render(Matrix4f modelViewMatrix, Matrix4f projectionMatrix) {
        long now = System.currentTimeMillis();
        if (now - this.cycle > 2000L) {
            this.cycle = now;
            this.rebuildSparkles();
        }
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableBlend();
        RenderSystem.depthMask((boolean)false);
        PoseStack poseStack = new PoseStack();
        poseStack.mulPose(modelViewMatrix);
        for (Quaternionf rotation : SKYBOX_SIDE_ROTATIONS) {
            poseStack.pushPose();
            poseStack.mulPose(rotation);
            Matrix4f matrix4f = poseStack.last().pose();
            BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            builder.addVertex(matrix4f, -100.0f, -100.0f, -100.0f).setColor(0.0f, 0.0f, 0.0f, 1.0f);
            builder.addVertex(matrix4f, -100.0f, -100.0f, 100.0f).setColor(0.0f, 0.0f, 0.0f, 1.0f);
            builder.addVertex(matrix4f, 100.0f, -100.0f, 100.0f).setColor(0.0f, 0.0f, 0.0f, 1.0f);
            builder.addVertex(matrix4f, 100.0f, -100.0f, -100.0f).setColor(0.0f, 0.0f, 0.0f, 1.0f);
            BufferUploader.drawWithShader((MeshData)builder.buildOrThrow());
            poseStack.popPose();
        }
        float fade = now - this.cycle;
        fade /= 1000.0f;
        if ((fade = 0.25f * (1.0f - Math.abs((fade - 1.0f) * (fade - 1.0f)))) > 0.0f) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor((float)fade, (float)fade, (float)fade, (float)1.0f);
            this.sparkleBuffer.bind();
            this.sparkleBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, GameRenderer.getPositionColorShader());
            VertexBuffer.unbind();
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        }
        RenderSystem.depthMask((boolean)true);
        RenderSystem.enableBlend();
    }

    private void rebuildSparkles() {
        BufferBuilder vb = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i < 50; ++i) {
            float iX = this.random.nextFloat() * 2.0f - 1.0f;
            float iY = this.random.nextFloat() * 2.0f - 1.0f;
            float iZ = this.random.nextFloat() * 2.0f - 1.0f;
            float d3 = 0.05f + this.random.nextFloat() * 0.1f;
            float dist = iX * iX + iY * iY + iZ * iZ;
            if (!(dist < 1.0f) || !(dist > 0.01f)) continue;
            dist = 1.0f / Mth.sqrt((float)dist);
            float x = (iX *= dist) * 100.0f;
            float y = (iY *= dist) * 100.0f;
            float z = (iZ *= dist) * 100.0f;
            float d8 = (float)Mth.atan2((double)iX, (double)iZ);
            float d9 = Mth.sin((float)d8);
            float d10 = Mth.cos((float)d8);
            float d11 = (float)Mth.atan2((double)Mth.sqrt((float)(iX * iX + iZ * iZ)), (double)iY);
            float d12 = Mth.sin((float)d11);
            float d13 = Mth.cos((float)d11);
            float d14 = this.random.nextFloat() * (float)Math.PI * 2.0f;
            float d15 = Mth.sin((float)d14);
            float d16 = Mth.cos((float)d14);
            for (int j = 0; j < 4; ++j) {
                float d17 = 0.0f;
                float d18 = (float)((j & 2) - 1) * d3;
                float d19 = (float)((j + 1 & 2) - 1) * d3;
                float d20 = d18 * d16 - d19 * d15;
                float d21 = d19 * d16 + d18 * d15;
                float d22 = d20 * d12 + d17 * d13;
                float d23 = d17 * d12 - d20 * d13;
                float d24 = d23 * d9 - d21 * d10;
                float d25 = d21 * d9 + d23 * d10;
                vb.addVertex(x + d24, y + d22, z + d25).setColor(255, 255, 255, 255);
            }
        }
        this.sparkleBuffer.bind();
        this.sparkleBuffer.upload(vb.buildOrThrow());
        VertexBuffer.unbind();
    }
}

