/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  it.unimi.dsi.fastutil.objects.Reference2LongArrayMap
 *  it.unimi.dsi.fastutil.objects.Reference2LongMap
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  org.jetbrains.annotations.Nullable
 */
package appeng.helpers.externalstorage;

import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.behaviors.GenericSlotCapacities;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AEKeyTypes;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.AEKeySlotFilter;
import appeng.api.storage.MEStorage;
import appeng.core.AELog;
import appeng.util.ConfigMenuInventory;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Reference2LongArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2LongMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class GenericStackInv
implements MEStorage,
GenericInternalInventory {
    protected final GenericStack[] stacks;
    private final Runnable listener;
    private boolean suppressOnChange;
    private boolean onChangeSuppressed;
    private final Reference2LongMap<AEKeyType> capacities = new Reference2LongArrayMap();
    private final Set<AEKeyType> supportedKeyTypes;
    @Nullable
    private AEKeySlotFilter filter;
    protected final Mode mode;
    private Component description = Component.empty();

    public GenericStackInv(@Nullable Runnable listener, int size) {
        this(listener, Mode.STORAGE, size);
    }

    public GenericStackInv(@Nullable Runnable listener, Mode mode, int size) {
        this(AEKeyTypes.getAll(), listener, mode, size);
    }

    public GenericStackInv(Set<AEKeyType> supportedKeyTypes, @Nullable Runnable listener, Mode mode, int size) {
        this.supportedKeyTypes = Set.copyOf((Collection)Objects.requireNonNull(supportedKeyTypes, "supportedKeyTypes"));
        this.stacks = new GenericStack[size];
        this.listener = listener;
        this.mode = mode;
    }

    protected void setFilter(@Nullable AEKeySlotFilter filter) {
        this.filter = filter;
    }

    @Nullable
    public AEKeySlotFilter getFilter() {
        return this.filter;
    }

    @Override
    public boolean isSupportedType(AEKeyType type) {
        return this.supportedKeyTypes.contains(type);
    }

    @Override
    public int size() {
        return this.stacks.length;
    }

    public boolean isEmpty() {
        for (GenericStack stack : this.stacks) {
            if (stack == null) continue;
            return false;
        }
        return true;
    }

    @Override
    @Nullable
    public GenericStack getStack(int slot) {
        return this.stacks[slot];
    }

    @Override
    @Nullable
    public AEKey getKey(int slot) {
        return this.stacks[slot] != null ? this.stacks[slot].what() : null;
    }

    @Override
    public long getAmount(int slot) {
        return this.stacks[slot] != null ? this.stacks[slot].amount() : 0L;
    }

    @Override
    public void setStack(int slot, @Nullable GenericStack stack) {
        if (stack != null && this.getMaxAmount(stack.what()) < stack.amount()) {
            stack = new GenericStack(stack.what(), this.getMaxAmount(stack.what()));
        }
        if (!Objects.equals(this.stacks[slot], stack)) {
            this.stacks[slot] = stack;
            this.onChange();
        }
    }

    @Override
    public long insert(int slot, AEKey what, long amount, Actionable mode) {
        long newAmount;
        Objects.requireNonNull(what, "what");
        Preconditions.checkArgument((amount >= 0L ? 1 : 0) != 0, (Object)"amount >= 0");
        if (!this.canInsert() || !this.isAllowedIn(slot, what)) {
            return 0L;
        }
        AEKey currentWhat = this.getKey(slot);
        long currentAmount = this.getAmount(slot);
        if ((currentWhat == null || currentWhat.equals(what)) && (newAmount = Math.min(currentAmount + amount, this.getMaxAmount(what))) > currentAmount) {
            if (mode == Actionable.MODULATE) {
                this.setStack(slot, new GenericStack(what, newAmount));
                newAmount = this.getAmount(slot);
            }
            return newAmount - currentAmount;
        }
        return 0L;
    }

    @Override
    public boolean isAllowedIn(int slot, AEKey what) {
        return this.isSupportedType(what) && (this.filter == null || this.filter.isAllowed(slot, what));
    }

    @Override
    public long extract(int slot, AEKey what, long amount, Actionable mode) {
        Objects.requireNonNull(what, "what");
        Preconditions.checkArgument((amount >= 0L ? 1 : 0) != 0, (Object)"amount >= 0");
        AEKey currentWhat = this.getKey(slot);
        if (!this.canExtract() || currentWhat == null || !currentWhat.equals(what)) {
            return 0L;
        }
        long currentAmount = this.getAmount(slot);
        long canExtract = Math.min(currentAmount, amount);
        if (canExtract > 0L && mode == Actionable.MODULATE) {
            long newAmount = currentAmount - canExtract;
            if (newAmount <= 0L) {
                this.setStack(slot, null);
            } else {
                this.setStack(slot, new GenericStack(what, newAmount));
            }
            long reallyExtracted = Math.max(0L, currentAmount - this.getAmount(slot));
            if (reallyExtracted != canExtract) {
                AELog.warn("GenericStackInv simulation/modulation extraction mismatch: canExtract=%d, reallyExtracted=%d", canExtract, reallyExtracted);
                canExtract = reallyExtracted;
            }
        }
        return canExtract;
    }

    @Override
    public long getCapacity(AEKeyType space) {
        return this.capacities.getOrDefault((Object)space, Long.MAX_VALUE);
    }

    @Override
    public boolean canInsert() {
        return true;
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    public void setCapacity(AEKeyType space, long capacity) {
        this.capacities.put((Object)space, capacity);
    }

    public void useRegisteredCapacities() {
        for (Map.Entry<AEKeyType, Long> entry : GenericSlotCapacities.getMap().entrySet()) {
            this.setCapacity(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public long getMaxAmount(AEKey key) {
        if (key instanceof AEItemKey) {
            AEItemKey itemKey = (AEItemKey)key;
            return Math.min((long)itemKey.getMaxStackSize(), this.getCapacity(key.getType()));
        }
        return this.getCapacity(key.getType());
    }

    @Override
    public final void onChange() {
        if (!this.suppressOnChange) {
            this.notifyListener();
        } else {
            this.onChangeSuppressed = true;
        }
    }

    protected void notifyListener() {
        if (this.listener != null) {
            this.listener.run();
        }
    }

    public ListTag writeToTag(HolderLookup.Provider registries) {
        ListTag tag = new ListTag();
        for (GenericStack stack : this.stacks) {
            tag.add((Object)GenericStack.writeTag(registries, stack));
        }
        for (int i = tag.size() - 1; i >= 0 && tag.getCompound(i).isEmpty(); --i) {
            tag.remove(i);
        }
        return tag;
    }

    public void writeToChildTag(CompoundTag tag, String name, HolderLookup.Provider registries) {
        boolean isEmpty = true;
        for (GenericStack stack : this.stacks) {
            if (stack == null) continue;
            isEmpty = false;
            break;
        }
        if (!isEmpty) {
            tag.put(name, (Tag)this.writeToTag(registries));
        } else {
            tag.remove(name);
        }
    }

    public void readFromTag(ListTag tag, HolderLookup.Provider registries) {
        int i;
        boolean changed = false;
        for (i = 0; i < Math.min(this.size(), tag.size()); ++i) {
            GenericStack stack = GenericStack.readTag(registries, tag.getCompound(i));
            if (Objects.equals(stack, this.stacks[i])) continue;
            this.stacks[i] = stack;
            changed = true;
        }
        for (i = tag.size(); i < this.size(); ++i) {
            if (this.stacks[i] == null) continue;
            this.stacks[i] = null;
            changed = true;
        }
        if (changed) {
            this.onChange();
        }
    }

    public void clear() {
        boolean changed = false;
        for (int i = 0; i < this.stacks.length; ++i) {
            changed |= this.stacks[i] != null;
            this.stacks[i] = null;
        }
        if (changed) {
            this.onChange();
        }
    }

    public void readFromChildTag(CompoundTag tag, String name, HolderLookup.Provider registries) {
        if (tag.contains(name, 9)) {
            this.readFromTag(tag.getList(name, 10), registries);
        } else {
            this.clear();
        }
    }

    public void readFromList(List<@Nullable GenericStack> stacks) {
        for (int i = 0; i < this.size(); ++i) {
            if (i < stacks.size()) {
                this.setStack(i, stacks.get(i));
                continue;
            }
            this.setStack(i, null);
        }
    }

    public List<@Nullable GenericStack> toList() {
        ArrayList<GenericStack> result = new ArrayList<GenericStack>(this.size());
        for (int i = 0; i < this.size(); ++i) {
            result.add(this.getStack(i));
        }
        return result;
    }

    @Override
    public void beginBatch() {
        Preconditions.checkState((!this.suppressOnChange ? 1 : 0) != 0, (Object)"beginBatch was called without endBatch");
        this.suppressOnChange = true;
    }

    @Override
    public void endBatch() {
        Preconditions.checkState((boolean)this.suppressOnChange, (Object)"endBatch was called without beginBatch");
        this.suppressOnChange = false;
        if (this.onChangeSuppressed) {
            this.onChangeSuppressed = false;
            this.onChange();
        }
    }

    @Override
    public void endBatchSuppressed() {
        Preconditions.checkState((boolean)this.suppressOnChange, (Object)"endBatch was called without beginBatch");
        this.suppressOnChange = false;
        this.onChangeSuppressed = false;
    }

    public Mode getMode() {
        return this.mode;
    }

    public ConfigMenuInventory createMenuWrapper() {
        return new ConfigMenuInventory(this);
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        Objects.requireNonNull(what, "what");
        Preconditions.checkArgument((amount >= 0L ? 1 : 0) != 0, (Object)"amount >= 0");
        if (!this.isSupportedType(what)) {
            return 0L;
        }
        if (this.mode == Mode.CONFIG_TYPES) {
            int freeSlot = -1;
            for (int i = 0; i < this.stacks.length; ++i) {
                AEKey key = this.getKey(i);
                if (key == what) {
                    return 0L;
                }
                if (key != null || freeSlot != -1) continue;
                freeSlot = i;
            }
            if (freeSlot != -1 && mode == Actionable.MODULATE) {
                this.setStack(freeSlot, new GenericStack(what, 0L));
            }
            return 0L;
        }
        long inserted = 0L;
        for (int i = 0; i < this.stacks.length && inserted < amount; inserted += this.insert(i, what, amount - inserted, mode), ++i) {
        }
        return inserted;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        Objects.requireNonNull(what, "what");
        Preconditions.checkArgument((amount >= 0L ? 1 : 0) != 0, (Object)"amount >= 0");
        long extracted = 0L;
        for (int i = 0; i < this.stacks.length && extracted < amount; extracted += this.extract(i, what, amount - extracted, mode), ++i) {
        }
        return extracted;
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        for (GenericStack stack : this.stacks) {
            if (stack == null) continue;
            out.add(stack.what(), stack.amount());
        }
    }

    @Override
    public Component getDescription() {
        return this.description;
    }

    public void setDescription(Component description) {
        this.description = description;
    }

    public static enum Mode {
        CONFIG_TYPES,
        CONFIG_STACKS,
        STORAGE;

    }
}

