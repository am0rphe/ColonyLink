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
 *  net.minecraft.world.item.crafting.RecipeSerializer
 *  net.minecraft.world.level.Level
 */
package appeng.recipes.game;

import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableItem;
import appeng.core.AppEng;
import appeng.recipes.game.AddItemUpgradeRecipeSerializer;
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
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class AddItemUpgradeRecipe
extends CustomRecipe {
    public static final AddItemUpgradeRecipe INSTANCE = new AddItemUpgradeRecipe();
    public static final ResourceLocation SERIALIZER_ID = AppEng.makeId("add_item_upgrade");
    private static final NonNullList<Ingredient> INGREDIENTS = NonNullList.create();
    public static final MapCodec<AddItemUpgradeRecipe> CODEC = MapCodec.unit((Object)((Object)INSTANCE));
    public static final StreamCodec<RegistryFriendlyByteBuf, AddItemUpgradeRecipe> STREAM_CODEC = StreamCodec.unit((Object)((Object)INSTANCE));

    private AddItemUpgradeRecipe() {
        super(CraftingBookCategory.MISC);
    }

    public NonNullList<Ingredient> getIngredients() {
        return INGREDIENTS;
    }

    private static ItemStack attemptUpgrade(CraftingInput input) {
        if (input.ingredientCount() < 2) {
            return ItemStack.EMPTY;
        }
        for (int i = 0; i < input.size(); ++i) {
            ItemStack stack = input.getItem(i);
            Item item = stack.getItem();
            if (!(item instanceof IUpgradeableItem)) continue;
            IUpgradeableItem upgradableItem = (IUpgradeableItem)item;
            ItemStack upgraded = stack.copy();
            IUpgradeInventory upgrades = upgradableItem.getUpgrades(upgraded);
            for (int j = 0; j < input.size(); ++j) {
                ItemStack upgrade;
                if (j == i || (upgrade = input.getItem(j)).isEmpty() || upgrades.addItems(upgrade = upgrade.copyWithCount(1)).isEmpty()) continue;
                return ItemStack.EMPTY;
            }
            return upgraded;
        }
        return ItemStack.EMPTY;
    }

    public boolean matches(CraftingInput container, Level level) {
        return !AddItemUpgradeRecipe.attemptUpgrade(container).isEmpty();
    }

    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    public ItemStack assemble(CraftingInput container, HolderLookup.Provider registries) {
        return AddItemUpgradeRecipe.attemptUpgrade(container);
    }

    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    public RecipeSerializer<?> getSerializer() {
        return AddItemUpgradeRecipeSerializer.INSTANCE;
    }
}

