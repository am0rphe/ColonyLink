/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.Vec3
 */
package appeng.server.testplots;

import appeng.core.definitions.AEParts;
import appeng.server.testplots.TestPlot;
import appeng.server.testplots.TestPlotClass;
import appeng.server.testworld.PlotBuilder;
import appeng.util.Platform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

@TestPlotClass
public class AnnihilationPlaneTests {
    @TestPlot(value="annihilation_plane_seed_farm")
    public static void annihilationPlaneSeedFarm(PlotBuilder plot) {
        BlockPos origin = BlockPos.ZERO;
        BlockPos grassPos = origin.east();
        plot.block(grassPos, Blocks.GRASS_BLOCK);
        plot.creativeEnergyCell(origin);
        plot.cable(origin.above()).part(Direction.EAST, AEParts.ANNIHILATION_PLANE);
        plot.storageDrive(origin.above().west());
        plot.test(helper -> helper.startSequence().thenExecute(() -> helper.getGrid(origin)).thenIdle(10).thenExecute(() -> {
            ItemStack stack = Items.BONE_MEAL.getDefaultInstance();
            BlockHitResult hitOnTop = new BlockHitResult(new Vec3(0.5, 1.0, 0.5), Direction.UP, helper.absolutePos(grassPos), false);
            Player fakePlayer = Platform.getFakePlayer(helper.getLevel(), null);
            UseOnContext useCtx = new UseOnContext((Level)helper.getLevel(), fakePlayer, InteractionHand.MAIN_HAND, stack, hitOnTop);
            stack.useOn(useCtx);
        }).thenExecute(() -> helper.assertBlockNotPresent(Blocks.AIR, grassPos.above())).thenWaitUntil(() -> helper.assertBlock(grassPos.above(), Blocks.AIR::equals, "expected air")).thenSucceed());
    }
}

