/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.block.BlockRenderDispatcher
 *  net.minecraft.client.renderer.blockentity.BlockEntityRenderer
 *  net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider$Context
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.client.resources.model.ModelManager
 *  net.minecraft.client.resources.model.ModelResourceLocation
 *  net.minecraft.core.BlockPos
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.level.BlockAndTintGetter
 *  net.minecraft.world.level.block.state.BlockState
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 *  org.joml.Quaternionf
 */
package appeng.client.render.tesr;

import appeng.api.orientation.BlockOrientation;
import appeng.blockentity.misc.CrankBlockEntity;
import appeng.core.AppEng;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Quaternionf;

@OnlyIn(value=Dist.CLIENT)
public class CrankRenderer
implements BlockEntityRenderer<CrankBlockEntity> {
    public static final ModelResourceLocation BASE_MODEL = ModelResourceLocation.standalone((ResourceLocation)AppEng.makeId("block/crank_base"));
    public static final ModelResourceLocation HANDLE_MODEL = ModelResourceLocation.standalone((ResourceLocation)AppEng.makeId("block/crank_handle"));
    private final BlockRenderDispatcher blockRenderer;
    private final ModelManager modelManager;

    public CrankRenderer(BlockEntityRendererProvider.Context context) {
        this.modelManager = context.getBlockRenderDispatcher().getBlockModelShaper().getModelManager();
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    public void render(CrankBlockEntity crank, float partialTick, PoseStack stack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        BakedModel baseModel = this.modelManager.getModel(BASE_MODEL);
        BakedModel handleModel = this.modelManager.getModel(HANDLE_MODEL);
        BlockState blockState = crank.getBlockState();
        VertexConsumer buffer = buffers.getBuffer(RenderType.cutout());
        BlockPos pos = crank.getBlockPos();
        stack.pushPose();
        stack.translate(0.5, 0.5, 0.5);
        stack.mulPose(BlockOrientation.get(crank).getQuaternion());
        stack.translate(-0.5, -0.5, -0.5);
        this.blockRenderer.getModelRenderer().tesselateWithAO((BlockAndTintGetter)crank.getLevel(), baseModel, blockState, pos, stack, buffer, false, RandomSource.create(), blockState.getSeed(pos), packedOverlay);
        stack.translate(0.5, 0.5, 0.5);
        stack.mulPose(new Quaternionf().rotationZ((float)(-Math.PI) / 180 * crank.getVisibleRotation()));
        stack.translate(-0.5, -0.5, -0.5);
        this.blockRenderer.getModelRenderer().tesselateWithAO((BlockAndTintGetter)crank.getLevel(), handleModel, blockState, pos, stack, buffer, false, RandomSource.create(), blockState.getSeed(pos), packedOverlay);
        stack.popPose();
    }
}

