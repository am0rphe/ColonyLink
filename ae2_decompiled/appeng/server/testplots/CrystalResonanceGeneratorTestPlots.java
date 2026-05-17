/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.BlockStateProperties
 *  net.minecraft.world.level.block.state.properties.Property
 */
package appeng.server.testplots;

import appeng.blockentity.networking.CrystalResonanceGeneratorBlockEntity;
import appeng.blockentity.networking.EnergyCellBlockEntity;
import appeng.core.AEConfig;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEParts;
import appeng.server.testplots.TestPlot;
import appeng.server.testplots.TestPlotClass;
import appeng.server.testworld.PlotBuilder;
import appeng.server.testworld.PlotTestHelper;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;

@TestPlotClass
public class CrystalResonanceGeneratorTestPlots {
    @TestPlot(value="crg_charges_cell")
    public static void chargesCell(PlotBuilder plot) {
        BlockPos origin = BlockPos.ZERO;
        plot.block(origin, AEBlocks.ENERGY_CELL);
        plot.blockState(origin.above(), (BlockState)AEBlocks.CRYSTAL_RESONANCE_GENERATOR.block().defaultBlockState().setValue((Property)BlockStateProperties.FACING, (Comparable)Direction.UP));
        CrystalResonanceGeneratorTestPlots.testPassiveGenerationRate(plot, origin);
    }

    @TestPlot(value="crg_suppression_generation")
    public static void suppression(PlotBuilder plot) {
        BlockPos origin = BlockPos.ZERO;
        CrystalResonanceGeneratorTestPlots.setupSubnetWithCrgs(plot, origin);
        CrystalResonanceGeneratorTestPlots.testPassiveGenerationRate(plot, origin);
    }

    @TestPlot(value="crg_suppression_flag")
    public static void suppressionFlag(PlotBuilder plot) {
        BlockPos origin = BlockPos.ZERO;
        CrystalResonanceGeneratorTestPlots.setupSubnetWithCrgs(plot, origin);
        plot.test(helper -> helper.startSequence().thenWaitUntil(helper::checkAllInitialized).thenExecute(() -> {
            CrystalResonanceGeneratorBlockEntity crg1 = (CrystalResonanceGeneratorBlockEntity)helper.getBlockEntity(origin.above());
            CrystalResonanceGeneratorBlockEntity crg2 = (CrystalResonanceGeneratorBlockEntity)helper.getBlockEntity(origin.east().east());
            if (crg1.isSuppressed() && crg2.isSuppressed()) {
                helper.check(false, "not both should be suppressed", helper.relativePos(crg1.getBlockPos()));
            } else if (!crg1.isSuppressed() && !crg2.isSuppressed()) {
                helper.check(false, "one of both should be suppressed", helper.relativePos(crg1.getBlockPos()));
            }
        }).thenSucceed());
    }

    private static void setupSubnetWithCrgs(PlotBuilder plot, BlockPos origin) {
        plot.block(origin, AEBlocks.ENERGY_CELL);
        plot.blockState(origin.above(), (BlockState)AEBlocks.CRYSTAL_RESONANCE_GENERATOR.block().defaultBlockState().setValue((Property)BlockStateProperties.FACING, (Comparable)Direction.UP));
        plot.cable(origin.east()).part(Direction.EAST, AEParts.QUARTZ_FIBER);
        plot.blockState(origin.east().east(), (BlockState)AEBlocks.CRYSTAL_RESONANCE_GENERATOR.block().defaultBlockState().setValue((Property)BlockStateProperties.FACING, (Comparable)Direction.EAST));
    }

    private static void testPassiveGenerationRate(PlotBuilder plot, final BlockPos origin) {
        plot.test(new Consumer<PlotTestHelper>(){
            double lastPower;
            EnergyCellBlockEntity cell;

            @Override
            public void accept(PlotTestHelper helper) {
                double rate = AEConfig.instance().getCrystalResonanceGeneratorRate();
                helper.startSequence().thenWaitUntil(helper::checkAllInitialized).thenExecute(() -> {
                    this.cell = (EnergyCellBlockEntity)helper.getBlockEntity(origin);
                    this.lastPower = this.cell.getAECurrentPower();
                }).thenExecuteAfter(1, () -> {
                    double expected;
                    double now = this.cell.getAECurrentPower();
                    helper.check(Math.abs(now - (expected = this.lastPower + rate)) < 0.01, "Expected " + expected + " AE, but has " + now, origin);
                    this.lastPower = now;
                }).thenExecuteAfter(1, () -> {
                    double expected;
                    double now = this.cell.getAECurrentPower();
                    helper.check(Math.abs(now - (expected = this.lastPower + rate)) < 0.01, "Expected " + expected + " AE, but has " + now, origin);
                }).thenSucceed();
            }
        });
    }
}

