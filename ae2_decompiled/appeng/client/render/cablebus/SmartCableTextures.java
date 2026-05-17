/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.texture.TextureAtlas
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.client.resources.model.Material
 *  net.minecraft.resources.ResourceLocation
 */
package appeng.client.render.cablebus;

import appeng.core.AppEng;
import java.util.Arrays;
import java.util.function.Function;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;

public class SmartCableTextures {
    public static final Material[] SMART_CHANNELS_TEXTURES = (Material[])Arrays.stream(new ResourceLocation[]{AppEng.makeId("part/cable/smart/channels_00"), AppEng.makeId("part/cable/smart/channels_01"), AppEng.makeId("part/cable/smart/channels_02"), AppEng.makeId("part/cable/smart/channels_03"), AppEng.makeId("part/cable/smart/channels_04"), AppEng.makeId("part/cable/smart/channels_10"), AppEng.makeId("part/cable/smart/channels_11"), AppEng.makeId("part/cable/smart/channels_12"), AppEng.makeId("part/cable/smart/channels_13"), AppEng.makeId("part/cable/smart/channels_14")}).map(e -> new Material(TextureAtlas.LOCATION_BLOCKS, e)).toArray(Material[]::new);
    private final TextureAtlasSprite[] textures;
    public static final Material[] DENSE_SMART_CHANNELS_TEXTURES = (Material[])Arrays.stream(new ResourceLocation[]{AppEng.makeId("part/cable/dense_smart/channels_00"), AppEng.makeId("part/cable/dense_smart/channels_01"), AppEng.makeId("part/cable/dense_smart/channels_02"), AppEng.makeId("part/cable/dense_smart/channels_03"), AppEng.makeId("part/cable/dense_smart/channels_04"), AppEng.makeId("part/cable/dense_smart/channels_10"), AppEng.makeId("part/cable/dense_smart/channels_11"), AppEng.makeId("part/cable/dense_smart/channels_12"), AppEng.makeId("part/cable/dense_smart/channels_13"), AppEng.makeId("part/cable/dense_smart/channels_14")}).map(e -> new Material(TextureAtlas.LOCATION_BLOCKS, e)).toArray(Material[]::new);
    private final TextureAtlasSprite[] densetextures;

    public SmartCableTextures(Function<Material, TextureAtlasSprite> bakedTextureGetter) {
        this.textures = (TextureAtlasSprite[])Arrays.stream(SMART_CHANNELS_TEXTURES).map(bakedTextureGetter).toArray(TextureAtlasSprite[]::new);
        this.densetextures = (TextureAtlasSprite[])Arrays.stream(DENSE_SMART_CHANNELS_TEXTURES).map(bakedTextureGetter).toArray(TextureAtlasSprite[]::new);
    }

    public TextureAtlasSprite getOddTextureForChannels(int channels) {
        if (channels < 0) {
            return this.textures[0];
        }
        if (channels <= 4) {
            return this.textures[channels];
        }
        return this.textures[4];
    }

    public TextureAtlasSprite getOddTextureForDenseChannels(int channels) {
        if (channels < 0) {
            return this.densetextures[0];
        }
        if (channels <= 4) {
            return this.densetextures[channels];
        }
        return this.densetextures[4];
    }

    public TextureAtlasSprite getEvenTextureForChannels(int channels) {
        if (channels < 5) {
            return this.textures[5];
        }
        if (channels <= 8) {
            return this.textures[1 + channels];
        }
        return this.textures[9];
    }

    public TextureAtlasSprite getEvenTextureForDenseChannels(int channels) {
        if (channels < 5) {
            return this.densetextures[5];
        }
        if (channels <= 8) {
            return this.densetextures[1 + channels];
        }
        return this.densetextures[9];
    }
}

