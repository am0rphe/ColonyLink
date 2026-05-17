/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.client.resources.model.Material
 *  net.minecraft.client.resources.model.ModelBaker
 *  net.minecraft.client.resources.model.ModelState
 *  net.minecraft.client.resources.model.UnbakedModel
 *  net.minecraft.resources.ResourceLocation
 */
package appeng.client.render.cablebus;

import appeng.api.parts.PartModelsInternal;
import appeng.api.util.AEColor;
import appeng.client.render.BasicUnbakedModel;
import appeng.client.render.cablebus.CableBuilder;
import appeng.client.render.cablebus.CableBusBakedModel;
import appeng.client.render.cablebus.CableCoreType;
import appeng.client.render.cablebus.FacadeBuilder;
import appeng.core.AELog;
import appeng.core.AppEng;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;

public class CableBusModel
implements BasicUnbakedModel {
    public static final ResourceLocation TRANSLUCENT_FACADE_MODEL = AppEng.makeId("part/translucent_facade");

    @Override
    public Collection<ResourceLocation> getDependencies() {
        PartModelsInternal.freeze();
        ArrayList<ResourceLocation> models = new ArrayList<ResourceLocation>(PartModelsInternal.getModels());
        models.add(TRANSLUCENT_FACADE_MODEL);
        return models;
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> function) {
    }

    public BakedModel bake(ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState) {
        Map<ResourceLocation, BakedModel> partModels = this.loadPartModels(baker, spriteGetter, modelState);
        CableBuilder cableBuilder = new CableBuilder(spriteGetter);
        BakedModel translucentFacadeModel = baker.bake(TRANSLUCENT_FACADE_MODEL, modelState, spriteGetter);
        FacadeBuilder facadeBuilder = new FacadeBuilder(baker, translucentFacadeModel);
        TextureAtlasSprite particleTexture = cableBuilder.getCoreTexture(CableCoreType.GLASS, AEColor.TRANSPARENT);
        return new CableBusBakedModel(cableBuilder, facadeBuilder, partModels, particleTexture);
    }

    private Map<ResourceLocation, BakedModel> loadPartModels(ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetterIn, ModelState transformIn) {
        ImmutableMap.Builder result = ImmutableMap.builder();
        for (ResourceLocation location : PartModelsInternal.getModels()) {
            BakedModel bakedModel = baker.bake(location, transformIn, spriteGetterIn);
            if (bakedModel == null) {
                AELog.warn("Failed to bake part model {}", location);
                continue;
            }
            result.put((Object)location, (Object)bakedModel);
        }
        return result.build();
    }
}

