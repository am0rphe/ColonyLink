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

import appeng.recipes.game.AddItemUpgradeRecipe;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class AddItemUpgradeRecipeSerializer
implements RecipeSerializer<AddItemUpgradeRecipe> {
    public static final AddItemUpgradeRecipeSerializer INSTANCE = new AddItemUpgradeRecipeSerializer();

    public MapCodec<AddItemUpgradeRecipe> codec() {
        return AddItemUpgradeRecipe.CODEC;
    }

    public StreamCodec<RegistryFriendlyByteBuf, AddItemUpgradeRecipe> streamCodec() {
        return AddItemUpgradeRecipe.STREAM_CODEC;
    }
}

