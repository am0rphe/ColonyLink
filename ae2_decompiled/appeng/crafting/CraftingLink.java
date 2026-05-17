/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 */
package appeng.crafting;

import appeng.api.config.Actionable;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.stacks.AEKey;
import appeng.crafting.CraftingLinkNexus;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;

public class CraftingLink
implements ICraftingLink {
    private final ICraftingRequester req;
    private final ICraftingCPU cpu;
    private final UUID craftId;
    private final boolean standalone;
    private boolean canceled = false;
    private boolean done = false;
    private CraftingLinkNexus tie;

    public CraftingLink(CompoundTag data, ICraftingRequester req) {
        this.craftId = data.getUUID("craftId");
        this.setCanceled(data.getBoolean("canceled"));
        this.setDone(data.getBoolean("done"));
        this.standalone = data.getBoolean("standalone");
        if (!data.contains("req") || !data.getBoolean("req")) {
            throw new IllegalStateException("Invalid Crafting Link for Object");
        }
        this.req = req;
        this.cpu = null;
    }

    public CraftingLink(CompoundTag data, ICraftingCPU cpu) {
        this.craftId = data.getUUID("craftId");
        this.setCanceled(data.getBoolean("canceled"));
        this.setDone(data.getBoolean("done"));
        this.standalone = data.getBoolean("standalone");
        if (!data.contains("req") || data.getBoolean("req")) {
            throw new IllegalStateException("Invalid Crafting Link for Object");
        }
        this.cpu = cpu;
        this.req = null;
    }

    @Override
    public boolean isCanceled() {
        if (this.canceled) {
            return true;
        }
        if (this.done) {
            return false;
        }
        if (this.tie == null) {
            return false;
        }
        return this.tie.isCanceled();
    }

    @Override
    public boolean isDone() {
        if (this.done) {
            return true;
        }
        if (this.canceled) {
            return false;
        }
        if (this.tie == null) {
            return false;
        }
        return this.tie.isDone();
    }

    @Override
    public void cancel() {
        if (this.done) {
            return;
        }
        this.setCanceled(true);
        if (this.tie != null) {
            this.tie.cancel();
        }
        this.tie = null;
    }

    @Override
    public boolean isStandalone() {
        return this.standalone;
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        tag.putUUID("craftId", this.craftId);
        tag.putBoolean("canceled", this.isCanceled());
        tag.putBoolean("done", this.isDone());
        tag.putBoolean("standalone", this.standalone);
        tag.putBoolean("req", this.getRequester() != null);
    }

    @Override
    public UUID getCraftingID() {
        return this.craftId;
    }

    public void setNexus(CraftingLinkNexus n) {
        if (this.tie != null) {
            this.tie.remove(this);
        }
        if (this.isCanceled() && n != null) {
            n.cancel();
            this.tie = null;
            return;
        }
        this.tie = n;
        if (n != null) {
            n.add(this);
        }
    }

    public long insert(AEKey what, long amount, Actionable mode) {
        if (this.tie == null || this.tie.getRequest() == null || this.tie.getRequest().getRequester() == null) {
            return 0L;
        }
        if (this.tie.isCanceled()) {
            return 0L;
        }
        return this.tie.getRequest().getRequester().insertCraftedItems(this.tie.getRequest(), what, amount, mode);
    }

    public void markDone() {
        if (this.tie != null) {
            this.tie.markDone();
        }
    }

    void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    ICraftingRequester getRequester() {
        return this.req;
    }

    ICraftingCPU getCpu() {
        return this.cpu;
    }

    void setDone(boolean done) {
        this.done = done;
    }
}

