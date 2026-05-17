/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  net.minecraft.client.renderer.LightTexture
 *  net.minecraft.client.renderer.block.model.BakedQuad
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Direction$Axis
 *  org.joml.Vector4f
 */
package appeng.client.render.cablebus;

import appeng.thirdparty.fabric.EncodingFormat;
import appeng.thirdparty.fabric.MutableQuadViewImpl;
import appeng.thirdparty.fabric.QuadEmitter;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import org.joml.Vector4f;

public class CubeBuilder {
    private final List<BakedQuad> output;
    private final EnumMap<Direction, TextureAtlasSprite> textures = new EnumMap(Direction.class);
    private EnumSet<Direction> drawFaces = EnumSet.allOf(Direction.class);
    private final EnumMap<Direction, Vector4f> customUv = new EnumMap(Direction.class);
    private byte[] uvRotations = new byte[Direction.values().length];
    private final boolean[] flipU = new boolean[Direction.values().length];
    private final boolean[] flipV = new boolean[Direction.values().length];
    private int color = -1;
    private boolean emissiveMaterial;

    public CubeBuilder(List<BakedQuad> output) {
        this.output = output;
    }

    public CubeBuilder() {
        this(new ArrayList<BakedQuad>(6));
    }

    public void addCube(float x1, float y1, float z1, float x2, float y2, float z2) {
        x1 /= 16.0f;
        y1 /= 16.0f;
        z1 /= 16.0f;
        x2 /= 16.0f;
        y2 /= 16.0f;
        z2 /= 16.0f;
        for (Direction face : this.drawFaces) {
            this.putFace(face, x1, y1, z1, x2, y2, z2);
        }
    }

    public void addQuad(Direction face, float x1, float y1, float z1, float x2, float y2, float z2) {
        this.putFace(face, x1, y1, z1, x2, y2, z2);
    }

    public void setFlipU(Direction side, boolean enable) {
        this.flipU[side.ordinal()] = enable;
    }

    public void setFlipV(Direction side, boolean enable) {
        this.flipV[side.ordinal()] = enable;
    }

    private void putFace(Direction face, float x1, float y1, float z1, float x2, float y2, float z2) {
        final TextureAtlasSprite texture = this.textures.get(face);
        MutableQuadViewImpl emitter = new MutableQuadViewImpl(){
            {
                this.begin(new int[EncodingFormat.TOTAL_STRIDE], 0);
            }

            @Override
            public QuadEmitter emit() {
                CubeBuilder.this.output.add(this.toBakedQuad(texture));
                return this;
            }
        };
        emitter.colorIndex(-1);
        UvVector uv = new UvVector();
        Vector4f customUv = this.customUv.get(face);
        if (customUv != null) {
            uv.u1 = texture.getU(customUv.x());
            uv.v1 = texture.getV(customUv.y());
            uv.u2 = texture.getU(customUv.z());
            uv.v2 = texture.getV(customUv.w());
        } else {
            uv = this.getStandardUv(face, texture, x1, y1, z1, x2, y2, z2);
        }
        emitter.color(this.color, this.color, this.color, this.color);
        emitter.normal(0, face.getStepX(), face.getStepY(), face.getStepZ());
        emitter.normal(1, face.getStepX(), face.getStepY(), face.getStepZ());
        emitter.normal(2, face.getStepX(), face.getStepY(), face.getStepZ());
        emitter.normal(3, face.getStepX(), face.getStepY(), face.getStepZ());
        this.setFaceUV(face, emitter, uv);
        switch (face) {
            case DOWN: {
                emitter.square(face, x1, z1, x2, z2, y1);
                break;
            }
            case UP: {
                emitter.square(face, x1, 1.0f - z2, x2, 1.0f - z1, 1.0f - y2);
                break;
            }
            case NORTH: {
                emitter.square(face, 1.0f - x2, y1, 1.0f - x1, y2, z1);
                break;
            }
            case SOUTH: {
                emitter.square(face, x1, y1, x2, y2, 1.0f - z2);
                break;
            }
            case WEST: {
                emitter.square(face, z1, y1, z2, y2, x1);
                break;
            }
            case EAST: {
                emitter.square(face, 1.0f - z2, y1, 1.0f - z1, y2, 1.0f - x2);
            }
        }
        if (this.emissiveMaterial) {
            int lightmap = LightTexture.pack((int)15, (int)15);
            emitter.lightmap(lightmap, lightmap, lightmap, lightmap);
        }
        emitter.emit();
    }

