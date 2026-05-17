/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.block.model.BakedQuad
 *  net.minecraft.client.renderer.block.model.ItemOverrides
 *  net.minecraft.client.renderer.texture.TextureAtlas
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.client.resources.model.Material
 *  net.minecraft.core.Direction
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 *  net.neoforged.neoforge.client.ChunkRenderTypeSet
 *  net.neoforged.neoforge.client.model.IDynamicBakedModel
 *  net.neoforged.neoforge.client.model.data.ModelData
 *  org.jetbrains.annotations.Nullable
 */
package appeng.block.qnb;

import appeng.block.qnb.QnbFormedState;
import appeng.blockentity.qnb.QuantumBridgeBlockEntity;
import appeng.client.render.cablebus.CubeBuilder;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

class QnbFormedBakedModel
implements IDynamicBakedModel {
    private static final ChunkRenderTypeSet RENDER_TYPES = ChunkRenderTypeSet.of((RenderType[])new RenderType[]{RenderType.CUTOUT});
    private static final Material TEXTURE_LINK = new Material(TextureAtlas.LOCATION_BLOCKS, AppEng.makeId("block/quantum_link"));
    private static final Material TEXTURE_RING = new Material(TextureAtlas.LOCATION_BLOCKS, AppEng.makeId("block/quantum_ring"));
    private static final Material TEXTURE_RING_LIGHT = new Material(TextureAtlas.LOCATION_BLOCKS, AppEng.makeId("block/quantum_ring_light"));
    private static final Material TEXTURE_RING_LIGHT_CORNER = new Material(TextureAtlas.LOCATION_BLOCKS, AppEng.makeId("block/quantum_ring_light_corner"));
    private static final Material TEXTURE_CABLE_GLASS = new Material(TextureAtlas.LOCATION_BLOCKS, AppEng.makeId("part/cable/glass/transparent"));
    private static final Material TEXTURE_COVERED_CABLE = new Material(TextureAtlas.LOCATION_BLOCKS, AppEng.makeId("part/cable/covered/transparent"));
    private static final float DEFAULT_RENDER_MIN = 2.0f;
    private static final float DEFAULT_RENDER_MAX = 14.0f;
    private static final float CORNER_POWERED_RENDER_MIN = 3.9f;
    private static final float CORNER_POWERED_RENDER_MAX = 12.1f;
    private static final float CENTER_POWERED_RENDER_MIN = -0.01f;
    private static final float CENTER_POWERED_RENDER_MAX = 16.01f;
    private final BakedModel baseModel;
    private final Block linkBlock;
    private final TextureAtlasSprite linkTexture;
    private final TextureAtlasSprite ringTexture;
    private final TextureAtlasSprite glassCableTexture;
    private final TextureAtlasSprite coveredCableTexture;
    private final TextureAtlasSprite lightTexture;
    private final TextureAtlasSprite lightCornerTexture;

    public QnbFormedBakedModel(BakedModel baseModel, Function<Material, TextureAtlasSprite> bakedTextureGetter) {
        this.baseModel = baseModel;
        this.linkTexture = bakedTextureGetter.apply(TEXTURE_LINK);
        this.ringTexture = bakedTextureGetter.apply(TEXTURE_RING);
        this.glassCableTexture = bakedTextureGetter.apply(TEXTURE_CABLE_GLASS);
        this.coveredCableTexture = bakedTextureGetter.apply(TEXTURE_COVERED_CABLE);
        this.lightTexture = bakedTextureGetter.apply(TEXTURE_RING_LIGHT);
        this.lightCornerTexture = bakedTextureGetter.apply(TEXTURE_RING_LIGHT_CORNER);
        this.linkBlock = AEBlocks.QUANTUM_LINK.block();
    }

    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData modelData, RenderType renderType) {
        QnbFormedState formedState = (QnbFormedState)modelData.get(QuantumBridgeBlockEntity.FORMED_STATE);
        if (formedState == null) {
            return this.baseModel.getQuads(state, side, rand);
        }
        if (side != null) {
            return Collections.emptyList();
        }
        return this.getQuads(formedState, state);
    }

    private List<BakedQuad> getQuads(QnbFormedState formedState, BlockState state) {
        CubeBuilder builder = new CubeBuilder();
        if (state.getBlock() == this.linkBlock) {
            Set<Direction> sides = formedState.getAdjacentQuantumBridges();
            this.renderCableAt(builder, 1.76f, this.glassCableTexture, 2.256f, sides);
            this.renderCableAt(builder, 3.008f, this.coveredCableTexture, 3.0f, sides);
            builder.setTexture(this.linkTexture);
            builder.addCube(2.0f, 2.0f, 2.0f, 14.0f, 14.0f, 14.0f);
        } else if (formedState.isCorner()) {
            this.renderCableAt(builder, 3.008f, this.coveredCableTexture, 0.8f, formedState.getAdjacentQuantumBridges());
            builder.setTexture(this.ringTexture);
            builder.addCube(2.0f, 2.0f, 2.0f, 14.0f, 14.0f, 14.0f);
            if (formedState.isPowered()) {
                builder.setTexture(this.lightCornerTexture);
                builder.setEmissiveMaterial(true);
                for (Direction facing : Direction.values()) {
                    float xOffset = Math.abs((float)facing.getStepX() * 0.01f);
                    float yOffset = Math.abs((float)facing.getStepY() * 0.01f);
                    float zOffset = Math.abs((float)facing.getStepZ() * 0.01f);
                    builder.setDrawFaces(EnumSet.of(facing));
                    builder.addCube(2.0f - xOffset, 2.0f - yOffset, 2.0f - zOffset, 14.0f + xOffset, 14.0f + yOffset, 14.0f + zOffset);
                }
                builder.setEmissiveMaterial(false);
            }
        } else {
            builder.setTexture(this.ringTexture);
            builder.addCube(0.0f, 2.0f, 2.0f, 16.0f, 14.0f, 14.0f);
            builder.addCube(2.0f, 0.0f, 2.0f, 14.0f, 16.0f, 14.0f);
            builder.addCube(2.0f, 2.0f, 0.0f, 14.0f, 14.0f, 16.0f);
            if (formedState.isPowered()) {
                builder.setTexture(this.lightTexture);
                builder.setEmissiveMaterial(true);
                for (Direction facing : Direction.values()) {
                    float xOffset = Math.abs((float)facing.getStepX() * 0.01f);
                    float yOffset = Math.abs((float)facing.getStepY() * 0.01f);
                    float zOffset = Math.abs((float)facing.getStepZ() * 0.01f);
                    builder.setDrawFaces(EnumSet.of(facing));
                    builder.addCube(-xOffset, -yOffset, -zOffset, 16.0f + xOffset, 16.0f + yOffset, 16.0f + zOffset);
                }
            }
        }
        return builder.getOutput();
    }

    private void renderCableAt(CubeBuilder builder, float thickness, TextureAtlasSprite texture, float pull, Set<Direction> connections) {
        builder.setTexture(texture);
        if (connections.contains(Direction.WEST)) {
            builder.addCube(0.0f, 8.0f - thickness, 8.0f - thickness, 8.0f - thickness - pull, 8.0f + thickness, 8.0f + thickness);
        }
        if (connections.contains(Direction.EAST)) {
            builder.addCube(8.0f + thickness + pull, 8.0f - thickness, 8.0f - thickness, 16.0f, 8.0f + thickness, 8.0f + thickness);
        }
        if (connections.contains(Direction.NORTH)) {
            builder.addCube(8.0f - thickness, 8.0f - thickness, 0.0f, 8.0f + thickness, 8.0f + thickness, 8.0f - thickness - pull);
        }
        if (connections.contains(Direction.SOUTH)) {
            builder.addCube(8.0f - thickness, 8.0f - thickness, 8.0f + thickness + pull, 8.0f + thickness, 8.0f + thickness, 16.0f);
        }
        if (connections.contains(Direction.DOWN)) {
            builder.addCube(8.0f - thickness, 0.0f, 8.0f - thickness, 8.0f + thickness, 8.0f - thickness - pull, 8.0f + thickness);
        }
        if (connections.contains(Direction.UP)) {
            builder.addCube(8.0f - thickness, 8.0f + thickness + pull, 8.0f - thickness, 8.0f + thickness, 16.0f, 8.0f + thickness);
        }
    }

    public boolean useAmbientOcclusion() {
        return this.baseModel.useAmbientOcclusion();
    }

    public boolean isGui3d() {
        return true;
    }

    public boolean usesBlockLight() {
        return false;
    }

    public boolean isCustomRenderer() {
        return false;
    }

    public TextureAtlasSprite getParticleIcon() {
        return this.baseModel.getParticleIcon();
    }

    public ItemOverrides getOverrides() {
        return this.baseModel.getOverrides();
    }

    public static List<Material> getRequiredTextures() {
        return ImmutableList.of((Object)TEXTURE_LINK, (Object)TEXTURE_RING, (Object)TEXTURE_CABLE_GLASS, (Object)TEXTURE_COVERED_CABLE, (Object)TEXTURE_RING_LIGHT, (Object)TEXTURE_RING_LIGHT_CORNER);
    }

    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        return RENDER_TYPES;
    }
}

