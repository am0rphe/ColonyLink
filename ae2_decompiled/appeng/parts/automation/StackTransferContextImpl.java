/*
 * Decompiled with CFR 0.152.
 */
package appeng.parts.automation;

import appeng.api.behaviors.StackTransferContext;
import appeng.api.config.Actionable;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.util.prioritylist.IPartitionList;
import java.util.HashSet;
import java.util.Set;

class StackTransferContextImpl
implements StackTransferContext {
    private final IStorageService internalStorage;
    private final IEnergySource energySource;
    private final IActionSource actionSource;
    private final IPartitionList filter;
    private final Set<AEKeyType> keyTypes;
    private final int initialOperations;
    private int operationsRemaining;
    private boolean isInverted;

    public StackTransferContextImpl(IStorageService internalStorage, IEnergySource energySource, IActionSource actionSource, int operationsRemaining, IPartitionList filter) {
        this.internalStorage = internalStorage;
        this.energySource = energySource;
        this.actionSource = actionSource;
        this.filter = filter;
        this.initialOperations = operationsRemaining;
        this.operationsRemaining = operationsRemaining;
        this.keyTypes = new HashSet<AEKeyType>();
        for (AEKey item : filter.getItems()) {
            this.keyTypes.add(item.getType());
        }
    }

    @Override
    public IStorageService getInternalStorage() {
        return this.internalStorage;
    }

    @Override
    public IEnergySource getEnergySource() {
        return this.energySource;
    }

    @Override
    public IActionSource getActionSource() {
        return this.actionSource;
    }

    @Override
    public int getOperationsRemaining() {
        return this.operationsRemaining;
    }

    @Override
    public void setOperationsRemaining(int operationsRemaining) {
        this.operationsRemaining = operationsRemaining;
    }

    @Override
    public boolean hasOperationsLeft() {
        return this.operationsRemaining > 0;
    }

    @Override
    public boolean hasDoneWork() {
        return this.initialOperations > this.operationsRemaining;
    }

    @Override
    public boolean isKeyTypeEnabled(AEKeyType space) {
        return this.keyTypes.isEmpty() || this.isInverted || this.keyTypes.contains(space);
    }

    @Override
    public boolean isInFilter(AEKey key) {
        return this.filter.isEmpty() || this.filter.isListed(key);
    }

    @Override
    public IPartitionList getFilter() {
        return this.filter;
    }

    @Override
    public void setInverted(boolean inverted) {
        this.isInverted = inverted;
    }

    @Override
    public boolean isInverted() {
        return !this.filter.isEmpty() && this.isInverted;
    }

    @Override
    public boolean canInsert(AEItemKey what, long amount) {
        return this.internalStorage.getInventory().insert(what, amount, Actionable.SIMULATE, this.actionSource) > 0L;
    }

    @Override
    public void reduceOperationsRemaining(long inserted) {
        this.operationsRemaining = (int)((long)this.operationsRemaining - inserted);
    }
}

