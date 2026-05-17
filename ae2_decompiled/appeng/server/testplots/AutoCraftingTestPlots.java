/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.BlockStateProperties
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.material.Fluid
 *  net.minecraft.world.level.material.Fluids
 */
package appeng.server.testplots;

import appeng.api.config.Actionable;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.blockentity.misc.InscriberBlockEntity;
import appeng.blockentity.storage.SkyChestBlockEntity;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.items.storage.CreativeCellItem;
import appeng.me.helpers.BaseActionSource;
import appeng.server.testplots.CraftingPatternHelper;
import appeng.server.testplots.TestPlot;
import appeng.server.testplots.TestPlotClass;
import appeng.server.testworld.DriveBuilder;
import appeng.server.testworld.PlotBuilder;
import appeng.server.testworld.PlotTestHelper;
import appeng.server.testworld.SpawnExtraGridTestToolsChest;
import appeng.server.testworld.TestCraftingJob;
import appeng.util.inv.AppEngInternalInventory;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

@TestPlotClass
public final class AutoCraftingTestPlots {
    private AutoCraftingTestPlots() {
    }

    @TestPlot(value="autocrafting_testplot")
    public static void create(PlotBuilder plot) {
        plot.creativeEnergyCell("4 -1 4");
        plot.cable("4 0 [1,5]");
        plot.cable("[3,6] 0 1");
        plot.block("[4,5] [0,1] [4,5]", AEBlocks.CONTROLLER);
        AutoCraftingTestPlots.craftingCube(plot.offset(1, 0, 1));
        plot.cable("[6,8] 0 5");
        PlotBuilder assemblerStack = plot.offset(8, 1, 5);
        for (int i = 0; i < 8; ++i) {
            AutoCraftingTestPlots.assemblerFlower(assemblerStack.offset(0, i * 3, 0));
        }
        plot.blockEntity("7 0 1", AEBlocks.DRIVE, drive -> {
            drive.getInternalInventory().addItems(AEItems.ITEM_CELL_64K.stack());
            drive.getInternalInventory().addItems(AEItems.ITEM_CELL_64K.stack());
            drive.getInternalInventory().addItems(AEItems.FLUID_CELL_64K.stack());
            drive.getInternalInventory().addItems(AEItems.FLUID_CELL_64K.stack());
            drive.getInternalInventory().addItems(CreativeCellItem.ofItems(new ItemLike[]{Items.REDSTONE}));
            drive.getInternalInventory().addItems(CreativeCellItem.ofFluids(new Fluid[]{Fluids.LAVA}));
        });
        plot.block("7 -1 0", AEBlocks.CELL_WORKBENCH);
        plot.part("6 0 1", Direction.NORTH, AEParts.PATTERN_ENCODING_TERMINAL, term -> {
            InternalInventory inv = term.getLogic().getBlankPatternInv();
            inv.addItems(AEItems.BLANK_PATTERN.stack(64));
        });
        plot.part("5 0 1", Direction.NORTH, AEParts.PATTERN_ACCESS_TERMINAL);
        plot.part("4 0 1", Direction.NORTH, AEParts.TERMINAL);
        plot.part("3 0 1", Direction.NORTH, AEParts.CRAFTING_TERMINAL);
        plot.blockState("6 1 1", (BlockState)AEBlocks.WIRELESS_ACCESS_POINT.block().defaultBlockState().setValue((Property)BlockStateProperties.FACING, (Comparable)Direction.UP));
        plot.addBuildAction(new SpawnExtraGridTestToolsChest(new BlockPos(8, 0, 1), new BlockPos(7, 0, 1), AppEng.makeId("autocrafting_testplot")));
        AutoCraftingTestPlots.buildObsidianCrafting(plot.offset(3, 0, 5));
        AutoCraftingTestPlots.buildChestCraftingExport(plot.offset(5, 0, 7));
        AutoCraftingTestPlots.buildWaterEmittingSource(plot.offset(5, 0, 9));
        plot.cable("4 0 [6,9]", AEParts.SMART_DENSE_CABLE);
        plot.afterGridInitAt(new BlockPos(4, 0, 4), (grid, gridNode) -> {
            ServerLevel level = gridNode.getLevel();
            ArrayList<ItemStack> patterns = new ArrayList<ItemStack>();
            patterns.add(CraftingPatternHelper.encodeCraftingPattern(level, new Object[]{Items.OAK_PLANKS, Items.OAK_PLANKS, null, Items.OAK_PLANKS, Items.CRIMSON_PLANKS, null, null, null, null}, true, false));
            patterns.add(CraftingPatternHelper.encodeCraftingPattern(level, new Object[]{Items.OAK_PLANKS, Items.OAK_PLANKS, Items.OAK_PLANKS, Items.OAK_PLANKS, null, Items.OAK_PLANKS, Items.CRIMSON_PLANKS, Items.OAK_PLANKS, Items.OAK_PLANKS}, true, false));
            patterns.add(CraftingPatternHelper.encodeStoneCutterPattern((Level)level, (ItemLike)Items.STONE, (ItemLike)Items.STONE_BRICKS, false));
            patterns.add(PatternDetailsHelper.encodeProcessingPattern(List.of(new GenericStack(AEFluidKey.of((Fluid)Fluids.WATER), 1000L), GenericStack.fromItemStack(new ItemStack((ItemLike)Items.REDSTONE))), List.of(new GenericStack(AEFluidKey.of((Fluid)Fluids.WATER), 1000L))));
            patterns.add(CraftingPatternHelper.encodeSmithingPattern((Level)level, (ItemLike)Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, (ItemLike)Items.DIAMOND_PICKAXE, (ItemLike)Items.NETHERITE_INGOT, true));
            MEStorage networkInv = grid.getStorageService().getInventory();
            networkInv.insert(AEItemKey.of((ItemLike)Items.OAK_PLANKS), 83L, Actionable.MODULATE, new BaseActionSource());
            networkInv.insert(AEItemKey.of((ItemLike)Items.STONE), 192L, Actionable.MODULATE, new BaseActionSource());
            networkInv.insert(AEItemKey.of((ItemLike)Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE), 64L, Actionable.MODULATE, new BaseActionSource());
            networkInv.insert(AEItemKey.of((ItemLike)Items.DIAMOND_PICKAXE), 64L, Actionable.MODULATE, new BaseActionSource());
            networkInv.insert(AEItemKey.of((ItemLike)Items.NETHERITE_INGOT), 64L, Actionable.MODULATE, new BaseActionSource());
            block0: for (PatternProviderBlockEntity provider : grid.getMachines(PatternProviderBlockEntity.class)) {
                while (!patterns.isEmpty()) {
                    ItemStack pattern = (ItemStack)patterns.get(0);
                    if (!provider.getLogic().getPatternInv().addItems(pattern).isEmpty()) continue block0;
                    patterns.remove(0);
                }
            }
        });
    }

