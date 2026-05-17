/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.networking.security;

import appeng.api.networking.security.IActionHost;
import appeng.me.helpers.BaseActionSource;
import appeng.me.helpers.MachineSource;
import appeng.me.helpers.PlayerSource;
import java.util.Optional;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public interface IActionSource {
    public static IActionSource empty() {
        return new BaseActionSource();
    }

    public static IActionSource ofPlayer(Player player) {
        return IActionSource.ofPlayer(player, null);
    }

    public static IActionSource ofPlayer(Player player, @Nullable IActionHost maybeHost) {
        return new PlayerSource(player, maybeHost);
    }

    public static IActionSource ofMachine(IActionHost machine) {
        return new MachineSource(machine);
    }

    public Optional<Player> player();

    public Optional<IActionHost> machine();

    public <T> Optional<T> context(Class<T> var1);
}

