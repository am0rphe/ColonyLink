/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.GlobalPos
 *  net.minecraft.world.item.ItemStack
 */
package appeng.api.features;

import net.minecraft.core.GlobalPos;
import net.minecraft.world.item.ItemStack;

public interface IGridLinkableHandler {
    public boolean canLink(ItemStack var1);

    public void link(ItemStack var1, GlobalPos var2);

    public void unlink(ItemStack var1);
}

