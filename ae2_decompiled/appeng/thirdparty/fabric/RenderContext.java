/*
 * Decompiled with CFR 0.152.
 */
package appeng.thirdparty.fabric;

import appeng.thirdparty.fabric.MutableQuadView;

public interface RenderContext {

    @FunctionalInterface
    public static interface QuadTransform {
        public boolean transform(MutableQuadView var1);
    }
}

