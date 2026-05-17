/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.ChestBlock
 *  net.minecraft.world.level.block.entity.HopperBlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.ChestType
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.material.Fluid
 *  net.minecraft.world.level.material.Fluids
 *  net.neoforged.neoforge.capabilities.Capabilities$FluidHandler
 *  net.neoforged.neoforge.capabilities.Capabilities$ItemHandler
 *  net.neoforged.neoforge.fluids.FluidStack
 *  net.neoforged.neoforge.fluids.capability.IFluidHandler
 *  net.neoforged.neoforge.items.IItemHandler
 */
package appeng.server.testplots;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.blockentity.misc.InterfaceBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.me.helpers.BaseActionSource;
import appeng.parts.misc.InterfacePart;
import appeng.server.testplots.TestPlot;
import appeng.server.testplots.TestPlotClass;
import appeng.server.testworld.PlotBuilder;
import appeng.util.ConfigInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

@TestPlotClass
public class InterfaceTestPlots {
    @TestPlot(value="interface_slot_filtering")
    public static void interfaceSlotFiltering(PlotBuilder builder) {
        BlockPos o = BlockPos.ZERO;
        builder.blockEntity(o, AEBlocks.INTERFACE, iface -> iface.getInterfaceLogic().getConfig().setStack(0, new GenericStack(AEItemKey.of((ItemLike)Items.STICK), 1L)));
        builder.hopper(o.above(), Direction.DOWN, new ItemLike[]{Items.BRICK});
        builder.test(helper -> helper.startSequence().thenExecute(() -> {
            IItemHandler itemCap = (IItemHandler)helper.getCapability(o, Capabilities.ItemHandler.BLOCK, Direction.UP);
            helper.check(itemCap.isItemValid(0, ItemStack.EMPTY), "empty stack should be valid in slot 0");
            helper.check(itemCap.isItemValid(0, Items.STICK.getDefaultInstance()), "stick should be valid in slot 0");
            helper.check(itemCap.isItemValid(1, Items.STICK.getDefaultInstance()), "stick should be valid in slot 1");
            helper.check(!itemCap.isItemValid(0, Blocks.BRICKS.asItem().getDefaultInstance()), "bricks should not be valid in slot 0");
            helper.check(itemCap.isItemValid(1, Blocks.BRICKS.asItem().getDefaultInstance()), "bricks should be valid in slot 1");
            IFluidHandler fluidCap = (IFluidHandler)helper.getCapability(o, Capabilities.FluidHandler.BLOCK, Direction.UP);
            helper.check(fluidCap.isFluidValid(0, FluidStack.EMPTY), "empty fluid stack should be valid in slot 0");
            helper.check(!fluidCap.isFluidValid(0, new FluidStack((Fluid)Fluids.WATER, 1)), "fluid should not be valid in slot 0");
            helper.check(fluidCap.isFluidValid(1, new FluidStack((Fluid)Fluids.WATER, 1)), "fluid should be valid in slot 1");
        }).thenWaitUntil(() -> {
            InterfaceBlockEntity iface = (InterfaceBlockEntity)helper.getBlockEntity(o);
            helper.assertEquals(o, null, iface.getStorage().getKey(0));
            helper.assertEquals(o, AEItemKey.of((ItemLike)Items.BRICK), iface.getStorage().getKey(1));
        }).thenExecute(() -> {
            HopperBlockEntity hopper = (HopperBlockEntity)helper.getBlockEntity(o.above());
            hopper.setItem(0, Items.STICK.getDefaultInstance());
        }).thenWaitUntil(() -> {
            InterfaceBlockEntity iface = (InterfaceBlockEntity)helper.getBlockEntity(o);
            helper.assertEquals(o, AEItemKey.of((ItemLike)Items.STICK), iface.getStorage().getKey(0));
            helper.assertEquals(o, AEItemKey.of((ItemLike)Items.BRICK), iface.getStorage().getKey(1));
        }).thenSucceed());
    }

    @TestPlot(value="interface_restock_dupe_test")
    public static void interfaceRestockDupeTest(PlotBuilder plot) {
        BlockPos o = BlockPos.ZERO;
        plot.chest(o.north(), new ItemStack((ItemLike)Items.STICK, 64));
        plot.blockState(o.north(), (BlockState)Blocks.CHEST.defaultBlockState().setValue((Property)ChestBlock.TYPE, (Comparable)ChestType.RIGHT));
        plot.cable(o).part(Direction.NORTH, AEParts.STORAGE_BUS);
        plot.blockState(o.north().west(), (BlockState)Blocks.CHEST.defaultBlockState().setValue((Property)ChestBlock.TYPE, (Comparable)ChestType.LEFT));
        plot.cable(o.west()).part(Direction.NORTH, AEParts.STORAGE_BUS);
        plot.blockEntity(o.above(), AEBlocks.INTERFACE, iface -> {
            iface.getConfig().setStack(0, GenericStack.fromItemStack(new ItemStack((ItemLike)Items.STICK, 64)));
            iface.getConfig().setStack(1, GenericStack.fromItemStack(new ItemStack((ItemLike)Items.STICK, 64)));
        });
        plot.creativeEnergyCell(o.below());
        plot.test(helper -> helper.succeedWhen(() -> {
            helper.assertContainerEmpty(o.north());
            helper.assertContainerEmpty(o.north().west());
            InterfaceBlockEntity iface = (InterfaceBlockEntity)helper.getBlockEntity(o.above());
            KeyCounter counter = new KeyCounter();
            iface.getInterfaceLogic().getStorage().getAvailableStacks(counter);
            long stickCount = counter.get(AEItemKey.of((ItemLike)Items.STICK));
            helper.check(stickCount == 64L, "Expected 64 sticks total, but found: " + stickCount);
        }));
    }

