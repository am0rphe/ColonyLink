/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.data.PackOutput
 *  net.minecraft.data.recipes.RecipeOutput
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 */
package appeng.datagen.providers.recipes;

import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.datagen.providers.recipes.AE2RecipeProvider;
import appeng.recipes.handlers.ChargerRecipeBuilder;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public class ChargerRecipes
extends AE2RecipeProvider {
    public ChargerRecipes(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    public void buildRecipes(RecipeOutput consumer) {
        ChargerRecipeBuilder.charge(consumer, AppEng.makeId("charger/charged_certus_quartz_crystal"), AEItems.CERTUS_QUARTZ_CRYSTAL, AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED);
        ChargerRecipeBuilder.charge(consumer, AppEng.makeId("charger/meteorite_compass"), (ItemLike)Items.COMPASS, AEItems.METEORITE_COMPASS);
        ChargerRecipeBuilder.charge(consumer, AppEng.makeId("charger/guide"), (ItemLike)Items.BOOK, AEItems.TABLET);
    }

    public String getName() {
        return "AE2 Charger Recipes";
    }
}

