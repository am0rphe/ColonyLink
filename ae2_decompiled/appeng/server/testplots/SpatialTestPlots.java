/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.ButtonBlock
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package appeng.server.testplots;

import appeng.api.implementations.items.ISpatialStorageCell;
import appeng.blockentity.spatial.SpatialIOPortBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.server.testplots.TestPlot;
import appeng.server.testplots.TestPlotClass;
import appeng.server.testworld.PlotBuilder;
import appeng.server.testworld.PlotTestHelper;
import appeng.spatial.SpatialStoragePlot;
import appeng.spatial.SpatialStoragePlotManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@TestPlotClass
public final class SpatialTestPlots {
    private SpatialTestPlots() {
    }

    @TestPlot(value="controller_inside_scs")
    public static void controllerInsideScs(PlotBuilder plot) {
        plot.creativeEnergyCell("0 0 0");
        plot.block("[1,10] 0 0", AEBlocks.SPATIAL_PYLON);
        plot.block("0 [1,10] 0", AEBlocks.SPATIAL_PYLON);
        plot.block("0 0 [1,10]", AEBlocks.SPATIAL_PYLON);
        plot.blockEntity("-1 0 0", AEBlocks.SPATIAL_IO_PORT, port -> port.getInternalInventory().insertItem(0, AEItems.SPATIAL_CELL128.stack(), false));
        BlockPos leverPos = plot.leverOn(new BlockPos(-1, 0, 0), Direction.WEST);
        plot.creativeEnergyCell("[4,6] 3 [4,6]");
        for (int x = -1; x <= 1; ++x) {
            for (int y = -1; y <= 1; ++y) {
                for (int z = -1; z <= 1; ++z) {
                    boolean edge;
                    boolean bl = edge = Math.abs(x) + Math.abs(y) + Math.abs(z) >= 2;
                    if (!edge) continue;
                    plot.block(new BlockPos(5 + x, 5 + y, 5 + z), AEBlocks.CONTROLLER);
                }
            }
        }
        plot.test(helper -> helper.startSequence().thenIdle(5).thenExecute(() -> helper.pullLever(leverPos)).thenIdle(5).thenSucceed());
    }

    @TestPlot(value="crafting_cpu_inside_scs")
    public static void craftingCpuInsideScs(PlotBuilder plot) {
        plot.creativeEnergyCell("0 0 0");
        plot.block("[1,10] 0 0", AEBlocks.SPATIAL_PYLON);
        plot.block("0 [1,10] 0", AEBlocks.SPATIAL_PYLON);
        plot.block("0 0 [1,10]", AEBlocks.SPATIAL_PYLON);
        plot.blockEntity("-1 0 0", AEBlocks.SPATIAL_IO_PORT, port -> port.getInternalInventory().insertItem(0, AEItems.SPATIAL_CELL128.stack(), false));
        BlockPos leverPos = plot.leverOn(new BlockPos(-1, 0, 0), Direction.WEST);
        plot.creativeEnergyCell("3 0 3");
        plot.block("[2,4] [1,3] [2,4]", AEBlocks.CRAFTING_STORAGE_64K);
        plot.test(helper -> helper.startSequence().thenIdle(5).thenExecute(() -> helper.pullLever(leverPos)).thenIdle(5).thenSucceed());
    }

