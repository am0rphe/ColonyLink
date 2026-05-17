/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.components.AbstractWidget
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Inventory
 */
package appeng.client.gui.implementations;

import appeng.api.config.CondenserOutput;
import appeng.api.config.Settings;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ProgressBar;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.core.localization.GuiText;
import appeng.menu.implementations.CondenserMenu;
import appeng.menu.interfaces.IProgressProvider;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CondenserScreen
extends AEBaseScreen<CondenserMenu> {
    private final SettingToggleButton<CondenserOutput> mode;

    public CondenserScreen(CondenserMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.mode = new ServerSettingToggleButton<CondenserOutput>(Settings.CONDENSER_OUTPUT, ((CondenserMenu)this.menu).getOutput());
        this.widgets.add("mode", (AbstractWidget)this.mode);
        this.widgets.add("progressBar", new ProgressBar((IProgressProvider)this.menu, style.getImage("progressBar"), ProgressBar.Direction.VERTICAL, (Component)GuiText.StoredEnergy.text()));
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        this.mode.set(((CondenserMenu)this.menu).getOutput());
    }
}

