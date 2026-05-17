/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 */
package appeng.util;

import com.google.common.base.Preconditions;
import java.util.EnumSet;
import java.util.Set;

public final class EnumCycler {
    private EnumCycler() {
    }

    public static <T extends Enum<T>> T rotateEnum(T ce, boolean backwards, Set<T> validOptions) {
        int pLoc;
        Preconditions.checkArgument((!validOptions.isEmpty() ? 1 : 0) != 0);
        int direction = backwards ? -1 : 1;
        Enum[] values = (Enum[])ce.getDeclaringClass().getEnumConstants();
        while (!validOptions.contains(ce = values[pLoc = Math.floorMod(ce.ordinal() + direction, values.length)])) {
        }
        return ce;
    }

    public static <T extends Enum<T>> T next(T ce) {
        return EnumCycler.rotateEnum(ce, false, EnumSet.allOf(ce.getDeclaringClass()));
    }

    public static <T extends Enum<T>> T prev(T ce) {
        return EnumCycler.rotateEnum(ce, true, EnumSet.allOf(ce.getDeclaringClass()));
    }
}

