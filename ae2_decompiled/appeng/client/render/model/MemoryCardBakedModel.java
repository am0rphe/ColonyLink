/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.cache.Cache
 *  com.google.common.cache.CacheBuilder
 *  com.google.common.collect.ImmutableList
 *  com.mojang.blaze3d.vertex.PoseStack
 *  javax.annotation.Nullable
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.block.model.BakedQuad
 *  net.minecraft.client.renderer.block.model.ItemOverrides
 *  net.minecraft.client.renderer.block.model.ItemTransforms
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.core.Direction
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.state.BlockState
 *  net.neoforged.neoforge.client.model.data.ModelData
 */
package appeng.client.render.model;

import appeng.api.ids.AEComponents;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardColors;
import appeng.api.util.AEColor;
import appeng.client.render.cablebus.CubeBuilder;
import appeng.core.AELog;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

class MemoryCardBakedModel
implements BakedModel {
    private final BakedModel baseModel;
    private final TextureAtlasSprite texture;
    private final MemoryCardColors colors;
    private final Cache<MemoryCardColors, MemoryCardBakedModel> modelCache;
    private final ImmutableList<BakedQuad> generalQuads;

    MemoryCardBakedModel(BakedModel baseModel, TextureAtlasSprite texture) {
        this(baseModel, texture, MemoryCardColors.DEFAULT, MemoryCardBakedModel.createCache());
    }

    private MemoryCardBakedModel(BakedModel baseModel, TextureAtlasSprite texture, MemoryCardColors colors, Cache<MemoryCardColors, MemoryCardBakedModel> modelCache) {
        this.baseModel = baseModel;
        this.texture = texture;
        this.colors = colors;
        this.generalQuads = ImmutableList.copyOf(this.buildGeneralQuads());
        this.modelCache = modelCache;
    }

    private static Cache<MemoryCardColors, MemoryCardBakedModel> createCache() {
        return CacheBuilder.newBuilder().maximumSize(100L).build();
    }

    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        List quads = this.baseModel.getQuads(state, side, rand, ModelData.EMPTY, null);
        if (side != null) {
            return quads;
        }
        ArrayList<BakedQuad> result = new ArrayList<BakedQuad>(quads.size() + this.generalQuads.size());
        result.addAll(quads);
        result.addAll((Collection<BakedQuad>)this.generalQuads);
        return result;
    }

    private List<BakedQuad> buildGeneralQuads() {
        CubeBuilder builder = new CubeBuilder();
        builder.setTexture(this.texture);
        for (int x = 0; x < 4; ++x) {
            for (int y = 0; y < 2; ++y) {
                AEColor color = this.colors.get(x, y);
                builder.setColorRGB(color.mediumVariant);
                builder.addCube(8 + x, 9 - y, 7.5f, 8 + x + 1, 9 - y + 1, 8.5f);
            }
        }
        return builder.getOutput();
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
        return new ItemOverrides(){

            public BakedModel resolve(BakedModel originalModel, ItemStack stack, ClientLevel level, LivingEntity entity, int seed) {
                try {
                    Item item = stack.getItem();
                    if (item instanceof IMemoryCard) {
                        IMemoryCard memoryCard = (IMemoryCard)item;
                        MemoryCardColors colors = (MemoryCardColors)stack.getOrDefault(AEComponents.MEMORY_CARD_COLORS, (Object)MemoryCardColors.DEFAULT);
                        return (BakedModel)MemoryCardBakedModel.this.modelCache.get((Object)colors, () -> new MemoryCardBakedModel(MemoryCardBakedModel.this.baseModel, MemoryCardBakedModel.this.texture, colors, MemoryCardBakedModel.this.modelCache));
                    }
                }
                catch (ExecutionException e) {
                    AELog.error(e);
                }
                return MemoryCardBakedModel.this;
            }
        };
    }

    public BakedModel applyTransform(ItemDisplayContext transformType, PoseStack poseStack, boolean applyLeftHandTransform) {
        this.baseModel.applyTransform(transformType, poseStack, applyLeftHandTransform);
        return this;
    }
}

