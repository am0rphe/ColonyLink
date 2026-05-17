/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.BufferBuilder
 *  com.mojang.blaze3d.vertex.BufferUploader
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.MeshData
 *  com.mojang.blaze3d.vertex.Tesselator
 *  com.mojang.blaze3d.vertex.VertexFormat$Mode
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.GuiSpriteManager
 *  net.minecraft.client.renderer.GameRenderer
 *  net.minecraft.client.renderer.Rect2i
 *  net.minecraft.client.renderer.texture.TextureAtlas
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.FastColor$ARGB32
 *  net.minecraft.util.Mth
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 *  org.joml.Matrix4f
 */
package appeng.client.gui.style;

import appeng.client.gui.style.TextureTransform;
import appeng.core.AppEng;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.GuiSpriteManager;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(value=Dist.CLIENT)
public final class Blitter {
    public static final int DEFAULT_TEXTURE_WIDTH = 256;
    public static final int DEFAULT_TEXTURE_HEIGHT = 256;
    private final ResourceLocation texture;
    private final int referenceWidth;
    private final int referenceHeight;
    private int r = 255;
    private int g = 255;
    private int b = 255;
    private int a = 255;
    private Rect2i srcRect;
    private Rect2i destRect = new Rect2i(0, 0, 0, 0);
    private boolean blending = true;
    private TextureTransform transform = TextureTransform.NONE;
    private int zOffset;

    Blitter(ResourceLocation texture, int referenceWidth, int referenceHeight) {
        this.texture = texture;
        this.referenceWidth = referenceWidth;
        this.referenceHeight = referenceHeight;
    }

    public static Blitter texture(ResourceLocation file) {
        return Blitter.texture(file, 256, 256);
    }

    public static Blitter texture(String file) {
        return Blitter.texture(file, 256, 256);
    }

    public static Blitter texture(ResourceLocation file, int referenceWidth, int referenceHeight) {
        return new Blitter(file, referenceWidth, referenceHeight);
    }

    public static Blitter texture(String file, int referenceWidth, int referenceHeight) {
        return new Blitter(AppEng.makeId("textures/" + file), referenceWidth, referenceHeight);
    }

    public static Blitter sprite(TextureAtlasSprite sprite) {
        TextureAtlas atlas = (TextureAtlas)Minecraft.getInstance().getTextureManager().getTexture(sprite.atlasLocation());
        return new Blitter(sprite.atlasLocation(), atlas.getWidth(), atlas.getHeight()).src(sprite.getX(), sprite.getY(), sprite.contents().width(), sprite.contents().height());
    }

    public static Blitter guiSprite(ResourceLocation resourceLocation) {
        GuiSpriteManager sprites = Minecraft.getInstance().getGuiSprites();
        TextureAtlasSprite sprite = sprites.getSprite(resourceLocation);
        return Blitter.sprite(sprite);
    }

    public Blitter copy() {
        Blitter result = new Blitter(this.texture, this.referenceWidth, this.referenceHeight);
        result.srcRect = this.srcRect;
        result.destRect = this.destRect;
        result.r = this.r;
        result.g = this.g;
        result.b = this.b;
        result.a = this.a;
        return result;
    }

    public int getSrcX() {
        return this.srcRect == null ? 0 : this.srcRect.getX();
    }

    public int getSrcY() {
        return this.srcRect == null ? 0 : this.srcRect.getY();
    }

    public int getSrcWidth() {
        return this.srcRect == null ? this.destRect.getWidth() : this.srcRect.getWidth();
    }

    public int getSrcHeight() {
        return this.srcRect == null ? this.destRect.getHeight() : this.srcRect.getHeight();
    }

    public Blitter src(int x, int y, int w, int h) {
        this.srcRect = new Rect2i(x, y, w, h);
        return this;
    }

    public Blitter srcWidth(int w) {
        this.srcRect = new Rect2i(this.srcRect.getX(), this.srcRect.getY(), w, this.srcRect.getHeight());
        return this;
    }

    public Blitter srcHeight(int h) {
        this.srcRect = new Rect2i(this.srcRect.getX(), this.srcRect.getY(), this.srcRect.getWidth(), h);
        return this;
    }

