/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package appeng.helpers.patternprovider;

import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;
import org.jetbrains.annotations.Nullable;

public interface PatternContainer {
    @Nullable
    public IGrid getGrid();

    default public boolean isVisibleInTerminal() {
        return true;
    }

    public InternalInventory getTerminalPatternInventory();

    default public long getTerminalSortOrder() {
        return 0L;
    }

    public PatternContainerGroup getTerminalGroup();
}

