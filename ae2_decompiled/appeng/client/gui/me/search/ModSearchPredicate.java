/*
 * Decompiled with CFR 0.152.
 */
package appeng.client.gui.me.search;

import appeng.api.stacks.AEKey;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.util.Platform;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;

final class ModSearchPredicate
implements Predicate<GridInventoryEntry> {
    private final String term;

    public ModSearchPredicate(String term) {
        this.term = ModSearchPredicate.normalize(term);
    }

    @Override
    public boolean test(GridInventoryEntry gridInventoryEntry) {
        AEKey entryInfo = Objects.requireNonNull(gridInventoryEntry.getWhat());
        String modId = entryInfo.getModId();
        if (modId != null) {
            if (modId.contains(this.term)) {
                return true;
            }
            String modName = Platform.getModName(modId);
            modName = ModSearchPredicate.normalize(modName);
            return modName.contains(this.term);
        }
        return false;
    }

    private static String normalize(String input) {
        return input.toLowerCase(Locale.ROOT);
    }
}

