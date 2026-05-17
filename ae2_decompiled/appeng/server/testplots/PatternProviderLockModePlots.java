/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.gametest.framework.GameTestSequence
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.entity.BlockEntity
 */
package appeng.server.testplots;

import appeng.api.config.Actionable;
import appeng.api.config.LockCraftingMode;
import appeng.api.config.Settings;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEParts;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.parts.crafting.PatternProviderPart;
import appeng.server.testplots.TestPlot;
import appeng.server.testplots.TestPlotClass;
import appeng.server.testworld.PlotBuilder;
import appeng.server.testworld.PlotTestHelper;
import appeng.server.testworld.SavedBlockEntity;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;

@TestPlotClass
public final class PatternProviderLockModePlots {
    private static final BlockPos LEVER_POS = BlockPos.ZERO.east();
    private static final BlockPos BUTTON_POS = BlockPos.ZERO.west();
    private static final GenericStack ONE_PLANK = new GenericStack(AEItemKey.of((ItemLike)Blocks.OAK_PLANKS), 1L);
    private static final GenericStack TWO_PLANK = new GenericStack(AEItemKey.of((ItemLike)Blocks.OAK_PLANKS), 2L);

    private PatternProviderLockModePlots() {
    }

    @TestPlot(value="pp_block_lockmode_pulse_starting_high")
    public static void testBlockLockModePulseStartingHigh(PlotBuilder plot) {
        PatternProviderLockModePlots.setup(plot, false, LockCraftingMode.LOCK_UNTIL_PULSE);
        PatternProviderLockModePlots.testLockModePulse(plot, true);
    }

    @TestPlot(value="pp_block_lockmode_pulse")
    public static void testBlockLockModePulse(PlotBuilder plot) {
        PatternProviderLockModePlots.setup(plot, false, LockCraftingMode.LOCK_UNTIL_PULSE);
        PatternProviderLockModePlots.testLockModePulse(plot);
    }

    @TestPlot(value="pp_part_lockmode_pulse")
    public static void testPartLockModePulse(PlotBuilder plot) {
        PatternProviderLockModePlots.setup(plot, true, LockCraftingMode.LOCK_UNTIL_PULSE);
        PatternProviderLockModePlots.testLockModePulse(plot);
    }

    private static void testLockModePulse(PlotBuilder plot) {
        PatternProviderLockModePlots.testLockModePulse(plot, false);
    }

    private static void testLockModePulse(PlotBuilder plot, boolean startHigh) {
        plot.test(helper -> {
            PatternProviderLogicHost host = PatternProviderLockModePlots.getHost(helper);
            PatternProviderLogic pp = host.getLogic();
            GameTestSequence sequence = helper.startSequence();
            if (startHigh) {
                sequence.thenExecute(() -> helper.pullLever(LEVER_POS));
            }
            sequence.thenExecuteAfter(1, () -> {
                helper.assertEquals(BlockPos.ZERO, (Object)LockCraftingMode.NONE, (Object)pp.getCraftingLockedReason());
                helper.check(PatternProviderLockModePlots.pushPattern(host), "Pushing pattern failed");
                helper.assertEquals(BlockPos.ZERO, (Object)LockCraftingMode.LOCK_UNTIL_PULSE, (Object)pp.getCraftingLockedReason());
                helper.check(!PatternProviderLockModePlots.pushPattern(host), "Pushing pattern should fail");
            });
            if (startHigh) {
                sequence.thenExecuteAfter(1, () -> {
                    helper.getLevel().updateNeighborsAt(helper.absolutePos(BlockPos.ZERO.above()), Blocks.CHEST);
                    helper.assertEquals(BlockPos.ZERO, (Object)LockCraftingMode.LOCK_UNTIL_PULSE, (Object)pp.getCraftingLockedReason());
                    helper.pullLever(LEVER_POS);
                    helper.assertEquals(BlockPos.ZERO, (Object)LockCraftingMode.LOCK_UNTIL_PULSE, (Object)pp.getCraftingLockedReason());
                });
            }
            sequence.thenExecuteAfter(1, () -> {
                helper.pullLever(LEVER_POS);
                helper.assertEquals(BlockPos.ZERO, (Object)LockCraftingMode.NONE, (Object)pp.getCraftingLockedReason());
                helper.check(PatternProviderLockModePlots.pushPattern(host), "Pushing pattern failed");
                helper.assertEquals(BlockPos.ZERO, (Object)LockCraftingMode.LOCK_UNTIL_PULSE, (Object)pp.getCraftingLockedReason());
                helper.check(!PatternProviderLockModePlots.pushPattern(host), "Pushing pattern should fail");
                helper.pullLever(LEVER_POS);
                helper.assertEquals(BlockPos.ZERO, (Object)LockCraftingMode.LOCK_UNTIL_PULSE, (Object)pp.getCraftingLockedReason());
                helper.check(!PatternProviderLockModePlots.pushPattern(host), "Pushing pattern should fail");
            }).thenExecuteAfter(1, () -> {
                helper.assertEquals(BlockPos.ZERO, (Object)LockCraftingMode.LOCK_UNTIL_PULSE, (Object)pp.getCraftingLockedReason());
                helper.check(!PatternProviderLockModePlots.pushPattern(host), "Pushing pattern should fail");
                helper.pressButton(BUTTON_POS);
                helper.assertEquals(BlockPos.ZERO, (Object)LockCraftingMode.NONE, (Object)pp.getCraftingLockedReason());
                helper.check(PatternProviderLockModePlots.pushPattern(host), "Pushing pattern should succeed");
            }).thenExecuteAfter(1, () -> {
                KeyCounter counter = helper.countContainerContentAt(BlockPos.ZERO.above());
                helper.assertEquals(BlockPos.ZERO.above(), 3L, counter.get(AEItemKey.of((ItemLike)Blocks.OAK_LOG)));
            }).thenSucceed();
        });
    }

