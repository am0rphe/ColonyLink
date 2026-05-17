/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  org.jetbrains.annotations.Nullable
 */
package appeng.blockentity.misc;

import appeng.api.config.Actionable;
import appeng.api.ids.AETags;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.implementations.blockentities.ICrankable;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.orientation.BlockOrientation;
import appeng.api.orientation.RelativeSide;
import appeng.api.util.AECableType;
import appeng.block.misc.GrowthAcceleratorBlock;
import appeng.blockentity.grid.AENetworkedPoweredBlockEntity;
import appeng.core.AEConfig;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

public class GrowthAcceleratorBlockEntity
extends AENetworkedPoweredBlockEntity
implements IPowerChannelState {
    public static final int MAX_STORED_POWER = 1600;
    private static final int POWER_PER_TICK = 8;

    public GrowthAcceleratorBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.setInternalMaxPower(1600.0);
        this.setPowerSides(this.getGridConnectableSides(this.getOrientation()));
        this.getMainNode().setFlags(new GridFlags[0]);
        this.getMainNode().setIdlePowerUsage(8.0);
        this.getMainNode().addService(IGridTickable.class, new IGridTickable(){

            @Override
            public TickingRequest getTickingRequest(IGridNode node) {
                int speed = AEConfig.instance().getGrowthAcceleratorSpeed();
                return new TickingRequest(speed, speed, false);
            }

            @Override
            public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
                GrowthAcceleratorBlockEntity.this.onTick(ticksSinceLastCall);
                return TickRateModulation.SAME;
            }
        });
    }

    @Override
    public InternalInventory getInternalInventory() {
        return InternalInventory.empty();
    }

    @Override
    public Set<Direction> getGridConnectableSides(BlockOrientation orientation) {
        return orientation.getSides(EnumSet.of(RelativeSide.FRONT, RelativeSide.BACK));
    }

    @Override
    protected void onOrientationChanged(BlockOrientation orientation) {
        super.onOrientationChanged(orientation);
        this.setPowerSides(this.getGridConnectableSides(this.getOrientation()));
    }

    private void onTick(int ticksSinceLastCall) {
        boolean powered = this.isPowered();
        if (powered != (Boolean)this.getBlockState().getValue((Property)GrowthAcceleratorBlock.POWERED)) {
            this.markForUpdate();
        }
        if (!powered) {
            return;
        }
        this.extractAEPower(8 * ticksSinceLastCall, Actionable.MODULATE);
        for (Direction direction : Direction.values()) {
            BlockPos adjPos = this.getBlockPos().relative(direction);
            BlockState adjState = this.getLevel().getBlockState(adjPos);
            if (!adjState.is(AETags.GROWTH_ACCELERATABLE)) continue;
            adjState.randomTick((ServerLevel)this.getLevel(), adjPos, this.getLevel().getRandom());
        }
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        if (reason == IGridNodeListener.State.POWER) {
            this.markForUpdate();
        }
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.COVERED;
    }

    @Override
    public boolean isPowered() {
        if (!this.isClientSide()) {
            return this.getMainNode().isPowered() || this.extractAEPower(8.0, Actionable.SIMULATE) >= 8.0;
        }
        return (Boolean)this.getBlockState().getValue((Property)GrowthAcceleratorBlock.POWERED);
    }

    @Override
    public boolean isActive() {
        return this.isPowered();
    }

    @Nullable
    public ICrankable getCrankable(Direction direction) {
        if (this.getPowerSides().contains(direction)) {
            return new AENetworkedPoweredBlockEntity.Crankable(this);
        }
        return null;
    }
}

