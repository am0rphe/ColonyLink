/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.base.Preconditions
 *  it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap
 *  it.unimi.dsi.fastutil.objects.Object2ObjectSortedMap
 */
package appeng.api.stacks;

import appeng.api.config.FuzzyMode;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKey2LongMap;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectSortedMap;
import java.util.Comparator;
import java.util.SortedMap;

final class FuzzySearch {
    @VisibleForTesting
    static final KeyComparator COMPARATOR = new KeyComparator();
    private static final int MIN_DAMAGE_VALUE = -1;

    private FuzzySearch() {
    }

    public static <K extends AEKey, V> Object2ObjectSortedMap<K, V> createMap() {
        return new Object2ObjectAVLTreeMap((Comparator)COMPARATOR);
    }

    public static AEKey2LongMap.AVLTreeMap createMap2Long() {
        return new AEKey2LongMap.AVLTreeMap(COMPARATOR);
    }

    public static <T extends SortedMap<K, V>, K, V> T findFuzzy(T map, AEKey key, FuzzyMode fuzzy) {
        FuzzyBound lowerBound = FuzzySearch.makeLowerBound(key, fuzzy);
        FuzzyBound upperBound = FuzzySearch.makeUpperBound(key, fuzzy);
        Preconditions.checkState((lowerBound.itemDamage > upperBound.itemDamage ? 1 : 0) != 0);
        return (T)map.subMap((FuzzyBound)lowerBound, (FuzzyBound)upperBound);
    }

    static FuzzyBound makeLowerBound(AEKey key, FuzzyMode fuzzy) {
        int damage;
        int maxValue = key.getFuzzySearchMaxValue();
        Preconditions.checkState((maxValue > 0 ? 1 : 0) != 0, (String)"Cannot use fuzzy search on keys that don't have a fuzzy max value: %s", (Object)key);
        if (fuzzy == FuzzyMode.IGNORE_ALL) {
            damage = maxValue;
        } else {
            int breakpoint = fuzzy.calculateBreakPoint(maxValue);
            damage = key.getFuzzySearchValue() <= breakpoint ? breakpoint : maxValue;
        }
        return new FuzzyBound(damage);
    }

    static FuzzyBound makeUpperBound(AEKey key, FuzzyMode fuzzy) {
        int damage;
        int maxValue = key.getFuzzySearchMaxValue();
        Preconditions.checkState((maxValue > 0 ? 1 : 0) != 0, (String)"Cannot use fuzzy search on keys that don't have a fuzzy max value: %s", (Object)key);
        if (fuzzy == FuzzyMode.IGNORE_ALL) {
            damage = -1;
        } else {
            int breakpoint = fuzzy.calculateBreakPoint(maxValue);
            damage = key.getFuzzySearchValue() <= breakpoint ? -1 : breakpoint;
        }
        return new FuzzyBound(damage);
    }

    private static class KeyComparator
    implements Comparator<Object> {
        private KeyComparator() {
        }

        @Override
        public int compare(Object a, Object b) {
            int fuzzyOrderA;
            int fuzzyOrderB;
            FuzzyBound boundA = null;
            AEKey stackA = null;
            if (a instanceof FuzzyBound) {
                boundA = (FuzzyBound)a;
                fuzzyOrderB = boundA.itemDamage;
            } else {
                stackA = (AEKey)a;
                fuzzyOrderB = stackA.getFuzzySearchValue();
            }
            FuzzyBound boundB = null;
            AEKey stackB = null;
            if (b instanceof FuzzyBound) {
                boundB = (FuzzyBound)b;
                fuzzyOrderA = boundB.itemDamage;
            } else {
                stackB = (AEKey)b;
                fuzzyOrderA = stackB.getFuzzySearchValue();
            }
            if (boundA != null || boundB != null) {
                return Integer.compare(fuzzyOrderA, fuzzyOrderB);
            }
            if (stackA.equals(stackB)) {
                return 0;
            }
            int fuzzyOrder = Integer.compare(fuzzyOrderA, fuzzyOrderB);
            if (fuzzyOrder != 0) {
                return fuzzyOrder;
            }
            return Long.compare(stackA.hashCode(), stackB.hashCode());
        }
    }

    @VisibleForTesting
    record FuzzyBound(int itemDamage) {
    }
}

