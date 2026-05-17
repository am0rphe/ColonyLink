/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.blockentity.BlockEntityRenderer
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package appeng.client.render.tesr;

import appeng.api.orientation.BlockOrientation;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.client.render.renderable.Renderable;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(value=Dist.CLIENT)
public class ModularTESR<T extends AEBaseBlockEntity>
implements BlockEntityRenderer<T> {
    private final List<Renderable<? super T>> renderables;

    @SafeVarargs
    public ModularTESR(Renderable<? super T> ... renderables) {
        this.renderables = ImmutableList.copyOf((Object[])renderables);
    }

    public void render(T blockEntity, float partialTicks, PoseStack ms, MultiBufferSource buffers, int combinedLight, int combinedOverlay) {
        ms.pushPose();
        ms.translate(0.5, 0.5, 0.5);
        BlockOrientation blockOrientation = BlockOrientation.get(blockEntity);
        ms.mulPose(blockOrientation.getQuaternion());
        ms.translate(-0.5, -0.5, -0.5);
        for (Renderable<T> renderable : this.renderables) {
            renderable.renderBlockEntityAt(blockEntity, partialTicks, ms, buffers, combinedLight, combinedOverlay);
        }
        ms.popPose();
    }
}

