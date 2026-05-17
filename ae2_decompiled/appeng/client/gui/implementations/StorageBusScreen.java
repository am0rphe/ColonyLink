/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Inventory
 */
package appeng.client.gui.implementations;

import appeng.api.config.AccessRestriction;
import appeng.api.config.ActionItems;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.api.config.StorageFilter;
import appeng.api.config.YesNo;
import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.Color;
import appeng.client.gui.style.PaletteColor;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ActionButton;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.core.localization.GuiText;
import appeng.menu.implementations.StorageBusMenu;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class StorageBusScreen
extends UpgradeableScreen<StorageBusMenu> {
    private final SettingToggleButton<AccessRestriction> rwMode;
    private final SettingToggleButton<StorageFilter> storageFilter;
    private final SettingToggleButton<YesNo> filterOnExtract;
    private final SettingToggleButton<FuzzyMode> fuzzyMode;

    public StorageBusScreen(StorageBusMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.widgets.addOpenPriorityButton();
        this.addToLeftToolbar(new ActionButton(ActionItems.CLOSE, btn -> menu.clear()));
        this.addToLeftToolbar(new ActionButton(ActionItems.COG, btn -> menu.partition()));
        this.rwMode = new ServerSettingToggleButton<AccessRestriction>(Settings.ACCESS, AccessRestriction.READ_WRITE);
        this.storageFilter = new ServerSettingToggleButton<StorageFilter>(Settings.STORAGE_FILTER, StorageFilter.EXTRACTABLE_ONLY);
        this.filterOnExtract = new ServerSettingToggleButton<YesNo>(Settings.FILTER_ON_EXTRACT, YesNo.YES);
        this.fuzzyMode = new ServerSettingToggleButton<FuzzyMode>(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);
        this.addToLeftToolbar(this.storageFilter);
        this.addToLeftToolbar(this.filterOnExtract);
        this.addToLeftToolbar(this.fuzzyMode);
        this.addToLeftToolbar(this.rwMode);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        this.storageFilter.set(((StorageBusMenu)this.menu).getStorageFilter());
        this.rwMode.set(((StorageBusMenu)this.menu).getReadWriteMode());
        this.filterOnExtract.set(((StorageBusMenu)this.menu).getFilterOnExtract());
        this.fuzzyMode.set(((StorageBusMenu)this.menu).getFuzzyMode());
        this.fuzzyMode.setVisibility(((StorageBusMenu)this.menu).supportsFuzzySearch());
    }

    @Override
    public void drawFG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY) {
        super.drawFG(guiGraphics, offsetX, offsetY, mouseX, mouseY);
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(10.0f, 17.0f, 0.0f);
        poseStack.scale(0.6f, 0.6f, 1.0f);
        Color color = this.style.getColor(PaletteColor.DEFAULT_TEXT_COLOR);
        if (((StorageBusMenu)this.menu).getConnectedTo() != null) {
            guiGraphics.drawString(this.font, (Component)GuiText.AttachedTo.text(((StorageBusMenu)this.menu).getConnectedTo()), 0, 0, color.toARGB(), false);
        } else {
            guiGraphics.drawString(this.font, (Component)GuiText.Unattached.text(), 0, 0, color.toARGB(), false);
        }
        poseStack.popPose();
    }
}