    @TestPlot(value="spatial_entity_storage")
    public static void storeAndRetrieveEntities(PlotBuilder plot) {
        BlockPos chickenPos = new BlockPos(1, 1, 1);
        BlockPos ioPortPos = new BlockPos(-1, 0, 0);
        plot.creativeEnergyCell("0 0 0");
        plot.block("[1,2] 0 0", AEBlocks.SPATIAL_PYLON);
        plot.block("0 [1,2] 0", AEBlocks.SPATIAL_PYLON);
        plot.block("0 0 [1,2]", AEBlocks.SPATIAL_PYLON);
        plot.block(chickenPos.below(), Blocks.STONE);
        plot.block(chickenPos.above(), Blocks.STONE);
        for (int i = 0; i < 4; ++i) {
            Direction dir = Direction.from2DDataValue((int)i);
            plot.block(chickenPos.relative(dir), Blocks.GLASS);
            plot.block(chickenPos.relative(dir).above(), Blocks.GLASS);
        }
        plot.blockEntity(ioPortPos, AEBlocks.SPATIAL_IO_PORT, port -> port.getInternalInventory().insertItem(0, AEItems.SPATIAL_CELL2.stack(), false));
        BlockPos buttonPos = plot.buttonOn(ioPortPos, Direction.WEST);
        plot.test(helper -> helper.startSequence().thenExecute(() -> {
            helper.spawn(EntityType.CHICKEN, Vec3.atBottomCenterOf((Vec3i)chickenPos.above()));
            helper.spawnItem(Items.OBSIDIAN, Vec3.atCenterOf((Vec3i)chickenPos));
        }).thenWaitUntil(() -> {
            helper.assertItemEntityCountIs(Items.OBSIDIAN, chickenPos, 1.0, 1);
            helper.assertEntitiesPresent(EntityType.CHICKEN, chickenPos, 1, 1.0);
        }).thenIdle(5).thenWaitUntil(helper::checkAllInitialized).thenExecute(() -> helper.pressButton(buttonPos)).thenWaitUntil(() -> {
            helper.assertItemEntityCountIs(Items.OBSIDIAN, chickenPos, 1.0, 0);
            helper.assertEntitiesPresent(EntityType.CHICKEN, chickenPos, 0, 1.0);
        }).thenExecute(() -> {
            ItemStack cell = SpatialTestPlots.getCellFromSpatialIoPortOutput(helper, ioPortPos);
            SpatialTestPlots.insertCell(helper, ioPortPos, cell);
        }).thenWaitUntil(() -> helper.assertBlockProperty(buttonPos, (Property)ButtonBlock.POWERED, Boolean.valueOf(false))).thenExecute(() -> helper.pressButton(buttonPos)).thenWaitUntil(() -> {
            helper.assertItemEntityCountIs(Items.OBSIDIAN, chickenPos, 1.0, 1);
            helper.assertEntitiesPresent(EntityType.CHICKEN, chickenPos, 1, 1.0);
        }).thenSucceed());
    }

    private static ItemStack getCellFromSpatialIoPortOutput(PlotTestHelper helper, BlockPos ioPortPos) {
        SpatialIOPortBlockEntity spatialIoPort = (SpatialIOPortBlockEntity)helper.getBlockEntity(ioPortPos);
        ItemStack cell = spatialIoPort.getInternalInventory().extractItem(1, 1, false);
        helper.check(AEItems.SPATIAL_CELL2.is(cell), "no spatial cell in output slot", ioPortPos);
        return cell;
    }

    private static void insertCell(PlotTestHelper helper, BlockPos ioPortPos, ItemStack cell) {
        SpatialIOPortBlockEntity spatialIoPort = (SpatialIOPortBlockEntity)helper.getBlockEntity(ioPortPos);
        spatialIoPort.getInternalInventory().insertItem(0, cell, false);
    }

    private static PlotInfo getPlotInfo(PlotTestHelper helper, BlockPos ioPortPos, ItemStack cell) {
        helper.check(cell.getItem() instanceof ISpatialStorageCell, "cell is not a spatial cell", ioPortPos);
        ISpatialStorageCell spatialCell = (ISpatialStorageCell)cell.getItem();
        int plotId = spatialCell.getAllocatedPlotId(cell);
        SpatialStoragePlot plot = SpatialStoragePlotManager.INSTANCE.getPlot(plotId);
        helper.check(plot != null, "plot not found", ioPortPos);
        return new PlotInfo(SpatialStoragePlotManager.INSTANCE.getLevel(), new AABB(Vec3.atLowerCornerOf((Vec3i)plot.getOrigin()), Vec3.atLowerCornerOf((Vec3i)plot.getOrigin().offset((Vec3i)plot.getSize()))), plot.getOrigin());
    }

    record PlotInfo(ServerLevel level, AABB bounds, BlockPos origin) {
    }
}

