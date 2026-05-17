/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 */
package appeng.blockentity.misc;

import appeng.api.config.CopyMode;
import appeng.api.config.Settings;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.cells.ICellWorkbenchItem;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.helpers.IConfigInvHost;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.util.ConfigInventory;
import appeng.util.ConfigManager;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CellWorkbenchBlockEntity
extends AEBaseBlockEntity
implements IConfigurableObject,
IUpgradeableObject,
InternalInventoryHost,
IConfigInvHost {
    private final AppEngInternalInventory cell = new AppEngInternalInventory(this, 1);
    private final GenericStackInv config = new GenericStackInv(this::configChanged, GenericStackInv.Mode.CONFIG_TYPES, 63);
    private final ConfigManager manager = new ConfigManager(this::saveChanges);
    private IUpgradeInventory cacheUpgrades = null;
    private ConfigInventory cacheConfig = null;
    private boolean locked = false;

    public CellWorkbenchBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.manager.registerSetting(Settings.COPY_MODE, CopyMode.CLEAR_ON_REMOVE);
        this.cell.setEnableClientEvents(true);
    }

    public ICellWorkbenchItem getCell() {
        if (this.cell.getStackInSlot(0).isEmpty()) {
            return null;
        }
        if (this.cell.getStackInSlot(0).getItem() instanceof ICellWorkbenchItem) {
            return (ICellWorkbenchItem)this.cell.getStackInSlot(0).getItem();
        }
        return null;
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        this.cell.writeToNBT(data, "cell", registries);
        this.config.writeToChildTag(data, "config", registries);
        this.manager.writeToNBT(data, registries);
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        this.cell.readFromNBT(data, "cell", registries);
        this.config.readFromChildTag(data, "config", registries);
        this.manager.readFromNBT(data, registries);
    }

    @Override
    public InternalInventory getSubInventory(ResourceLocation id) {
        if (id.equals((Object)ISegmentedInventory.CELLS)) {
            return this.cell;
        }
        return super.getSubInventory(id);
    }

    @Override
    public void saveChangedInventory(AppEngInternalInventory inv) {
        this.saveChanges();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void onChangeInventory(AppEngInternalInventory inv, int slot) {
        if (inv == this.cell && !this.locked) {
            this.locked = true;
            try {
                this.cacheUpgrades = null;
                this.cacheConfig = null;
                ConfigInventory configInventory = this.getCellConfigInventory();
                if (configInventory != null) {
                    if (!configInventory.isEmpty()) {
                        CellWorkbenchBlockEntity.copy(configInventory, this.config);
                    } else {
                        CellWorkbenchBlockEntity.copy(this.config, configInventory);
                        CellWorkbenchBlockEntity.copy(configInventory, this.config);
                    }
                } else if (this.manager.getSetting(Settings.COPY_MODE) == CopyMode.CLEAR_ON_REMOVE) {
                    this.config.clear();
                    this.saveChanges();
                }
            }
            finally {
                this.locked = false;
            }
        }
    }

    private void configChanged() {
        if (this.locked) {
            return;
        }
        this.locked = true;
        try {
            ConfigInventory c = this.getCellConfigInventory();
            if (c != null) {
                CellWorkbenchBlockEntity.copy(this.config, c);
                CellWorkbenchBlockEntity.copy(c, this.config);
            }
        }
        finally {
            this.locked = false;
        }
    }

    public static void copy(GenericStackInv from, GenericStackInv to) {
        int i;
        for (i = 0; i < Math.min(from.size(), to.size()); ++i) {
            GenericStack fromStack = from.getStack(i);
            if (fromStack != null && !to.isAllowedIn(i, fromStack.what())) {
                fromStack = null;
            }
            to.setStack(i, fromStack);
        }
        for (i = from.size(); i < to.size(); ++i) {
            to.setStack(i, null);
        }
    }

    private ConfigInventory getCellConfigInventory() {
        if (this.cacheConfig == null) {
            ICellWorkbenchItem cell = this.getCell();
            if (cell == null) {
                return null;
            }
            ItemStack is = this.cell.getStackInSlot(0);
            if (is.isEmpty()) {
                return null;
            }
            ConfigInventory inv = cell.getConfigInventory(is);
            if (inv == null) {
                return null;
            }
            this.cacheConfig = inv;
        }
        return this.cacheConfig;
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        if (!this.cell.getStackInSlot(0).isEmpty()) {
            drops.add(this.cell.getStackInSlot(0));
        }
    }

    @Override
    public void clearContent() {
        super.clearContent();
        this.cell.clear();
    }

    @Override
    public IConfigManager getConfigManager() {
        return this.manager;
    }

    @Override
    public GenericStackInv getConfig() {
        return this.config;
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        if (this.cacheUpgrades == null) {
            ICellWorkbenchItem cell = this.getCell();
            if (cell == null) {
                return UpgradeInventories.empty();
            }
            ItemStack is = this.cell.getStackInSlot(0);
            if (is.isEmpty()) {
                return UpgradeInventories.empty();
            }
            IUpgradeInventory inv = cell.getUpgrades(is);
            if (inv == null) {
                return UpgradeInventories.empty();
            }
            this.cacheUpgrades = inv;
            return this.cacheUpgrades;
        }
        return this.cacheUpgrades;
    }
}

