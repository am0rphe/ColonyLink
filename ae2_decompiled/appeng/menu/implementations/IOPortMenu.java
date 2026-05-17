/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.MenuType
 */
package appeng.menu.implementations;

import appeng.api.config.FullnessMode;
import appeng.api.config.OperationMode;
import appeng.api.config.Settings;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.util.IConfigManager;
import appeng.blockentity.storage.IOPortBlockEntity;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.slot.OutputSlot;
import appeng.menu.slot.RestrictedInputSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class IOPortMenu
extends UpgradeableMenu<IOPortBlockEntity> {
    public static final MenuType<IOPortMenu> TYPE = MenuTypeBuilder.create(IOPortMenu::new, IOPortBlockEntity.class).build("ioport");
    @GuiSync(value=2)
    public FullnessMode fMode = FullnessMode.EMPTY;
    @GuiSync(value=3)
    public OperationMode opMode = OperationMode.EMPTY;

    public IOPortMenu(int id, Inventory ip, IOPortBlockEntity host) {
        super((MenuType<?>)TYPE, id, ip, host);
    }

    @Override
    protected void setupConfig() {
        int i;
        InternalInventory cells = ((IOPortBlockEntity)this.getHost()).getSubInventory(ISegmentedInventory.CELLS);
        for (i = 0; i < 6; ++i) {
            this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.STORAGE_CELLS, cells, i), SlotSemantics.MACHINE_INPUT);
        }
        for (i = 0; i < 6; ++i) {
            this.addSlot(new OutputSlot(cells, 6 + i, RestrictedInputSlot.PlacableItemType.STORAGE_CELLS.icon), SlotSemantics.MACHINE_OUTPUT);
        }
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm) {
        this.setOperationMode(cm.getSetting(Settings.OPERATION_MODE));
        this.setFullMode(cm.getSetting(Settings.FULLNESS_MODE));
        this.setRedStoneMode(cm.getSetting(Settings.REDSTONE_CONTROLLED));
    }

    public FullnessMode getFullMode() {
        return this.fMode;
    }

    private void setFullMode(FullnessMode fMode) {
        this.fMode = fMode;
    }

    public OperationMode getOperationMode() {
        return this.opMode;
    }

    private void setOperationMode(OperationMode opMode) {
        this.opMode = opMode;
    }
}

