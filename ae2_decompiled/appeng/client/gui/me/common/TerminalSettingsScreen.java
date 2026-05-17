/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.components.AbstractWidget
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 */
package appeng.client.gui.me.common;

import appeng.client.gui.AESubScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.widgets.AECheckbox;
import appeng.client.gui.widgets.TabButton;
import appeng.core.localization.GuiText;
import appeng.integration.abstraction.ItemListMod;
import appeng.menu.SlotSemantics;
import appeng.menu.me.common.MEStorageMenu;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class TerminalSettingsScreen<C extends MEStorageMenu>
extends AESubScreen<C, MEStorageScreen<C>> {
    private final AECheckbox pinAutoCraftedItemsCheckbox;
    private final AECheckbox notifyForFinishedCraftingJobsCheckbox;
    private final AECheckbox clearGridOnCloseCheckbox;
    private final AECheckbox useInternalSearchRadio;
    private final AECheckbox useExternalSearchRadio;
    private final AECheckbox rememberCheckbox;
    private final AECheckbox autoFocusCheckbox;
    private final AECheckbox syncWithExternalCheckbox;
    private final AECheckbox clearExternalCheckbox;

    public TerminalSettingsScreen(MEStorageScreen<C> parent) {
        super(parent, "/screens/terminals/terminal_settings.json");
        boolean hasExternalSearch;
        MutableComponent externalSearchMod;
        this.addBackButton();
        if (ItemListMod.isEnabled()) {
            externalSearchMod = Component.literal((String)ItemListMod.getShortName());
            hasExternalSearch = true;
        } else {
            externalSearchMod = Component.literal((String)"REI/EMI");
            hasExternalSearch = false;
        }
        this.pinAutoCraftedItemsCheckbox = this.widgets.addCheckbox("pinAutoCraftedItemsCheckbox", (Component)GuiText.TerminalSettingsPinAutoCraftedItems.text(), this::save);
        this.notifyForFinishedCraftingJobsCheckbox = this.widgets.addCheckbox("notifyForFinishedCraftingJobsCheckbox", (Component)GuiText.TerminalSettingsNotifyForFinishedJobs.text(), this::save);
        this.clearGridOnCloseCheckbox = this.widgets.addCheckbox("clearGridOnCloseCheckbox", (Component)GuiText.TerminalSettingsClearGridOnClose.text(), this::save);
        this.useInternalSearchRadio = this.widgets.addCheckbox("useInternalSearchRadio", (Component)GuiText.SearchSettingsUseInternalSearch.text(), this::switchToAeSearch);
        this.useInternalSearchRadio.setRadio(true);
        this.useExternalSearchRadio = this.widgets.addCheckbox("useExternalSearchRadio", (Component)GuiText.SearchSettingsUseExternalSearch.text(externalSearchMod), this::switchToExternalSearch);
        this.useExternalSearchRadio.setRadio(true);
        this.useExternalSearchRadio.active = hasExternalSearch;
        this.rememberCheckbox = this.widgets.addCheckbox("rememberCheckbox", (Component)GuiText.SearchSettingsRememberSearch.text(), this::save);
        this.autoFocusCheckbox = this.widgets.addCheckbox("autoFocusCheckbox", (Component)GuiText.SearchSettingsAutoFocus.text(), this::save);
        this.syncWithExternalCheckbox = this.widgets.addCheckbox("syncWithExternalCheckbox", (Component)GuiText.SearchSettingsSyncWithExternal.text(externalSearchMod), this::save);
        this.clearExternalCheckbox = this.widgets.addCheckbox("clearExternalCheckbox", (Component)GuiText.SearchSettingsClearExternal.text(externalSearchMod), this::save);
        this.updateState();
    }

    @Override
    protected void init() {
        super.init();
        this.setSlotsHidden(SlotSemantics.TOOLBOX, true);
    }

    private void switchToAeSearch() {
        this.useInternalSearchRadio.setSelected(true);
        this.useExternalSearchRadio.setSelected(false);
        this.save();
    }

    private void switchToExternalSearch() {
        this.useInternalSearchRadio.setSelected(false);
        this.useExternalSearchRadio.setSelected(true);
        this.save();
    }

    private void addBackButton() {
        Component label = ((MEStorageMenu)this.menu).getHost().getMainMenuIcon().getHoverName();
        TabButton button = new TabButton(Icon.BACK, label, btn -> this.returnToParent());
        this.widgets.add("back", (AbstractWidget)button);
    }

    private void updateState() {
        this.pinAutoCraftedItemsCheckbox.setSelected(this.config.isPinAutoCraftedItems());
        this.notifyForFinishedCraftingJobsCheckbox.setSelected(this.config.isNotifyForFinishedCraftingJobs());
        this.clearGridOnCloseCheckbox.setSelected(this.config.isClearGridOnClose());
        this.useInternalSearchRadio.setSelected(!this.config.isUseExternalSearch());
        this.useExternalSearchRadio.setSelected(this.config.isUseExternalSearch());
        this.rememberCheckbox.setSelected(this.config.isRememberLastSearch());
        this.autoFocusCheckbox.setSelected(this.config.isAutoFocusSearch());
        this.syncWithExternalCheckbox.setSelected(this.config.isSyncWithExternalSearch());
        this.clearExternalCheckbox.setSelected(this.config.isClearExternalSearchOnOpen());
        this.rememberCheckbox.visible = this.useInternalSearchRadio.isSelected();
        this.autoFocusCheckbox.visible = this.useInternalSearchRadio.isSelected();
        this.syncWithExternalCheckbox.visible = this.useInternalSearchRadio.isSelected();
        this.clearExternalCheckbox.visible = this.useExternalSearchRadio.isSelected();
    }

    private void save() {
        this.config.setUseExternalSearch(this.useExternalSearchRadio.isSelected());
        this.config.setRememberLastSearch(this.rememberCheckbox.isSelected());
        this.config.setAutoFocusSearch(this.autoFocusCheckbox.isSelected());
        this.config.setSyncWithExternalSearch(this.syncWithExternalCheckbox.isSelected());
        this.config.setClearExternalSearchOnOpen(this.clearExternalCheckbox.isSelected());
        this.config.setPinAutoCraftedItems(this.pinAutoCraftedItemsCheckbox.isSelected());
        this.config.setNotifyForFinishedCraftingJobs(this.notifyForFinishedCraftingJobsCheckbox.isSelected());
        this.config.setClearGridOnClose(this.clearGridOnCloseCheckbox.isSelected());
        this.updateState();
    }
}

