/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  com.google.common.collect.UnmodifiableIterator
 *  net.minecraft.Util
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Holder
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.NonNullList
 *  net.minecraft.core.Registry
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.crafting.CraftingInput
 *  net.minecraft.world.item.crafting.CraftingRecipe
 *  net.minecraft.world.item.crafting.RecipeHolder
 *  net.minecraft.world.item.crafting.RecipeInput
 *  net.minecraft.world.item.crafting.RecipeType
 *  net.minecraft.world.item.enchantment.Enchantment
 *  net.minecraft.world.item.enchantment.Enchantments
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.ChestBlock
 *  net.minecraft.world.level.block.DispenserBlock
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.entity.ChestBlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.ChestType
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.levelgen.structure.BoundingBox
 *  net.minecraft.world.level.material.Fluid
 *  net.minecraft.world.level.material.Fluids
 *  net.neoforged.fml.ModList
 *  net.neoforged.neoforgespi.language.ModFileScanData
 *  net.neoforged.neoforgespi.language.ModFileScanData$AnnotationData
 *  org.jetbrains.annotations.Nullable
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package appeng.server.testplots;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;
import appeng.api.networking.pathing.ChannelMode;
import appeng.api.orientation.BlockOrientation;
import appeng.api.parts.PartHelper;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.StorageCell;
import appeng.api.util.AEColor;
import appeng.blockentity.crafting.MolecularAssemblerBlockEntity;
import appeng.blockentity.storage.DriveBlockEntity;
import appeng.blockentity.storage.MEChestBlockEntity;
import appeng.blockentity.storage.SkyStoneTankBlockEntity;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.items.storage.CreativeCellItem;
import appeng.items.tools.powered.MatterCannonItem;
import appeng.me.cells.BasicCellInventory;
import appeng.me.helpers.BaseActionSource;
import appeng.me.service.PathingService;
import appeng.parts.automation.ImportBusPart;
import appeng.parts.crafting.PatternProviderPart;
import appeng.server.testplots.TestPlot;
import appeng.server.testplots.TestPlotClass;
import appeng.server.testplots.TestPlotCollection;
import appeng.server.testplots.TestPlotGenerator;
import appeng.server.testworld.DriveBuilder;
import appeng.server.testworld.Plot;
import appeng.server.testworld.PlotBuilder;
import appeng.server.testworld.PlotTestHelper;
import appeng.server.testworld.TestCraftingJob;
import appeng.util.ConfigInventory;
import appeng.util.CraftingRecipeUtil;
import appeng.util.Platform;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;
import java.lang.annotation.ElementType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@TestPlotClass
public final class TestPlots {
    private static final Logger LOG = LoggerFactory.getLogger(TestPlots.class);
    @Nullable
    private static Map<ResourceLocation, Consumer<PlotBuilder>> plots;

    private TestPlots() {
    }

    private static synchronized Map<ResourceLocation, Consumer<PlotBuilder>> getPlots() {
        if (plots == null) {
            plots = TestPlots.scanForPlots();
        }
        return plots;
    }

    private static Map<ResourceLocation, Consumer<PlotBuilder>> scanForPlots() {
        HashMap<ResourceLocation, Consumer<PlotBuilder>> plots = new HashMap<ResourceLocation, Consumer<PlotBuilder>>();
        try {
            for (Class<?> clazz : TestPlots.findAllTestPlotClasses()) {
                AELog.info("Scanning %s for plots", clazz);
                for (Method method : clazz.getMethods()) {
                    TestPlot annotation = method.getAnnotation(TestPlot.class);
                    TestPlotGenerator generatorAnnotation = method.getAnnotation(TestPlotGenerator.class);
                    if (annotation == null && generatorAnnotation == null) continue;
                    if (annotation != null && generatorAnnotation != null) {
                        throw new IllegalStateException("Cannot annotate method " + String.valueOf(method) + " with both @TestPlot and @TestPlotGenerator");
                    }
                    if (!Modifier.isPublic(method.getModifiers())) {
                        throw new IllegalStateException("Method " + String.valueOf(method) + " must be public");
                    }
                    if (!Modifier.isStatic(method.getModifiers())) {
                        throw new IllegalStateException("Method " + String.valueOf(method) + " must be static");
                    }
                    if (!Void.TYPE.equals(method.getReturnType())) {
                        throw new IllegalStateException("Method " + String.valueOf(method) + " must return void");
                    }
                    if (annotation != null) {
                        if (!Arrays.asList(method.getParameterTypes()).equals(List.of(PlotBuilder.class))) {
                            throw new IllegalStateException("Method " + String.valueOf(method) + " must take a single PlotBuilder argument");
                        }
                        ResourceLocation id = AppEng.makeId(annotation.value());
                        plots.put(id, builder -> {
                            try {
                                method.invoke(null, builder);
                            }
                            catch (InvocationTargetException e) {
                                throw new RuntimeException("Failed building " + String.valueOf(id), e.getCause());
                            }
                            catch (IllegalAccessException e) {
                                throw new RuntimeException("Failed to access " + String.valueOf(method), e);
                            }
                        });
                        continue;
                    }
                    if (generatorAnnotation == null) continue;
                    if (!Arrays.asList(method.getParameterTypes()).equals(List.of(TestPlotCollection.class))) {
                        throw new IllegalStateException("Method " + String.valueOf(method) + " must take a single TestPlotCollection argument");
                    }
                    TestPlotCollection tpc = new TestPlotCollection(plots);
                    try {
                        method.invoke(null, tpc);
                    }
                    catch (InvocationTargetException e) {
                        throw new RuntimeException("Failed building " + String.valueOf(method), e.getCause());
                    }
                    catch (IllegalAccessException e) {
                        throw new RuntimeException("Failed to access " + String.valueOf(method), e);
                    }
                }
            }
        }
        catch (Exception e) {
            AELog.error("Failed to scan for plots: %s", e);
        }
        return plots;
    }

