/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.resources.model.UnbakedModel
 *  net.minecraft.resources.ResourceLocation
 */
package appeng.client.render;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;

public interface BasicUnbakedModel
extends UnbakedModel {
    default public Collection<ResourceLocation> getDependencies() {
        return Collections.emptyList();
    }

    default public void resolveParents(Function<ResourceLocation, UnbakedModel> function) {
        for (ResourceLocation dependency : this.getDependencies()) {
            function.apply(dependency).resolveParents(function);
        }
    }
}

