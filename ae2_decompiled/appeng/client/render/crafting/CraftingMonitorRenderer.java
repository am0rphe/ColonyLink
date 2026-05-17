/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.blockentity.BlockEntityRenderer
 *  net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider$Context
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package appeng.client.render.crafting;

import appeng.api.orientation.BlockOrientation;
import appeng.api.stacks.GenericStack;
import appeng.blockentity.crafting.CraftingMonitorBlockEntity;
import appeng.client.render.BlockEntityRenderHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(value=Dist.CLIENT)
public class CraftingMonitorRenderer
implements BlockEntityRenderer<CraftingMonitorBlockEntity> {
    public CraftingMonitorRenderer(BlockEntityRendererProvider.Context context) {
    }

    public void render(CraftingMonitorBlockEntity be, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int combinedLight, int combinedOverlay) {
        BlockOrientation orientation = be.getOrientation();
        GenericStack jobProgress = be.getJobProgress();
        if (jobProgress != null) {
            poseStack.pushPose();
            poseStack.translate(0.5, 0.5, 0.5);
            BlockEntityRenderHelper.rotateToFace(poseStack, orientation);
            poseStack.translate(0.0, 0.02, 0.5);
            BlockEntityRenderHelper.renderItem2dWithAmount(poseStack, buffers, jobProgress.what(), jobProgress.amount(), false, 0.3f, -0.18f, be.getColor().contrastTextColor, be.getLevel());
            poseStack.popPose();
        }
    }
}

