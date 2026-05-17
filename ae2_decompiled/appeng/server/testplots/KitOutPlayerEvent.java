/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent
 */
package appeng.server.testplots;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class KitOutPlayerEvent
extends PlayerEvent {
    public KitOutPlayerEvent(ServerPlayer player) {
        super((Player)player);
    }

    public ServerPlayer getPlayer() {
        return (ServerPlayer)this.getEntity();
    }
}

