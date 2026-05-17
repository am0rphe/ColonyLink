/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Position
 *  net.minecraft.core.dispenser.BlockSource
 *  net.minecraft.core.dispenser.DefaultDispenseItemBehavior
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.DispenserBlock
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.Vec3
 */
package appeng.hooks;

import appeng.items.tools.powered.MatterCannonItem;
import appeng.util.LookDirection;
import appeng.util.Platform;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;

public final class MatterCannonDispenseItemBehavior
extends DefaultDispenseItemBehavior {
    protected ItemStack execute(BlockSource source, ItemStack dispensedItem) {
        Item item = dispensedItem.getItem();
        if (item instanceof MatterCannonItem) {
            MatterCannonItem tm = (MatterCannonItem)item;
            Position position = DispenserBlock.getDispensePosition((BlockSource)source);
            Direction direction = (Direction)source.state().getValue((Property)DispenserBlock.FACING);
            Vec3 src = new Vec3(position.x(), position.y(), position.z());
            LookDirection dir = new LookDirection(src, src.add((double)(32 * direction.getStepX()), (double)(32 * direction.getStepY()), (double)(32 * direction.getStepZ())));
            ServerLevel level = source.level();
            Player p = Platform.getFakePlayer(level, null);
            Platform.configurePlayer(p, direction, (BlockEntity)source.blockEntity());
            tm.fireCannon((Level)level, dispensedItem, p, dir);
        }
        return dispensedItem;
    }
}

