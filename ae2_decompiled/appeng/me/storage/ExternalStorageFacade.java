/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.primitives.Ints
 *  javax.annotation.Nullable
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.neoforge.fluids.FluidStack
 *  net.neoforged.neoforge.fluids.capability.IFluidHandler
 *  net.neoforged.neoforge.fluids.capability.IFluidHandler$FluidAction
 *  net.neoforged.neoforge.items.IItemHandler
 */
package appeng.me.storage;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import com.google.common.primitives.Ints;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

public abstract class ExternalStorageFacade
implements MEStorage {
    private static final long MAX_REPORTED_AMOUNT = 0x40000000000L;
    @Nullable
    private Runnable changeListener;
    protected boolean extractableOnly;

    public void setChangeListener(@Nullable Runnable listener) {
        this.changeListener = listener;
    }

    public abstract int getSlots();

    @Nullable
    public abstract GenericStack getStackInSlot(int var1);

    public abstract AEKeyType getKeyType();

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        int inserted = this.insertExternal(what, Ints.saturatedCast((long)amount), mode);
        if (inserted > 0 && mode == Actionable.MODULATE && this.changeListener != null) {
            this.changeListener.run();
        }
        return inserted;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        int extracted = this.extractExternal(what, Ints.saturatedCast((long)amount), mode);
        if (extracted > 0 && mode == Actionable.MODULATE && this.changeListener != null) {
            this.changeListener.run();
        }
        return extracted;
    }

    @Override
    public Component getDescription() {
        return GuiText.ExternalStorage.text(AEKeyType.fluids().getDescription());
    }

    protected abstract int insertExternal(AEKey var1, int var2, Actionable var3);

    protected abstract int extractExternal(AEKey var1, int var2, Actionable var3);

    public abstract boolean containsAnyFuzzy(Set<AEKey> var1);

    public static ExternalStorageFacade of(IFluidHandler handler) {
        return new FluidHandlerFacade(handler);
    }

    public static ExternalStorageFacade of(IItemHandler handler) {
        return new ItemHandlerFacade(handler);
    }

    public void setExtractableOnly(boolean extractableOnly) {
        this.extractableOnly = extractableOnly;
    }

    private static class FluidHandlerFacade
    extends ExternalStorageFacade {
        private final IFluidHandler handler;

        public FluidHandlerFacade(IFluidHandler handler) {
            this.handler = handler;
        }

        @Override
        public int getSlots() {
            return this.handler.getTanks();
        }

        @Override
        @Nullable
        public GenericStack getStackInSlot(int slot) {
            return GenericStack.fromFluidStack(this.handler.getFluidInTank(slot));
        }

        @Override
        public AEKeyType getKeyType() {
            return AEKeyType.fluids();
        }

        @Override
        protected int insertExternal(AEKey what, int amount, Actionable mode) {
            if (!(what instanceof AEFluidKey)) {
                return 0;
            }
            AEFluidKey fluidKey = (AEFluidKey)what;
            return this.handler.fill(fluidKey.toStack(amount), mode.getFluidAction());
        }

        @Override
        public int extractExternal(AEKey what, int amount, Actionable mode) {
            if (!(what instanceof AEFluidKey)) {
                return 0;
            }
            AEFluidKey fluidKey = (AEFluidKey)what;
            FluidStack fluidStack = fluidKey.toStack(Ints.saturatedCast((long)amount));
            FluidStack gathered = this.handler.drain(fluidStack, mode.getFluidAction());
            if (gathered.isEmpty()) {
                return 0;
            }
            return gathered.getAmount();
        }

        @Override
        public boolean containsAnyFuzzy(Set<AEKey> keys) {
            for (int i = 0; i < this.handler.getTanks(); ++i) {
                AEFluidKey what = AEFluidKey.of(this.handler.getFluidInTank(i));
                if (what == null || !keys.contains(what.dropSecondary())) continue;
                return true;
            }
            return false;
        }

        @Override
        public void getAvailableStacks(KeyCounter out) {
            for (int i = 0; i < this.handler.getTanks(); ++i) {
                FluidStack stack = this.handler.getFluidInTank(i);
                if (stack.isEmpty() || this.extractableOnly && this.handler.drain(stack, IFluidHandler.FluidAction.SIMULATE).isEmpty()) continue;
                out.add(AEFluidKey.of(stack), stack.getAmount());
            }
        }
    }

    private static class ItemHandlerFacade
    extends ExternalStorageFacade {
        private final IItemHandler handler;

        public ItemHandlerFacade(IItemHandler handler) {
            this.handler = handler;
        }

        @Override
        public int getSlots() {
            return this.handler.getSlots();
        }

        @Override
        @Nullable
        public GenericStack getStackInSlot(int slot) {
            return GenericStack.fromItemStack(this.handler.getStackInSlot(slot));
        }

        @Override
        public AEKeyType getKeyType() {
            return AEKeyType.items();
        }

        @Override
        public int insertExternal(AEKey what, int amount, Actionable mode) {
            ItemStack orgInput;
            if (!(what instanceof AEItemKey)) {
                return 0;
            }
            AEItemKey itemKey = (AEItemKey)what;
            ItemStack remaining = orgInput = itemKey.toStack(Ints.saturatedCast((long)amount));
            int slotCount = this.handler.getSlots();
            boolean simulate = mode == Actionable.SIMULATE;
            for (int i = 0; i < slotCount && !remaining.isEmpty(); ++i) {
                remaining = this.handler.insertItem(i, remaining, simulate);
            }
            if (remaining == orgInput) {
                return 0;
            }
            return amount - remaining.getCount();
        }

        @Override
        public int extractExternal(AEKey what, int amount, Actionable mode) {
            int extracted;
            if (!(what instanceof AEItemKey)) {
                return 0;
            }
            AEItemKey itemKey = (AEItemKey)what;
            int totalExtracted = 0;
            for (int i = 0; i < this.handler.getSlots() && amount != (totalExtracted += (extracted = ItemHandlerFacade.extractFromHandler(this.handler, i, itemKey, amount - totalExtracted, mode))); ++i) {
            }
            return totalExtracted;
        }

        private static int extractFromHandler(IItemHandler handler, int slot, AEItemKey itemKey, int maxExtract, Actionable actionable) {
            ItemStack stackInInventorySlot = handler.getStackInSlot(slot);
            if (!itemKey.matches(stackInInventorySlot)) {
                return 0;
            }
            return switch (actionable) {
                default -> throw new MatchException(null, null);
                case Actionable.SIMULATE -> {
                    int amountInSlot = stackInInventorySlot.getCount();
                    int extracted = ItemHandlerFacade.wrapHandlerExtract(handler, slot, maxExtract, true);
                    if (extracted == itemKey.getMaxStackSize() && maxExtract > itemKey.getMaxStackSize() && amountInSlot > itemKey.getMaxStackSize()) {
                        yield Math.min(amountInSlot, maxExtract);
                    }
                    yield extracted;
                }
                case Actionable.MODULATE -> {
                    int extracted;
                    int totalExtracted = 0;
                    while ((extracted = ItemHandlerFacade.wrapHandlerExtract(handler, slot, maxExtract - totalExtracted, false)) > 0) {
                        totalExtracted += extracted;
                    }
                    yield totalExtracted;
                }
            };
        }

        private static int wrapHandlerExtract(IItemHandler handler, int slot, int maxExtract, boolean simulate) {
            int extracted = handler.extractItem(slot, maxExtract, simulate).getCount();
            if (extracted > maxExtract) {
                AELog.warn("Mod that provided item handler %s is broken. Returned %d items while only requesting %d.", handler.getClass().getName(), extracted, maxExtract);
                return maxExtract;
            }
            return extracted;
        }

        @Override
        public boolean containsAnyFuzzy(Set<AEKey> keys) {
            for (int i = 0; i < this.handler.getSlots(); ++i) {
                AEItemKey what = AEItemKey.of(this.handler.getStackInSlot(i));
                if (what == null || !keys.contains(what.dropSecondary())) continue;
                return true;
            }
            return false;
        }

        @Override
        public void getAvailableStacks(KeyCounter out) {
            for (int i = 0; i < this.handler.getSlots(); ++i) {
                ItemStack stack = this.handler.getStackInSlot(i);
                if (stack.isEmpty() || this.extractableOnly && this.handler.extractItem(i, 1, true).isEmpty() && this.handler.extractItem(i, stack.getCount(), true).isEmpty()) continue;
                out.add(AEItemKey.of(stack), stack.getCount());
            }
        }
    }
}

