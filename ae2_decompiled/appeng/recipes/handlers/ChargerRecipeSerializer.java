/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.world.item.crafting.RecipeSerializer
 */
package appeng.recipes.handlers;

import appeng.recipes.handlers.ChargerRecipe;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class ChargerRecipeSerializer
implements RecipeSerializer<ChargerRecipe> {
    public static final ChargerRecipeSerializer INSTANCE = new ChargerRecipeSerializer();

    public MapCodec<ChargerRecipe> codec() {
        return ChargerRecipe.CODEC;
    }

    public StreamCodec<RegistryFriendlyByteBuf, ChargerRecipe> streamCodec() {
        return ChargerRecipe.STREAM_CODEC;
    }
}

