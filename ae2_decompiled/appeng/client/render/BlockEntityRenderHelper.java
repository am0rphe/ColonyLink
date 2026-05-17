/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.Font$DisplayMode
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.world.level.Level
 *  org.joml.Quaternionf
 */
package appeng.client.render;

import appeng.api.client.AEKeyRendering;
import appeng.api.orientation.BlockOrientation;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AmountFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.Level;
import org.joml.Quaternionf;

public final class BlockEntityRenderHelper {
    private static final Quaternionf ROTATE_TO_FRONT = new Quaternionf().rotationY((float)Math.PI);

    private BlockEntityRenderHelper() {
    }

    public static void rotateToFace(PoseStack stack, BlockOrientation orientation) {
        stack.mulPose(orientation.getQuaternion());
        stack.mulPose(ROTATE_TO_FRONT);
    }

    public static void renderItem2d(PoseStack poseStack, MultiBufferSource buffers, AEKey what, float scale, int combinedLightIn, Level level) {
        AEKeyRendering.drawOnBlockFace(poseStack, buffers, what, scale, combinedLightIn, level);
    }

    public static void renderItem2dWithAmount(PoseStack poseStack, MultiBufferSource buffers, AEKey what, long amount, boolean canCraft, float itemScale, float spacing, int textColor, Level level) {
        BlockEntityRenderHelper.renderItem2d(poseStack, buffers, what, itemScale, 0xF000F0, level);
        String renderedStackSize = amount == 0L && canCraft ? "Craft" : what.formatAmount(amount, AmountFormat.SLOT);
        Font fr = Minecraft.getInstance().font;
        int width = fr.width(renderedStackSize);
        poseStack.pushPose();
        poseStack.translate(0.0f, spacing, 0.02f);
        poseStack.scale(0.016129032f, -0.016129032f, 0.016129032f);
        poseStack.scale(0.5f, 0.5f, 0.0f);
        poseStack.translate(-0.5f * (float)width, 0.0f, 0.5f);
        fr.drawInBatch(renderedStackSize, 0.0f, 0.0f, textColor, false, poseStack.last().pose(), buffers, Font.DisplayMode.NORMAL, 0, 0xF000F0);
        poseStack.popPose();
    }
}

