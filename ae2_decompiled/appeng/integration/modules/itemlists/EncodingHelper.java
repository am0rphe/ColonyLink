/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.math.LongMath
 *  net.minecraft.core.NonNullList
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.item.crafting.Recipe
 *  net.minecraft.world.item.crafting.RecipeHolder
 *  net.minecraft.world.item.crafting.RecipeType
 *  net.neoforged.neoforge.network.PacketDistributor
 *  org.apache.commons.lang3.tuple.Pair
 *  org.jetbrains.annotations.Nullable
 */
package appeng.integration.modules.itemlists;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.core.network.serverbound.InventoryActionPacket;
import appeng.helpers.InventoryAction;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.menu.slot.FakeSlot;
import appeng.parts.encoding.EncodingMode;
import appeng.util.CraftingRecipeUtil;
import com.google.common.math.LongMath;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

public final class EncodingHelper {
    static final Comparator<GridInventoryEntry> ENTRY_COMPARATOR = Comparator.comparing(GridInventoryEntry::isCraftable).thenComparing(EncodingHelper::isUndamaged).thenComparing(GridInventoryEntry::getStoredAmount);

    private EncodingHelper() {
    }

    private static Boolean isUndamaged(GridInventoryEntry entry) {
        AEItemKey itemKey;
        AEKey aEKey = entry.getWhat();
        return !(aEKey instanceof AEItemKey) || !(itemKey = (AEItemKey)aEKey).isDamaged();
    }

    public static void encodeProcessingRecipe(PatternEncodingTermMenu menu, List<List<GenericStack>> genericIngredients, List<GenericStack> genericResults) {
        menu.setMode(EncodingMode.PROCESSING);
        Map<AEKey, Integer> ingredientPriorities = EncodingHelper.getIngredientPriorities(menu, ENTRY_COMPARATOR);
        EncodingHelper.encodeBestMatchingStacksIntoSlots(genericIngredients, ingredientPriorities, menu.getProcessingInputSlots());
        EncodingHelper.encodeBestMatchingStacksIntoSlots(genericResults.stream().map(List::of).toList(), ingredientPriorities, menu.getProcessingOutputSlots());
    }

    private static void encodeBestMatchingStacksIntoSlots(List<List<GenericStack>> possibleInputsBySlot, Map<AEKey, Integer> ingredientPriorities, FakeSlot[] slots) {
        ArrayList<GenericStack> encodedInputs = new ArrayList<GenericStack>();
        for (List<GenericStack> genericIngredient : possibleInputsBySlot) {
            if (genericIngredient.isEmpty()) continue;
            EncodingHelper.addOrMerge(encodedInputs, EncodingHelper.findBestIngredient(ingredientPriorities, genericIngredient));
        }
        for (int i = 0; i < slots.length; ++i) {
            FakeSlot slot = slots[i];
            ItemStack stack = i < encodedInputs.size() ? GenericStack.wrapInItemStack(encodedInputs.get(i)) : ItemStack.EMPTY;
            InventoryActionPacket message = new InventoryActionPacket(InventoryAction.SET_FILTER, slot.index, stack);
            PacketDistributor.sendToServer((CustomPacketPayload)message, (CustomPacketPayload[])new CustomPacketPayload[0]);
        }
    }

    public static boolean isSupportedCraftingRecipe(@Nullable Recipe<?> recipe) {
        if (recipe == null) {
            return false;
        }
        RecipeType recipeType = recipe.getType();
        return recipeType == RecipeType.CRAFTING || recipeType == RecipeType.STONECUTTING || recipeType == RecipeType.SMITHING;
    }

