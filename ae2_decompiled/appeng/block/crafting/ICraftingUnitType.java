/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.Item
 */
package appeng.block.crafting;

import net.minecraft.world.item.Item;

public interface ICraftingUnitType {
    public long getStorageBytes();

    public int getAcceleratorThreads();

    public Item getItemFromType();
}

