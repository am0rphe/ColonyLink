/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.CraftingBookCategory
 *  net.minecraft.world.item.crafting.CraftingInput
 *  net.minecraft.world.item.crafting.CustomRecipe
 *  net.minecraft.world.item.crafting.RecipeSerializer
 *  net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer
 *  net.minecraft.world.level.Level
 */
package appeng.recipes.game;

import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.core.definitions.ItemDefinition;
import appeng.items.parts.FacadeItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;

public final class FacadeRecipe
extends CustomRecipe {
    public static RecipeSerializer<FacadeRecipe> SERIALIZER = new SimpleCraftingRecipeSerializer(category -> new FacadeRecipe(category, AEItems.FACADE.get()));
    private final ItemDefinition<?> anchor = AEParts.CABLE_ANCHOR;
    private final FacadeItem facade;

    public FacadeRecipe(CraftingBookCategory category, FacadeItem facade) {
        super(category);
        this.facade = facade;
    }

    public boolean matches(CraftingInput inv, Level level) {
        return !this.getOutput(inv, false).isEmpty();
    }

    private ItemStack getOutput(CraftingInput inv, boolean createFacade) {
        if (inv.width() == 3 && inv.height() == 3 && inv.getItem(0).isEmpty() && inv.getItem(2).isEmpty() && inv.getItem(6).isEmpty() && inv.getItem(8).isEmpty() && this.anchor.is(inv.getItem(1)) && this.anchor.is(inv.getItem(3)) && this.anchor.is(inv.getItem(5)) && this.anchor.is(inv.getItem(7))) {
            ItemStack facades = this.facade.createFacadeForItem(inv.getItem(4), !createFacade);
            if (!facades.isEmpty() && createFacade) {
                facades.setCount(4);
            }
            return facades;
        }
        return ItemStack.EMPTY;
    }

    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider registryAccess) {
        return this.getOutput(inv, true);
    }

    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }

    public RecipeSerializer<FacadeRecipe> getSerializer() {
        return SERIALIZER;
    }
}

