/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.ItemStack
 */
package appeng.blockentity.misc;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.blockentity.misc.CondenserBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

class CondenserMEStorage
implements MEStorage {
    private final CondenserBlockEntity target;

    CondenserMEStorage(CondenserBlockEntity te) {
        this.target = te;
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        MEStorage.checkPreconditions(what, amount, mode, source);
        if (!this.target.canAddOutput()) {
            return 0L;
        }
        if (mode == Actionable.MODULATE) {
            this.target.addPower(amount / (long)what.getAmountPerOperation());
        }
        return amount;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        AEItemKey itemKey;
        MEStorage.checkPreconditions(what, amount, mode, source);
        ItemStack slotItem = this.target.getOutputSlot().getStackInSlot(0);
        if (what instanceof AEItemKey && (itemKey = (AEItemKey)what).matches(slotItem)) {
            int count = (int)Math.min(amount, Integer.MAX_VALUE);
            return this.target.getOutputSlot().extractItem(0, count, mode == Actionable.SIMULATE).getCount();
        }
        return 0L;
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        ItemStack stack = this.target.getOutputSlot().getStackInSlot(0);
        if (!stack.isEmpty()) {
            out.add(AEItemKey.of(stack), stack.getCount());
        }
    }

    @Override
    public Component getDescription() {
        return this.target.getBlockState().getBlock().getName();
    }
}

