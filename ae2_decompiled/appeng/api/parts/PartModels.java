/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 */
package appeng.api.parts;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;

public final class PartModels {
    private static final Set<ResourceLocation> models = new HashSet<ResourceLocation>();
    private static volatile boolean frozen = false;

    private PartModels() {
    }

    static void freeze() {
        frozen = true;
    }

    static Set<ResourceLocation> getModels() {
        return models;
    }

    public static synchronized void registerModels(Collection<ResourceLocation> partModels) {
        if (frozen) {
            throw new IllegalStateException("Cannot register models after the pre-initialization phase!");
        }
        models.addAll(partModels);
    }

    public static void registerModels(ResourceLocation ... partModels) {
        PartModels.registerModels(Arrays.asList(partModels));
    }
}

