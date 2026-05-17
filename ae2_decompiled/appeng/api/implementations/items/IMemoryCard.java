/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 */
package appeng.api.implementations.items;

import appeng.api.implementations.items.MemoryCardMessages;
import net.minecraft.world.entity.player.Player;

public interface IMemoryCard {
    public void notifyUser(Player var1, MemoryCardMessages var2);
}

