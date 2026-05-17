/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.NonNullList
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.item.crafting.Recipe
 *  net.neoforged.neoforge.network.PacketDistributor
 *  org.jetbrains.annotations.Nullable
 */
package appeng.integration.modules.itemlists;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.core.AELog;
import appeng.core.network.serverbound.FillCraftingGridFromRecipePacket;
import appeng.integration.modules.itemlists.EncodingHelper;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.me.items.CraftingTermMenu;
import appeng.util.CraftingRecipeUtil;
import java.util.Comparator;
import java.util.Map;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public final class CraftingHelper {
    private static final Comparator<GridInventoryEntry> ENTRY_COMPARATOR = Comparator.comparing(GridInventoryEntry::getStoredAmount);

    private CraftingHelper() {
    }

    public static void performTransfer(CraftingTermMenu menu, @Nullable ResourceLocation recipeId, Recipe<?> recipe, boolean craftMissing) {
        NonNullList<ItemStack> templateItems = CraftingHelper.findGoodTemplateItems(recipe, menu);
        if (recipeId != null && menu.getPlayer().level().getRecipeManager().byKey(recipeId).isEmpty()) {
            AELog.debug("Cannot send recipe id %s to server because it's transient", recipeId);
            recipeId = null;
        }
        FillCraftingGridFromRecipePacket message = new FillCraftingGridFromRecipePacket(recipeId, templateItems, craftMissing);
        PacketDistributor.sendToServer((CustomPacketPayload)message, (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    private static NonNullList<ItemStack> findGoodTemplateItems(Recipe<?> recipe, MEStorageMenu menu) {
        Map<AEKey, Integer> ingredientPriorities = EncodingHelper.getIngredientPriorities(menu, ENTRY_COMPARATOR);
        NonNullList templateItems = NonNullList.withSize((int)9, (Object)ItemStack.EMPTY);
        NonNullList<Ingredient> ingredients = CraftingRecipeUtil.ensure3by3CraftingMatrix(recipe);
        for (int i = 0; i < ingredients.size(); ++i) {
            Ingredient ingredient = (Ingredient)ingredients.get(i);
            if (ingredient.isEmpty()) continue;
            ItemStack stack = ingredientPriorities.entrySet().stream().filter(e -> {
                AEItemKey itemKey;
                Object patt0$temp = e.getKey();
                return patt0$temp instanceof AEItemKey && (itemKey = (AEItemKey)patt0$temp).matches(ingredient);
            }).max(Comparator.comparingInt(Map.Entry::getValue)).map(e -> ((AEItemKey)e.getKey()).toStack()).orElse(ingredient.getItems()[0]);
            templateItems.set(i, (Object)stack);
        }
        return templateItems;
    }
}

