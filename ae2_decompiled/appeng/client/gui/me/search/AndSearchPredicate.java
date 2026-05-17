/*
 * Decompiled with CFR 0.152.
 */
package appeng.client.gui.me.search;

import appeng.menu.me.common.GridInventoryEntry;
import java.util.List;
import java.util.function.Predicate;

final class AndSearchPredicate
implements Predicate<GridInventoryEntry> {
    private final List<Predicate<GridInventoryEntry>> terms;

    private AndSearchPredicate(List<Predicate<GridInventoryEntry>> terms) {
        this.terms = terms;
    }

    public static Predicate<GridInventoryEntry> of(List<Predicate<GridInventoryEntry>> predicates) {
        if (predicates.isEmpty()) {
            return t -> true;
        }
        if (predicates.size() == 1) {
            return predicates.getFirst();
        }
        return new AndSearchPredicate(predicates);
    }

    @Override
    public boolean test(GridInventoryEntry entry) {
        for (Predicate<GridInventoryEntry> term : this.terms) {
            if (term.test(entry)) continue;
            return false;
        }
        return true;
    }
}