    public static void encodeCraftingRecipe(PatternEncodingTermMenu menu, @Nullable RecipeHolder<?> recipe, List<List<GenericStack>> genericIngredients, Predicate<ItemStack> visiblePredicate) {
        if (recipe != null && recipe.value().getType().equals((Object)RecipeType.STONECUTTING)) {
            menu.setMode(EncodingMode.STONECUTTING);
            menu.setStonecuttingRecipeId(recipe.id());
        } else if (recipe != null && recipe.value().getType().equals((Object)RecipeType.SMITHING)) {
            menu.setMode(EncodingMode.SMITHING_TABLE);
        } else {
            menu.setMode(EncodingMode.CRAFTING);
        }
        Map<AEKey, Integer> prioritizedNetworkInv = EncodingHelper.getIngredientPriorities(menu, ENTRY_COMPARATOR);
        NonNullList encodedInputs = NonNullList.withSize((int)menu.getCraftingGridSlots().length, (Object)ItemStack.EMPTY);
        if (recipe != null) {
            NonNullList<Ingredient> ingredients3x3 = CraftingRecipeUtil.ensure3by3CraftingMatrix(recipe.value());
            for (int slot = 0; slot < ingredients3x3.size(); ++slot) {
                Ingredient ingredient = (Ingredient)ingredients3x3.get(slot);
                if (ingredient.isEmpty()) continue;
                Optional<ItemStack> bestNetworkIngredient = prioritizedNetworkInv.entrySet().stream().filter(ni -> {
                    AEItemKey itemKey;
                    Object patt0$temp = ni.getKey();
                    return patt0$temp instanceof AEItemKey && (itemKey = (AEItemKey)patt0$temp).matches(ingredient);
                }).max(Comparator.comparingInt(Map.Entry::getValue)).map(entry -> {
                    ItemStack itemStack;
                    Object patt0$temp = entry.getKey();
                    if (patt0$temp instanceof AEItemKey) {
                        AEItemKey itemKey = (AEItemKey)patt0$temp;
                        itemStack = itemKey.toStack();
                    } else {
                        itemStack = null;
                    }
                    return itemStack;
                });
                ItemStack bestIngredient = bestNetworkIngredient.orElseGet(() -> {
                    for (ItemStack stack : ingredient.getItems()) {
                        if (!visiblePredicate.test(stack)) continue;
                        return stack;
                    }
                    return ingredient.getItems()[0];
                });
                encodedInputs.set(slot, (Object)bestIngredient);
            }
        } else {
            for (int slot = 0; slot < genericIngredients.size(); ++slot) {
                List<GenericStack> genericIngredient = genericIngredients.get(slot);
                if (genericIngredient.isEmpty()) continue;
                AEKey bestIngredient = EncodingHelper.findBestIngredient(prioritizedNetworkInv, genericIngredient).what();
                if (bestIngredient instanceof AEItemKey) {
                    AEItemKey itemKey = (AEItemKey)bestIngredient;
                    encodedInputs.set(slot, (Object)itemKey.toStack());
                    continue;
                }
                encodedInputs.set(slot, (Object)GenericStack.wrapInItemStack(bestIngredient, 1L));
            }
        }
        for (int i = 0; i < encodedInputs.size(); ++i) {
            ItemStack encodedInput = (ItemStack)encodedInputs.get(i);
            InventoryActionPacket message = new InventoryActionPacket(InventoryAction.SET_FILTER, menu.getCraftingGridSlots()[i].index, encodedInput);
            PacketDistributor.sendToServer((CustomPacketPayload)message, (CustomPacketPayload[])new CustomPacketPayload[0]);
        }
        for (FakeSlot outputSlot : menu.getProcessingOutputSlots()) {
            InventoryActionPacket message = new InventoryActionPacket(InventoryAction.SET_FILTER, outputSlot.index, ItemStack.EMPTY);
            PacketDistributor.sendToServer((CustomPacketPayload)message, (CustomPacketPayload[])new CustomPacketPayload[0]);
        }
    }

    private static GenericStack findBestIngredient(Map<AEKey, Integer> ingredientPriorities, List<GenericStack> possibleIngredients) {
        return possibleIngredients.stream().map(gi -> Pair.of((Object)gi, (Object)ingredientPriorities.getOrDefault(gi.what(), Integer.MIN_VALUE))).max(Comparator.comparingInt(Pair::getRight)).map(Pair::getLeft).orElseThrow();
    }

    private static void addOrMerge(List<GenericStack> stacks, GenericStack newStack) {
        for (int i = 0; i < stacks.size(); ++i) {
            GenericStack existingStack = stacks.get(i);
            if (!Objects.equals(existingStack.what(), newStack.what())) continue;
            long newAmount = LongMath.saturatedAdd((long)existingStack.amount(), (long)newStack.amount());
            stacks.set(i, new GenericStack(newStack.what(), newAmount));
            long overflow = newStack.amount() - (newAmount - existingStack.amount());
            if (overflow > 0L) {
                stacks.add(new GenericStack(newStack.what(), overflow));
            }
            return;
        }
        stacks.add(newStack);
    }

    public static Map<AEKey, Integer> getIngredientPriorities(MEStorageMenu menu, Comparator<GridInventoryEntry> comparator) {
        List<AEKey> orderedEntries = menu.getClientRepo().getAllEntries().stream().sorted(comparator).map(GridInventoryEntry::getWhat).toList();
        HashMap<AEKey, Integer> result = new HashMap<AEKey, Integer>(orderedEntries.size());
        for (int i = 0; i < orderedEntries.size(); ++i) {
            result.put(orderedEntries.get(i), i);
        }
        for (ItemStack item : menu.getPlayerInventory().items) {
            AEItemKey key = AEItemKey.of(item);
            if (key == null) continue;
            result.putIfAbsent(key, -1);
        }
        return result;
    }
}