    @TestPlot(value="pp_block_lockmode_high")
    public static void testBlockLockModeHigh(PlotBuilder plot) {
        PatternProviderLockModePlots.setup(plot, false, LockCraftingMode.LOCK_WHILE_HIGH);
        PatternProviderLockModePlots.testLockModeHigh(plot);
    }

    @TestPlot(value="pp_part_lockmode_high")
    public static void testPartLockModeHigh(PlotBuilder plot) {
        PatternProviderLockModePlots.setup(plot, true, LockCraftingMode.LOCK_WHILE_HIGH);
        PatternProviderLockModePlots.testLockModeHigh(plot);
    }

    private static void testLockModeHigh(PlotBuilder plot) {
        plot.test(helper -> {
            PatternProviderLogicHost host = PatternProviderLockModePlots.getHost(helper);
            PatternProviderLogic pp = host.getLogic();
            helper.startSequence().thenExecuteAfter(1, () -> {
                helper.assertEquals(BlockPos.ZERO, (Object)LockCraftingMode.NONE, (Object)pp.getCraftingLockedReason());
                helper.check(PatternProviderLockModePlots.pushPattern(host), "Pushing pattern failed (1st attempt)");
                helper.check(PatternProviderLockModePlots.pushPattern(host), "Pushing pattern failed (2nd attempt)");
            }).thenExecuteAfter(1, () -> {
                helper.pullLever(LEVER_POS);
                helper.assertEquals(BlockPos.ZERO, (Object)LockCraftingMode.LOCK_WHILE_HIGH, (Object)pp.getCraftingLockedReason());
                helper.check(!PatternProviderLockModePlots.pushPattern(host), "Pushing pattern should fail");
                helper.pullLever(LEVER_POS);
                helper.assertEquals(BlockPos.ZERO, (Object)LockCraftingMode.NONE, (Object)pp.getCraftingLockedReason());
                helper.check(PatternProviderLockModePlots.pushPattern(host), "Pushing pattern failed");
                helper.assertEquals(BlockPos.ZERO, (Object)LockCraftingMode.NONE, (Object)pp.getCraftingLockedReason());
                helper.check(PatternProviderLockModePlots.pushPattern(host), "Pushing pattern failed (2nd attempt)");
            }).thenExecuteAfter(1, () -> {
                KeyCounter counter = helper.countContainerContentAt(BlockPos.ZERO.above());
                helper.assertEquals(BlockPos.ZERO.above(), 4L, counter.get(AEItemKey.of((ItemLike)Blocks.OAK_LOG)));
            }).thenSucceed();
        });
    }

    @TestPlot(value="pp_block_lockmode_low")
    public static void testBlockLockModeLow(PlotBuilder plot) {
        PatternProviderLockModePlots.setup(plot, false, LockCraftingMode.LOCK_WHILE_LOW);
        PatternProviderLockModePlots.testLockModeLow(plot);
    }

