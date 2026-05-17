/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.ItemLike
 */
package appeng.hotkeys;

import appeng.api.features.HotkeyAction;
import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.ItemDefinition;
import appeng.hotkeys.CuriosHotkeyAction;
import appeng.hotkeys.InventoryHotkeyAction;
import appeng.items.tools.powered.AbstractPortableCell;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.level.ItemLike;

public class HotkeyActions {
    public static final Map<String, List<HotkeyAction>> REGISTRY = new HashMap<String, List<HotkeyAction>>();

    public static void init() {
        HotkeyActions.register(AEItems.WIRELESS_TERMINAL, (player, locator) -> AEItems.WIRELESS_TERMINAL.get().openFromInventory(player, locator), "wireless_terminal");
        HotkeyActions.register(AEItems.WIRELESS_CRAFTING_TERMINAL, (player, locator) -> AEItems.WIRELESS_CRAFTING_TERMINAL.get().openFromInventory(player, locator), "wireless_terminal");
        HotkeyActions.registerPortableCell(AEItems.PORTABLE_ITEM_CELL1K, "portable_item_cell");
        HotkeyActions.registerPortableCell(AEItems.PORTABLE_ITEM_CELL4K, "portable_item_cell");
        HotkeyActions.registerPortableCell(AEItems.PORTABLE_ITEM_CELL16K, "portable_item_cell");
        HotkeyActions.registerPortableCell(AEItems.PORTABLE_ITEM_CELL64K, "portable_item_cell");
        HotkeyActions.registerPortableCell(AEItems.PORTABLE_ITEM_CELL256K, "portable_item_cell");
        HotkeyActions.registerPortableCell(AEItems.PORTABLE_FLUID_CELL1K, "portable_fluid_cell");
        HotkeyActions.registerPortableCell(AEItems.PORTABLE_FLUID_CELL4K, "portable_fluid_cell");
        HotkeyActions.registerPortableCell(AEItems.PORTABLE_FLUID_CELL16K, "portable_fluid_cell");
        HotkeyActions.registerPortableCell(AEItems.PORTABLE_FLUID_CELL64K, "portable_fluid_cell");
        HotkeyActions.registerPortableCell(AEItems.PORTABLE_FLUID_CELL256K, "portable_fluid_cell");
    }

    public static void registerPortableCell(ItemDefinition<? extends AbstractPortableCell> cell, String id) {
        HotkeyActions.register(cell, (player, locator) -> ((AbstractPortableCell)cell.get()).openFromInventory(player, locator), id);
    }

    public static void register(ItemLike item, InventoryHotkeyAction.Opener opener, String id) {
        HotkeyActions.register(new InventoryHotkeyAction(item, opener), id);
        HotkeyActions.register(new CuriosHotkeyAction(item, opener), id);
    }

    public static synchronized void register(HotkeyAction hotkeyAction, String id) {
        if (REGISTRY.containsKey(id)) {
            REGISTRY.get(id).addFirst(hotkeyAction);
        } else {
            REGISTRY.put(id, new ArrayList<HotkeyAction>(List.of(hotkeyAction)));
            AppEng.instance().registerHotkey(id);
        }
    }
}

