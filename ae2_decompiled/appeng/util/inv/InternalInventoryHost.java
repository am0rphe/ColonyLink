/*
 * Decompiled with CFR 0.152.
 */
package appeng.util.inv;

import appeng.util.inv.AppEngInternalInventory;

public interface InternalInventoryHost {
    public void saveChangedInventory(AppEngInternalInventory var1);

    default public void onChangeInventory(AppEngInternalInventory inv, int slot) {
    }

    public boolean isClientSide();
}

