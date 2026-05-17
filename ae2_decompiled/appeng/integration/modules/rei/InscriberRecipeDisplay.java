/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  me.shedaniel.rei.api.common.category.CategoryIdentifier
 *  me.shedaniel.rei.api.common.display.Display
 *  me.shedaniel.rei.api.common.entry.EntryIngredient
 *  me.shedaniel.rei.api.common.util.EntryIngredients
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.item.crafting.RecipeHolder
 */
package appeng.integration.modules.rei;

import appeng.integration.modules.rei.InscriberRecipeCategory;
import appeng.recipes.handlers.InscriberRecipe;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

class InscriberRecipeDisplay
implements Display {
    private final RecipeHolder<InscriberRecipe> holder;
    private final List<EntryIngredient> inputs;
    private final List<EntryIngredient> outputs;

    public InscriberRecipeDisplay(RecipeHolder<InscriberRecipe> holder) {
        this.holder = holder;
        InscriberRecipe recipe = (InscriberRecipe)holder.value();
        this.inputs = ImmutableList.of((Object)EntryIngredients.ofIngredient((Ingredient)recipe.getTopOptional()), (Object)EntryIngredients.ofIngredient((Ingredient)recipe.getMiddleInput()), (Object)EntryIngredients.ofIngredient((Ingredient)recipe.getBottomOptional()));
        this.outputs = ImmutableList.of((Object)EntryIngredients.of((ItemStack)recipe.getResultItem()));
    }

    public List<EntryIngredient> getInputEntries() {
        return this.inputs;
    }

    public List<EntryIngredient> getOutputEntries() {
        return this.outputs;
    }

    public CategoryIdentifier<?> getCategoryIdentifier() {
        return InscriberRecipeCategory.ID;
    }

    public Optional<ResourceLocation> getDisplayLocation() {
        return Optional.of(this.holder.id());
    }
}

