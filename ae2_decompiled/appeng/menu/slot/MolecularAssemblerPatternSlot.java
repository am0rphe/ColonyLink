/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 */
package appeng.menu.slot;

import appeng.api.inventories.InternalInventory;
import appeng.blockentity.crafting.IMolecularAssemblerSupportedPattern;
import appeng.blockentity.crafting.MolecularAssemblerBlockEntity;
import appeng.client.Point;
import appeng.menu.implementations.MolecularAssemblerMenu;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.IOptionalSlot;
import net.minecraft.world.item.ItemStack;

public class MolecularAssemblerPatternSlot
extends AppEngSlot
implements IOptionalSlot {
    private final MolecularAssemblerMenu mac;

    public MolecularAssemblerPatternSlot(MolecularAssemblerMenu mac, InternalInventory inv, int invSlot) {
        super(inv, invSlot);
        this.mac = mac;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return super.mayPlace(stack) && this.mac.isValidItemForSlot(this.getSlotIndex(), stack);
    }

    @Override
    protected boolean getCurrentValidationState() {
        ItemStack stack = this.getItem();
        return stack.isEmpty() || this.mayPlace(stack);
    }

    @Override
    public boolean isRenderDisabled() {
        return true;
    }

    @Override
    public boolean isSlotEnabled() {
        if (!this.getInventory().getStackInSlot(this.slot).isEmpty()) {
            return true;
        }
        IMolecularAssemblerSupportedPattern pattern = ((MolecularAssemblerBlockEntity)this.mac.getHost()).getCurrentPattern();
        return this.slot >= 0 && this.slot < 9 && pattern != null && pattern.isSlotEnabled(this.slot);
    }

    @Override
    public Point getBackgroundPos() {
        return new Point(this.x - 1, this.y - 1);
    }
}

