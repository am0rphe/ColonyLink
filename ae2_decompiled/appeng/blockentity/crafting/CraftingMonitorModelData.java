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

import appeng.api.util.AEColor;
import appeng.blockentity.crafting.CraftingCubeModelData;
import java.util.EnumSet;
import java.util.Objects;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

public final class CraftingMonitorModelData {
    public static final ModelProperty<AEColor> COLOR = new ModelProperty();

    public static ModelData.Builder builder(EnumSet<Direction> connections, AEColor color) {
        return CraftingCubeModelData.builder(connections).with(COLOR, (Object)Objects.requireNonNull(color));
    }

    public static ModelData create(EnumSet<Direction> connections, AEColor color) {
        return CraftingMonitorModelData.builder(connections, color).build();
    }
}

