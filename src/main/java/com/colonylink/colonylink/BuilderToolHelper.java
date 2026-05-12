package com.colonylink.colonylink;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Logique de substitution d'outils pour ColonyLink — neutre AE2/RS2.
 *
 * v1.1.4 : Refactor pour supporter AE2 et RS2 sans dépendance directe sur
 * les types AE2 (KeyCounter, ICraftingService, AEItemKey) dans la signature
 * publique de findBestTool().
 *
 * Deux interfaces neutres remplacent les types AE2 :
 *   ToolInventoryView   → abstrait KeyCounter (AE2) et StorageNetworkComponent (RS2)
 *   ToolCraftingView    → abstrait ICraftingService (AE2) et AutocraftingNetworkComponent (RS2)
 *
 * SubstituteResult retourne maintenant un ItemStack (displayStack) et non plus
 * un AEItemKey — suffisant pour tous les callers.
 *
 * Wrappers statiques fournis pour construire ces interfaces depuis AE2 ou RS2
 * sans que les callers aient à le faire eux-mêmes.
 *
 * Tableau (niveau max enchant individuel autorisé) :
 * ─────────────────────────────────────────────────
 * Tool Tier  │ Hut 0 │ Hut 1 │ Hut 2 │ Hut 3 │ Hut 4 │ Hut 5
 * Wood/Gold  │   0   │   1   │   2   │   3   │   4   │  any
 * Stone      │   -   │   0   │   1   │   2   │   3   │  any
 * Iron       │   -   │   -   │   0   │   1   │   2   │  any
 * Diamond    │   -   │   -   │   -   │   0   │   1   │  any
 * Netherite+ │   -   │   -   │   -   │   -   │   0   │  any
 */
public class BuilderToolHelper
{
    // ── Interfaces neutres ────────────────────────────────────────────────────

    /**
     * Vue abstraite du stock d'items dans un réseau (AE2 ou RS2).
     * Remplace KeyCounter (AE2) dans la signature de findBestTool().
     */
    public interface ToolInventoryView
    {
        /** Retourne la quantité de cet item disponible dans le réseau. */
        long get(ItemStack stack);
    }

    /**
     * Vue abstraite du service de craft d'un réseau (AE2 ou RS2).
     * Remplace ICraftingService (AE2) dans la signature de findBestTool().
     */
    public interface ToolCraftingView
    {
        /** Retourne true si un pattern existe pour crafter cet item. */
        boolean isCraftable(ItemStack stack);
    }

    // ── Wrappers AE2 ─────────────────────────────────────────────────────────

    /**
     * Construit un ToolInventoryView depuis un KeyCounter AE2.
     * Appelé par ColonyLinkServerTicker et SendToBuilderHandler (AE2).
     */
    public static ToolInventoryView fromAE2Inventory(appeng.api.stacks.KeyCounter counter)
    {
        return stack -> {
            appeng.api.stacks.AEItemKey key = appeng.api.stacks.AEItemKey.of(stack);
            return key != null ? counter.get(key) : 0L;
        };
    }

    /**
     * Construit un ToolCraftingView depuis un ICraftingService AE2.
     * Appelé par ColonyLinkServerTicker et SendToBuilderHandler (AE2).
     */
    public static ToolCraftingView fromAE2CraftingService(
            appeng.api.networking.crafting.ICraftingService cs)
    {
        return stack -> {
            appeng.api.stacks.AEItemKey key = appeng.api.stacks.AEItemKey.of(stack);
            return key != null && cs.isCraftable(key);
        };
    }

    // ── Wrappers RS2 ──────────────────────────────────────────────────────────

    /**
     * Construit un ToolInventoryView depuis un StorageNetworkComponent RS2.
     * Appelé par SendToBuilderHandlerRS et ColonyLinkServerTicker (RS2).
     */
    public static ToolInventoryView fromRS2Storage(
            com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent storage)
    {
        return stack -> {
            com.refinedmods.refinedstorage.common.support.resource.ItemResource key =
                    com.refinedmods.refinedstorage.common.support.resource.ItemResource.ofItemStack(stack);
            return storage.get(key);
        };
    }

