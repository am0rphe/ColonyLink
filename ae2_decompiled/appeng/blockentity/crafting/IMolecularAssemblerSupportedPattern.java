/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.NonNullList
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.CraftingInput
 *  net.minecraft.world.level.Level
 */
package appeng.blockentity.crafting;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.Level;

public interface IMolecularAssemblerSupportedPattern
extends IPatternDetails {
    public ItemStack assemble(CraftingInput var1, Level var2);

    default public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        return NonNullList.withSize((int)input.size(), (Object)ItemStack.EMPTY);
    }

    public boolean isItemValid(int var1, AEItemKey var2, Level var3);

    public boolean isSlotEnabled(int var1);

    public void fillCraftingGrid(KeyCounter[] var1, CraftingGridAccessor var2);

    @Override
    default public boolean supportsPushInputsToExternalInventory() {
        return false;
    }

    @FunctionalInterface
    public static interface CraftingGridAccessor {
        public void set(int var1, ItemStack var2);
    }
}

