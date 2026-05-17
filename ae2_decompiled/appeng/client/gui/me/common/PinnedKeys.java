/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 *  org.jetbrains.annotations.Nullable
 */
package appeng.client.gui.me.common;

import appeng.api.stacks.AEKey;
import com.google.common.collect.ImmutableSet;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

@OnlyIn(value=Dist.CLIENT)
public final class PinnedKeys {
    public static final int MAX_PINNED = 9;
    private static final Comparator<Map.Entry<AEKey, PinInfo>> TIME_COMPARATOR = Comparator.comparing(e -> ((PinInfo)e.getValue()).since);
    private static final Map<AEKey, PinInfo> pinned = new HashMap<AEKey, PinInfo>(9);

    private PinnedKeys() {
    }

    public static boolean isEmpty() {
        return pinned.isEmpty();
    }

    public static Set<AEKey> getPinnedKeys() {
        return ImmutableSet.copyOf(pinned.keySet());
    }

    @Nullable
    public static PinInfo getPinInfo(AEKey key) {
        return pinned.get(key);
    }

    public static void clearPinnedKeys() {
        pinned.clear();
    }

    public static void pinKey(AEKey key, PinReason reason) {
        PinInfo info = pinned.get(key);
        if (info != null) {
            info.since = Instant.now();
        } else {
            pinned.put(key, new PinInfo(reason));
        }
        if (pinned.size() > 9) {
            ArrayList<Map.Entry<AEKey, PinInfo>> toRemove = new ArrayList<Map.Entry<AEKey, PinInfo>>(pinned.entrySet());
            toRemove.sort(TIME_COMPARATOR);
            for (Map.Entry<AEKey, PinInfo> entry : toRemove.subList(0, 9 - toRemove.size())) {
                pinned.remove(entry.getKey());
            }
        }
    }

    public static void unpin(AEKey what) {
        pinned.remove(what);
    }

    public static boolean isPinned(AEKey what) {
        return pinned.containsKey(what);
    }

    public static void prune() {
        pinned.values().removeIf(v -> v.canPrune);
    }

    public static class PinInfo {
        public Instant since;
        public PinReason reason;
        public boolean canPrune;

        public PinInfo(PinReason reason) {
            this.reason = reason;
            this.since = Instant.now();
        }
    }

    public static enum PinReason {
        CRAFTING;

    }
}

