/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.math.Transformation
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  org.apache.commons.lang3.tuple.Pair
 */
package appeng.client.render.renderable;

import appeng.client.render.renderable.Renderable;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.commons.lang3.tuple.Pair;

public class ItemRenderable<T extends BlockEntity>
implements Renderable<T> {
    private final Function<T, Pair<ItemStack, Transformation>> f;

    public ItemRenderable(Function<T, Pair<ItemStack, Transformation>> f) {
        this.f = f;
    }

    @Override
    public void renderBlockEntityAt(T be, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int combinedLight, int combinedOverlay) {
        Pair<ItemStack, Transformation> pair = this.f.apply(be);
        if (pair != null && pair.getLeft() != null) {
            poseStack.pushPose();
            if (pair.getRight() != null) {
                poseStack.mulPose(((Transformation)pair.getRight()).getMatrix());
            }
            Minecraft.getInstance().getItemRenderer().renderStatic((ItemStack)pair.getLeft(), ItemDisplayContext.GROUND, combinedLight, combinedOverlay, poseStack, buffers, be.getLevel(), 0);
            poseStack.popPose();
        }
    }
}

