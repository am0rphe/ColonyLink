/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.RecipeInput
 */
package appeng.recipes.transform;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record TransformRecipeInput(List<ItemStack> items) implements RecipeInput
{
    public ItemStack getItem(int index) {
        return this.items.get(index);
    }

    public int size() {
        return this.items.size();
    }
}

