/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.world.item.crafting.RecipeSerializer
 */
package appeng.recipes.game;

import appeng.recipes.game.CraftingUnitTransformRecipe;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class CraftingUnitTransformRecipeSerializer
implements RecipeSerializer<CraftingUnitTransformRecipe> {
    public static final CraftingUnitTransformRecipeSerializer INSTANCE = new CraftingUnitTransformRecipeSerializer();

    private CraftingUnitTransformRecipeSerializer() {
    }

    public MapCodec<CraftingUnitTransformRecipe> codec() {
        return CraftingUnitTransformRecipe.CODEC;
    }

    public StreamCodec<RegistryFriendlyByteBuf, CraftingUnitTransformRecipe> streamCodec() {
        return CraftingUnitTransformRecipe.STREAM_CODEC;
    }
}

