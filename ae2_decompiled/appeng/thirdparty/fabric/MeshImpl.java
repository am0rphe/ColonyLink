/*
 * Decompiled with CFR 0.152.
 */
package appeng.thirdparty.fabric;

import appeng.thirdparty.fabric.EncodingFormat;
import appeng.thirdparty.fabric.Mesh;
import appeng.thirdparty.fabric.QuadView;
import appeng.thirdparty.fabric.QuadViewImpl;
import java.util.function.Consumer;

public class MeshImpl
implements Mesh {
    ThreadLocal<QuadViewImpl> POOL = ThreadLocal.withInitial(QuadViewImpl::new);
    final int[] data;

    MeshImpl(int[] data) {
        this.data = data;
    }

    public int[] data() {
        return this.data;
    }

    @Override
    public void forEach(Consumer<QuadView> consumer) {
        this.forEach(consumer, this.POOL.get());
    }

    void forEach(Consumer<QuadView> consumer, QuadViewImpl cursor) {
        int limit = this.data.length;
        for (int index = 0; index < limit; index += EncodingFormat.TOTAL_STRIDE) {
            cursor.load(this.data, index);
            consumer.accept(cursor);
        }
    }
}

