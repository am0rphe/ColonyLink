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

import appeng.recipes.handlers.InscriberRecipe;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class InscriberRecipeSerializer
implements RecipeSerializer<InscriberRecipe> {
    public static final InscriberRecipeSerializer INSTANCE = new InscriberRecipeSerializer();

    private InscriberRecipeSerializer() {
    }

    public MapCodec<InscriberRecipe> codec() {
        return InscriberRecipe.CODEC;
    }

    public StreamCodec<RegistryFriendlyByteBuf, InscriberRecipe> streamCodec() {
        return InscriberRecipe.STREAM_CODEC;
    }
}

