/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  org.apache.commons.lang3.mutable.MutableObject
 *  org.jetbrains.annotations.Nullable
 */
package appeng.helpers;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.implementations.menuobjects.IPortableTerminal;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.stacks.AEKey;
import appeng.api.storage.ILinkStatus;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import appeng.api.storage.SupplierStorage;
import appeng.api.util.DimensionalBlockPos;
import appeng.api.util.IConfigManager;
import appeng.api.util.KeyTypeSelection;
import appeng.api.util.KeyTypeSelectionHost;
import appeng.blockentity.networking.WirelessAccessPointBlockEntity;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import appeng.core.localization.PlayerMessages;
import appeng.items.contents.StackDependentSupplier;
import appeng.items.tools.powered.WirelessTerminalItem;
import appeng.me.helpers.PlayerSource;
import appeng.me.storage.NullInventory;
import appeng.menu.ISubMenu;
import appeng.menu.locator.ItemMenuHostLocator;
import java.util.function.BiConsumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

public class WirelessTerminalMenuHost<T extends WirelessTerminalItem>
extends ItemMenuHost<T>
implements IPortableTerminal,
IActionHost,
KeyTypeSelectionHost {
    private final BiConsumer<Player, ISubMenu> returnToMainMenu;
    @Nullable
    private IWirelessAccessPoint currentAccessPoint;
    protected double currentDistanceFromGrid = Double.MAX_VALUE;
    protected double currentRemainingRange = Double.MIN_VALUE;
    private final MEStorage storage;
    private ILinkStatus linkStatus = ILinkStatus.ofDisconnected();

    public WirelessTerminalMenuHost(T item, Player player, ItemMenuHostLocator locator, BiConsumer<Player, ISubMenu> returnToMainMenu) {
        super(item, player, locator);
        this.returnToMainMenu = returnToMainMenu;
        this.storage = new SupplierStorage(new StackDependentSupplier<MEStorage>(this::getItemStack, this::getStorageFromStack));
        this.updateConnectedAccessPoint();
        this.updateLinkStatus();
    }

    @Override
    public ILinkStatus getLinkStatus() {
        return this.linkStatus;
    }

    @Nullable
    private MEStorage getStorageFromStack(ItemStack stack) {
        IGrid targetGrid = this.getLinkedGrid(stack);
        if (targetGrid != null) {
            return targetGrid.getStorageService().getInventory();
        }
        return NullInventory.of();
    }

    @Nullable
    private IGrid getLinkedGrid(ItemStack stack) {
        return ((WirelessTerminalItem)this.getItem()).getLinkedGrid(stack, this.getPlayer().level(), null);
    }

    @Override
    public MEStorage getInventory() {
        return this.storage;
    }

    @Override
    public double extractAEPower(double amt, Actionable mode, PowerMultiplier usePowerMultiplier) {
        double extracted = Math.min(amt, ((WirelessTerminalItem)this.getItem()).getAECurrentPower(this.getItemStack()));
        if (mode == Actionable.SIMULATE) {
            return extracted;
        }
        return ((WirelessTerminalItem)this.getItem()).usePower(this.getPlayer(), extracted, this.getItemStack()) ? extracted : 0.0;
    }

    @Override
    public IConfigManager getConfigManager() {
        return ((WirelessTerminalItem)this.getItem()).getConfigManager(this::getItemStack);
    }

    @Override
    public KeyTypeSelection getKeyTypeSelection() {
        return KeyTypeSelection.forStack(this.getItemStack(), keyType -> true);
    }

    @Override
    public IGridNode getActionableNode() {
        if (this.currentAccessPoint != null) {
            return this.currentAccessPoint.getActionableNode();
        }
        return null;
    }

    protected void updateConnectedAccessPoint() {
        this.currentAccessPoint = null;
        this.currentDistanceFromGrid = Double.MAX_VALUE;
        this.currentRemainingRange = Double.MIN_VALUE;
        IGrid targetGrid = this.getLinkedGrid(this.getItemStack());
        if (targetGrid != null) {
            @Nullable WirelessAccessPointBlockEntity bestWap = null;
            double bestSqDistance = Double.MAX_VALUE;
            double bestSqRemainingRange = Double.MIN_VALUE;
            for (WirelessAccessPointBlockEntity wap : targetGrid.getMachines(WirelessAccessPointBlockEntity.class)) {
                AccessPointSignal signal = this.getAccessPointSignal(wap);
                if (signal.distanceSquared < bestSqDistance) {
                    bestSqDistance = signal.distanceSquared;
                    bestWap = wap;
                }
                if (!(signal.remainingRangeSquared > bestSqRemainingRange)) continue;
                bestSqRemainingRange = signal.remainingRangeSquared;
            }
            this.currentAccessPoint = bestWap;
            this.currentDistanceFromGrid = Math.sqrt(bestSqDistance);
            this.currentRemainingRange = Math.sqrt(bestSqRemainingRange);
        }
    }

    protected AccessPointSignal getAccessPointSignal(IWirelessAccessPoint wap) {
        double offZ;
        double offY;
        double offX;
        double r;
        double rangeLimit = wap.getRange();
        rangeLimit *= rangeLimit;
        DimensionalBlockPos dc = wap.getLocation();
        if (dc.getLevel() == this.getPlayer().level() && (r = (offX = (double)dc.getPos().getX() - this.getPlayer().getX()) * offX + (offY = (double)dc.getPos().getY() - this.getPlayer().getY()) * offY + (offZ = (double)dc.getPos().getZ() - this.getPlayer().getZ()) * offZ) < rangeLimit && wap.isActive()) {
            return new AccessPointSignal(r, rangeLimit - r);
        }
        return new AccessPointSignal(Double.MAX_VALUE, Double.MIN_VALUE);
    }

    @Override
    public void tick() {
        this.updateConnectedAccessPoint();
        this.consumeIdlePower(Actionable.MODULATE);
        this.updateLinkStatus();
    }

    protected void updateLinkStatus() {
        if (!this.consumeIdlePower(Actionable.SIMULATE)) {
            this.linkStatus = ILinkStatus.ofDisconnected((Component)GuiText.OutOfPower.text());
        } else if (this.currentAccessPoint != null) {
            this.linkStatus = ILinkStatus.ofConnected();
        } else {
            MutableObject errorHolder = new MutableObject();
            this.linkStatus = ((WirelessTerminalItem)this.getItem()).getLinkedGrid(this.getItemStack(), this.getPlayer().level(), arg_0 -> ((MutableObject)errorHolder).setValue(arg_0)) == null ? ILinkStatus.ofDisconnected((Component)errorHolder.getValue()) : ILinkStatus.ofDisconnected((Component)PlayerMessages.OutOfRange.text());
        }
    }

    @Override
    protected double getPowerDrainPerTick() {
        if (this.currentAccessPoint != null && this.currentDistanceFromGrid < Double.MAX_VALUE) {
            return AEConfig.instance().wireless_getDrainRate(this.currentDistanceFromGrid);
        }
        return 0.0;
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        this.returnToMainMenu.accept(player, subMenu);
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return this.getItemStack();
    }

    @Override
    public String getCloseHotkey() {
        return "wireless_terminal";
    }

    @Override
    public long insert(Player player, AEKey what, long amount, Actionable mode) {
        if (this.isClientSide()) {
            return 0L;
        }
        if (this.getLinkStatus().connected()) {
            return StorageHelper.poweredInsert(this, this.getInventory(), what, amount, new PlayerSource(player), mode);
        }
        Component statusText = this.getLinkStatus().statusDescription();
        if (statusText != null && !mode.isSimulate()) {
            player.displayClientMessage(statusText, false);
        }
        return 0L;
    }

    public record AccessPointSignal(double distanceSquared, double remainingRangeSquared) {
    }
}

