/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.state.BlockState
 *  org.jetbrains.annotations.ApiStatus$NonExtendable
 */
package appeng.api.implementations.items;

import appeng.api.parts.IFacadePart;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface IFacadeItem {
    public IFacadePart createPartFromItemStack(ItemStack var1, Direction var2);

    public ItemStack getTextureItem(ItemStack var1);

    public BlockState getTextureBlockState(ItemStack var1);
}

