/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Stopwatch
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.Contract
 *  org.jetbrains.annotations.Nullable
 */
package appeng.crafting;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.crafting.ICraftingSimulationRequester;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.core.AELog;
import appeng.crafting.CraftBranchFailure;
import appeng.crafting.CraftingPlan;
import appeng.crafting.CraftingTreeNode;
import appeng.crafting.inv.ChildCraftingSimulationState;
import appeng.crafting.inv.CraftingSimulationState;
import appeng.crafting.inv.NetworkCraftingSimulationState;
import appeng.hooks.ticking.TickHandler;
import com.google.common.base.Stopwatch;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public class CraftingCalculation {
    private final NetworkCraftingSimulationState networkInv;
    private final Level level;
    private final KeyCounter missing = new KeyCounter();
    private final Object monitor = new Object();
    private final Stopwatch watch = Stopwatch.createUnstarted();
    private final CraftingTreeNode tree;
    private final AEKey output;
    private final long requestedAmount;
    private final CalculationStrategy strategy;
    private boolean simulate = false;
    final ICraftingSimulationRequester simRequester;
    private boolean running = false;
    private boolean done = false;
    private int time = 5;
    private int incTime = Integer.MAX_VALUE;
    private final List<CraftAttempt> attempts = AELog.isCraftingLogEnabled() ? new ArrayList() : null;

    public CraftingCalculation(Level level, IGrid grid, ICraftingSimulationRequester simRequester, GenericStack output, CalculationStrategy strategy) {
        this.level = level;
        this.output = output.what();
        this.requestedAmount = output.amount();
        this.strategy = strategy;
        this.simRequester = simRequester;
        IStorageService storage = grid.getStorageService();
        ICraftingService craftingService = grid.getCraftingService();
        this.networkInv = new NetworkCraftingSimulationState(storage, simRequester.getActionSource());
        this.tree = new CraftingTreeNode(craftingService, this, this.output, 1L, null, -1);
    }

    void addMissing(AEKey what, long amount) {
        this.missing.add(what, amount);
    }

    public ICraftingPlan run() {
        try {
            TickHandler.instance().registerCraftingSimulation(this.level, this);
            this.handlePausing();
            ICraftingPlan plan = this.computePlan();
            this.logCraftingJob(plan);
            ICraftingPlan iCraftingPlan = plan;
            return iCraftingPlan;
        }
        catch (Exception ex) {
            AELog.info(ex, "Exception during crafting calculation.");
            throw new RuntimeException(ex);
        }
        finally {
            this.finish();
        }
    }

    private ICraftingPlan computePlan() throws InterruptedException {
        CraftingPlan fullAmountPlan = this.runCraftAttempt(false, this.requestedAmount);
        if (fullAmountPlan != null) {
            return fullAmountPlan;
        }
        if (this.strategy == CalculationStrategy.CRAFT_LESS) {
            long successfulAmount = 0L;
            CraftingPlan successfulPlan = null;
            for (long increment = Long.highestOneBit(this.requestedAmount); increment > 0L; increment /= 2L) {
                CraftingPlan plan;
                long testAmount = successfulAmount + increment;
                if (testAmount >= this.requestedAmount || (plan = this.runCraftAttempt(false, testAmount)) == null) continue;
                successfulAmount = testAmount;
                successfulPlan = plan;
            }
            if (successfulPlan != null) {
                return successfulPlan;
            }
        }
        return this.runCraftAttempt(true, this.requestedAmount);
    }

    @Nullable
    @Contract(value="true, _ -> !null")
    private CraftingPlan runCraftAttempt(boolean simulate, long amount) throws InterruptedException {
        this.simulate = simulate;
        Stopwatch timer = Stopwatch.createStarted();
        ChildCraftingSimulationState craftingInventory = new ChildCraftingSimulationState(this.networkInv);
        craftingInventory.ignore(this.output);
        try {
            this.tree.request(craftingInventory, amount, null);
        }
        catch (CraftBranchFailure failure) {
            if (AELog.isCraftingLogEnabled()) {
                this.attempts.add(new CraftAttempt(amount + " failed", timer));
            }
            return null;
        }
        craftingInventory.addBytes(this.tree.getNodeCount() * 8L);
        CraftingPlan plan = CraftingSimulationState.buildCraftingPlan(craftingInventory, this, amount);
        if (AELog.isCraftingLogEnabled()) {
            String type = simulate ? "simulated" : "succeeded";
            this.attempts.add(new CraftAttempt("%d %s (%d bytes)".formatted(amount, type, plan.bytes()), timer));
        }
        return plan;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void handlePausing() throws InterruptedException {
        if (this.incTime > 100) {
            this.incTime = 0;
            Object object = this.monitor;
            synchronized (object) {
                if (this.watch.elapsed(TimeUnit.MICROSECONDS) > (long)this.time) {
                    this.running = false;
                    this.watch.stop();
                    this.monitor.notify();
                }
                if (!this.running) {
                    AELog.craftingDebug("crafting job will now sleep", new Object[0]);
                    while (!this.running) {
                        this.monitor.wait();
                    }
                    AELog.craftingDebug("crafting job now active", new Object[0]);
                }
            }
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
        }
        ++this.incTime;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void finish() {
        Object object = this.monitor;
        synchronized (object) {
            this.running = false;
            this.done = true;
            this.monitor.notify();
        }
    }

    public boolean isSimulation() {
        return this.simulate;
    }

    public AEKey getOutput() {
        return this.output;
    }

    public KeyCounter getMissingItems() {
        return this.missing;
    }

    Level getLevel() {
        return this.level;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean simulateFor(int micros) {
        this.time = micros;
        Object object = this.monitor;
        synchronized (object) {
            if (this.done) {
                return false;
            }
            this.watch.reset();
            this.watch.start();
            this.running = true;
            AELog.craftingDebug("main thread is now going to sleep", new Object[0]);
            this.monitor.notify();
            while (this.running) {
                try {
                    this.monitor.wait();
                }
                catch (InterruptedException interruptedException) {}
            }
            AELog.craftingDebug("main thread is now active", new Object[0]);
        }
        return true;
    }

    private void logCraftingJob(ICraftingPlan plan) {
        if (AELog.isCraftingLogEnabled()) {
            String actionSourceName;
            IActionSource actionSource = this.simRequester.getActionSource();
            if (actionSource != null && actionSource.player().isPresent()) {
                Player player = actionSource.player().get();
                actionSourceName = player.toString();
            } else {
                IActionHost machineSource;
                IGridNode actionableNode;
                actionSourceName = actionSource != null && actionSource.machine().isPresent() ? ((actionableNode = (machineSource = actionSource.machine().get()).getActionableNode()) != null ? actionableNode.toString() : machineSource.toString()) : "[unknown source]";
            }
            StringBuilder message = new StringBuilder();
            message.append("CraftingCalculation issued by %s requesting [%dx%s] breakdown:\n".formatted(actionSourceName, this.requestedAmount, this.output));
            for (CraftAttempt attempt : this.attempts) {
                message.append(" - %s in %d ms\n".formatted(attempt.description, attempt.stopwatch.elapsed(TimeUnit.MILLISECONDS)));
            }
            message.append(" - final plan: %d (%d bytes)".formatted(plan.finalOutput().amount(), plan.bytes()));
            AELog.crafting(message.toString(), new Object[0]);
        }
    }

    public boolean hasMultiplePaths() {
        return this.tree.hasMultiplePaths();
    }

    private record CraftAttempt(String description, Stopwatch stopwatch) {
    }
}

