/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  dev.emi.emi.api.recipe.EmiRecipe
 *  dev.emi.emi.api.recipe.handler.EmiCraftContext
 *  dev.emi.emi.api.recipe.handler.EmiCraftContext$Type
 *  dev.emi.emi.api.stack.EmiIngredient
 *  dev.emi.emi.api.stack.EmiStack
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.crafting.Recipe
 *  net.minecraft.world.item.crafting.RecipeHolder
 */
package appeng.integration.modules.emi;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.core.localization.ItemModText;
import appeng.integration.modules.emi.AbstractRecipeHandler;
import appeng.integration.modules.emi.EmiStackHelper;
import appeng.integration.modules.itemlists.EncodingHelper;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.common.IClientRepo;
import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.me.items.PatternEncodingTermMenu;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public class EmiEncodePatternHandler<T extends PatternEncodingTermMenu>
extends AbstractRecipeHandler<T> {
    public EmiEncodePatternHandler(Class<T> containerClass) {
        super(containerClass);
    }

    @Override
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<T> context) {
        if (context.getType() == EmiCraftContext.Type.FILL_BUTTON) {
            return this.transferRecipe(recipe, context, false).canCraft();
        }
        return false;
    }

    @Override
    protected AbstractRecipeHandler.Result transferRecipe(T menu, RecipeHolder<?> holder, EmiRecipe emiRecipe, boolean doTransfer) {
        ResourceLocation recipeId = holder != null ? holder.id() : null;
        Recipe recipe = holder != null ? holder.value() : null;
        boolean craftingRecipe = this.isCraftingRecipe(recipe, emiRecipe);
        if (craftingRecipe && !this.fitsIn3x3Grid(recipe, emiRecipe)) {
            return AbstractRecipeHandler.Result.createFailed((Component)ItemModText.RECIPE_TOO_LARGE.text());
        }
        if (doTransfer) {
            if (craftingRecipe && recipeId != null) {
                EncodingHelper.encodeCraftingRecipe(menu, new RecipeHolder(recipeId, recipe), this.getGuiIngredientsForCrafting(emiRecipe), stack -> true);
            } else {
                EncodingHelper.encodeProcessingRecipe(menu, EmiStackHelper.ofInputs(emiRecipe), EmiStackHelper.ofOutputs(emiRecipe));
            }
        } else {
            IClientRepo repo = ((MEStorageMenu)menu).getClientRepo();
            Set<AEKey> craftableKeys = repo != null ? repo.getAllEntries().stream().filter(GridInventoryEntry::isCraftable).map(GridInventoryEntry::getWhat).collect(Collectors.toSet()) : Set.of();
            return new AbstractRecipeHandler.Result.EncodeWithCraftables(craftableKeys);
        }
        return AbstractRecipeHandler.Result.createSuccessful();
    }

    private List<List<GenericStack>> getGuiIngredientsForCrafting(EmiRecipe emiRecipe) {
        ArrayList<List<GenericStack>> result = new ArrayList<List<GenericStack>>(9);
        for (int i = 0; i < 9; ++i) {
            ArrayList<GenericStack> stacks = new ArrayList<GenericStack>();
            if (i < emiRecipe.getInputs().size()) {
                for (EmiStack emiStack : ((EmiIngredient)emiRecipe.getInputs().get(i)).getEmiStacks()) {
                    GenericStack genericStack = EmiStackHelper.toGenericStack(emiStack);
                    if (genericStack == null || !(genericStack.what() instanceof AEItemKey)) continue;
                    stacks.add(genericStack);
                }
            }
            result.add(stacks);
        }
        return result;
    }
}

