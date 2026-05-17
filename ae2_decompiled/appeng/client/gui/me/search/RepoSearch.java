/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2BooleanMap
 *  it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap
 */
package appeng.client.gui.me.search;

import appeng.api.stacks.AEKey;
import appeng.client.gui.me.search.AndSearchPredicate;
import appeng.client.gui.me.search.ItemIdSearchPredicate;
import appeng.client.gui.me.search.ModSearchPredicate;
import appeng.client.gui.me.search.NameSearchPredicate;
import appeng.client.gui.me.search.OrSearchPredicate;
import appeng.client.gui.me.search.TagSearchPredicate;
import appeng.client.gui.me.search.TooltipsSearchPredicate;
import appeng.menu.me.common.GridInventoryEntry;
import it.unimi.dsi.fastutil.longs.Long2BooleanMap;
import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Predicate;

public class RepoSearch {
    private String searchString = "";
    private final Long2BooleanMap cache = new Long2BooleanOpenHashMap();
    private Predicate<GridInventoryEntry> search = e -> true;
    final Map<AEKey, String> tooltipCache = new WeakHashMap<AEKey, String>();

    public String getSearchString() {
        return this.searchString;
    }

    public void setSearchString(String searchString) {
        if (!searchString.equals(this.searchString)) {
            this.search = this.fromString(searchString);
            this.searchString = searchString;
            this.cache.clear();
        }
    }

    public boolean matches(GridInventoryEntry entry) {
        return this.cache.computeIfAbsent(entry.getSerial(), s -> this.search.test(entry));
    }

    private Predicate<GridInventoryEntry> fromString(String searchString) {
        String[] orParts = searchString.split("\\|");
        if (orParts.length == 1) {
            return AndSearchPredicate.of(this.getPredicates(orParts[0]));
        }
        ArrayList<Predicate<GridInventoryEntry>> orPartFilters = new ArrayList<Predicate<GridInventoryEntry>>(orParts.length);
        for (String orPart : orParts) {
            orPartFilters.add(AndSearchPredicate.of(this.getPredicates(orPart)));
        }
        return OrSearchPredicate.of(orPartFilters);
    }

    private List<Predicate<GridInventoryEntry>> getPredicates(String query) {
        String[] terms = query.toLowerCase().trim().split("\\s+");
        ArrayList<Predicate<GridInventoryEntry>> predicateFilters = new ArrayList<Predicate<GridInventoryEntry>>(terms.length);
        for (String part : terms) {
            if (part.startsWith("@")) {
                predicateFilters.add(new ModSearchPredicate(part.substring(1)));
                continue;
            }
            if (part.startsWith("$")) {
                predicateFilters.add(new TooltipsSearchPredicate(part.substring(1), this.tooltipCache));
                continue;
            }
            if (part.startsWith("#")) {
                predicateFilters.add(new TagSearchPredicate(part.substring(1)));
                continue;
            }
            if (part.startsWith("*")) {
                predicateFilters.add(new ItemIdSearchPredicate(part.substring(1)));
                continue;
            }
            predicateFilters.add(new NameSearchPredicate(part));
        }
        return predicateFilters;
    }
}

