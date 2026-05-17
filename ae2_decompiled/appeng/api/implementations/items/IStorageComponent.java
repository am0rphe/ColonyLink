/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 */
package appeng.api.implementations.items;

import net.minecraft.world.item.ItemStack;

public interface IStorageComponent {
    public int getBytes(ItemStack var1);

    public boolean isStorageComponent(ItemStack var1);
}

