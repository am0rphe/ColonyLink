/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterables
 *  it.unimi.dsi.fastutil.objects.Object2LongMap$Entry
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 */
package appeng.crafting.inv;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.inv.ICraftingInventory;
import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public class ListCraftingInventory
implements ICraftingInventory {
    public final KeyCounter list = new KeyCounter();
    private final ChangeListener listener;

    public ListCraftingInventory(ChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void insert(AEKey what, long amount, Actionable mode) {
        if (mode == Actionable.MODULATE) {
            this.list.add(what, amount);
            this.listener.onChange(what);
        }
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode) {
        long available = this.list.get(what);
        long extracted = Math.min(available, amount);
        if (mode == Actionable.MODULATE) {
            if (available > extracted) {
                this.list.remove(what, extracted);
            } else {
                this.list.remove(what);
            }
            this.listener.onChange(what);
        }
        return extracted;
    }

    @Override
    public Iterable<AEKey> findFuzzyTemplates(AEKey what) {
        return Iterables.transform(this.list.findFuzzy(what, FuzzyMode.IGNORE_ALL), Map.Entry::getKey);
    }

    public void clear() {
        for (Object2LongMap.Entry<AEKey> stack : this.list) {
            this.list.set((AEKey)stack.getKey(), 0L);
            this.listener.onChange((AEKey)stack.getKey());
        }
        this.list.removeZeros();
    }

    public void readFromNBT(ListTag data, HolderLookup.Provider registries) {
        this.list.clear();
        if (data != null) {
            for (int i = 0; i < data.size(); ++i) {
                CompoundTag compound = data.getCompound(i);
                AEKey key = AEKey.fromTagGeneric(registries, compound);
                if (key == null) continue;
                long amount = compound.getLong("#");
                this.insert(key, amount, Actionable.MODULATE);
            }
        }
    }

    public ListTag writeToNBT(HolderLookup.Provider registries) {
        ListTag tag = new ListTag();
        for (Object2LongMap.Entry<AEKey> entry : this.list) {
            AEKey key = (AEKey)entry.getKey();
            long amount = entry.getLongValue();
            CompoundTag entryTag = key.toTagGeneric(registries);
            entryTag.putLong("#", amount);
            tag.add((Object)entryTag);
        }
        return tag;
    }

    @FunctionalInterface
    public static interface ChangeListener {
        public void onChange(AEKey var1);
    }
}

