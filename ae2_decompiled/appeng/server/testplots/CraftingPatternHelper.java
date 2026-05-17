/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.NonNullList
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.CraftingInput
 *  net.minecraft.world.item.crafting.CraftingRecipe
 *  net.minecraft.world.item.crafting.RecipeHolder
 *  net.minecraft.world.item.crafting.RecipeInput
 *  net.minecraft.world.item.crafting.RecipeType
 *  net.minecraft.world.item.crafting.SingleRecipeInput
 *  net.minecraft.world.item.crafting.SmithingRecipe
 *  net.minecraft.world.item.crafting.SmithingRecipeInput
 *  net.minecraft.world.item.crafting.StonecutterRecipe
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 */
package appeng.server.testplots;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.AEItemKey;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

public class CraftingPatternHelper {
    public static ItemStack encodeCraftingPattern(ServerLevel level, Object[] ingredients, boolean allowSubstitutions, boolean allowFluidSubstitutions) {
        ItemStack[] stacks = (ItemStack[])Arrays.stream(ingredients).map(in -> {
            if (in instanceof ItemLike) {
                ItemLike itemLike = (ItemLike)in;
                return new ItemStack(itemLike);
            }
            if (in instanceof ItemStack) {
                ItemStack itemStack = (ItemStack)in;
                return itemStack;
            }
            if (in == null) {
                return ItemStack.EMPTY;
            }
            throw new IllegalArgumentException("Unsupported argument: " + String.valueOf(in));
        }).toArray(ItemStack[]::new);
        NonNullList c = NonNullList.withSize((int)9, (Object)ItemStack.EMPTY);
        for (int i = 0; i < stacks.length; ++i) {
            c.set(i, (Object)stacks[i]);
        }
        CraftingInput recipeInput = CraftingInput.of((int)3, (int)3, (List)c);
        RecipeHolder recipe = (RecipeHolder)level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, (RecipeInput)recipeInput, (Level)level).orElseThrow();
        ItemStack result = ((CraftingRecipe)recipe.value()).assemble((RecipeInput)recipeInput, (HolderLookup.Provider)level.registryAccess());
        return PatternDetailsHelper.encodeCraftingPattern((RecipeHolder<CraftingRecipe>)recipe, stacks, result, allowSubstitutions, allowFluidSubstitutions);
    }

    public static ItemStack encodeShapelessCraftingRecipe(Level level, ItemStack ... inputs) {
        NonNullList items = NonNullList.withSize((int)9, (Object)ItemStack.EMPTY);
        for (int i = 0; i < inputs.length; ++i) {
            items.set(i, (Object)inputs[i]);
        }
        CraftingInput recipeInput = CraftingInput.of((int)3, (int)3, (List)items);
        RecipeHolder recipe = (RecipeHolder)level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, (RecipeInput)recipeInput, level).orElseThrow(() -> new RuntimeException("Couldn't get a shapeless recipe for the provided input."));
        ItemStack[] actualInputs = new ItemStack[9];
        for (int i = 0; i < actualInputs.length; ++i) {
            actualInputs[i] = i < inputs.length ? inputs[i] : ItemStack.EMPTY;
        }
        return PatternDetailsHelper.encodeCraftingPattern((RecipeHolder<CraftingRecipe>)recipe, actualInputs, ((CraftingRecipe)recipe.value()).getResultItem((HolderLookup.Provider)level.registryAccess()), false, false);
    }

    public static ItemStack encodeStoneCutterPattern(Level level, ItemLike inputItem, ItemLike outputItem, boolean allowSubstitutes) {
        SingleRecipeInput input = new SingleRecipeInput(new ItemStack(inputItem));
        RecipeHolder foundRecipe = null;
        for (RecipeHolder holder : level.getRecipeManager().getRecipesFor(RecipeType.STONECUTTING, (RecipeInput)input, level)) {
            StonecutterRecipe recipe = (StonecutterRecipe)holder.value();
            if (!recipe.getResultItem((HolderLookup.Provider)level.registryAccess()).is(outputItem.asItem())) continue;
            foundRecipe = holder;
            break;
        }
        if (foundRecipe == null) {
            throw new RuntimeException("No stonecutter recipe found for input=" + String.valueOf(inputItem) + " and output=" + String.valueOf(outputItem));
        }
        return PatternDetailsHelper.encodeStonecuttingPattern(foundRecipe, AEItemKey.of(inputItem), AEItemKey.of(outputItem), allowSubstitutes);
    }

    public static ItemStack encodeSmithingPattern(Level level, ItemLike template, ItemLike base, ItemLike addition, boolean allowSubstitutes) {
        SmithingRecipeInput input = new SmithingRecipeInput(new ItemStack(template), new ItemStack(base), new ItemStack(addition));
        RecipeHolder foundRecipe = level.getRecipeManager().getRecipeFor(RecipeType.SMITHING, (RecipeInput)input, level).orElse(null);
        if (foundRecipe == null) {
            throw new RuntimeException("No stonecutter recipe found for template=" + String.valueOf(template) + " and base=" + String.valueOf(base) + " and addition=" + String.valueOf(addition));
        }
        ItemStack result = ((SmithingRecipe)foundRecipe.value()).assemble((RecipeInput)input, (HolderLookup.Provider)level.registryAccess());
        return PatternDetailsHelper.encodeSmithingTablePattern((RecipeHolder<SmithingRecipe>)foundRecipe, AEItemKey.of(template), AEItemKey.of(base), AEItemKey.of(addition), AEItemKey.of(result), allowSubstitutes);
    }
}

