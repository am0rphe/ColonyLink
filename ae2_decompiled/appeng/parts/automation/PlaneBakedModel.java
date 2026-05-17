/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.block.model.BakedQuad
 *  net.minecraft.client.renderer.block.model.ItemOverrides
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.core.Direction
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.level.block.state.BlockState
 *  net.neoforged.neoforge.client.model.IDynamicBakedModel
 *  net.neoforged.neoforge.client.model.data.ModelData
 *  org.jetbrains.annotations.Nullable
 */
package appeng.parts.automation;

import appeng.client.render.cablebus.CubeBuilder;
import appeng.parts.automation.PlaneConnections;
import appeng.parts.automation.PlaneModelData;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

public class PlaneBakedModel
implements IDynamicBakedModel {
    private static final PlaneConnections DEFAULT_PERMUTATION = PlaneConnections.of(false, false, false, false);
    private final TextureAtlasSprite frontTexture;
    private final Map<PlaneConnections, List<BakedQuad>> quads;

    PlaneBakedModel(TextureAtlasSprite frontTexture, TextureAtlasSprite sidesTexture, TextureAtlasSprite backTexture) {
        this.frontTexture = frontTexture;
        this.quads = new HashMap<PlaneConnections, List<BakedQuad>>(PlaneConnections.PERMUTATIONS.size());
        for (PlaneConnections permutation : PlaneConnections.PERMUTATIONS) {
            ArrayList<BakedQuad> quads = new ArrayList<BakedQuad>(24);
            CubeBuilder builder = new CubeBuilder(quads);
            builder.setTextures(sidesTexture, sidesTexture, frontTexture, backTexture, sidesTexture, sidesTexture);
            boolean minX = !permutation.isRight();
            int maxX = permutation.isLeft() ? 16 : 15;
            boolean minY = !permutation.isDown();
            int maxY = permutation.isUp() ? 16 : 15;
            builder.addCube((float)minX, (float)minY, 0.0f, maxX, maxY, 1.0f);
            this.quads.put(permutation, (List<BakedQuad>)ImmutableList.copyOf(quads));
        }
    }

    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData modelData, RenderType renderType) {
        if (side == null) {
            PlaneConnections connections = DEFAULT_PERMUTATION;
            if (modelData.has(PlaneModelData.CONNECTIONS)) {
                connections = (PlaneConnections)modelData.get(PlaneModelData.CONNECTIONS);
            }
            return this.quads.get(connections);
        }
        return Collections.emptyList();
    }

    public boolean useAmbientOcclusion() {
        return false;
    }

    public boolean isGui3d() {
        return false;
    }

    public boolean usesBlockLight() {
        return false;
    }

    public boolean isCustomRenderer() {
        return false;
    }

    public TextureAtlasSprite getParticleIcon() {
        return this.frontTexture;
    }

    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}

