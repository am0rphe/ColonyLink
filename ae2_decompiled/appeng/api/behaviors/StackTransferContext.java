/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.ApiStatus$Experimental
 *  org.jetbrains.annotations.ApiStatus$NonExtendable
 */
package appeng.api.behaviors;

import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.util.prioritylist.IPartitionList;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
@ApiStatus.NonExtendable
public interface StackTransferContext {
    public IStorageService getInternalStorage();

    public IEnergySource getEnergySource();

    public IActionSource getActionSource();

    public int getOperationsRemaining();

    public void setOperationsRemaining(int var1);

    public boolean hasOperationsLeft();

    public boolean hasDoneWork();

    public boolean isKeyTypeEnabled(AEKeyType var1);

    public boolean isInFilter(AEKey var1);

    public IPartitionList getFilter();

    public void setInverted(boolean var1);

    public boolean isInverted();

    public boolean canInsert(AEItemKey var1, long var2);

    public void reduceOperationsRemaining(long var1);
}

