/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 */
package appeng.server.testplots;

import appeng.api.ids.AEComponents;
import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.AEItemKey;
import appeng.blockentity.misc.InscriberBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.server.testplots.TestPlotClass;
import appeng.server.testplots.TestPlotCollection;
import appeng.server.testplots.TestPlotGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

@TestPlotClass
public final class InscriberTestPlots {
    private InscriberTestPlots() {
    }

    @TestPlotGenerator
    public static void generateInscriberRecipePlots(TestPlotCollection tests) {
        ItemStack namePlate = AEItems.NAME_PRESS.stack();
        namePlate.set(AEComponents.NAME_PRESS_NAME, (Object)Component.literal((String)"HELLO WORLD"));
        ItemStack ironIngots = new ItemStack((ItemLike)Items.IRON_INGOT, 2);
        ItemStack namedIngots = ironIngots.copy();
        namedIngots.set(DataComponents.CUSTOM_NAME, (Object)Component.literal((String)"HELLO WORLD"));
        InscriberTestPlots.addTest("nameplate", tests, namePlate, ironIngots, ItemStack.EMPTY, namePlate, ItemStack.EMPTY, ItemStack.EMPTY, namedIngots);
        InscriberTestPlots.addPrintTest(tests, AEItems.SILICON, AEItems.SILICON_PRESS, AEItems.SILICON_PRINT);
        InscriberTestPlots.addPrintTest(tests, (ItemLike)Items.GOLD_INGOT, AEItems.LOGIC_PROCESSOR_PRESS, AEItems.LOGIC_PROCESSOR_PRINT);
        InscriberTestPlots.addPrintTest(tests, (ItemLike)Items.DIAMOND, AEItems.ENGINEERING_PROCESSOR_PRESS, AEItems.ENGINEERING_PROCESSOR_PRINT);
        InscriberTestPlots.addPrintTest(tests, AEItems.CERTUS_QUARTZ_CRYSTAL, AEItems.CALCULATION_PROCESSOR_PRESS, AEItems.CALCULATION_PROCESSOR_PRINT);
    }

    private static void addPrintTest(TestPlotCollection tests, ItemLike ingredient, ItemLike press, ItemLike expectedResult) {
        String suffix = AEItemKey.of(ingredient).getId().getPath() + "_print";
        InscriberTestPlots.addTest("inscriber_" + suffix, tests, new ItemStack(press), new ItemStack(ingredient, 2), ItemStack.EMPTY, new ItemStack(press), ItemStack.EMPTY, ItemStack.EMPTY, new ItemStack(expectedResult, 2));
    }

    private static void addTest(String suffix, TestPlotCollection tests, ItemStack topSlot, ItemStack middleSlot, ItemStack bottomSlot, ItemStack expectedTopSlot, ItemStack expectedMiddleSlot, ItemStack expectedBottomSlot, ItemStack expectedResult) {
        tests.add("inscriber_recipe_" + suffix, plot -> {
            plot.creativeEnergyCell(BlockPos.ZERO.below());
            plot.blockEntity(BlockPos.ZERO, AEBlocks.INSCRIBER, be -> {
                be.getUpgrades().addItems(AEItems.SPEED_CARD.stack());
                be.getUpgrades().addItems(AEItems.SPEED_CARD.stack());
                be.getUpgrades().addItems(AEItems.SPEED_CARD.stack());
                be.getUpgrades().addItems(AEItems.SPEED_CARD.stack());
                be.getUpgrades().addItems(AEItems.SPEED_CARD.stack());
                InternalInventory inv = be.getInternalInventory();
                inv.insertItem(0, topSlot.copy(), false);
                inv.insertItem(1, bottomSlot.copy(), false);
                inv.insertItem(2, middleSlot.copy(), false);
            });
        }, plotTestHelper -> plotTestHelper.startSequence().thenWaitUntil(() -> {
            InscriberBlockEntity inscriber = (InscriberBlockEntity)plotTestHelper.getBlockEntity(BlockPos.ZERO);
            InternalInventory inv = inscriber.getInternalInventory();
            plotTestHelper.check(ItemStack.isSameItemSameComponents((ItemStack)inv.getStackInSlot(0), (ItemStack)expectedTopSlot), "Top slot is not as expected", BlockPos.ZERO);
            plotTestHelper.check(ItemStack.isSameItemSameComponents((ItemStack)inv.getStackInSlot(1), (ItemStack)expectedBottomSlot), "Bottom slot is not as expected", BlockPos.ZERO);
            plotTestHelper.check(ItemStack.isSameItemSameComponents((ItemStack)inv.getStackInSlot(2), (ItemStack)expectedMiddleSlot), "Middle slot is not as expected", BlockPos.ZERO);
            plotTestHelper.check(ItemStack.isSameItemSameComponents((ItemStack)inv.getStackInSlot(3), (ItemStack)expectedResult), "Result slot is not as expected", BlockPos.ZERO);
        }).thenSucceed());
    }
}

