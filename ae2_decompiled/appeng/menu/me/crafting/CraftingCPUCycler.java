/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  net.minecraft.network.chat.Component
 */
package appeng.menu.me.crafting;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingService;
import appeng.menu.me.crafting.CraftingCPURecord;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.network.chat.Component;

class CraftingCPUCycler {
    private final Predicate<ICraftingCPU> cpuFilter;
    private final ChangeListener changeListener;
    private final List<CraftingCPURecord> cpus = new ArrayList<CraftingCPURecord>();
    private int selectedCpu = -1;
    private boolean initialDataSent = false;
    private boolean allowNoSelection;

    public CraftingCPUCycler(Predicate<ICraftingCPU> cpuFilter, ChangeListener changeListener) {
        this.cpuFilter = cpuFilter;
        this.changeListener = changeListener;
    }

    public void detectAndSendChanges(IGrid network) {
        ICraftingService cc = network.getCraftingService();
        ImmutableSet<ICraftingCPU> cpuSet = cc.getCpus();
        int matches = 0;
        boolean changed = !this.initialDataSent;
        this.initialDataSent = true;
        for (ICraftingCPU c : cpuSet) {
            boolean matched;
            boolean found = false;
            for (CraftingCPURecord ccr : this.cpus) {
                if (ccr.getCpu() != c) continue;
                found = true;
                break;
            }
            if (matched = this.cpuFilter.test(c)) {
                ++matches;
            }
            if (found == matched) continue;
            changed = true;
        }
        if (changed || this.cpus.size() != matches) {
            this.cpus.clear();
            for (ICraftingCPU c : cpuSet) {
                if (!this.cpuFilter.test(c)) continue;
                this.cpus.add(new CraftingCPURecord(c.getAvailableStorage(), c.getCoProcessors(), c));
            }
            Collections.sort(this.cpus);
            for (int i = 0; i < this.cpus.size(); ++i) {
                CraftingCPURecord cpu = this.cpus.get(i);
                if (cpu.getName() != null) continue;
                cpu.setName((Component)Component.literal((String)("#" + (i + 1))));
            }
            this.notifyListener();
        }
    }

    public void cycleCpu(boolean next) {
        int lowerLimit;
        this.selectedCpu = next ? ++this.selectedCpu : --this.selectedCpu;
        int n = lowerLimit = this.allowNoSelection ? -1 : 0;
        if (this.selectedCpu < lowerLimit) {
            this.selectedCpu = this.cpus.size() - 1;
        } else if (this.selectedCpu >= this.cpus.size()) {
            this.selectedCpu = lowerLimit;
        }
        this.notifyListener();
    }

    public boolean isAllowNoSelection() {
        return this.allowNoSelection;
    }

    public void setAllowNoSelection(boolean allowNoSelection) {
        this.allowNoSelection = allowNoSelection;
    }

    private void notifyListener() {
        if (this.selectedCpu >= this.cpus.size()) {
            this.selectedCpu = -1;
        }
        if (!this.allowNoSelection && this.selectedCpu == -1 && !this.cpus.isEmpty()) {
            this.selectedCpu = 0;
        }
        if (this.selectedCpu != -1) {
            this.changeListener.onChange(this.cpus.get(this.selectedCpu), true);
        } else {
            this.changeListener.onChange(null, !this.cpus.isEmpty());
        }
    }

    @FunctionalInterface
    public static interface ChangeListener {
        public void onChange(CraftingCPURecord var1, boolean var2);
    }
}