    @TestPlot(value="pp_part_lockmode_low")
    public static void testPartLockModeLow(PlotBuilder plot) {
        PatternProviderLockModePlots.setup(plot, true, LockCraftingMode.LOCK_WHILE_LOW);
        PatternProviderLockModePlots.testLockModeLow(plot);
    }

    private static void testLockModeLow(PlotBuilder plot) {
        plot.test(helper -> {
            PatternProviderLogicHost host = PatternProviderLockModePlots.getHost(helper);
            PatternProviderLogic pp = host.getLogic();
            helper.startSequence().thenExecuteAfter(1, () -> {
                helper.assertEquals(BlockPos.ZERO, (Object)LockCraftingMode.LOCK_WHILE_LOW, (Object)pp.getCraftingLockedReason());
                helper.check(!PatternProviderLockModePlots.pushPattern(host), "Pushing pattern should fail");
            }).thenExecuteAfter(1, () -> {
                helper.pullLever(LEVER_POS);
                helper.assertEquals(BlockPos.ZERO, (Object)LockCraftingMode.NONE, (Object)pp.getCraftingLockedReason());
                helper.check(PatternProviderLockModePlots.pushPattern(host), "Pushing pattern failed");
                helper.assertEquals(BlockPos.ZERO, (Object)LockCraftingMode.NONE, (Object)pp.getCraftingLockedReason());
                helper.check(PatternProviderLockModePlots.pushPattern(host), "Pushing pattern failed (2nd attempt)");
                helper.pullLever(LEVER_POS);
                helper.assertEquals(BlockPos.ZERO, (Object)LockCraftingMode.LOCK_WHILE_LOW, (Object)pp.getCraftingLockedReason());
                helper.check(!PatternProviderLockModePlots.pushPattern(host), "Pushing pattern should fail");
            }).thenExecuteAfter(1, () -> {
                KeyCounter counter = helper.countContainerContentAt(BlockPos.ZERO.above());
                helper.assertEquals(BlockPos.ZERO.above(), 2L, counter.get(AEItemKey.of((ItemLike)Blocks.OAK_LOG)));
            }).thenSucceed();
        });
    }

    @TestPlot(value="pp_block_lockmode_result")
    public static void testBlockLockModeResult(PlotBuilder plot) {
        PatternProviderLockModePlots.setup(plot, false, LockCraftingMode.LOCK_UNTIL_RESULT);
        PatternProviderLockModePlots.testLockModeResult(plot);
    }

    @TestPlot(value="pp_part_lockmode_result")
    public static void testPartLockModeResult(PlotBuilder plot) {
        PatternProviderLockModePlots.setup(plot, true, LockCraftingMode.LOCK_UNTIL_RESULT);
        PatternProviderLockModePlots.testLockModeResult(plot);
    }

