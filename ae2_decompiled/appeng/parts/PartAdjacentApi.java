/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.neoforged.neoforge.capabilities.BlockCapability
 *  net.neoforged.neoforge.capabilities.BlockCapabilityCache
 *  org.jetbrains.annotations.Nullable
 */
package appeng.parts;

import appeng.api.parts.IPartHost;
import appeng.parts.AEBasePart;
import appeng.util.Platform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import org.jetbrains.annotations.Nullable;

public class PartAdjacentApi<T> {
    private final AEBasePart part;
    private final BlockCapability<T, Direction> capability;
    private final Runnable invalidationListener;
    private BlockCapabilityCache<T, Direction> cache;

    public PartAdjacentApi(AEBasePart part, BlockCapability<T, Direction> capability) {
        this(part, capability, () -> {});
    }

    public PartAdjacentApi(AEBasePart part, BlockCapability<T, Direction> capability, Runnable invalidationListener) {
        this.capability = capability;
        this.part = part;
        this.invalidationListener = invalidationListener;
    }

    @Nullable
    public T find() {
        Level level = this.part.getLevel();
        if (!(level instanceof ServerLevel)) {
            return null;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        BlockEntity host = this.part.getHost().getBlockEntity();
        Direction attachedSide = this.part.getSide();
        BlockPos targetPos = host.getBlockPos().relative(attachedSide);
        if (!Platform.areBlockEntitiesTicking((Level)serverLevel, targetPos)) {
            return null;
        }
        if (this.cache == null) {
            this.cache = BlockCapabilityCache.create(this.capability, (ServerLevel)serverLevel, (BlockPos)targetPos, (Object)attachedSide.getOpposite(), () -> PartAdjacentApi.isPartValid(this.part), (Runnable)this.invalidationListener);
        }
        return (T)this.cache.getCapability();
    }

    public static boolean isPartValid(AEBasePart part) {
        IPartHost host;
        BlockEntity be = part.getBlockEntity();
        return be instanceof IPartHost && (host = (IPartHost)be).getPart(part.getSide()) == part && !be.isRemoved();
    }
}

