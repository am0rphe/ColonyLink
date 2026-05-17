/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.implementations.menuobjects;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.menuobjects.DelegateItemUpgradeInventory;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.stacks.AEKey;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.menu.locator.ItemMenuHostLocator;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ItemMenuHost<T extends Item>
implements IUpgradeableObject {
    private static final int BUFFER_ENERGY_TICKS = 10;
    private final T item;
    private final Player player;
    private final ItemMenuHostLocator locator;
    private final IUpgradeInventory upgrades;
    private int remainingEnergyTicks = 0;

    public ItemMenuHost(T item, Player player, ItemMenuHostLocator locator) {
        this.player = player;
        this.locator = locator;
        this.item = item;
        ItemStack currentStack = this.getItemStack();
        if (!currentStack.is(item)) {
            throw new IllegalArgumentException("The current item in-slot is " + String.valueOf(currentStack.getItem()) + " but this menu requires " + String.valueOf(item));
        }
        this.upgrades = new DelegateItemUpgradeInventory(this::getItemStack);
    }

    public Player getPlayer() {
        return this.player;
    }

    @Nullable
    public Integer getPlayerInventorySlot() {
        return this.locator.getPlayerInventorySlot();
    }

    @Nullable
    public ItemMenuHostLocator getLocator() {
        return this.locator;
    }

    public T getItem() {
        return this.item;
    }

    public ItemStack getItemStack() {
        return this.locator.locateItem(this.player);
    }

    public boolean isClientSide() {
        return this.player.level().isClientSide;
    }

    public void tick() {
    }

    public boolean isValid() {
        ItemStack currentItem = this.getItemStack();
        return !currentItem.isEmpty() && currentItem.is(this.item);
    }

    public boolean consumeIdlePower(Actionable action) {
        ItemMenuHost itemMenuHost;
        if (this.player.isCreative()) {
            return true;
        }
        if (this.remainingEnergyTicks > 0) {
            if (action == Actionable.MODULATE) {
                --this.remainingEnergyTicks;
            }
            return true;
        }
        double powerDrainPerTick = this.getPowerDrainPerTick();
        if (powerDrainPerTick > 0.0 && (itemMenuHost = this) instanceof IEnergySource) {
            IEnergySource energySource = (IEnergySource)((Object)itemMenuHost);
            double amt = 10.0 * powerDrainPerTick;
            double actualExtracted = energySource.extractAEPower(amt, action, PowerMultiplier.CONFIG);
            int remainingEnergyTicks = (int)Math.ceil(actualExtracted / powerDrainPerTick);
            if (action == Actionable.MODULATE) {
                this.remainingEnergyTicks = remainingEnergyTicks;
            }
            return remainingEnergyTicks > 0;
        }
        return true;
    }

    protected double getPowerDrainPerTick() {
        return 0.5;
    }

    @Override
    public final IUpgradeInventory getUpgrades() {
        return this.upgrades;
    }

    public long insert(Player player, AEKey what, long amount, Actionable mode) {
        return 0L;
    }
}

