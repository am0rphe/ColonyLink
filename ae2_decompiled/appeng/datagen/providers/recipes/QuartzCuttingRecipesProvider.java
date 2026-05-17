/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.NonNullList
 *  net.minecraft.data.PackOutput
 *  net.minecraft.data.recipes.RecipeOutput
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.item.crafting.Recipe
 */
package appeng.datagen.providers.recipes;

import appeng.api.ids.AETags;
import appeng.core.AppEng;
import appeng.core.definitions.AEParts;
import appeng.datagen.providers.recipes.AE2RecipeProvider;
import appeng.datagen.providers.tags.ConventionTags;
import appeng.recipes.quartzcutting.QuartzCuttingRecipe;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

public class QuartzCuttingRecipesProvider
extends AE2RecipeProvider {
    public QuartzCuttingRecipesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    protected void buildRecipes(RecipeOutput recipeOutput) {
        recipeOutput.accept(AppEng.makeId("network/parts/cable_anchor"), (Recipe)new QuartzCuttingRecipe(AEParts.CABLE_ANCHOR.stack(4), (NonNullList<Ingredient>)NonNullList.of((Object)Ingredient.EMPTY, (Object[])new Ingredient[]{Ingredient.of(ConventionTags.QUARTZ_KNIFE), Ingredient.of(AETags.METAL_INGOTS)})), null);
    }

    public String getName() {
        return "AE2 Quartz Cutting Recipes";
    }
}

