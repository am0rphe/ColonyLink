/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.ByteBufCodecs
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.CraftingBookCategory
 *  net.minecraft.world.item.crafting.CraftingInput
 *  net.minecraft.world.item.crafting.CustomRecipe
 *  net.minecraft.world.item.crafting.RecipeHolder
 *  net.minecraft.world.item.crafting.RecipeManager
 *  net.minecraft.world.item.crafting.RecipeSerializer
 *  net.minecraft.world.item.crafting.RecipeType
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 */
package appeng.recipes.game;

import appeng.recipes.AERecipeTypes;
import appeng.recipes.game.CraftingUnitTransformRecipeSerializer;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class CraftingUnitTransformRecipe
extends CustomRecipe {
    public static final MapCodec<CraftingUnitTransformRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group((App)BuiltInRegistries.BLOCK.byNameCodec().fieldOf("upgraded_block").forGetter(CraftingUnitTransformRecipe::getUpgradedBlock), (App)BuiltInRegistries.ITEM.byNameCodec().fieldOf("upgrade_item").forGetter(CraftingUnitTransformRecipe::getUpgradeItem)).apply((Applicative)builder, CraftingUnitTransformRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, CraftingUnitTransformRecipe> STREAM_CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.registry((ResourceKey)BuiltInRegistries.BLOCK.key()), CraftingUnitTransformRecipe::getUpgradedBlock, (StreamCodec)ByteBufCodecs.registry((ResourceKey)BuiltInRegistries.ITEM.key()), CraftingUnitTransformRecipe::getUpgradeItem, CraftingUnitTransformRecipe::new);
    private final Block upgradedBlock;
    private final Item upgradeItem;

    public CraftingUnitTransformRecipe(Block upgradedBlock, Item upgradeItem) {
        super(CraftingBookCategory.MISC);
        this.upgradedBlock = upgradedBlock;
        this.upgradeItem = upgradeItem;
    }

    public Block getUpgradedBlock() {
        return this.upgradedBlock;
    }

    public Item getUpgradeItem() {
        return this.upgradeItem;
    }

    public static ItemStack getRemovedUpgrade(Level level, Block upgradedBlock) {
        RecipeManager recipeManager = level.getRecipeManager();
        for (RecipeHolder holder : recipeManager.byType(AERecipeTypes.CRAFTING_UNIT_TRANSFORM)) {
            if (((CraftingUnitTransformRecipe)holder.value()).upgradedBlock != upgradedBlock) continue;
            return ((CraftingUnitTransformRecipe)holder.value()).upgradeItem.getDefaultInstance();
        }
        return ItemStack.EMPTY;
    }

    public static Block getUpgradedBlock(Level level, ItemStack upgradeItem) {
        for (RecipeHolder holder : level.getRecipeManager().byType(AERecipeTypes.CRAFTING_UNIT_TRANSFORM)) {
            if (!upgradeItem.is(((CraftingUnitTransformRecipe)holder.value()).getUpgradeItem())) continue;
            return ((CraftingUnitTransformRecipe)holder.value()).upgradedBlock;
        }
        return null;
    }

    public boolean matches(CraftingInput input, Level level) {
        return false;
    }

    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    public RecipeSerializer<?> getSerializer() {
        return CraftingUnitTransformRecipeSerializer.INSTANCE;
    }

    public RecipeType<?> getType() {
        return AERecipeTypes.CRAFTING_UNIT_TRANSFORM;
    }
}

