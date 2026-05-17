/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.neoforge.capabilities.Capabilities$ItemHandler
 *  net.neoforged.neoforge.items.IItemHandler
 *  net.neoforged.neoforge.items.ItemHandlerHelper
 */
package appeng.parts.p2p;

import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.stacks.AEKeyType;
import appeng.core.AppEng;
import appeng.items.parts.PartModels;
import appeng.parts.p2p.CapabilityP2PTunnelPart;
import appeng.parts.p2p.P2PModels;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public class ItemP2PTunnelPart
extends CapabilityP2PTunnelPart<ItemP2PTunnelPart, IItemHandler> {
    private static final P2PModels MODELS = new P2PModels(AppEng.makeId("part/p2p/p2p_tunnel_items"));
    private static final IItemHandler NULL_ITEM_HANDLER = new NullItemHandler();

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    public ItemP2PTunnelPart(IPartItem<?> partItem) {
        super(partItem, Capabilities.ItemHandler.BLOCK);
        this.inputHandler = new InputItemHandler();
        this.outputHandler = new OutputItemHandler();
        this.emptyHandler = NULL_ITEM_HANDLER;
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

    private class InputItemHandler
    implements IItemHandler {
        private InputItemHandler() {
        }

        public int getSlots() {
            return 1;
        }

        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            int remainder = stack.getCount();
            int outputTunnels = ItemP2PTunnelPart.this.getOutputs().size();
            int amount = stack.getCount();
            if (outputTunnels == 0 || amount == 0) {
                return stack;
            }
            int amountPerOutput = amount / outputTunnels;
            int overflow = amountPerOutput == 0 ? amount : amount % amountPerOutput;
            for (ItemP2PTunnelPart target : ItemP2PTunnelPart.this.getOutputs()) {
                CapabilityP2PTunnelPart.CapabilityGuard capabilityGuard = target.getAdjacentCapability();
                try {
                    IItemHandler output = (IItemHandler)capabilityGuard.get();
                    int toSend = amountPerOutput + overflow;
                    if (toSend <= 0) break;
                    ItemStack stackCopy = stack.copy();
                    stackCopy.setCount(toSend);
                    int sent = toSend - ItemHandlerHelper.insertItem((IItemHandler)output, (ItemStack)stackCopy, (boolean)simulate).getCount();
                    overflow = toSend - sent;
                    remainder -= sent;
                }
                finally {
                    if (capabilityGuard == null) continue;
                    capabilityGuard.close();
                }
            }
            if (!simulate) {
                ItemP2PTunnelPart.this.deductTransportCost(amount - remainder, AEKeyType.items());
            }
            if (remainder == stack.getCount()) {
                return stack;
            }
            if (remainder == 0) {
                return ItemStack.EMPTY;
            }
            ItemStack copy = stack.copy();
            copy.setCount(remainder);
            return copy;
        }

        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        public int getSlotLimit(int slot) {
            return Integer.MAX_VALUE;
        }

        public boolean isItemValid(int slot, ItemStack stack) {
            return true;
        }
    }

    private class OutputItemHandler
    implements IItemHandler {
        private OutputItemHandler() {
        }

        public int getSlots() {
            try (CapabilityP2PTunnelPart.CapabilityGuard input = ItemP2PTunnelPart.this.getInputCapability();){
                int n = ((IItemHandler)input.get()).getSlots();
                return n;
            }
        }

        public ItemStack getStackInSlot(int slot) {
            try (CapabilityP2PTunnelPart.CapabilityGuard input = ItemP2PTunnelPart.this.getInputCapability();){
                ItemStack itemStack = ((IItemHandler)input.get()).getStackInSlot(slot);
                return itemStack;
            }
        }

        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack;
        }

        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            try (CapabilityP2PTunnelPart.CapabilityGuard input = ItemP2PTunnelPart.this.getInputCapability();){
                ItemStack result = ((IItemHandler)input.get()).extractItem(slot, amount, simulate);
                if (!simulate) {
                    ItemP2PTunnelPart.this.deductTransportCost(result.getCount(), AEKeyType.items());
                }
                ItemStack itemStack = result;
                return itemStack;
            }
        }

        public int getSlotLimit(int slot) {
            try (CapabilityP2PTunnelPart.CapabilityGuard input = ItemP2PTunnelPart.this.getInputCapability();){
                int n = ((IItemHandler)input.get()).getSlotLimit(slot);
                return n;
            }
        }

        public boolean isItemValid(int slot, ItemStack stack) {
            try (CapabilityP2PTunnelPart.CapabilityGuard input = ItemP2PTunnelPart.this.getInputCapability();){
                boolean bl = ((IItemHandler)input.get()).isItemValid(slot, stack);
                return bl;
            }
        }
    }

    private static class NullItemHandler
    implements IItemHandler {
        private NullItemHandler() {
        }

        public int getSlots() {
            return 0;
        }

        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack;
        }

        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        public int getSlotLimit(int slot) {
            return 0;
        }

        public boolean isItemValid(int slot, ItemStack stack) {
            return false;
        }
    }
}

