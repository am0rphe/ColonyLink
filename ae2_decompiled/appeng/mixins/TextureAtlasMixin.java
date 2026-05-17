/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.texture.SpriteLoader$Preparations
 *  net.minecraft.client.renderer.texture.TextureAtlas
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.resources.ResourceLocation
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package appeng.mixins;

import appeng.thirdparty.fabric.SpriteFinderImpl;
import java.util.Map;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={TextureAtlas.class})
public class TextureAtlasMixin
implements SpriteFinderImpl.SpriteFinderAccess {
    @Shadow
    private Map<ResourceLocation, TextureAtlasSprite> texturesByName;
    private SpriteFinderImpl fabric_spriteFinder = null;

    @Inject(at={@At(value="TAIL")}, method={"upload"})
    private void uploadHook(SpriteLoader.Preparations input, CallbackInfo info) {
        this.fabric_spriteFinder = null;
    }

    @Override
    public SpriteFinderImpl fabric_spriteFinder() {
        SpriteFinderImpl result = this.fabric_spriteFinder;
        if (result == null) {
            this.fabric_spriteFinder = result = new SpriteFinderImpl(this.texturesByName, (TextureAtlas)this);
        }
        return result;
    }
}

