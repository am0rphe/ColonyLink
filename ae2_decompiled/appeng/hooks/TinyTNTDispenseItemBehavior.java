/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.core.dispenser.BlockSource
 *  net.minecraft.core.dispenser.DefaultDispenseItemBehavior
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.DispenserBlock
 *  net.minecraft.world.level.block.state.properties.Property
 */
package appeng.hooks;

import appeng.entity.TinyTNTPrimedEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.properties.Property;

public final class TinyTNTDispenseItemBehavior
extends DefaultDispenseItemBehavior {
    protected ItemStack execute(BlockSource dispenser, ItemStack dispensedItem) {
        Direction Direction2 = (Direction)dispenser.state().getValue((Property)DispenserBlock.FACING);
        ServerLevel level = dispenser.level();
        int i = dispenser.pos().getX() + Direction2.getStepX();
        int j = dispenser.pos().getY() + Direction2.getStepY();
        int k = dispenser.pos().getZ() + Direction2.getStepZ();
        TinyTNTPrimedEntity primedTinyTNTEntity = new TinyTNTPrimedEntity((Level)level, (float)i + 0.5f, j, (float)k + 0.5f, null);
        level.addFreshEntity((Entity)primedTinyTNTEntity);
        dispensedItem.setCount(dispensedItem.getCount() - 1);
        return dispensedItem;
    }
}

