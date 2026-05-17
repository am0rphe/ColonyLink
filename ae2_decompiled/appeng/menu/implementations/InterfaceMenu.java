/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.MenuType
 */
package appeng.menu.implementations;

import appeng.api.config.Settings;
import appeng.api.stacks.GenericStack;
import appeng.api.util.IConfigManager;
import appeng.helpers.InterfaceLogic;
import appeng.helpers.InterfaceLogicHost;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.implementations.SetStockAmountMenu;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.FakeSlot;
import appeng.util.ConfigMenuInventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class InterfaceMenu
extends UpgradeableMenu<InterfaceLogicHost> {
    public static final String ACTION_OPEN_SET_AMOUNT = "setAmount";
    public static final MenuType<InterfaceMenu> TYPE = MenuTypeBuilder.create(InterfaceMenu::new, InterfaceLogicHost.class).build("interface");

    public InterfaceMenu(MenuType<? extends InterfaceMenu> menuType, int id, Inventory ip, InterfaceLogicHost host) {
        super((MenuType<?>)menuType, id, ip, host);
        this.registerClientAction(ACTION_OPEN_SET_AMOUNT, Integer.class, this::openSetAmountMenu);
        InterfaceLogic logic = host.getInterfaceLogic();
        ConfigMenuInventory config = logic.getConfig().createMenuWrapper();
        for (int x = 0; x < config.size(); ++x) {
            this.addSlot(new FakeSlot(config, x), SlotSemantics.CONFIG);
        }
        ConfigMenuInventory storage = logic.getStorage().createMenuWrapper();
        for (int x = 0; x < storage.size(); ++x) {
            this.addSlot(new AppEngSlot(storage, x), SlotSemantics.STORAGE);
        }
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm) {
        this.setFuzzyMode(cm.getSetting(Settings.FUZZY_MODE));
    }

    public void openSetAmountMenu(int configSlot) {
        if (this.isClientSide()) {
            this.sendClientAction(ACTION_OPEN_SET_AMOUNT, configSlot);
        } else {
            GenericStack stack = ((InterfaceLogicHost)this.getHost()).getConfig().getStack(configSlot);
            if (stack != null) {
                SetStockAmountMenu.open((ServerPlayer)this.getPlayer(), this.getLocator(), configSlot, stack.what(), (int)stack.amount());
            }
        }
    }
}

