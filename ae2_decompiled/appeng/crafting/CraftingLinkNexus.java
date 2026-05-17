/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package appeng.crafting;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.crafting.CraftingLink;
import appeng.me.service.CraftingService;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

public class CraftingLinkNexus {
    private final UUID craftId;
    private boolean canceled = false;
    private boolean done = false;
    private int tickOfDeath = 0;
    @Nullable
    private CraftingLink req;
    @Nullable
    private CraftingLink cpu;

    public CraftingLinkNexus(UUID craftId) {
        this.craftId = craftId;
    }

    public boolean isDead(IGrid g, CraftingService craftingService) {
        if (this.canceled || this.done) {
            return true;
        }
        if (this.getRequest() == null || this.cpu == null) {
            ++this.tickOfDeath;
        } else {
            boolean hasMachine;
            boolean hasCpu = craftingService.hasCpu(this.cpu.getCpu());
            boolean bl = hasMachine = this.getRequest().getRequester().getActionableNode().getGrid() == g;
            this.tickOfDeath = hasCpu && hasMachine ? 0 : (this.tickOfDeath += 60);
        }
        if (this.tickOfDeath > 60) {
            this.cancel();
            return true;
        }
        return false;
    }

    void cancel() {
        this.canceled = true;
        if (this.getRequest() != null) {
            this.getRequest().setCanceled(true);
            if (this.getRequest().getRequester() != null) {
                this.getRequest().getRequester().jobStateChange(this.getRequest());
            }
        }
        if (this.cpu != null) {
            this.cpu.setCanceled(true);
        }
    }

    void remove(CraftingLink craftingLink) {
        if (this.getRequest() == craftingLink) {
            this.setRequest(null);
        } else if (this.cpu == craftingLink) {
            this.cpu = null;
        }
    }

    void add(CraftingLink craftingLink) {
        if (craftingLink.getCpu() != null) {
            this.cpu = craftingLink;
        } else if (craftingLink.getRequester() != null) {
            this.setRequest(craftingLink);
        }
    }

    boolean isCanceled() {
        return this.canceled;
    }

    boolean isDone() {
        return this.done;
    }

    void markDone() {
        this.done = true;
        if (this.getRequest() != null) {
            this.getRequest().setDone(true);
            if (this.getRequest().getRequester() != null) {
                this.getRequest().getRequester().jobStateChange(this.getRequest());
            }
        }
        if (this.cpu != null) {
            this.cpu.setDone(true);
        }
    }

    public boolean isRequester(ICraftingRequester requester) {
        return this.req != null && this.req.getRequester() == requester;
    }

    public void removeNode() {
        if (this.getRequest() != null) {
            this.getRequest().setNexus(null);
        }
        this.setRequest(null);
        this.tickOfDeath = 0;
    }

    @Nullable
    public CraftingLink getRequest() {
        return this.req;
    }

    public void setRequest(CraftingLink req) {
        this.req = req;
    }
}

