/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.block.model.BakedQuad
 *  net.minecraft.client.renderer.texture.TextureAtlas
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.core.Direction
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 *  org.joml.Vector2f
 *  org.joml.Vector3f
 */
package appeng.thirdparty.fabric;

import appeng.thirdparty.fabric.MutableQuadView;
import appeng.thirdparty.fabric.SpriteFinder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3f;

public interface QuadView {
    public static final int VANILLA_VERTEX_STRIDE = 8;
    public static final int VANILLA_QUAD_STRIDE = 32;

    public float x(int var1);

    public float y(int var1);

    public float z(int var1);

    public float posByIndex(int var1, int var2);

    public Vector3f copyPos(int var1, @Nullable Vector3f var2);

    public int color(int var1);

    public float u(int var1);

    public float v(int var1);

    public boolean hasShade();

    public boolean hasAmbientOcclusion();

    default public Vector2f copyUv(int vertexIndex, @Nullable Vector2f target) {
        if (target == null) {
            target = new Vector2f();
        }
        target.set(this.u(vertexIndex), this.v(vertexIndex));
        return target;
    }

    public int lightmap(int var1);

    public boolean hasNormal(int var1);

    public float normalX(int var1);

    public float normalY(int var1);

    public float normalZ(int var1);

    @Nullable
    public Vector3f copyNormal(int var1, @Nullable Vector3f var2);

    @Nullable
    public Direction cullFace();

    @NotNull
    public Direction lightFace();

    public Direction nominalFace();

    public Vector3f faceNormal();

    public int colorIndex();

    public int tag();

    public void copyTo(MutableQuadView var1);

    public void toVanilla(int[] var1, int var2);

    default public BakedQuad toBakedQuad(TextureAtlasSprite sprite) {
        int[] vertexData = new int[32];
        this.toVanilla(vertexData, 0);
        return new BakedQuad(vertexData, this.colorIndex(), this.lightFace(), sprite, this.hasShade(), this.hasAmbientOcclusion());
    }

    default public BakedQuad toBlockBakedQuad() {
        SpriteFinder finder = SpriteFinder.get(Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS));
        return this.toBakedQuad(finder.find(this));
    }
}

