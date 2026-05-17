/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.NonNullList
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.CraftingBookCategory
 *  net.minecraft.world.item.crafting.CraftingInput
 *  net.minecraft.world.item.crafting.CustomRecipe
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.item.crafting.RecipeInput
 *  net.minecraft.world.item.crafting.RecipeSerializer
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.Nullable
 */
package appeng.recipes.game;

import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableItem;
import appeng.core.AppEng;
import appeng.recipes.game.RemoveItemUpgradeRecipeSerializer;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class RemoveItemUpgradeRecipe
extends CustomRecipe {
    public static final RemoveItemUpgradeRecipe INSTANCE = new RemoveItemUpgradeRecipe();
    public static final ResourceLocation SERIALIZER_ID = AppEng.makeId("remove_item_upgrade");
    private static final NonNullList<Ingredient> INGREDIENTS = NonNullList.create();
    public static final MapCodec<RemoveItemUpgradeRecipe> CODEC = MapCodec.unit((Object)((Object)INSTANCE));
    public static final StreamCodec<RegistryFriendlyByteBuf, RemoveItemUpgradeRecipe> STREAM_CODEC = StreamCodec.unit((Object)((Object)INSTANCE));

    private RemoveItemUpgradeRecipe() {
        super(CraftingBookCategory.MISC);
    }

    public NonNullList<Ingredient> getIngredients() {
        return INGREDIENTS;
    }

    @Nullable
    private static RemovalResult attemptRemoval(CraftingInput input) {
        if (input.size() != 1) {
            return null;
        }
        ItemStack item = input.getItem(0);
        Item item2 = item.getItem();
        if (!(item2 instanceof IUpgradeableItem)) {
            return null;
        }
        IUpgradeableItem upgradableItem = (IUpgradeableItem)item2;
        ItemStack upgradable = item.copy();
        IUpgradeInventory upgrades = upgradableItem.getUpgrades(upgradable);
        for (int i = 0; i < upgrades.size(); ++i) {
            ItemStack upgrade = upgrades.extractItem(i, 1, false);
            if (upgrade.isEmpty()) continue;
            return new RemovalResult(upgradable, upgrade);
        }
        return null;
    }

    public boolean matches(CraftingInput input, Level level) {
        return RemoveItemUpgradeRecipe.attemptRemoval(input) != null;
    }

    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        RemovalResult result = RemoveItemUpgradeRecipe.attemptRemoval(input);
        return result != null ? result.upgrade() : ItemStack.EMPTY;
    }

    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        RemovalResult result = RemoveItemUpgradeRecipe.attemptRemoval(input);
        if (result == null || input.size() != 1) {
            return super.getRemainingItems((RecipeInput)input);
        }
        return NonNullList.of((Object)ItemStack.EMPTY, (Object[])new ItemStack[]{result.upgradableItem()});
    }

    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    public RecipeSerializer<?> getSerializer() {
        return RemoveItemUpgradeRecipeSerializer.INSTANCE;
    }

    record RemovalResult(ItemStack upgradableItem, ItemStack upgrade) {
    }
}

