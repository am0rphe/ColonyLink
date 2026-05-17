/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.cache.Cache
 *  com.google.common.cache.CacheBuilder
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.block.model.BakedQuad
 *  net.minecraft.client.renderer.block.model.ItemOverrides
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.core.Direction
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.level.block.state.BlockState
 *  net.neoforged.neoforge.client.model.IDynamicBakedModel
 *  net.neoforged.neoforge.client.model.data.ModelData
 */
package appeng.client.render.cablebus;

import appeng.api.util.AEColor;
import appeng.client.render.cablebus.CubeBuilder;
import appeng.client.render.cablebus.P2PTunnelFrequencyModelData;
import appeng.util.Platform;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;

public class P2PTunnelFrequencyBakedModel
implements IDynamicBakedModel {
    private final TextureAtlasSprite texture;
    private static final Cache<Long, List<BakedQuad>> modelCache = CacheBuilder.newBuilder().maximumSize(100L).build();
    private static final int[][] QUAD_OFFSETS = new int[][]{{3, 11, 2}, {11, 11, 2}, {3, 3, 2}, {11, 3, 2}};

    public P2PTunnelFrequencyBakedModel(TextureAtlasSprite texture) {
        this.texture = texture;
    }

    public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData modelData, RenderType renderType) {
        if (side != null || !modelData.has(P2PTunnelFrequencyModelData.FREQUENCY)) {
            return Collections.emptyList();
        }
        return this.getPartQuads((Long)modelData.get(P2PTunnelFrequencyModelData.FREQUENCY));
    }

    private List<BakedQuad> getQuadsForFrequency(short frequency, boolean active) {
        AEColor[] colors = Platform.p2p().toColors(frequency);
        CubeBuilder cb = new CubeBuilder();
        cb.setTexture(this.texture);
        cb.setEmissiveMaterial(active);
        for (int i = 0; i < 4; ++i) {
            int[] offs = QUAD_OFFSETS[i];
            for (int j = 0; j < 4; ++j) {
                AEColor col = colors[j];
                if (active) {
                    cb.setColorRGB(col.mediumVariant);
                } else {
                    float scale = 0.0011764707f;
                    cb.setColorRGB((float)(col.blackVariant >> 16 & 0xFF) * 0.0011764707f, (float)(col.blackVariant >> 8 & 0xFF) * 0.0011764707f, (float)(col.blackVariant & 0xFF) * 0.0011764707f);
                }
                int startx = j % 2;
                int starty = 1 - j / 2;
                cb.addCube(offs[0] + startx, offs[1] + starty, offs[2], offs[0] + startx + 1, offs[1] + starty + 1, offs[2] + 1);
            }
        }
        cb.setEmissiveMaterial(false);
        return cb.getOutput();
    }

    private List<BakedQuad> getPartQuads(long partFlags) {
        try {
            return (List)modelCache.get((Object)partFlags, () -> {
                short frequency = (short)(partFlags & 0xFFFFL);
                boolean active = (partFlags & 0x10000L) != 0L;
                return this.getQuadsForFrequency(frequency, active);
            });
        }
        catch (ExecutionException e) {
            return Collections.emptyList();
        }
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
        return true;
    }

    public TextureAtlasSprite getParticleIcon() {
        return this.texture;
    }

    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}

