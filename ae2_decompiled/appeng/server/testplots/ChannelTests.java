/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.levelgen.structure.BoundingBox
 */
package appeng.server.testplots;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridConnectionVisitor;
import appeng.api.networking.IGridNode;
import appeng.core.definitions.AEBlocks;
import appeng.server.testplots.TestPlot;
import appeng.server.testplots.TestPlotClass;
import appeng.server.testworld.PlotBuilder;
import appeng.server.testworld.PlotTestHelper;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

@TestPlotClass
public class ChannelTests {
    @TestPlot(value="channel_assignment_test")
    public static void channelAssignmentTest(PlotBuilder plot) {
        plot.block("[-1,1] 0 0", AEBlocks.CONTROLLER);
        plot.creativeEnergyCell("0 -1 0");
        plot.block("0 1 0", AEBlocks.ME_CHEST);
        plot.denseCable("0 0 [1,3]");
        plot.block("0 0 4", AEBlocks.PATTERN_PROVIDER);
        plot.cable("[1,2] 0 3");
        plot.block("3 [0,1] [3,4]", AEBlocks.CRAFTING_STORAGE_4K);
        plot.cable("[4,9] 0 3");
        plot.block("5 1 3", AEBlocks.INTERFACE);
        plot.block("5 -1 3", AEBlocks.INTERFACE);
        plot.block("5 0 2", AEBlocks.INTERFACE);
        plot.block("5 0 4", AEBlocks.INTERFACE);
        plot.block("7 -1 3", AEBlocks.INTERFACE);
        plot.block("7 0 2", AEBlocks.INTERFACE);
        plot.block("7 0 4", AEBlocks.INTERFACE);
        plot.block("10 0 3", AEBlocks.CRAFTING_STORAGE_256K);
        plot.test(helper -> helper.startSequence().thenWaitUntil(() -> {
            IGrid grid = helper.getGrid(BlockPos.ZERO);
            helper.check(!grid.getPathingService().isNetworkBooting(), "Network is still booting");
        }).thenExecute(() -> {
            ChannelChecker checker = new ChannelChecker(plot, (PlotTestHelper)((Object)helper));
            checker.node("[-1,1] 0 0", 0);
            checker.connection("-1 0 0", "0 0 0", 0);
            checker.connection("0 0 0", "1 0 0", 0);
            checker.leafNode("0 -1 0", 0);
            checker.leafNode("0 1 0", 1);
            checker.node("0 0 [1,3]", 9);
            checker.connection("0 0 0", "0 0 1", 9);
            checker.connection("0 0 1", "0 0 2", 9);
            checker.connection("0 0 2", "0 0 3", 9);
            checker.leafNode("0 0 4", 1);
            checker.node("[1,2] 0 3", 8);
            checker.connection("0 0 3", "1 0 3", 8);
            checker.connection("1 0 3", "2 0 3", 8);
            checker.connection("2 0 3", "3 0 3", 8);
            checker.node("3 0 3", 8);
            checker.node("3 1 3", 1);
            checker.node("3 [0,1] 4", 1);
            checker.connection("3 0 [3,4]", "3 1 [3,4]", 0);
            checker.connection("3 [0,1] 3", "3 [0,1] 4", 0);
            checker.connection("3 0 3", "4 0 3", 7);
            checker.node("4 0 3", 7);
            checker.connection("4 0 3", "5 0 3", 7);
            checker.node("5 0 3", 7);
            checker.connection("5 0 3", "6 0 3", 3);
            checker.node("6 0 3", 3);
            checker.connection("6 0 3", "7 0 3", 3);
            checker.node("7 0 3", 3);
            checker.connection("7 0 3", "8 0 3", 0);
            checker.node("8 0 3", 0);
            checker.connection("8 0 3", "9 0 3", 0);
            checker.node("9 0 3", 0);
            checker.leafNode("5 1 3", 1);
            checker.leafNode("5 -1 3", 1);
            checker.leafNode("5 0 2", 1);
            checker.leafNode("5 0 4", 1);
            checker.leafNode("7 -1 3", 1);
            checker.leafNode("7 0 2", 1);
            checker.leafNode("7 0 4", 1);
            checker.leafNode("10 0 3", 0);
            checker.ensureEverythingWasChecked();
        }).thenSucceed());
    }