    private static List<Class<?>> findAllTestPlotClasses() {
        ArrayList result = new ArrayList();
        for (ModFileScanData data : ModList.get().getAllScanData()) {
            for (ModFileScanData.AnnotationData annotation : data.getAnnotations()) {
                if (annotation.targetType() != ElementType.TYPE || !annotation.annotationType().getClassName().equals(TestPlotClass.class.getName())) continue;
                try {
                    result.add(Class.forName(annotation.memberName()));
                }
                catch (Throwable e) {
                    LOG.error("Failed to load class {} annotated with @TestPlotClass", (Object)annotation.memberName(), (Object)e);
                }
            }
        }
        return result;
    }

    public static List<ResourceLocation> getPlotIds() {
        ArrayList<ResourceLocation> list = new ArrayList<ResourceLocation>(TestPlots.getPlots().keySet());
        list.sort(Comparator.comparing(ResourceLocation::toString));
        return list;
    }

    public static List<Plot> createPlots() {
        ArrayList<Plot> plots = new ArrayList<Plot>();
        for (Map.Entry<ResourceLocation, Consumer<PlotBuilder>> entry : TestPlots.getPlots().entrySet()) {
            Plot plot = new Plot(entry.getKey());
            entry.getValue().accept(plot);
            plots.add(plot);
        }
        return plots;
    }

    @Nullable
    public static Plot getById(ResourceLocation name) {
        Consumer<PlotBuilder> factory = TestPlots.getPlots().get(name);
        if (factory == null) {
            return null;
        }
        Plot plot = new Plot(name);
        factory.accept(plot);
        return plot;
    }

    private static AEItemKey createEnchantedPickaxe(Level level) {
        Registry enchantmentRegistry = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        ItemStack enchantedPickaxe = new ItemStack((ItemLike)Items.DIAMOND_PICKAXE);
        enchantedPickaxe.enchant((Holder)enchantmentRegistry.getHolderOrThrow(Enchantments.FORTUNE), 3);
        return AEItemKey.of(enchantedPickaxe);
    }

    @TestPlot(value="all_terminals")
    public static void allTerminals(PlotBuilder plot) {
        plot.creativeEnergyCell("0 -1 0");
        plot.cable("[-1,0] [0,8] 0", AEParts.COVERED_DENSE_CABLE);
        plot.part("0 [0,8] 0", Direction.WEST, AEParts.CABLE_ANCHOR);
        plot.block("[-1,0] 5 0", AEBlocks.CONTROLLER);
        plot.storageDrive(new BlockPos(0, 5, 1));
        plot.afterGridInitAt(new BlockPos(0, 5, 1), (grid, gridNode) -> {
            AEItemKey enchantedPickaxe = TestPlots.createEnchantedPickaxe((Level)gridNode.getLevel());
            MEStorage storage = grid.getStorageService().getInventory();
            BaseActionSource src = new BaseActionSource();
            storage.insert(AEItemKey.of((ItemLike)Items.DIAMOND_PICKAXE), 10L, Actionable.MODULATE, src);
            storage.insert(enchantedPickaxe, 1234L, Actionable.MODULATE, src);
            storage.insert(AEItemKey.of((ItemLike)Items.ACACIA_LOG), Integer.MAX_VALUE, Actionable.MODULATE, src);
        });
        int y = 0;
        for (AEColor color : TestPlots.getColorsTransparentFirst()) {
            PlotBuilder line = y >= 9 ? plot.transform(bb -> new BoundingBox(-1 - bb.maxX(), bb.minY(), bb.minZ(), -1 - bb.minX(), bb.maxY(), bb.maxZ())).offset(0, y - 9, 0) : plot.offset(0, y, 0);
            ++y;
            line.cable("[1,9] 0 0", AEParts.GLASS_CABLE, color);
            if (color == AEColor.TRANSPARENT) {
                line.part("[1,9] 0 0", Direction.UP, AEParts.CABLE_ANCHOR);
            }
            line.part("1 0 0", Direction.NORTH, AEParts.TERMINAL);
            line.part("2 0 0", Direction.NORTH, AEParts.CRAFTING_TERMINAL);
            line.part("3 0 0", Direction.NORTH, AEParts.PATTERN_ENCODING_TERMINAL);
            line.part("4 0 0", Direction.NORTH, AEParts.PATTERN_ACCESS_TERMINAL);
            line.part("5 0 0", Direction.NORTH, AEParts.STORAGE_MONITOR, monitor -> {
                AEItemKey enchantedPickaxe = TestPlots.createEnchantedPickaxe(monitor.getLevel());
                monitor.setConfiguredItem(enchantedPickaxe);
                monitor.setLocked(true);
            });
            line.part("6 0 0", Direction.NORTH, AEParts.CONVERSION_MONITOR, monitor -> {
                monitor.setConfiguredItem(AEItemKey.of((ItemLike)Items.ACACIA_LOG));
                monitor.setLocked(true);
            });
            line.part("7 0 0", Direction.NORTH, AEParts.MONITOR);
            line.part("8 0 0", Direction.NORTH, AEParts.SEMI_DARK_MONITOR);
            line.part("9 0 0", Direction.NORTH, AEParts.DARK_MONITOR);
        }
    }

