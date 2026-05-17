/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.neoforged.neoforge.client.model.data.ModelData
 *  net.neoforged.neoforge.client.model.data.ModelData$Builder
 *  net.neoforged.neoforge.client.model.data.ModelProperty
 */
package appeng.client.render.model;

import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

public final class AEModelData {
    public static final ModelProperty<Boolean> SKIP_CACHE = new ModelProperty();
    public static final ModelProperty<Byte> SPIN = new ModelProperty();

    public static ModelData.Builder builder() {
        return ModelData.builder();
    }

    public static ModelData create() {
        return AEModelData.builder().build();
    }
}