    /**
     * Construit un ToolCraftingView depuis un AutocraftingNetworkComponent RS2.
     * Appelé par SendToBuilderHandlerRS et ColonyLinkServerTicker (RS2).
     * Accepte null (RS2 sans autocrafters configurés).
     */
    public static ToolCraftingView fromRS2Crafting(
            com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent crafting)
    {
        if (crafting == null) return stack -> false;
        return stack -> {
            com.refinedmods.refinedstorage.common.support.resource.ItemResource key =
                    com.refinedmods.refinedstorage.common.support.resource.ItemResource.ofItemStack(stack);
            return crafting.getOutputs().contains(key);
        };
    }

    // ── Tiers ordonnés ───────────────────────────────────────────────────────

    private static final Tiers[] TIER_ORDER = {
            Tiers.NETHERITE, Tiers.DIAMOND, Tiers.IRON, Tiers.STONE, Tiers.WOOD
    };

    private static final Tiers[] MAX_TIER_FOR_LEVEL = {
            Tiers.WOOD,      // level 0
            Tiers.STONE,     // level 1
            Tiers.IRON,      // level 2
            Tiers.DIAMOND,   // level 3
            Tiers.NETHERITE, // level 4
            Tiers.NETHERITE  // level 5
    };

    private static final int[][] MAX_ENCHANT_LEVEL = {
            // Hut0  Hut1  Hut2  Hut3  Hut4  Hut5
            {  0,    1,    2,    3,    4,    Integer.MAX_VALUE }, // Wood/Gold
            { -1,    0,    1,    2,    3,    Integer.MAX_VALUE }, // Stone
            { -1,   -1,    0,    1,    2,    Integer.MAX_VALUE }, // Iron
            { -1,   -1,   -1,    0,    1,    Integer.MAX_VALUE }, // Diamond
            { -1,   -1,   -1,   -1,    0,    Integer.MAX_VALUE }, // Netherite
    };

    // ── Résultat ──────────────────────────────────────────────────────────────

    public enum SubstituteAction { SEND, CRAFT, NONE }

    /**
     * Résultat de la substitution.
     *
     * displayStack : l'item à envoyer ou crafter (count = 1).
     *   Côté AE2 : le caller extrait via AEItemKey.of(displayStack).
     *   Côté RS2 : le caller extrait via ItemResource.ofItemStack(displayStack).
     *   Les deux peuvent reconstruire leur clé réseau depuis un ItemStack.
     */
    public record SubstituteResult(
            SubstituteAction action,
            ItemStack displayStack
    )
    {
        public static final SubstituteResult NONE =
                new SubstituteResult(SubstituteAction.NONE, ItemStack.EMPTY);
    }

    // ── API principale ────────────────────────────────────────────────────────

