/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.features;

import appeng.api.networking.security.IActionHost;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class Locatables {
    private static final Type<IActionHost> QUANTUM_NETWORK_BRIDGES = new Type();

    private Locatables() {
    }

    public static Type<IActionHost> quantumNetworkBridges() {
        return QUANTUM_NETWORK_BRIDGES;
    }

    public static class Type<T> {
        private final Map<Long, T> objects = new HashMap<Long, T>();

        @Nullable
        public T get(Level level, long key) {
            Objects.requireNonNull(level, "level");
            if (level.isClientSide()) {
                return null;
            }
            return this.objects.get(key);
        }

        public void register(Level level, long key, T locatable) {
            Objects.requireNonNull(level, "level");
            Objects.requireNonNull(locatable, "locatable");
            if (!level.isClientSide()) {
                this.objects.put(key, locatable);
            }
        }

        public void unregister(Level level, long key) {
            Objects.requireNonNull(level, "level");
            if (!level.isClientSide()) {
                this.objects.remove(key);
            }
        }
    }
}

