/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.core.dispenser.BlockSource
 *  net.minecraft.core.dispenser.DefaultDispenseItemBehavior
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.context.DirectionalPlaceContext
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.DispenserBlock
 *  net.minecraft.world.level.block.state.properties.Property
 */
package appeng.hooks;

import appeng.hooks.IBlockTool;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.properties.Property;

public final class BlockToolDispenseItemBehavior
extends DefaultDispenseItemBehavior {
    protected ItemStack execute(BlockSource dispenser, ItemStack dispensedItem) {
        Item i = dispensedItem.getItem();
        if (i instanceof IBlockTool) {
            IBlockTool tm = (IBlockTool)i;
            Direction direction = (Direction)dispenser.state().getValue((Property)DispenserBlock.FACING);
            ServerLevel level = dispenser.level();
            if (level instanceof ServerLevel) {
                DirectionalPlaceContext context = new DirectionalPlaceContext((Level)level, dispenser.pos().relative(direction), direction, dispensedItem, direction.getOpposite());
                tm.useOn((UseOnContext)context);
            }
        }
        return dispensedItem;
    }
}

