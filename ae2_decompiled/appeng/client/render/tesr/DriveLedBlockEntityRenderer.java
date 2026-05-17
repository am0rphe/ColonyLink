/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.blockentity.BlockEntityRenderer
 *  net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider$Context
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 *  org.joml.Vector3f
 */
package appeng.client.render.tesr;

import appeng.api.orientation.BlockOrientation;
import appeng.blockentity.storage.DriveBlockEntity;
import appeng.client.render.model.DriveBakedModel;
import appeng.client.render.tesr.CellLedRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Vector3f;

@OnlyIn(value=Dist.CLIENT)
public class DriveLedBlockEntityRenderer
implements BlockEntityRenderer<DriveBlockEntity> {
    public DriveLedBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    public void render(DriveBlockEntity drive, float partialTicks, PoseStack ms, MultiBufferSource buffers, int combinedLightIn, int combinedOverlayIn) {
        if (drive.getCellCount() != 10) {
            throw new IllegalStateException("Expected drive to have 10 slots");
        }
        ms.pushPose();
        ms.translate(0.5, 0.5, 0.5);
        BlockOrientation blockOrientation = BlockOrientation.get(drive);
        ms.mulPose(blockOrientation.getQuaternion());
        ms.translate(-0.5, -0.5, -0.5);
        VertexConsumer buffer = buffers.getBuffer(CellLedRenderer.RENDER_LAYER);
        Vector3f slotTranslation = new Vector3f();
        for (int row = 0; row < 5; ++row) {
            for (int col = 0; col < 2; ++col) {
                ms.pushPose();
                DriveBakedModel.getSlotOrigin(row, col, slotTranslation);
                ms.translate(slotTranslation.x(), slotTranslation.y(), slotTranslation.z());
                int slot = row * 2 + col;
                CellLedRenderer.renderLed(drive, slot, buffer, ms, partialTicks);
                ms.popPose();
            }
        }
        ms.popPose();
    }
}

