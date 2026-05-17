/*
 * Decompiled with CFR 0.152.
 */
package appeng.client.gui.me.patternaccess;

import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.util.inv.AppEngInternalInventory;

public class PatternContainerRecord
implements Comparable<PatternContainerRecord> {
    private final PatternContainerGroup group;
    private final String searchName;
    private final long serverId;
    private final AppEngInternalInventory inventory;
    private final long order;

    public PatternContainerRecord(long serverId, int slots, long order, PatternContainerGroup group) {
        this.inventory = new AppEngInternalInventory(slots);
        this.group = group;
        this.searchName = group.name().getString().toLowerCase();
        this.serverId = serverId;
        this.order = order;
    }

    public PatternContainerGroup getGroup() {
        return this.group;
    }

    public String getSearchName() {
        return this.searchName;
    }

    @Override
    public int compareTo(PatternContainerRecord o) {
        return Long.compare(this.order, o.order);
    }

    public long getServerId() {
        return this.serverId;
    }

    public AppEngInternalInventory getInventory() {
        return this.inventory;
    }
}

