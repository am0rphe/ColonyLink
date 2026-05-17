/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 */
package appeng.me.helpers;

import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.world.entity.player.Player;

public class MachineSource
implements IActionSource {
    private final IActionHost via;

    public MachineSource(IActionHost v) {
        this.via = Objects.requireNonNull(v);
    }

    @Override
    public Optional<Player> player() {
        return Optional.empty();
    }

    @Override
    public Optional<IActionHost> machine() {
        return Optional.of(this.via);
    }

    @Override
    public <T> Optional<T> context(Class<T> key) {
        return Optional.empty();
    }
}

