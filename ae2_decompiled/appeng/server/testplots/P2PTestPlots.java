/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.LightLayer
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.material.Fluid
 *  net.minecraft.world.level.material.Fluids
 *  net.neoforged.neoforge.fluids.IFluidTank
 *  org.apache.commons.lang3.mutable.MutableDouble
 *  org.apache.commons.lang3.mutable.MutableInt
 *  org.apache.commons.lang3.mutable.MutableObject
 *  org.apache.commons.lang3.mutable.MutableShort
 */
package appeng.server.testplots;

import appeng.api.networking.IGrid;
import appeng.blockentity.networking.EnergyCellBlockEntity;
import appeng.blockentity.storage.SkyStoneTankBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEParts;
import appeng.parts.p2p.MEP2PTunnelPart;
import appeng.parts.reporting.AbstractPanelPart;
import appeng.parts.reporting.PanelPart;
import appeng.server.testplots.P2PPlotHelper;
import appeng.server.testplots.TestPlot;
import appeng.server.testplots.TestPlotClass;
import appeng.server.testworld.PlotBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.IFluidTank;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.commons.lang3.mutable.MutableShort;

@TestPlotClass
public class P2PTestPlots {
    @TestPlot(value="p2p_me")
    public static void me(PlotBuilder plot) {
        BlockPos origin = BlockPos.ZERO;
        P2PPlotHelper.placeTunnel(plot, AEParts.ME_P2P_TUNNEL);
        plot.cable(origin.west().west()).part(Direction.WEST, AEParts.IMPORT_BUS);
        plot.chest(origin.west().west().west(), new ItemStack((ItemLike)Items.BEDROCK));
        plot.cable(origin.east().east()).part(Direction.EAST, AEParts.STORAGE_BUS);
        plot.chest(origin.east().east().east(), new ItemStack[0]);
        plot.part(origin, Direction.UP, AEParts.STORAGE_BUS, storageBus -> storageBus.setPriority(1));
        plot.chest(origin.above(), new ItemStack[0]);
        plot.cable(origin.east().above());
        plot.cable(origin.east().east().above()).part(Direction.WEST, AEParts.QUARTZ_FIBER);
        plot.test(helper -> helper.succeedWhen(() -> {
            helper.assertContainerEmpty(origin.west().west().west());
            helper.assertContainerContains(origin.east().east().east(), Items.BEDROCK);
        }));
    }

    @TestPlot(value="p2p_fluids")
    public static void fluid(PlotBuilder plot) {
        BlockPos origin = BlockPos.ZERO;
        P2PPlotHelper.placeTunnel(plot, AEParts.FLUID_P2P_TUNNEL);
        BlockPos outputPos = origin.east().east();
        plot.block(outputPos, AEBlocks.SKY_STONE_TANK);
        plot.cable(origin.west().west()).part(Direction.EAST, AEParts.EXPORT_BUS, part -> part.getConfig().addFilter((Fluid)Fluids.WATER));
        plot.creativeEnergyCell(origin.west().west().below());
        plot.drive(origin.west().west().above()).addCreativeCell().add((Fluid)Fluids.WATER);
        plot.test(helper -> helper.succeedWhen(() -> {
            SkyStoneTankBlockEntity tank = (SkyStoneTankBlockEntity)helper.getBlockEntity(outputPos);
            IFluidTank storage = tank.getTank();
            helper.check(storage.getFluid().is((Fluid)Fluids.WATER), "No water stored");
            helper.check(storage.getFluidAmount() > 0, "No amount >0 stored");
        }));
    }

    @TestPlot(value="p2p_energy")
    public static void energy(PlotBuilder plot) {
        BlockPos origin = BlockPos.ZERO;
        P2PPlotHelper.placeTunnel(plot, AEParts.FE_P2P_TUNNEL);
        plot.block(origin.west().west(), AEBlocks.DEBUG_ENERGY_GEN);
        plot.block(origin.east().east(), AEBlocks.ENERGY_ACCEPTOR);
        BlockPos cellPos = origin.east().east().above();
        plot.block(cellPos, AEBlocks.ENERGY_CELL);
        MutableDouble cellEnergy = new MutableDouble(0.0);
        plot.test(helper -> helper.startSequence().thenIdle(10).thenWaitUntil(() -> {
            EnergyCellBlockEntity cell = (EnergyCellBlockEntity)helper.getBlockEntity(cellPos);
            cellEnergy.setValue(cell.getAECurrentPower());
        }).thenIdle(10).thenWaitUntil(() -> {
            EnergyCellBlockEntity cell = (EnergyCellBlockEntity)helper.getBlockEntity(cellPos);
            helper.check(cell.getAECurrentPower() > cellEnergy.getValue(), "Cell should start charging through the P2P tunnel");
        }).thenSucceed());
    }

