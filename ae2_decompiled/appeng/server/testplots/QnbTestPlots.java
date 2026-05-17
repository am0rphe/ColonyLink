/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.properties.Property
 */
package appeng.server.testplots;

import appeng.api.networking.IGrid;
import appeng.block.qnb.QuantumBaseBlock;
import appeng.blockentity.qnb.QuantumBridgeBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.BlockDefinition;
import appeng.server.testplots.TestPlot;
import appeng.server.testplots.TestPlotClass;
import appeng.server.testworld.PlotBuilder;
import appeng.server.testworld.PlotTestHelper;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.Property;

@TestPlotClass
public final class QnbTestPlots {
    private QnbTestPlots() {
    }

    @TestPlot(value="simple_qnb_link")
    public static void simpleQnbLink(PlotBuilder plot) {
        BlockPos qnbA = BlockPos.ZERO.above();
        BlockPos qnbB = qnbA.east(4);
        QnbTestPlots.qnbRing(plot, qnbA);
        QnbTestPlots.qnbRing(plot, qnbB);
        plot.test(helper -> helper.startSequence().thenWaitUntil(() -> {
            QnbTestPlots.ringAround(qnbA, pos -> helper.assertBlockProperty((BlockPos)pos, (Property)QuantumBaseBlock.FORMED, Boolean.valueOf(true)));
            helper.assertBlockProperty(qnbA, (Property)QuantumBaseBlock.FORMED, Boolean.valueOf(true));
            QnbTestPlots.ringAround(qnbB, pos -> helper.assertBlockProperty((BlockPos)pos, (Property)QuantumBaseBlock.FORMED, Boolean.valueOf(true)));
            helper.assertBlockProperty(qnbB, (Property)QuantumBaseBlock.FORMED, Boolean.valueOf(true));
        }).thenExecute(() -> {
            ItemStack singularities = AEItems.QUANTUM_ENTANGLED_SINGULARITY.stack();
            QuantumBridgeBlockEntity.assignFrequency(singularities);
            QuantumBridgeBlockEntity coreA = QnbTestPlots.getCore(helper, qnbA);
            helper.check(coreA.getExposedInventoryForSide(Direction.SOUTH).addItems(singularities.copy()).isEmpty(), "failed to add singularity", qnbA);
            QuantumBridgeBlockEntity coreB = QnbTestPlots.getCore(helper, qnbB);
            helper.check(coreB.getExposedInventoryForSide(Direction.SOUTH).addItems(singularities.copy()).isEmpty(), "failed to add singularity", qnbB);
        }).thenWaitUntil(() -> {
            IGrid gridB;
            IGrid gridA = helper.getGrid(qnbA);
            if (gridA != (gridB = helper.getGrid(qnbB))) {
                helper.fail("not same grid", qnbA);
                helper.fail("not same grid", qnbB);
            }
        }).thenWaitUntil(() -> {
            IGrid gridB;
            IGrid gridA = helper.getGrid(qnbA);
            if (gridA != (gridB = helper.getGrid(qnbB))) {
                helper.fail("not same grid", qnbA);
                helper.fail("not same grid", qnbB);
            }
        }).thenExecute(() -> QnbTestPlots.getCore(helper, qnbA).clearContent()).thenWaitUntil(() -> {
            QuantumBridgeBlockEntity coreA = QnbTestPlots.getCore(helper, qnbA);
            helper.check(!coreA.hasQES(), "still has singularity", qnbA);
            IGrid gridA = helper.getGrid(qnbA);
            IGrid gridB = helper.getGrid(qnbB);
            helper.check(gridA != gridB, "still same grid", qnbA);
        }).thenSucceed());
    }

    private static QuantumBridgeBlockEntity getCore(PlotTestHelper helper, BlockPos pos) {
        BlockEntity be = helper.getBlockEntity(pos);
        helper.check(be instanceof QuantumBridgeBlockEntity, "is not a QNB", pos);
        QuantumBridgeBlockEntity qnb = (QuantumBridgeBlockEntity)be;
        helper.check(qnb.isFormed(), "not formed", pos);
        helper.check(!qnb.isCorner(), "is corner", pos);
        return qnb;
    }

    private static void qnbRing(PlotBuilder plot, BlockPos origin) {
        plot.block(origin, AEBlocks.QUANTUM_LINK);
        QnbTestPlots.ringAround(origin, pos -> plot.block((BlockPos)pos, (BlockDefinition<?>)AEBlocks.QUANTUM_RING));
    }

    private static void ringAround(BlockPos origin, Consumer<BlockPos> consumer) {
        for (int x = -1; x <= 1; ++x) {
            for (int y = -1; y <= 1; ++y) {
                BlockPos pos = origin.offset(x, y, 0);
                if (x == 0 && y == 0) continue;
                consumer.accept(pos);
            }
        }
    }
}

