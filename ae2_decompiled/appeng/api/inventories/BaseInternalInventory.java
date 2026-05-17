/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.neoforged.neoforge.items.IItemHandler
 */
package appeng.api.inventories;

import appeng.api.inventories.InternalInventory;
import appeng.api.inventories.InternalInventoryItemHandler;
import net.neoforged.neoforge.items.IItemHandler;

public abstract class BaseInternalInventory
implements InternalInventory {
    private IItemHandler platformWrapper;

    @Override
    public final IItemHandler toItemHandler() {
        if (this.platformWrapper == null) {
            this.platformWrapper = new InternalInventoryItemHandler(this);
        }
        return this.platformWrapper;
    }
}