    private static void testLockModeResult(PlotBuilder plot) {
        plot.storageDrive(BlockPos.ZERO.south(), Direction.SOUTH);
        plot.test(helper -> {
            PatternProviderLogicHost host = PatternProviderLockModePlots.getHost(helper);
            PatternProviderLogic pp = host.getLogic();
            helper.startSequence().thenExecuteAfter(1, () -> {
                helper.assertEquals(BlockPos.ZERO, (Object)LockCraftingMode.NONE, (Object)pp.getCraftingLockedReason());
                helper.check(PatternProviderLockModePlots.pushPattern(host), "Pushing pattern should not fail");
                helper.assertEquals(BlockPos.ZERO, (Object)LockCraftingMode.LOCK_UNTIL_RESULT, (Object)pp.getCraftingLockedReason());
                GenericStack expectedResult = TWO_PLANK;
                helper.assertEquals(BlockPos.ZERO, expectedResult, pp.getUnlockStack());
            }).thenExecuteAfter(1, () -> {
                pp.getReturnInv().insert(AEItemKey.of((ItemLike)Items.OAK_PLANKS), 1L, Actionable.SIMULATE, null);
                helper.assertEquals(BlockPos.ZERO, (Object)LockCraftingMode.LOCK_UNTIL_RESULT, (Object)pp.getCraftingLockedReason());
                helper.assertEquals(BlockPos.ZERO, TWO_PLANK, pp.getUnlockStack());
                pp.getReturnInv().insert(AEItemKey.of((ItemLike)Items.OAK_PLANKS), 1L, Actionable.MODULATE, null);
                helper.check(!pp.getReturnInv().isEmpty(), "Items should not be returned to the network immediately");
                helper.assertEquals(BlockPos.ZERO, (Object)LockCraftingMode.LOCK_UNTIL_RESULT, (Object)pp.getCraftingLockedReason());
                helper.assertEquals(BlockPos.ZERO, TWO_PLANK, pp.getUnlockStack());
            }).thenExecuteAfter(1, () -> {
                helper.assertEquals(BlockPos.ZERO, (Object)LockCraftingMode.LOCK_UNTIL_RESULT, (Object)pp.getCraftingLockedReason());
                helper.assertEquals(BlockPos.ZERO, ONE_PLANK, pp.getUnlockStack());
            }).thenExecute(() -> pp.getReturnInv().insert(AEItemKey.of((ItemLike)Items.OAK_PLANKS), 1L, Actionable.MODULATE, null)).thenExecuteAfter(1, () -> {
                helper.assertEquals(BlockPos.ZERO, (Object)LockCraftingMode.NONE, (Object)pp.getCraftingLockedReason());
                helper.assertEquals(BlockPos.ZERO, null, pp.getUnlockStack());
            }).thenExecuteAfter(1, () -> {
                helper.check(PatternProviderLockModePlots.pushPattern(host), "Pushing pattern failed");
                helper.assertEquals(BlockPos.ZERO, (Object)LockCraftingMode.LOCK_UNTIL_RESULT, (Object)pp.getCraftingLockedReason());
                helper.assertEquals(BlockPos.ZERO, TWO_PLANK, pp.getUnlockStack());
            }).thenExecuteAfter(1, () -> {
                KeyCounter counter = helper.countContainerContentAt(BlockPos.ZERO.above());
                helper.assertEquals(BlockPos.ZERO.above(), 2L, counter.get(AEItemKey.of((ItemLike)Blocks.OAK_LOG)));
            }).thenSucceed();
        });
    }

    @TestPlot(value="pp_part_wait_for_pulse_saved")
    public static void testPartWaitForPulseSaved(PlotBuilder plot) {
        PatternProviderLockModePlots.setup(plot, true, LockCraftingMode.LOCK_UNTIL_PULSE);
        PatternProviderLockModePlots.testWaitForPulseSaved(plot);
    }

    @TestPlot(value="pp_block_wait_for_pulse_saved")
    public static void testBlockWaitForPulseSaved(PlotBuilder plot) {
        PatternProviderLockModePlots.setup(plot, false, LockCraftingMode.LOCK_UNTIL_PULSE);
        PatternProviderLockModePlots.testWaitForPulseSaved(plot);
    }

    private static void testWaitForPulseSaved(PlotBuilder plot) {
        plot.test(helper -> {
            PatternProviderLogicHost host = PatternProviderLockModePlots.getHost(helper);
            PatternProviderLogic pp = host.getLogic();
            SavedBlockEntity savedBe = new SavedBlockEntity((PlotTestHelper)((Object)helper));
            helper.startSequence().thenExecute(() -> {
                helper.check(PatternProviderLockModePlots.pushPattern(host), "push pattern should succeed");
                helper.assertEquals(BlockPos.ZERO, (Object)LockCraftingMode.LOCK_UNTIL_PULSE, (Object)pp.getCraftingLockedReason());
                savedBe.saveAndRemove(BlockPos.ZERO);
            }).thenExecuteAfter(1, () -> savedBe.restore()).thenExecuteAfter(1, () -> {
                PatternProviderLogicHost newHost = PatternProviderLockModePlots.getHost(helper);
                PatternProviderLogic newPp = newHost.getLogic();
                helper.check(newPp != pp, "New pattern provider should not be the same as the old one");
                helper.assertEquals(BlockPos.ZERO, (Object)LockCraftingMode.LOCK_UNTIL_PULSE, (Object)newPp.getCraftingLockedReason());
                helper.check(!PatternProviderLockModePlots.pushPattern(newHost), "push pattern should fail");
            }).thenSucceed();
        });
    }

    @TestPlot(value="pp_part_wait_for_result_saved")
    public static void testPartWaitForResultSaved(PlotBuilder plot) {
        PatternProviderLockModePlots.setup(plot, true, LockCraftingMode.LOCK_UNTIL_RESULT);
        PatternProviderLockModePlots.testWaitForResultSaved(plot);
    }

    @TestPlot(value="pp_block_wait_for_result_saved")
    public static void testBlockWaitForResultSaved(PlotBuilder plot) {
        PatternProviderLockModePlots.setup(plot, false, LockCraftingMode.LOCK_UNTIL_RESULT);
        PatternProviderLockModePlots.testWaitForResultSaved(plot);
    }

