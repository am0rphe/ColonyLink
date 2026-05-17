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
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.item.crafting.RecipeHolder
 */
package appeng.integration.modules.rei;

import appeng.core.AppEng;
import appeng.recipes.handlers.ChargerRecipe;
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

public record ChargerDisplay(RecipeHolder<ChargerRecipe> holder) implements Display
{
    public static CategoryIdentifier<ChargerDisplay> ID = CategoryIdentifier.of((ResourceLocation)AppEng.makeId("charger"));

    public List<EntryIngredient> getInputEntries() {
        return List.of(EntryIngredients.ofIngredient((Ingredient)((ChargerRecipe)this.holder.value()).getIngredient()));
    }

    public List<EntryIngredient> getOutputEntries() {
        return List.of(EntryIngredients.of((ItemStack)((ChargerRecipe)this.holder.value()).getResultItem()));
    }

    public CategoryIdentifier<?> getCategoryIdentifier() {
        return ID;
    }

    public Optional<ResourceLocation> getDisplayLocation() {
        return Optional.of(this.holder.id());
    }
}

