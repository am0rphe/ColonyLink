/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.block.model.BakedQuad
 *  net.minecraft.client.renderer.blockentity.BlockEntityRenderer
 *  net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider$Context
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.core.BlockPos
 *  net.minecraft.util.Mth
 *  net.minecraft.world.inventory.InventoryMenu
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.material.Fluid
 *  net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions
 *  net.neoforged.neoforge.fluids.FluidStack
 *  net.neoforged.neoforge.fluids.FluidType
 */
package appeng.client.render.tesr;

import appeng.blockentity.storage.SkyStoneTankBlockEntity;
import appeng.client.render.cablebus.CubeBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

public final class SkyStoneTankBlockEntityRenderer
implements BlockEntityRenderer<SkyStoneTankBlockEntity> {
    private static final float TANK_W = 0.0635f;
    public static final int FULL_LIGHT = 0xF000F0;

    public SkyStoneTankBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    public void render(SkyStoneTankBlockEntity tank, float tickDelta, PoseStack ms, MultiBufferSource vertexConsumers, int light, int overlay) {
        if (!tank.getTank().getFluid().isEmpty()) {
            SkyStoneTankBlockEntityRenderer.drawFluidInTank(tank, ms, vertexConsumers, tank.getTank().getFluid(), (float)tank.getTank().getFluid().getAmount() / (float)tank.getTank().getCapacity());
        }
    }

    public static void drawFluidInTank(BlockEntity be, PoseStack ms, MultiBufferSource vcp, FluidStack fluid, float fill) {
        SkyStoneTankBlockEntityRenderer.drawFluidInTank(be.getLevel(), be.getBlockPos(), ms, vcp, fluid, fill);
    }

    public static void drawFluidInTank(Level level, BlockPos pos, PoseStack ps, MultiBufferSource mbs, FluidStack fluid, float fill) {
        float fillY;
        VertexConsumer vc = mbs.getBuffer(RenderType.translucentMovingBlock());
        IClientFluidTypeExtensions renderProps = IClientFluidTypeExtensions.of((Fluid)fluid.getFluid());
        TextureAtlasSprite sprite = (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(renderProps.getStillTexture(fluid));
        int color = renderProps.getTintColor(fluid);
        float r = (float)(color >> 16 & 0xFF) / 256.0f;
        float g = (float)(color >> 8 & 0xFF) / 256.0f;
        float b = (float)(color & 0xFF) / 256.0f;
        float topHeight = fillY = Mth.lerp((float)Mth.clamp((float)fill, (float)0.0f, (float)1.0f), (float)0.0635f, (float)0.9365f);
        float bottomHeight = 0.0635f;
        FluidType attributes = fluid.getFluid().getFluidType();
        if (attributes.isLighterThanAir()) {
            topHeight = 0.9365f;
            bottomHeight = 1.0f - fillY;
        }
        CubeBuilder builder = new CubeBuilder();
        builder.setTexture(sprite);
        float x1 = 1.016f;
        float z1 = 1.016f;
        float x2 = 14.984f;
        float z2 = 14.984f;
        float y1 = bottomHeight * 16.0f;
        float y2 = topHeight * 16.0f;
        builder.addCube(x1, y1, z1, x2, y2, z2);
        for (BakedQuad bakedQuad : builder.getOutput()) {
            vc.putBulkData(ps.last(), bakedQuad, r, g, b, 1.0f, 0xF000F0, OverlayTexture.NO_OVERLAY);
        }
    }
}

