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
 *  com.mojang.blaze3d.vertex.VertexFormat$Mode
 *  guideme.document.LytRect
 *  net.minecraft.client.renderer.GameRenderer
 *  net.minecraft.util.Mth
 *  org.joml.Matrix4f
 */
package appeng.client.gui;

import appeng.client.gui.DashPattern;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import guideme.document.LytRect;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public final class DashedRectangle {
    private DashedRectangle() {
    }

    public static void render(PoseStack stack, LytRect bounds, DashPattern pattern, float z) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        float t = 0.0f;
        if (pattern.animationCycleMs() > 0.0f) {
            t = (float)(System.currentTimeMillis() % (long)((int)pattern.animationCycleMs())) / pattern.animationCycleMs();
        }
        DashedRectangle.buildHorizontalDashedLine(builder, stack, t, bounds.x(), bounds.right(), bounds.y(), z, pattern, false);
        DashedRectangle.buildHorizontalDashedLine(builder, stack, t, bounds.x(), bounds.right(), (float)bounds.bottom() - pattern.width(), z, pattern, true);
        DashedRectangle.buildVerticalDashedLine(builder, stack, t, bounds.x(), bounds.y(), bounds.bottom(), z, pattern, true);
        DashedRectangle.buildVerticalDashedLine(builder, stack, t, (float)bounds.right() - pattern.width(), bounds.y(), bounds.bottom(), z, pattern, false);
        BufferUploader.drawWithShader((MeshData)builder.buildOrThrow());
        RenderSystem.disableBlend();
    }

    private static void buildHorizontalDashedLine(BufferBuilder builder, PoseStack stack, float t, float x1, float x2, float y, float z, DashPattern pattern, boolean reverse) {
        if (!reverse) {
            t = 1.0f - t;
        }
        float phase = t * pattern.length();
        Matrix4f pose = stack.last().pose();
        int color = pattern.color();
        for (float x = x1 - phase; x < x2; x += pattern.length()) {
            builder.addVertex(pose, Mth.clamp((float)(x + pattern.onLength()), (float)x1, (float)x2), y, z).setColor(color);
            builder.addVertex(pose, Mth.clamp((float)x, (float)x1, (float)x2), y, z).setColor(color);
            builder.addVertex(pose, Mth.clamp((float)x, (float)x1, (float)x2), y + pattern.width(), z).setColor(color);
            builder.addVertex(pose, Mth.clamp((float)(x + pattern.onLength()), (float)x1, (float)x2), y + pattern.width(), z).setColor(color);
        }
    }

    private static void buildVerticalDashedLine(BufferBuilder builder, PoseStack stack, float t, float x, float y1, float y2, float z, DashPattern pattern, boolean reverse) {
        if (!reverse) {
            t = 1.0f - t;
        }
        float phase = t * pattern.length();
        Matrix4f pose = stack.last().pose();
        int color = pattern.color();
        for (float y = y1 - phase; y < y2; y += pattern.length()) {
            builder.addVertex(pose, x + pattern.width(), Mth.clamp((float)y, (float)y1, (float)y2), z).setColor(color);
            builder.addVertex(pose, x, Mth.clamp((float)y, (float)y1, (float)y2), z).setColor(color);
            builder.addVertex(pose, x, Mth.clamp((float)(y + pattern.onLength()), (float)y1, (float)y2), z).setColor(color);
            builder.addVertex(pose, x + pattern.width(), Mth.clamp((float)(y + pattern.onLength()), (float)y1, (float)y2), z).setColor(color);
        }
    }
}

