/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.neoforge.network.PacketDistributor
 */
package appeng.menu.slot;

import appeng.api.config.Actionable;
import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.GenericStack;
import appeng.core.network.serverbound.InventoryActionPacket;
import appeng.helpers.InventoryAction;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.menu.slot.AppEngSlot;
import appeng.util.ConfigMenuInventory;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class FakeSlot
extends AppEngSlot {
    public FakeSlot(InternalInventory inv, int invSlot) {
        super(inv, invSlot);
    }

    public void onTake(Player player, ItemStack stack) {
    }

    @Override
    public ItemStack remove(int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public void set(ItemStack is) {
        if (!this.canSetFilterTo(is)) {
            return;
        }
        if (!is.isEmpty()) {
            is = is.copy();
        }
        super.set(is);
    }

    @Override
    public boolean mayPickup(Player player) {
        return false;
    }

    public boolean canSetFilterTo(ItemStack stack) {
        return this.slot < this.getInventory().size() && this.getInventory().isItemValid(this.slot, stack);
    }

    public void setFilterTo(ItemStack itemStack) {
        InventoryActionPacket message = new InventoryActionPacket(InventoryAction.SET_FILTER, this.index, itemStack);
        PacketDistributor.sendToServer((CustomPacketPayload)message, (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    public void increase(ItemStack is) {
        GenericStack newFilter;
        ConfigMenuInventory configInv;
        GenericStackInv realInv;
        InternalInventory internalInventory = this.getInventory();
        if (internalInventory instanceof ConfigMenuInventory && (realInv = (configInv = (ConfigMenuInventory)internalInventory).getDelegate()).getMode() == GenericStackInv.Mode.CONFIG_STACKS && (newFilter = configInv.convertToSuitableStack(is)) != null && newFilter.what().equals(realInv.getKey(this.slot))) {
            realInv.insert(this.slot, newFilter.what(), newFilter.amount(), Actionable.MODULATE);
            return;
        }
        this.set(is);
    }

    public void decrease(ItemStack is) {
        GenericStack newFilter;
        ConfigMenuInventory configInv;
        GenericStackInv realInv;
        InternalInventory internalInventory = this.getInventory();
        if (internalInventory instanceof ConfigMenuInventory && (realInv = (configInv = (ConfigMenuInventory)internalInventory).getDelegate()).getMode() == GenericStackInv.Mode.CONFIG_STACKS && (newFilter = configInv.convertToSuitableStack(is)) != null) {
            realInv.extract(this.slot, newFilter.what(), newFilter.amount(), Actionable.MODULATE);
            return;
        }
        ItemStack current = this.getItem();
        if (is.isEmpty()) {
            current = current.copy();
            current.shrink(1);
            this.set(current);
        } else if (ItemStack.isSameItemSameComponents((ItemStack)current, (ItemStack)is)) {
            current = current.copy();
            current.grow(1);
            this.set(current);
        } else {
            is = is.copy();
            is.setCount(1);
            this.set(is);
        }
    }
}

