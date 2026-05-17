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
 *  net.minecraft.resources.ResourceLocation
 *  org.jetbrains.annotations.Nullable
 */
package appeng.client.render.model;

import appeng.client.render.BasicUnbakedModel;
import appeng.client.render.model.MeteoriteCompassBakedModel;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.function.Function;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class MeteoriteCompassModel
implements BasicUnbakedModel {
    private static final ResourceLocation MODEL_BASE = ResourceLocation.parse((String)"ae2:item/meteorite_compass_base");
    private static final ResourceLocation MODEL_POINTER = ResourceLocation.parse((String)"ae2:item/meteorite_compass_pointer");

    @Nullable
    public BakedModel bake(ModelBaker loader, Function<Material, TextureAtlasSprite> textureGetter, ModelState rotationContainer) {
        BakedModel baseModel = loader.bake(MODEL_BASE, rotationContainer);
        BakedModel pointerModel = loader.bake(MODEL_POINTER, rotationContainer);
        return new MeteoriteCompassBakedModel(baseModel, pointerModel);
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return ImmutableSet.of((Object)MODEL_BASE, (Object)MODEL_POINTER);
    }
}

