/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.NonNullList
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.item.crafting.Recipe
 *  net.minecraft.world.item.crafting.RecipeInput
 *  net.minecraft.world.item.crafting.RecipeSerializer
 *  net.minecraft.world.item.crafting.RecipeType
 *  net.minecraft.world.level.Level
 */
package appeng.recipes.handlers;

import appeng.core.AppEng;
import appeng.recipes.AERecipeTypes;
import appeng.recipes.handlers.ChargerRecipeSerializer;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class ChargerRecipe
implements Recipe<RecipeInput> {
    @Deprecated(forRemoval=true, since="1.21.1")
    public static final ResourceLocation TYPE_ID = AppEng.makeId("charger");
    @Deprecated(forRemoval=true, since="1.21.1")
    public static final RecipeType<ChargerRecipe> TYPE = AERecipeTypes.CHARGER;
    public final Ingredient ingredient;
    public final NonNullList<Ingredient> ingredients;
    public final ItemStack result;
    public static final MapCodec<ChargerRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group((App)Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(ChargerRecipe::getIngredient), (App)ItemStack.CODEC.fieldOf("result").forGetter(cr -> cr.result)).apply((Applicative)builder, ChargerRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChargerRecipe> STREAM_CODEC = StreamCodec.composite((StreamCodec)Ingredient.CONTENTS_STREAM_CODEC, ChargerRecipe::getIngredient, (StreamCodec)ItemStack.STREAM_CODEC, ChargerRecipe::getResultItem, ChargerRecipe::new);

    public ChargerRecipe(Ingredient ingredient, ItemStack result) {
        this.ingredient = ingredient;
        this.result = result;
        this.ingredients = NonNullList.of((Object)Ingredient.EMPTY, (Object[])new Ingredient[]{ingredient});
    }

    public boolean matches(RecipeInput container, Level level) {
        return false;
    }

    public ItemStack assemble(RecipeInput container, HolderLookup.Provider registries) {
        return null;
    }

    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.getResultItem();
    }

    public ItemStack getResultItem() {
        return this.result;
    }

    public RecipeSerializer<?> getSerializer() {
        return ChargerRecipeSerializer.INSTANCE;
    }

    public RecipeType<?> getType() {
        return TYPE;
    }

    public Ingredient getIngredient() {
        return this.ingredient;
    }

    public NonNullList<Ingredient> getIngredients() {
        return this.ingredients;
    }

    public boolean isSpecial() {
        return true;
    }
}

