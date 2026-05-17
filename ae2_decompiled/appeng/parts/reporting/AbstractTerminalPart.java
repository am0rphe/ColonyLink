/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.phys.Vec3
 *  org.jetbrains.annotations.MustBeInvokedByOverriders
 */
package appeng.parts.reporting;

import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.implementations.blockentities.IViewCellStorage;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;
import appeng.api.parts.IPartItem;
import appeng.api.storage.ILinkStatus;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.MEStorage;
import appeng.api.storage.SupplierStorage;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigManagerBuilder;
import appeng.api.util.KeyTypeSelection;
import appeng.api.util.KeyTypeSelectionHost;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.menu.me.common.MEStorageMenu;
import appeng.parts.reporting.AbstractDisplayPart;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

public abstract class AbstractTerminalPart
extends AbstractDisplayPart
implements ITerminalHost,
IViewCellStorage,
InternalInventoryHost,
KeyTypeSelectionHost {
    private final IConfigManager cm;
    private final KeyTypeSelection keyTypeSelection = new KeyTypeSelection(this::saveChanges, keyType -> true);
    private final AppEngInternalInventory viewCell = new AppEngInternalInventory(this, 5);

    public AbstractTerminalPart(IPartItem<?> partItem) {
        super(partItem, true);
        IConfigManagerBuilder builder = IConfigManager.builder(this::saveChanges);
        this.registerSettings(builder);
        this.cm = builder.build();
    }

    @MustBeInvokedByOverriders
    protected void registerSettings(IConfigManagerBuilder builder) {
        builder.registerSetting(Settings.SORT_BY, SortOrder.NAME);
        builder.registerSetting(Settings.VIEW_MODE, ViewItems.ALL);
        builder.registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);
    }

    @Override
    public void addAdditionalDrops(List<ItemStack> drops, boolean wrenched) {
        super.addAdditionalDrops(drops, wrenched);
        for (ItemStack is : this.viewCell) {
            if (is.isEmpty()) continue;
            drops.add(is);
        }
    }

    @Override
    public void clearContent() {
        super.clearContent();
        this.viewCell.clear();
    }

    @Override
    public IConfigManager getConfigManager() {
        return this.cm;
    }

    public void saveChanges() {
        this.getHost().markForSave();
    }

    @Override
    public void saveChangedInventory(AppEngInternalInventory inv) {
        this.saveChanges();
    }

    @Override
    public void readFromNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.readFromNBT(data, registries);
        this.cm.readFromNBT(data, registries);
        this.keyTypeSelection.readFromNBT(data, registries);
        this.viewCell.readFromNBT(data, "viewCell", registries);
    }

    @Override
    public void writeToNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.writeToNBT(data, registries);
        this.cm.writeToNBT(data, registries);
        this.keyTypeSelection.writeToNBT(data);
        this.viewCell.writeToNBT(data, "viewCell", registries);
    }

    @Override
    public boolean onUseWithoutItem(Player player, Vec3 pos) {
        if (!super.onUseWithoutItem(player, pos) && !player.level().isClientSide) {
            MenuOpener.open(this.getMenuType(player), player, MenuLocators.forPart(this));
        }
        return true;
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        MenuOpener.open(this.getMenuType(player), player, subMenu.getLocator(), true);
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return new ItemStack(this.getPartItem());
    }

    public MenuType<?> getMenuType(Player player) {
        return MEStorageMenu.TYPE;
    }

    @Override
    public MEStorage getInventory() {
        return new SupplierStorage(() -> {
            IGrid grid = this.getMainNode().getGrid();
            if (grid != null) {
                return grid.getStorageService().getInventory();
            }
            return null;
        });
    }

    @Override
    public ILinkStatus getLinkStatus() {
        return ILinkStatus.ofManagedNode(this.getMainNode());
    }

    @Override
    public InternalInventory getViewCellStorage() {
        return this.viewCell;
    }

    @Override
    public void onChangeInventory(AppEngInternalInventory inv, int slot) {
        this.getHost().markForSave();
    }

    @Override
    public KeyTypeSelection getKeyTypeSelection() {
        return this.keyTypeSelection;
    }
}
