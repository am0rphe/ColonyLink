/*
 * Decompiled with CFR 0.152.
 */
package appeng.thirdparty.fabric;

import appeng.thirdparty.fabric.Mesh;
import appeng.thirdparty.fabric.QuadEmitter;

public interface MeshBuilder {
    public QuadEmitter getEmitter();

    public Mesh build();
}

