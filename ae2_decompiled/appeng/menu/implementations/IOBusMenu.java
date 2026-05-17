/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.MenuType
 */
package appeng.menu.implementations;

import appeng.api.util.KeyTypeSelection;
import appeng.api.util.KeyTypeSelectionHost;
import appeng.core.definitions.AEItems;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.interfaces.KeyTypeSelectionMenu;
import appeng.parts.automation.ExportBusPart;
import appeng.parts.automation.IOBusPart;
import appeng.parts.automation.ImportBusPart;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class IOBusMenu
extends UpgradeableMenu<IOBusPart>
implements KeyTypeSelectionMenu {
    public static final MenuType<IOBusMenu> EXPORT_TYPE = MenuTypeBuilder.create(IOBusMenu::new, ExportBusPart.class).build("export_bus");
    public static final MenuType<IOBusMenu> IMPORT_TYPE = MenuTypeBuilder.create(IOBusMenu::new, ImportBusPart.class).build("import_bus");
    @GuiSync(value=20)
    public KeyTypeSelectionMenu.SyncedKeyTypes importKeyTypes = new KeyTypeSelectionMenu.SyncedKeyTypes();

    public IOBusMenu(MenuType<?> menuType, int id, Inventory ip, IOBusPart host) {
        super(menuType, id, ip, host);
    }

    @Override
    protected void setupConfig() {
        this.addExpandableConfigSlots(((IOBusPart)this.getHost()).getConfig(), 2, 9, 5);
    }

    @Override
    public boolean isSlotEnabled(int idx) {
        int upgrades = this.getUpgrades().getInstalledUpgrades(AEItems.CAPACITY_CARD);
        return upgrades > idx;
    }

    @Override
    public void broadcastChanges() {
        Object t;
        super.broadcastChanges();
        if (this.isServerSide() && (t = this.getHost()) instanceof KeyTypeSelectionHost) {
            KeyTypeSelectionHost selectionHost = (KeyTypeSelectionHost)t;
            this.importKeyTypes = new KeyTypeSelectionMenu.SyncedKeyTypes(selectionHost.getKeyTypeSelection().enabled());
        }
    }

    @Override
    public KeyTypeSelection getServerKeyTypeSelection() {
        return ((KeyTypeSelectionHost)this.getHost()).getKeyTypeSelection();
    }

    @Override
    public KeyTypeSelectionMenu.SyncedKeyTypes getClientKeyTypeSelection() {
        return this.importKeyTypes;
    }
}

