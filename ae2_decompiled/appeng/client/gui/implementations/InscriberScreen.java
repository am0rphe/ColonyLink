/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Inventory
 */
package appeng.client.gui.implementations;

import appeng.api.config.InscriberInputCapacity;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ProgressBar;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.menu.implementations.InscriberMenu;
import appeng.menu.interfaces.IProgressProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class InscriberScreen
extends UpgradeableScreen<InscriberMenu> {
    private final ProgressBar pb;
    private final SettingToggleButton<YesNo> separateSidesBtn;
    private final SettingToggleButton<YesNo> autoExportBtn;
    private final SettingToggleButton<InscriberInputCapacity> bufferSizeBtn;

    public InscriberScreen(InscriberMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.pb = new ProgressBar((IProgressProvider)this.menu, style.getImage("progressBar"), ProgressBar.Direction.VERTICAL);
        this.widgets.add("progressBar", this.pb);
        this.separateSidesBtn = new ServerSettingToggleButton<YesNo>(Settings.INSCRIBER_SEPARATE_SIDES, YesNo.NO);
        this.addToLeftToolbar(this.separateSidesBtn);
        this.autoExportBtn = new ServerSettingToggleButton<YesNo>(Settings.AUTO_EXPORT, YesNo.NO);
        this.addToLeftToolbar(this.autoExportBtn);
        this.bufferSizeBtn = new ServerSettingToggleButton<InscriberInputCapacity>(Settings.INSCRIBER_INPUT_CAPACITY, InscriberInputCapacity.SIXTY_FOUR);
        this.addToLeftToolbar(this.bufferSizeBtn);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        int progress = ((InscriberMenu)this.menu).getCurrentProgress() * 100 / ((InscriberMenu)this.menu).getMaxProgress();
        this.pb.setFullMsg((Component)Component.literal((String)(progress + "%")));
        this.separateSidesBtn.set(((InscriberMenu)this.getMenu()).getSeparateSides());
        this.autoExportBtn.set(((InscriberMenu)this.getMenu()).getAutoExport());
        this.bufferSizeBtn.set(((InscriberMenu)this.getMenu()).getBufferSize());
    }
}

