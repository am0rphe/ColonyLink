/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  com.google.common.base.Predicate
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.material.Fluid
 *  org.jetbrains.annotations.Nullable
 */
package appeng.util;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AEKeyTypes;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.AEKeySlotFilter;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.me.helpers.BaseActionSource;
import appeng.util.ConfigMenuInventory;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

public class ConfigInventory
extends GenericStackInv {
    private final boolean allowOverstacking;
    private static final ConfigInventory EMPTY_TYPES = ConfigInventory.configTypes(0).build();

    public static ConfigInventory emptyTypes() {
        return EMPTY_TYPES;
    }

    protected ConfigInventory(Set<AEKeyType> supportedTypes, @Nullable AEKeySlotFilter slotFilter, GenericStackInv.Mode mode, int size, @Nullable Runnable listener, boolean allowOverstacking) {
        super(supportedTypes, listener, mode, size);
        this.allowOverstacking = allowOverstacking;
        this.setFilter(slotFilter);
    }

    public static Builder configTypes(int size) {
        return new Builder(GenericStackInv.Mode.CONFIG_TYPES, size);
    }

    public static Builder configStacks(int size) {
        return new Builder(GenericStackInv.Mode.CONFIG_STACKS, size);
    }

    public static Builder storage(int size) {
        return new Builder(GenericStackInv.Mode.STORAGE, size);
    }

    @Override
    @Nullable
    public GenericStack getStack(int slot) {
        GenericStack stack = super.getStack(slot);
        if (stack != null && !this.isSupportedType(stack.what())) {
            this.setStack(slot, null);
            stack = null;
        }
        return stack;
    }

    @Override
    @Nullable
    public AEKey getKey(int slot) {
        AEKey key = super.getKey(slot);
        if (key == null) {
            return null;
        }
        if (!this.isSupportedType(key)) {
            this.setStack(slot, null);
            key = null;
        }
        return key;
    }

    public Set<AEKey> keySet() {
        LinkedHashSet<AEKey> result = new LinkedHashSet<AEKey>();
        for (int i = 0; i < this.stacks.length; ++i) {
            AEKey what = this.getKey(i);
            if (what == null) continue;
            result.add(what);
        }
        return result;
    }

    @Override
    public void setStack(int slot, @Nullable GenericStack stack) {
        if (stack != null) {
            boolean typesOnly;
            if (!this.isSupportedType(stack.what())) {
                return;
            }
            boolean bl = typesOnly = this.mode == GenericStackInv.Mode.CONFIG_TYPES;
            if (typesOnly && stack.amount() != 0L) {
                stack = new GenericStack(stack.what(), 0L);
            } else if (!typesOnly && stack.amount() <= 0L) {
                stack = this.mode == GenericStackInv.Mode.CONFIG_STACKS && this.getStack(slot) == null ? new GenericStack(stack.what(), 1L) : null;
            }
        }
        super.setStack(slot, stack);
    }

    @Override
    public long getMaxAmount(AEKey key) {
        if (this.allowOverstacking) {
            return this.getCapacity(key.getType());
        }
        return super.getMaxAmount(key);
    }

    @Override
    public ConfigMenuInventory createMenuWrapper() {
        return new ConfigMenuInventory(this);
    }

    public ConfigInventory addFilter(ItemLike item) {
        this.addFilter(AEItemKey.of(item));
        return this;
    }

    public ConfigInventory addFilter(Fluid fluid) {
        this.addFilter(AEFluidKey.of(fluid));
        return this;
    }

    public ConfigInventory addFilter(AEKey what) {
        Preconditions.checkState((this.getMode() == GenericStackInv.Mode.CONFIG_TYPES ? 1 : 0) != 0);
        this.insert(what, 1L, Actionable.MODULATE, new BaseActionSource());
        return this;
    }

    public static final class Builder {
        private final GenericStackInv.Mode mode;
        private final int size;
        private Set<AEKeyType> supportedTypes = AEKeyTypes.getAll();
        @Nullable
        private AEKeySlotFilter slotFilter;
        @Nullable
        private Runnable changeListener;
        private boolean allowOverstacking;

        private Builder(GenericStackInv.Mode mode, int size) {
            this.mode = mode;
            this.size = size;
        }

        public Builder supportedType(AEKeyType type) {
            this.supportedTypes = Set.of(type);
            return this;
        }

        public Builder supportedTypes(AEKeyType type, AEKeyType ... moreTypes) {
            if (moreTypes.length == 0) {
                return this.supportedType(type);
            }
            this.supportedTypes = new HashSet<AEKeyType>(1 + moreTypes.length);
            this.supportedTypes.add(type);
            Collections.addAll(this.supportedTypes, moreTypes);
            return this;
        }

        public Builder supportedTypes(Collection<AEKeyType> types) {
            if (types.isEmpty()) {
                throw new IllegalArgumentException("Configuration inventories must support at least one key type");
            }
            this.supportedTypes = Set.copyOf(types);
            return this;
        }

        public Builder slotFilter(AEKeySlotFilter slotFilter) {
            this.slotFilter = slotFilter;
            return this;
        }

        public Builder slotFilter(Predicate<AEKey> filter) {
            this.slotFilter = (slot, what) -> filter.apply((Object)what);
            return this;
        }

        public Builder changeListener(Runnable changeListener) {
            this.changeListener = changeListener;
            return this;
        }

        public Builder allowOverstacking(boolean enable) {
            this.allowOverstacking = enable;
            return this;
        }

        public ConfigInventory build() {
            return new ConfigInventory(this.supportedTypes, this.slotFilter, this.mode, this.size, this.changeListener, this.allowOverstacking);
        }
    }
}

