/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 */
package appeng.api.parts;

import appeng.api.parts.PartModels;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;

public final class PartModelsInternal {
    private PartModelsInternal() {
    }

    public static void freeze() {
        PartModels.freeze();
    }

    public static Set<ResourceLocation> getModels() {
        return PartModels.getModels();
    }
}

