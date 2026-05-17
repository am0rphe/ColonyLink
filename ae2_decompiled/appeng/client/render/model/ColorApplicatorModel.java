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
import appeng.client.render.model.ColorApplicatorBakedModel;
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

public class ColorApplicatorModel
implements BasicUnbakedModel {
    private static final ResourceLocation MODEL_BASE = AppEng.makeId("item/color_applicator_colored");
    private static final Material TEXTURE_DARK = new Material(TextureAtlas.LOCATION_BLOCKS, AppEng.makeId("item/color_applicator_tip_dark"));
    private static final Material TEXTURE_MEDIUM = new Material(TextureAtlas.LOCATION_BLOCKS, AppEng.makeId("item/color_applicator_tip_medium"));
    private static final Material TEXTURE_BRIGHT = new Material(TextureAtlas.LOCATION_BLOCKS, AppEng.makeId("item/color_applicator_tip_bright"));

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return Collections.singleton(MODEL_BASE);
    }

    @Nullable
    public BakedModel bake(ModelBaker bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform) {
        BakedModel baseModel = bakery.bake(MODEL_BASE, modelTransform);
        TextureAtlasSprite texDark = spriteGetter.apply(TEXTURE_DARK);
        TextureAtlasSprite texMedium = spriteGetter.apply(TEXTURE_MEDIUM);
        TextureAtlasSprite texBright = spriteGetter.apply(TEXTURE_BRIGHT);
        return new ColorApplicatorBakedModel(baseModel, texDark, texMedium, texBright);
    }
}

