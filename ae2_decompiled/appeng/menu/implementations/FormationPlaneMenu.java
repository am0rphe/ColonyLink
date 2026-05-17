/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.MenuType
 */
package appeng.menu.implementations;

import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.stacks.AEKey;
import appeng.api.util.IConfigManager;
import appeng.core.definitions.AEItems;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.parts.automation.FormationPlanePart;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class FormationPlaneMenu
extends UpgradeableMenu<FormationPlanePart> {
    public static final MenuType<FormationPlaneMenu> TYPE = MenuTypeBuilder.create(FormationPlaneMenu::new, FormationPlanePart.class).build("formationplane");
    @GuiSync(value=7)
    public YesNo placeMode;

    public FormationPlaneMenu(MenuType<FormationPlaneMenu> type, int id, Inventory ip, FormationPlanePart host) {
        super((MenuType<?>)type, id, ip, host);
    }

    @Override
    protected void setupConfig() {
        this.addExpandableConfigSlots(((FormationPlanePart)this.getHost()).getConfig(), 2, 9, 5);
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm) {
        if (this.supportsFuzzyRangeSearch()) {
            this.setFuzzyMode(cm.getSetting(Settings.FUZZY_MODE));
        }
        this.setPlaceMode(cm.getSetting(Settings.PLACE_BLOCK));
    }

    @Override
    public boolean isSlotEnabled(int idx) {
        int upgrades = this.getUpgrades().getInstalledUpgrades(AEItems.CAPACITY_CARD);
        return upgrades > idx;
    }

    public YesNo getPlaceMode() {
        return this.placeMode;
    }

    private void setPlaceMode(YesNo placeMode) {
        this.placeMode = placeMode;
    }

    public boolean supportsFuzzyMode() {
        return this.hasUpgrade(AEItems.FUZZY_CARD) && this.supportsFuzzyRangeSearch();
    }

    private boolean supportsFuzzyRangeSearch() {
        for (AEKey key : ((FormationPlanePart)this.getHost()).getConfig().keySet()) {
            if (!key.supportsFuzzyRangeSearch()) continue;
            return true;
        }
        return false;
    }
}