    @TestPlot(value="p2p_light")
    public static void light(PlotBuilder plot) {
        BlockPos origin = BlockPos.ZERO;
        P2PPlotHelper.placeTunnel(plot, AEParts.LIGHT_P2P_TUNNEL);
        plot.block(origin.west().west(), Blocks.REDSTONE_LAMP);
        BlockPos leverPos = origin.west().west().above();
        plot.block(leverPos, Blocks.LEVER);
        BlockPos outputPos = origin.east().east();
        plot.test(helper -> {
            MutableInt lightLevel = new MutableInt(0);
            helper.startSequence().thenIdle(20).thenExecute(() -> {
                lightLevel.setValue(helper.getLevel().getBrightness(LightLayer.BLOCK, helper.absolutePos(outputPos)));
                helper.pullLever(leverPos);
            }).thenWaitUntil(() -> {
                int newLightLevel = helper.getLevel().getBrightness(LightLayer.BLOCK, helper.absolutePos(outputPos));
                helper.check(newLightLevel > lightLevel.getValue(), "Light-Level didn't increase");
            }).thenExecute(() -> helper.pullLever(leverPos)).thenWaitUntil(() -> {
                int newLightLevel = helper.getLevel().getBrightness(LightLayer.BLOCK, helper.absolutePos(outputPos));
                helper.check(newLightLevel <= lightLevel.getValue(), "Light-Level didn't reset");
            }).thenSucceed();
        });
    }

    @TestPlot(value="p2p_channel_reconnect_behavior")
    public static void testOutOfChannelReconnectBehavior(PlotBuilder plot) {
        BlockPos origin = BlockPos.ZERO;
        plot.creativeEnergyCell(origin.below());
        plot.block(origin, AEBlocks.CONTROLLER);
        plot.cable(origin.west()).part(Direction.WEST, AEParts.ME_P2P_TUNNEL);
        plot.cable(origin.west().north());
        plot.cable(origin.west().north().west());
        plot.cable(origin.west().north().west().south()).part(Direction.NORTH, AEParts.QUARTZ_FIBER);
        plot.cable(origin.east()).part(Direction.NORTH, AEParts.TERMINAL).part(Direction.SOUTH, AEParts.TERMINAL).part(Direction.UP, AEParts.TERMINAL).part(Direction.DOWN, AEParts.TERMINAL);
        plot.cable(origin.east().east()).part(Direction.NORTH, AEParts.TERMINAL).part(Direction.UP, AEParts.TERMINAL).part(Direction.DOWN, AEParts.TERMINAL).part(Direction.SOUTH, AEParts.TOGGLE_BUS);
        BlockPos leverPos = origin.east().east().north();
        plot.block(leverPos, Blocks.REDSTONE_LAMP);
        plot.leverOn(leverPos.above().above(), Direction.DOWN);
        plot.cable(origin.east().east().south()).part(Direction.UP, AEParts.TERMINAL);
        plot.cable(origin.east().east().east()).part(Direction.EAST, AEParts.CABLE_ANCHOR);
        plot.cable(origin.east().east().east().north());
        plot.cable(origin.east().east().east().north().east()).part(Direction.EAST, AEParts.CABLE_ANCHOR);
        plot.cable(origin.east().east().east().north().east().south());
        plot.cable(origin.east().east().east().north().east().south().east());
        BlockPos p2pOutputPos = origin.east().east().east().north().east().south().east().north();
        plot.cable(p2pOutputPos).part(Direction.EAST, AEParts.ME_P2P_TUNNEL);
        plot.cable(p2pOutputPos.east()).part(Direction.UP, AEParts.MONITOR);
        plot.test(helper -> {
            MutableShort freq = new MutableShort();
            MutableObject lightPanel = new MutableObject();
            helper.startSequence().thenWaitUntil(() -> helper.getGrid(origin)).thenExecute(() -> {
                lightPanel.setValue((Object)helper.getPart(p2pOutputPos.east(), Direction.UP, PanelPart.class));
                IGrid grid = helper.getGrid(origin);
                BlockPos inputPos = helper.absolutePos(origin.west());
                BlockPos outputPos = helper.absolutePos(p2pOutputPos);
                freq.setValue(P2PPlotHelper.linkTunnels(grid, MEP2PTunnelPart.class, inputPos, outputPos));
            }).thenWaitUntil(() -> helper.check(((AbstractPanelPart)lightPanel.getValue()).getMainNode().isOnline(), "The panel should initially be on")).thenExecute(() -> helper.pullLever(leverPos.above())).thenWaitUntil(() -> {
                MEP2PTunnelPart inputTunnel = helper.getPart(p2pOutputPos, Direction.EAST, MEP2PTunnelPart.class);
                if (inputTunnel.getMainNode().isOnline()) {
                    helper.fail("should be offline", p2pOutputPos);
                }
            }).thenWaitUntil(() -> {
                if (((AbstractPanelPart)lightPanel.getValue()).getMainNode().isOnline()) {
                    helper.fail("should be offline", p2pOutputPos.east());
                }
            }).thenExecute(() -> helper.pullLever(leverPos.above())).thenWaitUntil(() -> {
                MEP2PTunnelPart inputTunnel = helper.getPart(p2pOutputPos, Direction.EAST, MEP2PTunnelPart.class);
                if (!inputTunnel.getMainNode().isOnline()) {
                    helper.fail("should be online", p2pOutputPos);
                }
            }).thenWaitUntil(() -> {
                if (!((AbstractPanelPart)lightPanel.getValue()).getMainNode().isOnline()) {
                    helper.fail("should be online", p2pOutputPos.east());
                }
            }).thenSucceed();
        });
    }
}

