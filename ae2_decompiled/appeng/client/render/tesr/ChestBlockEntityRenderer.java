/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.block.ModelBlockRenderer
 *  net.minecraft.client.renderer.block.model.BakedQuad
 *  net.minecraft.client.renderer.blockentity.BlockEntityRenderer
 *  net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider$Context
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.client.resources.model.ModelManager
 *  net.minecraft.core.Direction
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.level.BlockAndTintGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.state.BlockState
 *  net.neoforged.neoforge.client.model.data.ModelData
 *  org.jetbrains.annotations.Nullable
 */
package appeng.client.render.tesr;

import appeng.api.orientation.BlockOrientation;
import appeng.blockentity.storage.MEChestBlockEntity;
import appeng.client.render.BakedModelUnwrapper;
import appeng.client.render.DelegateBakedModel;
import appeng.client.render.model.DriveBakedModel;
import appeng.client.render.tesr.CellLedRenderer;
import appeng.core.definitions.AEBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

public class ChestBlockEntityRenderer
implements BlockEntityRenderer<MEChestBlockEntity> {
    private final ModelManager modelManager;
    private final ModelBlockRenderer blockRenderer;

    public ChestBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        Minecraft client = Minecraft.getInstance();
        this.modelManager = client.getModelManager();
        this.blockRenderer = client.getBlockRenderer().getModelRenderer();
    }

    public void render(MEChestBlockEntity chest, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int combinedLight, int combinedOverlay) {
        Level level = chest.getLevel();
        if (level == null) {
            return;
        }
        Item cellItem = chest.getCellItem(0);
        if (cellItem == null) {
            return;
        }
        DriveBakedModel driveModel = this.getDriveModel();
        if (driveModel == null) {
            return;
        }
        BakedModel cellModel = driveModel.getCellChassisModel(cellItem);
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        BlockOrientation rotation = BlockOrientation.get(chest);
        poseStack.mulPose(rotation.getQuaternion());
        poseStack.translate(-0.5, -0.5, -0.5);
        poseStack.translate(0.3125, 0.25, 0.0);
        VertexConsumer buffer = buffers.getBuffer(RenderType.cutout());
        FaceRotatingModel rotatedModel = new FaceRotatingModel(cellModel, rotation);
        this.blockRenderer.tesselateBlock((BlockAndTintGetter)level, (BakedModel)rotatedModel, chest.getBlockState(), chest.getBlockPos(), poseStack, buffer, false, RandomSource.create(), 0L, combinedOverlay, ModelData.EMPTY, null);
        VertexConsumer ledBuffer = buffers.getBuffer(CellLedRenderer.RENDER_LAYER);
        CellLedRenderer.renderLed(chest, 0, ledBuffer, poseStack, partialTicks);
        poseStack.popPose();
    }

    private DriveBakedModel getDriveModel() {
        BakedModel driveModel = this.modelManager.getBlockModelShaper().getBlockModel(AEBlocks.DRIVE.block().defaultBlockState());
        return BakedModelUnwrapper.unwrap(driveModel, DriveBakedModel.class);
    }

    public static class FaceRotatingModel
    extends DelegateBakedModel {
        private final BlockOrientation r;

        protected FaceRotatingModel(BakedModel base, BlockOrientation r) {
            super(base);
            this.r = r;
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData, RenderType renderType) {
            if (side != null) {
                side = this.r.resultingRotate(side);
            }
            ArrayList<BakedQuad> quads = new ArrayList<BakedQuad>(super.getQuads(state, side, rand, extraData, renderType));
            for (int i = 0; i < quads.size(); ++i) {
                BakedQuad quad = (BakedQuad)quads.get(i);
                quads.set(i, new BakedQuad(quad.getVertices(), quad.getTintIndex(), this.r.rotate(quad.getDirection()), quad.getSprite(), quad.isShade()));
            }
            return quads;
        }
    }
}

