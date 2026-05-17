/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Reference2IntArrayMap
 *  it.unimi.dsi.fastutil.objects.Reference2IntMap
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.upgrades;

import appeng.api.inventories.InternalInventory;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.Upgrades;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import appeng.util.inv.filter.IAEItemFilter;
import it.unimi.dsi.fastutil.objects.Reference2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

abstract class UpgradeInventory
extends AppEngInternalInventory
implements InternalInventoryHost,
IUpgradeInventory {
    private final Item item;
    @Nullable
    private Reference2IntMap<Item> installed = null;

    public UpgradeInventory(Item item, int slots) {
        super(null, slots, 1);
        this.item = item;
        this.setHost(this);
        this.setFilter(new UpgradeInvFilter());
    }

    @Override
    public boolean isClientSide() {
        return false;
    }

    @Override
    protected boolean eventsEnabled() {
        return true;
    }

    @Override
    public int getMaxInstalled(ItemLike upgradeCard) {
        return Upgrades.getMaxInstallable(upgradeCard, (ItemLike)this.item);
    }

    @Override
    public ItemLike getUpgradableItem() {
        return this.item;
    }

    @Override
    public int getInstalledUpgrades(ItemLike upgradeCard) {
        if (this.installed == null) {
            this.updateUpgradeInfo();
        }
        return this.installed.getOrDefault((Object)upgradeCard.asItem(), 0);
    }

    private void updateUpgradeInfo() {
        this.installed = new Reference2IntArrayMap(this.size());
        for (ItemStack is : this) {
            int maxInstalled = this.getMaxInstalled((ItemLike)is.getItem());
            if (maxInstalled <= 0) continue;
            this.installed.merge((Object)is.getItem(), 1, (a, b) -> Math.min(maxInstalled, a + b));
        }
    }

    @Override
    public void readFromNBT(CompoundTag data, String name, HolderLookup.Provider registries) {
        super.readFromNBT(data, name, registries);
        this.updateUpgradeInfo();
    }

    @Override
    public void saveChangedInventory(AppEngInternalInventory inv) {
    }

    @Override
    public void onChangeInventory(AppEngInternalInventory inv, int slot) {
        this.installed = null;
    }

    @Override
    public void sendChangeNotification(int slot) {
        this.installed = null;
        super.sendChangeNotification(slot);
    }

    private class UpgradeInvFilter
    implements IAEItemFilter {
        private UpgradeInvFilter() {
        }

        @Override
        public boolean allowExtract(InternalInventory inv, int slot, int amount) {
            return true;
        }

        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack itemstack) {
            Item cardItem = itemstack.getItem();
            return UpgradeInventory.this.getInstalledUpgrades((ItemLike)cardItem) < UpgradeInventory.this.getMaxInstalled((ItemLike)cardItem);
        }
    }
}

