/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 */
package appeng.menu.implementations;

import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.AEItemKey;
import appeng.blockentity.crafting.IMolecularAssemblerSupportedPattern;
import appeng.blockentity.crafting.MolecularAssemblerBlockEntity;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.interfaces.IProgressProvider;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.MolecularAssemblerPatternSlot;
import appeng.menu.slot.OutputSlot;
import appeng.menu.slot.RestrictedInputSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class MolecularAssemblerMenu
extends UpgradeableMenu<MolecularAssemblerBlockEntity>
implements IProgressProvider {
    public static final MenuType<MolecularAssemblerMenu> TYPE = MenuTypeBuilder.create(MolecularAssemblerMenu::new, MolecularAssemblerBlockEntity.class).build("molecular_assembler");
    private static final int MAX_CRAFT_PROGRESS = 100;
    private final MolecularAssemblerBlockEntity molecularAssembler;
    @GuiSync(value=4)
    public int craftProgress = 0;
    private Slot encodedPatternSlot;

    public MolecularAssemblerMenu(int id, Inventory playerInv, MolecularAssemblerBlockEntity be) {
        super((MenuType<?>)TYPE, id, playerInv, be);
        this.molecularAssembler = be;
    }

    public boolean isValidItemForSlot(int slotIndex, ItemStack i) {
        IMolecularAssemblerSupportedPattern details = this.molecularAssembler.getCurrentPattern();
        if (details != null) {
            return details.isItemValid(slotIndex, AEItemKey.of(i), this.molecularAssembler.getLevel());
        }
        return false;
    }

    @Override
    protected void setupConfig() {
        InternalInventory mac = ((MolecularAssemblerBlockEntity)this.getHost()).getSubInventory(MolecularAssemblerBlockEntity.INV_MAIN);
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new MolecularAssemblerPatternSlot(this, mac, i), SlotSemantics.MACHINE_CRAFTING_GRID);
        }
        this.encodedPatternSlot = this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.MOLECULAR_ASSEMBLER_PATTERN, mac, 10), SlotSemantics.ENCODED_PATTERN);
        this.addSlot(new OutputSlot(mac, 9, null), SlotSemantics.MACHINE_OUTPUT);
    }

    @Override
    public void broadcastChanges() {
        this.craftProgress = this.molecularAssembler.getCraftingProgress();
        this.standardDetectAndSendChanges();
    }

    @Override
    public int getCurrentProgress() {
        return this.craftProgress;
    }

    @Override
    public int getMaxProgress() {
        return 100;
    }

    @Override
    public void onSlotChange(Slot s) {
        if (s == this.encodedPatternSlot) {
            for (Slot otherSlot : this.slots) {
                if (otherSlot == s || !(otherSlot instanceof AppEngSlot)) continue;
                ((AppEngSlot)otherSlot).resetCachedValidation();
            }
        }
    }
}

