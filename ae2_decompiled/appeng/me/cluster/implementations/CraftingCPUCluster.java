/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.Nullable
 */
package appeng.me.cluster.implementations;

import appeng.api.config.Actionable;
import appeng.api.config.CpuSelectionMode;
import appeng.api.config.Settings;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.CraftingJobStatus;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.crafting.ICraftingSubmitResult;
import appeng.api.networking.events.GridCraftingCpuChange;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.util.IConfigManager;
import appeng.blockentity.crafting.CraftingBlockEntity;
import appeng.blockentity.crafting.CraftingMonitorBlockEntity;
import appeng.crafting.execution.CraftingCpuLogic;
import appeng.crafting.execution.ElapsedTimeTracker;
import appeng.me.cluster.IAECluster;
import appeng.me.cluster.MBCalculator;
import appeng.me.helpers.MachineSource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class CraftingCPUCluster
implements IAECluster,
ICraftingCPU {
    private static final String LOG_MARK_AS_COMPLETE = "Completed job for %s.";
    private final BlockPos boundsMin;
    private final BlockPos boundsMax;
    private final List<CraftingBlockEntity> blockEntities = new ArrayList<CraftingBlockEntity>();
    private final List<CraftingMonitorBlockEntity> status = new ArrayList<CraftingMonitorBlockEntity>();
    private final IConfigManager configManager;
    private Component myName = null;
    private boolean isDestroyed = false;
    private long storage = 0L;
    private MachineSource machineSrc = null;
    private int accelerator = 0;
    public final CraftingCpuLogic craftingLogic = new CraftingCpuLogic(this);

    public CraftingCPUCluster(BlockPos boundsMin, BlockPos boundsMax) {
        this.boundsMin = boundsMin.immutable();
        this.boundsMax = boundsMax.immutable();
        this.configManager = IConfigManager.builder(this::markDirty).registerSetting(Settings.CPU_SELECTION_MODE, CpuSelectionMode.ANY).build();
    }

    @Override
    public boolean isDestroyed() {
        return this.isDestroyed;
    }

    @Override
    public BlockPos getBoundsMin() {
        return this.boundsMin;
    }

    @Override
    public BlockPos getBoundsMax() {
        return this.boundsMax;
    }

    @Override
    public void updateStatus(boolean updateGrid) {
        for (CraftingBlockEntity r : this.blockEntities) {
            r.updateSubType(true);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void destroy() {
        boolean ownsModification;
        if (this.isDestroyed) {
            return;
        }
        this.isDestroyed = true;
        boolean bl = ownsModification = !MBCalculator.isModificationInProgress();
        if (ownsModification) {
            MBCalculator.setModificationInProgress(this);
        }
        try {
            boolean posted = false;
            for (CraftingBlockEntity r : this.blockEntities) {
                IGridNode n = r.getActionableNode();
                if (n != null && !posted) {
                    n.getGrid().postEvent(new GridCraftingCpuChange(n));
                    posted = true;
                }
                r.updateStatus(null);
            }
        }
        finally {
            if (ownsModification) {
                MBCalculator.setModificationInProgress(null);
            }
        }
    }

    public Iterator<CraftingBlockEntity> getBlockEntities() {
        return this.blockEntities.iterator();
    }

    void addBlockEntity(CraftingBlockEntity te) {
        if (this.machineSrc == null || te.isCoreBlock()) {
            this.machineSrc = new MachineSource(te);
        }
        te.setCoreBlock(false);
        te.saveChanges();
        this.blockEntities.add(0, te);
        if (te instanceof CraftingMonitorBlockEntity) {
            this.status.add((CraftingMonitorBlockEntity)te);
        }
        if (te.getStorageBytes() > 0L) {
            this.storage += te.getStorageBytes();
        }
        if (te.getAcceleratorThreads() > 0) {
            if (te.getAcceleratorThreads() <= 16) {
                this.accelerator += te.getAcceleratorThreads();
            } else {
                throw new IllegalArgumentException("Co-processor threads may not exceed 16 per single unit block.");
            }
        }
    }

    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        return this.craftingLogic.insert(what, amount, mode);
    }

    public void markDirty() {
        this.getCore().saveChanges();
    }

    public void updateOutput(GenericStack finalOutput) {
        GenericStack send = finalOutput;
        if (finalOutput != null && finalOutput.amount() <= 0L) {
            send = null;
        }
        for (CraftingMonitorBlockEntity t : this.status) {
            t.setJob(send);
        }
    }

    public IActionSource getSrc() {
        return Objects.requireNonNull(this.machineSrc);
    }

    private CraftingBlockEntity getCore() {
        if (this.machineSrc == null) {
            return null;
        }
        return (CraftingBlockEntity)this.machineSrc.machine().get();
    }

    @Nullable
    public IGrid getGrid() {
        IGridNode node = this.getNode();
        return node != null ? node.getGrid() : null;
    }

    @Override
    public void cancelJob() {
        this.craftingLogic.cancel();
    }

    public ICraftingSubmitResult submitJob(IGrid g, ICraftingPlan plan, IActionSource src, ICraftingRequester requestingMachine) {
        return this.craftingLogic.trySubmitJob(g, plan, src, requestingMachine);
    }

    @Override
    public boolean isBusy() {
        return this.craftingLogic.hasJob();
    }

    @Override
    @Nullable
    public CraftingJobStatus getJobStatus() {
        GenericStack finalOutput = this.craftingLogic.getFinalJobOutput();
        if (finalOutput != null) {
            ElapsedTimeTracker elapsedTimeTracker = this.craftingLogic.getElapsedTimeTracker();
            long progress = Math.max(0L, elapsedTimeTracker.getStartItemCount() - elapsedTimeTracker.getRemainingItemCount());
            return new CraftingJobStatus(finalOutput, elapsedTimeTracker.getStartItemCount(), progress, elapsedTimeTracker.getElapsedTime());
        }
        return null;
    }

    @Override
    public long getAvailableStorage() {
        return this.storage;
    }

    @Override
    public int getCoProcessors() {
        return this.accelerator;
    }

    @Override
    public Component getName() {
        return this.myName;
    }

    @Nullable
    public IGridNode getNode() {
        CraftingBlockEntity core = this.getCore();
        return core != null ? core.getActionableNode() : null;
    }

    public boolean isActive() {
        IGridNode node = this.getNode();
        return node != null && node.isActive();
    }

    public void writeToNBT(CompoundTag data, HolderLookup.Provider registries) {
        this.craftingLogic.writeToNBT(data, registries);
        this.configManager.writeToNBT(data, registries);
    }

    void done() {
        CraftingBlockEntity core = this.getCore();
        core.setCoreBlock(true);
        if (core.getPreviousState() != null) {
            this.readFromNBT(core.getPreviousState(), (HolderLookup.Provider)core.getLevel().registryAccess());
            core.setPreviousState(null);
        }
        this.updateName();
    }

    public void readFromNBT(CompoundTag data, HolderLookup.Provider registries) {
        this.craftingLogic.readFromNBT(data, registries);
        this.configManager.readFromNBT(data, registries);
    }

    public void updateName() {
        this.myName = null;
        for (CraftingBlockEntity te : this.blockEntities) {
            if (!te.hasCustomName()) continue;
            if (this.myName != null) {
                this.myName.copy().append(" ").append(te.getCustomName());
                continue;
            }
            this.myName = te.getCustomName().copy();
        }
    }

    public Level getLevel() {
        return this.getCore().getLevel();
    }

    public void breakCluster() {
        CraftingBlockEntity t = this.getCore();
        if (t != null) {
            t.breakCluster();
        }
    }

    @Override
    public CpuSelectionMode getSelectionMode() {
        return this.configManager.getSetting(Settings.CPU_SELECTION_MODE);
    }

    public IConfigManager getConfigManager() {
        return this.configManager;
    }

    public boolean canBeAutoSelectedFor(IActionSource source) {
        return switch (this.getSelectionMode()) {
            default -> throw new MatchException(null, null);
            case CpuSelectionMode.ANY -> true;
            case CpuSelectionMode.PLAYER_ONLY -> source.player().isPresent();
            case CpuSelectionMode.MACHINE_ONLY -> source.player().isEmpty();
        };
    }

    public boolean isPreferredFor(IActionSource source) {
        return switch (this.getSelectionMode()) {
            default -> throw new MatchException(null, null);
            case CpuSelectionMode.ANY -> false;
            case CpuSelectionMode.PLAYER_ONLY -> source.player().isPresent();
            case CpuSelectionMode.MACHINE_ONLY -> source.player().isEmpty();
        };
    }
}

