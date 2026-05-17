/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.Nullable
 */
package appeng.hooks;

import appeng.util.Platform;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class VisualStateSaving {
    private static final ThreadLocal<Boolean> SAVE_CLIENT_SIDE_STATE = new ThreadLocal();

    private VisualStateSaving() {
    }

    public static void setEnabled(boolean enabled) {
        SAVE_CLIENT_SIDE_STATE.set(enabled);
    }

    public static boolean isEnabled(@Nullable Level level) {
        return Boolean.TRUE.equals(SAVE_CLIENT_SIDE_STATE.get()) || level != null && Platform.isPonderLevel(level);
    }
}

