/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  org.jetbrains.annotations.Nullable
 */
package appeng.helpers.patternprovider;

import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;
import appeng.api.stacks.AEItemKey;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.helpers.IPriorityHost;
import appeng.helpers.patternprovider.PatternContainer;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.PatternProviderMenu;
import appeng.menu.locator.MenuHostLocator;
import java.util.EnumSet;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public interface PatternProviderLogicHost
extends IConfigurableObject,
IPriorityHost,
PatternContainer {
    public PatternProviderLogic getLogic();

    public BlockEntity getBlockEntity();

    public EnumSet<Direction> getTargets();

    public void saveChanges();

    @Override
    default public IConfigManager getConfigManager() {
        return this.getLogic().getConfigManager();
    }

    @Override
    default public int getPriority() {
        return this.getLogic().getPriority();
    }

    @Override
    default public void setPriority(int newValue) {
        this.getLogic().setPriority(newValue);
    }

    default public void openMenu(Player player, MenuHostLocator locator) {
        MenuOpener.open(PatternProviderMenu.TYPE, player, locator);
    }

    @Override
    default public void returnToMainMenu(Player player, ISubMenu subMenu) {
        MenuOpener.returnTo(PatternProviderMenu.TYPE, player, subMenu.getLocator());
    }

    @Override
    @Nullable
    default public IGrid getGrid() {
        return this.getLogic().getGrid();
    }

    public AEItemKey getTerminalIcon();

    @Override
    default public boolean isVisibleInTerminal() {
        return this.getLogic().getConfigManager().getSetting(Settings.PATTERN_ACCESS_TERMINAL) == YesNo.YES;
    }

    @Override
    default public InternalInventory getTerminalPatternInventory() {
        return this.getLogic().getPatternInv();
    }

    @Override
    default public long getTerminalSortOrder() {
        return this.getLogic().getSortValue();
    }

    @Override
    default public PatternContainerGroup getTerminalGroup() {
        return this.getLogic().getTerminalGroup();
    }
}

