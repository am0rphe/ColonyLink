/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  net.minecraft.network.RegistryFriendlyByteBuf
 */
package appeng.menu.me.crafting;

import appeng.api.stacks.AEKey;
import appeng.crafting.execution.CraftingCpuLogic;
import appeng.menu.me.common.IncrementalUpdateHelper;
import appeng.menu.me.crafting.CraftingStatusEntry;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class CraftingStatus {
    public static final CraftingStatus EMPTY = new CraftingStatus(true, 0L, 0L, 0L, Collections.emptyList(), false);
    private final boolean fullStatus;
    private final long elapsedTime;
    private final long remainingItemCount;
    private final long startItemCount;
    private final List<CraftingStatusEntry> entries;
    private final boolean suspended;

    public CraftingStatus(boolean fullStatus, long elapsedTime, long remainingItemCount, long startItemCount, List<CraftingStatusEntry> entries, boolean suspended) {
        this.fullStatus = fullStatus;
        this.elapsedTime = elapsedTime;
        this.remainingItemCount = remainingItemCount;
        this.startItemCount = startItemCount;
        this.entries = ImmutableList.copyOf(entries);
        this.suspended = suspended;
    }

    @Deprecated(forRemoval=true)
    public CraftingStatus(boolean fullStatus, long elapsedTime, long remainingItemCount, long startItemCount, List<CraftingStatusEntry> entries) {
        this(fullStatus, elapsedTime, remainingItemCount, startItemCount, entries, false);
    }

    public boolean isFullStatus() {
        return this.fullStatus;
    }

    public long getElapsedTime() {
        return this.elapsedTime;
    }

    public long getRemainingItemCount() {
        return this.remainingItemCount;
    }

    public long getStartItemCount() {
        return this.startItemCount;
    }

    public List<CraftingStatusEntry> getEntries() {
        return this.entries;
    }

    public boolean isSuspended() {
        return this.suspended;
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBoolean(this.fullStatus);
        buffer.writeVarLong(this.elapsedTime);
        buffer.writeVarLong(this.remainingItemCount);
        buffer.writeVarLong(this.startItemCount);
        CraftingStatusEntry.LIST_STREAM_CODEC.encode((Object)buffer, this.entries);
        buffer.writeBoolean(this.suspended);
    }

    public static CraftingStatus read(RegistryFriendlyByteBuf buffer) {
        boolean fullStatus = buffer.readBoolean();
        long elapsedTime = buffer.readVarLong();
        long remainingItemCount = buffer.readVarLong();
        long startItemCount = buffer.readVarLong();
        List entries = (List)CraftingStatusEntry.LIST_STREAM_CODEC.decode((Object)buffer);
        boolean suspended = buffer.readBoolean();
        return new CraftingStatus(fullStatus, elapsedTime, remainingItemCount, startItemCount, List.copyOf(entries), suspended);
    }

    public static CraftingStatus create(IncrementalUpdateHelper changes, CraftingCpuLogic logic) {
        boolean full = changes.isFullUpdate();
        ImmutableList.Builder newEntries = ImmutableList.builder();
        for (AEKey what : changes) {
            long storedCount = logic.getStored(what);
            long activeCount = logic.getWaitingFor(what);
            long pendingCount = logic.getPendingOutputs(what);
            AEKey sentStack = what;
            if (!full && changes.getSerial(what) != null) {
                sentStack = null;
            }
            CraftingStatusEntry entry = new CraftingStatusEntry(changes.getOrAssignSerial(what), sentStack, storedCount, activeCount, pendingCount);
            newEntries.add((Object)entry);
            if (!entry.isDeleted()) continue;
            changes.removeSerial(what);
        }
        long elapsedTime = logic.getElapsedTimeTracker().getElapsedTime();
        long remainingItems = logic.getElapsedTimeTracker().getRemainingItemCount();
        long startItems = logic.getElapsedTimeTracker().getStartItemCount();
        boolean suspended = logic.isJobSuspended();
        return new CraftingStatus(full, elapsedTime, remainingItems, startItems, (List<CraftingStatusEntry>)newEntries.build(), suspended);
    }
}

