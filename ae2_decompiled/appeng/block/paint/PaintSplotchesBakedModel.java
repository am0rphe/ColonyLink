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
 *  net.minecraft.client.resources.model.Material
 *  net.minecraft.core.Direction
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.level.block.state.BlockState
 *  net.neoforged.neoforge.client.ChunkRenderTypeSet
 *  net.neoforged.neoforge.client.model.IDynamicBakedModel
 *  net.neoforged.neoforge.client.model.data.ModelData
 *  org.jetbrains.annotations.Nullable
 */
package appeng.block.paint;

import appeng.block.paint.PaintSplotches;
import appeng.blockentity.misc.PaintSplotchesBlockEntity;
import appeng.client.render.cablebus.CubeBuilder;
import appeng.core.AppEng;
import appeng.helpers.Splotch;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

class PaintSplotchesBakedModel
implements IDynamicBakedModel {
    private static final Material TEXTURE_PAINT1 = new Material(TextureAtlas.LOCATION_BLOCKS, AppEng.makeId("block/paint1"));
    private static final Material TEXTURE_PAINT2 = new Material(TextureAtlas.LOCATION_BLOCKS, AppEng.makeId("block/paint2"));
    private static final Material TEXTURE_PAINT3 = new Material(TextureAtlas.LOCATION_BLOCKS, AppEng.makeId("block/paint3"));
    private static final ChunkRenderTypeSet RENDER_TYPES = ChunkRenderTypeSet.of((RenderType[])new RenderType[]{RenderType.CUTOUT});
    private final TextureAtlasSprite[] textures;

    PaintSplotchesBakedModel(Function<Material, TextureAtlasSprite> bakedTextureGetter) {
        this.textures = new TextureAtlasSprite[]{bakedTextureGetter.apply(TEXTURE_PAINT1), bakedTextureGetter.apply(TEXTURE_PAINT2), bakedTextureGetter.apply(TEXTURE_PAINT3)};
    }

    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData, RenderType renderType) {
        if (side != null) {
            return Collections.emptyList();
        }
        PaintSplotches splotchesState = (PaintSplotches)extraData.get(PaintSplotchesBlockEntity.SPLOTCHES);
        if (splotchesState == null) {
            ArrayList<BakedQuad> quads = new ArrayList<BakedQuad>(1);
            CubeBuilder builder = new CubeBuilder(quads);
            builder.setTexture(this.textures[0]);
            builder.addCube(0.0f, 0.0f, 0.0f, 16.0f, 16.0f, 16.0f);
            return quads;
        }
        List<Splotch> splotches = splotchesState.getSplotches();
        CubeBuilder builder = new CubeBuilder();
        float offsetConstant = 0.001f;
        for (Splotch s : splotches) {
            if (s.isLumen()) {
                builder.setColorRGB(s.getColor().whiteVariant);
                builder.setEmissiveMaterial(true);
            } else {
                builder.setColorRGB(s.getColor().mediumVariant);
                builder.setEmissiveMaterial(false);
            }
            float offset = offsetConstant;
            offsetConstant += 0.001f;
            float buffer = 0.1f;
            float pos_x = s.x();
            float pos_y = s.y();
            pos_x = Math.max(0.1f, Math.min(0.9f, pos_x));
            pos_y = Math.max(0.1f, Math.min(0.9f, pos_y));
            TextureAtlasSprite ico = this.textures[s.getSeed() % this.textures.length];
            builder.setTexture(ico);
            builder.setCustomUv(s.getSide().getOpposite(), 0.0f, 0.0f, 1.0f, 1.0f);
            switch (s.getSide()) {
                case UP: {
                    offset = 1.0f - offset;
                    builder.addQuad(Direction.DOWN, pos_x - 0.1f, offset, pos_y - 0.1f, pos_x + 0.1f, offset, pos_y + 0.1f);
                    break;
                }
                case DOWN: {
                    builder.addQuad(Direction.UP, pos_x - 0.1f, offset, pos_y - 0.1f, pos_x + 0.1f, offset, pos_y + 0.1f);
                    break;
                }
                case EAST: {
                    offset = 1.0f - offset;
                    builder.addQuad(Direction.WEST, offset, pos_x - 0.1f, pos_y - 0.1f, offset, pos_x + 0.1f, pos_y + 0.1f);
                    break;
                }
                case WEST: {
                    builder.addQuad(Direction.EAST, offset, pos_x - 0.1f, pos_y - 0.1f, offset, pos_x + 0.1f, pos_y + 0.1f);
                    break;
                }
                case SOUTH: {
                    offset = 1.0f - offset;
                    builder.addQuad(Direction.NORTH, pos_x - 0.1f, pos_y - 0.1f, offset, pos_x + 0.1f, pos_y + 0.1f, offset);
                    break;
                }
                case NORTH: {
                    builder.addQuad(Direction.SOUTH, pos_x - 0.1f, pos_y - 0.1f, offset, pos_x + 0.1f, pos_y + 0.1f, offset);
                    break;
                }
            }
        }
        return builder.getOutput();
    }

    public boolean useAmbientOcclusion() {
        return false;
    }

    public boolean isGui3d() {
        return true;
    }

    public boolean isCustomRenderer() {
        return false;
    }

    public TextureAtlasSprite getParticleIcon() {
        return this.textures[0];
    }

    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    public boolean usesBlockLight() {
        return false;
    }

    static List<Material> getRequiredTextures() {
        return ImmutableList.of((Object)TEXTURE_PAINT1, (Object)TEXTURE_PAINT2, (Object)TEXTURE_PAINT3);
    }

    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        return RENDER_TYPES;
    }
}