    public static ArrayList<AEColor> getColorsTransparentFirst() {
        ArrayList<AEColor> colors = new ArrayList<AEColor>();
        Collections.addAll(colors, AEColor.values());
        colors.remove((Object)AEColor.TRANSPARENT);
        colors.add(0, AEColor.TRANSPARENT);
        return colors;
    }

    @TestPlot(value="item_chest")
    public static void itemChest(PlotBuilder plot) {
        plot.blockEntity("0 0 0", AEBlocks.ME_CHEST, chest -> {
            Item item;
            ItemStack cellItem = AEItems.ITEM_CELL_1K.stack();
            StorageCell cellInv = StorageCells.getCellInventory(cellItem, null);
            RandomSource r = RandomSource.create();
            for (int i = 0; i < 100 && cellInv.insert(AEItemKey.of((ItemLike)(item = BuiltInRegistries.ITEM.getRandom(r).map(Holder::value).get())), 64L, Actionable.MODULATE, new BaseActionSource()) != 0L; ++i) {
            }
            chest.setCell(cellItem);
        });
        plot.creativeEnergyCell("0 -1 0");
    }

    @TestPlot(value="fluid_chest")
    public static void fluidChest(PlotBuilder plot) {
        plot.blockEntity("0 0 0", AEBlocks.ME_CHEST, chest -> {
            Fluid fluid;
            ItemStack cellItem = AEItems.FLUID_CELL_1K.stack();
            StorageCell cellInv = StorageCells.getCellInventory(cellItem, null);
            RandomSource r = RandomSource.create();
            for (int i = 0; i < 100 && ((fluid = BuiltInRegistries.FLUID.getRandom(r).map(Holder::value).get()).isSame(Fluids.EMPTY) || !fluid.isSource(fluid.defaultFluidState()) || cellInv.insert(AEFluidKey.of(fluid), 64000L, Actionable.MODULATE, new BaseActionSource()) != 0L); ++i) {
            }
            chest.setCell(cellItem);
        });
        plot.creativeEnergyCell("0 -1 0");
    }

    @TestPlot(value="import_exportbus")
    public static void importExportBus(PlotBuilder plot) {
        plot.chest("1 0 1", new ItemStack((ItemLike)Items.ACACIA_LOG, 16), new ItemStack((ItemLike)Items.ENDER_PEARL, 6));
        plot.block("1 1 1", Blocks.HOPPER);
        plot.creativeEnergyCell("3 -1 1");
        plot.cable("3 0 1").part(Direction.NORTH, AEParts.TERMINAL);
        plot.cable("2 0 1").part(Direction.WEST, AEParts.IMPORT_BUS);
        plot.cable("2 1 1").part(Direction.WEST, AEParts.EXPORT_BUS, bus -> bus.getConfig().setStack(0, new GenericStack(AEItemKey.of((ItemLike)Items.ENDER_PEARL), 1L)));
        plot.blockEntity("3 -1 0", AEBlocks.DRIVE, drive -> drive.getInternalInventory().addItems(AEItems.ITEM_CELL_64K.stack()));
    }

    @TestPlot(value="inverted_import_bus_multitype")
    public static void invertedImportBusMultitype(PlotBuilder plot) {
        BlockPos origin = BlockPos.ZERO;
        plot.creativeEnergyCell(origin.below());
        plot.cable(origin).part(Direction.WEST, AEParts.IMPORT_BUS, part -> {
            part.getUpgrades().addItems(AEItems.INVERTER_CARD.stack());
            part.getConfig().addFilter((Fluid)Fluids.LAVA);
        });
        plot.blockEntity(origin.west(), AEBlocks.INTERFACE, iface -> {
            iface.getConfig().insert(0, AEFluidKey.of((Fluid)Fluids.LAVA), 1000L, Actionable.MODULATE);
            iface.getStorage().insert(0, AEFluidKey.of((Fluid)Fluids.LAVA), 1000L, Actionable.MODULATE);
            iface.getConfig().insert(1, AEFluidKey.of((Fluid)Fluids.WATER), 1000L, Actionable.MODULATE);
            iface.getStorage().insert(1, AEFluidKey.of((Fluid)Fluids.WATER), 1000L, Actionable.MODULATE);
            iface.getConfig().insert(2, AEItemKey.of((ItemLike)Items.STICK), 1L, Actionable.MODULATE);
            iface.getStorage().insert(2, AEItemKey.of((ItemLike)Items.STICK), 1L, Actionable.MODULATE);
        });
        plot.storageDrive(origin.east());
        plot.test(helper -> helper.succeedWhen(() -> {
            helper.assertNetworkContainsNot(origin, (Fluid)Fluids.LAVA);
            helper.assertNetworkContains(origin, (Fluid)Fluids.WATER);
            helper.assertNetworkContains(origin, (ItemLike)Items.STICK);
        }));
    }

    @TestPlot(value="inscriber")
    public static void inscriber(PlotBuilder plot) {
        TestPlots.processorInscriber(plot.offset(0, 1, 2), AEItems.LOGIC_PROCESSOR_PRESS, (ItemLike)Items.GOLD_INGOT);
        TestPlots.processorInscriber(plot.offset(5, 1, 2), AEItems.ENGINEERING_PROCESSOR_PRESS, (ItemLike)Items.DIAMOND);
        TestPlots.processorInscriber(plot.offset(10, 1, 2), AEItems.CALCULATION_PROCESSOR_PRESS, AEItems.CERTUS_QUARTZ_CRYSTAL);
    }

