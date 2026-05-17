/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.blockentity.BlockEntityRenderer
 *  net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider$Context
 *  net.minecraft.core.Direction
 */
package appeng.blockentity.networking;

import appeng.api.parts.IPart;
import appeng.blockentity.networking.CableBusBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

public class CableBusTESR
implements BlockEntityRenderer<CableBusBlockEntity> {
    public CableBusTESR(BlockEntityRendererProvider.Context context) {
    }

    public void render(CableBusBlockEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffers, int combinedLightIn, int combinedOverlayIn) {
        if (!te.getCableBus().isRequiresDynamicRender()) {
            return;
        }
        for (Direction facing : Direction.values()) {
            IPart part = te.getPart(facing);
            if (part == null || !part.requireDynamicRender()) continue;
            part.renderDynamic(partialTicks, ms, buffers, combinedLightIn, combinedOverlayIn);
        }
    }

    public int getViewDistance() {
        return 900;
    }
}

