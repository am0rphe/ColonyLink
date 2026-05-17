/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.texture.TextureAtlas
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.client.resources.model.Material
 *  net.minecraft.client.resources.model.ModelBaker
 *  net.minecraft.client.resources.model.ModelState
 *  net.minecraft.resources.ResourceLocation
 *  org.jetbrains.annotations.Nullable
 */
package appeng.client.render.model;

import appeng.client.render.BasicUnbakedModel;
import appeng.client.render.model.MemoryCardBakedModel;
import appeng.core.AppEng;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class MemoryCardModel
implements BasicUnbakedModel {
    public static final ResourceLocation MODEL_BASE = AppEng.makeId("item/memory_card_base");
    private static final Material TEXTURE = new Material(TextureAtlas.LOCATION_BLOCKS, AppEng.makeId("item/memory_card_hash"));

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return Collections.singleton(MODEL_BASE);
    }

    @Nullable
    public BakedModel bake(ModelBaker loader, Function<Material, TextureAtlasSprite> textureGetter, ModelState rotationContainer) {
        TextureAtlasSprite texture = textureGetter.apply(TEXTURE);
        BakedModel baseModel = loader.bake(MODEL_BASE, rotationContainer);
        return new MemoryCardBakedModel(baseModel, texture);
    }
}

