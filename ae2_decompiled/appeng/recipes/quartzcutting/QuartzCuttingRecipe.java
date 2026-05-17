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
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.CraftingBookCategory
 *  net.minecraft.world.item.crafting.CraftingInput
 *  net.minecraft.world.item.crafting.CraftingRecipe
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.item.crafting.Recipe
 *  net.minecraft.world.item.crafting.RecipeSerializer
 *  net.minecraft.world.level.Level
 *  net.neoforged.neoforge.common.CommonHooks
 *  net.neoforged.neoforge.common.util.RecipeMatcher
 *  net.neoforged.neoforge.server.ServerLifecycleHooks
 *  org.apache.commons.lang3.mutable.MutableBoolean
 */
package appeng.recipes.quartzcutting;

import appeng.datagen.providers.tags.ConventionTags;
import appeng.recipes.quartzcutting.QuartzCuttingRecipeSerializer;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.util.RecipeMatcher;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class QuartzCuttingRecipe
implements CraftingRecipe {
    static final int MAX_HEIGHT = 3;
    static final int MAX_WIDTH = 3;
    public static final MapCodec<QuartzCuttingRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group((App)ItemStack.STRICT_CODEC.fieldOf("result").forGetter(QuartzCuttingRecipe::getResult), (App)Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").flatXmap(r -> {
        Object[] ingredients = (Ingredient[])r.toArray(Ingredient[]::new);
        if (ingredients.length == 0) {
            return DataResult.error(() -> "No ingredients for quartz cutting recipe");
        }
        return ingredients.length > 9 ? DataResult.error(() -> "Too many ingredients for quartz cutting recipe. The maximum is: %s".formatted(9)) : DataResult.success((Object)NonNullList.of((Object)Ingredient.EMPTY, (Object[])ingredients));
    }, DataResult::success).forGetter(QuartzCuttingRecipe::getIngredients)).apply((Applicative)builder, QuartzCuttingRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, QuartzCuttingRecipe> STREAM_CODEC = StreamCodec.composite((StreamCodec)ItemStack.STREAM_CODEC, QuartzCuttingRecipe::getResult, (StreamCodec)StreamCodec.of((buffer, value) -> {
        buffer.writeVarInt(value.size());
        for (Ingredient ingredient : value) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, (Object)ingredient);
        }
    }, buffer -> {
        int count = buffer.readVarInt();
        NonNullList ingredients = NonNullList.withSize((int)count, (Object)Ingredient.EMPTY);
        ingredients.replaceAll(ignored -> (Ingredient)Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
        return ingredients;
    }), QuartzCuttingRecipe::getIngredients, QuartzCuttingRecipe::new);
    final ItemStack result;
    final NonNullList<Ingredient> ingredients;
    private final boolean isSimple;

    public QuartzCuttingRecipe(ItemStack result, NonNullList<Ingredient> ingredients) {
        this.result = result;
        this.ingredients = ingredients;
        this.isSimple = ingredients.stream().allMatch(Ingredient::isSimple);
    }

    public RecipeSerializer<?> getSerializer() {
        return QuartzCuttingRecipeSerializer.INSTANCE;
    }

    public CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }

    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.result;
    }

    public NonNullList<Ingredient> getIngredients() {
        return this.ingredients;
    }

    public boolean matches(CraftingInput input, Level level) {
        if (input.ingredientCount() != this.ingredients.size()) {
            return false;
        }
        if (!this.isSimple) {
            ArrayList<ItemStack> nonEmptyItems = new ArrayList<ItemStack>(input.ingredientCount());
            for (ItemStack item : input.items()) {
                if (item.isEmpty()) continue;
                nonEmptyItems.add(item);
            }
            return RecipeMatcher.findMatches(nonEmptyItems, this.ingredients) != null;
        }
        if (input.size() == 1 && this.ingredients.size() == 1) {
            return ((Ingredient)this.ingredients.getFirst()).test(input.getItem(0));
        }
        return input.stackedContents().canCraft((Recipe)this, null);
    }

    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return this.result.copy();
    }

    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= this.ingredients.size();
    }

    private ItemStack getResult() {
        return this.result;
    }

    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList remainingItems = NonNullList.withSize((int)input.size(), (Object)ItemStack.EMPTY);
        boolean damagedKnife = false;
        for (int i = 0; i < remainingItems.size(); ++i) {
            ItemStack item = input.getItem(i);
            if (!damagedKnife && item.is(ConventionTags.QUARTZ_KNIFE)) {
                damagedKnife = true;
                ItemStack result = item.copy();
                MutableBoolean broken = new MutableBoolean(false);
                Player player = CommonHooks.getCraftingPlayer();
                if (player instanceof ServerPlayer) {
                    ServerPlayer serverPlayer = (ServerPlayer)player;
                    result.hurtAndBreak(1, serverPlayer.serverLevel(), serverPlayer, ignored -> broken.setTrue());
                } else {
                    MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
                    if (currentServer != null) {
                        result.hurtAndBreak(1, currentServer.overworld(), null, ignored -> broken.setTrue());
                    }
                }
                remainingItems.set(i, (Object)(broken.getValue() != false ? ItemStack.EMPTY : result));
                continue;
            }
            if (!item.hasCraftingRemainingItem()) continue;
            remainingItems.set(i, (Object)item.getCraftingRemainingItem());
        }
        return remainingItems;
    }
}

