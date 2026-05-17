/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.NonNullList
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.ByteBufCodecs
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.tags.FluidTags
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.item.crafting.Recipe
 *  net.minecraft.world.item.crafting.RecipeSerializer
 *  net.minecraft.world.item.crafting.RecipeType
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.material.Fluid
 */
package appeng.recipes.transform;

import appeng.blockentity.qnb.QuantumBridgeBlockEntity;
import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.recipes.AERecipeTypes;
import appeng.recipes.transform.TransformCircumstance;
import appeng.recipes.transform.TransformRecipeInput;
import appeng.recipes.transform.TransformRecipeSerializer;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

public final class TransformRecipe
implements Recipe<TransformRecipeInput> {
    @Deprecated(forRemoval=true, since="1.21.1")
    public static final ResourceLocation TYPE_ID = AppEng.makeId("transform");
    @Deprecated(forRemoval=true, since="1.21.1")
    public static final RecipeType<TransformRecipe> TYPE = AERecipeTypes.TRANSFORM;
    public static final MapCodec<TransformRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group((App)Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").flatXmap(ingredients -> DataResult.success((Object)NonNullList.of((Object)Ingredient.EMPTY, (Object[])((Ingredient[])ingredients.toArray(Ingredient[]::new)))), DataResult::success).forGetter(r -> r.ingredients), (App)ItemStack.CODEC.fieldOf("result").forGetter(r -> r.output), (App)TransformCircumstance.CODEC.optionalFieldOf("circumstance", (Object)TransformCircumstance.fluid((TagKey<Fluid>)FluidTags.WATER)).forGetter(t -> t.circumstance)).apply((Applicative)builder, TransformRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, TransformRecipe> STREAM_CODEC = StreamCodec.composite((StreamCodec)Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.collection(NonNullList::createWithCapacity)), TransformRecipe::getIngredients, (StreamCodec)ItemStack.STREAM_CODEC, TransformRecipe::getResultItem, TransformCircumstance.STREAM_CODEC, TransformRecipe::getCircumstance, TransformRecipe::new);
    public final NonNullList<Ingredient> ingredients;
    public final ItemStack output;
    public final TransformCircumstance circumstance;

    public TransformRecipe(NonNullList<Ingredient> ingredients, ItemStack output, TransformCircumstance circumstance) {
        this.ingredients = ingredients;
        this.output = output;
        this.circumstance = circumstance;
    }

    public NonNullList<Ingredient> getIngredients() {
        return this.ingredients;
    }

    public TransformCircumstance getCircumstance() {
        return this.circumstance;
    }

    public boolean matches(TransformRecipeInput container, Level level) {
        return false;
    }

    public ItemStack assemble(TransformRecipeInput container, HolderLookup.Provider registries) {
        ItemStack result = this.getResultItem(registries).copy();
        if (AEItems.QUANTUM_ENTANGLED_SINGULARITY.is(result) && result.getCount() > 1) {
            QuantumBridgeBlockEntity.assignFrequency(result);
        }
        return result;
    }

    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.getResultItem();
    }

    public ItemStack getResultItem() {
        return this.output;
    }

    public RecipeSerializer<?> getSerializer() {
        return TransformRecipeSerializer.INSTANCE;
    }

    public RecipeType<?> getType() {
        return TYPE;
    }

    public boolean isSpecial() {
        return true;
    }
}

