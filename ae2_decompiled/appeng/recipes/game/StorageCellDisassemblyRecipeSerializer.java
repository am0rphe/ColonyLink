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

import appeng.recipes.game.StorageCellDisassemblyRecipe;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class StorageCellDisassemblyRecipeSerializer
implements RecipeSerializer<StorageCellDisassemblyRecipe> {
    public static final StorageCellDisassemblyRecipeSerializer INSTANCE = new StorageCellDisassemblyRecipeSerializer();

    private StorageCellDisassemblyRecipeSerializer() {
    }

    public MapCodec<StorageCellDisassemblyRecipe> codec() {
        return StorageCellDisassemblyRecipe.CODEC;
    }

    public StreamCodec<RegistryFriendlyByteBuf, StorageCellDisassemblyRecipe> streamCodec() {
        return StorageCellDisassemblyRecipe.STREAM_CODEC;
    }
}

