/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.component.DataComponentMap
 *  net.minecraft.core.component.DataComponentMap$Builder
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.GameType
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.material.Fluid
 *  net.minecraft.world.level.material.Fluids
 */
package appeng.server.testplots;

import appeng.api.config.FuzzyMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.SchedulingMode;
import appeng.api.config.Setting;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.blockentity.misc.InterfaceBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.items.tools.NetworkToolItem;
import appeng.parts.automation.ExportBusPart;
import appeng.parts.crafting.PatternProviderPart;
import appeng.parts.misc.InterfacePart;
import appeng.server.testplots.CraftingPatternHelper;
import appeng.server.testplots.TestPlot;
import appeng.server.testplots.TestPlotClass;
import appeng.server.testworld.PlotBuilder;
import appeng.server.testworld.PlotTestHelper;
import appeng.util.SettingsFrom;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

@TestPlotClass
public final class MemoryCardTestPlots {
    private MemoryCardTestPlots() {
    }

    @TestPlot(value="memcard_export_bus")
    public static void testExportBus(PlotBuilder plot) {
        BlockPos origin = BlockPos.ZERO;
        plot.creativeEnergyCell(origin.below());
        plot.cable(origin).part(Direction.WEST, AEParts.EXPORT_BUS).part(Direction.EAST, AEParts.EXPORT_BUS);
        plot.test(helper -> {
            ExportBusPart fromPart = helper.getPart(BlockPos.ZERO, Direction.WEST, ExportBusPart.class);
            ExportBusPart toPart = helper.getPart(BlockPos.ZERO, Direction.EAST, ExportBusPart.class);
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            InternalInventory networkToolInv = MemoryCardTestPlots.addNetworkToolToPlayer(player);
            fromPart.getUpgrades().addItems(AEItems.REDSTONE_CARD.stack());
            fromPart.getUpgrades().addItems(AEItems.FUZZY_CARD.stack());
            fromPart.getUpgrades().addItems(AEItems.CRAFTING_CARD.stack());
            fromPart.getUpgrades().addItems(AEItems.CAPACITY_CARD.stack());
            fromPart.getConfig().addFilter((ItemLike)Items.STICK);
            fromPart.getConfig().addFilter((Fluid)Fluids.WATER);
            fromPart.getConfigManager().putSetting(Settings.REDSTONE_CONTROLLED, RedstoneMode.SIGNAL_PULSE);
            fromPart.getConfigManager().putSetting(Settings.FUZZY_MODE, FuzzyMode.PERCENT_25);
            fromPart.getConfigManager().putSetting(Settings.CRAFT_ONLY, YesNo.YES);
            fromPart.getConfigManager().putSetting(Settings.SCHEDULING_MODE, SchedulingMode.RANDOM);
            MemoryCardTestPlots.copyUpgradesToNetworkInv(fromPart, networkToolInv);
            DataComponentMap.Builder settings = DataComponentMap.builder();
            fromPart.exportSettings(SettingsFrom.MEMORY_CARD, settings);
            toPart.importSettings(SettingsFrom.MEMORY_CARD, settings.build(), player);
            MemoryCardTestPlots.assertUpgradeEquals(origin, helper, fromPart, toPart);
            MemoryCardTestPlots.assertConfigEquals(origin, helper, fromPart, toPart);
            if (!toPart.getConfig().keySet().equals(Set.of(AEItemKey.of((ItemLike)Items.STICK), AEFluidKey.of((Fluid)Fluids.WATER)))) {
                helper.fail("wrong filter", origin);
            }
            helper.succeed();
        });
    }

    @TestPlot(value="memcard_interface")
    public static void testInterface(PlotBuilder plot) {
        BlockPos origin = BlockPos.ZERO;
        plot.cable(origin).part(Direction.WEST, AEParts.INTERFACE);
        plot.block(origin.east(), AEBlocks.INTERFACE);
        plot.test(helper -> {
            InterfaceBlockEntity from = (InterfaceBlockEntity)helper.getBlockEntity(BlockPos.ZERO.east());
            InterfacePart to = helper.getPart(BlockPos.ZERO, Direction.WEST, InterfacePart.class);
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            InternalInventory networkToolInv = MemoryCardTestPlots.addNetworkToolToPlayer(player);
            from.getUpgrades().addItems(AEItems.FUZZY_CARD.stack());
            to.getUpgrades().addItems(AEItems.REDSTONE_CARD.stack());
            from.getConfig().setStack(0, new GenericStack(AEItemKey.of((ItemLike)Items.STICK), 1L));
            from.getConfig().setStack(1, new GenericStack(AEFluidKey.of((Fluid)Fluids.WATER), 1L));
            from.getConfigManager().putSetting(Settings.FUZZY_MODE, FuzzyMode.PERCENT_25);
            MemoryCardTestPlots.copyUpgradesToNetworkInv(from, networkToolInv);
            DataComponentMap.Builder settings = DataComponentMap.builder();
            from.exportSettings(SettingsFrom.MEMORY_CARD, settings, null);
            to.importSettings(SettingsFrom.MEMORY_CARD, settings.build(), player);
            MemoryCardTestPlots.assertUpgradeEquals(origin, helper, from, to);
            MemoryCardTestPlots.assertConfigEquals(origin, helper, from, to);
            if (!Objects.equals(to.getConfig().getKey(0), AEItemKey.of((ItemLike)Items.STICK))) {
                helper.fail("missing stick in filter", origin);
            }
            if (!Objects.equals(to.getConfig().getKey(1), AEFluidKey.of((Fluid)Fluids.WATER))) {
                helper.fail("missing water in filter", origin);
            }
            helper.succeed();
        });
    }

