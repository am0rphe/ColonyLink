/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.model.geom.ModelLayerLocation
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.client.model.geom.PartPose
 *  net.minecraft.client.model.geom.builders.CubeListBuilder
 *  net.minecraft.client.model.geom.builders.LayerDefinition
 *  net.minecraft.client.model.geom.builders.MeshDefinition
 *  net.minecraft.client.model.geom.builders.PartDefinition
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.blockentity.BlockEntityRenderer
 *  net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider$Context
 *  net.minecraft.client.renderer.texture.TextureAtlas
 *  net.minecraft.client.resources.model.Material
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.phys.AABB
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 *  org.joml.Quaternionf
 */
package appeng.client.render.tesr;

import appeng.block.storage.SkyChestBlock;
import appeng.blockentity.storage.SkyChestBlockEntity;
import appeng.core.AppEng;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Quaternionf;

@OnlyIn(value=Dist.CLIENT)
public class SkyChestTESR
implements BlockEntityRenderer<SkyChestBlockEntity> {
    public static ModelLayerLocation MODEL_LAYER = new ModelLayerLocation(AppEng.makeId("sky_chest"), "main");
    public static final Material TEXTURE_STONE = new Material(TextureAtlas.LOCATION_BLOCKS, AppEng.makeId("block/skychest"));
    public static final Material TEXTURE_BLOCK = new Material(TextureAtlas.LOCATION_BLOCKS, AppEng.makeId("block/skyblockchest"));
    private final ModelPart lid;
    private final ModelPart bottom;
    private final ModelPart lock;

    public SkyChestTESR(BlockEntityRendererProvider.Context context) {
        ModelPart modelpart = context.bakeLayer(MODEL_LAYER);
        this.bottom = modelpart.getChild("bottom");
        this.lid = modelpart.getChild("lid");
        this.lock = modelpart.getChild("lock");
    }

    public AABB getRenderBoundingBox(SkyChestBlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        return AABB.encapsulatingFullBlocks((BlockPos)pos.offset(-1, 0, -1), (BlockPos)pos.offset(1, 1, 1));
    }

    public static LayerDefinition createSingleBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 19).addBox(1.0f, 0.0f, 1.0f, 14.0f, 10.0f, 14.0f), PartPose.ZERO);
        partdefinition.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 0).addBox(1.0f, 0.0f, 0.0f, 14.0f, 5.0f, 14.0f), PartPose.offset((float)0.0f, (float)10.0f, (float)1.0f));
        partdefinition.addOrReplaceChild("lock", CubeListBuilder.create().texOffs(0, 0).addBox(7.0f, -1.0f, 15.0f, 2.0f, 4.0f, 1.0f), PartPose.offset((float)0.0f, (float)9.0f, (float)0.0f));
        return LayerDefinition.create((MeshDefinition)meshdefinition, (int)64, (int)64);
    }

    public void render(SkyChestBlockEntity blockEntity, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        matrixStackIn.pushPose();
        float f = blockEntity.getFront().toYRot();
        matrixStackIn.translate(0.5, 0.5, 0.5);
        matrixStackIn.mulPose(new Quaternionf().rotationY((float)Math.PI / 180 * -f));
        matrixStackIn.translate(-0.5, -0.5, -0.5);
        float f1 = blockEntity.getOpenNess(partialTicks);
        f1 = 1.0f - f1;
        f1 = 1.0f - f1 * f1 * f1;
        Material material = this.getRenderMaterial(blockEntity);
        VertexConsumer ivertexbuilder = material.buffer(bufferIn, RenderType::entityCutout);
        this.renderModels(matrixStackIn, ivertexbuilder, this.lid, this.lock, this.bottom, f1, combinedLightIn, combinedOverlayIn);
        matrixStackIn.popPose();
    }

    private void renderModels(PoseStack matrixStackIn, VertexConsumer bufferIn, ModelPart chestLid, ModelPart chestLatch, ModelPart chestBottom, float lidAngle, int combinedLightIn, int combinedOverlayIn) {
        chestLatch.xRot = chestLid.xRot = -(lidAngle * 1.5707964f);
        chestLid.render(matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
        chestLatch.render(matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
        chestBottom.render(matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
    }

    protected Material getRenderMaterial(SkyChestBlockEntity blockEntity) {
        Block blockType;
        SkyChestBlock.SkyChestType type = SkyChestBlock.SkyChestType.BLOCK;
        if (blockEntity.getLevel() != null && (blockType = blockEntity.getBlockState().getBlock()) instanceof SkyChestBlock) {
            type = ((SkyChestBlock)blockType).type;
        }
        return switch (type) {
            default -> throw new MatchException(null, null);
            case SkyChestBlock.SkyChestType.STONE -> TEXTURE_STONE;
            case SkyChestBlock.SkyChestType.BLOCK -> TEXTURE_BLOCK;
        };
    }
}

