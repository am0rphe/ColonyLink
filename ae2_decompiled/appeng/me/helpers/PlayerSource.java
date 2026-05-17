/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 *  org.jetbrains.annotations.Nullable
 */
package appeng.me.helpers;

import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class PlayerSource
implements IActionSource {
    private final Player player;
    @Nullable
    private final IActionHost via;

    public PlayerSource(Player p) {
        this(p, null);
    }

    public PlayerSource(Player p, @Nullable IActionHost v) {
        Objects.requireNonNull(p);
        this.player = p;
        this.via = v;
    }

    @Override
    public Optional<Player> player() {
        return Optional.of(this.player);
    }

    @Override
    public Optional<IActionHost> machine() {
        return Optional.ofNullable(this.via);
    }

    @Override
    public <T> Optional<T> context(Class<T> key) {
        return Optional.empty();
    }
}

