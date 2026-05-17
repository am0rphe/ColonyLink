/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.VertexFormat
 *  net.minecraft.core.Direction
 *  net.minecraft.util.Mth
 */
package appeng.thirdparty.fabric;

import appeng.thirdparty.fabric.ModelHelper;
import com.google.common.base.Preconditions;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public abstract class EncodingFormat {
    static final int HEADER_BITS = 0;
    static final int HEADER_COLOR_INDEX = 1;
    static final int HEADER_TAG = 2;
    public static final int HEADER_STRIDE = 3;
    static final int VERTEX_X;
    static final int VERTEX_Y;
    static final int VERTEX_Z;
    static final int VERTEX_COLOR;
    static final int VERTEX_U;
    static final int VERTEX_V;
    static final int VERTEX_LIGHTMAP;
    static final int VERTEX_NORMAL;
    public static final int VERTEX_STRIDE;
    public static final int QUAD_STRIDE;
    public static final int QUAD_STRIDE_BYTES;
    public static final int TOTAL_STRIDE;
    static final int[] EMPTY;
    private static final int DIRECTION_MASK;
    private static final int DIRECTION_BIT_COUNT;
    private static final int CULL_SHIFT = 0;
    private static final int CULL_INVERSE_MASK;
    private static final int LIGHT_SHIFT;
    private static final int LIGHT_INVERSE_MASK;
    private static final int NORMALS_SHIFT;
    private static final int NORMALS_COUNT = 4;
    private static final int NORMALS_MASK = 15;
    private static final int NORMALS_INVERSE_MASK;
    private static final int GEOMETRY_SHIFT;
    private static final int GEOMETRY_MASK = 7;
    private static final int GEOMETRY_INVERSE_MASK;
    private static final int SHADE_SHIFT;
    private static final int SHADE_MASK;
    private static final int SHADE_BIT_COUNT;
    private static final int SHADE_INVERSE_MASK;
    private static final int AO_SHIFT;
    private static final int AO_MASK;
    private static final int AO_BIT_COUNT;
    private static final int AO_INVERSE_MASK;
    private static final int MATERIAL_SHIFT;
    private static final int MATERIAL_MASK;
    private static final int MATERIAL_BIT_COUNT;
    private static final int MATERIAL_INVERSE_MASK;

    private EncodingFormat() {
    }

    static Direction cullFace(int bits) {
        return ModelHelper.faceFromIndex(bits >> 0 & DIRECTION_MASK);
    }

    static int cullFace(int bits, Direction face) {
        return bits & CULL_INVERSE_MASK | ModelHelper.toFaceIndex(face) << 0;
    }

    static Direction lightFace(int bits) {
        return ModelHelper.faceFromIndex(bits >> LIGHT_SHIFT & DIRECTION_MASK);
    }

    static int lightFace(int bits, Direction face) {
        return bits & LIGHT_INVERSE_MASK | ModelHelper.toFaceIndex(face) << LIGHT_SHIFT;
    }

    static int normalFlags(int bits) {
        return bits >> NORMALS_SHIFT & 0xF;
    }

    static int normalFlags(int bits, int normalFlags) {
        return bits & NORMALS_INVERSE_MASK | (normalFlags & 0xF) << NORMALS_SHIFT;
    }

    static int geometryFlags(int bits) {
        return bits >> GEOMETRY_SHIFT & 7;
    }

    static int geometryFlags(int bits, int geometryFlags) {
        return bits & GEOMETRY_INVERSE_MASK | (geometryFlags & 7) << GEOMETRY_SHIFT;
    }

    static boolean shade(int bits) {
        return (bits >> SHADE_SHIFT & SHADE_MASK) != 0;
    }

    static int shade(int bits, boolean shade) {
        int value = shade ? 1 : 0;
        return bits & SHADE_INVERSE_MASK | value << SHADE_SHIFT;
    }

    static boolean ambientOcclusion(int bits) {
        return (bits >> AO_SHIFT & AO_MASK) != 0;
    }

    static int ambientOcclusion(int bits, boolean ambientOcclusion) {
        int value = ambientOcclusion ? 1 : 0;
        return bits & AO_INVERSE_MASK | value << AO_SHIFT;
    }

    static {
        VertexFormat format = DefaultVertexFormat.BLOCK;
        VERTEX_X = 3;
        VERTEX_Y = 4;
        VERTEX_Z = 5;
        VERTEX_COLOR = 6;
        VERTEX_U = 7;
        VERTEX_V = VERTEX_U + 1;
        VERTEX_LIGHTMAP = 9;
        VERTEX_NORMAL = 10;
        VERTEX_STRIDE = 8;
        QUAD_STRIDE = VERTEX_STRIDE * 4;
        QUAD_STRIDE_BYTES = QUAD_STRIDE * 4;
        TOTAL_STRIDE = 3 + QUAD_STRIDE;
        Preconditions.checkState((VERTEX_STRIDE == 8 ? 1 : 0) != 0, (String)"Indigo vertex stride (%s) mismatched with rendering API (%s)", (int)VERTEX_STRIDE, (int)8);
        Preconditions.checkState((QUAD_STRIDE == 32 ? 1 : 0) != 0, (String)"Indigo quad stride (%s) mismatched with rendering API (%s)", (int)QUAD_STRIDE, (int)32);
        EMPTY = new int[TOTAL_STRIDE];
        DIRECTION_MASK = Mth.smallestEncompassingPowerOfTwo((int)6) - 1;
        DIRECTION_BIT_COUNT = Integer.bitCount(DIRECTION_MASK);
        CULL_INVERSE_MASK = ~(DIRECTION_MASK << 0);
        LIGHT_SHIFT = 0 + DIRECTION_BIT_COUNT;
        LIGHT_INVERSE_MASK = ~(DIRECTION_MASK << LIGHT_SHIFT);
        NORMALS_SHIFT = LIGHT_SHIFT + DIRECTION_BIT_COUNT;
        NORMALS_INVERSE_MASK = ~(15 << NORMALS_SHIFT);
        GEOMETRY_SHIFT = NORMALS_SHIFT + 4;
        GEOMETRY_INVERSE_MASK = ~(7 << GEOMETRY_SHIFT);
        SHADE_SHIFT = GEOMETRY_SHIFT + 3;
        SHADE_MASK = Mth.smallestEncompassingPowerOfTwo((int)2) - 1;
        SHADE_BIT_COUNT = Integer.bitCount(SHADE_MASK);
        SHADE_INVERSE_MASK = ~(SHADE_MASK << SHADE_SHIFT);
        AO_SHIFT = SHADE_SHIFT + SHADE_BIT_COUNT;
        AO_MASK = Mth.smallestEncompassingPowerOfTwo((int)2) - 1;
        AO_BIT_COUNT = Integer.bitCount(AO_MASK);
        AO_INVERSE_MASK = ~(AO_MASK << AO_SHIFT);
        MATERIAL_SHIFT = AO_SHIFT + AO_BIT_COUNT;
        MATERIAL_MASK = Mth.smallestEncompassingPowerOfTwo((int)1) - 1;
        MATERIAL_BIT_COUNT = Integer.bitCount(MATERIAL_MASK);
        MATERIAL_INVERSE_MASK = ~(MATERIAL_MASK << MATERIAL_SHIFT);
        Preconditions.checkArgument((MATERIAL_SHIFT + MATERIAL_BIT_COUNT <= 32 ? 1 : 0) != 0, (String)"Indigo header encoding bit count (%s) exceeds integer bit length)", (int)TOTAL_STRIDE);
    }
}

