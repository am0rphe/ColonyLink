/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.gametest.framework.GameTestAssertException
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.Nullable
 */
package appeng.server.testworld;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.crafting.ICraftingSimulationRequester;
import appeng.api.networking.crafting.ICraftingSubmitResult;
import appeng.api.stacks.AEKey;
import appeng.core.AELog;
import appeng.me.helpers.BaseActionSource;
import appeng.me.helpers.MachineSource;
import appeng.server.testworld.PlotTestHelper;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class TestCraftingJob {
    private final PlotTestHelper helper;
    private final BlockPos gridOrigin;
    private final AEKey what;
    private final long amount;
    private final CalculationStrategy strategy;
    @Nullable
    private Future<ICraftingPlan> planFuture;
    @Nullable
    private ICraftingPlan plan;
    private boolean submitted = false;

    public TestCraftingJob(PlotTestHelper helper, BlockPos gridOrigin, AEKey what, long amount) {
        this(helper, gridOrigin, what, amount, CalculationStrategy.REPORT_MISSING_ITEMS);
    }

    public TestCraftingJob(PlotTestHelper helper, BlockPos gridOrigin, AEKey what, long amount, CalculationStrategy strategy) {
        this.what = what;
        this.amount = amount;
        this.strategy = strategy;
        this.helper = helper;
        this.gridOrigin = gridOrigin;
    }

    public void tickUntilStarted() {
        IGrid grid;
        if (this.planFuture == null) {
            grid = this.helper.getGrid(this.gridOrigin);
            MachineSource src = new MachineSource(grid::getPivot);
            ICraftingService craftingService = grid.getCraftingService();
            ICraftingSimulationRequester simRequester = () -> src;
            this.planFuture = craftingService.beginCraftingCalculation((Level)grid.getPivot().getLevel(), simRequester, this.what, this.amount, this.strategy);
        }
        if (this.plan == null) {
            try {
                this.plan = this.planFuture.get(0L, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException | ExecutionException e) {
                AELog.error(e);
                throw new GameTestAssertException("Crafting job planning failed: " + String.valueOf(e));
            }
            catch (TimeoutException e) {
                throw new GameTestAssertException("Crafting job planning did not complete");
            }
        }
        if (!this.submitted) {
            grid = this.helper.getGrid(BlockPos.ZERO);
            ICraftingSubmitResult result = grid.getCraftingService().submitJob(this.plan, null, null, true, new BaseActionSource());
            this.helper.check(result.successful(), "failed to submit job");
            this.submitted = true;
        }
    }
}

