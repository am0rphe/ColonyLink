/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
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
package appeng.client.render.crafting;

import appeng.blockentity.crafting.CraftingCubeModelData;
import appeng.client.render.cablebus.CubeBuilder;
import appeng.util.Platform;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
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

abstract class CraftingCubeBakedModel
implements IDynamicBakedModel {
    private static final ChunkRenderTypeSet RENDER_TYPES = ChunkRenderTypeSet.of((RenderType[])new RenderType[]{RenderType.CUTOUT});
    private final TextureAtlasSprite ringCorner;
    private final TextureAtlasSprite ringHor;
    private final TextureAtlasSprite ringVer;

    CraftingCubeBakedModel(TextureAtlasSprite ringCorner, TextureAtlasSprite ringHor, TextureAtlasSprite ringVer) {
        this.ringCorner = ringCorner;
        this.ringHor = ringHor;
        this.ringVer = ringVer;
    }

    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData, RenderType renderType) {
        if (side == null) {
            return Collections.emptyList();
        }
        EnumSet<Direction> connections = CraftingCubeBakedModel.getConnections(extraData);
        ArrayList<BakedQuad> quads = new ArrayList<BakedQuad>();
        CubeBuilder builder = new CubeBuilder(quads);
        builder.setDrawFaces(EnumSet.of(side));
        this.addRing(builder, side, connections);
        float x2 = connections.contains(Direction.EAST) ? 16.0f : 13.01f;
        float x1 = connections.contains(Direction.WEST) ? 0.0f : 2.99f;
        float y2 = connections.contains(Direction.UP) ? 16.0f : 13.01f;
        float y1 = connections.contains(Direction.DOWN) ? 0.0f : 2.99f;
        float z2 = connections.contains(Direction.SOUTH) ? 16.0f : 13.01f;
        float z1 = connections.contains(Direction.NORTH) ? 0.0f : 2.99f;
        switch (side) {
            case DOWN: 
            case UP: {
                y1 = 0.0f;
                y2 = 16.0f;
                break;
            }
            case NORTH: 
            case SOUTH: {
                z1 = 0.0f;
                z2 = 16.0f;
                break;
            }
            case WEST: 
            case EAST: {
                x1 = 0.0f;
                x2 = 16.0f;
            }
        }
        this.addInnerCube(side, state, extraData, builder, x1, y1, z1, x2, y2, z2);
        return quads;
    }

    private void addRing(CubeBuilder builder, Direction side, EnumSet<Direction> connections) {
        builder.setTexture(this.ringCorner);
        this.addCornerCap(builder, connections, side, Direction.UP, Direction.EAST, Direction.NORTH);
        this.addCornerCap(builder, connections, side, Direction.UP, Direction.EAST, Direction.SOUTH);
        this.addCornerCap(builder, connections, side, Direction.UP, Direction.WEST, Direction.NORTH);
        this.addCornerCap(builder, connections, side, Direction.UP, Direction.WEST, Direction.SOUTH);
        this.addCornerCap(builder, connections, side, Direction.DOWN, Direction.EAST, Direction.NORTH);
        this.addCornerCap(builder, connections, side, Direction.DOWN, Direction.EAST, Direction.SOUTH);
        this.addCornerCap(builder, connections, side, Direction.DOWN, Direction.WEST, Direction.NORTH);
        this.addCornerCap(builder, connections, side, Direction.DOWN, Direction.WEST, Direction.SOUTH);
        for (Direction a : Direction.values()) {
            if (a == side || a == side.getOpposite()) continue;
            if (side.getAxis() != Direction.Axis.Y && (a == Direction.NORTH || a == Direction.EAST || a == Direction.WEST || a == Direction.SOUTH)) {
                builder.setTexture(this.ringVer);
            } else if (side.getAxis() == Direction.Axis.Y && (a == Direction.EAST || a == Direction.WEST)) {
                builder.setTexture(this.ringVer);
            } else {
                builder.setTexture(this.ringHor);
            }
            if (connections.contains(a)) continue;
            float x1 = 0.0f;
            float y1 = 0.0f;
            float z1 = 0.0f;
            float x2 = 16.0f;
            float y2 = 16.0f;
            float z2 = 16.0f;
            switch (a) {
                case DOWN: {
                    y1 = 0.0f;
                    y2 = 3.0f;
                    break;
                }
                case UP: {
                    y1 = 13.0f;
                    y2 = 16.0f;
                    break;
                }
                case WEST: {
                    x1 = 0.0f;
                    x2 = 3.0f;
                    break;
                }
                case EAST: {
                    x1 = 13.0f;
                    x2 = 16.0f;
                    break;
                }
                case NORTH: {
                    z1 = 0.0f;
                    z2 = 3.0f;
                    break;
                }
                case SOUTH: {
                    z1 = 13.0f;
                    z2 = 16.0f;
                }
            }
            Direction perpendicular = Platform.rotateAround(a, side);
            for (Direction cornerCandidate : EnumSet.of(perpendicular, perpendicular.getOpposite())) {
                if (connections.contains(cornerCandidate)) continue;
                switch (cornerCandidate) {
                    case DOWN: {
                        y1 = 3.0f;
                        break;
                    }
                    case UP: {
                        y2 = 13.0f;
                        break;
                    }
                    case NORTH: {
                        z1 = 3.0f;
                        break;
                    }
                    case SOUTH: {
                        z2 = 13.0f;
                        break;
                    }
                    case WEST: {
                        x1 = 3.0f;
                        break;
                    }
                    case EAST: {
                        x2 = 13.0f;
                    }
                }
            }
            builder.addCube(x1, y1, z1, x2, y2, z2);
        }
    }

    private void addCornerCap(CubeBuilder builder, EnumSet<Direction> connections, Direction side, Direction down, Direction west, Direction north) {
        if (connections.contains(down) || connections.contains(west) || connections.contains(north)) {
            return;
        }
        if (side != down && side != west && side != north) {
            return;
        }
        float x1 = west == Direction.WEST ? 0.0f : 13.0f;
        float y1 = down == Direction.DOWN ? 0.0f : 13.0f;
        float z1 = north == Direction.NORTH ? 0.0f : 13.0f;
        float x2 = west == Direction.WEST ? 3.0f : 16.0f;
        float y2 = down == Direction.DOWN ? 3.0f : 16.0f;
        float z2 = north == Direction.NORTH ? 3.0f : 16.0f;
        builder.addCube(x1, y1, z1, x2, y2, z2);
    }

    private static EnumSet<Direction> getConnections(ModelData modelData) {
        if (modelData.has(CraftingCubeModelData.CONNECTIONS)) {
            return (EnumSet)modelData.get(CraftingCubeModelData.CONNECTIONS);
        }
        return EnumSet.noneOf(Direction.class);
    }

    protected abstract void addInnerCube(Direction var1, BlockState var2, ModelData var3, CubeBuilder var4, float var5, float var6, float var7, float var8, float var9, float var10);

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
        return this.ringCorner;
    }

    public boolean usesBlockLight() {
        return false;
    }

    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        return RENDER_TYPES;
    }
}

