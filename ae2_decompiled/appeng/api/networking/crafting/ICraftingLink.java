/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 */
package appeng.api.networking.crafting;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;

public interface ICraftingLink {
    public boolean isCanceled();

    public boolean isDone();

    public void cancel();

    public boolean isStandalone();

    public void writeToNBT(CompoundTag var1);

    public UUID getCraftingID();
}

