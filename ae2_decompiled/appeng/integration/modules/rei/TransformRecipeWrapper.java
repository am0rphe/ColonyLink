/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  me.shedaniel.rei.api.common.category.CategoryIdentifier
 *  me.shedaniel.rei.api.common.display.Display
 *  me.shedaniel.rei.api.common.entry.EntryIngredient
 *  me.shedaniel.rei.api.common.util.EntryIngredients
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.RecipeHolder
 */
package appeng.integration.modules.rei;

import appeng.integration.modules.rei.TransformCategory;
import appeng.recipes.transform.TransformCircumstance;
import appeng.recipes.transform.TransformRecipe;
import java.util.List;
import java.util.Optional;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

public class TransformRecipeWrapper
implements Display {
    private final RecipeHolder<TransformRecipe> holder;
    private final List<EntryIngredient> inputs;
    private final List<EntryIngredient> outputs;

    public TransformRecipeWrapper(RecipeHolder<TransformRecipe> holder) {
        this.holder = holder;
        this.inputs = EntryIngredients.ofIngredients(((TransformRecipe)holder.value()).getIngredients());
        this.outputs = List.of(EntryIngredients.of((ItemStack)((TransformRecipe)holder.value()).getResultItem()));
    }

    public List<EntryIngredient> getInputEntries() {
        return this.inputs;
    }

    public List<EntryIngredient> getOutputEntries() {
        return this.outputs;
    }

    public Optional<ResourceLocation> getDisplayLocation() {
        return Optional.of(this.holder.id());
    }

    public CategoryIdentifier<?> getCategoryIdentifier() {
        return TransformCategory.ID;
    }

    public TransformCircumstance getTransformCircumstance() {
        return ((TransformRecipe)this.holder.value()).circumstance;
    }
}

