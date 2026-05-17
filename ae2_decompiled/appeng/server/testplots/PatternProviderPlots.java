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
import appeng.api.stacks.AEItemKey;
import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEParts;
import appeng.server.testplots.TestPlot;
import appeng.server.testplots.TestPlotClass;
import appeng.server.testworld.PlotBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

@TestPlotClass
public final class PatternProviderPlots {
    private PatternProviderPlots() {
    }

    @TestPlot(value="pattern_provider_loop")
    public static void patternProviderLoop(PlotBuilder plot) {
        plot.creativeEnergyCell("0 0 0");
        plot.cable("0 1 0");
        plot.block("1 0 0", AEBlocks.PATTERN_PROVIDER);
        plot.cable("1 1 0").part(Direction.DOWN, AEParts.STORAGE_BUS);
        plot.block("-1 0 0", AEBlocks.PATTERN_PROVIDER);
        plot.cable("-1 1 0").part(Direction.DOWN, AEParts.STORAGE_BUS);
        plot.test(helper -> helper.startSequence().thenIdle(5).thenExecute(() -> {
            PatternProviderBlockEntity pp = (PatternProviderBlockEntity)helper.getBlockEntity(new BlockPos(1, 0, 0));
            pp.getLogic().getReturnInv().insert(0, AEItemKey.of((ItemLike)Items.ANDESITE), 1L, Actionable.MODULATE);
        }).thenIdle(5).thenSucceed());
    }
}