    private static void buildChestCraftingExport(PlotBuilder plot) {
        plot.cable("0 0 0").part(Direction.SOUTH, AEParts.EXPORT_BUS, eb -> {
            eb.getUpgrades().addItems(AEItems.CRAFTING_CARD.stack());
            eb.getConfig().insert(0, AEItemKey.of((ItemLike)Items.CHEST), 1L, Actionable.MODULATE);
        });
        plot.block("0 0 1", Blocks.CHEST);
    }

    private static void buildWaterEmittingSource(PlotBuilder plot) {
        plot.cable("0 0 0").part(Direction.SOUTH, AEParts.QUARTZ_FIBER);
        plot.cable("0 0 1").part(Direction.NORTH, AEParts.TOGGLE_BUS);
        plot.cable("0 1 0");
        plot.cable("0 1 1").craftingEmitter(Direction.DOWN, (Fluid)Fluids.WATER).part(Direction.SOUTH, AEParts.INTERFACE);
        plot.cable("0 1 2").part(Direction.NORTH, AEParts.STORAGE_BUS, storageBus -> storageBus.getConfig().insert(0, AEFluidKey.of((Fluid)Fluids.WATER), 1L, Actionable.MODULATE));
        plot.cable("0 0 2").part(Direction.DOWN, AEParts.ANNIHILATION_PLANE);
        plot.block("[-1,1] -1 2", Blocks.WATER);
    }

    private static void buildObsidianCrafting(PlotBuilder plot) {
        plot.blockEntity("0 0 0", AEBlocks.PATTERN_PROVIDER, provider -> {
            ItemStack pattern = PatternDetailsHelper.encodeProcessingPattern(List.of(new GenericStack(AEFluidKey.of((Fluid)Fluids.LAVA), 1000L)), List.of(new GenericStack(AEItemKey.of((ItemLike)Items.OBSIDIAN), 1L)));
            provider.getLogic().getPatternInv().addItems(pattern);
        });
        plot.cable("-1 0 0").part(Direction.EAST, AEParts.INTERFACE).part(Direction.WEST, AEParts.FORMATION_PLANE, plane -> plane.getConfig().insert(0, AEFluidKey.of((Fluid)Fluids.LAVA), 1L, Actionable.MODULATE));
        plot.cable("-2 1 0").part(Direction.DOWN, AEParts.ANNIHILATION_PLANE);
        plot.cable("-1 1 0").part(Direction.DOWN, AEParts.QUARTZ_FIBER);
        plot.cable("0 1 0").part(Direction.DOWN, AEParts.STORAGE_BUS, part -> part.getConfig().insert(0, AEItemKey.of((ItemLike)Items.OBSIDIAN), 1L, Actionable.MODULATE)).part(Direction.EAST, AEParts.QUARTZ_FIBER);
        plot.block("-3 0 [-2,0]", Blocks.COBBLESTONE);
        plot.block("-1 0 [-2,-1]", Blocks.COBBLESTONE);
        plot.block("-2 0 1", Blocks.COBBLESTONE);
        plot.block("-2 0 -2", Blocks.WATER);
    }

