/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.NonNullList
 *  net.minecraft.data.recipes.RecipeOutput
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.ByteBufCodecs
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.item.crafting.Recipe
 *  net.minecraft.world.item.crafting.RecipeInput
 *  net.minecraft.world.item.crafting.RecipeSerializer
 *  net.minecraft.world.item.crafting.RecipeType
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.neoforged.neoforge.common.conditions.ICondition
 *  net.neoforged.neoforge.common.conditions.NotCondition
 *  net.neoforged.neoforge.common.conditions.TagEmptyCondition
 */
package appeng.recipes.mattercannon;

import appeng.core.AppEng;
import appeng.recipes.AERecipeTypes;
import appeng.recipes.mattercannon.MatterCannonAmmoSerializer;
import com.google.common.base.Preconditions;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.NotCondition;
import net.neoforged.neoforge.common.conditions.TagEmptyCondition;

public class MatterCannonAmmo
implements Recipe<RecipeInput> {
    @Deprecated(forRemoval=true, since="1.21.1")
    public static final ResourceLocation TYPE_ID = AppEng.makeId("matter_cannon");
    @Deprecated(forRemoval=true, since="1.21.1")
    public static final RecipeType<MatterCannonAmmo> TYPE = AERecipeTypes.MATTER_CANNON_AMMO;
    public static final MapCodec<MatterCannonAmmo> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group((App)Ingredient.CODEC_NONEMPTY.fieldOf("ammo").forGetter(MatterCannonAmmo::getAmmo), (App)Codec.FLOAT.fieldOf("weight").forGetter(MatterCannonAmmo::getWeight)).apply((Applicative)builder, MatterCannonAmmo::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, MatterCannonAmmo> STREAM_CODEC = StreamCodec.composite((StreamCodec)Ingredient.CONTENTS_STREAM_CODEC, MatterCannonAmmo::getAmmo, (StreamCodec)ByteBufCodecs.FLOAT, MatterCannonAmmo::getWeight, MatterCannonAmmo::new);
    private final Ingredient ammo;
    private final float weight;

    public MatterCannonAmmo(Ingredient ammo, float weight) {
        Preconditions.checkArgument((weight >= 0.0f ? 1 : 0) != 0, (Object)"Weight must not be negative");
        this.ammo = Objects.requireNonNull(ammo, "ammo must not be null");
        this.weight = weight;
    }

    public static void ammo(RecipeOutput consumer, ResourceLocation id, ItemLike item, float weight) {
        consumer.accept(id, (Recipe)new MatterCannonAmmo(Ingredient.of((ItemLike[])new ItemLike[]{item}), weight), null);
    }

    public static void ammo(RecipeOutput consumer, ResourceLocation id, Ingredient ammo, float weight) {
        consumer.accept(id, (Recipe)new MatterCannonAmmo(ammo, weight), null);
    }

    public static void ammo(RecipeOutput consumer, ResourceLocation id, TagKey<Item> tag, float weight) {
        consumer.accept(id, (Recipe)new MatterCannonAmmo(Ingredient.of(tag), weight), null, new ICondition[]{new NotCondition((ICondition)new TagEmptyCondition(tag.location()))});
    }

    public boolean matches(RecipeInput inv, Level level) {
        return false;
    }

    public ItemStack assemble(RecipeInput inv, HolderLookup.Provider registryAccess) {
        return ItemStack.EMPTY;
    }

    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    public ItemStack getResultItem(HolderLookup.Provider registryAccess) {
        return ItemStack.EMPTY;
    }

    public RecipeSerializer<?> getSerializer() {
        return MatterCannonAmmoSerializer.INSTANCE;
    }

    public RecipeType<?> getType() {
        return TYPE;
    }

    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.create();
    }

    public Ingredient getAmmo() {
        return this.ammo;
    }

    public float getWeight() {
        return this.weight;
    }
}

