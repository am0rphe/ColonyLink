/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.block.model.BakedQuad
 *  net.minecraft.client.renderer.block.model.ItemOverrides
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Direction$Axis
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.level.block.state.BlockState
 *  net.neoforged.neoforge.client.ChunkRenderTypeSet
 *  net.neoforged.neoforge.client.model.IDynamicBakedModel
 *  net.neoforged.neoforge.client.model.data.ModelData
 *  org.jetbrains.annotations.Nullable
 */
package appeng.client.render.tesr.spatial;

import appeng.blockentity.spatial.SpatialPylonBlockEntity;
import appeng.client.render.cablebus.CubeBuilder;
import appeng.client.render.tesr.spatial.SpatialPylonTextureType;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

class SpatialPylonBakedModel
implements IDynamicBakedModel {
    private static final ChunkRenderTypeSet RENDER_TYPES = ChunkRenderTypeSet.of((RenderType[])new RenderType[]{RenderType.CUTOUT});
    private final Map<SpatialPylonTextureType, TextureAtlasSprite> textures;

    SpatialPylonBakedModel(Map<SpatialPylonTextureType, TextureAtlasSprite> textures) {
        this.textures = ImmutableMap.copyOf(textures);
    }

    public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction side, RandomSource rand, ModelData extraData, RenderType renderType) {
        SpatialPylonBlockEntity.ClientState state = this.getState(extraData);
        CubeBuilder builder = new CubeBuilder();
        if (state.axisPosition() != SpatialPylonBlockEntity.AxisPosition.NONE) {
            Direction ori = null;
            Direction.Axis displayAxis = state.axis();
            SpatialPylonBlockEntity.AxisPosition axisPos = state.axisPosition();
            if (displayAxis == Direction.Axis.Y) {
                ori = Direction.UP;
                if (axisPos == SpatialPylonBlockEntity.AxisPosition.END) {
                    builder.setFlipV(Direction.NORTH, true);
                    builder.setFlipV(Direction.SOUTH, true);
                    builder.setFlipV(Direction.WEST, true);
                    builder.setFlipV(Direction.EAST, true);
                }
            } else if (displayAxis == Direction.Axis.X) {
                ori = Direction.EAST;
                builder.setUvRotation(Direction.NORTH, 1);
                builder.setUvRotation(Direction.SOUTH, 1);
                builder.setUvRotation(Direction.UP, 3);
                builder.setUvRotation(Direction.DOWN, 3);
                if (axisPos == SpatialPylonBlockEntity.AxisPosition.START) {
                    builder.setFlipV(Direction.UP, true);
                    builder.setFlipV(Direction.DOWN, true);
                    builder.setFlipV(Direction.NORTH, true);
                } else if (axisPos == SpatialPylonBlockEntity.AxisPosition.END) {
                    builder.setFlipV(Direction.SOUTH, true);
                }
            } else if (displayAxis == Direction.Axis.Z) {
                ori = Direction.NORTH;
                builder.setUvRotation(Direction.WEST, 1);
                builder.setUvRotation(Direction.EAST, 1);
                if (axisPos == SpatialPylonBlockEntity.AxisPosition.START) {
                    builder.setFlipV(Direction.UP, true);
                    builder.setFlipV(Direction.EAST, true);
                } else if (axisPos == SpatialPylonBlockEntity.AxisPosition.END) {
                    builder.setFlipV(Direction.DOWN, true);
                    builder.setFlipV(Direction.WEST, true);
                }
            }
            builder.setTextures(this.textures.get((Object)SpatialPylonBakedModel.getTextureTypeFromSideOutside(state, ori, Direction.UP)), this.textures.get((Object)SpatialPylonBakedModel.getTextureTypeFromSideOutside(state, ori, Direction.DOWN)), this.textures.get((Object)SpatialPylonBakedModel.getTextureTypeFromSideOutside(state, ori, Direction.NORTH)), this.textures.get((Object)SpatialPylonBakedModel.getTextureTypeFromSideOutside(state, ori, Direction.SOUTH)), this.textures.get((Object)SpatialPylonBakedModel.getTextureTypeFromSideOutside(state, ori, Direction.EAST)), this.textures.get((Object)SpatialPylonBakedModel.getTextureTypeFromSideOutside(state, ori, Direction.WEST)));
            builder.addCube(0.0f, 0.0f, 0.0f, 16.0f, 16.0f, 16.0f);
            if (state.powered()) {
                builder.setEmissiveMaterial(true);
            }
            builder.setTextures(this.textures.get((Object)SpatialPylonBakedModel.getTextureTypeFromSideInside(state, ori, Direction.UP)), this.textures.get((Object)SpatialPylonBakedModel.getTextureTypeFromSideInside(state, ori, Direction.DOWN)), this.textures.get((Object)SpatialPylonBakedModel.getTextureTypeFromSideInside(state, ori, Direction.NORTH)), this.textures.get((Object)SpatialPylonBakedModel.getTextureTypeFromSideInside(state, ori, Direction.SOUTH)), this.textures.get((Object)SpatialPylonBakedModel.getTextureTypeFromSideInside(state, ori, Direction.EAST)), this.textures.get((Object)SpatialPylonBakedModel.getTextureTypeFromSideInside(state, ori, Direction.WEST)));
        } else {
            builder.setTexture(this.textures.get((Object)SpatialPylonTextureType.BASE));
            builder.addCube(0.0f, 0.0f, 0.0f, 16.0f, 16.0f, 16.0f);
            builder.setTexture(this.textures.get((Object)SpatialPylonTextureType.DIM));
        }
        builder.addCube(0.0f, 0.0f, 0.0f, 16.0f, 16.0f, 16.0f);
        builder.setEmissiveMaterial(false);
        return builder.getOutput();
    }

    private SpatialPylonBlockEntity.ClientState getState(ModelData modelData) {
        SpatialPylonBlockEntity.ClientState state = (SpatialPylonBlockEntity.ClientState)modelData.get(SpatialPylonBlockEntity.STATE);
        return state != null ? state : SpatialPylonBlockEntity.ClientState.DEFAULT;
    }

    private static SpatialPylonTextureType getTextureTypeFromSideOutside(SpatialPylonBlockEntity.ClientState state, Direction ori, Direction dir) {
        if (ori == dir || ori.getOpposite() == dir) {
            return SpatialPylonTextureType.BASE;
        }
        if (state.axisPosition() == SpatialPylonBlockEntity.AxisPosition.MIDDLE) {
            return SpatialPylonTextureType.BASE_SPANNED;
        }
        if (state.axisPosition() == SpatialPylonBlockEntity.AxisPosition.START || state.axisPosition() == SpatialPylonBlockEntity.AxisPosition.END) {
            return SpatialPylonTextureType.BASE_END;
        }
        return SpatialPylonTextureType.BASE;
    }

    private static SpatialPylonTextureType getTextureTypeFromSideInside(SpatialPylonBlockEntity.ClientState state, Direction ori, Direction dir) {
        boolean good = state.online();
        if (ori == dir || ori.getOpposite() == dir) {
            return good ? SpatialPylonTextureType.DIM : SpatialPylonTextureType.RED;
        }
        if (state.axisPosition() == SpatialPylonBlockEntity.AxisPosition.MIDDLE) {
            return good ? SpatialPylonTextureType.DIM_SPANNED : SpatialPylonTextureType.RED_SPANNED;
        }
        if (state.axisPosition() == SpatialPylonBlockEntity.AxisPosition.START || state.axisPosition() == SpatialPylonBlockEntity.AxisPosition.END) {
            return good ? SpatialPylonTextureType.DIM_END : SpatialPylonTextureType.RED_END;
        }
        return SpatialPylonTextureType.BASE;
    }

    public boolean usesBlockLight() {
        return false;
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
        return this.textures.get((Object)SpatialPylonTextureType.DIM);
    }

    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        return RENDER_TYPES;
    }
}

