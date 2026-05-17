/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.ApiStatus$Experimental
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.behaviors;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Experimental
public interface GenericInternalInventory {
    public int size();

    @Nullable
    public GenericStack getStack(int var1);

    @Nullable
    public AEKey getKey(int var1);

    public long getAmount(int var1);

    public long getMaxAmount(AEKey var1);

    public long getCapacity(AEKeyType var1);

    public boolean canInsert();

    public boolean canExtract();

    public void setStack(int var1, @Nullable GenericStack var2);

    public boolean isSupportedType(AEKeyType var1);

    default public boolean isSupportedType(AEKey what) {
        return this.isSupportedType(what.getType());
    }

    public boolean isAllowedIn(int var1, AEKey var2);

    public long insert(int var1, AEKey var2, long var3, Actionable var5);

    public long extract(int var1, AEKey var2, long var3, Actionable var5);

    public void beginBatch();

    public void endBatch();

    public void endBatchSuppressed();

    public void onChange();
}

