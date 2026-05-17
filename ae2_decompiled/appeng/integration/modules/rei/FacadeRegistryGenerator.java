/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  me.shedaniel.rei.api.client.registry.display.DynamicDisplayGenerator
 *  me.shedaniel.rei.api.common.entry.EntryStack
 *  me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes
 *  me.shedaniel.rei.plugin.common.displays.crafting.DefaultShapedDisplay
 *  net.minecraft.core.NonNullList
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.CraftingBookCategory
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.item.crafting.Recipe
 *  net.minecraft.world.item.crafting.RecipeHolder
 *  net.minecraft.world.item.crafting.ShapedRecipe
 *  net.minecraft.world.item.crafting.ShapedRecipePattern
 */
package appeng.integration.modules.rei;

import appeng.core.AppEng;
import appeng.core.FacadeCreativeTab;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.items.parts.FacadeItem;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import me.shedaniel.rei.api.client.registry.display.DynamicDisplayGenerator;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultShapedDisplay;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

class FacadeRegistryGenerator
implements DynamicDisplayGenerator<DefaultShapedDisplay> {
    private final Ingredient cableAnchor;
    private final FacadeItem itemFacade = AEItems.FACADE.get();

    FacadeRegistryGenerator() {
        this.cableAnchor = Ingredient.of((ItemStack[])new ItemStack[]{AEParts.CABLE_ANCHOR.stack()});
    }

    public Optional<List<DefaultShapedDisplay>> getRecipeFor(EntryStack<?> entry) {
        if (entry.getType() != VanillaEntryTypes.ITEM) {
            return Optional.empty();
        }
        ItemStack itemStack = (ItemStack)entry.castValue();
        Item item = itemStack.getItem();
        if (item instanceof FacadeItem) {
            FacadeItem facadeItem = (FacadeItem)item;
            ItemStack textureItem = facadeItem.getTextureItem(itemStack);
            return Optional.of(Collections.singletonList(this.make(textureItem, itemStack.copy())));
        }
        return Optional.empty();
    }

    public Optional<List<DefaultShapedDisplay>> getUsageFor(EntryStack<?> entry) {
        if (entry.getType() != VanillaEntryTypes.ITEM) {
            return Optional.empty();
        }
        ItemStack itemStack = (ItemStack)entry.castValue();
        itemStack = itemStack.copy();
        itemStack.setCount(1);
        ItemStack facade = this.itemFacade.createFacadeForItem(itemStack, false);
        if (!facade.isEmpty()) {
            return Optional.of(Collections.singletonList(this.make(itemStack, facade)));
        }
        if (this.cableAnchor.test(itemStack)) {
            return Optional.of(FacadeCreativeTab.getDisplayItems().stream().map(stack -> {
                Item patt0$temp = stack.getItem();
                if (patt0$temp instanceof FacadeItem) {
                    FacadeItem facadeItem = (FacadeItem)patt0$temp;
                    ItemStack textureItem = facadeItem.getTextureItem((ItemStack)stack);
                    return this.make(textureItem, stack.copy());
                }
                return null;
            }).filter(Objects::nonNull).toList());
        }
        return Optional.empty();
    }

    private DefaultShapedDisplay make(ItemStack textureItem, ItemStack result) {
        NonNullList ingredients = NonNullList.withSize((int)9, (Object)Ingredient.EMPTY);
        ingredients.set(1, (Object)this.cableAnchor);
        ingredients.set(3, (Object)this.cableAnchor);
        ingredients.set(5, (Object)this.cableAnchor);
        ingredients.set(7, (Object)this.cableAnchor);
        ingredients.set(4, (Object)Ingredient.of((ItemStack[])new ItemStack[]{textureItem}));
        ShapedRecipePattern pattern = new ShapedRecipePattern(3, 3, ingredients, Optional.empty());
        result.setCount(4);
        ResourceLocation id = AppEng.makeId("facade/" + Item.getId((Item)textureItem.getItem()));
        return new DefaultShapedDisplay(new RecipeHolder(id, (Recipe)new ShapedRecipe("", CraftingBookCategory.MISC, pattern, result)));
    }
}

