/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.crafting.Recipe
 *  net.minecraft.world.item.crafting.RecipeType
 *  net.neoforged.neoforge.registries.DeferredRegister
 */
package appeng.recipes;

import appeng.core.AppEng;
import appeng.recipes.entropy.EntropyRecipe;
import appeng.recipes.game.CraftingUnitTransformRecipe;
import appeng.recipes.game.StorageCellDisassemblyRecipe;
import appeng.recipes.handlers.ChargerRecipe;
import appeng.recipes.handlers.InscriberRecipe;
import appeng.recipes.mattercannon.MatterCannonAmmo;
import appeng.recipes.quartzcutting.QuartzCuttingRecipe;
import appeng.recipes.transform.TransformRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AERecipeTypes {
    public static final DeferredRegister<RecipeType<?>> DR = DeferredRegister.create((ResourceKey)Registries.RECIPE_TYPE, (String)"ae2");
    public static final RecipeType<TransformRecipe> TRANSFORM = AERecipeTypes.register("transform");
    public static final RecipeType<EntropyRecipe> ENTROPY = AERecipeTypes.register("entropy");
    public static final RecipeType<InscriberRecipe> INSCRIBER = AERecipeTypes.register("inscriber");
    public static final RecipeType<ChargerRecipe> CHARGER = AERecipeTypes.register("charger");
    public static final RecipeType<MatterCannonAmmo> MATTER_CANNON_AMMO = AERecipeTypes.register("matter_cannon");
    public static final RecipeType<QuartzCuttingRecipe> QUARTZ_CUTTING = AERecipeTypes.register("quartz_cutting");
    public static final RecipeType<CraftingUnitTransformRecipe> CRAFTING_UNIT_TRANSFORM = AERecipeTypes.register("crafting_unit_transform");
    public static final RecipeType<StorageCellDisassemblyRecipe> CELL_DISASSEMBLY = AERecipeTypes.register("storage_cell_disassembly");

    private AERecipeTypes() {
    }

    private static <T extends Recipe<?>> RecipeType<T> register(String id) {
        RecipeType type = RecipeType.simple((ResourceLocation)AppEng.makeId(id));
        DR.register(id, () -> type);
        return type;
    }
}

