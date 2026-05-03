package com.colonylink.colonylink;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class ClearNbtRecipe extends CustomRecipe
{
    public final Item targetItem;

    public ClearNbtRecipe(CraftingBookCategory category, Item targetItem)
    {
        super(category);
        this.targetItem = targetItem;
    }

    @Override
    public boolean matches(CraftingInput input, Level level)
    {
        int found = 0;
        for (int i = 0; i < input.size(); i++)
        {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() == targetItem)
                found++;
            else
                return false;
        }
        return found == 1;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider provider)
    {
        for (int i = 0; i < input.size(); i++)
        {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty() && stack.getItem() == targetItem)
                return new ItemStack(targetItem, 1);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height)
    {
        return width * height >= 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return ColonyLinkRecipes.CLEAR_NBT_SERIALIZER.get();
    }
}