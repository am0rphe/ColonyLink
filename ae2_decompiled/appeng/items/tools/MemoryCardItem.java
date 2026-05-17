/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap
 *  it.unimi.dsi.fastutil.objects.Reference2IntMap$Entry
 *  it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap
 *  net.minecraft.ChatFormatting
 *  net.minecraft.Util
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Holder
 *  net.minecraft.core.component.DataComponentMap
 *  net.minecraft.core.component.DataComponentMap$Builder
 *  net.minecraft.core.component.DataComponentType
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.Item$TooltipContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.ItemStackLinkedSet
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.item.component.DyedItemColor
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelReader
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 *  org.jetbrains.annotations.Nullable
 */
package appeng.items.tools;

import appeng.api.components.ExportedUpgrades;
import appeng.api.ids.AEComponents;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.inventories.InternalInventory;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.util.IConfigurableObject;
import appeng.core.localization.GuiText;
import appeng.core.localization.InGameTooltip;
import appeng.core.localization.PlayerMessages;
import appeng.core.localization.Tooltips;
import appeng.datagen.providers.tags.ConventionTags;
import appeng.helpers.IConfigInvHost;
import appeng.helpers.IPriorityHost;
import appeng.items.AEBaseItem;
import appeng.items.contents.NetworkToolMenuHost;
import appeng.items.tools.NetworkToolItem;
import appeng.util.InteractionUtil;
import appeng.util.Platform;
import appeng.util.inv.PlayerInternalInventory;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

