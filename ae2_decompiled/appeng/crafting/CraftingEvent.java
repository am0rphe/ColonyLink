/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.Container
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.neoforged.bus.api.Event
 *  net.neoforged.neoforge.common.NeoForge
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$ItemCraftedEvent
 */
package appeng.crafting;

import appeng.api.crafting.IPatternDetails;
import appeng.util.Platform;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class CraftingEvent {
    public static void fireAutoCraftingEvent(Level level, IPatternDetails pattern, ItemStack craftedItem, Container container) {
        ServerLevel serverLevel = (ServerLevel)level;
        Player fakePlayer = Platform.getFakePlayer(serverLevel, null);
        NeoForge.EVENT_BUS.post((Event)new PlayerEvent.ItemCraftedEvent(fakePlayer, craftedItem, container));
    }
}