    private static void craftingCube(PlotBuilder plot) {
        plot.block("[-1,1] [0,2] [-1,1]", AEBlocks.CRAFTING_STORAGE_64K);
        plot.block("-1 2 -1", AEBlocks.CRAFTING_STORAGE_16K);
        plot.block("1 2 -1", AEBlocks.CRAFTING_STORAGE_4K);
        plot.block("-1 2 1", AEBlocks.CRAFTING_STORAGE_1K);
        plot.block("[-1,1] 0 [-1,1]", AEBlocks.CRAFTING_ACCELERATOR);
        plot.block("0 1 -1", AEBlocks.CRAFTING_MONITOR);
    }

    private static void assemblerFlower(PlotBuilder plot) {
        plot.block("0 0 0", AEBlocks.PATTERN_PROVIDER);
        plot.block("-1 0 0", AEBlocks.MOLECULAR_ASSEMBLER);
        plot.block("1 0 0", AEBlocks.MOLECULAR_ASSEMBLER);
        plot.block("0 0 1", AEBlocks.MOLECULAR_ASSEMBLER);
        plot.block("0 0 -1", AEBlocks.MOLECULAR_ASSEMBLER);
        plot.block("0 -1 0", AEBlocks.MOLECULAR_ASSEMBLER);
        plot.block("0 1 0", AEBlocks.MOLECULAR_ASSEMBLER);
    }

    @TestPlot(value="pattern_provider_faces_round_robin")
    public static void patternProviderFacesRoundRobin(PlotBuilder plot) {
        BlockPos[] inscriberPos = new BlockPos[]{new BlockPos(-1, 0, -3), new BlockPos(1, 0, -3)};
        AutoCraftingTestPlots.craftingCube(plot);
        plot.cable("0 0 -2");
        plot.blockEntity("0 0 -3", AEBlocks.PATTERN_PROVIDER, provider -> {
            ItemStack pattern = PatternDetailsHelper.encodeProcessingPattern(List.of(GenericStack.fromItemStack(AEItems.CERTUS_QUARTZ_CRYSTAL.stack())), List.of(GenericStack.fromItemStack(AEItems.CERTUS_QUARTZ_DUST.stack())));
            provider.getLogic().getPatternInv().addItems(pattern);
        });
        for (BlockPos pos : inscriberPos) {
            plot.blockEntity(pos, AEBlocks.INSCRIBER, inscriber -> inscriber.getConfigManager().putSetting(Settings.AUTO_EXPORT, YesNo.YES));
        }
        plot.cable("0 0 -4");
        DriveBuilder db = plot.drive(new BlockPos(0, 0, -5));
        db.addCreativeCell().add(AEItems.CERTUS_QUARTZ_CRYSTAL);
        db.addItemCell64k();
        plot.cable("0 1 -5").part(Direction.NORTH, AEParts.CRAFTING_TERMINAL);
        plot.creativeEnergyCell("0 -1 -5");
        plot.test(helper -> {
            TestCraftingJob craftingJob = new TestCraftingJob((PlotTestHelper)((Object)helper), BlockPos.ZERO, AEItemKey.of(AEItems.CERTUS_QUARTZ_DUST), 10L);
            helper.startSequence().thenWaitUntil(craftingJob::tickUntilStarted).thenIdle(1).thenExecute(() -> {
                for (BlockPos pos : inscriberPos) {
                    InscriberBlockEntity inscriber = (InscriberBlockEntity)helper.getBlockEntity(pos);
                    helper.check(inscriber.getInternalInventory().getStackInSlot(2).getCount() == 5, "Inscriber should have 5 = 10/2 items", pos);
                }
            }).thenSucceed();
        });
    }