    /**
     * Tente de trouver un substitut pour l'outil demandé.
     * Neutre AE2/RS2 — utilise les interfaces ToolInventoryView et ToolCraftingView.
     *
     * @param requested     item demandé par le builder
     * @param buildingLevel niveau du Work Hut (0-5)
     * @param inventory     vue du stock réseau (AE2 ou RS2)
     * @param crafting      vue du craft réseau (AE2 ou RS2)
     */
    public static SubstituteResult findBestTool(
            ItemStack requested,
            int buildingLevel,
            ToolInventoryView inventory,
            ToolCraftingView crafting)
    {
        if (!isTool(requested)) return SubstituteResult.NONE;

        int level = Math.max(0, Math.min(5, buildingLevel));
        Tiers maxTier = MAX_TIER_FOR_LEVEL[level];
        int maxTierIndex = getTierIndex(maxTier);

        // Passe 1 : meilleur tier en stock
        ItemStack bestInStock = null;
        int bestInStockTierIndex = -1;

        for (Tiers tier : TIER_ORDER)
        {
            int tierIndex = getTierIndex(tier);
            if (tierIndex < 0 || tierIndex > maxTierIndex) continue;
            int maxEnchant = MAX_ENCHANT_LEVEL[tierIndex][level];
            if (maxEnchant < 0) continue;

            ItemStack found = findInInventory(requested, tier, maxEnchant, inventory);
            if (found != null)
            {
                bestInStock = found;
                bestInStockTierIndex = tierIndex;
                break;
            }
        }

        // Passe 2 : meilleur tier craftable
        ItemStack bestCraftable = null;
        int bestCraftableTierIndex = -1;

        for (Tiers tier : TIER_ORDER)
        {
            int tierIndex = getTierIndex(tier);
            if (tierIndex < 0 || tierIndex > maxTierIndex) continue;
            if (MAX_ENCHANT_LEVEL[tierIndex][level] < 0) continue;

            List<Item> candidates = getCandidateItems(requested, tier);
            for (Item candidate : candidates)
            {
                ItemStack candidateStack = new ItemStack(candidate);
                if (crafting.isCraftable(candidateStack))
                {
                    bestCraftable = candidateStack;
                    bestCraftableTierIndex = tierIndex;
                    break;
                }
            }
            if (bestCraftable != null) break;
        }

        // Décision
        if (bestInStock != null && bestCraftable != null)
        {
            if (bestCraftableTierIndex > bestInStockTierIndex)
                return new SubstituteResult(SubstituteAction.CRAFT, bestCraftable);
            else
                return new SubstituteResult(SubstituteAction.SEND, bestInStock);
        }
        else if (bestInStock != null)
            return new SubstituteResult(SubstituteAction.SEND, bestInStock);
        else if (bestCraftable != null)
            return new SubstituteResult(SubstituteAction.CRAFT, bestCraftable);

        return SubstituteResult.NONE;
    }

    // ── Surcharges de compatibilité AE2 (non-breaking pour le code existant) ──

    /**
     * Surcharge AE2 legacy — convertit KeyCounter + ICraftingService en interfaces neutres.
     * Permet au code AE2 existant (ServerTicker, SendToBuilderHandler) de continuer
     * à appeler findBestTool sans modification.
     *
     * @deprecated Préférer la version neutre avec ToolInventoryView/ToolCraftingView.
     */
    public static SubstituteResult findBestTool(
            ItemStack requested,
            int buildingLevel,
            appeng.api.stacks.KeyCounter inventory,
            appeng.api.networking.crafting.ICraftingService craftingService)
    {
        return findBestTool(
                requested,
                buildingLevel,
                fromAE2Inventory(inventory),
                fromAE2CraftingService(craftingService));
    }

    // ── Recherche dans l'inventaire ───────────────────────────────────────────

    /**
     * Cherche dans l'inventaire réseau un item du type demandé,
     * du tier donné, avec enchants valides.
     * Retourne le premier ItemStack trouvé (count=1), ou null.
     */
    private static ItemStack findInInventory(ItemStack requested, Tiers tier,
                                             int maxEnchantLevel, ToolInventoryView inventory)
    {
        List<Item> candidates = getCandidateItems(requested, tier);
        if (candidates.isEmpty()) return null;

        for (Item candidate : candidates)
        {
            ItemStack probe = new ItemStack(candidate);
            long qty = inventory.get(probe);
            if (qty <= 0) continue;
            if (!isEnchantValid(probe, maxEnchantLevel)) continue;
            return probe;
        }

        // Fallback : items avec enchantements — on cherche sans filtrer les enchants
        // (le réseau peut contenir des versions enchantées)
        // On délègue au caller de vérifier le niveau exact via isEnchantValid()
        // Note : pour les items moddés avec enchants stockés différemment,
        // le probe sans enchant ne correspond pas — acceptable pour la v1.1.4.
        return null;
    }

    // ── Validation enchants ───────────────────────────────────────────────────

    public static boolean isEnchantValid(ItemStack stack, int maxLevel)
    {
        if (!ColonyLinkConfig.RESPECT_ENCHANT_LEVEL_CAP.get()) return true;
        var enchants = EnchantmentHelper.getEnchantmentsForCrafting(stack);
        if (enchants.isEmpty()) return true;
        if (maxLevel <= 0) return false;
        for (var entry : enchants.entrySet())
            if (entry.getValue() > maxLevel) return false;
        return true;
    }

