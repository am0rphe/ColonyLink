/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  dev.emi.emi.api.recipe.EmiCraftingRecipe
 *  dev.emi.emi.api.recipe.EmiRecipe
 *  dev.emi.emi.api.stack.EmiStack
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 */
package appeng.integration.modules.emi;

import appeng.core.definitions.AEParts;
import appeng.items.parts.FacadeItem;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

class EmiFacadeGenerator {
    private final EmiStack cableAnchor = EmiStack.of((ItemStack)AEParts.CABLE_ANCHOR.stack());

    EmiFacadeGenerator() {
    }

    public Optional<EmiRecipe> getRecipeFor(ItemStack potentialFacade) {
        if (potentialFacade.isEmpty()) {
            return Optional.empty();
        }
        Item item = potentialFacade.getItem();
        if (item instanceof FacadeItem) {
            FacadeItem facadeItem = (FacadeItem)item;
            ItemStack textureItem = facadeItem.getTextureItem(potentialFacade);
            return Optional.of(this.make(textureItem, potentialFacade.copy()));
        }
        return Optional.empty();
    }

    private EmiRecipe make(ItemStack textureItem, ItemStack result) {
        EmiStack textureStack = EmiStack.of((ItemStack)textureItem);
        EmiStack resultStack = EmiStack.of((ItemStack)result, (long)4L);
        List<EmiStack> input = List.of(EmiStack.EMPTY, this.cableAnchor, EmiStack.EMPTY, this.cableAnchor, textureStack, this.cableAnchor, EmiStack.EMPTY, this.cableAnchor, EmiStack.EMPTY);
        return new EmiCraftingRecipe(input, resultStack, null, false);
    }
}

