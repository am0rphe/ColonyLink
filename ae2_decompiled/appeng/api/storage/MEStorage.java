/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  net.minecraft.network.chat.Component
 */
package appeng.api.storage;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import com.google.common.base.Preconditions;
import java.util.Objects;
import net.minecraft.network.chat.Component;

public interface MEStorage {
    default public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
        return false;
    }

    default public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        return 0L;
    }

    default public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        return 0L;
    }

    default public void getAvailableStacks(KeyCounter out) {
    }

    public Component getDescription();

    default public KeyCounter getAvailableStacks() {
        KeyCounter result = new KeyCounter();
        this.getAvailableStacks(result);
        return result;
    }

    public static void checkPreconditions(AEKey what, long amount, Actionable mode, IActionSource source) {
        Objects.requireNonNull(what, "Cannot pass a null key");
        Objects.requireNonNull(mode, "Cannot pass a null mode");
        Objects.requireNonNull(source, "Cannot pass a null source");
        Preconditions.checkArgument((amount >= 0L ? 1 : 0) != 0, (Object)"Cannot pass a negative amount");
    }
}

