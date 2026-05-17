/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.primitives.Ints
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.neoforge.capabilities.Capabilities$FluidHandler
 *  net.neoforged.neoforge.fluids.capability.IFluidHandlerItem
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.behaviors;

import appeng.api.behaviors.ContainerItemStrategy;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.GenericStack;
import appeng.util.GenericContainerHelper;
import appeng.util.fluid.FluidSoundHelper;
import com.google.common.primitives.Ints;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.Nullable;

class FluidContainerItemStrategy
implements ContainerItemStrategy<AEFluidKey, Context> {
    FluidContainerItemStrategy() {
    }

    @Override
    @Nullable
    public GenericStack getContainedStack(ItemStack stack) {
        return GenericContainerHelper.getContainedFluidStack(stack);
    }

    @Override
    @Nullable
    public Context findCarriedContext(Player player, AbstractContainerMenu menu) {
        if (menu.getCarried().getCapability(Capabilities.FluidHandler.ITEM) != null) {
            return new CarriedContext(player, menu);
        }
        return null;
    }

    @Override
    @Nullable
    public Context findPlayerSlotContext(Player player, int slot) {
        if (player.getInventory().getItem(slot).getCapability(Capabilities.FluidHandler.ITEM) != null) {
            return new PlayerInvContext(player, slot);
        }
        return null;
    }

    @Override
    public long extract(Context context, AEFluidKey what, long amount, Actionable mode) {
        ItemStack stack = context.getStack();
        ItemStack copy = stack.copyWithCount(1);
        IFluidHandlerItem fluidHandler = (IFluidHandlerItem)copy.getCapability(Capabilities.FluidHandler.ITEM);
        if (fluidHandler == null) {
            return 0L;
        }
        int extracted = fluidHandler.drain(what.toStack(Ints.saturatedCast((long)amount)), mode.getFluidAction()).getAmount();
        if (mode == Actionable.MODULATE) {
            stack.shrink(1);
            context.addOverflow(fluidHandler.getContainer());
        }
        return extracted;
    }

    @Override
    public long insert(Context context, AEFluidKey what, long amount, Actionable mode) {
        ItemStack stack = context.getStack();
        ItemStack copy = stack.copyWithCount(1);
        IFluidHandlerItem fluidHandler = (IFluidHandlerItem)copy.getCapability(Capabilities.FluidHandler.ITEM);
        if (fluidHandler == null) {
            return 0L;
        }
        int filled = fluidHandler.fill(what.toStack(Ints.saturatedCast((long)amount)), mode.getFluidAction());
        if (mode == Actionable.MODULATE) {
            stack.shrink(1);
            context.addOverflow(fluidHandler.getContainer());
        }
        return filled;
    }

    @Override
    public void playFillSound(Player player, AEFluidKey what) {
        FluidSoundHelper.playFillSound(player, what);
    }

    @Override
    public void playEmptySound(Player player, AEFluidKey what) {
        FluidSoundHelper.playEmptySound(player, what);
    }

    @Override
    @Nullable
    public GenericStack getExtractableContent(Context context) {
        return this.getContainedStack(context.getStack());
    }

    private record CarriedContext(Player player, AbstractContainerMenu menu) implements Context
    {
        @Override
        public ItemStack getStack() {
            return this.menu.getCarried();
        }

        @Override
        public void setStack(ItemStack stack) {
            this.menu.setCarried(stack);
        }

        @Override
        public void addOverflow(ItemStack stack) {
            if (this.menu.getCarried().isEmpty()) {
                this.menu.setCarried(stack);
            } else {
                this.player.getInventory().placeItemBackInInventory(stack);
            }
        }
    }

    private record PlayerInvContext(Player player, int slot) implements Context
    {
        @Override
        public ItemStack getStack() {
            return this.player.getInventory().getItem(this.slot);
        }

        @Override
        public void setStack(ItemStack stack) {
            this.player.getInventory().setItem(this.slot, stack);
        }

        @Override
        public void addOverflow(ItemStack stack) {
            this.player.getInventory().placeItemBackInInventory(stack);
        }
    }

    static interface Context {
        public ItemStack getStack();

        public void setStack(ItemStack var1);

        public void addOverflow(ItemStack var1);
    }
}

