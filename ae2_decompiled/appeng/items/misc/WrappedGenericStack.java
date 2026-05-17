/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.SlotAccess
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.ClickAction
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.CreativeModeTab$ItemDisplayParameters
 *  net.minecraft.world.item.CreativeModeTab$Output
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  org.jetbrains.annotations.Nullable
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package appeng.items.misc;

import appeng.api.behaviors.ContainerItemContext;
import appeng.api.behaviors.ContainerItemStrategies;
import appeng.api.config.Actionable;
import appeng.api.ids.AEComponents;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.core.definitions.AEItems;
import appeng.items.AEBaseItem;
import java.util.Objects;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WrappedGenericStack
extends AEBaseItem {
    private static final Logger LOG = LoggerFactory.getLogger(WrappedGenericStack.class);

    public static ItemStack wrap(GenericStack stack) {
        Objects.requireNonNull(stack, "stack");
        WrappedGenericStack item = AEItems.WRAPPED_GENERIC_STACK.asItem();
        ItemStack result = new ItemStack((ItemLike)item);
        result.set(AEComponents.WRAPPED_STACK, (Object)stack);
        return result;
    }

    public static ItemStack wrap(AEKey what, long amount) {
        Objects.requireNonNull(what, "what");
        return WrappedGenericStack.wrap(new GenericStack(what, amount));
    }

    public WrappedGenericStack(Item.Properties properties) {
        super(properties.stacksTo(1));
    }

    @Nullable
    public AEKey unwrapWhat(ItemStack stack) {
        if (stack.getItem() != this) {
            return null;
        }
        GenericStack wrapped = (GenericStack)stack.get(AEComponents.WRAPPED_STACK);
        if (wrapped == null) {
            return null;
        }
        return wrapped.what();
    }

    public long unwrapAmount(ItemStack stack) {
        if (stack.getItem() != this) {
            return 0L;
        }
        GenericStack wrapped = (GenericStack)stack.get(AEComponents.WRAPPED_STACK);
        if (wrapped == null) {
            return 0L;
        }
        return wrapped.amount();
    }

    public boolean overrideOtherStackedOnMe(ItemStack itemInSlot, ItemStack otherStack, Slot slot, ClickAction clickAction, Player player, SlotAccess access) {
        ContainerItemContext heldContainer;
        if (player.containerMenu == null) {
            return true;
        }
        AEKey what = this.unwrapWhat(itemInSlot);
        if (what == null && slot.getItem() == itemInSlot) {
            LOG.error("Removing a broken wrapped generic stack from player {} slot {}", (Object)player, (Object)slot.slot);
            slot.setByPlayer(ItemStack.EMPTY, itemInSlot);
            return true;
        }
        if (clickAction == ClickAction.PRIMARY && (heldContainer = ContainerItemStrategies.findCarriedContextForKey(what, player, player.containerMenu)) != null) {
            long amount = this.unwrapAmount(itemInSlot);
            long inserted = heldContainer.insert(what, amount, Actionable.MODULATE);
            if (player.level().isClientSide) {
                heldContainer.playFillSound(player, what);
            }
            if (inserted >= amount) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.set(WrappedGenericStack.wrap(what, amount - inserted));
            }
        }
        return true;
    }

    @Override
    public void addToMainCreativeTab(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
    }
}

