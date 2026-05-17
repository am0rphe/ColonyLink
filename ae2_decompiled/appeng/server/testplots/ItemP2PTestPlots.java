/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.gametest.framework.GameTestHelper
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 */
package appeng.server.testplots;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEParts;
import appeng.parts.AEBasePart;
import appeng.server.testplots.P2PPlotHelper;
import appeng.server.testplots.PosAndSide;
import appeng.server.testplots.TestPlot;
import appeng.server.testplots.TestPlotClass;
import appeng.server.testworld.CableBuilder;
import appeng.server.testworld.PlotBuilder;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

@TestPlotClass
public class ItemP2PTestPlots {
    @TestPlot(value="p2p_items")
    public static void item(PlotBuilder plot) {
        BlockPos origin = BlockPos.ZERO;
        P2PPlotHelper.placeTunnel(plot, AEParts.ITEM_P2P_TUNNEL);
        plot.hopper(origin.west().west(), Direction.EAST, new ItemStack((ItemLike)Items.BEDROCK));
        BlockPos chestPos = origin.east().east();
        plot.chest(chestPos, new ItemStack[0]);
        plot.test(helper -> helper.startSequence().thenWaitUntil(() -> helper.assertContainerContains(chestPos, Items.BEDROCK)).thenSucceed());
    }

    @TestPlot(value="p2p_recursive_item")
    public static void recursiveItemP2P(PlotBuilder plot) {
        BlockPos origin = BlockPos.ZERO;
        plot.block(origin, AEBlocks.DEBUG_ITEM_GEN);
        plot.creativeEnergyCell(origin.south().above().above());
        BlockPos curPos = origin.south();
        for (int i = 0; i < 5; ++i) {
            ItemP2PTestPlots.placeSubnet(plot, curPos);
            curPos = curPos.south(6);
        }
        plot.test(GameTestHelper::succeed);
    }

    private static void placeSubnet(PlotBuilder plot, BlockPos origin) {
        ArrayList<PosAndSide> outputTunnels = new ArrayList<PosAndSide>();
        for (int i = 0; i < 6; ++i) {
            boolean last;
            BlockPos p = origin.relative(Direction.SOUTH, i);
            CableBuilder cb = plot.cable(p);
            cb.part(Direction.DOWN, AEParts.ITEM_P2P_TUNNEL);
            outputTunnels.add(PosAndSide.down(p));
            boolean first = i == 0;
            boolean bl = last = i + 1 >= 6;
            if (first) {
                cb.part(Direction.NORTH, AEParts.ITEM_P2P_TUNNEL);
            } else if (last) {
                cb.part(Direction.SOUTH, AEParts.ITEM_P2P_TUNNEL);
                outputTunnels.add(PosAndSide.south(p));
            }
            if (first || last) {
                cb.part(Direction.UP, AEParts.QUARTZ_FIBER);
            }
            plot.hopper(p.below(), Direction.DOWN);
            plot.block(p.below().below(), AEBlocks.CONDENSER);
        }
        plot.cable(origin.above());
        plot.cable(origin.south(5).above());
        plot.afterGridInitAt(origin, (grid, gridNode) -> {
            BlockPos absOrigin = ((AEBasePart)gridNode.getOwner()).getBlockEntity().getBlockPos();
            BlockPos relativeOffset = absOrigin.offset(-origin.getX(), -origin.getY(), -origin.getZ());
            P2PPlotHelper.linkTunnels(grid, PosAndSide.north(origin.offset((Vec3i)relativeOffset)), outputTunnels.stream().map(p -> p.offset((Vec3i)relativeOffset)).toList());
        });
    }
}

