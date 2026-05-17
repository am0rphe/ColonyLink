/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.StringTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 */
package appeng.api.util;

import appeng.api.ids.AEComponents;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AEKeyTypes;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class KeyTypeSelection {
    private final Listener listener;
    private final Map<AEKeyType, Boolean> keyTypes = new LinkedHashMap<AEKeyType, Boolean>();

    public KeyTypeSelection(Runnable listener, Predicate<AEKeyType> allowKeyType) {
        this((KeyTypeSelection selection) -> listener.run(), allowKeyType);
    }

    public KeyTypeSelection(Listener listener, Predicate<AEKeyType> allowKeyType) {
        this.listener = listener;
        for (AEKeyType keyType : AEKeyTypes.getAll()) {
            if (!allowKeyType.test(keyType)) continue;
            this.keyTypes.put(keyType, true);
        }
    }

    public static KeyTypeSelection forStack(ItemStack stack, Predicate<AEKeyType> allowKeyType) {
        KeyTypeSelection out = new KeyTypeSelection(selection -> stack.set(AEComponents.ENABLED_KEY_TYPES, selection.enabledSet()), allowKeyType);
        List selected = (List)stack.get(AEComponents.ENABLED_KEY_TYPES);
        if (selected != null) {
            out.setEnabledSet(selected);
        }
        return out;
    }

    public void setEnabled(AEKeyType type, boolean enabled) {
        if (!this.keyTypes.containsKey(type)) {
            throw new IllegalArgumentException("Key type " + String.valueOf(type) + " is not allowed.");
        }
        if (!enabled && this.enabledSet().size() <= 1) {
            return;
        }
        this.keyTypes.put(type, enabled);
        this.listener.onKeyTypeSelectionChanged(this);
    }

    public boolean isEnabled(AEKeyType type) {
        if (!this.keyTypes.containsKey(type)) {
            throw new IllegalArgumentException("Key type " + String.valueOf(type) + " is not allowed.");
        }
        return this.keyTypes.get(type);
    }

    public Map<AEKeyType, Boolean> enabled() {
        return new LinkedHashMap<AEKeyType, Boolean>(this.keyTypes);
    }

    public List<AEKeyType> enabledSet() {
        return this.keyTypes.entrySet().stream().filter(Map.Entry::getValue).map(Map.Entry::getKey).toList();
    }

    public void setEnabledSet(List<AEKeyType> selected) {
        for (Map.Entry<AEKeyType, Boolean> entry : this.keyTypes.entrySet()) {
            entry.setValue(selected.contains(entry.getKey()));
        }
    }

    public Predicate<AEKeyType> enabledPredicate() {
        return keyType -> this.keyTypes.getOrDefault(keyType, Boolean.FALSE);
    }

    public void writeToNBT(CompoundTag tag) {
        ListTag enabledKeyTypesTag = new ListTag();
        for (Map.Entry<AEKeyType, Boolean> entry : this.keyTypes.entrySet()) {
            if (!entry.getValue().booleanValue()) continue;
            enabledKeyTypesTag.add((Object)StringTag.valueOf((String)entry.getKey().getId().toString()));
        }
        tag.put("enabledKeyTypes", (Tag)enabledKeyTypesTag);
    }

    public void readFromNBT(CompoundTag tag, HolderLookup.Provider registries) {
        block4: {
            Iterator<Map.Entry<AEKeyType, Boolean>> iterator;
            for (Map.Entry<AEKeyType, Boolean> entry : this.keyTypes.entrySet()) {
                entry.setValue(false);
            }
            ListTag enabledKeyTypesTag = tag.getList("enabledKeyTypes", 8);
            for (int i = 0; i < enabledKeyTypesTag.size(); ++i) {
                try {
                    AEKeyType keyType = AEKeyTypes.get(ResourceLocation.parse((String)enabledKeyTypesTag.getString(i)));
                    if (!this.keyTypes.containsKey(keyType)) continue;
                    this.keyTypes.put(keyType, true);
                    continue;
                }
                catch (IllegalArgumentException keyType) {
                    // empty catch block
                }
            }
            if (!this.enabledSet().isEmpty() || !(iterator = this.keyTypes.entrySet().iterator()).hasNext()) break block4;
            Map.Entry<AEKeyType, Boolean> entry = iterator.next();
            entry.setValue(true);
        }
    }

    @FunctionalInterface
    public static interface Listener {
        public void onKeyTypeSelectionChanged(KeyTypeSelection var1);
    }
}

