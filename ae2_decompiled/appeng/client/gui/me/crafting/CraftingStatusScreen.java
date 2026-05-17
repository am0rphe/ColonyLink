/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Inventory
 */
package appeng.client.gui.me.crafting;

import appeng.client.gui.implementations.AESubScreen;
import appeng.client.gui.me.crafting.CraftingCPUScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.CPUSelectionList;
import appeng.client.gui.widgets.Scrollbar;
import appeng.menu.me.crafting.CraftingStatusMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CraftingStatusScreen
extends CraftingCPUScreen<CraftingStatusMenu> {
    public CraftingStatusScreen(CraftingStatusMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        AESubScreen.addBackButton(menu, "back", this.widgets);
        Scrollbar scrollbar = this.widgets.addScrollBar("selectCpuScrollbar", Scrollbar.BIG);
        this.widgets.add("selectCpuList", new CPUSelectionList(menu, scrollbar, style));
    }

    @Override
    protected Component getGuiDisplayName(Component in) {
        return in;
    }
}

