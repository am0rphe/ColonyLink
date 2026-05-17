/*
 * Decompiled with CFR 0.152.
 */
package appeng.client.gui.me.search;

import appeng.menu.me.common.GridInventoryEntry;
import java.util.List;
import java.util.function.Predicate;

final class OrSearchPredicate
implements Predicate<GridInventoryEntry> {
    private final List<Predicate<GridInventoryEntry>> terms;

    private OrSearchPredicate(List<Predicate<GridInventoryEntry>> terms) {
        this.terms = terms;
    }

    public static Predicate<GridInventoryEntry> of(List<Predicate<GridInventoryEntry>> filters) {
        if (filters.isEmpty()) {
            return t -> false;
        }
        if (filters.size() == 1) {
            return filters.getFirst();
        }
        return new OrSearchPredicate(filters);
    }

    @Override
    public boolean test(GridInventoryEntry entry) {
        for (Predicate<GridInventoryEntry> term : this.terms) {
            if (!term.test(entry)) continue;
            return true;
        }
        return false;
    }
}

