/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Inventory
 */
package appeng.client.gui.implementations;

import appeng.api.config.FuzzyMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.client.gui.NumberEntryType;
import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.NumberEntryWidget;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.core.definitions.AEItems;
import appeng.menu.implementations.StorageLevelEmitterMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class StorageLevelEmitterScreen
extends UpgradeableScreen<StorageLevelEmitterMenu> {
    private final SettingToggleButton<YesNo> craftingMode;
    private final SettingToggleButton<RedstoneMode> redstoneMode = new ServerSettingToggleButton<RedstoneMode>(Settings.REDSTONE_EMITTER, RedstoneMode.LOW_SIGNAL);
    private final SettingToggleButton<FuzzyMode> fuzzyMode = new ServerSettingToggleButton<FuzzyMode>(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);
    private final NumberEntryWidget level;

    public StorageLevelEmitterScreen(StorageLevelEmitterMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.craftingMode = new ServerSettingToggleButton<YesNo>(Settings.CRAFT_VIA_REDSTONE, YesNo.NO);
        this.addToLeftToolbar(this.redstoneMode);
        this.addToLeftToolbar(this.craftingMode);
        this.addToLeftToolbar(this.fuzzyMode);
        this.level = this.widgets.addNumberEntryWidget("level", NumberEntryType.of(menu.getConfiguredFilter()));
        this.level.setTextFieldStyle(style.getWidget("levelInput"));
        this.level.setLongValue(((StorageLevelEmitterMenu)this.menu).getCurrentValue());
        this.level.setOnChange(this::saveReportingValue);
        this.level.setOnConfirm(() -> ((StorageLevelEmitterScreen)this).onClose());
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        this.level.setType(NumberEntryType.of(((StorageLevelEmitterMenu)this.menu).getConfiguredFilter()));
        this.fuzzyMode.set(((StorageLevelEmitterMenu)this.menu).getFuzzyMode());
        this.fuzzyMode.setVisibility(((StorageLevelEmitterMenu)this.menu).supportsFuzzySearch());
        boolean notCraftingMode = !((StorageLevelEmitterMenu)this.menu).hasUpgrade(AEItems.CRAFTING_CARD);
        this.level.setActive(notCraftingMode);
        this.redstoneMode.active = notCraftingMode;
        this.redstoneMode.set(((StorageLevelEmitterMenu)this.menu).getRedStoneMode());
        this.redstoneMode.setVisibility(notCraftingMode);
        this.craftingMode.set(((StorageLevelEmitterMenu)this.menu).getCraftingMode());
        this.craftingMode.setVisibility(!notCraftingMode);
    }

    private void saveReportingValue() {
        this.level.getLongValue().ifPresent(((StorageLevelEmitterMenu)this.menu)::setValue);
    }
}

