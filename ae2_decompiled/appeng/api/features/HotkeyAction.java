/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 */
package appeng.api.features;

import appeng.hotkeys.HotkeyActions;
import net.minecraft.world.entity.player.Player;

public interface HotkeyAction {
    public static final String WIRELESS_TERMINAL = "wireless_terminal";
    public static final String PORTABLE_ITEM_CELL = "portable_item_cell";
    public static final String PORTABLE_FLUID_CELL = "portable_fluid_cell";

    public boolean run(Player var1);

    public static void register(HotkeyAction hotkeyAction, String id) {
        HotkeyActions.register(hotkeyAction, id);
    }
}