public class MemoryCardItem
extends AEBaseItem
implements IMemoryCard {
    private static final int DEFAULT_BASE_COLOR = 10277887;

    public MemoryCardItem(Item.Properties properties) {
        super(properties);
    }

    public static void exportGenericSettings(Object exportFrom, DataComponentMap.Builder builder) {
        if (exportFrom instanceof IUpgradeableObject) {
            IUpgradeableObject upgradeableObject = (IUpgradeableObject)exportFrom;
            builder.set(AEComponents.EXPORTED_UPGRADES, (Object)MemoryCardItem.storeUpgrades(upgradeableObject));
        }
        if (exportFrom instanceof IConfigurableObject) {
            IConfigurableObject configurableObject = (IConfigurableObject)exportFrom;
            builder.set(AEComponents.EXPORTED_SETTINGS, configurableObject.getConfigManager().exportSettings());
        }
        if (exportFrom instanceof IPriorityHost) {
            IPriorityHost pHost = (IPriorityHost)exportFrom;
            builder.set(AEComponents.EXPORTED_PRIORITY, (Object)pHost.getPriority());
        }
        if (exportFrom instanceof IConfigInvHost) {
            IConfigInvHost configInvHost = (IConfigInvHost)exportFrom;
            builder.set(AEComponents.EXPORTED_CONFIG_INV, configInvHost.getConfig().toList());
        }
    }

    public static Set<DataComponentType<?>> importGenericSettings(Object importTo, DataComponentMap input, @Nullable Player player) {
        HashSet imported = new HashSet();
        if (player != null && importTo instanceof IUpgradeableObject) {
            IUpgradeableObject upgradeableObject = (IUpgradeableObject)importTo;
            ExportedUpgrades desiredUpgrades = (ExportedUpgrades)input.get(AEComponents.EXPORTED_UPGRADES);
            if (desiredUpgrades != null) {
                MemoryCardItem.restoreUpgrades(player, desiredUpgrades, upgradeableObject);
                imported.add(AEComponents.EXPORTED_UPGRADES);
            }
        }
        if (importTo instanceof IConfigurableObject) {
            IConfigurableObject configurableObject = (IConfigurableObject)importTo;
            Map exportedSettings = (Map)input.get(AEComponents.EXPORTED_SETTINGS);
            if (exportedSettings != null && configurableObject.getConfigManager().importSettings(exportedSettings)) {
                imported.add(AEComponents.EXPORTED_SETTINGS);
            }
        }
        if (importTo instanceof IPriorityHost) {
            IPriorityHost pHost = (IPriorityHost)importTo;
            Integer exportedPriority = (Integer)input.get(AEComponents.EXPORTED_PRIORITY);
            if (exportedPriority != null) {
                pHost.setPriority(exportedPriority);
                imported.add(AEComponents.EXPORTED_PRIORITY);
            }
        }
        if (importTo instanceof IConfigInvHost) {
            IConfigInvHost configInvHost = (IConfigInvHost)importTo;
            List exportedConfigInv = (List)input.get(AEComponents.EXPORTED_CONFIG_INV);
            if (exportedConfigInv != null) {
                configInvHost.getConfig().readFromList(exportedConfigInv);
                imported.add(AEComponents.EXPORTED_CONFIG_INV);
            }
        }
        return imported;
    }

    public static void importGenericSettingsAndNotify(Object importTo, DataComponentMap input, @Nullable Player player) {
        Set<DataComponentType<?>> imported = MemoryCardItem.importGenericSettings(importTo, input, player);
        if (player != null && !player.getCommandSenderWorld().isClientSide()) {
            if (imported.isEmpty()) {
                player.displayClientMessage((Component)PlayerMessages.InvalidMachine.text(), true);
            } else {
                Component restored = Tooltips.conjunction(imported.stream().map(MemoryCardItem::getSettingComponent).distinct().toList());
                player.displayClientMessage((Component)PlayerMessages.InvalidMachinePartiallyRestored.text(restored), true);
            }
        }
    }

    private static Set<DataComponentType<?>> getExportedSettings(DataComponentMap input) {
        HashSet result = new HashSet();
        for (DataComponentType type : input.keySet()) {
            if (!BuiltInRegistries.DATA_COMPONENT_TYPE.wrapAsHolder((Object)type).is(ConventionTags.EXPORTED_SETTINGS)) continue;
            result.add(type);
        }
        return result;
    }

    public static String getSettingTranslationKey(DataComponentType<?> settingsType) {
        ResourceLocation id = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(settingsType);
        return Util.makeDescriptionId((String)"exported_setting", (ResourceLocation)id);
    }

    private static Component getSettingComponent(DataComponentType<?> settingsType) {
        return Component.translatable((String)MemoryCardItem.getSettingTranslationKey(settingsType));
    }

    private static ExportedUpgrades storeUpgrades(IUpgradeableObject upgradeableObject) {
        Object2IntOpenCustomHashMap upgradeCount = new Object2IntOpenCustomHashMap(ItemStackLinkedSet.TYPE_AND_TAG);
        for (ItemStack upgrade : upgradeableObject.getUpgrades()) {
            upgradeCount.mergeInt((Object)upgrade, upgrade.getCount(), Integer::sum);
        }
        ArrayList<ItemStack> result = new ArrayList<ItemStack>(upgradeCount.size());
        for (Object2IntMap.Entry entry : upgradeCount.object2IntEntrySet()) {
            result.add(((ItemStack)entry.getKey()).copyWithCount(entry.getIntValue()));
        }
        return new ExportedUpgrades(result);
    }

    private static void restoreUpgrades(Player player, ExportedUpgrades desiredUpgrades, IUpgradeableObject upgradeableObject) {
        IUpgradeInventory upgrades = upgradeableObject.getUpgrades();
        if (player.getAbilities().instabuild) {
            for (int i = 0; i < upgrades.size(); ++i) {
                upgrades.setItemDirect(i, ItemStack.EMPTY);
            }
            for (ItemStack upgrade : desiredUpgrades.upgrades()) {
                upgrades.addItems(upgrade);
            }
        }
        ArrayList<InternalInventory> upgradeSources = new ArrayList<InternalInventory>();
        upgradeSources.add(new PlayerInternalInventory(player.getInventory()));
        NetworkToolMenuHost networkTool = NetworkToolItem.findNetworkToolInv(player);
        if (networkTool != null) {
            upgradeSources.add(networkTool.getInventory());
        }
        Reference2IntOpenHashMap desiredUpgradeCounts = new Reference2IntOpenHashMap(desiredUpgrades.upgrades().size());
        for (ItemStack desiredUpgrade : desiredUpgrades.upgrades()) {
            desiredUpgradeCounts.put((Object)desiredUpgrade.getItem(), desiredUpgrade.getCount());
        }
        for (int i = 0; i < upgrades.size(); ++i) {
            ItemStack current = upgrades.getStackInSlot(i);
            if (current.isEmpty()) continue;
            int desiredCount = desiredUpgradeCounts.getOrDefault((Object)current, 0);
            int totalInstalled = upgradeableObject.getInstalledUpgrades((ItemLike)current.getItem());
            int toRemove = totalInstalled - desiredCount;
            if (toRemove <= 0) continue;
            ItemStack removed = upgrades.extractItem(i, toRemove, false);
            for (InternalInventory upgradeSource : upgradeSources) {
                if (removed.isEmpty()) continue;
                removed = upgradeSource.addItems(removed);
            }
            if (removed.isEmpty()) continue;
            player.drop(removed, false);
        }
        for (Reference2IntMap.Entry entry : desiredUpgradeCounts.reference2IntEntrySet()) {
            int missingAmount = entry.getIntValue() - upgradeableObject.getInstalledUpgrades((ItemLike)entry.getKey());
            if (missingAmount <= 0) continue;
            ItemStack potential = new ItemStack((ItemLike)entry.getKey(), missingAmount);
            ItemStack overflow = upgrades.addItems(potential, true);
            if (!overflow.isEmpty()) {
                missingAmount -= overflow.getCount();
            }
            for (InternalInventory upgradeSource : upgradeSources) {
                ItemStack cards = upgradeSource.removeItems(missingAmount, potential, null);
                if (!cards.isEmpty()) {
                    overflow = upgrades.addItems(cards);
                    if (!overflow.isEmpty()) {
                        player.getInventory().placeItemBackInInventory(overflow);
                    }
                    missingAmount -= cards.getCount();
                }
                if (missingAmount > 0) continue;
                break;
            }
            if (missingAmount <= 0 || player.level().isClientSide()) continue;
            player.displayClientMessage((Component)PlayerMessages.MissingUpgrades.text(((Item)entry.getKey()).getDescription(), missingAmount), true);
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> lines, TooltipFlag advancedTooltips) {
        Component settingsSource = (Component)stack.get(AEComponents.EXPORTED_SETTINGS_SOURCE);
        if (settingsSource != null) {
            lines.add((Component)Tooltips.of(settingsSource));
        } else {
            lines.add((Component)Tooltips.of((Component)GuiText.Blank.text()));
        }
        Short p2pFreq = (Short)stack.get(AEComponents.EXPORTED_P2P_FREQUENCY);
        if (p2pFreq != null) {
            MutableComponent freqTooltip = Platform.p2p().toColoredHexString(p2pFreq).withStyle(ChatFormatting.BOLD);
            lines.add((Component)Tooltips.of((Component)Component.translatable((String)InGameTooltip.P2PFrequency.getTranslationKey(), (Object[])new Object[]{freqTooltip})));
        }
    }

    @Override
    public void notifyUser(Player player, MemoryCardMessages msg) {
        if (player.getCommandSenderWorld().isClientSide()) {
            return;
        }
        switch (msg) {
            case SETTINGS_CLEARED: {
                player.displayClientMessage((Component)PlayerMessages.SettingCleared.text(), true);
                break;
            }
            case INVALID_MACHINE: {
                player.displayClientMessage((Component)PlayerMessages.InvalidMachine.text(), true);
                break;
            }
            case SETTINGS_LOADED: {
                player.displayClientMessage((Component)PlayerMessages.LoadedSettings.text(), true);
                break;
            }
            case SETTINGS_SAVED: {
                player.displayClientMessage((Component)PlayerMessages.SavedSettings.text(), true);
                break;
            }
            case SETTINGS_RESET: {
                player.displayClientMessage((Component)PlayerMessages.ResetSettings.text(), true);
                break;
            }
        }
    }

    public InteractionResult useOn(UseOnContext context) {
        if (InteractionUtil.isInAlternateUseMode(context.getPlayer())) {
            Level level = context.getLevel();
            if (!level.isClientSide()) {
                this.clearCard(context.getPlayer(), context.getLevel(), context.getHand());
            }
            return InteractionResult.sidedSuccess((boolean)level.isClientSide());
        }
        return super.useOn(context);
    }

    public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        return true;
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (InteractionUtil.isInAlternateUseMode(player) && !level.isClientSide) {
            this.clearCard(player, level, hand);
        }
        return super.use(level, player, hand);
    }

    private void clearCard(Player player, Level level, InteractionHand hand) {
        IMemoryCard mem = (IMemoryCard)player.getItemInHand(hand).getItem();
        mem.notifyUser(player, MemoryCardMessages.SETTINGS_CLEARED);
        MemoryCardItem.clearCard(player.getItemInHand(hand));
    }

    public static void clearCard(ItemStack card) {
        for (Holder holder : BuiltInRegistries.DATA_COMPONENT_TYPE.getTagOrEmpty(ConventionTags.EXPORTED_SETTINGS)) {
            card.remove((DataComponentType)holder.value());
        }
        card.remove(AEComponents.MEMORY_CARD_COLORS);
    }

    public int getColor(ItemStack stack) {
        return DyedItemColor.getOrDefault((ItemStack)stack, (int)10277887);
    }

    public static int getTintColor(ItemStack stack, int tintIndex) {
        Item item;
        if (tintIndex == 1 && (item = stack.getItem()) instanceof MemoryCardItem) {
            MemoryCardItem memoryCard = (MemoryCardItem)item;
            return memoryCard.getColor(stack);
        }
        return 0xFFFFFF;
    }
}