    private static class ChannelChecker {
        private final PlotBuilder plot;
        private final PlotTestHelper helper;
        private final Set<IGridNode> nodes = new HashSet<IGridNode>();
        private final Set<IGridConnection> connections = new HashSet<IGridConnection>();

        private ChannelChecker(PlotBuilder plot, PlotTestHelper helper) {
            this.plot = plot;
            this.helper = helper;
            helper.getGrid(BlockPos.ZERO).getPivot().beginVisit(new IGridConnectionVisitor(){

                @Override
                public void visitConnection(IGridConnection n) {
                    connections.add(n);
                }

                @Override
                public boolean visitNode(IGridNode n) {
                    nodes.add(n);
                    return true;
                }
            });
        }

        private void forEachInBb(String bb, Consumer<BlockPos> action) {
            BlockPos.betweenClosedStream((BoundingBox)this.plot.bb(bb)).forEach(action);
        }

        private void checkNode(BlockPos pos, IGridNode node, int expectedChannelCount) {
            if (this.nodes.contains(node)) {
                if (node.getUsedChannels() != expectedChannelCount) {
                    this.helper.fail("Node has wrong channel count. Expected %d. Got %d.".formatted(expectedChannelCount, node.getUsedChannels()), pos);
                }
                this.nodes.remove(node);
            } else {
                this.helper.fail("Node is not in the grid or it was already checked", pos);
            }
        }

        private void checkConnection(BlockPos pos, IGridConnection connection, int expectedChannelCount) {
            if (this.connections.contains(connection)) {
                if (connection.getUsedChannels() != expectedChannelCount) {
                    this.helper.fail("Connection has wrong channel count. Expected %d. Got %d.".formatted(expectedChannelCount, connection.getUsedChannels()), pos);
                }
                this.connections.remove(connection);
            } else {
                this.helper.fail("Connection is not in the grid or it was already checked", pos);
            }
        }

        public void node(String bb, int expectedChannelCount) {
            this.forEachInBb(bb, pos -> {
                IGridNode node = this.helper.getGridNode((BlockPos)pos);
                this.checkNode((BlockPos)pos, node, expectedChannelCount);
            });
        }

        public void leafNode(String bb, int expectedChannelCount) {
            this.forEachInBb(bb, pos -> {
                IGridNode node = this.helper.getGridNode((BlockPos)pos);
                this.checkNode((BlockPos)pos, node, expectedChannelCount);
                this.helper.check(node.getConnections().size() == 1, "Node does not have exactly one connection", (BlockPos)pos);
                this.checkConnection((BlockPos)pos, node.getConnections().getFirst(), expectedChannelCount);
            });
        }

        public void connection(String bb, String bb2, int expectedChannelCount) {
            AtomicBoolean foundAny = new AtomicBoolean();
            this.forEachInBb(bb, pos -> {
                IGridNode node = this.helper.getGridNode((BlockPos)pos);
                this.forEachInBb(bb2, pos2 -> {
                    IGridNode node2 = this.helper.getGridNode((BlockPos)pos2);
                    for (IGridConnection connection : node.getConnections()) {
                        if (connection.getOtherSide(node) != node2) continue;
                        this.checkConnection((BlockPos)pos, connection, expectedChannelCount);
                        foundAny.setPlain(true);
                    }
                });
            });
            if (!foundAny.getPlain()) {
                this.helper.fail("Connection spec " + bb + " and " + bb2 + " did not find any connections.");
            }
        }

        public void ensureEverythingWasChecked() {
            this.helper.check(this.nodes.isEmpty(), "Not all nodes were checked: " + String.valueOf(this.nodes));
            this.helper.check(this.connections.isEmpty(), "Not all connections were checked: " + String.valueOf(this.connections));
        }
    }
}

