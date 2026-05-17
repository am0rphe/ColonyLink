/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.Nullable
 */
package appeng.crafting;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.CraftBranchFailure;
import appeng.crafting.CraftingCalculation;
import appeng.crafting.CraftingTreeProcess;
import appeng.crafting.execution.CraftingCpuHelper;
import appeng.crafting.execution.InputTemplate;
import appeng.crafting.inv.ChildCraftingSimulationState;
import appeng.crafting.inv.CraftingSimulationState;
import appeng.crafting.inv.ICraftingInventory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class CraftingTreeNode {
    @Nullable
    final IPatternDetails.IInput parentInput;
    private final CraftingCalculation job;
    private final CraftingTreeProcess parent;
    private final Level level;
    private final AEKey what;
    private final long amount;
    private ArrayList<CraftingTreeProcess> nodes = null;
    private final boolean canEmit;

    public CraftingTreeNode(ICraftingService cc, CraftingCalculation job, AEKey what, long amount, CraftingTreeProcess par, int slot) {
        this.parent = par;
        this.parentInput = slot == -1 ? null : par.details.getInputs()[slot];
        this.level = job.getLevel();
        this.job = job;
        this.what = this.findCraftedStack(cc, what);
        this.amount = amount;
        this.canEmit = cc.canEmitFor(what);
    }

    private AEKey findCraftedStack(ICraftingService cc, AEKey wat) {
        if (cc.canEmitFor(wat)) {
            return wat;
        }
        Collection<IPatternDetails> patterns = cc.getCraftingFor(wat);
        if (patterns.isEmpty() && this.parentInput != null) {
            long acceptableAmount = this.parentInput.getPossibleInputs()[0].amount();
            for (GenericStack possibleInput : this.parentInput.getPossibleInputs()) {
                AEKey fuzzy;
                if (possibleInput.amount() != acceptableAmount || (fuzzy = cc.getFuzzyCraftable(possibleInput.what(), fuzzyCandidate -> this.parentInput.isValid(fuzzyCandidate, this.level))) == null) continue;
                return fuzzy;
            }
        }
        return wat;
    }

    private void buildChildPatterns() {
        if (this.canEmit) {
            throw new IllegalStateException("Internal AE2 error: this node is emitable, it shouldn't use patterns!");
        }
        if (this.nodes == null) {
            this.nodes = new ArrayList();
            IGridNode gridNode = this.job.simRequester.getGridNode();
            if (gridNode != null) {
                ICraftingService craftingService = gridNode.getGrid().getCraftingService();
                for (IPatternDetails details : craftingService.getCraftingFor(this.what)) {
                    if (this.parent != null && !this.parent.notRecursive(details)) continue;
                    this.nodes.add(new CraftingTreeProcess(craftingService, this.job, details, this));
                }
            }
        }
    }

    boolean notRecursive(IPatternDetails details) {
        for (GenericStack output : details.getOutputs()) {
            if (!this.what.matches(output)) continue;
            return false;
        }
        for (IPatternDetails.IInput input : details.getInputs()) {
            if (!this.what.matches(input.getPossibleInputs()[0])) continue;
            return false;
        }
        if (this.parent == null) {
            return true;
        }
        return this.parent.notRecursive(details);
    }

    void request(CraftingSimulationState inv, long requestedAmount, @Nullable KeyCounter containerItems) throws CraftBranchFailure, InterruptedException {
        this.job.handlePausing();
        inv.addStackBytes(this.what, this.amount, requestedAmount);
        for (InputTemplate template : this.getValidItemTemplates(inv)) {
            long extracted = CraftingCpuHelper.extractTemplates(inv, template, requestedAmount);
            if (extracted <= 0L) continue;
            this.addContainerItems(template.key(), extracted, containerItems);
            if ((requestedAmount -= extracted) != 0L) continue;
            return;
        }
        this.addContainerItems(this.what, requestedAmount, containerItems);
        if (this.canEmit) {
            inv.emitItems(this.what, this.amount * requestedAmount);
            return;
        }
        this.buildChildPatterns();
        long totalRequestedItems = requestedAmount * this.amount;
        if (this.nodes.size() == 1) {
            CraftingTreeProcess pro = this.nodes.get(0);
            long craftedPerPattern = pro.getOutputCount(this.what);
            while (pro.possible && totalRequestedItems > 0L) {
                long times = pro.limitsQuantity() ? 1L : (totalRequestedItems + craftedPerPattern - 1L) / craftedPerPattern;
                pro.request(inv, times);
                long available = inv.extract(this.what, totalRequestedItems, Actionable.MODULATE);
                if (available != 0L) {
                    if ((totalRequestedItems -= available) > 0L) continue;
                    return;
                }
                AEItemKey pattern = pro.details.getDefinition();
                String outputs = pro.details.getOutputs().stream().map(GenericStack::toString).collect(Collectors.joining(", "));
                String errorMessage = "Unexpected error in the crafting calculation: can't find created items.\nThis is an AE2 bug, please report it, with the following important information:\n\n- Found none of %s. Remaining request: %d of %d*%d.\n- Tried crafting %d times the pattern %s.\n- Pattern outputs: %s.\n".formatted(this.what, totalRequestedItems, requestedAmount, this.amount, times, pattern, outputs);
                throw new UnsupportedOperationException(errorMessage);
            }
        } else if (this.nodes.size() > 1) {
            for (CraftingTreeProcess pro : this.nodes) {
                try {
                    while (pro.possible && totalRequestedItems > 0L) {
                        ChildCraftingSimulationState child = new ChildCraftingSimulationState(inv);
                        pro.request(child, 1L);
                        long available = child.extract(this.what, totalRequestedItems, Actionable.MODULATE);
                        if (available != 0L) {
                            child.applyDiff(inv);
                            if ((totalRequestedItems -= available) > 0L) continue;
                            return;
                        }
                        pro.possible = false;
                    }
                }
                catch (CraftBranchFailure fail) {
                    pro.possible = true;
                }
            }
        }
        if (!this.job.isSimulation()) {
            throw new CraftBranchFailure(this.what, totalRequestedItems);
        }
        this.job.addMissing(this.what, totalRequestedItems);
    }

    private void addContainerItems(AEKey template, long multiplier, @Nullable KeyCounter outputList) {
        AEKey containerItem;
        if (outputList != null && (containerItem = this.parentInput.getRemainingKey(template)) != null) {
            outputList.add(containerItem, multiplier);
        }
    }

    private Iterable<InputTemplate> getValidItemTemplates(ICraftingInventory inv) {
        if (this.parentInput == null) {
            return List.of(new InputTemplate(this.what, 1L));
        }
        return CraftingCpuHelper.getValidItemTemplates(inv, this.parentInput, this.level);
    }

    long getNodeCount() {
        long tot = 1L;
        if (this.nodes != null) {
            for (CraftingTreeProcess pro : this.nodes) {
                tot += pro.getNodeCount();
            }
        }
        return tot;
    }

    boolean hasMultiplePaths() {
        if (this.nodes == null) {
            return false;
        }
        if (this.nodes.size() > 1) {
            return true;
        }
        for (CraftingTreeProcess pro : this.nodes) {
            if (!pro.hasMultiplePaths()) continue;
            return true;
        }
        return false;
    }
}

