/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.world.item.crafting.RecipeSerializer
 */
package appeng.recipes.entropy;

import appeng.recipes.entropy.EntropyRecipe;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class EntropyRecipeSerializer
implements RecipeSerializer<EntropyRecipe> {
    public static final EntropyRecipeSerializer INSTANCE = new EntropyRecipeSerializer();

    private EntropyRecipeSerializer() {
    }

    public MapCodec<EntropyRecipe> codec() {
        return EntropyRecipe.CODEC;
    }

    public StreamCodec<RegistryFriendlyByteBuf, EntropyRecipe> streamCodec() {
        return EntropyRecipe.STREAM_CODEC;
    }
}

