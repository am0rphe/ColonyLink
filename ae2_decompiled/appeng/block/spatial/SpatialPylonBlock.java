/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.BooleanProperty
 *  net.minecraft.world.level.block.state.properties.Property
 */
package appeng.block.spatial;

import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.spatial.SpatialPylonBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;

public class SpatialPylonBlock
extends AEBaseEntityBlock<SpatialPylonBlockEntity> {
    public static final BooleanProperty POWERED_ON = BooleanProperty.create((String)"powered_on");

    public SpatialPylonBlock() {
        super(SpatialPylonBlock.glassProps().lightLevel(state -> (Boolean)state.getValue((Property)POWERED_ON) != false ? 8 : 0));
        this.registerDefaultState((BlockState)this.defaultBlockState().setValue((Property)POWERED_ON, (Comparable)Boolean.valueOf(false)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{POWERED_ON});
    }

    @Override
    protected BlockState updateBlockStateFromBlockEntity(BlockState currentState, SpatialPylonBlockEntity be) {
        SpatialPylonBlockEntity.ClientState state = be.getClientState();
        return (BlockState)currentState.setValue((Property)POWERED_ON, (Comparable)Boolean.valueOf(state.powered()));
    }

    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        SpatialPylonBlockEntity tsp = (SpatialPylonBlockEntity)this.getBlockEntity((BlockGetter)level, pos);
        if (tsp != null) {
            tsp.neighborChanged(fromPos);
        }
    }
}

