/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.inventory.Slot
 *  org.jetbrains.annotations.Nullable
 */
package appeng.menu.implementations;

import appeng.api.config.Settings;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.util.IConfigManager;
import appeng.core.definitions.AEItems;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.slot.FakeSlot;
import appeng.parts.automation.StorageLevelEmitterPart;
import appeng.util.ConfigMenuInventory;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;

public class StorageLevelEmitterMenu
extends UpgradeableMenu<StorageLevelEmitterPart> {
    private static final String ACTION_SET_REPORTING_VALUE = "setReportingValue";
    public static final MenuType<StorageLevelEmitterMenu> TYPE = MenuTypeBuilder.create(StorageLevelEmitterMenu::new, StorageLevelEmitterPart.class).withInitialData((host, buffer) -> {
        GenericStack.writeBuffer(host.getConfig().getStack(0), buffer);
        buffer.writeVarLong(host.getReportingValue());
    }, (host, menu, buffer) -> {
        ((StorageLevelEmitterPart)menu.getHost()).getConfig().setStack(0, GenericStack.readBuffer(buffer));
        menu.currentValue = buffer.readVarLong();
    }).build("storage_level_emitter");
    private long currentValue;

    public StorageLevelEmitterMenu(MenuType<StorageLevelEmitterMenu> menuType, int id, Inventory ip, StorageLevelEmitterPart te) {
        super((MenuType<?>)menuType, id, ip, te);
        this.registerClientAction(ACTION_SET_REPORTING_VALUE, Long.class, this::setValue);
    }

    public long getCurrentValue() {
        return this.currentValue;
    }

    public void setValue(long initialValue) {
        if (this.isClientSide()) {
            if (initialValue != this.currentValue) {
                this.currentValue = initialValue;
                this.sendClientAction(ACTION_SET_REPORTING_VALUE, initialValue);
            }
        } else {
            ((StorageLevelEmitterPart)this.getHost()).setReportingValue(initialValue);
        }
    }

    @Override
    protected void setupConfig() {
        ConfigMenuInventory inv = ((StorageLevelEmitterPart)this.getHost()).getConfig().createMenuWrapper();
        this.addSlot(new FakeSlot(inv, 0), SlotSemantics.CONFIG);
    }

    @Override
    public void onSlotChange(Slot s) {
        super.onSlotChange(s);
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm) {
        this.setCraftingMode(cm.getSetting(Settings.CRAFT_VIA_REDSTONE));
        if (cm.hasSetting(Settings.FUZZY_MODE)) {
            this.setFuzzyMode(cm.getSetting(Settings.FUZZY_MODE));
        }
        this.setRedStoneMode(cm.getSetting(Settings.REDSTONE_EMITTER));
    }

    public boolean supportsFuzzySearch() {
        return ((StorageLevelEmitterPart)this.getHost()).getConfigManager().hasSetting(Settings.FUZZY_MODE) && this.hasUpgrade(AEItems.FUZZY_CARD);
    }

    @Nullable
    public AEKey getConfiguredFilter() {
        return ((StorageLevelEmitterPart)this.getHost()).getConfig().getKey(0);
    }
}