    @TestPlot(value="memcard_pattern_provider")
    public static void testPatternProvider(PlotBuilder plot) {
        BlockPos origin = BlockPos.ZERO;
        plot.cable(origin).part(Direction.WEST, AEParts.PATTERN_PROVIDER);
        plot.block(origin.east(), AEBlocks.PATTERN_PROVIDER);
        plot.test(helper -> {
            ItemStack processingPattern = PatternDetailsHelper.encodeProcessingPattern(List.of(new GenericStack(AEFluidKey.of((Fluid)Fluids.WATER), 1L)), List.of(new GenericStack(AEFluidKey.of((Fluid)Fluids.LAVA), 1L)));
            ItemStack craftingPattern = CraftingPatternHelper.encodeShapelessCraftingRecipe((Level)helper.getLevel(), Items.OAK_LOG.getDefaultInstance());
            ItemStack differentCraftingPattern = CraftingPatternHelper.encodeShapelessCraftingRecipe((Level)helper.getLevel(), Items.SPRUCE_LOG.getDefaultInstance());
            PatternProviderBlockEntity from = (PatternProviderBlockEntity)helper.getBlockEntity(BlockPos.ZERO.east());
            PatternProviderPart to = helper.getPart(BlockPos.ZERO, Direction.WEST, PatternProviderPart.class);
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.getInventory().placeItemBackInInventory(AEItems.BLANK_PATTERN.stack(64));
            InternalInventory fromPatternInv = from.getLogic().getPatternInv();
            fromPatternInv.addItems(processingPattern);
            fromPatternInv.addItems(craftingPattern);
            InternalInventory toPatternInv = to.getLogic().getPatternInv();
            toPatternInv.addItems(differentCraftingPattern.copy());
            toPatternInv.addItems(differentCraftingPattern.copy());
            toPatternInv.addItems(differentCraftingPattern.copy());
            int blankPatternsBefore = player.getInventory().countItem((Item)AEItems.BLANK_PATTERN.asItem());
            DataComponentMap.Builder settings = DataComponentMap.builder();
            from.exportSettings(SettingsFrom.MEMORY_CARD, settings, null);
            to.importSettings(SettingsFrom.MEMORY_CARD, settings.build(), player);
            int blankPatternsAfter = player.getInventory().countItem((Item)AEItems.BLANK_PATTERN.asItem());
            helper.check(blankPatternsAfter == blankPatternsBefore + 1, "Expected player to be given back one blank pattern");
            for (int i = 0; i < fromPatternInv.size(); ++i) {
                ItemStack toItem;
                ItemStack fromItem = fromPatternInv.getStackInSlot(i);
                if (ItemStack.isSameItemSameComponents((ItemStack)fromItem, (ItemStack)(toItem = toPatternInv.getStackInSlot(i)))) continue;
                helper.fail("Mismatch in slot " + i, origin.east());
            }
            helper.succeed();
        });
    }

    private static InternalInventory addNetworkToolToPlayer(Player player) {
        player.addItem(AEItems.NETWORK_TOOL.stack());
        return NetworkToolItem.findNetworkToolInv(player).getInventory();
    }

    private static void assertUpgradeEquals(BlockPos origin, PlotTestHelper helper, Object fromPart, Object toPart) {
        if (fromPart instanceof IUpgradeableObject) {
            IUpgradeableObject upgradableFrom = (IUpgradeableObject)fromPart;
            IUpgradeableObject upgradeableTo = (IUpgradeableObject)toPart;
            for (ItemStack upgrade : upgradableFrom.getUpgrades()) {
                if (upgradableFrom.getInstalledUpgrades((ItemLike)upgrade.getItem()) == upgradeableTo.getInstalledUpgrades((ItemLike)upgrade.getItem())) continue;
                helper.fail(upgrade.getHoverName().getString() + " mismatch", origin);
            }
        }
    }

    private static void assertConfigEquals(BlockPos origin, PlotTestHelper helper, Object fromPart, Object toPart) {
        if (fromPart instanceof IConfigurableObject) {
            IConfigurableObject fromConfigurable = (IConfigurableObject)fromPart;
            IConfigurableObject toConfigurable = (IConfigurableObject)toPart;
            IConfigManager fromConfig = fromConfigurable.getConfigManager();
            IConfigManager toConfig = toConfigurable.getConfigManager();
            for (Setting<?> setting : fromConfig.getSettings()) {
                if (((Enum)fromConfig.getSetting(setting)).equals(toConfig.getSetting(setting))) continue;
                helper.fail("Setting " + String.valueOf(setting) + " mismatch", origin);
            }
        }
    }

    private static void copyUpgradesToNetworkInv(Object fromPart, InternalInventory networkToolInv) {
        if (fromPart instanceof IUpgradeableObject) {
            IUpgradeableObject upgradeable = (IUpgradeableObject)fromPart;
            for (ItemStack upgrade : upgradeable.getUpgrades()) {
                networkToolInv.addItems(upgrade.copy());
            }
        }
    }
}

