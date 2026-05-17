/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  net.minecraft.network.chat.Component
 *  org.jetbrains.annotations.Nullable
 */
package appeng.me.storage;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.core.localization.GuiText;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class NetworkStorage
implements MEStorage {
    private static final Comparator<Integer> PRIORITY_SORTER = (o1, o2) -> Integer.compare(o2, o1);
    private boolean mountsInUse;
    private final NavigableMap<Integer, List<MEStorage>> priorityInventory;
    private final List<MEStorage> secondPassInventories = new ArrayList<MEStorage>();
    @Nullable
    private List<QueuedOperation> queuedOperations;

    public NetworkStorage() {
        this.priorityInventory = new TreeMap<Integer, List<MEStorage>>(PRIORITY_SORTER);
    }

    public void mount(int priority, MEStorage inventory) {
        if (this.mountsInUse) {
            if (this.queuedOperations == null) {
                this.queuedOperations = new ArrayList<QueuedOperation>();
            }
            this.queuedOperations.add(new MountOperation(priority, inventory));
        } else {
            this.priorityInventory.computeIfAbsent(priority, k -> new ArrayList()).add(inventory);
        }
    }

    public void unmount(MEStorage inventory) {
        if (this.mountsInUse) {
            if (this.queuedOperations == null) {
                this.queuedOperations = new ArrayList<QueuedOperation>();
            }
            this.queuedOperations.add(new UnmountOperation(inventory));
        } else {
            Iterator prioIt = this.priorityInventory.entrySet().iterator();
            while (prioIt.hasNext()) {
                Map.Entry prioEntry = prioIt.next();
                List inventories = (List)prioEntry.getValue();
                if (!inventories.remove(inventory) || !inventories.isEmpty()) continue;
                prioIt.remove();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public long insert(AEKey what, long amount, Actionable type, IActionSource src) {
        if (this.mountsInUse) {
            return 0L;
        }
        long remaining = amount;
        this.mountsInUse = true;
        try {
            block3: for (List invList : this.priorityInventory.values()) {
                this.secondPassInventories.clear();
                Iterator ii = invList.iterator();
                while (ii.hasNext() && remaining > 0L) {
                    MEStorage inv = (MEStorage)ii.next();
                    if (this.isQueuedForRemoval(inv)) continue;
                    if (inv.isPreferredStorageFor(what, src)) {
                        remaining -= inv.insert(what, remaining, type, src);
                        continue;
                    }
                    this.secondPassInventories.add(inv);
                }
                for (MEStorage inv : this.secondPassInventories) {
                    if (remaining <= 0L) continue block3;
                    if (this.isQueuedForRemoval(inv)) continue;
                    remaining -= inv.insert(what, remaining, type, src);
                }
            }
        }
        finally {
            this.mountsInUse = false;
        }
        this.flushQueuedOperations();
        return amount - remaining;
    }

    private void flushQueuedOperations() {
        Preconditions.checkState((!this.mountsInUse ? 1 : 0) != 0);
        List<QueuedOperation> queuedOperations = this.queuedOperations;
        if (queuedOperations != null) {
            this.queuedOperations = null;
            for (QueuedOperation op : queuedOperations) {
                if (op instanceof MountOperation) {
                    MountOperation mountOp = (MountOperation)op;
                    this.mount(mountOp.priority, mountOp.storage);
                    continue;
                }
                if (op instanceof UnmountOperation) {
                    UnmountOperation unmountOp = (UnmountOperation)op;
                    this.unmount(unmountOp.storage);
                    continue;
                }
                throw new IllegalStateException("Unknown operation: " + String.valueOf(op));
            }
        }
    }

    private boolean isQueuedForRemoval(MEStorage inv) {
        if (this.queuedOperations != null) {
            for (QueuedOperation queuedOperation : this.queuedOperations) {
                if (!(queuedOperation instanceof UnmountOperation)) continue;
                UnmountOperation unmountOperation = (UnmountOperation)queuedOperation;
                if (unmountOperation.storage != inv) continue;
                return true;
            }
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (this.mountsInUse) {
            return 0L;
        }
        long extracted = 0L;
        this.mountsInUse = true;
        try {
            for (List invList : this.priorityInventory.descendingMap().values()) {
                Iterator ii = invList.iterator();
                while (ii.hasNext() && extracted < amount) {
                    MEStorage inv = (MEStorage)ii.next();
                    if (this.isQueuedForRemoval(inv)) continue;
                    extracted += inv.extract(what, amount - extracted, mode, source);
                }
            }
        }
        finally {
            this.mountsInUse = false;
        }
        this.flushQueuedOperations();
        return extracted;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void getAvailableStacks(KeyCounter out) {
        if (this.mountsInUse) {
            return;
        }
        this.mountsInUse = true;
        try {
            for (List i : this.priorityInventory.values()) {
                for (MEStorage j : i) {
                    j.getAvailableStacks(out);
                }
            }
        }
        finally {
            this.mountsInUse = false;
        }
    }

    @Override
    public Component getDescription() {
        return GuiText.MENetworkStorage.text();
    }

    private record MountOperation(int priority, MEStorage storage) implements QueuedOperation
    {
    }

    private record UnmountOperation(MEStorage storage) implements QueuedOperation
    {
    }

    static sealed interface QueuedOperation
    permits MountOperation, UnmountOperation {
    }
}

