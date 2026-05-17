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
 */
package appeng.parts.automation;

import appeng.client.render.BasicUnbakedModel;
import appeng.parts.automation.PlaneBakedModel;
import java.util.function.Function;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;

public class PlaneModel
implements BasicUnbakedModel {
    private final Material frontTexture;
    private final Material sidesTexture;
    private final Material backTexture;

    public PlaneModel(ResourceLocation frontTexture, ResourceLocation sidesTexture, ResourceLocation backTexture) {
        this.frontTexture = new Material(TextureAtlas.LOCATION_BLOCKS, frontTexture);
        this.sidesTexture = new Material(TextureAtlas.LOCATION_BLOCKS, sidesTexture);
        this.backTexture = new Material(TextureAtlas.LOCATION_BLOCKS, backTexture);
    }

    public BakedModel bake(ModelBaker bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform) {
        TextureAtlasSprite frontSprite = spriteGetter.apply(this.frontTexture);
        TextureAtlasSprite sidesSprite = spriteGetter.apply(this.sidesTexture);
        TextureAtlasSprite backSprite = spriteGetter.apply(this.backTexture);
        return new PlaneBakedModel(frontSprite, sidesSprite, backSprite);
    }
}

