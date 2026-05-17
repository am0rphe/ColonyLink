/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.GlobalPos
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 */
package appeng.server.testplots;

import appeng.api.config.Actionable;
import appeng.api.features.GridLinkables;
import appeng.api.inventories.InternalInventory;
import appeng.blockentity.networking.WirelessAccessPointBlockEntity;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.ItemDefinition;
import appeng.items.tools.powered.WirelessTerminalItem;
import appeng.server.testplots.SpawnExtraGridTestTools;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber
public final class SpawnTestTools {
    @SubscribeEvent
    public static void spawnWirelessTerminals(SpawnExtraGridTestTools e) {
        Set<WirelessAccessPointBlockEntity> waps = e.getGrid().getMachines(WirelessAccessPointBlockEntity.class);
        if (waps.isEmpty()) {
            return;
        }
        WirelessAccessPointBlockEntity wap = waps.iterator().next();
        InternalInventory inventory = e.getInventory();
        for (ItemDefinition<WirelessTerminalItem> item : List.of(AEItems.WIRELESS_CRAFTING_TERMINAL, AEItems.WIRELESS_TERMINAL)) {
            ItemStack terminal = item.stack();
            item.get().injectAEPower(terminal, Double.MAX_VALUE, Actionable.MODULATE);
            GridLinkables.get(item).link(terminal, GlobalPos.of((ResourceKey)wap.getLevel().dimension(), (BlockPos)wap.getBlockPos()));
            inventory.addItems(terminal);
        }
    }
}

