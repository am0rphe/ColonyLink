/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.client.resources.model.Material
 *  net.minecraft.client.resources.model.ModelBaker
 *  net.minecraft.client.resources.model.ModelState
 *  net.minecraft.client.resources.model.UnbakedModel
 *  net.minecraft.resources.ResourceLocation
 *  org.jetbrains.annotations.Nullable
 */
package appeng.block.qnb;

import appeng.block.qnb.QnbFormedBakedModel;
import appeng.client.render.BasicUnbakedModel;
import appeng.core.AppEng;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.function.Function;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class QnbFormedModel
implements BasicUnbakedModel {
    private static final ResourceLocation MODEL_RING = AppEng.makeId("block/qnb/ring");

    @Nullable
    public BakedModel bake(ModelBaker modelBaker, Function<Material, TextureAtlasSprite> textureGetter, ModelState modelState) {
        BakedModel ringModel = modelBaker.bake(MODEL_RING, modelState);
        return new QnbFormedBakedModel(ringModel, textureGetter);
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return ImmutableSet.of((Object)MODEL_RING);
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> function) {
    }
}