    private void setFaceUV(Direction face, QuadEmitter emitter, UvVector uv) {
        float tmp;
        byte rotation = this.uvRotations[face.ordinal()];
        if (this.flipU[face.ordinal()]) {
            tmp = uv.u1;
            uv.u1 = uv.u2;
            uv.u2 = tmp;
        }
        if (this.flipV[face.ordinal()]) {
            tmp = uv.v1;
            uv.v1 = uv.v2;
            uv.v2 = tmp;
        }
        switch (face) {
            case DOWN: 
            case UP: {
                emitter.uv((4 - rotation) % 4, uv.u1, uv.v1);
                emitter.uv((5 - rotation) % 4, uv.u1, uv.v2);
                emitter.uv((6 - rotation) % 4, uv.u2, uv.v2);
                emitter.uv((7 - rotation) % 4, uv.u2, uv.v1);
                break;
            }
            case NORTH: 
            case SOUTH: 
            case WEST: 
            case EAST: {
                emitter.uv((4 - rotation) % 4, uv.u1, uv.v2);
                emitter.uv((5 - rotation) % 4, uv.u1, uv.v1);
                emitter.uv((6 - rotation) % 4, uv.u2, uv.v1);
                emitter.uv((7 - rotation) % 4, uv.u2, uv.v2);
            }
        }
    }

    private UvVector getStandardUv(Direction face, TextureAtlasSprite texture, float x1, float y1, float z1, float x2, float y2, float z2) {
        UvVector uv = new UvVector();
        if (face.getAxis() != Direction.Axis.Y) {
            uv.v1 = texture.getV(1.0f - y1);
            uv.v2 = texture.getV(1.0f - y2);
        } else {
            uv.v1 = texture.getV(z1);
            uv.v2 = texture.getV(z2);
        }
        switch (face) {
            case DOWN: 
            case UP: 
            case SOUTH: {
                uv.u1 = texture.getU(x1);
                uv.u2 = texture.getU(x2);
                break;
            }
            case NORTH: {
                uv.u1 = texture.getU(1.0f - x2);
                uv.u2 = texture.getU(1.0f - x1);
                break;
            }
            case WEST: {
                uv.u1 = texture.getU(z1);
                uv.u2 = texture.getU(z2);
                break;
            }
            case EAST: {
                uv.u1 = texture.getU(1.0f - z2);
                uv.u2 = texture.getU(1.0f - z1);
            }
        }
        return uv;
    }

    public void setTexture(TextureAtlasSprite texture) {
        for (Direction face : Direction.values()) {
            this.textures.put(face, texture);
        }
    }

    public void setTextures(TextureAtlasSprite up, TextureAtlasSprite down, TextureAtlasSprite north, TextureAtlasSprite south, TextureAtlasSprite east, TextureAtlasSprite west) {
        this.textures.put(Direction.UP, up);
        this.textures.put(Direction.DOWN, down);
        this.textures.put(Direction.NORTH, north);
        this.textures.put(Direction.SOUTH, south);
        this.textures.put(Direction.EAST, east);
        this.textures.put(Direction.WEST, west);
    }

    public void setTexture(Direction facing, TextureAtlasSprite sprite) {
        this.textures.put(facing, sprite);
    }

    public void setDrawFaces(EnumSet<Direction> drawFaces) {
        this.drawFaces = drawFaces;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setColorRGB(int color) {
        this.setColor(color | 0xFF000000);
    }

    public void setColorRGB(float r, float g, float b) {
        this.setColorRGB((int)(r * 255.0f) << 16 | (int)(g * 255.0f) << 8 | (int)(b * 255.0f));
    }

    public void setEmissiveMaterial(boolean renderFullBright) {
        this.emissiveMaterial = renderFullBright;
    }

    public void setCustomUv(Direction facing, float u1, float v1, float u2, float v2) {
        this.customUv.put(facing, new Vector4f(u1, v1, u2, v2));
    }

    public void setUvRotation(Direction facing, int rotation) {
        Preconditions.checkArgument((rotation >= 0 && rotation <= 3 ? 1 : 0) != 0, (Object)"rotation");
        this.uvRotations[facing.ordinal()] = (byte)rotation;
    }

    public List<BakedQuad> getOutput() {
        return this.output;
    }

    private static final class UvVector {
        float u1;
        float u2;
        float v1;
        float v2;

        private UvVector() {
        }
    }
}

