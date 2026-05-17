/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.MenuType
 */
package appeng.menu.implementations;

import appeng.blockentity.misc.VibrationChamberBlockEntity;
import appeng.core.definitions.AEItems;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.interfaces.IProgressProvider;
import appeng.menu.slot.RestrictedInputSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class VibrationChamberMenu
extends UpgradeableMenu<VibrationChamberBlockEntity>
implements IProgressProvider {
    public static final MenuType<VibrationChamberMenu> TYPE = MenuTypeBuilder.create(VibrationChamberMenu::new, VibrationChamberBlockEntity.class).build("vibrationchamber");
    private final VibrationChamberBlockEntity vibrationChamber;
    @GuiSync(value=2)
    public double currentFuelTicksPerTick = 0.0;
    @GuiSync(value=3)
    public int remainingBurnTime = 0;

    public VibrationChamberMenu(int id, Inventory ip, VibrationChamberBlockEntity vibrationChamber) {
        super((MenuType<?>)TYPE, id, ip, vibrationChamber);
        this.vibrationChamber = vibrationChamber;
        this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.FUEL, vibrationChamber.getInternalInventory(), 0), SlotSemantics.MACHINE_INPUT);
    }

    @Override
    public void broadcastChanges() {
        if (this.isServerSide()) {
            this.remainingBurnTime = this.vibrationChamber.getFuelItemFuelTicks() <= 0.0 ? 0 : (int)(100.0 * this.vibrationChamber.getRemainingFuelTicks() / this.vibrationChamber.getFuelItemFuelTicks());
            this.currentFuelTicksPerTick = this.remainingBurnTime <= 0 ? 0.0 : this.vibrationChamber.getCurrentFuelTicksPerTick();
        }
        super.broadcastChanges();
    }

    @Override
    public int getCurrentProgress() {
        return (int)(this.currentFuelTicksPerTick * 100.0);
    }

    public int getRemainingBurnTime() {
        return this.remainingBurnTime;
    }

    @Override
    public int getMaxProgress() {
        return (int)(this.vibrationChamber.getMaxFuelTicksPerTick() * 100.0);
    }

    public double getPowerPerTick() {
        return this.currentFuelTicksPerTick * this.vibrationChamber.getEnergyPerFuelTick();
    }

    public double getFuelEfficiency() {
        return 100 + this.vibrationChamber.getInstalledUpgrades(AEItems.ENERGY_CARD) * 50;
    }
}

