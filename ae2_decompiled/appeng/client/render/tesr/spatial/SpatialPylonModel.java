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
 */
package appeng.client.render.tesr.spatial;

import appeng.client.render.BasicUnbakedModel;
import appeng.client.render.tesr.spatial.SpatialPylonBakedModel;
import appeng.client.render.tesr.spatial.SpatialPylonTextureType;
import appeng.core.AppEng;
import java.util.EnumMap;
import java.util.Locale;
import java.util.function.Function;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;

public class SpatialPylonModel
implements BasicUnbakedModel {
    public BakedModel bake(ModelBaker bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform) {
        EnumMap<SpatialPylonTextureType, TextureAtlasSprite> textures = new EnumMap<SpatialPylonTextureType, TextureAtlasSprite>(SpatialPylonTextureType.class);
        for (SpatialPylonTextureType type : SpatialPylonTextureType.values()) {
            textures.put(type, spriteGetter.apply(SpatialPylonModel.getTexturePath(type)));
        }
        return new SpatialPylonBakedModel(textures);
    }

    private static Material getTexturePath(SpatialPylonTextureType type) {
        return new Material(TextureAtlas.LOCATION_BLOCKS, AppEng.makeId("block/spatial_pylon/" + type.name().toLowerCase(Locale.ROOT)));
    }
}

