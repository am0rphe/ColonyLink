/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.Container
 *  net.minecraft.world.SimpleContainer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 *  org.jetbrains.annotations.Nullable
 */
package appeng.menu.slot;

import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.GenericStack;
import appeng.client.gui.Icon;
import appeng.core.AELog;
import appeng.menu.AEBaseMenu;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class AppEngSlot
extends Slot {
    private static final Container EMPTY_INVENTORY = new SimpleContainer(0);
    private final InternalInventory inventory;
    private final int invSlot;
    private boolean hideAmount;
    private boolean isEnabled = true;
    private Supplier<@Nullable List<Component>> emptyTooltip = () -> null;
    private boolean isDraggable = true;
    private AEBaseMenu menu = null;
    private boolean active = true;
    @Nullable
    private Icon icon;
    private Boolean validState = null;

    public AppEngSlot(InternalInventory inv, int invSlot) {
        super(EMPTY_INVENTORY, invSlot, 0, 0);
        this.inventory = inv;
        this.invSlot = invSlot;
    }

    public Slot setNotDraggable() {
        this.setDraggable(false);
        return this;
    }

    public void clearStack() {
        this.inventory.setItemDirect(this.invSlot, ItemStack.EMPTY);
    }

    @Nullable
    public List<Component> getCustomTooltip(ItemStack carriedItem) {
        List<Component> tooltip;
        if (this.getDisplayStack().isEmpty() && (tooltip = this.emptyTooltip.get()) != null) {
            return tooltip;
        }
        return null;
    }

    public boolean mayPlace(ItemStack stack) {
        if (this.containsWrapperItem()) {
            return false;
        }
        if (this.isSlotEnabled()) {
            return this.inventory.isItemValid(this.invSlot, stack);
        }
        return false;
    }

    public ItemStack getItem() {
        if (!this.isSlotEnabled()) {
            return ItemStack.EMPTY;
        }
        if (this.slot >= this.inventory.size()) {
            return ItemStack.EMPTY;
        }
        return this.inventory.getStackInSlot(this.invSlot);
    }

    public void set(ItemStack stack) {
        if (this.isSlotEnabled()) {
            this.inventory.setItemDirect(this.invSlot, stack);
            this.setChanged();
        }
    }

    public void initialize(ItemStack stack) {
        if (!this.isSlotEnabled() && this.invSlot >= this.inventory.size() && stack.isEmpty()) {
            return;
        }
        this.inventory.setItemDirect(this.invSlot, stack);
    }

    private void notifyContainerSlotChanged() {
        if (this.getMenu() != null) {
            this.getMenu().onSlotChange(this);
        }
    }

    public InternalInventory getInventory() {
        return this.inventory;
    }

    public void setChanged() {
        super.setChanged();
        this.validState = null;
        this.notifyContainerSlotChanged();
    }

    public int getMaxStackSize() {
        return this.inventory.getSlotLimit(this.invSlot);
    }

    public int getMaxStackSize(ItemStack stack) {
        return Math.min(this.getMaxStackSize(), stack.getMaxStackSize());
    }

    public boolean mayPickup(Player player) {
        if (this.containsWrapperItem()) {
            return false;
        }
        if (this.isSlotEnabled()) {
            return !this.inventory.extractItem(this.invSlot, 1, true).isEmpty();
        }
        return false;
    }

    public ItemStack remove(int amount) {
        if (this.containsWrapperItem()) {
            return ItemStack.EMPTY;
        }
        return this.inventory.extractItem(this.invSlot, amount, false);
    }

    private boolean containsWrapperItem() {
        return GenericStack.isWrapped(this.getItem());
    }

    public boolean isSameInventory(Slot other) {
        return other instanceof AppEngSlot && ((AppEngSlot)other).inventory == this.inventory;
    }

    public InternalInventory getSlotInv() {
        return this.inventory.getSlotInv(this.invSlot);
    }

    public boolean isActive() {
        return this.isSlotEnabled() && this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isSlotEnabled() {
        return this.isEnabled;
    }

    public void setSlotEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public ItemStack getDisplayStack() {
        ItemStack is = this.getItem();
        if (this.hideAmount) {
            GenericStack unwrapped = GenericStack.unwrapItemStack(is);
            if (unwrapped != null) {
                return GenericStack.wrapInItemStack(unwrapped.what(), 0L);
            }
            is = is.copy();
            is.setCount(1);
        }
        return is;
    }

    public float getOpacityOfIcon() {
        return 1.0f;
    }

    public boolean renderIconWithItem() {
        return false;
    }

    public Icon getIcon() {
        return this.icon;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    public boolean isDraggable() {
        return this.isDraggable;
    }

    private void setDraggable(boolean isDraggable) {
        this.isDraggable = isDraggable;
    }

    protected AEBaseMenu getMenu() {
        return this.menu;
    }

    public void setMenu(AEBaseMenu menu) {
        this.menu = menu;
    }

    protected boolean isRemote() {
        return this.menu == null || this.menu.isClientSide();
    }

    public final boolean isValid() {
        if (this.validState == null) {
            try {
                this.validState = this.getCurrentValidationState();
            }
            catch (Exception e) {
                this.validState = false;
                AELog.warn("Failed to update validation state for slot %s: %s", new Object[]{this, e});
            }
        }
        return this.validState;
    }

    protected boolean getCurrentValidationState() {
        return true;
    }

    public void resetCachedValidation() {
        this.validState = null;
    }

    public boolean isHideAmount() {
        return this.hideAmount;
    }

    public void setHideAmount(boolean hideAmount) {
        this.hideAmount = hideAmount;
    }

    public void setEmptyTooltip(Supplier<@Nullable List<Component>> emptyTooltip) {
        this.emptyTooltip = emptyTooltip;
    }
}

