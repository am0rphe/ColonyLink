/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.BufferBuilder
 *  com.mojang.blaze3d.vertex.BufferUploader
 *  com.mojang.blaze3d.vertex.MeshData
 *  com.mojang.blaze3d.vertex.Tesselator
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.FogRenderer
 *  net.minecraft.client.renderer.ItemBlockRenderTypes
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.block.BlockRenderDispatcher
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Holder
 *  net.minecraft.world.level.BlockAndTintGetter
 *  net.minecraft.world.level.ColorResolver
 *  net.minecraft.world.level.LightLayer
 *  net.minecraft.world.level.biome.Biome
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.lighting.LevelLightEngine
 *  net.minecraft.world.level.material.Fluid
 *  net.minecraft.world.level.material.FluidState
 *  net.minecraft.world.level.material.Fluids
 *  org.joml.Matrix4fStack
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 */
package appeng.integration.modules.itemlists;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

public final class FluidBlockRendering {
    private FluidBlockRendering() {
    }

    public static void render(GuiGraphics guiGraphics, Fluid fluid, int x, int y, int width, int height) {
        RenderSystem.runAsFancy(() -> FluidBlockRendering.renderInFancy(guiGraphics, fluid, x, y, width, height));
    }

    public static void renderInFancy(GuiGraphics guiGraphics, Fluid fluid, int x, int y, int width, int height) {
        FluidState fluidState = fluid.defaultFluidState();
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        RenderType renderType = ItemBlockRenderTypes.getRenderLayer((FluidState)fluidState);
        renderType.setupRenderState();
        RenderSystem.disableDepthTest();
        Matrix4fStack worldMatStack = RenderSystem.getModelViewStack();
        worldMatStack.pushMatrix();
        worldMatStack.mul((Matrix4fc)guiGraphics.pose().last().pose());
        worldMatStack.translate((float)x, (float)y, 0.0f);
        FogRenderer.setupNoFog();
        worldMatStack.translate((float)width / 2.0f, (float)height / 2.0f, 0.0f);
        worldMatStack.scale((float)width, (float)height, 1.0f);
        FluidBlockRendering.setupOrthographicProjection(worldMatStack);
        BufferBuilder builder = Tesselator.getInstance().begin(renderType.mode(), renderType.format());
        blockRenderer.renderLiquid(BlockPos.ZERO, (BlockAndTintGetter)new FakeWorld(fluidState), (VertexConsumer)builder, fluidState.createLegacyBlock(), fluidState);
        MeshData meshData = builder.build();
        if (meshData != null) {
            BufferUploader.drawWithShader((MeshData)meshData);
        }
        renderType.clearRenderState();
        worldMatStack.popMatrix();
        RenderSystem.applyModelViewMatrix();
    }

    private static void setupOrthographicProjection(Matrix4fStack worldMatStack) {
        float angle = 36.0f;
        float rotation = 45.0f;
        worldMatStack.scale(1.0f, 1.0f, -1.0f);
        worldMatStack.rotate((Quaternionfc)new Quaternionf().rotationY((float)(-Math.PI)));
        Quaternionf flip = new Quaternionf().rotationZ((float)Math.PI);
        flip.mul((Quaternionfc)new Quaternionf().rotationX((float)Math.PI / 180 * angle));
        Quaternionf rotate = new Quaternionf().rotationY((float)Math.PI / 180 * rotation);
        worldMatStack.rotate((Quaternionfc)flip);
        worldMatStack.rotate((Quaternionfc)rotate);
        worldMatStack.translate(-0.5f, -0.5f, -0.5f);
        RenderSystem.applyModelViewMatrix();
    }

    private static class FakeWorld
    implements BlockAndTintGetter {
        private final FluidState fluidState;

        public FakeWorld(FluidState fluidState) {
            this.fluidState = fluidState;
        }

        public float getShade(Direction direction, boolean bl) {
            return 1.0f;
        }

        public LevelLightEngine getLightEngine() {
            throw new UnsupportedOperationException();
        }

        public int getBrightness(LightLayer lightLayer, BlockPos blockPos) {
            return 15;
        }

        public int getRawBrightness(BlockPos blockPos, int i) {
            return 15;
        }

        public int getBlockTint(BlockPos blockPos, ColorResolver colorResolver) {
            ClientLevel level = Minecraft.getInstance().level;
            if (level != null) {
                Holder biome = Minecraft.getInstance().level.getBiome(blockPos);
                return colorResolver.getColor((Biome)biome.value(), 0.0, 0.0);
            }
            return -1;
        }

        public BlockEntity getBlockEntity(BlockPos blockPos) {
            return null;
        }

        public BlockState getBlockState(BlockPos blockPos) {
            if (blockPos.equals((Object)BlockPos.ZERO)) {
                return this.fluidState.createLegacyBlock();
            }
            return Blocks.AIR.defaultBlockState();
        }

        public FluidState getFluidState(BlockPos blockPos) {
            if (blockPos.equals((Object)BlockPos.ZERO)) {
                return this.fluidState;
            }
            return Fluids.EMPTY.defaultFluidState();
        }

        public int getHeight() {
            return 0;
        }

        public int getMinBuildHeight() {
            return 0;
        }
    }
}

