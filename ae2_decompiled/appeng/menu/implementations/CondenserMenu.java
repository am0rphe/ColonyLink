/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.MenuType
 */
package appeng.menu.implementations;

import appeng.api.config.CondenserOutput;
import appeng.api.config.Settings;
import appeng.api.inventories.InternalInventory;
import appeng.blockentity.misc.CondenserBlockEntity;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.interfaces.IProgressProvider;
import appeng.menu.slot.OutputSlot;
import appeng.menu.slot.RestrictedInputSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class CondenserMenu
extends AEBaseMenu
implements IProgressProvider {
    public static final MenuType<CondenserMenu> TYPE = MenuTypeBuilder.create(CondenserMenu::new, CondenserBlockEntity.class).build("condenser");
    private final CondenserBlockEntity condenser;
    @GuiSync(value=0)
    public long requiredEnergy = 0L;
    @GuiSync(value=1)
    public long storedPower = 0L;
    @GuiSync(value=2)
    public CondenserOutput output = CondenserOutput.TRASH;

    public CondenserMenu(int id, Inventory ip, CondenserBlockEntity condenser) {
        super(TYPE, id, ip, condenser);
        this.condenser = condenser;
        InternalInventory inv = condenser.getInternalInventory();
        this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.TRASH, inv, 0), SlotSemantics.MACHINE_INPUT);
        this.addSlot(new OutputSlot(inv, 1, null), SlotSemantics.MACHINE_OUTPUT);
        this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.STORAGE_COMPONENT, inv, 2).setStackLimit(1), SlotSemantics.STORAGE_CELL);
        this.createPlayerInventorySlots(ip);
    }

    @Override
    public void broadcastChanges() {
        if (this.isServerSide()) {
            double maxStorage = this.condenser.getStorage();
            double requiredEnergy = this.condenser.getRequiredPower();
            this.requiredEnergy = requiredEnergy == 0.0 ? (long)((int)maxStorage) : (long)((int)Math.min(requiredEnergy, maxStorage));
            this.storedPower = (int)this.condenser.getStoredPower();
            this.output = this.condenser.getConfigManager().getSetting(Settings.CONDENSER_OUTPUT);
        }
        super.broadcastChanges();
    }

    @Override
    public int getCurrentProgress() {
        return (int)this.storedPower;
    }

    @Override
    public int getMaxProgress() {
        return (int)this.requiredEnergy;
    }

    public CondenserOutput getOutput() {
        return this.output;
    }
}

