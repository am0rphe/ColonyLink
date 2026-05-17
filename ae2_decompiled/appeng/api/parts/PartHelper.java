/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.parts;

import appeng.api.parts.CableRenderMode;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlockEntities;
import appeng.core.definitions.AEBlocks;
import appeng.parts.PartPlacement;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class PartHelper {
    private PartHelper() {
    }

    public static InteractionResult usePartItem(UseOnContext context) {
        return PartPlacement.place(context);
    }

    @Nullable
    public static <T extends IPart> T getPart(IPartItem<T> partItem, BlockGetter level, BlockPos pos, @Nullable Direction side) {
        Class<T> partClass;
        IPart part = PartHelper.getPart(level, pos, side);
        if (part != null && (partClass = partItem.getPartClass()).isInstance(part)) {
            return (T)((IPart)partClass.cast(part));
        }
        return null;
    }

    @Nullable
    public static IPart getPart(BlockGetter level, BlockPos pos, @Nullable Direction side) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof IPartHost) {
            IPartHost partHost = (IPartHost)be;
            return partHost.getPart(side);
        }
        return null;
    }

    @Nullable
    public static <T extends IPart> T setPart(ServerLevel level, BlockPos pos, @Nullable Direction side, @Nullable Player player, IPartItem<T> partItem) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(pos, "pos");
        IPartHost host = PartHelper.getOrPlacePartHost((Level)level, pos, true, null);
        if (host == null) {
            return null;
        }
        T part = host.replacePart(partItem, side, player, null);
        if (host.isEmpty()) {
            host.cleanup();
        }
        return part;
    }

    @Nullable
    public static IPartHost getOrPlacePartHost(Level level, BlockPos pos, boolean force, @Nullable Player player) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof IPartHost) {
            IPartHost partHost = (IPartHost)blockEntity;
            return partHost;
        }
        if (!force && !PartHelper.canPlacePartHost(player, level, pos)) {
            return null;
        }
        BlockState state = AEBlocks.CABLE_BUS.block().getStateForPlacement(level, pos);
        level.setBlockAndUpdate(pos, state);
        return AEBlockEntities.CABLE_BUS.getBlockEntity((BlockGetter)level, pos);
    }

    @Nullable
    public static IPartHost placePartHost(@Nullable Player player, Level level, BlockPos pos) {
        if (!PartHelper.canPlacePartHost(player, level, pos)) {
            return null;
        }
        BlockState state = AEBlocks.CABLE_BUS.block().getStateForPlacement(level, pos);
        level.setBlockAndUpdate(pos, state);
        return AEBlockEntities.CABLE_BUS.getBlockEntity((BlockGetter)level, pos);
    }

    public static boolean canPlacePartHost(@Nullable Player player, Level level, BlockPos pos) {
        if (player != null && !level.mayInteract(player, pos)) {
            return false;
        }
        return level.isEmptyBlock(pos) || level.getBlockState(pos).canBeReplaced();
    }

    @Nullable
    public static IPartHost getPartHost(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof IPartHost) {
            IPartHost partHost = (IPartHost)blockEntity;
            return partHost;
        }
        return null;
    }

    public static CableRenderMode getCableRenderMode() {
        return AppEng.instance().getCableRenderMode();
    }
}