    @TestPlot(value="processing_pattern_inputs_unstacking")
    public static void processingPatternInputsUnstacking(PlotBuilder plot) {
        BlockPos chestPos = new BlockPos(1, 0, -3);
        AutoCraftingTestPlots.craftingCube(plot);
        plot.cable("0 0 -2");
        plot.blockEntity("0 0 -3", AEBlocks.PATTERN_PROVIDER, provider -> {
            ItemStack pattern = PatternDetailsHelper.encodeProcessingPattern(List.of(GenericStack.fromItemStack(new ItemStack((ItemLike)Items.STONE)), GenericStack.fromItemStack(new ItemStack((ItemLike)Items.STONE)), GenericStack.fromItemStack(new ItemStack((ItemLike)Items.DIAMOND)), GenericStack.fromItemStack(new ItemStack((ItemLike)Items.STONE))), List.of(GenericStack.fromItemStack(AEItems.CERTUS_QUARTZ_DUST.stack())));
            provider.getLogic().getPatternInv().addItems(pattern);
        });
        plot.blockEntity(chestPos, AEBlocks.SKY_STONE_CHEST, skyChest -> {
            AppEngInternalInventory inv = (AppEngInternalInventory)skyChest.getInternalInventory();
            for (int i = 0; i < inv.size(); ++i) {
                inv.setMaxStackSize(i, 1);
            }
        });
        plot.cable("0 0 -4");
        DriveBuilder db = plot.drive(new BlockPos(0, 0, -5));
        db.addCreativeCell().add((ItemLike)Items.STONE).add((ItemLike)Items.DIAMOND);
        db.addItemCell64k();
        plot.cable("0 1 -5").part(Direction.NORTH, AEParts.CRAFTING_TERMINAL);
        plot.creativeEnergyCell("0 -1 -5");
        plot.test(helper -> {
            TestCraftingJob craftingJob = new TestCraftingJob((PlotTestHelper)((Object)helper), BlockPos.ZERO, AEItemKey.of(AEItems.CERTUS_QUARTZ_DUST), 1L);
            helper.startSequence().thenWaitUntil(craftingJob::tickUntilStarted).thenIdle(1).thenExecute(() -> {
                SkyChestBlockEntity chest = (SkyChestBlockEntity)helper.getBlockEntity(chestPos);
                InternalInventory inv = chest.getInternalInventory();
                for (int i = 0; i < 4; ++i) {
                    helper.check(inv.getStackInSlot(i).getCount() == 1, "Chest should have 1 item in slot " + i, chestPos);
                }
                helper.check(inv.getStackInSlot(0).is(Items.STONE), "Chest should have stone in slot 0", chestPos);
                helper.check(inv.getStackInSlot(1).is(Items.STONE), "Chest should have stone in slot 1", chestPos);
                helper.check(inv.getStackInSlot(2).is(Items.DIAMOND), "Chest should have diamond in slot 2", chestPos);
                helper.check(inv.getStackInSlot(3).is(Items.STONE), "Chest should have stone in slot 3", chestPos);
            }).thenSucceed();
        });
    }

    @TestPlot(value="regression_7288")
    public static void testCraftingCpuDupe(PlotBuilder plot) {
        AutoCraftingTestPlots.craftingCube(plot);
        plot.cable("0 0 -2");
        plot.blockEntity("0 0 -3", AEBlocks.PATTERN_PROVIDER, provider -> {
            ItemStack pattern = PatternDetailsHelper.encodeProcessingPattern(List.of(GenericStack.fromItemStack(new ItemStack((ItemLike)Items.DIAMOND))), List.of(GenericStack.fromItemStack(new ItemStack((ItemLike)Items.STICK))));
            provider.getLogic().getPatternInv().addItems(pattern);
        });
        plot.cable("0 0 -4");
        DriveBuilder db = plot.drive(new BlockPos(0, 0, -5));
        db.addItemCell64k().add((ItemLike)Items.DIAMOND, 1L);
        plot.cable("0 1 -5").part(Direction.NORTH, AEParts.CRAFTING_TERMINAL);
        plot.creativeEnergyCell("0 -1 -5");
        plot.test(helper -> {
            TestCraftingJob craftingJob = new TestCraftingJob((PlotTestHelper)((Object)helper), BlockPos.ZERO, AEItemKey.of((ItemLike)Items.STICK), 1L);
            helper.startSequence().thenWaitUntil(craftingJob::tickUntilStarted).thenIdle(1).thenExecute(() -> {
                helper.destroyBlock(new BlockPos(0, 0, -2));
                helper.destroyBlock(new BlockPos(0, 0, -1));
                helper.assertItemEntityCountIs(Items.DIAMOND, new BlockPos(0, 0, 0), 3.0, 1);
            }).thenSucceed();
        });
    }
}

