/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.texture.TextureAtlas
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 */
package appeng.thirdparty.fabric;

import appeng.thirdparty.fabric.QuadView;
import appeng.thirdparty.fabric.SpriteFinderImpl;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public interface SpriteFinder {
    public static SpriteFinder get(TextureAtlas atlas) {
        return SpriteFinderImpl.get(atlas);
    }

    public TextureAtlasSprite find(QuadView var1);

    public TextureAtlasSprite find(float var1, float var2);
}

