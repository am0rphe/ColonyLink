/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.data.PackOutput
 *  net.minecraft.data.recipes.RecipeOutput
 *  net.minecraft.tags.FluidTags
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.material.Fluid
 */
package appeng.datagen.providers.recipes;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.datagen.providers.recipes.AE2RecipeProvider;
import appeng.datagen.providers.tags.ConventionTags;
import appeng.recipes.transform.TransformCircumstance;
import appeng.recipes.transform.TransformRecipeBuilder;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;

public class TransformRecipes
extends AE2RecipeProvider {
    public TransformRecipes(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    public void buildRecipes(RecipeOutput consumer) {
        TransformCircumstance water = TransformCircumstance.fluid((TagKey<Fluid>)FluidTags.WATER);
        TransformRecipeBuilder.transform(consumer, AppEng.makeId("transform/fluix_crystals"), AEItems.FLUIX_CRYSTAL, 2, water, new ItemLike[]{AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED, Items.REDSTONE, Items.QUARTZ});
        TransformRecipeBuilder.transform(consumer, AppEng.makeId("transform/certus_quartz_crystals"), AEItems.CERTUS_QUARTZ_CRYSTAL, 2, water, AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED, AEItems.CERTUS_QUARTZ_DUST);
        TransformRecipeBuilder.transform(consumer, AppEng.makeId("transform/fluix_crystal"), AEItems.FLUIX_CRYSTAL, 1, water, AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED, AEItems.FLUIX_DUST);
        TransformRecipeBuilder.transform(consumer, AppEng.makeId("transform/damaged_budding_quartz"), AEBlocks.DAMAGED_BUDDING_QUARTZ, 1, water, AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED, AEBlocks.QUARTZ_BLOCK);
        TransformRecipeBuilder.transform(consumer, AppEng.makeId("transform/chipped_budding_quartz"), AEBlocks.CHIPPED_BUDDING_QUARTZ, 1, water, AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED, AEBlocks.DAMAGED_BUDDING_QUARTZ);
        TransformRecipeBuilder.transform(consumer, AppEng.makeId("transform/flawed_budding_quartz"), AEBlocks.FLAWED_BUDDING_QUARTZ, 1, water, AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED, AEBlocks.CHIPPED_BUDDING_QUARTZ);
        TransformRecipeBuilder.transform(consumer, AppEng.makeId("transform/entangled_singularity"), AEItems.QUANTUM_ENTANGLED_SINGULARITY, 2, TransformCircumstance.EXPLOSION, Ingredient.of((ItemLike[])new ItemLike[]{AEItems.SINGULARITY}), Ingredient.of(ConventionTags.ENDER_PEARL_DUST));
        TransformRecipeBuilder.transform(consumer, AppEng.makeId("transform/entangled_singularity_from_pearl"), AEItems.QUANTUM_ENTANGLED_SINGULARITY, 2, TransformCircumstance.EXPLOSION, Ingredient.of((ItemLike[])new ItemLike[]{AEItems.SINGULARITY}), Ingredient.of(ConventionTags.ENDER_PEARL));
    }

    public String getName() {
        return "AE2 Transform Recipes";
    }
}

