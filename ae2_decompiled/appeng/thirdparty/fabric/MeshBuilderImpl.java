/*
 * Decompiled with CFR 0.152.
 */
package appeng.thirdparty.fabric;

import appeng.thirdparty.fabric.EncodingFormat;
import appeng.thirdparty.fabric.Mesh;
import appeng.thirdparty.fabric.MeshBuilder;
import appeng.thirdparty.fabric.MeshImpl;
import appeng.thirdparty.fabric.MutableQuadViewImpl;
import appeng.thirdparty.fabric.QuadEmitter;

public class MeshBuilderImpl
implements MeshBuilder {
    int[] data = new int[256];
    private final Maker maker = new Maker();
    int index = 0;
    int limit = this.data.length;

    protected void ensureCapacity(int stride) {
        if (stride > this.limit - this.index) {
            this.limit *= 2;
            int[] bigger = new int[this.limit];
            System.arraycopy(this.data, 0, bigger, 0, this.index);
            this.data = bigger;
            this.maker.data = bigger;
        }
    }

    @Override
    public Mesh build() {
        int[] packed = new int[this.index];
        System.arraycopy(this.data, 0, packed, 0, this.index);
        this.index = 0;
        this.maker.begin(this.data, this.index);
        return new MeshImpl(packed);
    }

    @Override
    public QuadEmitter getEmitter() {
        this.ensureCapacity(EncodingFormat.TOTAL_STRIDE);
        this.maker.begin(this.data, this.index);
        return this.maker;
    }

    private class Maker
    extends MutableQuadViewImpl
    implements QuadEmitter {
        private Maker() {
        }

        @Override
        public Maker emit() {
            this.computeGeometry();
            MeshBuilderImpl.this.index += EncodingFormat.TOTAL_STRIDE;
            MeshBuilderImpl.this.ensureCapacity(EncodingFormat.TOTAL_STRIDE);
            this.baseIndex = MeshBuilderImpl.this.index;
            this.clear();
            return this;
        }
    }
}

