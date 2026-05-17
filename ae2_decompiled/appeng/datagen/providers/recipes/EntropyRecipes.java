/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.data.PackOutput
 *  net.minecraft.data.recipes.RecipeOutput
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.material.Fluid
 *  net.minecraft.world.level.material.Fluids
 */
package appeng.datagen.providers.recipes;

import appeng.core.AppEng;
import appeng.datagen.providers.recipes.AE2RecipeProvider;
import appeng.recipes.entropy.EntropyRecipeBuilder;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class EntropyRecipes
extends AE2RecipeProvider {
    public EntropyRecipes(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    public void buildRecipes(RecipeOutput consumer) {
        this.buildCoolRecipes(consumer);
        this.buildHeatRecipes(consumer);
    }

    private void buildCoolRecipes(RecipeOutput consumer) {
        EntropyRecipeBuilder.cool().setInputFluid((Fluid)Fluids.FLOWING_WATER).setDrops(new ItemStack((ItemLike)Items.SNOWBALL)).save(consumer, AppEng.makeId("entropy/cool/flowing_water_snowball"));
        EntropyRecipeBuilder.cool().setInputBlock(Blocks.GRASS_BLOCK).setOutputBlock(Blocks.DIRT).save(consumer, AppEng.makeId("entropy/cool/grass_block_dirt"));
        EntropyRecipeBuilder.cool().setInputFluid((Fluid)Fluids.LAVA).setOutputBlock(Blocks.OBSIDIAN).save(consumer, AppEng.makeId("entropy/cool/lava_obsidian"));
        EntropyRecipeBuilder.cool().setInputBlock(Blocks.STONE_BRICKS).setOutputBlock(Blocks.CRACKED_STONE_BRICKS).save(consumer, AppEng.makeId("entropy/cool/stone_bricks_cracked_stone_bricks"));
        EntropyRecipeBuilder.cool().setInputBlock(Blocks.STONE).setOutputBlock(Blocks.COBBLESTONE).save(consumer, AppEng.makeId("entropy/cool/stone_cobblestone"));
        EntropyRecipeBuilder.cool().setInputFluid((Fluid)Fluids.WATER).setOutputBlock(Blocks.ICE).save(consumer, AppEng.makeId("entropy/cool/water_ice"));
    }

    private void buildHeatRecipes(RecipeOutput consumer) {
        EntropyRecipeBuilder.heat().setInputBlock(Blocks.COBBLESTONE).setOutputBlock(Blocks.STONE).save(consumer, AppEng.makeId("entropy/heat/cobblestone_stone"));
        EntropyRecipeBuilder.heat().setInputBlock(Blocks.ICE).setOutputFluid((Fluid)Fluids.WATER).save(consumer, AppEng.makeId("entropy/heat/ice_water"));
        EntropyRecipeBuilder.heat().setInputBlock(Blocks.SNOW).setOutputFluid((Fluid)Fluids.FLOWING_WATER).save(consumer, AppEng.makeId("entropy/heat/snow_water"));
        EntropyRecipeBuilder.heat().setInputFluid((Fluid)Fluids.WATER).setOutputBlock(Blocks.AIR).save(consumer, AppEng.makeId("entropy/heat/water_air"));
    }

    public String getName() {
        return "AE2 Entropy Manipualator Recipes";
    }
}

