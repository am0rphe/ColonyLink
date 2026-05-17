/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.world.level.block.entity.BlockEntity
 */
package appeng.client.render.renderable;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface Renderable<T extends BlockEntity> {
    public void renderBlockEntityAt(T var1, float var2, PoseStack var3, MultiBufferSource var4, int var5, int var6);
}

