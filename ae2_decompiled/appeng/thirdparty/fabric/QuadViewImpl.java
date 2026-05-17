/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  org.joml.Vector3f
 */
package appeng.thirdparty.fabric;

import appeng.thirdparty.fabric.ColorHelper;
import appeng.thirdparty.fabric.EncodingFormat;
import appeng.thirdparty.fabric.GeometryHelper;
import appeng.thirdparty.fabric.MutableQuadView;
import appeng.thirdparty.fabric.MutableQuadViewImpl;
import appeng.thirdparty.fabric.NormalHelper;
import appeng.thirdparty.fabric.QuadView;
import net.minecraft.core.Direction;
import org.joml.Vector3f;

public class QuadViewImpl
implements QuadView {
    protected Direction nominalFace;
    protected boolean isGeometryInvalid = true;
    protected final Vector3f faceNormal = new Vector3f();
    protected int[] data;
    protected int baseIndex = 0;

    final void load(int[] data, int baseIndex) {
        this.data = data;
        this.baseIndex = baseIndex;
        this.load();
    }

    public final void load() {
        this.isGeometryInvalid = false;
        this.nominalFace = this.lightFace();
        NormalHelper.computeFaceNormal(this.faceNormal, this);
    }

    public int[] data() {
        return this.data;
    }

    public int normalFlags() {
        return EncodingFormat.normalFlags(this.data[this.baseIndex + 0]);
    }

    public boolean hasVertexNormals() {
        return this.normalFlags() != 0;
    }

    public int geometryFlags() {
        this.computeGeometry();
        return EncodingFormat.geometryFlags(this.data[this.baseIndex + 0]);
    }

    protected void computeGeometry() {
        if (this.isGeometryInvalid) {
            this.isGeometryInvalid = false;
            NormalHelper.computeFaceNormal(this.faceNormal, this);
            this.data[this.baseIndex + 0] = EncodingFormat.lightFace(this.data[this.baseIndex + 0], GeometryHelper.lightFace(this));
            this.data[this.baseIndex + 0] = EncodingFormat.geometryFlags(this.data[this.baseIndex + 0], GeometryHelper.computeShapeFlags(this));
        }
    }

    @Override
    public final int colorIndex() {
        return this.data[this.baseIndex + 1];
    }

    @Override
    public final int tag() {
        return this.data[this.baseIndex + 2];
    }

    @Override
    public final Direction lightFace() {
        this.computeGeometry();
        return EncodingFormat.lightFace(this.data[this.baseIndex + 0]);
    }

    @Override
    public final Direction cullFace() {
        return EncodingFormat.cullFace(this.data[this.baseIndex + 0]);
    }

    @Override
    public final Direction nominalFace() {
        return this.nominalFace;
    }

    @Override
    public final Vector3f faceNormal() {
        this.computeGeometry();
        return this.faceNormal;
    }

    @Override
    public void copyTo(MutableQuadView target) {
        this.computeGeometry();
        MutableQuadViewImpl quad = (MutableQuadViewImpl)target;
        System.arraycopy(this.data, this.baseIndex, quad.data, quad.baseIndex, EncodingFormat.TOTAL_STRIDE);
        quad.faceNormal.set(this.faceNormal.x(), this.faceNormal.y(), this.faceNormal.z());
        quad.nominalFace = this.nominalFace;
        quad.isGeometryInvalid = false;
    }

    @Override
    public Vector3f copyPos(int vertexIndex, Vector3f target) {
        if (target == null) {
            target = new Vector3f();
        }
        int index = this.baseIndex + vertexIndex * EncodingFormat.VERTEX_STRIDE + EncodingFormat.VERTEX_X;
        target.set(Float.intBitsToFloat(this.data[index]), Float.intBitsToFloat(this.data[index + 1]), Float.intBitsToFloat(this.data[index + 2]));
        return target;
    }

    @Override
    public float posByIndex(int vertexIndex, int coordinateIndex) {
        return Float.intBitsToFloat(this.data[this.baseIndex + vertexIndex * EncodingFormat.VERTEX_STRIDE + EncodingFormat.VERTEX_X + coordinateIndex]);
    }

    @Override
    public float x(int vertexIndex) {
        return Float.intBitsToFloat(this.data[this.baseIndex + vertexIndex * EncodingFormat.VERTEX_STRIDE + EncodingFormat.VERTEX_X]);
    }

    @Override
    public float y(int vertexIndex) {
        return Float.intBitsToFloat(this.data[this.baseIndex + vertexIndex * EncodingFormat.VERTEX_STRIDE + EncodingFormat.VERTEX_Y]);
    }

    @Override
    public float z(int vertexIndex) {
        return Float.intBitsToFloat(this.data[this.baseIndex + vertexIndex * EncodingFormat.VERTEX_STRIDE + EncodingFormat.VERTEX_Z]);
    }

    @Override
    public boolean hasNormal(int vertexIndex) {
        return (this.normalFlags() & 1 << vertexIndex) != 0;
    }

    protected final int normalIndex(int vertexIndex) {
        return this.baseIndex + vertexIndex * EncodingFormat.VERTEX_STRIDE + EncodingFormat.VERTEX_NORMAL;
    }

    @Override
    public Vector3f copyNormal(int vertexIndex, Vector3f target) {
        if (this.hasNormal(vertexIndex)) {
            if (target == null) {
                target = new Vector3f();
            }
            int normal = this.data[this.normalIndex(vertexIndex)];
            target.set(NormalHelper.getPackedNormalComponent(normal, 0), NormalHelper.getPackedNormalComponent(normal, 1), NormalHelper.getPackedNormalComponent(normal, 2));
            return target;
        }
        return null;
    }

    @Override
    public float normalX(int vertexIndex) {
        return this.hasNormal(vertexIndex) ? NormalHelper.getPackedNormalComponent(this.data[this.normalIndex(vertexIndex)], 0) : Float.NaN;
    }

    @Override
    public float normalY(int vertexIndex) {
        return this.hasNormal(vertexIndex) ? NormalHelper.getPackedNormalComponent(this.data[this.normalIndex(vertexIndex)], 1) : Float.NaN;
    }

    @Override
    public float normalZ(int vertexIndex) {
        return this.hasNormal(vertexIndex) ? NormalHelper.getPackedNormalComponent(this.data[this.normalIndex(vertexIndex)], 2) : Float.NaN;
    }

    @Override
    public int lightmap(int vertexIndex) {
        return this.data[this.baseIndex + vertexIndex * EncodingFormat.VERTEX_STRIDE + EncodingFormat.VERTEX_LIGHTMAP];
    }

    @Override
    public int color(int vertexIndex) {
        return this.data[this.baseIndex + vertexIndex * EncodingFormat.VERTEX_STRIDE + EncodingFormat.VERTEX_COLOR];
    }

    @Override
    public float u(int vertexIndex) {
        return Float.intBitsToFloat(this.data[this.baseIndex + vertexIndex * EncodingFormat.VERTEX_STRIDE + EncodingFormat.VERTEX_U]);
    }

    @Override
    public float v(int vertexIndex) {
        return Float.intBitsToFloat(this.data[this.baseIndex + vertexIndex * EncodingFormat.VERTEX_STRIDE + EncodingFormat.VERTEX_V]);
    }

    public int vertexStart() {
        return this.baseIndex + 3;
    }

    @Override
    public boolean hasShade() {
        return EncodingFormat.shade(this.data[this.baseIndex + 0]);
    }

    @Override
    public boolean hasAmbientOcclusion() {
        return EncodingFormat.ambientOcclusion(this.data[this.baseIndex + 0]);
    }

    @Override
    public final void toVanilla(int[] target, int targetIndex) {
        System.arraycopy(this.data, this.baseIndex + 3, target, targetIndex, EncodingFormat.QUAD_STRIDE);
        int colorIndex = targetIndex + 3;
        for (int i = 0; i < 4; ++i) {
            target[colorIndex] = ColorHelper.toVanillaColor(target[colorIndex]);
            colorIndex += 8;
        }
    }
}

