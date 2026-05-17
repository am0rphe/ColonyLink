/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.networking.crafting;

import appeng.api.config.CpuSelectionMode;
import appeng.api.networking.crafting.CraftingJobStatus;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public interface ICraftingCPU {
    public boolean isBusy();

    @Nullable
    public CraftingJobStatus getJobStatus();

    public void cancelJob();

    public long getAvailableStorage();

    public int getCoProcessors();

    @Nullable
    public Component getName();

    public CpuSelectionMode getSelectionMode();
}

