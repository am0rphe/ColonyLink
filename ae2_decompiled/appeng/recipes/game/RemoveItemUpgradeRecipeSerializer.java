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

import appeng.recipes.game.RemoveItemUpgradeRecipe;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class RemoveItemUpgradeRecipeSerializer
implements RecipeSerializer<RemoveItemUpgradeRecipe> {
    public static final RemoveItemUpgradeRecipeSerializer INSTANCE = new RemoveItemUpgradeRecipeSerializer();

    public MapCodec<RemoveItemUpgradeRecipe> codec() {
        return RemoveItemUpgradeRecipe.CODEC;
    }

    public StreamCodec<RegistryFriendlyByteBuf, RemoveItemUpgradeRecipe> streamCodec() {
        return RemoveItemUpgradeRecipe.STREAM_CODEC;
    }
}

