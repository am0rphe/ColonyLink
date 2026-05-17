/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.Font$DisplayMode
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  org.joml.Matrix4f
 */
package appeng.client.gui.me.common;

import appeng.core.AEConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;

public class StackSizeRenderer {
    private static void renderSizeLabel(Matrix4f matrix, Font fontRenderer, float xPos, float yPos, String text, boolean largeFonts) {
        float scaleFactor = largeFonts ? 0.85f : 0.666f;
        float inverseScaleFactor = 1.0f / scaleFactor;
        int offset = largeFonts ? 0 : -1;
        RenderSystem.disableBlend();
        int X = (int)((xPos + (float)offset + 16.0f + 2.0f - (float)fontRenderer.width(text) * scaleFactor) * inverseScaleFactor);
        int Y = (int)((yPos + (float)offset + 16.0f - 5.0f * scaleFactor) * inverseScaleFactor);
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        fontRenderer.drawInBatch(text, (float)(X + 1), (float)(Y + 1), 4276052, false, matrix, (MultiBufferSource)buffer, Font.DisplayMode.NORMAL, 0, 0xF000F0);
        fontRenderer.drawInBatch(text, (float)X, (float)Y, 0xFFFFFF, false, matrix, (MultiBufferSource)buffer, Font.DisplayMode.NORMAL, 0, 0xF000F0);
        buffer.endBatch();
        RenderSystem.enableBlend();
    }

    public static void renderSizeLabel(GuiGraphics guiGraphics, Font fontRenderer, float xPos, float yPos, String text) {
        StackSizeRenderer.renderSizeLabel(guiGraphics, fontRenderer, xPos, yPos, text, AEConfig.instance().isUseLargeFonts());
    }

    public static void renderSizeLabel(GuiGraphics guiGraphics, Font fontRenderer, float xPos, float yPos, String text, boolean largeFonts) {
        float scaleFactor = largeFonts ? 0.85f : 0.666f;
        PoseStack stack = guiGraphics.pose();
        stack.pushPose();
        stack.translate(0.0f, 0.0f, 200.0f);
        stack.scale(scaleFactor, scaleFactor, scaleFactor);
        StackSizeRenderer.renderSizeLabel(stack.last().pose(), fontRenderer, xPos, yPos, text, largeFonts);
        stack.popPose();
    }
}

