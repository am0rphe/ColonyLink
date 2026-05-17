/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.SlotAccess
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.ClickAction
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  org.jetbrains.annotations.Nullable
 */
package appeng.items.tools.powered;

import appeng.api.behaviors.ContainerItemContext;
import appeng.api.behaviors.ContainerItemStrategies;
import appeng.api.config.Actionable;
import appeng.api.implementations.menuobjects.IMenuItem;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.menu.locator.MenuLocators;
import java.util.function.DoubleSupplier;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public abstract class PoweredContainerItem
extends AEBasePoweredItem
implements IMenuItem {
    public PoweredContainerItem(DoubleSupplier powerCapacity, Item.Properties props) {
        super(powerCapacity, props);
    }

    protected long insert(Player player, ItemStack stack, AEKey what, @Nullable AEKeyType allowed, long amount, Actionable mode) {
        if (allowed != null && what.getType() != allowed) {
            return 0L;
        }
        ItemMenuHost host = this.getMenuHost(player, MenuLocators.forStack(stack), null);
        if (host == null) {
            return 0L;
        }
        return host.insert(player, what, amount, mode);
    }

    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY || !slot.allowModification(player)) {
            return false;
        }
        ItemStack other = slot.getItem();
        if (other.isEmpty()) {
            return true;
        }
        this.tryInsertFromPlayerOwnedItem(player, stack, other);
        return true;
    }

    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (action != ClickAction.SECONDARY || !slot.allowModification(player)) {
            return false;
        }
        if (other.isEmpty()) {
            return false;
        }
        this.tryInsertFromPlayerOwnedItem(player, stack, other);
        return true;
    }

    protected boolean tryInsertFromPlayerOwnedItem(Player player, ItemStack cellStack, ItemStack otherStack) {
        for (AEKeyType keyType : ContainerItemStrategies.getSupportedKeyTypes()) {
            if (!this.tryInsertFromPlayerOwnedItem(player, cellStack, otherStack, keyType)) continue;
            return true;
        }
        AEItemKey key = AEItemKey.of(otherStack);
        int inserted = (int)this.insert(player, cellStack, key, AEKeyType.items(), otherStack.getCount(), Actionable.MODULATE);
        if (inserted > 0) {
            otherStack.shrink(inserted);
            return true;
        }
        return false;
    }

    protected boolean tryInsertFromPlayerOwnedItem(Player player, ItemStack cellStack, ItemStack otherStack, AEKeyType keyType) {
        long extracted;
        GenericStack containedStack;
        ContainerItemContext context = ContainerItemStrategies.findOwnedItemContext(keyType, player, otherStack);
        if (context != null && (containedStack = context.getExtractableContent()) != null && this.insert(player, cellStack, containedStack.what(), keyType, containedStack.amount(), Actionable.SIMULATE) == containedStack.amount() && (extracted = context.extract(containedStack.what(), containedStack.amount(), Actionable.MODULATE)) > 0L) {
            this.insert(player, cellStack, containedStack.what(), keyType, extracted, Actionable.MODULATE);
            context.playEmptySound(player, containedStack.what());
            return true;
        }
        return false;
    }
}

