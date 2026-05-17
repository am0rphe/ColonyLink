/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.block.BlockRenderDispatcher
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.client.renderer.entity.TntMinecartRenderer
 *  net.minecraft.client.renderer.texture.TextureAtlas
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 *  org.joml.Quaternionf
 */
package appeng.entity;

import appeng.core.definitions.AEBlocks;
import appeng.entity.TinyTNTPrimedEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TntMinecartRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Quaternionf;

@OnlyIn(value=Dist.CLIENT)
public class TinyTNTPrimedRenderer
extends EntityRenderer<TinyTNTPrimedEntity> {
    private final BlockRenderDispatcher blockRenderer;

    public TinyTNTPrimedRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.25f;
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    public void render(TinyTNTPrimedEntity tnt, float entityYaw, float partialTicks, PoseStack mStack, MultiBufferSource buffers, int packedLight) {
        mStack.pushPose();
        mStack.translate(0.0f, 0.5f, 0.0f);
        if ((float)tnt.getFuse() - partialTicks + 1.0f < 10.0f) {
            float f2 = 1.0f - ((float)tnt.getFuse() - partialTicks + 1.0f) / 10.0f;
            if (f2 < 0.0f) {
                f2 = 0.0f;
            }
            if (f2 > 1.0f) {
                f2 = 1.0f;
            }
            f2 *= f2;
            f2 *= f2;
            float f3 = 1.0f + f2 * 0.3f;
            mStack.scale(f3, f3, f3);
        }
        mStack.mulPose(new Quaternionf().rotationY(-1.5707964f));
        mStack.translate(-0.5, -0.5, 0.5);
        mStack.mulPose(new Quaternionf().rotationY(1.5707964f));
        TntMinecartRenderer.renderWhiteSolidBlock((BlockRenderDispatcher)this.blockRenderer, (BlockState)AEBlocks.TINY_TNT.block().defaultBlockState(), (PoseStack)mStack, (MultiBufferSource)buffers, (int)packedLight, (tnt.getFuse() / 5 % 2 == 0 ? 1 : 0) != 0);
        mStack.popPose();
        super.render((Entity)tnt, entityYaw, partialTicks, mStack, buffers, packedLight);
    }

    public ResourceLocation getTextureLocation(TinyTNTPrimedEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}