    public static void processorInscriber(PlotBuilder plot, ItemLike processorPress, ItemLike processorMaterial) {
        plot.filledHopper("-1 3 0", Direction.DOWN, processorMaterial);
        plot.creativeEnergyCell("-1 2 1");
        plot.blockEntity("-1 2 0", AEBlocks.INSCRIBER, inscriber -> {
            inscriber.getInternalInventory().setItemDirect(0, new ItemStack(processorPress));
            BlockOrientation.NORTH_WEST.setOn((BlockEntity)inscriber);
        });
        plot.filledHopper("1 3 0", Direction.DOWN, AEItems.SILICON);
        plot.creativeEnergyCell("1 2 1");
        plot.blockEntity("1 2 0", AEBlocks.INSCRIBER, inscriber -> {
            inscriber.getInternalInventory().setItemDirect(0, AEItems.SILICON_PRESS.stack());
            BlockOrientation.NORTH_WEST.setOn((BlockEntity)inscriber);
        });
        plot.hopper("1 1 0", Direction.WEST, new ItemStack[0]);
        plot.hopper("-1 1 0", Direction.EAST, new ItemStack[0]);
        plot.filledHopper("0 2 0", Direction.DOWN, (ItemLike)Items.REDSTONE);
        plot.creativeEnergyCell("0 1 1");
        plot.blockEntity("0 1 0", AEBlocks.INSCRIBER, BlockOrientation.NORTH_WEST::setOn);
        plot.hopper("0 0 0", Direction.DOWN, new ItemStack[0]);
    }

    @TestPlot(value="import_and_export_in_one_tick")
    public static void importAndExportInOneTick(PlotBuilder plot) {
        plot.creativeEnergyCell("-1 0 0");
        plot.chest("0 0 1", new ItemStack[0]);
        plot.cable("0 0 0").part(Direction.SOUTH, AEParts.EXPORT_BUS, exportBus -> {
            exportBus.getUpgrades().addItems(AEItems.CRAFTING_CARD.stack());
            exportBus.getConfig().addFilter((ItemLike)Items.OAK_PLANKS);
        });
        plot.cable("0 1 0");
        plot.cable("0 1 -1").craftingEmitter(Direction.DOWN, (ItemLike)Items.OAK_PLANKS);
        plot.cable("0 0 -1").part(Direction.NORTH, AEParts.IMPORT_BUS, part -> {
            part.getUpgrades().addItems(AEItems.REDSTONE_CARD.stack());
            part.getConfigManager().putSetting(Settings.REDSTONE_CONTROLLED, RedstoneMode.HIGH_SIGNAL);
        });
        plot.block("1 0 0", AEBlocks.CRAFTING_STORAGE_1K);
        plot.chest("0 0 -2", new ItemStack((ItemLike)Items.OAK_PLANKS, 1));
        plot.test(helper -> helper.succeedWhen(() -> {
            helper.assertContainerContains(new BlockPos(0, 0, 1), Items.OAK_PLANKS);
            helper.assertContainerEmpty(new BlockPos(0, 0, -2));
        }));
    }

    @TestPlot(value="export_from_storagebus")
    public static void exportFromStorageBus(PlotBuilder plot) {
        plot.creativeEnergyCell("1 0 0");
        plot.cable("0 0 0").part(Direction.SOUTH, AEParts.EXPORT_BUS, part -> part.getConfig().addFilter((ItemLike)Items.OAK_PLANKS)).part(Direction.NORTH, AEParts.STORAGE_BUS);
        plot.chest("0 0 1", new ItemStack[0]);
        plot.chest("0 0 -1", new ItemStack((ItemLike)Items.OAK_PLANKS));
        plot.test(helper -> helper.succeedWhen(() -> {
            helper.assertContainerContains(new BlockPos(0, 0, 1), Items.OAK_PLANKS);
            helper.assertContainerEmpty(new BlockPos(0, 0, -1));
        }));
    }

    @TestPlot(value="import_into_storagebus")
    public static void importIntoStorageBus(PlotBuilder plot) {
        plot.creativeEnergyCell("1 0 0");
        plot.cable("0 0 0").part(Direction.NORTH, AEParts.IMPORT_BUS).part(Direction.SOUTH, AEParts.STORAGE_BUS);
        plot.chest("0 0 1", new ItemStack[0]);
        plot.chest("0 0 -1", new ItemStack((ItemLike)Items.OAK_PLANKS));
        plot.test(helper -> {
            helper.succeedWhen(() -> {
                helper.assertContainerContains(new BlockPos(0, 0, 1), Items.OAK_PLANKS);
                helper.assertContainerEmpty(new BlockPos(0, 0, -1));
            });
            helper.startSequence().thenIdle(10).thenSucceed();
        });
    }

