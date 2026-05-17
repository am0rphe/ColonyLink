/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.block.entity.BlockEntity
 */
package appeng.helpers;

import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.helpers.IConfigInvHost;
import appeng.helpers.IPriorityHost;
import appeng.helpers.InterfaceLogic;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.InterfaceMenu;
import appeng.menu.locator.MenuHostLocator;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface InterfaceLogicHost
extends IConfigurableObject,
IUpgradeableObject,
IPriorityHost,
IConfigInvHost {
    public BlockEntity getBlockEntity();

    public void saveChanges();

    public InterfaceLogic getInterfaceLogic();

    @Override
    default public IConfigManager getConfigManager() {
        return this.getInterfaceLogic().getConfigManager();
    }

    @Override
    default public IUpgradeInventory getUpgrades() {
        return this.getInterfaceLogic().getUpgrades();
    }

    @Override
    default public int getPriority() {
        return this.getInterfaceLogic().getPriority();
    }

    @Override
    default public void setPriority(int newValue) {
        this.getInterfaceLogic().setPriority(newValue);
    }

    @Override
    default public GenericStackInv getConfig() {
        return this.getInterfaceLogic().getConfig();
    }

    default public GenericStackInv getStorage() {
        return this.getInterfaceLogic().getStorage();
    }

    default public void openMenu(Player player, MenuHostLocator locator) {
        MenuOpener.open(InterfaceMenu.TYPE, player, locator);
    }

    @Override
    default public void returnToMainMenu(Player player, ISubMenu subMenu) {
        MenuOpener.returnTo(InterfaceMenu.TYPE, player, subMenu.getLocator());
    }
}

