/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 */
package appeng.server.testplots;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEParts;
import appeng.me.service.EnergyService;
import appeng.server.testplots.TestPlot;
import appeng.server.testplots.TestPlotClass;
import appeng.server.testworld.PlotBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

@TestPlotClass
public final class SubnetPlots {
    private static final AEItemKey STICK = AEItemKey.of((ItemLike)Items.STICK);

    private SubnetPlots() {
    }

    @TestPlot(value="subnet")
    public static void subnet(PlotBuilder plot) {
        BlockPos origin = BlockPos.ZERO;
        plot.creativeEnergyCell(origin.below());
        plot.cable(origin).part(Direction.NORTH, AEParts.TERMINAL).part(Direction.SOUTH, AEParts.STORAGE_BUS).part(Direction.EAST, AEParts.QUARTZ_FIBER);
        plot.cable(origin.east());
        plot.cable(origin.east().south());
        plot.cable(origin.south()).part(Direction.NORTH, AEParts.INTERFACE);
        plot.storageDrive(origin.south().above());
        BlockPos subNetPos = origin.south();
        BlockPos mainNetPos = origin;
        plot.test(helper -> helper.startSequence().thenWaitUntil(() -> helper.getGrid(subNetPos)).thenWaitUntil(() -> helper.getGrid(mainNetPos)).thenExecute(() -> {
            IGrid mainGrid = helper.getGrid(mainNetPos);
            IStorageService storageService = mainGrid.getStorageService();
            long inserted = storageService.getInventory().insert(STICK, 1L, Actionable.MODULATE, null);
            helper.check(inserted == 1L, "inserted != 1: " + inserted, mainNetPos);
            KeyCounter inventory = storageService.getInventory().getAvailableStacks();
            helper.check(inventory.get(STICK) == 1L, "stick not present", mainNetPos);
        }).thenIdle(10).thenExecute(() -> {
            IGrid mainGrid = helper.getGrid(mainNetPos);
            IStorageService storageService = mainGrid.getStorageService();
            KeyCounter inventory = storageService.getInventory().getAvailableStacks();
            helper.check(inventory.get(STICK) == 1L, "stick not present in tick #10", mainNetPos);
            long extracted = storageService.getInventory().extract(STICK, 1L, Actionable.MODULATE, null);
            helper.check(extracted == 1L, "unable to extract", mainNetPos);
        }).thenSucceed());
    }

    @TestPlot(value="energy_overlay")
    public static void energy_overlay(PlotBuilder plot) {
        BlockPos origin = BlockPos.ZERO;
        plot.cable(origin).part(Direction.EAST, AEParts.QUARTZ_FIBER);
        plot.block(origin.east(), AEBlocks.DENSE_ENERGY_CELL);
        plot.block(origin.west(), AEBlocks.ENERGY_CELL);
        plot.cable(origin.west().west()).part(Direction.EAST, AEParts.QUARTZ_FIBER);
        plot.test(helper -> helper.startSequence().thenWaitUntil(() -> helper.getGrid(origin)).thenExecute(() -> {
            IGrid denseCellGrid = helper.getGrid(origin.east());
            IGrid cellGrid = helper.getGrid(origin.west());
            IGrid noCellGrid = helper.getGrid(origin.west().west());
            EnergyService denseCellService = (EnergyService)denseCellGrid.getEnergyService();
            EnergyService cellService = (EnergyService)cellGrid.getEnergyService();
            EnergyService noCellService = (EnergyService)noCellGrid.getEnergyService();
            denseCellService.injectPower(1.0, Actionable.MODULATE);
            helper.check(SubnetPlots.getLocalStoredPower(denseCellService) == 1.0, "expect power = 1", origin.east());
            cellService.injectPower(1.0, Actionable.MODULATE);
            helper.check(SubnetPlots.getLocalStoredPower(denseCellService) == 2.0, "expect power = 2", origin.east());
            noCellService.injectPower(1.0, Actionable.MODULATE);
            helper.check(SubnetPlots.getLocalStoredPower(denseCellService) == 3.0, "expect power = 3", origin.east());
            denseCellService.extractAEPower(1.0, Actionable.MODULATE, PowerMultiplier.ONE);
            helper.check(SubnetPlots.getLocalStoredPower(denseCellService) == 2.0, "expect power = 2", origin.east());
            cellService.extractAEPower(1.0, Actionable.MODULATE, PowerMultiplier.ONE);
            helper.check(SubnetPlots.getLocalStoredPower(denseCellService) == 1.0, "expect power = 1", origin.east());
            noCellService.extractAEPower(1.0, Actionable.MODULATE, PowerMultiplier.ONE);
            helper.check(SubnetPlots.getLocalStoredPower(denseCellService) == 0.0, "expect power = 0", origin.east());
        }).thenSucceed());
    }

    @TestPlot(value="multi_storage_bus")
    public static void multi_storage_bus(PlotBuilder plot) {
        BlockPos origin = BlockPos.ZERO;
        plot.storageDrive(origin.west());
        plot.block(origin, AEBlocks.INTERFACE);
        plot.block(origin.east(), AEBlocks.INTERFACE);
        plot.creativeEnergyCell(origin.east().east());
        BlockPos subnetOrigin = origin.north();
        plot.cable(subnetOrigin).part(Direction.SOUTH, AEParts.STORAGE_BUS, bus -> bus.getConfig().addFilter((ItemLike)Items.RED_CONCRETE));
        plot.cable(subnetOrigin.east()).part(Direction.SOUTH, AEParts.STORAGE_BUS, bus -> bus.getConfig().addFilter((ItemLike)Items.BLUE_CONCRETE));
        plot.cable(subnetOrigin.east().east()).part(Direction.SOUTH, AEParts.QUARTZ_FIBER).part(Direction.NORTH, AEParts.TERMINAL);
        plot.test(helper -> helper.startSequence().thenWaitUntil(() -> {
            helper.getGrid(origin);
            helper.getGrid(subnetOrigin);
        }).thenExecute(() -> {
            IGrid mainGrid = helper.getGrid(origin);
            MEStorage mainInv = mainGrid.getStorageService().getInventory();
            mainInv.insert(AEItemKey.of((ItemLike)Items.RED_CONCRETE), 64L, Actionable.MODULATE, IActionSource.empty());
            mainInv.insert(AEItemKey.of((ItemLike)Items.BLUE_CONCRETE), 64L, Actionable.MODULATE, IActionSource.empty());
        }).thenIdle(1).thenExecute(() -> {
            helper.assertNetworkContains(subnetOrigin, (ItemLike)Items.RED_CONCRETE);
            helper.assertNetworkContains(subnetOrigin, (ItemLike)Items.BLUE_CONCRETE);
        }).thenSucceed()).withSkyAccess();
    }

    private static double getLocalStoredPower(EnergyService service) {
        service.refreshPower();
        return service.getStoredPower();
    }
}