    @TestPlot(value="import_on_pulse")
    public static void importOnPulse(PlotBuilder plot) {
        BlockPos origin = BlockPos.ZERO;
        BlockPos inputPos = origin.south();
        plot.creativeEnergyCell(origin.west().west());
        plot.storageDrive(origin.west());
        plot.cable(origin).part(Direction.SOUTH, AEParts.IMPORT_BUS, bus -> {
            bus.getUpgrades().addItems(AEItems.REDSTONE_CARD.stack());
            bus.getConfigManager().putSetting(Settings.REDSTONE_CONTROLLED, RedstoneMode.SIGNAL_PULSE);
        }).part(Direction.NORTH, AEParts.TERMINAL);
        plot.chest(inputPos, new ItemStack((ItemLike)Items.OAK_PLANKS));
        plot.block(origin.east(), Blocks.STONE);
        BlockPos leverPos = plot.leverOn(origin.east(), Direction.NORTH);
        plot.test(helper -> {
            ChestBlockEntity inputChest = (ChestBlockEntity)helper.getBlockEntity(inputPos);
            IGrid grid = helper.getGrid(origin);
            Runnable assertNothingMoved = () -> helper.assertContainerContains(inputPos, Items.OAK_PLANKS);
            Runnable assertMoved = () -> {
                helper.assertContainerEmpty(inputPos);
                helper.assertNetworkContains(origin, (ItemLike)Items.OAK_PLANKS);
            };
            Runnable reset = () -> {
                inputChest.clearContent();
                helper.clearStorage(grid);
                inputChest.setItem(0, new ItemStack((ItemLike)Items.OAK_PLANKS));
            };
            Runnable toggleSignal = () -> helper.pullLever(leverPos);
            helper.startSequence().thenExecuteAfter(1, assertNothingMoved).thenExecute(toggleSignal).thenExecute(assertNothingMoved).thenExecuteAfter(1, assertMoved).thenExecute(reset).thenExecute(toggleSignal).thenExecuteFor(30, assertNothingMoved).thenSucceed();
        });
    }

    @TestPlot(value="import_on_pulse_transactioncrash")
    public static void importOnPulseTransactionCrash(PlotBuilder plot) {
        plot.creativeEnergyCell("1 0 0");
        plot.chest("0 0 -1", new ItemStack((ItemLike)Items.OAK_PLANKS));
        plot.chest("0 0 1", new ItemStack[0]);
        plot.block("0 1 0", Blocks.REDSTONE_BLOCK);
        plot.cable("-1 0 0");
        plot.cable("-1 0 -1").part(Direction.EAST, AEParts.STORAGE_BUS, storageBus -> storageBus.getConfigManager().putSetting(Settings.ACCESS, AccessRestriction.READ));
        plot.cable("0 0 0").part(Direction.SOUTH, AEParts.STORAGE_BUS);
        plot.test(helper -> helper.startSequence().thenExecuteAfter(1, () -> {
            BlockPos pos = helper.absolutePos(BlockPos.ZERO);
            ImportBusPart importBus = (ImportBusPart)PartHelper.setPart(helper.getLevel(), pos, Direction.NORTH, null, AEParts.IMPORT_BUS.get());
            importBus.getUpgrades().addItems(AEItems.REDSTONE_CARD.stack());
            importBus.getConfigManager().putSetting(Settings.REDSTONE_CONTROLLED, RedstoneMode.SIGNAL_PULSE);
        }).thenExecuteFor(100, () -> helper.assertContainerEmpty(new BlockPos(0, 0, 1))).thenSucceed()).setupTicks(20).maxTicks(150);
    }

    @TestPlot(value="mattercannon_range")
    public static void matterCannonRange(PlotBuilder plot) {
        BlockPos origin = BlockPos.ZERO;
        plot.fencedEntity(origin.offset(0, 0, 5), EntityType.COW, entity -> entity.setSilent(true));
        plot.creativeEnergyCell(origin.below());
        plot.blockEntity(origin, AEBlocks.ME_CHEST, chest -> chest.setCell(TestPlots.createMatterCannon(Items.IRON_NUGGET)));
        plot.block("-2 [0,1] 5", Blocks.STONE);
        plot.block("2 [0,1] 5", Blocks.STONE);
        plot.creativeEnergyCell(origin.west().below());
        plot.block(origin.west(), AEBlocks.CHARGER);
        TestPlots.matterCannonDispenser(plot.offset(-2, 1, 1), AEItems.COLORED_LUMEN_PAINT_BALL.item(AEColor.PURPLE));
        TestPlots.matterCannonDispenser(plot.offset(0, 1, 1), Items.IRON_NUGGET);
        TestPlots.matterCannonDispenser(plot.offset(2, 1, 1), new Item[0]);
    }

    private static void matterCannonDispenser(PlotBuilder plot, Item ... ammos) {
        plot.blockState(BlockPos.ZERO, (BlockState)Blocks.DISPENSER.defaultBlockState().setValue((Property)DispenserBlock.FACING, (Comparable)Direction.SOUTH));
        plot.customizeBlockEntity(BlockPos.ZERO, BlockEntityType.DISPENSER, dispenser -> dispenser.setItem(0, TestPlots.createMatterCannon(ammos)));
        plot.buttonOn(BlockPos.ZERO, Direction.NORTH);
    }

    private static ItemStack createMatterCannon(Item ... ammo) {
        ItemStack cannon = AEItems.MATTER_CANNON.stack();
        ((MatterCannonItem)cannon.getItem()).injectAEPower(cannon, Double.MAX_VALUE, Actionable.MODULATE);
        BasicCellInventory cannonInv = BasicCellInventory.createInventory(cannon, null);
        for (Item item : ammo) {
            AEItemKey key = AEItemKey.of((ItemLike)item);
            cannonInv.insert(key, key.getMaxStackSize(), Actionable.MODULATE, new BaseActionSource());
        }
        return cannon;
    }

