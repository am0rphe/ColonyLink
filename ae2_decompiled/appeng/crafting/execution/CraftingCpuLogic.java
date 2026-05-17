/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  it.unimi.dsi.fastutil.objects.Object2LongMap$Entry
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.Nullable
 */
package appeng.crafting.execution;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.crafting.IPatternDetails;
import appeng.api.features.IPlayerRegistry;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.crafting.ICraftingSubmitResult;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.core.AELog;
import appeng.core.network.clientbound.CraftingJobStatusPacket;
import appeng.crafting.CraftingLink;
import appeng.crafting.execution.CraftingCpuHelper;
import appeng.crafting.execution.CraftingSubmitResult;
import appeng.crafting.execution.ElapsedTimeTracker;
import appeng.crafting.execution.ExecutingCraftingJob;
import appeng.crafting.inv.ListCraftingInventory;
import appeng.hooks.ticking.TickHandler;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.service.CraftingService;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class CraftingCpuLogic {
    final CraftingCPUCluster cluster;
    private ExecutingCraftingJob job = null;
    private final ListCraftingInventory inventory = new ListCraftingInventory(this::postChange);
    private final int[] usedOps = new int[3];
    private final Set<Consumer<AEKey>> listeners = new HashSet<Consumer<AEKey>>();
    private boolean cantStoreItems = false;
    private long lastModifiedOnTick = TickHandler.instance().getCurrentTick();

    public CraftingCpuLogic(CraftingCPUCluster cluster) {
        this.cluster = cluster;
    }

    public ICraftingSubmitResult trySubmitJob(IGrid grid, ICraftingPlan plan, IActionSource src, @Nullable ICraftingRequester requester) {
        GenericStack missingIngredient;
        if (this.job != null) {
            return CraftingSubmitResult.CPU_BUSY;
        }
        if (!this.cluster.isActive()) {
            return CraftingSubmitResult.CPU_OFFLINE;
        }
        if (this.cluster.getAvailableStorage() < plan.bytes()) {
            return CraftingSubmitResult.CPU_TOO_SMALL;
        }
        if (!this.inventory.list.isEmpty()) {
            AELog.warn("Crafting CPU inventory is not empty yet a job was submitted.", new Object[0]);
        }
        if ((missingIngredient = CraftingCpuHelper.tryExtractInitialItems(plan, grid, this.inventory, src)) != null) {
            return CraftingSubmitResult.missingIngredient(missingIngredient);
        }
        Integer playerId = src.player().map(p -> {
            Integer n;
            if (p instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer)p;
                n = IPlayerRegistry.getPlayerId(serverPlayer);
            } else {
                n = null;
            }
            return n;
        }).orElse(null);
        UUID craftId = UUID.randomUUID();
        CraftingLink linkCpu = new CraftingLink(CraftingCpuHelper.generateLinkData(craftId, requester == null, false), this.cluster);
        this.job = new ExecutingCraftingJob(plan, this::postChange, linkCpu, playerId);
        this.cluster.updateOutput(plan.finalOutput());
        this.cluster.markDirty();
        this.notifyJobOwner(this.job, CraftingJobStatusPacket.Status.STARTED);
        if (requester != null) {
            CraftingLink linkReq = new CraftingLink(CraftingCpuHelper.generateLinkData(craftId, false, true), requester);
            CraftingService craftingService = (CraftingService)grid.getCraftingService();
            craftingService.addLink(linkCpu);
            craftingService.addLink(linkReq);
            return CraftingSubmitResult.successful(linkReq);
        }
        return CraftingSubmitResult.successful(null);
    }

    public void tickCraftingLogic(IEnergyService eg, CraftingService cc) {
        int remainingOperations;
        if (!this.cluster.isActive()) {
            return;
        }
        this.cantStoreItems = false;
        if (this.job == null) {
            this.storeItems();
            if (!this.inventory.list.isEmpty()) {
                this.cantStoreItems = true;
            }
            return;
        }
        if (this.job.link.isCanceled()) {
            this.cancel();
            return;
        }
        if (this.job.suspended) {
            return;
        }
        int started = remainingOperations = this.cluster.getCoProcessors() + 1 - (this.usedOps[0] + this.usedOps[1] + this.usedOps[2]);
        if (remainingOperations > 0) {
            int pushedPatterns;
            while ((pushedPatterns = this.executeCrafting(remainingOperations, cc, eg, this.cluster.getLevel())) > 0 && (remainingOperations -= pushedPatterns) > 0) {
            }
        }
        this.usedOps[2] = this.usedOps[1];
        this.usedOps[1] = this.usedOps[0];
        this.usedOps[0] = started - remainingOperations;
    }

    public int executeCrafting(int maxPatterns, CraftingService craftingService, IEnergyService energyService, Level level) {
        ExecutingCraftingJob job = this.job;
        if (job == null) {
            return 0;
        }
        int pushedPatterns = 0;
        Iterator<Map.Entry<IPatternDetails, ExecutingCraftingJob.TaskProgress>> it = job.tasks.entrySet().iterator();
        block0: while (it.hasNext()) {
            Map.Entry<IPatternDetails, ExecutingCraftingJob.TaskProgress> task = it.next();
            if (task.getValue().value <= 0L) {
                it.remove();
                continue;
            }
            IPatternDetails details = task.getKey();
            KeyCounter expectedOutputs = new KeyCounter();
            KeyCounter expectedContainerItems = new KeyCounter();
            KeyCounter[] craftingContainer = CraftingCpuHelper.extractPatternInputs(details, this.inventory, level, expectedOutputs, expectedContainerItems);
            for (ICraftingProvider provider : craftingService.getProviders(details)) {
                if (craftingContainer == null) break;
                if (provider.isBusy()) continue;
                double patternPower = CraftingCpuHelper.calculatePatternPower(craftingContainer);
                if (energyService.extractAEPower(patternPower, Actionable.SIMULATE, PowerMultiplier.CONFIG) < patternPower - 0.01) break;
                if (!provider.pushPattern(details, craftingContainer)) continue;
                energyService.extractAEPower(patternPower, Actionable.MODULATE, PowerMultiplier.CONFIG);
                ++pushedPatterns;
                for (Object2LongMap.Entry<AEKey> expectedOutput : expectedOutputs) {
                    job.waitingFor.insert((AEKey)expectedOutput.getKey(), expectedOutput.getLongValue(), Actionable.MODULATE);
                }
                for (Object2LongMap.Entry<AEKey> expectedContainerItem : expectedContainerItems) {
                    job.waitingFor.insert((AEKey)expectedContainerItem.getKey(), expectedContainerItem.getLongValue(), Actionable.MODULATE);
                    job.timeTracker.addMaxItems(expectedContainerItem.getLongValue(), ((AEKey)expectedContainerItem.getKey()).getType());
                }
                this.cluster.markDirty();
                --task.getValue().value;
                if (task.getValue().value <= 0L) {
                    it.remove();
                    continue block0;
                }
                if (pushedPatterns == maxPatterns) break block0;
                expectedOutputs.reset();
                expectedContainerItems.reset();
                craftingContainer = CraftingCpuHelper.extractPatternInputs(details, this.inventory, level, expectedOutputs, expectedContainerItems);
            }
            if (craftingContainer == null) continue;
            CraftingCpuHelper.reinjectPatternInputs(this.inventory, craftingContainer);
        }
        return pushedPatterns;
    }

    public long insert(AEKey what, long amount, Actionable type) {
        if (what == null || this.job == null) {
            return 0L;
        }
        long waitingFor = this.job.waitingFor.extract(what, amount, Actionable.SIMULATE);
        if (waitingFor <= 0L) {
            return 0L;
        }
        if (amount > waitingFor) {
            amount = waitingFor;
        }
        if (type == Actionable.MODULATE) {
            this.job.timeTracker.decrementItems(amount, what.getType());
            this.job.waitingFor.extract(what, amount, Actionable.MODULATE);
            this.cluster.markDirty();
        }
        long inserted = amount;
        if (what.matches(this.job.finalOutput)) {
            inserted = this.job.link.insert(what, amount, type);
            if (type == Actionable.MODULATE) {
                this.postChange(what);
                this.job.remainingAmount = Math.max(0L, this.job.remainingAmount - amount);
                if (this.job.remainingAmount <= 0L) {
                    this.finishJob(true);
                    this.cluster.updateOutput(null);
                } else {
                    this.cluster.updateOutput(new GenericStack(this.job.finalOutput.what(), this.job.remainingAmount));
                }
            }
        } else if (type == Actionable.MODULATE) {
            this.inventory.insert(what, amount, Actionable.MODULATE);
        }
        return inserted;
    }

    private void finishJob(boolean success) {
        if (success) {
            this.job.link.markDone();
        } else {
            this.job.link.cancel();
        }
        this.job.waitingFor.clear();
        for (Map.Entry<IPatternDetails, ExecutingCraftingJob.TaskProgress> entry : this.job.tasks.entrySet()) {
            for (GenericStack output : entry.getKey().getOutputs()) {
                this.postChange(output.what());
            }
        }
        this.notifyJobOwner(this.job, success ? CraftingJobStatusPacket.Status.FINISHED : CraftingJobStatusPacket.Status.CANCELLED);
        this.job = null;
        this.storeItems();
    }

    public void cancel() {
        if (this.job == null) {
            return;
        }
        this.cluster.updateOutput(null);
        this.finishJob(false);
    }

    public void storeItems() {
        Preconditions.checkState((this.job == null ? 1 : 0) != 0, (Object)"CPU should not have a job to prevent re-insertion when dumping items");
        if (this.inventory.list.isEmpty()) {
            return;
        }
        IGrid g = this.cluster.getGrid();
        if (g == null) {
            return;
        }
        MEStorage storage = g.getStorageService().getInventory();
        for (Object2LongMap.Entry<AEKey> entry : this.inventory.list) {
            this.postChange((AEKey)entry.getKey());
            long inserted = storage.insert((AEKey)entry.getKey(), entry.getLongValue(), Actionable.MODULATE, this.cluster.getSrc());
            entry.setValue(entry.getLongValue() - inserted);
        }
        this.inventory.list.removeZeros();
        this.cluster.markDirty();
    }

    private void postChange(AEKey what) {
        this.lastModifiedOnTick = TickHandler.instance().getCurrentTick();
        for (Consumer<AEKey> listener : this.listeners) {
            listener.accept(what);
        }
    }

    public long getLastModifiedOnTick() {
        return this.lastModifiedOnTick;
    }

    public boolean hasJob() {
        return this.job != null;
    }

    @Nullable
    public GenericStack getFinalJobOutput() {
        return this.job != null ? this.job.finalOutput : null;
    }

    public ElapsedTimeTracker getElapsedTimeTracker() {
        if (this.job != null) {
            return this.job.timeTracker;
        }
        return new ElapsedTimeTracker();
    }

    public void readFromNBT(CompoundTag data, HolderLookup.Provider registries) {
        this.inventory.readFromNBT(data.getList("inventory", 10), registries);
        if (data.contains("job")) {
            this.job = new ExecutingCraftingJob(data.getCompound("job"), registries, this::postChange, this);
            if (this.job.finalOutput == null) {
                this.finishJob(false);
            } else {
                this.cluster.updateOutput(new GenericStack(this.job.finalOutput.what(), this.job.remainingAmount));
            }
        } else {
            this.cluster.updateOutput(null);
        }
    }

    public void writeToNBT(CompoundTag data, HolderLookup.Provider registries) {
        data.put("inventory", (Tag)this.inventory.writeToNBT(registries));
        if (this.job != null) {
            data.put("job", (Tag)this.job.writeToNBT(registries));
        }
    }

    public ICraftingLink getLastLink() {
        if (this.job != null) {
            return this.job.link;
        }
        return null;
    }

    public ListCraftingInventory getInventory() {
        return this.inventory;
    }

    public void addListener(Consumer<AEKey> listener) {
        this.listeners.add(listener);
    }

    public void removeListener(Consumer<AEKey> listener) {
        this.listeners.remove(listener);
    }

    public long getStored(AEKey template) {
        return this.inventory.extract(template, Long.MAX_VALUE, Actionable.SIMULATE);
    }

    public long getWaitingFor(AEKey template) {
        if (this.job != null) {
            return this.job.waitingFor.extract(template, Long.MAX_VALUE, Actionable.SIMULATE);
        }
        return 0L;
    }

    public void getAllWaitingFor(Set<AEKey> waitingFor) {
        if (this.job != null) {
            for (Object2LongMap.Entry<AEKey> entry : this.job.waitingFor.list) {
                waitingFor.add((AEKey)entry.getKey());
            }
        }
    }

    public long getPendingOutputs(AEKey template) {
        long count = 0L;
        if (this.job != null) {
            for (Map.Entry<IPatternDetails, ExecutingCraftingJob.TaskProgress> t : this.job.tasks.entrySet()) {
                for (GenericStack output : t.getKey().getOutputs()) {
                    if (!template.matches(output)) continue;
                    count += output.amount() * t.getValue().value;
                }
            }
        }
        return count;
    }

    public void getAllItems(KeyCounter out) {
        out.addAll(this.inventory.list);
        if (this.job != null) {
            out.addAll(this.job.waitingFor.list);
            for (Map.Entry<IPatternDetails, ExecutingCraftingJob.TaskProgress> t : this.job.tasks.entrySet()) {
                for (GenericStack output : t.getKey().getOutputs()) {
                    out.add(output.what(), output.amount() * t.getValue().value);
                }
            }
        }
    }

    public boolean isCantStoreItems() {
        return this.cantStoreItems;
    }

    public boolean isJobSuspended() {
        return this.job != null && this.job.suspended;
    }

    public void setJobSuspended(boolean suspended) {
        if (this.job != null && this.job.suspended != suspended) {
            this.job.suspended = suspended;
        }
    }

    private void notifyJobOwner(ExecutingCraftingJob job, CraftingJobStatusPacket.Status status) {
        this.lastModifiedOnTick = TickHandler.instance().getCurrentTick();
        Integer playerId = job.playerId;
        if (playerId == null) {
            return;
        }
        MinecraftServer server = this.cluster.getLevel().getServer();
        ServerPlayer connectedPlayer = IPlayerRegistry.getConnected(server, playerId);
        if (connectedPlayer != null) {
            UUID jobId = job.link.getCraftingID();
            CraftingJobStatusPacket message = new CraftingJobStatusPacket(jobId, job.finalOutput.what(), job.finalOutput.amount(), job.remainingAmount, status);
            connectedPlayer.connection.send((CustomPacketPayload)message);
        }
    }
}

