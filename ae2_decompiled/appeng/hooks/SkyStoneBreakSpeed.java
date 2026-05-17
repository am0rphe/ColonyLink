/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Tiers
 *  net.minecraft.world.level.block.state.BlockState
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$BreakSpeed
 */
package appeng.hooks;

import appeng.core.definitions.AEBlocks;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public final class SkyStoneBreakSpeed {
    public static final int SPEEDUP_FACTOR = 10;

    private SkyStoneBreakSpeed() {
    }

    public static void handleBreakFaster(PlayerEvent.BreakSpeed event) {
        ItemStack tool;
        BlockState blockState = event.getState();
        if (blockState.getBlock() == AEBlocks.SKY_STONE_BLOCK.block() && (tool = event.getEntity().getItemBySlot(EquipmentSlot.MAINHAND)).getDestroySpeed(blockState) > Tiers.IRON.getSpeed()) {
            event.setNewSpeed(event.getNewSpeed() * 10.0f);
        }
    }
}

