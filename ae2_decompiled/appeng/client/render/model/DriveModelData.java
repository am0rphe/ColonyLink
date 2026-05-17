/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.Item
 *  net.neoforged.neoforge.client.model.data.ModelData
 *  net.neoforged.neoforge.client.model.data.ModelData$Builder
 *  net.neoforged.neoforge.client.model.data.ModelProperty
 */
package appeng.client.render.model;

import appeng.client.render.model.AEModelData;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

public final class DriveModelData {
    public static final ModelProperty<Item[]> STATE = new ModelProperty();

    private DriveModelData() {
    }

    public static ModelData.Builder builder(Item[] cells) {
        return AEModelData.builder().with(STATE, (Object)cells).with(AEModelData.SKIP_CACHE, (Object)true);
    }

    public static ModelData create(Item[] cells) {
        return DriveModelData.builder(cells).build();
    }
}

