/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterators
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.MenuType
 *  org.jetbrains.annotations.Nullable
 */
package appeng.menu.implementations;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Settings;
import appeng.api.config.StorageFilter;
import appeng.api.config.YesNo;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import appeng.api.util.IConfigManager;
import appeng.core.definitions.AEItems;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.parts.storagebus.StorageBusPart;
import appeng.util.ConfigInventory;
import com.google.common.collect.Iterators;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

public class StorageBusMenu
extends UpgradeableMenu<StorageBusPart> {
    private static final String ACTION_CLEAR = "clear";
    private static final String ACTION_PARTITION = "partition";
    public static final MenuType<StorageBusMenu> TYPE = MenuTypeBuilder.create(StorageBusMenu::new, StorageBusPart.class).build("storagebus");
    @GuiSync(value=3)
    public AccessRestriction rwMode = AccessRestriction.READ_WRITE;
    @GuiSync(value=4)
    public StorageFilter storageFilter = StorageFilter.EXTRACTABLE_ONLY;
    @GuiSync(value=7)
    public YesNo filterOnExtract = YesNo.YES;
    @GuiSync(value=8)
    @Nullable
    public Component connectedTo;

    public StorageBusMenu(MenuType<StorageBusMenu> menuType, int id, Inventory ip, StorageBusPart te) {
        super((MenuType<?>)menuType, id, ip, te);
        this.registerClientAction(ACTION_CLEAR, this::clear);
        this.registerClientAction(ACTION_PARTITION, this::partition);
        this.connectedTo = te.getConnectedToDescription();
    }

    @Override
    protected void setupConfig() {
        this.addExpandableConfigSlots(((StorageBusPart)this.getHost()).getConfig(), 2, 9, 5);
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        this.connectedTo = ((StorageBusPart)this.getHost()).getConnectedToDescription();
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm) {
        this.setFuzzyMode(cm.getSetting(Settings.FUZZY_MODE));
        this.setReadWriteMode(cm.getSetting(Settings.ACCESS));
        this.setStorageFilter(cm.getSetting(Settings.STORAGE_FILTER));
        this.setFilterOnExtract(cm.getSetting(Settings.FILTER_ON_EXTRACT));
    }

    @Override
    public boolean isSlotEnabled(int idx) {
        int upgrades = this.getUpgrades().getInstalledUpgrades(AEItems.CAPACITY_CARD);
        return upgrades > idx;
    }

    public void clear() {
        if (this.isClientSide()) {
            this.sendClientAction(ACTION_CLEAR);
            return;
        }
        ((StorageBusPart)this.getHost()).getConfig().clear();
        this.broadcastChanges();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void partition() {
        if (this.isClientSide()) {
            this.sendClientAction(ACTION_PARTITION);
            return;
        }
        ConfigInventory inv = ((StorageBusPart)this.getHost()).getConfig();
        MEStorage cellInv = ((StorageBusPart)this.getHost()).getInternalHandler();
        Iterator i = Collections.emptyIterator();
        if (cellInv != null) {
            i = Iterators.transform(cellInv.getAvailableStacks().iterator(), Map.Entry::getKey);
        }
        inv.beginBatch();
        try {
            for (int x = 0; x < inv.size(); ++x) {
                if (i.hasNext() && this.isSlotEnabled(x / 9 - 2)) {
                    inv.setStack(x, new GenericStack((AEKey)i.next(), 1L));
                    continue;
                }
                inv.setStack(x, null);
            }
        }
        finally {
            inv.endBatch();
        }
        this.broadcastChanges();
    }

    public AccessRestriction getReadWriteMode() {
        return this.rwMode;
    }

    private void setReadWriteMode(AccessRestriction rwMode) {
        this.rwMode = rwMode;
    }

    public StorageFilter getStorageFilter() {
        return this.storageFilter;
    }

    private void setStorageFilter(StorageFilter storageFilter) {
        this.storageFilter = storageFilter;
    }

    public YesNo getFilterOnExtract() {
        return this.filterOnExtract;
    }

    public void setFilterOnExtract(YesNo filterOnExtract) {
        this.filterOnExtract = filterOnExtract;
    }

    public boolean supportsFuzzySearch() {
        return this.hasUpgrade(AEItems.FUZZY_CARD);
    }

    @Nullable
    public Component getConnectedTo() {
        return this.connectedTo;
    }
}

