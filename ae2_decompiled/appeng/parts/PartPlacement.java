/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.component.DataComponentMap
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.block.SoundType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraft.world.phys.shapes.VoxelShape
 *  org.jetbrains.annotations.Nullable
 */
package appeng.parts;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartHelper;
import appeng.core.AELog;
import appeng.core.definitions.AEAttachmentTypes;
import appeng.parts.BusCollisionHelper;
import appeng.parts.networking.CablePart;
import appeng.util.Platform;
import appeng.util.SettingsFrom;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class PartPlacement {
    public static InteractionResult place(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack partStack = context.getItemInHand();
        Direction side = context.getClickedFace();
        Item item = partStack.getItem();
        if (!(item instanceof IPartItem)) {
            return InteractionResult.PASS;
        }
        IPartItem partItem = (IPartItem)item;
        Placement placement = PartPlacement.getPartPlacement(player, level, partStack, pos, side, context.getClickLocation());
        if (placement == null) {
            return InteractionResult.FAIL;
        }
        Object part = PartPlacement.placePart(player, level, partItem, partStack.getComponents(), placement.pos(), placement.side());
        if (part == null) {
            Platform.sendImmediateBlockEntityUpdate(player, pos);
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide && player != null && !player.isCreative()) {
            partStack.shrink(1);
            if (partStack.getCount() == 0) {
                player.setItemInHand(context.getHand(), ItemStack.EMPTY);
            }
        }
        return InteractionResult.sidedSuccess((boolean)level.isClientSide());
    }

    @Nullable
    public static <T extends IPart> T placePart(@Nullable Player player, Level level, IPartItem<T> partItem, @Nullable DataComponentMap configData, BlockPos pos, Direction side) {
        IPartHost host = PartHelper.getOrPlacePartHost(level, pos, false, player);
        if (host == null) {
            return null;
        }
        T addedPart = host.addPart(partItem, side, player);
        if (addedPart == null) {
            if (host.isEmpty()) {
                host.cleanup();
            }
            return null;
        }
        VoxelShape collisionShape = host.getCollisionShape(null);
        if (!collisionShape.isEmpty() && !level.isUnobstructed(null, collisionShape.move((double)pos.getX(), (double)pos.getY(), (double)pos.getZ()))) {
            host.removePart((IPart)addedPart);
            if (host.isEmpty()) {
                host.cleanup();
            }
            return null;
        }
        if (configData != null) {
            try {
                addedPart.importSettings(SettingsFrom.DISMANTLE_ITEM, configData, player);
            }
            catch (Exception e) {
                AELog.warn(e, "Failed to import part settings during placement.");
            }
        }
        BlockState state = level.getBlockState(pos);
        SoundType ss = state.getSoundType((LevelReader)level, pos, (Entity)player);
        level.playSound(null, pos, ss.getPlaceSound(), SoundSource.BLOCKS, (ss.getVolume() + 1.0f) / 2.0f, ss.getPitch() * 0.8f);
        return addedPart;
    }

    @Nullable
    public static Placement getPartPlacement(@Nullable Player player, Level level, ItemStack partStack, BlockPos pos, Direction side, Vec3 clickLocation) {
        Direction replaceCablePlacement = PartPlacement.tryReplaceCableSegment(level, partStack, pos, clickLocation);
        if (replaceCablePlacement != null) {
            side = replaceCablePlacement;
        }
        if (player != null) {
            Direction direction = side = (Boolean)player.getData(AEAttachmentTypes.HOLDING_CTRL) != false ? side.getOpposite() : side;
        }
        if (PartPlacement.canPlacePartOnBlock(player, level, partStack, pos, side)) {
            return new Placement(pos, side);
        }
        if (PartPlacement.canPlacePartOnBlock(player, level, partStack, pos = pos.relative(side), side = side.getOpposite())) {
            return new Placement(pos, side);
        }
        return null;
    }

    @Nullable
    private static Direction tryReplaceCableSegment(Level level, ItemStack partStack, BlockPos pos, Vec3 clickLocation) {
        IPartHost host = PartHelper.getPartHost(level, pos);
        if (host == null) {
            return null;
        }
        IPart cable = host.getPart(null);
        if (!(cable instanceof CablePart)) {
            return null;
        }
        CablePart cablePart = (CablePart)cable;
        Direction hitSide = null;
        Vec3 localClickLocation = clickLocation.subtract((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
        block0: for (Direction side : Direction.values()) {
            ArrayList<AABB> boxes = new ArrayList<AABB>();
            BusCollisionHelper bch = new BusCollisionHelper(boxes, null, true);
            cablePart.getBoxes(bch, boxSide -> boxSide == side);
            for (AABB box : boxes) {
                if (!box.inflate(0.02).contains(localClickLocation)) continue;
                hitSide = side;
                break block0;
            }
        }
        if (host.canAddPart(partStack, hitSide)) {
            return hitSide;
        }
        return null;
    }

    public static boolean canPlacePartOnBlock(@Nullable Player player, Level level, ItemStack partStack, BlockPos pos, Direction side) {
        IPartHost host = PartHelper.getPartHost(level, pos);
        if (host == null && !PartHelper.canPlacePartHost(player, level, pos)) {
            return false;
        }
        return host == null || host.canAddPart(partStack, side);
    }

    public record Placement(BlockPos pos, Direction side) {
    }
}

