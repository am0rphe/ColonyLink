/*
 * Decompiled with CFR 0.152.
 */
package appeng.crafting.pattern;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class AEPatternHelper {
    private AEPatternHelper() {
    }

    public static List<GenericStack> condenseStacks(List<GenericStack> sparseInput) {
        LinkedHashMap<AEKey, Long> map = new LinkedHashMap<AEKey, Long>();
        for (GenericStack input : sparseInput) {
            if (input == null) continue;
            map.merge(input.what(), input.amount(), Long::sum);
        }
        if (map.isEmpty()) {
            throw new IllegalStateException("No pattern here!");
        }
        ArrayList<GenericStack> out = new ArrayList<GenericStack>(map.size());
        for (Map.Entry entry : map.entrySet()) {
            out.add(new GenericStack((AEKey)entry.getKey(), (Long)entry.getValue()));
        }
        return out;
    }
}

