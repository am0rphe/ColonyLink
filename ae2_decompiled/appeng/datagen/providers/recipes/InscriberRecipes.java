/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.data.PackOutput
 *  net.minecraft.data.recipes.RecipeOutput
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.level.ItemLike
 */
package appeng.datagen.providers.recipes;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.datagen.providers.recipes.AE2RecipeProvider;
import appeng.datagen.providers.tags.ConventionTags;
import appeng.recipes.handlers.InscriberProcessType;
import appeng.recipes.handlers.InscriberRecipeBuilder;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public class InscriberRecipes
extends AE2RecipeProvider {
    public InscriberRecipes(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    public void buildRecipes(RecipeOutput consumer) {
        InscriberRecipeBuilder.inscribe((ItemLike)Items.IRON_BLOCK, AEItems.SILICON_PRESS, 1).setTop(Ingredient.of((ItemLike[])new ItemLike[]{AEItems.SILICON_PRESS})).setMode(InscriberProcessType.INSCRIBE).save(consumer, AppEng.makeId("inscriber/silicon_press"));
        InscriberRecipeBuilder.inscribe(ConventionTags.SILICON, AEItems.SILICON_PRINT, 1).setTop(Ingredient.of((ItemLike[])new ItemLike[]{AEItems.SILICON_PRESS})).setMode(InscriberProcessType.INSCRIBE).save(consumer, AppEng.makeId("inscriber/silicon_print"));
        this.processor(consumer, "calculation_processor", AEItems.CALCULATION_PROCESSOR_PRESS, AEItems.CALCULATION_PROCESSOR_PRINT, AEItems.CALCULATION_PROCESSOR, Ingredient.of((ItemLike[])new ItemLike[]{AEItems.CERTUS_QUARTZ_CRYSTAL}));
        this.processor(consumer, "engineering_processor", AEItems.ENGINEERING_PROCESSOR_PRESS, AEItems.ENGINEERING_PROCESSOR_PRINT, AEItems.ENGINEERING_PROCESSOR, Ingredient.of(ConventionTags.DIAMOND));
        this.processor(consumer, "logic_processor", AEItems.LOGIC_PROCESSOR_PRESS, AEItems.LOGIC_PROCESSOR_PRINT, AEItems.LOGIC_PROCESSOR, Ingredient.of(ConventionTags.GOLD_INGOT));
        InscriberRecipeBuilder.inscribe(ConventionTags.FLUIX_CRYSTAL, AEItems.FLUIX_DUST, 1).setMode(InscriberProcessType.INSCRIBE).save(consumer, AppEng.makeId("inscriber/fluix_dust"));
        InscriberRecipeBuilder.inscribe(ConventionTags.CERTUS_QUARTZ, AEItems.CERTUS_QUARTZ_DUST, 1).setMode(InscriberProcessType.INSCRIBE).save(consumer, AppEng.makeId("inscriber/certus_quartz_dust"));
        InscriberRecipeBuilder.inscribe(AEBlocks.SKY_STONE_BLOCK, AEItems.SKY_DUST, 1).setMode(InscriberProcessType.INSCRIBE).save(consumer, AppEng.makeId("inscriber/sky_stone_dust"));
        InscriberRecipeBuilder.inscribe((ItemLike)Items.ENDER_PEARL, AEItems.ENDER_DUST, 1).setMode(InscriberProcessType.INSCRIBE).save(consumer, AppEng.makeId("inscriber/ender_dust"));
    }

    private void processor(RecipeOutput consumer, String name, ItemLike press, ItemLike print, ItemLike processor, Ingredient printMaterial) {
        InscriberRecipeBuilder.inscribe(printMaterial, print, 1).setTop(Ingredient.of((ItemLike[])new ItemLike[]{press})).setMode(InscriberProcessType.INSCRIBE).save(consumer, AppEng.makeId("inscriber/" + name + "_print"));
        InscriberRecipeBuilder.inscribe((ItemLike)Items.REDSTONE, processor, 1).setTop(Ingredient.of((ItemLike[])new ItemLike[]{print})).setBottom(Ingredient.of((ItemLike[])new ItemLike[]{AEItems.SILICON_PRINT})).setMode(InscriberProcessType.PRESS).save(consumer, AppEng.makeId("inscriber/" + name));
        InscriberRecipeBuilder.inscribe((ItemLike)Items.IRON_BLOCK, press, 1).setTop(Ingredient.of((ItemLike[])new ItemLike[]{press})).setMode(InscriberProcessType.INSCRIBE).save(consumer, AppEng.makeId("inscriber/" + name + "_press"));
    }

    public String getName() {
        return "AE2 Inscriber Recipes";
    }
}

