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

import appeng.api.networking.IGrid;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.ILinkStatus;
import appeng.block.misc.GrowthAcceleratorBlock;
import appeng.blockentity.misc.GrowthAcceleratorBlockEntity;
import appeng.blockentity.storage.MEChestBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.server.testplots.TestPlot;
import appeng.server.testplots.TestPlotClass;
import appeng.server.testworld.PlotBuilder;
import appeng.server.testworld.PlotTestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;

@TestPlotClass
public class ExternalEnergyTestPlots {
    private static final BlockPos ORIGIN = BlockPos.ZERO;

    @TestPlot(value="fe_energy_acceptor_block")
    public static void testEnergyAcceptorBlock(PlotBuilder plot) {
        ExternalEnergyTestPlots.placeForgeEnergyGenerator(plot);
        plot.block(ORIGIN, AEBlocks.ENERGY_ACCEPTOR);
        ExternalEnergyTestPlots.testGridIsReceivingEnergy(plot);
    }

    @TestPlot(value="fe_energy_acceptor_part")
    public static void testEnergyAcceptorPart(PlotBuilder plot) {
        ExternalEnergyTestPlots.placeForgeEnergyGenerator(plot);
        plot.cable(ORIGIN).part(Direction.DOWN, AEParts.ENERGY_ACCEPTOR);
        ExternalEnergyTestPlots.testGridIsReceivingEnergy(plot);
    }

    @TestPlot(value="fe_controller")
    public static void testController(PlotBuilder plot) {
        ExternalEnergyTestPlots.placeForgeEnergyGenerator(plot);
        plot.block(ORIGIN, AEBlocks.CONTROLLER);
        ExternalEnergyTestPlots.testGridIsReceivingEnergy(plot);
    }

    @TestPlot(value="fe_inscriber")
    public static void testInscriber(PlotBuilder plot) {
        ExternalEnergyTestPlots.placeForgeEnergyGenerator(plot);
        plot.blockEntity(ORIGIN, AEBlocks.INSCRIBER, inscriber -> {
            inscriber.getInternalInventory().insertItem(0, AEItems.SILICON_PRESS.stack(), false);
            inscriber.getInternalInventory().insertItem(2, AEItems.SILICON.stack(), false);
        });
        plot.test(helper -> helper.startSequence().thenWaitUntil(helper::checkAllInitialized).thenWaitUntil(() -> {
            KeyCounter content = helper.countContainerContentAt(ORIGIN);
            helper.assertEquals(ORIGIN, 1L, content.get(AEItemKey.of(AEItems.SILICON_PRINT)));
        }).thenExecuteAfter(20, () -> ExternalEnergyTestPlots.checkGridHasNoEnergy(helper)).thenSucceed());
    }

    @TestPlot(value="fe_chest")
    public static void testChest(PlotBuilder plot) {
        ExternalEnergyTestPlots.placeForgeEnergyGenerator(plot);
        plot.blockEntity(ORIGIN, AEBlocks.ME_CHEST, chest -> chest.getInternalInventory().addItems(AEItems.ITEM_CELL_1K.stack()));
        plot.test(helper -> helper.startSequence().thenWaitUntil(helper::checkAllInitialized).thenWaitUntil(() -> {
            MEChestBlockEntity chest = (MEChestBlockEntity)helper.getBlockEntity(ORIGIN);
            helper.check(chest.isPowered(), "should be powered", ORIGIN);
            ILinkStatus linkStatus = chest.getLinkStatus();
            helper.check(linkStatus.connected(), "link status should be connected: " + String.valueOf(linkStatus.statusDescription()), ORIGIN);
        }).thenExecuteAfter(20, () -> ExternalEnergyTestPlots.checkGridHasNoEnergy(helper)).thenSucceed());
    }

    @TestPlot(value="fe_charger")
    public static void testCharger(PlotBuilder plot) {
        ExternalEnergyTestPlots.placeForgeEnergyGenerator(plot);
        plot.blockEntity(ORIGIN, AEBlocks.CHARGER, charger -> charger.getInternalInventory().insertItem(0, AEItems.CERTUS_QUARTZ_CRYSTAL.stack(), false));
        plot.test(helper -> helper.startSequence().thenWaitUntil(helper::checkAllInitialized).thenWaitUntil(() -> {
            KeyCounter content = helper.countContainerContentAt(ORIGIN);
            helper.assertEquals(ORIGIN, 1L, content.get(AEItemKey.of(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED)));
        }).thenExecuteAfter(20, () -> ExternalEnergyTestPlots.checkGridHasNoEnergy(helper)).thenSucceed()).maxTicks(300);
    }

    @TestPlot(value="fe_growth_accelerator")
    public static void testGrowthAccelerator(PlotBuilder plot) {
        ExternalEnergyTestPlots.placeForgeEnergyGenerator(plot);
        plot.blockState(ORIGIN, (BlockState)AEBlocks.GROWTH_ACCELERATOR.block().defaultBlockState().setValue((Property)BlockStateProperties.FACING, (Comparable)Direction.UP));
        plot.test(helper -> helper.startSequence().thenWaitUntil(helper::checkAllInitialized).thenWaitUntil(() -> {
            GrowthAcceleratorBlockEntity accel = (GrowthAcceleratorBlockEntity)helper.getBlockEntity(ORIGIN);
            helper.check(accel.isPowered(), "should be powered", ORIGIN);
        }).thenWaitUntil(() -> helper.assertBlockProperty(ORIGIN, (Property)GrowthAcceleratorBlock.POWERED, Boolean.valueOf(true))).thenExecuteAfter(20, () -> ExternalEnergyTestPlots.checkGridHasNoEnergy(helper)).thenSucceed());
    }

    private static void testGridIsReceivingEnergy(PlotBuilder plot) {
        plot.test(helper -> helper.startSequence().thenWaitUntil(helper::checkAllInitialized).thenWaitUntil(() -> {
            IGrid grid = helper.getGrid(BlockPos.ZERO);
            double power = grid.getEnergyService().getStoredPower();
            helper.check(power > 0.0, "grid should contain energy", ORIGIN);
        }).thenSucceed());
    }

    private static void checkGridHasNoEnergy(PlotTestHelper helper) {
        IGrid grid = helper.getGrid(BlockPos.ZERO);
        double power = grid.getEnergyService().getStoredPower();
        helper.check(power == 0.0, "grid should not contain energy (has " + power + ")", ORIGIN);
    }

    private static void placeForgeEnergyGenerator(PlotBuilder plot) {
        plot.blockEntity(ORIGIN.below(), AEBlocks.DEBUG_ENERGY_GEN, generator -> generator.setGenerationRate(128));
    }
}

