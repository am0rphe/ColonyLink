/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Strings
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.block.model.BakedQuad
 *  net.minecraft.client.renderer.block.model.ItemOverrides
 *  net.minecraft.client.renderer.texture.TextureAtlas
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.client.resources.model.Material
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.Mth
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.level.BlockAndTintGetter
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.neoforge.client.ChunkRenderTypeSet
 *  net.neoforged.neoforge.client.model.IDynamicBakedModel
 *  net.neoforged.neoforge.client.model.data.ModelData
 *  net.neoforged.neoforge.client.model.data.ModelProperty
 *  net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 *  org.joml.Vector3f
 */
package appeng.client.render.model;

import appeng.client.render.model.RenderHelper;
import appeng.decorative.solid.GlassState;
import appeng.decorative.solid.QuartzGlassBlock;
import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.IntStream;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class GlassBakedModel
implements IDynamicBakedModel {
    private static final ChunkRenderTypeSet RENDER_TYPES = ChunkRenderTypeSet.of((RenderType[])new RenderType[]{RenderType.CUTOUT});
    public static final ModelProperty<GlassState> GLASS_STATE = new ModelProperty();
    static final Material TEXTURE_A = new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.parse((String)"ae2:block/glass/quartz_glass_a"));
    static final Material TEXTURE_B = new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.parse((String)"ae2:block/glass/quartz_glass_b"));
    static final Material TEXTURE_C = new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.parse((String)"ae2:block/glass/quartz_glass_c"));
    static final Material TEXTURE_D = new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.parse((String)"ae2:block/glass/quartz_glass_d"));
    static final Material[] TEXTURES_FRAME = GlassBakedModel.generateTexturesFrame();
    private final TextureAtlasSprite[] glassTextures;
    private final TextureAtlasSprite[] frameTextures;

    private static Material[] generateTexturesFrame() {
        return (Material[])IntStream.range(1, 16).mapToObj(Integer::toBinaryString).map(s -> Strings.padStart((String)s, (int)4, (char)'0')).map(s -> ResourceLocation.parse((String)("ae2:block/glass/quartz_glass_frame" + s))).map(rl -> new Material(TextureAtlas.LOCATION_BLOCKS, rl)).toArray(Material[]::new);
    }

    public GlassBakedModel(Function<Material, TextureAtlasSprite> bakedTextureGetter) {
        this.glassTextures = new TextureAtlasSprite[]{bakedTextureGetter.apply(TEXTURE_A), bakedTextureGetter.apply(TEXTURE_B), bakedTextureGetter.apply(TEXTURE_C), bakedTextureGetter.apply(TEXTURE_D)};
        this.frameTextures = new TextureAtlasSprite[16];
        for (int i = 0; i < TEXTURES_FRAME.length; ++i) {
            this.frameTextures[1 + i] = bakedTextureGetter.apply(TEXTURES_FRAME[i]);
        }
    }

    @NotNull
    public ModelData getModelData(BlockAndTintGetter blockView, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData modelData) {
        GlassState glassState = GlassBakedModel.getGlassState(blockView, state, pos);
        return modelData.derive().with(GLASS_STATE, (Object)glassState).build();
    }

    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData, RenderType renderType) {
        if (side == null) {
            return Collections.emptyList();
        }
        GlassState glassState = Objects.requireNonNullElse((GlassState)extraData.get(GLASS_STATE), GlassState.DEFAULT);
        int randomOffset = rand.nextInt(4);
        float u = (float)randomOffset / 16.0f;
        float v = (float)rand.nextInt(4) / 16.0f;
        int texIdx = (randomOffset + rand.nextInt(4)) % 4;
        if (texIdx < 2) {
            u /= 2.0f;
            v /= 2.0f;
        }
        TextureAtlasSprite glassTexture = this.glassTextures[texIdx];
        if (glassState.hasAdjacentGlassBlock(side)) {
            return Collections.emptyList();
        }
        ArrayList<BakedQuad> quads = new ArrayList<BakedQuad>(5);
        List<Vector3f> corners = RenderHelper.getFaceCorners(side);
        quads.add(this.createQuad(side, corners, glassTexture, u, v));
        int edgeBitmask = glassState.getMask(side);
        TextureAtlasSprite sideSprite = this.frameTextures[edgeBitmask];
        if (sideSprite != null) {
            quads.add(this.createQuad(side, corners, sideSprite, 0.0f, 0.0f));
        }
        return quads;
    }

    public boolean usesBlockLight() {
        return false;
    }

    private static int makeBitmask(BlockAndTintGetter level, BlockState state, BlockPos pos, Direction side) {
        return switch (side) {
            default -> throw new MatchException(null, null);
            case Direction.DOWN -> GlassBakedModel.makeBitmask(level, state, pos, side, Direction.SOUTH, Direction.EAST, Direction.NORTH, Direction.WEST);
            case Direction.UP -> GlassBakedModel.makeBitmask(level, state, pos, side, Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST);
            case Direction.NORTH -> GlassBakedModel.makeBitmask(level, state, pos, side, Direction.UP, Direction.WEST, Direction.DOWN, Direction.EAST);
            case Direction.SOUTH -> GlassBakedModel.makeBitmask(level, state, pos, side, Direction.UP, Direction.EAST, Direction.DOWN, Direction.WEST);
            case Direction.WEST -> GlassBakedModel.makeBitmask(level, state, pos, side, Direction.UP, Direction.SOUTH, Direction.DOWN, Direction.NORTH);
            case Direction.EAST -> GlassBakedModel.makeBitmask(level, state, pos, side, Direction.UP, Direction.NORTH, Direction.DOWN, Direction.SOUTH);
        };
    }

    private static int makeBitmask(BlockAndTintGetter level, BlockState state, BlockPos pos, Direction face, Direction up, Direction right, Direction down, Direction left) {
        int bitmask = 0;
        if (!GlassBakedModel.isGlassBlock(level, state, pos, face, up, face)) {
            bitmask |= 1;
        }
        if (!GlassBakedModel.isGlassBlock(level, state, pos, face, right, face)) {
            bitmask |= 2;
        }
        if (!GlassBakedModel.isGlassBlock(level, state, pos, face, down, face)) {
            bitmask |= 4;
        }
        if (!GlassBakedModel.isGlassBlock(level, state, pos, face, left, face)) {
            bitmask |= 8;
        }
        return bitmask;
    }

    private BakedQuad createQuad(Direction side, List<Vector3f> corners, TextureAtlasSprite sprite, float uOffset, float vOffset) {
        return this.createQuad(side, corners.get(0), corners.get(1), corners.get(2), corners.get(3), sprite, uOffset, vOffset);
    }

    private BakedQuad createQuad(Direction side, Vector3f c1, Vector3f c2, Vector3f c3, Vector3f c4, TextureAtlasSprite sprite, float uOffset, float vOffset) {
        Vec3 normal = new Vec3((double)side.getNormal().getX(), (double)side.getNormal().getY(), (double)side.getNormal().getZ());
        float u1 = Mth.clamp((float)(0.0f - uOffset), (float)0.0f, (float)1.0f);
        float u2 = Mth.clamp((float)(1.0f - uOffset), (float)0.0f, (float)1.0f);
        float v1 = Mth.clamp((float)(0.0f - vOffset), (float)0.0f, (float)1.0f);
        float v2 = Mth.clamp((float)(1.0f - vOffset), (float)0.0f, (float)1.0f);
        QuadBakingVertexConsumer builder = new QuadBakingVertexConsumer();
        builder.setSprite(sprite);
        builder.setDirection(side);
        this.putVertex(builder, normal, c1.x(), c1.y(), c1.z(), sprite, u1, v1);
        this.putVertex(builder, normal, c2.x(), c2.y(), c2.z(), sprite, u1, v2);
        this.putVertex(builder, normal, c3.x(), c3.y(), c3.z(), sprite, u2, v2);
        this.putVertex(builder, normal, c4.x(), c4.y(), c4.z(), sprite, u2, v1);
        return builder.bakeQuad();
    }

    private void putVertex(QuadBakingVertexConsumer builder, Vec3 normal, float x, float y, float z, TextureAtlasSprite sprite, float u, float v) {
        builder.addVertex(x, y, z);
        builder.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        builder.setNormal((float)normal.x, (float)normal.y, (float)normal.z);
        u = sprite.getU(u);
        v = sprite.getV(v);
        builder.setUv(u, v);
    }

    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    public boolean useAmbientOcclusion() {
        return false;
    }

    public boolean isGui3d() {
        return false;
    }

    public boolean isCustomRenderer() {
        return false;
    }

    public TextureAtlasSprite getParticleIcon() {
        return this.frameTextures[this.frameTextures.length - 1];
    }

    private static GlassState getGlassState(BlockAndTintGetter level, BlockState state, BlockPos pos) {
        int[] masks = new int[6];
        for (Direction facing : Direction.values()) {
            masks[facing.get3DDataValue()] = GlassBakedModel.makeBitmask(level, state, pos, facing);
        }
        boolean[] adjacentGlassBlocks = new boolean[6];
        for (Direction facing : Direction.values()) {
            adjacentGlassBlocks[facing.get3DDataValue()] = GlassBakedModel.isGlassBlock(level, state, pos, facing, facing, facing.getOpposite());
        }
        return new GlassState(masks, adjacentGlassBlocks);
    }

    private static boolean isGlassBlock(BlockAndTintGetter level, BlockState state, BlockPos pos, Direction queryingFace, Direction adjDir, Direction adjFace) {
        BlockPos adjacentPos = pos.relative(adjDir);
        BlockState adjacentState = level.getBlockState(adjacentPos);
        if (!(adjacentState.getAppearance(level, adjacentPos, adjFace, state, pos).getBlock() instanceof QuartzGlassBlock)) {
            return false;
        }
        return state.getAppearance(level, pos, queryingFace, adjacentState, adjacentPos).getBlock() instanceof QuartzGlassBlock;
    }

    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        return RENDER_TYPES;
    }
}

