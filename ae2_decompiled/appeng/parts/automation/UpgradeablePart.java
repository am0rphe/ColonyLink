/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  org.jetbrains.annotations.MustBeInvokedByOverriders
 *  org.jetbrains.annotations.Nullable
 */
package appeng.parts.automation;

import appeng.api.config.RedstoneMode;
import appeng.api.config.Setting;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.parts.IPartItem;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigManagerBuilder;
import appeng.api.util.IConfigurableObject;
import appeng.core.definitions.AEItems;
import appeng.parts.AEBasePart;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

public abstract class UpgradeablePart
extends AEBasePart
implements IConfigurableObject,
IUpgradeableObject {
    private final IConfigManager config;
    private final IUpgradeInventory upgrades;

    public UpgradeablePart(IPartItem<?> partItem) {
        super(partItem);
        this.upgrades = UpgradeInventories.forMachine((ItemLike)partItem.asItem(), this.getUpgradeSlots(), this::onUpgradesChanged);
        IConfigManagerBuilder configBuilder = IConfigManager.builder((manager, setting) -> {
            this.onSettingChanged(manager, setting);
            this.getHost().markForSave();
        });
        this.registerSettings(configBuilder);
        this.config = configBuilder.build();
        this.getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL);
    }

    @MustBeInvokedByOverriders
    protected void registerSettings(IConfigManagerBuilder builder) {
    }

    private void onUpgradesChanged() {
        this.getHost().markForSave();
        this.upgradesChanged();
    }

    protected int getUpgradeSlots() {
        return 4;
    }

    public void upgradesChanged() {
    }

    protected boolean isSleeping() {
        if (this.upgrades.isInstalled(AEItems.REDSTONE_CARD)) {
            return switch (this.getRSMode()) {
                default -> throw new MatchException(null, null);
                case RedstoneMode.IGNORE -> false;
                case RedstoneMode.HIGH_SIGNAL -> {
                    if (!this.getHost().hasRedstone()) {
                        yield true;
                    }
                    yield false;
                }
                case RedstoneMode.LOW_SIGNAL -> this.getHost().hasRedstone();
                case RedstoneMode.SIGNAL_PULSE -> true;
            };
        }
        return false;
    }

    @Override
    public boolean canConnectRedstone() {
        return this.upgrades.getMaxInstalled(AEItems.REDSTONE_CARD) > 0;
    }

    @Override
    public void readFromNBT(CompoundTag extra, HolderLookup.Provider registries) {
        super.readFromNBT(extra, registries);
        this.config.readFromNBT(extra, registries);
        this.upgrades.readFromNBT(extra, "upgrades", registries);
    }

    @Override
    public void writeToNBT(CompoundTag extra, HolderLookup.Provider registries) {
        super.writeToNBT(extra, registries);
        this.config.writeToNBT(extra, registries);
        this.upgrades.writeToNBT(extra, "upgrades", registries);
    }

    @Override
    public void addAdditionalDrops(List<ItemStack> drops, boolean wrenched) {
        super.addAdditionalDrops(drops, wrenched);
        for (ItemStack is : this.upgrades) {
            if (is.isEmpty()) continue;
            drops.add(is);
        }
    }

    @Override
    public void clearContent() {
        super.clearContent();
        this.upgrades.clear();
    }

    @Override
    public IConfigManager getConfigManager() {
        return this.config;
    }

    @Override
    @Nullable
    public InternalInventory getSubInventory(ResourceLocation id) {
        if (id.equals((Object)UPGRADES)) {
            return this.upgrades;
        }
        return super.getSubInventory(id);
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return this.upgrades;
    }

    public RedstoneMode getRSMode() {
        return null;
    }

    protected void onSettingChanged(IConfigManager manager, Setting<?> setting) {
    }
}

