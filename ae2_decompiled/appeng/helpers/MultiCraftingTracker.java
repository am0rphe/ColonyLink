/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.world.level.Level
 */
package appeng.helpers;

import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.crafting.ICraftingSubmitResult;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.storage.StorageHelper;
import appeng.helpers.NonNullArrayIterator;
import com.google.common.collect.ImmutableSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;

public class MultiCraftingTracker {
    private final int size;
    private final ICraftingRequester owner;
    private Future<ICraftingPlan>[] jobs = null;
    private ICraftingLink[] links = null;

    public MultiCraftingTracker(ICraftingRequester o, int size) {
        this.owner = o;
        this.size = size;
    }

    public void readFromNBT(CompoundTag extra) {
        for (int x = 0; x < this.size; ++x) {
            CompoundTag link = extra.getCompound("links-" + x);
            if (link == null || link.isEmpty()) continue;
            this.setLink(x, StorageHelper.loadCraftingLink(link, this.owner));
        }
    }

    public void writeToNBT(CompoundTag extra) {
        for (int x = 0; x < this.size; ++x) {
            ICraftingLink link = this.getLink(x);
            if (link == null) continue;
            CompoundTag ln = new CompoundTag();
            link.writeToNBT(ln);
            extra.put("links-" + x, (Tag)ln);
        }
    }

    public boolean handleCrafting(int x, AEKey what, long amount, Level level, ICraftingService cg, IActionSource mySrc) {
        block9: {
            Future<ICraftingPlan> craftingJob = this.getJob(x);
            if (this.getLink(x) != null) {
                return false;
            }
            if (craftingJob != null) {
                try {
                    ICraftingPlan job = null;
                    if (craftingJob.isDone()) {
                        job = craftingJob.get();
                    }
                    if (job == null) break block9;
                    ICraftingSubmitResult result = cg.submitJob(job, this.owner, null, false, mySrc);
                    this.setJob(x, null);
                    if (result.successful()) {
                        this.setLink(x, result.link());
                        return true;
                    }
                }
                catch (InterruptedException interruptedException) {
                }
                catch (ExecutionException executionException) {}
            } else if (this.getLink(x) == null) {
                this.setJob(x, cg.beginCraftingCalculation(level, () -> mySrc, what, amount, CalculationStrategy.CRAFT_LESS));
            }
        }
        return false;
    }

    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        if (this.links == null) {
            return ImmutableSet.of();
        }
        return ImmutableSet.copyOf(new NonNullArrayIterator<ICraftingLink>(this.links));
    }

    public void jobStateChange(ICraftingLink link) {
        if (this.links != null) {
            for (int x = 0; x < this.links.length; ++x) {
                if (this.links[x] != link) continue;
                this.setLink(x, null);
                return;
            }
        }
    }

    int getSlot(ICraftingLink link) {
        if (this.links != null) {
            for (int x = 0; x < this.links.length; ++x) {
                if (this.links[x] != link) continue;
                return x;
            }
        }
        return -1;
    }

    void cancel() {
        if (this.links != null) {
            for (ICraftingLink iCraftingLink : this.links) {
                if (iCraftingLink == null) continue;
                iCraftingLink.cancel();
            }
            this.links = null;
        }
        if (this.jobs != null) {
            for (Future<ICraftingPlan> future : this.jobs) {
                if (future == null) continue;
                future.cancel(true);
            }
            this.jobs = null;
        }
    }

    boolean isBusy(int slot) {
        return this.getLink(slot) != null || this.getJob(slot) != null;
    }

    private ICraftingLink getLink(int slot) {
        if (this.links == null) {
            return null;
        }
        return this.links[slot];
    }

    private void setLink(int slot, ICraftingLink l) {
        if (this.links == null) {
            this.links = new ICraftingLink[this.size];
        }
        this.links[slot] = l;
        boolean hasStuff = false;
        for (int x = 0; x < this.links.length; ++x) {
            ICraftingLink g = this.links[x];
            if (g == null || g.isCanceled() || g.isDone()) {
                this.links[x] = null;
                continue;
            }
            hasStuff = true;
        }
        if (!hasStuff) {
            this.links = null;
        }
    }

    private Future<ICraftingPlan> getJob(int slot) {
        if (this.jobs == null) {
            return null;
        }
        return this.jobs[slot];
    }

    private void setJob(int slot, Future<ICraftingPlan> l) {
        if (this.jobs == null) {
            this.jobs = new Future[this.size];
        }
        this.jobs[slot] = l;
        boolean hasStuff = false;
        for (Future<ICraftingPlan> job : this.jobs) {
            if (job == null) continue;
            hasStuff = true;
            break;
        }
        if (!hasStuff) {
            this.jobs = null;
        }
    }
}

