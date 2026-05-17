/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.bus.api.Event
 *  net.neoforged.neoforge.common.NeoForge
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent
 *  net.neoforged.neoforge.items.IItemHandler
 */
package appeng.util;

import appeng.integration.modules.curios.CuriosIntegration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.items.IItemHandler;

public class SearchInventoryEvent
extends PlayerEvent {
    private final List<ItemStack> stacks;

    public SearchInventoryEvent(Player player, List<ItemStack> stacks) {
        super(player);
        this.stacks = stacks;
    }

    public List<ItemStack> getStacks() {
        return this.stacks;
    }

    public static List<ItemStack> getItems(Player player) {
        ArrayList<ItemStack> items = new ArrayList<ItemStack>();
        NeoForge.EVENT_BUS.post((Event)new SearchInventoryEvent(player, items));
        return items;
    }

    static {
        NeoForge.EVENT_BUS.addListener(event -> event.getStacks().addAll((Collection<ItemStack>)event.getEntity().getInventory().items));
        NeoForge.EVENT_BUS.addListener(event -> {
            IItemHandler cap = (IItemHandler)event.getEntity().getCapability(CuriosIntegration.ITEM_HANDLER);
            if (cap == null) {
                return;
            }
            for (int i = 0; i < cap.getSlots(); ++i) {
                event.getStacks().add(cap.getStackInSlot(i));
            }
        });
    }
}

