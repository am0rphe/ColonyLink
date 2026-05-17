/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.client.resources.model.Material
 *  net.minecraft.client.resources.model.ModelBaker
 *  net.minecraft.client.resources.model.ModelState
 *  net.minecraft.resources.ResourceLocation
 */
package appeng.client.render;

import appeng.client.render.BasicUnbakedModel;
import appeng.client.render.FacadeDispatcherBakedModel;
import appeng.client.render.cablebus.FacadeBuilder;
import appeng.core.AppEng;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;

public class FacadeItemModel
implements BasicUnbakedModel {
    private static final ResourceLocation MODEL_BASE = AppEng.makeId("item/facade_base");

    public BakedModel bake(ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform) {
        BakedModel bakedBaseModel = baker.bake(MODEL_BASE, modelTransform);
        FacadeBuilder facadeBuilder = new FacadeBuilder(baker, null);
        return new FacadeDispatcherBakedModel(bakedBaseModel, facadeBuilder);
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return Collections.singleton(MODEL_BASE);
    }
}

