/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 */
package appeng.init.internal;

import appeng.api.client.StorageCellModels;
import appeng.api.storage.StorageCells;
import appeng.core.definitions.AEItems;
import appeng.me.cells.BasicCellHandler;
import appeng.me.cells.CreativeCellHandler;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.resources.ResourceLocation;

public final class InitStorageCells {
    private static final ResourceLocation MODEL_BASE = ResourceLocation.parse((String)"ae2:block/drive/drive_base");
    private static final ResourceLocation MODEL_CELL_EMPTY = ResourceLocation.parse((String)"ae2:block/drive/drive_cell_empty");
    private static final ResourceLocation MODEL_CELL_ITEMS_1K = ResourceLocation.parse((String)"ae2:block/drive/cells/1k_item_cell");
    private static final ResourceLocation MODEL_CELL_ITEMS_4K = ResourceLocation.parse((String)"ae2:block/drive/cells/4k_item_cell");
    private static final ResourceLocation MODEL_CELL_ITEMS_16K = ResourceLocation.parse((String)"ae2:block/drive/cells/16k_item_cell");
    private static final ResourceLocation MODEL_CELL_ITEMS_64K = ResourceLocation.parse((String)"ae2:block/drive/cells/64k_item_cell");
    private static final ResourceLocation MODEL_CELL_ITEMS_256K = ResourceLocation.parse((String)"ae2:block/drive/cells/256k_item_cell");
    private static final ResourceLocation MODEL_CELL_FLUIDS_1K = ResourceLocation.parse((String)"ae2:block/drive/cells/1k_fluid_cell");
    private static final ResourceLocation MODEL_CELL_FLUIDS_4K = ResourceLocation.parse((String)"ae2:block/drive/cells/4k_fluid_cell");
    private static final ResourceLocation MODEL_CELL_FLUIDS_16K = ResourceLocation.parse((String)"ae2:block/drive/cells/16k_fluid_cell");
    private static final ResourceLocation MODEL_CELL_FLUIDS_64K = ResourceLocation.parse((String)"ae2:block/drive/cells/64k_fluid_cell");
    private static final ResourceLocation MODEL_CELL_FLUIDS_256K = ResourceLocation.parse((String)"ae2:block/drive/cells/256k_fluid_cell");
    private static final ResourceLocation MODEL_CELL_CREATIVE = ResourceLocation.parse((String)"ae2:block/drive/cells/creative_cell");
    private static final ResourceLocation[] MODELS = new ResourceLocation[]{MODEL_BASE, MODEL_CELL_EMPTY, StorageCellModels.getDefaultModel(), MODEL_CELL_ITEMS_1K, MODEL_CELL_ITEMS_4K, MODEL_CELL_ITEMS_16K, MODEL_CELL_ITEMS_64K, MODEL_CELL_ITEMS_256K, MODEL_CELL_FLUIDS_1K, MODEL_CELL_FLUIDS_4K, MODEL_CELL_FLUIDS_16K, MODEL_CELL_FLUIDS_64K, MODEL_CELL_FLUIDS_256K, MODEL_CELL_CREATIVE};

    public static Collection<ResourceLocation> getModels() {
        return Arrays.asList(MODELS);
    }

    private InitStorageCells() {
    }

    public static void init() {
        StorageCells.addCellHandler(BasicCellHandler.INSTANCE);
        StorageCells.addCellHandler(CreativeCellHandler.INSTANCE);
        StorageCellModels.registerModel(AEItems.ITEM_CELL_1K, MODEL_CELL_ITEMS_1K);
        StorageCellModels.registerModel(AEItems.ITEM_CELL_4K, MODEL_CELL_ITEMS_4K);
        StorageCellModels.registerModel(AEItems.ITEM_CELL_16K, MODEL_CELL_ITEMS_16K);
        StorageCellModels.registerModel(AEItems.ITEM_CELL_64K, MODEL_CELL_ITEMS_64K);
        StorageCellModels.registerModel(AEItems.ITEM_CELL_256K, MODEL_CELL_ITEMS_256K);
        StorageCellModels.registerModel(AEItems.FLUID_CELL_1K, MODEL_CELL_FLUIDS_1K);
        StorageCellModels.registerModel(AEItems.FLUID_CELL_4K, MODEL_CELL_FLUIDS_4K);
        StorageCellModels.registerModel(AEItems.FLUID_CELL_16K, MODEL_CELL_FLUIDS_16K);
        StorageCellModels.registerModel(AEItems.FLUID_CELL_64K, MODEL_CELL_FLUIDS_64K);
        StorageCellModels.registerModel(AEItems.FLUID_CELL_256K, MODEL_CELL_FLUIDS_256K);
        StorageCellModels.registerModel(AEItems.CREATIVE_CELL, MODEL_CELL_CREATIVE);
        StorageCellModels.registerModel(AEItems.PORTABLE_ITEM_CELL1K, MODEL_CELL_ITEMS_1K);
        StorageCellModels.registerModel(AEItems.PORTABLE_ITEM_CELL4K, MODEL_CELL_ITEMS_4K);
        StorageCellModels.registerModel(AEItems.PORTABLE_ITEM_CELL16K, MODEL_CELL_ITEMS_16K);
        StorageCellModels.registerModel(AEItems.PORTABLE_ITEM_CELL64K, MODEL_CELL_ITEMS_64K);
        StorageCellModels.registerModel(AEItems.PORTABLE_ITEM_CELL256K, MODEL_CELL_ITEMS_256K);
        StorageCellModels.registerModel(AEItems.PORTABLE_FLUID_CELL1K, MODEL_CELL_FLUIDS_1K);
        StorageCellModels.registerModel(AEItems.PORTABLE_FLUID_CELL4K, MODEL_CELL_FLUIDS_4K);
        StorageCellModels.registerModel(AEItems.PORTABLE_FLUID_CELL16K, MODEL_CELL_FLUIDS_16K);
        StorageCellModels.registerModel(AEItems.PORTABLE_FLUID_CELL64K, MODEL_CELL_FLUIDS_64K);
        StorageCellModels.registerModel(AEItems.PORTABLE_FLUID_CELL256K, MODEL_CELL_FLUIDS_256K);
    }
}