    private static void testWaitForResultSaved(PlotBuilder plot) {
        plot.test(helper -> {
            PatternProviderLogicHost host = PatternProviderLockModePlots.getHost(helper);
            PatternProviderLogic pp = host.getLogic();
            SavedBlockEntity savedBe = new SavedBlockEntity((PlotTestHelper)((Object)helper));
            helper.startSequence().thenExecute(() -> {
                helper.check(PatternProviderLockModePlots.pushPattern(host), "push pattern should succeed");
                helper.assertEquals(BlockPos.ZERO, (Object)LockCraftingMode.LOCK_UNTIL_RESULT, (Object)pp.getCraftingLockedReason());
                helper.assertEquals(BlockPos.ZERO, TWO_PLANK, pp.getUnlockStack());
                savedBe.save(BlockPos.ZERO);
                helper.destroyBlock(BlockPos.ZERO);
            }).thenExecuteAfter(1, () -> savedBe.restore()).thenExecuteAfter(1, () -> {
                PatternProviderLogicHost newHost = PatternProviderLockModePlots.getHost(helper);
                PatternProviderLogic newPp = newHost.getLogic();
                helper.check(newPp != pp, "New pattern provider should not be the same as the old one");
                helper.assertEquals(BlockPos.ZERO, (Object)LockCraftingMode.LOCK_UNTIL_RESULT, (Object)newPp.getCraftingLockedReason());
                helper.assertEquals(BlockPos.ZERO, TWO_PLANK, pp.getUnlockStack());
                helper.check(!PatternProviderLockModePlots.pushPattern(newHost), "push pattern should fail");
            }).thenSucceed();
        });
    }

    private static boolean pushPattern(PatternProviderLogicHost host) {
        IPatternDetails details = PatternProviderLockModePlots.createPatternDetails(host);
        KeyCounter[] inputs = new KeyCounter[]{new KeyCounter()};
        inputs[0].add(AEItemKey.of((ItemLike)Blocks.OAK_LOG), 1L);
        return host.getLogic().pushPattern(details, inputs);
    }

    private static PatternProviderLogicHost getHost(PlotTestHelper plotTestHelper) {
        BlockEntity be = plotTestHelper.getBlockEntity(BlockPos.ZERO);
        if (be instanceof PatternProviderBlockEntity) {
            PatternProviderBlockEntity host = (PatternProviderBlockEntity)be;
            return host;
        }
        return plotTestHelper.getPart(BlockPos.ZERO, Direction.UP, PatternProviderPart.class);
    }

    private static void setup(PlotBuilder plot, boolean usePart, LockCraftingMode mode) {
        BlockPos origin = BlockPos.ZERO;
        plot.creativeEnergyCell(origin.below());
        if (!usePart) {
            plot.blockEntity(origin, AEBlocks.PATTERN_PROVIDER, host -> PatternProviderLockModePlots.setupPatternProvider(host, mode));
        } else {
            plot.cable(origin).part(Direction.UP, AEParts.PATTERN_PROVIDER, host -> PatternProviderLockModePlots.setupPatternProvider(host, mode)).facade(Direction.WEST, (ItemLike)Blocks.STONE).facade(Direction.EAST, (ItemLike)Blocks.STONE);
        }
        plot.buttonOn(BlockPos.ZERO, Direction.WEST);
        plot.leverOn(BlockPos.ZERO, Direction.EAST);
        plot.chest(origin.above(), new ItemStack[0]);
    }

    private static void setupPatternProvider(PatternProviderLogicHost host, LockCraftingMode mode) {
        PatternProviderLogic pp = host.getLogic();
        pp.getConfigManager().putSetting(Settings.LOCK_CRAFTING_MODE, mode);
        ItemStack pattern = PatternProviderLockModePlots.createPattern();
        pp.getPatternInv().addItems(pattern);
    }

    private static ItemStack createPattern() {
        return PatternDetailsHelper.encodeProcessingPattern(List.of(new GenericStack(AEItemKey.of((ItemLike)Blocks.OAK_LOG), 1L)), List.of(TWO_PLANK));
    }

    private static IPatternDetails createPatternDetails(PatternProviderLogicHost host) {
        return PatternDetailsHelper.decodePattern(PatternProviderLockModePlots.createPattern(), host.getBlockEntity().getLevel());
    }
}

