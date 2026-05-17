/*
 * Decompiled with CFR 0.152.
 */
package appeng.init.internal;

import appeng.api.upgrades.Upgrades;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.core.definitions.ItemDefinition;
import appeng.core.localization.GuiText;
import appeng.items.storage.BasicStorageCell;
import appeng.items.tools.powered.PortableCellItem;
import java.util.List;

public final class InitUpgrades {
    private InitUpgrades() {
    }

    public static void init() {
        String interfaceGroup = GuiText.Interface.getTranslationKey();
        String itemIoBusGroup = GuiText.IOBuses.getTranslationKey();
        String storageCellGroup = GuiText.StorageCells.getTranslationKey();
        String portableCellGroup = GuiText.PortableCells.getTranslationKey();
        String wirelessTerminalGroup = GuiText.WirelessTerminals.getTranslationKey();
        Upgrades.add(AEItems.CRAFTING_CARD, AEParts.INTERFACE, 1, interfaceGroup);
        Upgrades.add(AEItems.CRAFTING_CARD, AEBlocks.INTERFACE, 1, interfaceGroup);
        Upgrades.add(AEItems.FUZZY_CARD, AEParts.INTERFACE, 1, interfaceGroup);
        Upgrades.add(AEItems.FUZZY_CARD, AEBlocks.INTERFACE, 1, interfaceGroup);
        Upgrades.add(AEItems.SPEED_CARD, AEBlocks.IO_PORT, 3);
        Upgrades.add(AEItems.REDSTONE_CARD, AEBlocks.IO_PORT, 1);
        Upgrades.add(AEItems.FUZZY_CARD, AEParts.LEVEL_EMITTER, 1);
        Upgrades.add(AEItems.CRAFTING_CARD, AEParts.LEVEL_EMITTER, 1);
        Upgrades.add(AEItems.FUZZY_CARD, AEParts.IMPORT_BUS, 1, itemIoBusGroup);
        Upgrades.add(AEItems.REDSTONE_CARD, AEParts.IMPORT_BUS, 1, itemIoBusGroup);
        Upgrades.add(AEItems.CAPACITY_CARD, AEParts.IMPORT_BUS, 5, itemIoBusGroup);
        Upgrades.add(AEItems.SPEED_CARD, AEParts.IMPORT_BUS, 4, itemIoBusGroup);
        Upgrades.add(AEItems.INVERTER_CARD, AEParts.IMPORT_BUS, 1, itemIoBusGroup);
        Upgrades.add(AEItems.FUZZY_CARD, AEParts.EXPORT_BUS, 1, itemIoBusGroup);
        Upgrades.add(AEItems.REDSTONE_CARD, AEParts.EXPORT_BUS, 1, itemIoBusGroup);
        Upgrades.add(AEItems.CAPACITY_CARD, AEParts.EXPORT_BUS, 5, itemIoBusGroup);
        Upgrades.add(AEItems.SPEED_CARD, AEParts.EXPORT_BUS, 4, itemIoBusGroup);
        Upgrades.add(AEItems.CRAFTING_CARD, AEParts.EXPORT_BUS, 1, itemIoBusGroup);
        List<ItemDefinition<BasicStorageCell>> itemCells = List.of(AEItems.ITEM_CELL_1K, AEItems.ITEM_CELL_4K, AEItems.ITEM_CELL_16K, AEItems.ITEM_CELL_64K, AEItems.ITEM_CELL_256K);
        for (ItemDefinition<BasicStorageCell> itemDefinition : itemCells) {
            Upgrades.add(AEItems.FUZZY_CARD, itemDefinition, 1, storageCellGroup);
            Upgrades.add(AEItems.INVERTER_CARD, itemDefinition, 1, storageCellGroup);
            Upgrades.add(AEItems.EQUAL_DISTRIBUTION_CARD, itemDefinition, 1, storageCellGroup);
            Upgrades.add(AEItems.VOID_CARD, itemDefinition, 1, storageCellGroup);
        }
        List<ItemDefinition<BasicStorageCell>> fluidCells = List.of(AEItems.FLUID_CELL_1K, AEItems.FLUID_CELL_4K, AEItems.FLUID_CELL_16K, AEItems.FLUID_CELL_64K, AEItems.FLUID_CELL_256K);
        for (ItemDefinition<BasicStorageCell> itemDefinition : fluidCells) {
            Upgrades.add(AEItems.INVERTER_CARD, itemDefinition, 1, storageCellGroup);
            Upgrades.add(AEItems.EQUAL_DISTRIBUTION_CARD, itemDefinition, 1, storageCellGroup);
            Upgrades.add(AEItems.VOID_CARD, itemDefinition, 1, storageCellGroup);
        }
        List<ItemDefinition<PortableCellItem>> list = List.of(AEItems.PORTABLE_ITEM_CELL1K, AEItems.PORTABLE_ITEM_CELL4K, AEItems.PORTABLE_ITEM_CELL16K, AEItems.PORTABLE_ITEM_CELL64K, AEItems.PORTABLE_ITEM_CELL256K);
        for (ItemDefinition<PortableCellItem> itemDefinition : list) {
            Upgrades.add(AEItems.FUZZY_CARD, itemDefinition, 1, portableCellGroup);
            Upgrades.add(AEItems.INVERTER_CARD, itemDefinition, 1, portableCellGroup);
            Upgrades.add(AEItems.EQUAL_DISTRIBUTION_CARD, itemDefinition, 1, portableCellGroup);
            Upgrades.add(AEItems.VOID_CARD, itemDefinition, 1, portableCellGroup);
            Upgrades.add(AEItems.ENERGY_CARD, itemDefinition, 2, portableCellGroup);
        }
        List<ItemDefinition<PortableCellItem>> list2 = List.of(AEItems.PORTABLE_FLUID_CELL1K, AEItems.PORTABLE_FLUID_CELL4K, AEItems.PORTABLE_FLUID_CELL16K, AEItems.PORTABLE_FLUID_CELL64K, AEItems.PORTABLE_FLUID_CELL256K);
        for (ItemDefinition<PortableCellItem> portableFluidCell : list2) {
            Upgrades.add(AEItems.INVERTER_CARD, portableFluidCell, 1, portableCellGroup);
            Upgrades.add(AEItems.EQUAL_DISTRIBUTION_CARD, portableFluidCell, 1, portableCellGroup);
            Upgrades.add(AEItems.VOID_CARD, portableFluidCell, 1, portableCellGroup);
            Upgrades.add(AEItems.ENERGY_CARD, portableFluidCell, 2, portableCellGroup);
        }
        Upgrades.add(AEItems.ENERGY_CARD, AEItems.WIRELESS_TERMINAL, 2, wirelessTerminalGroup);
        Upgrades.add(AEItems.ENERGY_CARD, AEItems.WIRELESS_CRAFTING_TERMINAL, 2, wirelessTerminalGroup);
        Upgrades.add(AEItems.FUZZY_CARD, AEItems.VIEW_CELL, 1);
        Upgrades.add(AEItems.INVERTER_CARD, AEItems.VIEW_CELL, 1);
        Upgrades.add(AEItems.FUZZY_CARD, AEParts.STORAGE_BUS, 1);
        Upgrades.add(AEItems.INVERTER_CARD, AEParts.STORAGE_BUS, 1);
        Upgrades.add(AEItems.CAPACITY_CARD, AEParts.STORAGE_BUS, 5);
        Upgrades.add(AEItems.VOID_CARD, AEParts.STORAGE_BUS, 1);
        Upgrades.add(AEItems.FUZZY_CARD, AEParts.FORMATION_PLANE, 1);
        Upgrades.add(AEItems.INVERTER_CARD, AEParts.FORMATION_PLANE, 1);
        Upgrades.add(AEItems.CAPACITY_CARD, AEParts.FORMATION_PLANE, 5);
        Upgrades.add(AEItems.ENERGY_CARD, AEItems.COLOR_APPLICATOR, 2);
        Upgrades.add(AEItems.EQUAL_DISTRIBUTION_CARD, AEItems.COLOR_APPLICATOR, 1);
        Upgrades.add(AEItems.VOID_CARD, AEItems.COLOR_APPLICATOR, 1);
        Upgrades.add(AEItems.ENERGY_CARD, AEItems.MATTER_CANNON, 2);
        Upgrades.add(AEItems.FUZZY_CARD, AEItems.MATTER_CANNON, 1);
        Upgrades.add(AEItems.INVERTER_CARD, AEItems.MATTER_CANNON, 1);
        Upgrades.add(AEItems.VOID_CARD, AEItems.MATTER_CANNON, 1);
        Upgrades.add(AEItems.SPEED_CARD, AEItems.MATTER_CANNON, 4);
        Upgrades.add(AEItems.SPEED_CARD, AEBlocks.MOLECULAR_ASSEMBLER, 5);
        Upgrades.add(AEItems.SPEED_CARD, AEBlocks.INSCRIBER, 4);
        Upgrades.add(AEItems.ENERGY_CARD, AEBlocks.VIBRATION_CHAMBER, 3);
        Upgrades.add(AEItems.SPEED_CARD, AEBlocks.VIBRATION_CHAMBER, 3);
    }
}

