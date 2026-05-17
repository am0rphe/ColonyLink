/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 */
package appeng.items.contents;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.implementations.menuobjects.IPortableTerminal;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.storage.ILinkStatus;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageCells;
import appeng.api.storage.StorageHelper;
import appeng.api.storage.SupplierStorage;
import appeng.api.storage.cells.IBasicCellItem;
import appeng.api.util.IConfigManager;
import appeng.core.localization.GuiText;
import appeng.items.tools.powered.AbstractPortableCell;
import appeng.me.helpers.PlayerSource;
import appeng.menu.ISubMenu;
import appeng.menu.locator.ItemMenuHostLocator;
import com.google.common.base.Preconditions;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class PortableCellMenuHost<T extends AbstractPortableCell>
extends ItemMenuHost<T>
implements IPortableTerminal {
    private final BiConsumer<Player, ISubMenu> returnMainMenu;
    private final MEStorage cellStorage;
    private final AbstractPortableCell item;
    private final IConfigManager configManager;
    private ILinkStatus linkStatus = ILinkStatus.ofDisconnected();

    public PortableCellMenuHost(T item, Player player, ItemMenuHostLocator locator, BiConsumer<Player, ISubMenu> returnMainMenu) {
        super(item, player, locator);
        Preconditions.checkArgument((boolean)this.getItemStack().is(item), (Object)"Stack doesn't match item");
        this.returnMainMenu = returnMainMenu;
        this.cellStorage = new SupplierStorage(new CellStorageSupplier());
        Objects.requireNonNull(this.cellStorage, "Portable cell doesn't expose a cell inventory.");
        this.item = item;
        this.updateLinkStatus();
        this.configManager = IConfigManager.builder(this::getItemStack).registerSetting(Settings.SORT_BY, SortOrder.NAME).registerSetting(Settings.VIEW_MODE, ViewItems.ALL).registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING).build();
    }

    @Override
    public void tick() {
        super.tick();
        this.consumeIdlePower(Actionable.MODULATE);
        this.updateLinkStatus();
    }

    @Override
    public long insert(Player player, AEKey what, long amount, Actionable mode) {
        if (this.getLinkStatus().connected()) {
            MEStorage inv = this.getInventory();
            if (inv == null) {
                return 0L;
            }
            return StorageHelper.poweredInsert(this, inv, what, amount, new PlayerSource(player), mode);
        }
        Component statusText = this.getLinkStatus().statusDescription();
        if (this.isClientSide() && statusText != null && !mode.isSimulate()) {
            player.displayClientMessage(statusText, false);
        }
        return 0L;
    }

    private void updateLinkStatus() {
        this.linkStatus = !this.consumeIdlePower(Actionable.SIMULATE) ? ILinkStatus.ofDisconnected((Component)GuiText.OutOfPower.text()) : ILinkStatus.ofConnected();
    }

    @Override
    public ILinkStatus getLinkStatus() {
        return this.linkStatus;
    }

    @Override
    public double extractAEPower(double amt, Actionable mode, PowerMultiplier usePowerMultiplier) {
        amt = usePowerMultiplier.multiply(amt);
        if (mode == Actionable.SIMULATE) {
            return usePowerMultiplier.divide(Math.min(amt, this.item.getAECurrentPower(this.getItemStack())));
        }
        return usePowerMultiplier.divide(this.item.extractAEPower(this.getItemStack(), amt, Actionable.MODULATE));
    }

    @Override
    public MEStorage getInventory() {
        return this.cellStorage;
    }

    @Override
    public IConfigManager getConfigManager() {
        return this.configManager;
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        this.returnMainMenu.accept(player, subMenu);
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return this.getItemStack();
    }

    @Override
    public String getCloseHotkey() {
        AbstractPortableCell abstractPortableCell = this.item;
        if (abstractPortableCell instanceof IBasicCellItem) {
            IBasicCellItem cellItem = (IBasicCellItem)((Object)abstractPortableCell);
            if (cellItem.getKeyType().equals(AEKeyType.items())) {
                return "portable_item_cell";
            }
            if (cellItem.getKeyType().equals(AEKeyType.fluids())) {
                return "portable_fluid_cell";
            }
        }
        return null;
    }

    private class CellStorageSupplier
    implements Supplier<MEStorage> {
        private MEStorage currentStorage;
        private ItemStack currentStack;

        private CellStorageSupplier() {
        }

        @Override
        public MEStorage get() {
            ItemStack stack = PortableCellMenuHost.this.getItemStack();
            if (stack != this.currentStack) {
                this.currentStorage = StorageCells.getCellInventory(stack, null);
                this.currentStack = stack;
            }
            return this.currentStorage;
        }
    }
}

