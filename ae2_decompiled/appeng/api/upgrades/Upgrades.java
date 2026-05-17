/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.block.Block
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.upgrades;

import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableItem;
import appeng.core.localization.GuiText;
import appeng.core.localization.Tooltips;
import appeng.items.materials.EnergyCardItem;
import appeng.items.materials.UpgradeCardItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public final class Upgrades {
    private static final Map<Item, List<Association>> ASSOCIATIONS = new IdentityHashMap<Item, List<Association>>();
    private static final Map<IUpgradeableItem, Set<Item>> SUPPORTED_ITEM_UPGRADES = new IdentityHashMap<IUpgradeableItem, Set<Item>>();
    private static final Map<Item, List<Component>> UPGRADE_CARD_TOOLTIP_LINES = new IdentityHashMap<Item, List<Component>>();

    private Upgrades() {
    }

    public static synchronized void add(ItemLike upgradeCard, ItemLike upgradableObject, int maxSupported) {
        Upgrades.add(upgradeCard, upgradableObject, maxSupported, null);
    }

    public static synchronized void add(ItemLike upgradeCard, ItemLike upgradableObject, int maxSupported, @Nullable String tooltipGroup) {
        Block block;
        Item item = upgradableObject.asItem();
        if (item instanceof BlockItem) {
            BlockItem blockItem = (BlockItem)item;
            block = blockItem.getBlock();
        } else {
            block = null;
        }
        Item upgrade = upgradeCard.asItem();
        if (item instanceof IUpgradeableItem) {
            IUpgradeableItem upgradeableItem = (IUpgradeableItem)item;
            Set<Item> upgrades = SUPPORTED_ITEM_UPGRADES.get(upgradeableItem);
            if (upgrades == null) {
                SUPPORTED_ITEM_UPGRADES.put(upgradeableItem, Set.of(upgrade));
            } else {
                HashSet<Item> newSet = new HashSet<Item>(upgrades);
                newSet.add(upgrade);
                SUPPORTED_ITEM_UPGRADES.put(upgradeableItem, Set.copyOf(newSet));
            }
        }
        MutableComponent translatedTooltipGroup = tooltipGroup != null ? Component.translatable((String)tooltipGroup) : null;
        Association association = new Association(upgrade, item, block, maxSupported, (Component)translatedTooltipGroup);
        ASSOCIATIONS.computeIfAbsent(association.upgradeCard(), ignored -> new ArrayList()).add(association);
        UPGRADE_CARD_TOOLTIP_LINES.remove(association.upgradeCard());
    }

    public static synchronized int getMaxInstallable(ItemLike card, ItemLike upgradableItem) {
        List<Association> associations = ASSOCIATIONS.get(card.asItem());
        if (associations == null) {
            return 0;
        }
        for (Association association : associations) {
            if (association.upgradableItem() != upgradableItem.asItem()) continue;
            return association.maxCount();
        }
        return 0;
    }

    public static synchronized Map<IUpgradeableItem, Set<Item>> getUpgradableItems() {
        return Map.copyOf(SUPPORTED_ITEM_UPGRADES);
    }

    public static int getEnergyCardMultiplier(IUpgradeInventory upgrades) {
        int multiplier = 0;
        for (ItemStack card : upgrades) {
            Item item = card.getItem();
            if (!(item instanceof EnergyCardItem)) continue;
            EnergyCardItem ec = (EnergyCardItem)item;
            multiplier += ec.getEnergyMultiplier();
        }
        return multiplier;
    }

    public static Item createUpgradeCardItem(Item.Properties p) {
        return new UpgradeCardItem(p);
    }

    public static boolean isUpgradeCardItem(ItemLike card) {
        return card.asItem() instanceof UpgradeCardItem;
    }

    public static boolean isUpgradeCardItem(ItemStack stack) {
        return stack.getItem() instanceof UpgradeCardItem;
    }

    public static synchronized List<Component> getTooltipLinesForCard(ItemLike card) {
        return UPGRADE_CARD_TOOLTIP_LINES.computeIfAbsent(card.asItem(), Upgrades::createTooltipLinesForCard);
    }

    public static synchronized List<Component> getTooltipLinesForMachine(ItemLike upgradableItemLike) {
        Item upgradableItem = upgradableItemLike.asItem();
        ArrayList<Component> result = new ArrayList<Component>();
        block0: for (List<Association> cardAssociations : ASSOCIATIONS.values()) {
            for (Association association : cardAssociations) {
                if (association.upgradableItem() != upgradableItem) continue;
                result.add((Component)GuiText.CompatibleUpgrade.text(association.upgradeCard().getDescription(), association.maxCount()).withStyle(ChatFormatting.GRAY));
                continue block0;
            }
        }
        return result;
    }

    private static List<Component> createTooltipLinesForCard(Item card) {
        ArrayList<Association> associations = new ArrayList<Association>(ASSOCIATIONS.getOrDefault(card, Collections.emptyList()));
        associations.sort(Comparator.comparingInt(o -> o.maxCount));
        ArrayList<Component> supportedTooltipLines = new ArrayList<Component>(associations.size());
        HashSet<Component> namesAdded = new HashSet<Component>();
        for (int i = 0; i < associations.size(); ++i) {
            MutableComponent base;
            Association association = (Association)associations.get(i);
            Component name = association.upgradableItem().getDescription();
            if (association.tooltipGroup() != null && namesAdded.contains(association.tooltipGroup())) continue;
            if (association.tooltipGroup() != null) {
                for (int j = i + 1; j < associations.size(); ++j) {
                    Component otherGroup = ((Association)associations.get(j)).tooltipGroup();
                    if (!association.tooltipGroup().equals((Object)otherGroup)) continue;
                    name = association.tooltipGroup();
                    break;
                }
            }
            if (!namesAdded.add(name)) continue;
            MutableComponent main = base = name.copy().withStyle(Tooltips.NORMAL_TOOLTIP_TEXT);
            if (association.maxCount() > 1) {
                main = Tooltips.of(new Component[]{base, Tooltips.of(" ("), Tooltips.ofUnformattedNumber(association.maxCount()), Tooltips.of(")")});
            }
            supportedTooltipLines.add((Component)main);
        }
        return supportedTooltipLines;
    }

    private record Association(Item upgradeCard, Item upgradableItem, @Nullable Block upgradableBlock, int maxCount, @Nullable Component tooltipGroup) {
    }
}