    @TestPlot(value="interface_to_interface_different_networks")
    public static void interfaceToInterfaceDifferentNetworks(PlotBuilder plot) {
        BlockPos o = BlockPos.ZERO;
        plot.cable(o).part(Direction.NORTH, AEParts.STORAGE_BUS);
        plot.blockEntity(o.north(), AEBlocks.INTERFACE, iface -> {
            iface.getConfig().setStack(0, GenericStack.fromItemStack(new ItemStack((ItemLike)Items.APPLE)));
            iface.getStorage().setStack(0, GenericStack.fromItemStack(new ItemStack((ItemLike)Items.APPLE, 64)));
        });
        plot.block(o.north().north(), AEBlocks.CREATIVE_ENERGY_CELL);
        plot.block(o.east(), AEBlocks.CREATIVE_ENERGY_CELL);
        plot.blockEntity(o.south(), AEBlocks.INTERFACE, iface -> iface.getConfig().setStack(0, GenericStack.fromItemStack(new ItemStack((ItemLike)Items.APPLE))));
        plot.test(helper -> helper.startSequence().thenWaitUntil(() -> {
            InterfaceBlockEntity iface = (InterfaceBlockEntity)helper.getBlockEntity(o.south());
            GenericStack apples = iface.getStorage().getStack(0);
            helper.check(apples != null && apples.amount() == 1L, "Expected 1 apple", o.south());
        }).thenExecute(() -> {
            InterfaceBlockEntity iface = (InterfaceBlockEntity)helper.getBlockEntity(o.south());
            iface.getStorage().setStack(1, GenericStack.fromItemStack(new ItemStack((ItemLike)Items.DIAMOND)));
        }).thenWaitUntil(() -> {
            InterfaceBlockEntity iface = (InterfaceBlockEntity)helper.getBlockEntity(o.north());
            GenericStack diamonds = iface.getStorage().getStack(1);
            helper.check(diamonds != null && diamonds.amount() == 1L, "Expected 1 diamond", o.north());
        }).thenSucceed());
    }

    @TestPlot(value="interface_part_caps")
    public static void interfacePartCaps(PlotBuilder plot) {
        plot.cable(BlockPos.ZERO).part(Direction.UP, AEParts.INTERFACE);
        plot.hopper(BlockPos.ZERO.above(), Direction.DOWN, new ItemLike[]{Items.DIRT});
        plot.test(helper -> helper.startSequence().thenWaitUntil(() -> {
            InterfacePart interfacePart = helper.getPart(BlockPos.ZERO, Direction.UP, InterfacePart.class);
            ConfigInventory storage = interfacePart.getInterfaceLogic().getStorage();
            helper.assertEquals(BlockPos.ZERO, AEItemKey.of((ItemLike)Items.DIRT), storage.getKey(0));
        }).thenSucceed());
    }

    @TestPlot(value="canceling_jobs_from_interfacecrash")
    public static void cancelingJobsFromInterfaceCrash(PlotBuilder plot) {
        BlockPos origin = BlockPos.ZERO;
        plot.creativeEnergyCell(origin);
        plot.blockEntity(origin.above(), AEBlocks.INTERFACE, iface -> {
            iface.getUpgrades().addItems(AEItems.CRAFTING_CARD.stack());
            iface.getConfig().setStack(0, new GenericStack(AEItemKey.of((ItemLike)Items.OAK_PLANKS), 1L));
        });
        plot.block(origin.east(), AEBlocks.CRAFTING_STORAGE_1K);
        plot.cable(origin.west()).craftingEmitter(Direction.WEST, (ItemLike)Items.OAK_PLANKS);
        plot.test(helper -> helper.startSequence().thenWaitUntil(() -> {
            IGrid grid = helper.getGrid(origin);
            helper.check(grid.getCraftingService().isRequesting(AEItemKey.of((ItemLike)Items.OAK_PLANKS)), "Interface is not crafting oak planks");
        }).thenExecute(() -> {
            InterfaceBlockEntity iface = (InterfaceBlockEntity)helper.getBlockEntity(origin.above());
            iface.getUpgrades().removeItems(1, ItemStack.EMPTY, null);
            IGrid grid = helper.getGrid(origin);
            long inserted = grid.getStorageService().getInventory().insert(AEItemKey.of((ItemLike)Items.OAK_PLANKS), 1L, Actionable.MODULATE, new BaseActionSource());
            helper.check(inserted == 0L, "Nothing should have been inserted into the network");
            helper.check(iface.getInterfaceLogic().getStorage().isEmpty(), "Nothing should have been inserted into the interface");
        }).thenSucceed()).maxTicks(300);
    }
}

