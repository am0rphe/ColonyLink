/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2LongMap$Entry
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  org.jetbrains.annotations.Nullable
 */
package appeng.crafting.execution;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.crafting.CraftingLink;
import appeng.crafting.execution.CraftingCpuLogic;
import appeng.crafting.execution.ElapsedTimeTracker;
import appeng.crafting.inv.ListCraftingInventory;
import appeng.me.service.CraftingService;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import java.lang.invoke.LambdaMetafactory;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

public class ExecutingCraftingJob {
    private static final String NBT_LINK = "link";
    private static final String NBT_PLAYER_ID = "playerId";
    private static final String NBT_FINAL_OUTPUT = "finalOutput";
    private static final String NBT_WAITING_FOR = "waitingFor";
    private static final String NBT_TIME_TRACKER = "timeTracker";
    private static final String NBT_REMAINING_AMOUNT = "remainingAmount";
    private static final String NBT_TASKS = "tasks";
    private static final String NBT_SUSPENDED = "suspended";
    private static final String NBT_CRAFTING_PROGRESS = "#craftingProgress";
    final CraftingLink link;
    final ListCraftingInventory waitingFor;
    final Map<IPatternDetails, TaskProgress> tasks = new HashMap<IPatternDetails, TaskProgress>();
    final ElapsedTimeTracker timeTracker;
    GenericStack finalOutput;
    long remainingAmount;
    @Nullable
    Integer playerId;
    boolean suspended;

    ExecutingCraftingJob(ICraftingPlan plan, CraftingDifferenceListener postCraftingDifference, CraftingLink link, @Nullable Integer playerId) {
        this.finalOutput = plan.finalOutput();
        this.remainingAmount = this.finalOutput.amount();
        this.waitingFor = new ListCraftingInventory(postCraftingDifference::onCraftingDifference);
        this.timeTracker = new ElapsedTimeTracker();
        for (Object2LongMap.Entry<AEKey> entry : plan.emittedItems()) {
            this.waitingFor.insert((AEKey)entry.getKey(), entry.getLongValue(), Actionable.MODULATE);
            this.timeTracker.addMaxItems(entry.getLongValue(), ((AEKey)entry.getKey()).getType());
        }
        for (Map.Entry entry : plan.patternTimes().entrySet()) {
            this.tasks.computeIfAbsent((IPatternDetails)((IPatternDetails)entry.getKey()), (Function<IPatternDetails, TaskProgress>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, lambda$new$0(appeng.api.crafting.IPatternDetails ), (Lappeng/api/crafting/IPatternDetails;)Lappeng/crafting/execution/ExecutingCraftingJob$TaskProgress;)()).value += ((Long)entry.getValue()).longValue();
            for (GenericStack output : ((IPatternDetails)entry.getKey()).getOutputs()) {
                long amount = output.amount() * (Long)entry.getValue() * (long)output.what().getAmountPerUnit();
                this.timeTracker.addMaxItems(amount, output.what().getType());
            }
        }
        this.link = link;
        this.playerId = playerId;
        this.suspended = false;
    }

    ExecutingCraftingJob(CompoundTag data, HolderLookup.Provider registries, CraftingDifferenceListener postCraftingDifference, CraftingCpuLogic cpu) {
        this.link = new CraftingLink(data.getCompound(NBT_LINK), cpu.cluster);
        IGrid grid = cpu.cluster.getGrid();
        if (grid != null) {
            ((CraftingService)grid.getCraftingService()).addLink(this.link);
        }
        this.finalOutput = GenericStack.readTag(registries, data.getCompound(NBT_FINAL_OUTPUT));
        this.remainingAmount = data.getLong(NBT_REMAINING_AMOUNT);
        this.waitingFor = new ListCraftingInventory(postCraftingDifference::onCraftingDifference);
        this.waitingFor.readFromNBT(data.getList(NBT_WAITING_FOR, 10), registries);
        this.timeTracker = new ElapsedTimeTracker(data.getCompound(NBT_TIME_TRACKER));
        this.playerId = data.contains(NBT_PLAYER_ID, 3) ? Integer.valueOf(data.getInt(NBT_PLAYER_ID)) : null;
        ListTag tasksTag = data.getList(NBT_TASKS, 10);
        for (int i = 0; i < tasksTag.size(); ++i) {
            CompoundTag item = tasksTag.getCompound(i);
            AEItemKey pattern = AEItemKey.fromTag(registries, item);
            IPatternDetails details = PatternDetailsHelper.decodePattern(pattern, cpu.cluster.getLevel());
            if (details == null) continue;
            TaskProgress tp = new TaskProgress();
            tp.value = item.getLong(NBT_CRAFTING_PROGRESS);
            this.tasks.put(details, tp);
        }
        this.suspended = data.getBoolean(NBT_SUSPENDED);
    }

    CompoundTag writeToNBT(HolderLookup.Provider registries) {
        CompoundTag data = new CompoundTag();
        CompoundTag linkData = new CompoundTag();
        this.link.writeToNBT(linkData);
        data.put(NBT_LINK, (Tag)linkData);
        data.put(NBT_FINAL_OUTPUT, (Tag)GenericStack.writeTag(registries, this.finalOutput));
        data.put(NBT_WAITING_FOR, (Tag)this.waitingFor.writeToNBT(registries));
        data.put(NBT_TIME_TRACKER, (Tag)this.timeTracker.writeToNBT());
        ListTag list = new ListTag();
        for (Map.Entry<IPatternDetails, TaskProgress> e : this.tasks.entrySet()) {
            CompoundTag item = e.getKey().getDefinition().toTag(registries);
            item.putLong(NBT_CRAFTING_PROGRESS, e.getValue().value);
            list.add((Object)item);
        }
        data.put(NBT_TASKS, (Tag)list);
        data.putLong(NBT_REMAINING_AMOUNT, this.remainingAmount);
        if (this.playerId != null) {
            data.putInt(NBT_PLAYER_ID, this.playerId.intValue());
        }
        data.putBoolean(NBT_SUSPENDED, this.suspended);
        return data;
    }

    private static /* synthetic */ TaskProgress lambda$new$0(IPatternDetails p) {
        return new TaskProgress();
    }

    @FunctionalInterface
    static interface CraftingDifferenceListener {
        public void onCraftingDifference(AEKey var1);
    }

    static class TaskProgress {
        long value = 0L;

        TaskProgress() {
        }
    }
}

