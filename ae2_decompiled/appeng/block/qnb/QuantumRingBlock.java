/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package appeng.block.qnb;

import appeng.block.qnb.QuantumBaseBlock;
import appeng.blockentity.qnb.QuantumBridgeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class QuantumRingBlock
extends QuantumBaseBlock {
    private static final VoxelShape SHAPE = QuantumRingBlock.createShape(0.125);
    private static final VoxelShape SHAPE_CORNER = QuantumRingBlock.createShape(0.125);
    private static final VoxelShape SHAPE_CENTER = QuantumRingBlock.createCenterShape(0.125);
    private static final double DEFAULT_MODEL_MIN = 0.125;
    private static final double DEFAULT_MODEL_MAX = 0.875;

    public QuantumRingBlock() {
        super(QuantumRingBlock.metalProps());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        QuantumBridgeBlockEntity bridge = (QuantumBridgeBlockEntity)this.getBlockEntity(level, pos);
        if (bridge != null && bridge.isCorner()) {
            return SHAPE_CORNER;
        }
        if (bridge != null && bridge.isFormed()) {
            return SHAPE_CENTER;
        }
        return SHAPE;
    }

    private static VoxelShape createShape(double onePixel) {
        return Shapes.create((AABB)new AABB(onePixel, onePixel, onePixel, 1.0 - onePixel, 1.0 - onePixel, 1.0 - onePixel));
    }

    private static VoxelShape createCenterShape(double offset) {
        VoxelShape centerShape = SHAPE;
        for (Direction facing : Direction.values()) {
            double xOffset = Math.abs((double)facing.getStepX() * offset);
            double yOffset = Math.abs((double)facing.getStepY() * offset);
            double zOffset = Math.abs((double)facing.getStepZ() * offset);
            VoxelShape extrusion = Shapes.create((AABB)new AABB(0.125 - xOffset, 0.125 - yOffset, 0.125 - zOffset, 0.875 + xOffset, 0.875 + yOffset, 0.875 + zOffset));
            centerShape = Shapes.or((VoxelShape)centerShape, (VoxelShape)extrusion);
        }
        return centerShape;
    }
}

