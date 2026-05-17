/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.primitives.Ints
 *  javax.annotation.Nullable
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.neoforge.fluids.FluidStack
 *  net.neoforged.neoforge.fluids.capability.IFluidHandler
 *  net.neoforged.neoforge.items.IItemHandler
 *  net.neoforged.neoforge.items.ItemHandlerHelper
 *  org.jetbrains.annotations.Nullable
 */
package appeng.parts.automation;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.me.storage.ExternalStorageFacade;
import com.google.common.primitives.Ints;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

public abstract class HandlerStrategy<C, S> {
    private final AEKeyType keyType;
    public static final HandlerStrategy<IItemHandler, ItemStack> ITEMS = new HandlerStrategy<IItemHandler, ItemStack>(AEKeyType.items()){

        @Override
        public boolean isSupported(AEKey what) {
            return AEItemKey.is(what);
        }

        @Override
        public ExternalStorageFacade getFacade(IItemHandler handler) {
            return ExternalStorageFacade.of(handler);
        }

        @Override
        public long insert(IItemHandler handler, AEKey what, long amount, Actionable mode) {
            if (what instanceof AEItemKey) {
                AEItemKey itemKey = (AEItemKey)what;
                ItemStack stack = itemKey.toStack(Ints.saturatedCast((long)amount));
                ItemStack remainder = ItemHandlerHelper.insertItem((IItemHandler)handler, (ItemStack)stack, (boolean)mode.isSimulate());
                return amount - (long)remainder.getCount();
            }
            return 0L;
        }

        @Override
        @Nullable
        public ItemStack getStack(AEKey what, long amount) {
            if (what instanceof AEItemKey) {
                AEItemKey itemKey = (AEItemKey)what;
                return itemKey.toStack(Ints.saturatedCast((long)amount));
            }
            return null;
        }
    };
    public static final HandlerStrategy<IFluidHandler, FluidStack> FLUIDS = new HandlerStrategy<IFluidHandler, FluidStack>(AEKeyType.fluids()){

        @Override
        public boolean isSupported(AEKey what) {
            return AEFluidKey.is(what);
        }

        @Override
        public ExternalStorageFacade getFacade(IFluidHandler handler) {
            return ExternalStorageFacade.of(handler);
        }

        @Override
        public long insert(IFluidHandler handler, AEKey what, long amount, Actionable mode) {
            if (what instanceof AEFluidKey) {
                AEFluidKey itemKey = (AEFluidKey)what;
                if (amount > 0L) {
                    FluidStack stack = itemKey.toStack(Ints.saturatedCast((long)amount));
                    return handler.fill(stack, mode.getFluidAction());
                }
            }
            return 0L;
        }

        @Override
        public FluidStack getStack(AEKey what, long amount) {
            if (what instanceof AEFluidKey) {
                AEFluidKey fluidKey = (AEFluidKey)what;
                return fluidKey.toStack(Ints.saturatedCast((long)amount));
            }
            return null;
        }
    };

    public HandlerStrategy(AEKeyType keyType) {
        this.keyType = keyType;
    }

    public boolean isSupported(AEKey what) {
        return what.getType() == this.keyType;
    }

    public AEKeyType getKeyType() {
        return this.keyType;
    }

    public abstract ExternalStorageFacade getFacade(C var1);

    @javax.annotation.Nullable
    public abstract S getStack(AEKey var1, long var2);

    public abstract long insert(C var1, AEKey var2, long var3, Actionable var5);
}

