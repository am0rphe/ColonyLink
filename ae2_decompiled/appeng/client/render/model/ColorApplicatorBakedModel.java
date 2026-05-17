/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.renderer.block.model.BakedQuad
 *  net.minecraft.client.renderer.block.model.ItemOverrides
 *  net.minecraft.client.renderer.block.model.ItemTransforms
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.core.Direction
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.level.block.state.BlockState
 *  net.neoforged.neoforge.client.model.data.ModelData
 *  org.jetbrains.annotations.Nullable
 */
package appeng.client.render.model;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

class ColorApplicatorBakedModel
implements BakedModel {
    private final BakedModel baseModel;
    private final EnumMap<Direction, List<BakedQuad>> quadsBySide;
    private final List<BakedQuad> generalQuads;

    ColorApplicatorBakedModel(BakedModel baseModel, TextureAtlasSprite texDark, TextureAtlasSprite texMedium, TextureAtlasSprite texBright) {
        this.baseModel = baseModel;
        this.generalQuads = this.fixQuadTint(null, texDark, texMedium, texBright);
        this.quadsBySide = new EnumMap(Direction.class);
        for (Direction facing : Direction.values()) {
            this.quadsBySide.put(facing, this.fixQuadTint(facing, texDark, texMedium, texBright));
        }
    }

    private List<BakedQuad> fixQuadTint(Direction facing, TextureAtlasSprite texDark, TextureAtlasSprite texMedium, TextureAtlasSprite texBright) {
        List quads = this.baseModel.getQuads(null, facing, RandomSource.create((long)0L), ModelData.EMPTY, null);
        ArrayList<BakedQuad> result = new ArrayList<BakedQuad>(quads.size());
        for (BakedQuad quad : quads) {
            int tint;
            if (quad.getSprite() == texDark) {
                tint = 1;
            } else if (quad.getSprite() == texMedium) {
                tint = 2;
            } else if (quad.getSprite() == texBright) {
                tint = 3;
            } else {
                result.add(quad);
                continue;
            }
            BakedQuad newQuad = new BakedQuad(quad.getVertices(), tint, quad.getDirection(), quad.getSprite(), quad.isShade());
            result.add(newQuad);
        }
        return result;
    }

    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        if (side == null) {
            return this.generalQuads;
        }
        return this.quadsBySide.get(side);
    }

    public boolean useAmbientOcclusion() {
        return this.baseModel.useAmbientOcclusion();
    }

    public boolean isGui3d() {
        return this.baseModel.isGui3d();
    }

    public boolean usesBlockLight() {
        return false;
    }

    public boolean isCustomRenderer() {
        return this.baseModel.isCustomRenderer();
    }

    public TextureAtlasSprite getParticleIcon() {
        return this.baseModel.getParticleIcon();
    }

    public ItemTransforms getTransforms() {
        return this.baseModel.getTransforms();
    }

    public ItemOverrides getOverrides() {
        return this.baseModel.getOverrides();
    }

    public BakedModel applyTransform(ItemDisplayContext transformType, PoseStack poseStack, boolean applyLeftHandTransform) {
        this.baseModel.applyTransform(transformType, poseStack, applyLeftHandTransform);
        return this;
    }
}

