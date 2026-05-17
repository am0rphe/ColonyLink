/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.world.entity.player.Inventory
 */
package appeng.client.gui.implementations;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.CommonButtons;
import appeng.core.localization.GuiText;
import appeng.menu.implementations.WirelessAccessPointMenu;
import appeng.util.Platform;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;

public class WirelessAccessPointScreen
extends AEBaseScreen<WirelessAccessPointMenu> {
    public WirelessAccessPointScreen(WirelessAccessPointMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.addToLeftToolbar(CommonButtons.togglePowerUnit());
        this.widgets.addBackgroundPanel("linkPanel");
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        MutableComponent rangeText = Component.empty();
        MutableComponent energyUseText = Component.empty();
        if (((WirelessAccessPointMenu)this.menu).getRange() > 0L) {
            double rangeBlocks = (double)((WirelessAccessPointMenu)this.menu).getRange() / 10.0;
            rangeText = GuiText.WirelessRange.text(rangeBlocks);
            energyUseText = GuiText.PowerUsageRate.text(Platform.formatPowerLong(((WirelessAccessPointMenu)this.menu).getDrain(), true));
        }
        this.setTextContent("range", (Component)rangeText);
        this.setTextContent("energy_use", (Component)energyUseText);
    }
}

