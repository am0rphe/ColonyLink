/*
 * Decompiled with CFR 0.152.
 */
package appeng.client.gui.me.common;

import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.stacks.AEKey;
import java.util.Comparator;

final class KeySorters {
    public static final Comparator<AEKey> NAME_ASC = Comparator.comparing(is -> is.getDisplayName().getString(), String::compareToIgnoreCase);
    public static final Comparator<AEKey> NAME_DESC = NAME_ASC.reversed();
    public static final Comparator<AEKey> MOD_ASC = Comparator.comparing(AEKey::getModId, String::compareToIgnoreCase).thenComparing(NAME_ASC);
    public static final Comparator<AEKey> MOD_DESC = MOD_ASC.reversed();

    private KeySorters() {
    }

    public static Comparator<AEKey> getComparator(SortOrder order, SortDir dir) {
        return switch (order) {
            default -> throw new MatchException(null, null);
            case SortOrder.NAME -> {
                if (dir == SortDir.ASCENDING) {
                    yield NAME_ASC;
                }
                yield NAME_DESC;
            }
            case SortOrder.MOD -> {
                if (dir == SortDir.ASCENDING) {
                    yield MOD_ASC;
                }
                yield MOD_DESC;
            }
            case SortOrder.AMOUNT -> throw new UnsupportedOperationException();
        };
    }
}

