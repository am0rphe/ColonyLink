/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.data.recipes.RecipeOutput
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.item.crafting.Recipe
 *  net.minecraft.world.level.ItemLike
 */
package appeng.recipes.handlers;

import appeng.recipes.handlers.InscriberProcessType;
import appeng.recipes.handlers.InscriberRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;

public class InscriberRecipeBuilder {
    private final Ingredient middleInput;
    private Ingredient topOptional = Ingredient.EMPTY;
    private Ingredient bottomOptional = Ingredient.EMPTY;
    private final ItemLike output;
    private final int count;
    private InscriberProcessType mode = InscriberProcessType.INSCRIBE;

    public InscriberRecipeBuilder(Ingredient middleInput, ItemLike output, int count) {
        this.middleInput = middleInput;
        this.output = output;
        this.count = count;
    }

    public static InscriberRecipeBuilder inscribe(ItemLike middle, ItemLike output, int count) {
        return new InscriberRecipeBuilder(Ingredient.of((ItemLike[])new ItemLike[]{middle}), output, count);
    }

    public static InscriberRecipeBuilder inscribe(TagKey<Item> middle, ItemLike output, int count) {
        return new InscriberRecipeBuilder(Ingredient.of(middle), output, count);
    }

    public static InscriberRecipeBuilder inscribe(Ingredient middle, ItemLike output, int count) {
        return new InscriberRecipeBuilder(middle, output, count);
    }

    public InscriberRecipeBuilder setTop(Ingredient topOptional) {
        this.topOptional = topOptional;
        return this;
    }

    public InscriberRecipeBuilder setBottom(Ingredient bottomOptional) {
        this.bottomOptional = bottomOptional;
        return this;
    }

    public InscriberRecipeBuilder setMode(InscriberProcessType processType) {
        this.mode = processType;
        return this;
    }

    public void save(RecipeOutput consumer, ResourceLocation id) {
        ItemStack result = this.output.asItem().getDefaultInstance();
        result.setCount(this.count);
        InscriberRecipe recipe = new InscriberRecipe(this.middleInput, result, this.topOptional, this.bottomOptional, this.mode);
        consumer.accept(id, (Recipe)recipe, null);
    }
}

