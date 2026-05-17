/*
 * Decompiled with CFR 0.152.
 */
package appeng.thirdparty.fabric;

import appeng.thirdparty.fabric.MeshBuilder;
import appeng.thirdparty.fabric.MeshBuilderImpl;

public interface Renderer {
    public MeshBuilder meshBuilder();

    public static Renderer getInstance() {
        return new Renderer(){

            @Override
            public MeshBuilder meshBuilder() {
                return new MeshBuilderImpl();
            }
        };
    }
}

