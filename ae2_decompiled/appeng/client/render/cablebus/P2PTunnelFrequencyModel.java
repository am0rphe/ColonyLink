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
 *  org.jetbrains.annotations.Nullable
 */
package appeng.client.render.cablebus;

import appeng.client.render.BasicUnbakedModel;
import appeng.client.render.cablebus.P2PTunnelFrequencyBakedModel;
import appeng.core.AppEng;
import java.util.function.Function;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import org.jetbrains.annotations.Nullable;

public class P2PTunnelFrequencyModel
implements BasicUnbakedModel {
    private static final Material TEXTURE = new Material(TextureAtlas.LOCATION_BLOCKS, AppEng.makeId("part/p2p_tunnel_frequency"));

    @Nullable
    public BakedModel bake(ModelBaker baker, Function<Material, TextureAtlasSprite> textureGetter, ModelState modelState) {
        TextureAtlasSprite texture = textureGetter.apply(TEXTURE);
        return new P2PTunnelFrequencyBakedModel(texture);
    }
}

