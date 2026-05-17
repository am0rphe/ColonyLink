/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2LongMap$Entry
 */
package appeng.crafting;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.CraftBranchFailure;
import appeng.crafting.CraftingCalculation;
import appeng.crafting.CraftingTreeNode;
import appeng.crafting.inv.CraftingSimulationState;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class CraftingTreeProcess {
    private final CraftingTreeNode parent;
    final IPatternDetails details;
    private final CraftingCalculation job;
    private final Map<CraftingTreeNode, Long> nodes = new LinkedHashMap<CraftingTreeNode, Long>();
    boolean possible = true;
    private boolean containerItems;
    private boolean limitQty;

    public CraftingTreeProcess(ICraftingService cc, CraftingCalculation job, IPatternDetails details, CraftingTreeNode craftingTreeNode) {
        this.parent = craftingTreeNode;
        this.details = details;
        this.job = job;
        this.updateLimitQty();
        IPatternDetails.IInput[] inputs = this.details.getInputs();
        for (int x = 0; x < inputs.length; ++x) {
            IPatternDetails.IInput input = inputs[x];
            GenericStack firstInput = input.getPossibleInputs()[0];
            this.nodes.put(new CraftingTreeNode(cc, job, firstInput.what(), firstInput.amount(), this, x), input.getMultiplier());
        }
    }

    boolean notRecursive(IPatternDetails details) {
        return this.parent == null || this.parent.notRecursive(details);
    }

    private void updateLimitQty() {
        for (IPatternDetails.IInput input : this.details.getInputs()) {
            GenericStack primaryInput = input.getPossibleInputs()[0];
            boolean isAnInput = false;
            for (GenericStack output : this.details.getOutputs()) {
                if (!output.what().matches(primaryInput)) continue;
                isAnInput = true;
                break;
            }
            if (isAnInput) {
                this.limitQty = true;
            }
            if (input.getRemainingKey(primaryInput.what()) == null) continue;
            this.containerItems = true;
            this.limitQty = true;
        }
    }

    boolean limitsQuantity() {
        return this.limitQty;
    }

    void request(CraftingSimulationState inv, long times) throws CraftBranchFailure, InterruptedException {
        this.job.handlePausing();
        KeyCounter containerItems = this.containerItems ? new KeyCounter() : null;
        for (Map.Entry<CraftingTreeNode, Long> entry : this.nodes.entrySet()) {
            entry.getKey().request(inv, entry.getValue() * times, containerItems);
        }
        if (containerItems != null) {
            for (Object2LongMap.Entry stack : containerItems) {
                inv.insert((AEKey)stack.getKey(), stack.getLongValue(), Actionable.MODULATE);
                inv.addStackBytes((AEKey)stack.getKey(), stack.getLongValue(), 1L);
            }
        }
        for (GenericStack out : this.details.getOutputs()) {
            inv.insert(out.what(), out.amount() * times, Actionable.MODULATE);
        }
        inv.addCrafting(this.details, times);
        inv.addBytes(times);
    }

    long getNodeCount() {
        long tot = 0L;
        for (CraftingTreeNode node : this.nodes.keySet()) {
            tot += node.getNodeCount();
        }
        return tot;
    }

    long getOutputCount(AEKey what) {
        long tot = 0L;
        for (GenericStack is : this.details.getOutputs()) {
            if (!what.matches(is)) continue;
            tot += is.amount();
        }
        return tot;
    }

    boolean hasMultiplePaths() {
        for (Map.Entry<CraftingTreeNode, Long> entry : this.nodes.entrySet()) {
            if (!entry.getKey().hasMultiplePaths()) continue;
            return true;
        }
        return false;
    }
}

