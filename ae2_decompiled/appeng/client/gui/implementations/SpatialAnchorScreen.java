/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Inventory
 */
package appeng.client.gui.implementations;

import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.CommonButtons;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.core.localization.GuiText;
import appeng.menu.implementations.SpatialAnchorMenu;
import appeng.util.Platform;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class SpatialAnchorScreen
extends AEBaseScreen<SpatialAnchorMenu> {
    private final SettingToggleButton<YesNo> overlayToggle;

    public SpatialAnchorScreen(SpatialAnchorMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.addToLeftToolbar(CommonButtons.togglePowerUnit());
        this.overlayToggle = new ServerSettingToggleButton<YesNo>(Settings.OVERLAY_MODE, YesNo.NO);
        this.addToLeftToolbar(this.overlayToggle);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        this.overlayToggle.set(((SpatialAnchorMenu)this.menu).getOverlayMode());
        this.setTextContent("used_power", (Component)GuiText.SpatialAnchorUsedPower.text(Platform.formatPowerLong(((SpatialAnchorMenu)this.menu).powerConsumption * 100L, true)));
        this.setTextContent("loaded_chunks", (Component)GuiText.SpatialAnchorLoadedChunks.text(((SpatialAnchorMenu)this.menu).loadedChunks));
        this.setTextContent("statistics_loaded", (Component)GuiText.SpatialAnchorAllLoaded.text(((SpatialAnchorMenu)this.menu).allLoadedChunks, ((SpatialAnchorMenu)this.menu).allLoadedWorlds));
        this.setTextContent("statistics_total", (Component)GuiText.SpatialAnchorAll.text(((SpatialAnchorMenu)this.menu).allChunks, ((SpatialAnchorMenu)this.menu).allWorlds));
    }
}