    public static int getMaxEnchantLevel(Tiers tier, int buildingLevel)
    {
        int level = Math.max(0, Math.min(5, buildingLevel));
        int tierIndex = getTierIndex(tier);
        if (tierIndex < 0) return -1;
        return MAX_ENCHANT_LEVEL[tierIndex][level];
    }

    // ── Détection d'outil ─────────────────────────────────────────────────────

    public static boolean isTool(ItemStack stack)
    {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        return item instanceof PickaxeItem
                || item instanceof AxeItem
                || item instanceof ShovelItem
                || item instanceof HoeItem
                || item instanceof SwordItem
                || item instanceof TieredItem;
    }

    // ── Correspondance tier ───────────────────────────────────────────────────

    private static int getTierIndex(Tiers tier)
    {
        return switch (tier) {
            case WOOD      -> 0;
            case GOLD      -> 0;
            case STONE     -> 1;
            case IRON      -> 2;
            case DIAMOND   -> 3;
            case NETHERITE -> 4;
        };
    }

    private static Item getEquivalentItem(ItemStack requested, Tiers tier)
    {
        Item item = requested.getItem();
        String typeId;
        if      (item instanceof PickaxeItem) typeId = "pickaxe";
        else if (item instanceof AxeItem)     typeId = "axe";
        else if (item instanceof ShovelItem)  typeId = "shovel";
        else if (item instanceof HoeItem)     typeId = "hoe";
        else if (item instanceof SwordItem)   typeId = "sword";
        else return null;

        String materialName = switch (tier) {
            case WOOD      -> "wooden";
            case GOLD      -> "golden";
            case STONE     -> "stone";
            case IRON      -> "iron";
            case DIAMOND   -> "diamond";
            case NETHERITE -> "netherite";
        };

        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                "minecraft", materialName + "_" + typeId);
        if (!BuiltInRegistries.ITEM.containsKey(id)) return null;
        Item result = BuiltInRegistries.ITEM.get(id);
        if (result == net.minecraft.world.item.Items.AIR) return null;
        return result;
    }

    // ── Cache candidats ───────────────────────────────────────────────────────

    private static final java.util.concurrent.ConcurrentHashMap<String, List<Item>> CANDIDATE_CACHE
            = new java.util.concurrent.ConcurrentHashMap<>();

    private static String cacheKey(Class<?> toolClass, Tiers tier)
    { return toolClass.getSimpleName() + "_" + tier.name(); }

    private static List<Item> getCandidateItems(ItemStack requested, Tiers tier)
    {
        Item requestedItem = requested.getItem();
        final Class<?> toolClass;
        if      (requestedItem instanceof PickaxeItem) toolClass = PickaxeItem.class;
        else if (requestedItem instanceof AxeItem)     toolClass = AxeItem.class;
        else if (requestedItem instanceof ShovelItem)  toolClass = ShovelItem.class;
        else if (requestedItem instanceof HoeItem)     toolClass = HoeItem.class;
        else if (requestedItem instanceof SwordItem)   toolClass = SwordItem.class;
        else toolClass = requestedItem.getClass();

        final Item vanillaItem = getEquivalentItem(requested, tier);
        String key = cacheKey(toolClass, tier);

        return CANDIDATE_CACHE.computeIfAbsent(key, k -> {
            List<Item> candidates = new ArrayList<>();
            if (vanillaItem != null) candidates.add(vanillaItem);
            for (Item item : BuiltInRegistries.ITEM)
            {
                if (candidates.contains(item)) continue;
                if (!toolClass.isInstance(item)) continue;
                if (!(item instanceof TieredItem tiered)) continue;
                if (!(tiered.getTier() instanceof Tiers t) || t != tier) continue;
                candidates.add(item);
            }
            return java.util.Collections.unmodifiableList(candidates);
        });
    }
}