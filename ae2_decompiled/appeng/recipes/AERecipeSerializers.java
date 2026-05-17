/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.item.crafting.RecipeSerializer
 *  net.neoforged.neoforge.registries.DeferredRegister
 */
package appeng.recipes;

import appeng.recipes.entropy.EntropyRecipeSerializer;
import appeng.recipes.game.AddItemUpgradeRecipeSerializer;
import appeng.recipes.game.CraftingUnitTransformRecipeSerializer;
import appeng.recipes.game.FacadeRecipe;
import appeng.recipes.game.RemoveItemUpgradeRecipeSerializer;
import appeng.recipes.game.StorageCellDisassemblyRecipeSerializer;
import appeng.recipes.game.StorageCellUpgradeRecipeSerializer;
import appeng.recipes.handlers.ChargerRecipeSerializer;
import appeng.recipes.handlers.InscriberRecipeSerializer;
import appeng.recipes.mattercannon.MatterCannonAmmoSerializer;
import appeng.recipes.quartzcutting.QuartzCuttingRecipeSerializer;
import appeng.recipes.transform.TransformRecipeSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AERecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> DR = DeferredRegister.create((ResourceKey)Registries.RECIPE_SERIALIZER, (String)"ae2");

    private AERecipeSerializers() {
    }

    private static void register(String id, RecipeSerializer<?> serializer) {
        DR.register(id, () -> serializer);
    }

    static {
        AERecipeSerializers.register("inscriber", InscriberRecipeSerializer.INSTANCE);
        AERecipeSerializers.register("facade", FacadeRecipe.SERIALIZER);
        AERecipeSerializers.register("entropy", EntropyRecipeSerializer.INSTANCE);
        AERecipeSerializers.register("matter_cannon", MatterCannonAmmoSerializer.INSTANCE);
        AERecipeSerializers.register("transform", TransformRecipeSerializer.INSTANCE);
        AERecipeSerializers.register("charger", ChargerRecipeSerializer.INSTANCE);
        AERecipeSerializers.register("storage_cell_upgrade", StorageCellUpgradeRecipeSerializer.INSTANCE);
        AERecipeSerializers.register("add_item_upgrade", AddItemUpgradeRecipeSerializer.INSTANCE);
        AERecipeSerializers.register("remove_item_upgrade", RemoveItemUpgradeRecipeSerializer.INSTANCE);
        AERecipeSerializers.register("quartz_cutting", QuartzCuttingRecipeSerializer.INSTANCE);
        AERecipeSerializers.register("crafting_unit_transform", CraftingUnitTransformRecipeSerializer.INSTANCE);
        AERecipeSerializers.register("storage_cell_disassembly", StorageCellDisassemblyRecipeSerializer.INSTANCE);
    }
}