    public Blitter src(Rect2i rect) {
        return this.src(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }

    public Blitter dest(int x, int y, int w, int h) {
        this.destRect = new Rect2i(x, y, w, h);
        return this;
    }

    public Blitter dest(int x, int y) {
        return this.dest(x, y, 0, 0);
    }

    public Blitter dest(Rect2i rect) {
        return this.dest(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }

    public Rect2i getDestRect() {
        int x = this.destRect.getX();
        int y = this.destRect.getY();
        int w = 0;
        int h = 0;
        if (this.destRect.getWidth() != 0 && this.destRect.getHeight() != 0) {
            w = this.destRect.getWidth();
            h = this.destRect.getHeight();
        } else if (this.srcRect != null) {
            w = this.srcRect.getWidth();
            h = this.srcRect.getHeight();
        }
        return new Rect2i(x, y, w, h);
    }

    public Blitter color(float r, float g, float b) {
        this.r = (int)(Mth.clamp((float)r, (float)0.0f, (float)1.0f) * 255.0f);
        this.g = (int)(Mth.clamp((float)g, (float)0.0f, (float)1.0f) * 255.0f);
        this.b = (int)(Mth.clamp((float)b, (float)0.0f, (float)1.0f) * 255.0f);
        return this;
    }

    public Blitter colorArgb(int packedArgb) {
        this.a = FastColor.ARGB32.alpha((int)packedArgb);
        this.r = FastColor.ARGB32.red((int)packedArgb);
        this.g = FastColor.ARGB32.green((int)packedArgb);
        this.b = FastColor.ARGB32.blue((int)packedArgb);
        return this;
    }

    public Blitter opacity(float a) {
        this.a = (int)(Mth.clamp((float)a, (float)0.0f, (float)1.0f) * 255.0f);
        return this;
    }

    public Blitter color(float r, float g, float b, float a) {
        return this.color(r, g, b).opacity(a);
    }

    public Blitter transform(TextureTransform transform) {
        this.transform = Objects.requireNonNull(transform);
        return this;
    }

    public Blitter blending(boolean enable) {
        this.blending = enable;
        return this;
    }

    public Blitter colorRgb(int packedRgb) {
        float r = (float)(packedRgb >> 16 & 0xFF) / 255.0f;
        float g = (float)(packedRgb >> 8 & 0xFF) / 255.0f;
        float b = (float)(packedRgb & 0xFF) / 255.0f;
        return this.color(r, g, b);
    }

    public Blitter zOffset(int offset) {
        this.zOffset = offset;
        return this;
    }

    public void blit(GuiGraphics guiGraphics) {
        float maxU;
        float maxV;
        float minU;
        float minV;
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture((int)0, (ResourceLocation)this.texture);
        if (this.srcRect == null) {
            minV = 0.0f;
            minU = 0.0f;
            maxV = 1.0f;
            maxU = 1.0f;
        } else {
            minU = (float)this.srcRect.getX() / (float)this.referenceWidth;
            minV = (float)this.srcRect.getY() / (float)this.referenceHeight;
            maxU = (float)(this.srcRect.getX() + this.srcRect.getWidth()) / (float)this.referenceWidth;
            maxV = (float)(this.srcRect.getY() + this.srcRect.getHeight()) / (float)this.referenceHeight;
        }
        switch (this.transform) {
            case MIRROR_H: {
                float tmp = minU;
                minU = maxU;
                maxU = tmp;
                break;
            }
            case MIRROR_V: {
                float tmp = minV;
                minV = maxV;
                maxV = tmp;
            }
        }
        float x1 = this.destRect.getX();
        float y1 = this.destRect.getY();
        float x2 = x1;
        float y2 = y1;
        if (this.destRect.getWidth() != 0 && this.destRect.getHeight() != 0) {
            x2 += (float)this.destRect.getWidth();
            y2 += (float)this.destRect.getHeight();
        } else if (this.srcRect != null) {
            x2 += (float)this.srcRect.getWidth();
            y2 += (float)this.srcRect.getHeight();
        }
        Matrix4f matrix = guiGraphics.pose().last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.addVertex(matrix, x1, y2, (float)this.zOffset).setUv(minU, maxV).setColor(this.r, this.g, this.b, this.a);
        bufferbuilder.addVertex(matrix, x2, y2, (float)this.zOffset).setUv(maxU, maxV).setColor(this.r, this.g, this.b, this.a);
        bufferbuilder.addVertex(matrix, x2, y1, (float)this.zOffset).setUv(maxU, minV).setColor(this.r, this.g, this.b, this.a);
        bufferbuilder.addVertex(matrix, x1, y1, (float)this.zOffset).setUv(minU, minV).setColor(this.r, this.g, this.b, this.a);
        if (this.blending) {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc((int)770, (int)771);
        } else {
            RenderSystem.disableBlend();
        }
        BufferUploader.drawWithShader((MeshData)bufferbuilder.buildOrThrow());
    }
}

