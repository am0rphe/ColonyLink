/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.world.item.crafting.RecipeSerializer
 */
package appeng.recipes.transform;

import appeng.recipes.transform.TransformRecipe;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class TransformRecipeSerializer
implements RecipeSerializer<TransformRecipe> {
    public static final TransformRecipeSerializer INSTANCE = new TransformRecipeSerializer();

    private TransformRecipeSerializer() {
    }

    public MapCodec<TransformRecipe> codec() {
        return TransformRecipe.CODEC;
    }

    public StreamCodec<RegistryFriendlyByteBuf, TransformRecipe> streamCodec() {
        return TransformRecipe.STREAM_CODEC;
    }
}

