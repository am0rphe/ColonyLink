/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.neoforged.neoforge.client.model.data.ModelData
 *  net.neoforged.neoforge.client.model.data.ModelData$Builder
 *  net.neoforged.neoforge.client.model.data.ModelProperty
 */
package appeng.blockentity.crafting;

import appeng.client.render.model.AEModelData;
import java.util.EnumSet;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

public final class CraftingCubeModelData {
    public static final ModelProperty<EnumSet<Direction>> CONNECTIONS = new ModelProperty();

    private CraftingCubeModelData() {
    }

    public static ModelData.Builder builder(EnumSet<Direction> connections) {
        return AEModelData.builder().with(AEModelData.SKIP_CACHE, (Object)true).with(CONNECTIONS, connections);
    }

    public static ModelData create(EnumSet<Direction> connections) {
        return CraftingCubeModelData.builder(connections).build();
    }
}