    @TestPlot(value="insert_fluid_into_mechest")
    public static void testInsertFluidIntoMEChest(PlotBuilder plot) {
        BlockPos origin = BlockPos.ZERO;
        plot.creativeEnergyCell(origin.below());
        plot.blockEntity(origin, AEBlocks.ME_CHEST, chest -> chest.setCell(AEItems.FLUID_CELL_4K.stack()));
        plot.cable(origin.east()).part(Direction.WEST, AEParts.EXPORT_BUS, bus -> bus.getConfig().addFilter((Fluid)Fluids.WATER));
        plot.blockEntity(origin.east().north(), AEBlocks.DRIVE, drive -> drive.getInternalInventory().addItems(CreativeCellItem.ofFluids(new Fluid[]{Fluids.WATER})));
        plot.creativeEnergyCell(origin.east().north().below());
        plot.test(helper -> helper.succeedWhen(() -> {
            MEChestBlockEntity meChest = (MEChestBlockEntity)helper.getBlockEntity(origin);
            helper.assertContains(meChest.getInventory(), AEFluidKey.of((Fluid)Fluids.WATER));
        }));
    }

    @TestPlot(value="insert_item_into_mechest")
    public static void testInsertItemsIntoMEChest(PlotBuilder plot) {
        BlockPos origin = BlockPos.ZERO;
        plot.creativeEnergyCell(origin.below());
        plot.blockEntity(origin, AEBlocks.ME_CHEST, chest -> {
            ItemStack cell = AEItems.ITEM_CELL_1K.stack();
            AEItems.ITEM_CELL_1K.get().getConfigInventory(cell).addFilter((ItemLike)Items.REDSTONE);
            chest.setCell(cell);
        });
        plot.hopper(origin.above(), Direction.DOWN, new ItemLike[]{Items.STICK, Items.REDSTONE});
        plot.test(helper -> helper.succeedWhen(() -> {
            MEChestBlockEntity meChest = (MEChestBlockEntity)helper.getBlockEntity(origin);
            helper.assertContains(meChest.getInventory(), AEItemKey.of((ItemLike)Items.REDSTONE));
            helper.assertContainerContains(origin.above(), Items.STICK);
        }));
    }

