package com.colonylink.colonylink;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ColonyLinkRecipes
{
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(net.minecraft.core.registries.Registries.RECIPE_SERIALIZER, ColonyLink.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ClearNbtRecipe>> CLEAR_NBT_SERIALIZER =
            RECIPE_SERIALIZERS.register("clear_nbt", ClearNbtRecipeSerializer::new);

    public static class ClearNbtRecipeSerializer implements RecipeSerializer<ClearNbtRecipe>
    {
        public static final MapCodec<ClearNbtRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                CraftingBookCategory.CODEC
                        .fieldOf("category")
                        .orElse(CraftingBookCategory.MISC)
                        .forGetter(ClearNbtRecipe::category),
                BuiltInRegistries.ITEM.byNameCodec()
                        .fieldOf("target_item")
                        .forGetter(r -> r.targetItem)
        ).apply(inst, ClearNbtRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ClearNbtRecipe> STREAM_CODEC =
                StreamCodec.of(
                        (buf, recipe) -> {
                            buf.writeEnum(recipe.category());
                            buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(recipe.targetItem));
                        },
                        buf -> {
                            CraftingBookCategory cat = buf.readEnum(CraftingBookCategory.class);
                            Item item = BuiltInRegistries.ITEM.get(buf.readResourceLocation());
                            return new ClearNbtRecipe(cat, item);
                        }
                );

        @Override
        public MapCodec<ClearNbtRecipe> codec() { return CODEC; }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ClearNbtRecipe> streamCodec() { return STREAM_CODEC; }
    }
}