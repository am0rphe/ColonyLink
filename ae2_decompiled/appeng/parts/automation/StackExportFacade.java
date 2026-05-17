/*
 * Decompiled with CFR 0.152.
 */
package appeng.parts.automation;

import appeng.api.behaviors.StackExportStrategy;
import appeng.api.behaviors.StackTransferContext;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import java.util.List;

public class StackExportFacade
implements StackExportStrategy {
    private final List<StackExportStrategy> strategies;

    public StackExportFacade(List<StackExportStrategy> strategies) {
        this.strategies = strategies;
    }

    @Override
    public long transfer(StackTransferContext context, AEKey what, long maxAmount) {
        for (StackExportStrategy strategy : this.strategies) {
            long result = strategy.transfer(context, what, maxAmount);
            if (result <= 0L) continue;
            return result;
        }
        return 0L;
    }

    @Override
    public long push(AEKey what, long amount, Actionable mode) {
        for (StackExportStrategy strategy : this.strategies) {
            long result = strategy.push(what, amount, mode);
            if (result <= 0L) continue;
            return result;
        }
        return 0L;
    }
}

