/*
 * Decompiled with CFR 0.152.
 */
package appeng.parts.automation;

import appeng.api.behaviors.StackImportStrategy;
import appeng.api.behaviors.StackTransferContext;
import java.util.List;

public class StackImportFacade
implements StackImportStrategy {
    private final List<StackImportStrategy> strategies;

    public StackImportFacade(List<StackImportStrategy> strategies) {
        this.strategies = strategies;
    }

    @Override
    public boolean transfer(StackTransferContext context) {
        for (StackImportStrategy strategy : this.strategies) {
            if (!strategy.transfer(context)) continue;
            return true;
        }
        return true;
    }
}

