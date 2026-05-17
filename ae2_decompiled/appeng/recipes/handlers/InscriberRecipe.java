/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
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
 *  net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs
 */
package appeng.recipes.handlers;

import appeng.core.AppEng;
import appeng.recipes.AERecipeTypes;
import appeng.recipes.handlers.InscriberProcessType;
import appeng.recipes.handlers.InscriberRecipeSerializer;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
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
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

public class InscriberRecipe
implements Recipe<RecipeInput> {
    private static final Codec<InscriberProcessType> MODE_CODEC = Codec.stringResolver(mode -> switch (mode) {
        default -> throw new MatchException(null, null);
        case InscriberProcessType.INSCRIBE -> "inscribe";
        case InscriberProcessType.PRESS -> "press";
    }, mode -> switch (mode) {
        default -> InscriberProcessType.INSCRIBE;
        case "press" -> InscriberProcessType.PRESS;
    });
    public static final MapCodec<InscriberRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group((App)Ingredients.CODEC.fieldOf("ingredients").forGetter(InscriberRecipe::getSerializedIngredients), (App)ItemStack.CODEC.fieldOf("result").forGetter(ir -> ir.output), (App)MODE_CODEC.fieldOf("mode").forGetter(ir -> ir.processType)).apply((Applicative)builder, InscriberRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, InscriberRecipe> STREAM_CODEC = StreamCodec.composite(Ingredients.STREAM_CODEC, InscriberRecipe::getSerializedIngredients, (StreamCodec)ItemStack.STREAM_CODEC, InscriberRecipe::getResultItem, (StreamCodec)NeoForgeStreamCodecs.enumCodec(InscriberProcessType.class), InscriberRecipe::getProcessType, InscriberRecipe::new);
    @Deprecated(forRemoval=true, since="1.21.1")
    public static final ResourceLocation TYPE_ID = AppEng.makeId("inscriber");
    @Deprecated(forRemoval=true, since="1.21.1")
    public static final RecipeType<InscriberRecipe> TYPE = AERecipeTypes.INSCRIBER;
    private final Ingredient middleInput;
    private final Ingredient topOptional;
    private final Ingredient bottomOptional;
    private final ItemStack output;
    private final InscriberProcessType processType;

    private InscriberRecipe(Ingredients ingredients, ItemStack output, InscriberProcessType processType) {
        this(ingredients.middle(), output, ingredients.top(), ingredients.bottom(), processType);
    }

    public InscriberRecipe(Ingredient middleInput, ItemStack output, Ingredient topOptional, Ingredient bottomOptional, InscriberProcessType processType) {
        this.middleInput = Objects.requireNonNull(middleInput, "middleInput");
        this.output = Objects.requireNonNull(output, "output");
        this.topOptional = Objects.requireNonNull(topOptional, "topOptional");
        this.bottomOptional = Objects.requireNonNull(bottomOptional, "bottomOptional");
        this.processType = Objects.requireNonNull(processType, "processType");
    }

    public boolean matches(RecipeInput inv, Level level) {
        return false;
    }

    public ItemStack assemble(RecipeInput inv, HolderLookup.Provider registries) {
        return this.getResultItem(registries).copy();
    }

    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.getResultItem();
    }

    public ItemStack getResultItem() {
        return this.output;
    }

    public RecipeSerializer<?> getSerializer() {
        return InscriberRecipeSerializer.INSTANCE;
    }

    public RecipeType<?> getType() {
        return TYPE;
    }

    public NonNullList<Ingredient> getIngredients() {
        NonNullList ingredients = NonNullList.create();
        ingredients.add((Object)this.topOptional);
        ingredients.add((Object)this.middleInput);
        ingredients.add((Object)this.bottomOptional);
        return ingredients;
    }

    public Ingredient getMiddleInput() {
        return this.middleInput;
    }

    public Ingredient getTopOptional() {
        return this.topOptional;
    }

    public Ingredient getBottomOptional() {
        return this.bottomOptional;
    }

    public InscriberProcessType getProcessType() {
        return this.processType;
    }

    public boolean isSpecial() {
        return true;
    }

    private Ingredients getSerializedIngredients() {
        return new Ingredients(this.topOptional, this.middleInput, this.bottomOptional);
    }

    private record Ingredients(Ingredient top, Ingredient middle, Ingredient bottom) {
        public static final Codec<Ingredients> CODEC = RecordCodecBuilder.create(builder -> builder.group((App)Ingredient.CODEC.optionalFieldOf("top", (Object)Ingredient.EMPTY).forGetter(Ingredients::top), (App)Ingredient.CODEC_NONEMPTY.fieldOf("middle").forGetter(Ingredients::middle), (App)Ingredient.CODEC.optionalFieldOf("bottom", (Object)Ingredient.EMPTY).forGetter(Ingredients::bottom)).apply((Applicative)builder, Ingredients::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, Ingredients> STREAM_CODEC = StreamCodec.composite((StreamCodec)Ingredient.CONTENTS_STREAM_CODEC, Ingredients::top, (StreamCodec)Ingredient.CONTENTS_STREAM_CODEC, Ingredients::middle, (StreamCodec)Ingredient.CONTENTS_STREAM_CODEC, Ingredients::bottom, Ingredients::new);
    }
}

