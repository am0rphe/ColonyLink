/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.geom.ModelLayerLocation
 *  net.minecraft.client.model.geom.builders.LayerDefinition
 */
package appeng.init.client;

import appeng.client.render.tesr.SkyChestTESR;
import java.util.function.BiConsumer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;

public final class InitEntityLayerDefinitions {
    private InitEntityLayerDefinitions() {
    }

    public static void init(BiConsumer<ModelLayerLocation, LayerDefinition> consumer) {
        consumer.accept(SkyChestTESR.MODEL_LAYER, SkyChestTESR.createSingleBodyLayer());
    }
}

