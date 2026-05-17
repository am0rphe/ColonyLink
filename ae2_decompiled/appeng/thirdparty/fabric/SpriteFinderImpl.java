/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.texture.MissingTextureAtlasSprite
 *  net.minecraft.client.renderer.texture.TextureAtlas
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.resources.ResourceLocation
 */
package appeng.thirdparty.fabric;

import appeng.thirdparty.fabric.QuadView;
import appeng.thirdparty.fabric.SpriteFinder;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

public class SpriteFinderImpl
implements SpriteFinder {
    private final Node root = new Node(0.5f, 0.5f, 0.25f);
    private final TextureAtlas spriteAtlasTexture;

    public SpriteFinderImpl(Map<ResourceLocation, TextureAtlasSprite> sprites, TextureAtlas spriteAtlasTexture) {
        this.spriteAtlasTexture = spriteAtlasTexture;
        sprites.values().forEach(this.root::add);
    }

    @Override
    public TextureAtlasSprite find(QuadView quad) {
        float u = 0.0f;
        float v = 0.0f;
        for (int i = 0; i < 4; ++i) {
            u += quad.u(i);
            v += quad.v(i);
        }
        return this.find(u * 0.25f, v * 0.25f);
    }

    @Override
    public TextureAtlasSprite find(float u, float v) {
        return this.root.find(u, v);
    }

    public static SpriteFinderImpl get(TextureAtlas atlas) {
        return ((SpriteFinderAccess)atlas).fabric_spriteFinder();
    }

    private class Node {
        final float midU;
        final float midV;
        final float cellRadius;
        Object lowLow = null;
        Object lowHigh = null;
        Object highLow = null;
        Object highHigh = null;
        static final float EPS = 1.0E-5f;

        Node(float midU, float midV, float radius) {
            this.midU = midU;
            this.midV = midV;
            this.cellRadius = radius;
        }

        void add(TextureAtlasSprite sprite) {
            boolean highV;
            boolean lowU = sprite.getU0() < this.midU - 1.0E-5f;
            boolean highU = sprite.getU1() > this.midU + 1.0E-5f;
            boolean lowV = sprite.getV0() < this.midV - 1.0E-5f;
            boolean bl = highV = sprite.getV1() > this.midV + 1.0E-5f;
            if (lowU && lowV) {
                this.addInner(sprite, this.lowLow, -1, -1, q -> {
                    this.lowLow = q;
                });
            }
            if (lowU && highV) {
                this.addInner(sprite, this.lowHigh, -1, 1, q -> {
                    this.lowHigh = q;
                });
            }
            if (highU && lowV) {
                this.addInner(sprite, this.highLow, 1, -1, q -> {
                    this.highLow = q;
                });
            }
            if (highU && highV) {
                this.addInner(sprite, this.highHigh, 1, 1, q -> {
                    this.highHigh = q;
                });
            }
        }

        private void addInner(TextureAtlasSprite sprite, Object quadrant, int uStep, int vStep, Consumer<Object> setter) {
            if (quadrant == null) {
                setter.accept(sprite);
            } else if (quadrant instanceof Node) {
                ((Node)quadrant).add(sprite);
            } else {
                Node n = new Node(this.midU + this.cellRadius * (float)uStep, this.midV + this.cellRadius * (float)vStep, this.cellRadius * 0.5f);
                if (quadrant instanceof TextureAtlasSprite) {
                    n.add((TextureAtlasSprite)quadrant);
                }
                n.add(sprite);
                setter.accept(n);
            }
        }

        private TextureAtlasSprite find(float u, float v) {
            if (u < this.midU) {
                return v < this.midV ? this.findInner(this.lowLow, u, v) : this.findInner(this.lowHigh, u, v);
            }
            return v < this.midV ? this.findInner(this.highLow, u, v) : this.findInner(this.highHigh, u, v);
        }

        private TextureAtlasSprite findInner(Object quadrant, float u, float v) {
            if (quadrant instanceof TextureAtlasSprite) {
                return (TextureAtlasSprite)quadrant;
            }
            if (quadrant instanceof Node) {
                return ((Node)quadrant).find(u, v);
            }
            return SpriteFinderImpl.this.spriteAtlasTexture.getSprite(MissingTextureAtlasSprite.getLocation());
        }
    }

    public static interface SpriteFinderAccess {
        public SpriteFinderImpl fabric_spriteFinder();
    }
}

