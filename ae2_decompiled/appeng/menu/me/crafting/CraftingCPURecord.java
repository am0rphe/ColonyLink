/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package appeng.menu.me.crafting;

import appeng.api.networking.crafting.ICraftingCPU;
import net.minecraft.network.chat.Component;

public class CraftingCPURecord
implements Comparable<CraftingCPURecord> {
    private final ICraftingCPU cpu;
    private final long size;
    private final int processors;
    private Component name;

    public CraftingCPURecord(long size, int coProcessors, ICraftingCPU server) {
        this.size = size;
        this.processors = coProcessors;
        this.cpu = server;
        this.name = server.getName();
    }

    @Override
    public int compareTo(CraftingCPURecord o) {
        int a = Long.compare(o.getProcessors(), this.getProcessors());
        if (a != 0) {
            return a;
        }
        return Long.compare(o.getSize(), this.getSize());
    }

    ICraftingCPU getCpu() {
        return this.cpu;
    }

    int getProcessors() {
        return this.processors;
    }

    long getSize() {
        return this.size;
    }

    public Component getName() {
        return this.name;
    }

    public void setName(Component name) {
        this.name = name;
    }
}

