/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 */
package appeng.client.gui.me.common;

import appeng.client.gui.me.common.ClientReadOnlySlot;
import appeng.client.gui.me.common.Repo;
import appeng.menu.me.common.GridInventoryEntry;
import net.minecraft.world.item.ItemStack;

public class RepoSlot
extends ClientReadOnlySlot {
    private final Repo repo;
    private final int offset;

    public RepoSlot(Repo repo, int offset, int displayX, int displayY) {
        super(displayX, displayY);
        this.repo = repo;
        this.offset = offset;
    }

    public int getRepoViewIndex() {
        return this.offset;
    }

    public GridInventoryEntry getEntry() {
        if (this.repo.isEnabled()) {
            return this.repo.get(this.offset);
        }
        return null;
    }

    public long getStoredAmount() {
        GridInventoryEntry entry = this.getEntry();
        return entry != null ? entry.getStoredAmount() : 0L;
    }

    public long getRequestableAmount() {
        GridInventoryEntry entry = this.getEntry();
        return entry != null ? entry.getRequestableAmount() : 0L;
    }

    public boolean isCraftable() {
        GridInventoryEntry entry = this.getEntry();
        return entry != null && entry.isCraftable();
    }

    public ItemStack getItem() {
        GridInventoryEntry entry = this.getEntry();
        if (entry != null) {
            return entry.getWhat().wrapForDisplayOrFilter();
        }
        return ItemStack.EMPTY;
    }

    public boolean hasItem() {
        return this.getEntry() != null;
    }
}