    @TestPlot(value="maxchannels_adhoctest")
    public static void maxChannelsAdHocTest(PlotBuilder plot) {
        plot.creativeEnergyCell("0 -1 0");
        plot.block("[-3,3] -2 [-3,3]", AEBlocks.DRIVE);
        plot.cable("[-3,3] 0 [-3,3]", AEParts.SMART_DENSE_CABLE);
        plot.cable("[-3,3] [1,64] [-3,2]").part(Direction.EAST, AEParts.TERMINAL).part(Direction.NORTH, AEParts.TERMINAL).part(Direction.WEST, AEParts.TERMINAL).part(Direction.WEST, AEParts.TERMINAL);
        plot.cable("[-3,3] [1,64] 3").part(Direction.NORTH, AEParts.PATTERN_PROVIDER).part(Direction.SOUTH, AEParts.PATTERN_PROVIDER).part(Direction.EAST, AEParts.PATTERN_PROVIDER).part(Direction.WEST, AEParts.PATTERN_PROVIDER);
        plot.afterGridExistsAt(BlockPos.ZERO, (grid, node) -> {
            ((PathingService)grid.getPathingService()).setForcedChannelMode(ChannelMode.INFINITE);
            Iterator<PatternProviderPart> patternProviders = grid.getMachines(PatternProviderPart.class).iterator();
            PatternProviderPart current = patternProviders.next();
            List craftingRecipes = node.getLevel().getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING);
            HashSet<AEItemKey> neededIngredients = new HashSet<AEItemKey>();
            HashSet<AEItemKey> providedResults = new HashSet<AEItemKey>();
            for (RecipeHolder holder : craftingRecipes) {
                ItemStack craftingPattern;
                block8: {
                    CraftingRecipe recipe = (CraftingRecipe)holder.value();
                    if (recipe.isSpecial()) continue;
                    try {
                        ItemStack[] ingredients = (ItemStack[])CraftingRecipeUtil.ensure3by3CraftingMatrix(recipe).stream().map(i -> {
                            if (i.isEmpty()) {
                                return ItemStack.EMPTY;
                            }
                            return i.getItems()[0];
                        }).toArray(ItemStack[]::new);
                        craftingPattern = PatternDetailsHelper.encodeCraftingPattern((RecipeHolder<CraftingRecipe>)holder, ingredients, recipe.getResultItem((HolderLookup.Provider)node.getLevel().registryAccess()), false, false);
                        for (ItemStack ingredient : ingredients) {
                            AEItemKey key = AEItemKey.of(ingredient);
                            if (key == null) continue;
                            neededIngredients.add(key);
                        }
                        if (recipe.getResultItem((HolderLookup.Provider)node.getLevel().registryAccess()).isEmpty()) break block8;
                        providedResults.add(AEItemKey.of(recipe.getResultItem((HolderLookup.Provider)node.getLevel().registryAccess())));
                    }
                    catch (Exception e) {
                        AELog.warn(e);
                        continue;
                    }
                }
                if (current.getLogic().getPatternInv().addItems(craftingPattern).isEmpty()) continue;
                if (!patternProviders.hasNext()) break;
                current = patternProviders.next();
                current.getLogic().getPatternInv().addItems(craftingPattern);
            }
            UnmodifiableIterator keysToAdd = Sets.difference(neededIngredients, providedResults).iterator();
            block4: for (DriveBlockEntity drive : grid.getMachines(DriveBlockEntity.class)) {
                InternalInventory cellInv = drive.getInternalInventory();
                for (int i2 = 0; i2 < cellInv.size(); ++i2) {
                    ItemStack creativeCell = AEItems.CREATIVE_CELL.stack();
                    ConfigInventory configInv = AEItems.CREATIVE_CELL.get().getConfigInventory(creativeCell);
                    for (int j = 0; j < configInv.size(); ++j) {
                        if (!keysToAdd.hasNext()) {
                            cellInv.addItems(creativeCell);
                            break block4;
                        }
                        AEItemKey keyToAdd = (AEItemKey)keysToAdd.next();
                        configInv.setStack(j, new GenericStack(keyToAdd, 1L));
                    }
                    cellInv.addItems(creativeCell);
                }
            }
        });
    }

    @TestPlot(value="blockingmode_subnetwork_chesttest")
    public static void blockingModeSubnetworkChestTest(PlotBuilder plot) {
        plot.creativeEnergyCell("0 -1 0");
        plot.block("[0,1] [0,1] [0,1]", AEBlocks.CRAFTING_ACCELERATOR);
        plot.block("0 0 0", AEBlocks.CRAFTING_STORAGE_64K);
        GenericStack input = GenericStack.fromItemStack(new ItemStack((ItemLike)Items.GOLD_INGOT));
        GenericStack output = GenericStack.fromItemStack(new ItemStack((ItemLike)Items.DIAMOND));
        plot.cable("2 0 0").part(Direction.EAST, AEParts.PATTERN_PROVIDER, pp -> {
            pp.getLogic().getPatternInv().addItems(PatternDetailsHelper.encodeProcessingPattern(List.of(input), List.of(output)));
            pp.getLogic().getConfigManager().putSetting(Settings.BLOCKING_MODE, YesNo.YES);
        });
        plot.drive(new BlockPos(2, 0, -1)).addCreativeCell().add(input);
        plot.creativeEnergyCell("3 -1 0");
        plot.cable("3 0 0").part(Direction.WEST, AEParts.INTERFACE).part(Direction.EAST, AEParts.STORAGE_BUS);
        plot.block("4 0 0", Blocks.CHEST);
        plot.test(helper -> {
            TestCraftingJob craftingJob = new TestCraftingJob((PlotTestHelper)((Object)helper), BlockPos.ZERO, output.what(), 64L);
            helper.startSequence().thenWaitUntil(craftingJob::tickUntilStarted).thenWaitUntil(() -> {
                IGrid grid = helper.getGrid(BlockPos.ZERO);
                long requesting = grid.getCraftingService().getRequestedAmount(output.what());
                helper.check(requesting > 0L, "not yet requesting items");
                if (requesting != 1L) {
                    helper.fail("blocking mode failed, requesting: " + requesting);
                }
            }).thenSucceed();
        });
    }

    @TestPlot(value="terminal_fullof_enchanteditems")
    public static void terminalFullOfEnchantedItems(PlotBuilder plot) {
        BlockPos origin = BlockPos.ZERO;
        plot.creativeEnergyCell(origin.below());
        plot.cable(origin).part(Direction.NORTH, AEParts.TERMINAL);
        DriveBuilder drive = plot.drive(origin.east());
        plot.addPostBuildAction((level, player, ignored) -> {
            Holder<Enchantment> enchantment = Platform.getEnchantment(level, (ResourceKey<Enchantment>)Enchantments.FORTUNE);
            ItemStack pickaxe = new ItemStack((ItemLike)Items.DIAMOND_PICKAXE);
            pickaxe.enchant(enchantment, 1);
            for (int i = 0; i < 10; ++i) {
                DriveBuilder.ItemCellBuilder cell = drive.addItemCell64k();
                for (int j = 0; j < 63; ++j) {
                    pickaxe.setDamageValue(pickaxe.getDamageValue() + 1);
                    cell.add(AEItemKey.of(pickaxe), 2L);
                }
            }
        });
    }

    @TestPlot(value="import_from_cauldron")
    public static void importLavaFromCauldron(PlotBuilder plot) {
        BlockPos origin = BlockPos.ZERO;
        plot.creativeEnergyCell(origin.below());
        plot.cable(origin).part(Direction.EAST, AEParts.IMPORT_BUS, importBus -> importBus.getUpgrades().addItems(AEItems.SPEED_CARD.stack())).part(Direction.WEST, AEParts.STORAGE_BUS);
        plot.block(origin.west(), AEBlocks.SKY_STONE_TANK);
        plot.block(origin.east(), Blocks.LAVA_CAULDRON);
        plot.test(helper -> helper.succeedWhen(() -> {
            helper.assertBlockPresent(Blocks.CAULDRON, origin.east());
            SkyStoneTankBlockEntity tank = (SkyStoneTankBlockEntity)helper.getBlockEntity(origin.west());
            helper.check(tank.getTank().getFluidAmount() == 1000, "Less than a bucket stored");
            helper.check(tank.getTank().getFluid().getFluid() == Fluids.LAVA, "Something other than lava stored");
        }));
    }

    @TestPlot(value="tool_repair_recipe")
    public static void toolRepairRecipe(PlotBuilder plot) {
        AEItemKey undamaged = AEItemKey.of((ItemLike)Items.DIAMOND_PICKAXE);
        int maxDamage = undamaged.getFuzzySearchMaxValue();
        AEItemKey damaged = (AEItemKey)Util.make(() -> {
            ItemStack is = undamaged.toStack();
            is.setDamageValue(maxDamage - 1);
            return AEItemKey.of(is);
        });
        AEItemKey correctResult = (AEItemKey)Util.make(() -> {
            ItemStack is = undamaged.toStack();
            int usesLeft = 2 + maxDamage * 5 / 100;
            is.setDamageValue(maxDamage - usesLeft);
            return AEItemKey.of(is);
        });
        plot.creativeEnergyCell("0 0 0");
        BlockPos molecularAssemblerPos = new BlockPos(0, 1, 0);
        plot.blockEntity(molecularAssemblerPos, AEBlocks.MOLECULAR_ASSEMBLER, molecularAssembler -> {
            NonNullList items = NonNullList.withSize((int)9, (Object)ItemStack.EMPTY);
            items.set(0, (Object)undamaged.toStack());
            items.set(1, (Object)undamaged.toStack());
            CraftingInput input = CraftingInput.of((int)3, (int)3, (List)items);
            Level level = molecularAssembler.getLevel();
            RecipeHolder recipe = (RecipeHolder)level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, (RecipeInput)input, level).get();
            ItemStack[] sparseInputs = new ItemStack[9];
            sparseInputs[0] = undamaged.toStack();
            sparseInputs[1] = undamaged.toStack();
            for (int i = 2; i < 9; ++i) {
                sparseInputs[i] = ItemStack.EMPTY;
            }
            ItemStack encodedPattern = PatternDetailsHelper.encodeCraftingPattern((RecipeHolder<CraftingRecipe>)recipe, sparseInputs, undamaged.toStack(), true, false);
            IPatternDetails patternDetails = PatternDetailsHelper.decodePattern(encodedPattern, level);
            KeyCounter[] table = new KeyCounter[]{new KeyCounter()};
            table[0].add(damaged, 2L);
            molecularAssembler.pushPattern(patternDetails, table, Direction.UP);
        });
        plot.test(helper -> helper.runAfterDelay(40L, () -> {
            MolecularAssemblerBlockEntity molecularAssembler = (MolecularAssemblerBlockEntity)helper.getBlockEntity(molecularAssemblerPos);
            ItemStack outputItem = molecularAssembler.getInternalInventory().getStackInSlot(9);
            if (correctResult.matches(outputItem)) {
                helper.succeed();
            } else if (undamaged.matches(outputItem)) {
                helper.fail("created undamaged item");
            }
        }));
    }

    @TestPlot(value="double_chest_storage_bus")
    private static void doubleChestStorageBus(PlotBuilder plot) {
        BlockPos o = BlockPos.ZERO;
        plot.chest(o.north(), new ItemStack((ItemLike)Items.STICK));
        plot.chest(o.north().west(), new ItemStack((ItemLike)Items.STICK));
        plot.blockState(o.north(), (BlockState)Blocks.CHEST.defaultBlockState().setValue((Property)ChestBlock.TYPE, (Comparable)ChestType.RIGHT));
        plot.blockState(o.north().west(), (BlockState)Blocks.CHEST.defaultBlockState().setValue((Property)ChestBlock.TYPE, (Comparable)ChestType.LEFT));
        plot.cable(o).part(Direction.NORTH, AEParts.STORAGE_BUS);
        plot.creativeEnergyCell(o.below());
        plot.test(helper -> helper.succeedWhen(() -> {
            IGrid grid = helper.getGrid(o);
            KeyCounter stacks = grid.getStorageService().getInventory().getAvailableStacks();
            long stickCount = stacks.get(AEItemKey.of((ItemLike)Items.STICK));
            helper.check(2L == stickCount, "Stick count wasn't 2: " + stickCount);
        }));
    }

    @TestPlot(value="export_bus_dupe_regression")
    private static void exportBusDupeRegression(PlotBuilder plot) {
        BlockPos o = BlockPos.ZERO;
        plot.chest(o.north(), new ItemStack((ItemLike)Items.STICK, 64));
        plot.blockState(o.north(), (BlockState)Blocks.CHEST.defaultBlockState().setValue((Property)ChestBlock.TYPE, (Comparable)ChestType.RIGHT));
        plot.blockState(o.north().west(), (BlockState)Blocks.CHEST.defaultBlockState().setValue((Property)ChestBlock.TYPE, (Comparable)ChestType.LEFT));
        plot.cable(o).part(Direction.NORTH, AEParts.STORAGE_BUS).part(Direction.SOUTH, AEParts.EXPORT_BUS, part -> {
            part.getConfig().addFilter((ItemLike)Items.STICK);
            part.getUpgrades().addItems(AEItems.SPEED_CARD.stack(1));
            part.getUpgrades().addItems(AEItems.SPEED_CARD.stack(1));
            part.getUpgrades().addItems(AEItems.SPEED_CARD.stack(1));
            part.getUpgrades().addItems(AEItems.SPEED_CARD.stack(1));
        });
        plot.chest(o.south(), new ItemStack[0]);
        plot.cable(o.west()).part(Direction.NORTH, AEParts.STORAGE_BUS);
        plot.creativeEnergyCell(o.below());
        plot.test(helper -> helper.succeedWhen(() -> {
            helper.assertContainerEmpty(o.north());
            helper.assertContainerEmpty(o.north().west());
            KeyCounter counter = helper.countContainerContentAt(o.south());
            long stickCount = counter.get(AEItemKey.of((ItemLike)Items.STICK));
            helper.check(stickCount == 64L, "Expected 64 sticks total, but found: " + stickCount);
        }));
    }
}

