/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.renderer.RenderType
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
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package appeng.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.renderer.RenderType;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class DelegateBakedModel
implements BakedModel {
    private final BakedModel baseModel;

    protected DelegateBakedModel(BakedModel base) {
        this.baseModel = base;
    }

    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource rand) {
        return this.baseModel.getQuads(state, direction, rand);
    }

    @NotNull
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType) {
        return this.baseModel.getQuads(state, side, rand, data, renderType);
    }

    public boolean usesBlockLight() {
        return this.baseModel.usesBlockLight();
    }

    public ItemOverrides getOverrides() {
        return this.baseModel.getOverrides();
    }

    public BakedModel applyTransform(ItemDisplayContext transformType, PoseStack poseStack, boolean applyLeftHandTransform) {
        this.baseModel.applyTransform(transformType, poseStack, applyLeftHandTransform);
        return this;
    }

    public TextureAtlasSprite getParticleIcon() {
        return this.baseModel.getParticleIcon();
    }

    public ItemTransforms getTransforms() {
        return this.baseModel.getTransforms();
    }

    public boolean useAmbientOcclusion() {
        return this.baseModel.useAmbientOcclusion();
    }

    public boolean isGui3d() {
        return this.baseModel.isGui3d();
    }

    public boolean isCustomRenderer() {
        return this.baseModel.isCustomRenderer();
    }

    public BakedModel getBaseModel() {
        return this.baseModel;
    }
}

