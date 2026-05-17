/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Blocks
 */
package appeng.server.testplots;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.ids.AEComponents;
import appeng.blockentity.storage.SkyChestBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.crafting.pattern.EncodedCraftingPattern;
import appeng.server.testplots.CraftingPatternHelper;
import appeng.server.testplots.TestPlot;
import appeng.server.testplots.TestPlotClass;
import appeng.server.testworld.PlotBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

@TestPlotClass
public class InvalidPatternTestPlot {
    @TestPlot(value="pattern_invalid_recipe_id")
    public static void patternInvalidRecipeId(PlotBuilder builder) {
        builder.blockEntity(BlockPos.ZERO, AEBlocks.SMOOTH_SKY_STONE_CHEST, chest -> {
            ItemStack oakLog = Blocks.OAK_LOG.asItem().getDefaultInstance();
            ItemStack pattern = CraftingPatternHelper.encodeShapelessCraftingRecipe(chest.getLevel(), oakLog);
            EncodedCraftingPattern encodedPattern = (EncodedCraftingPattern)pattern.get(AEComponents.ENCODED_CRAFTING_PATTERN);
            pattern.set(AEComponents.ENCODED_CRAFTING_PATTERN, (Object)new EncodedCraftingPattern(encodedPattern.inputs(), encodedPattern.result(), ResourceLocation.parse((String)"invalid"), encodedPattern.canSubstitute(), encodedPattern.canSubstituteFluids()));
            chest.getInternalInventory().addItems(pattern);
        });
        builder.test(helper -> {
            SkyChestBlockEntity chest = (SkyChestBlockEntity)helper.getBlockEntity(BlockPos.ZERO);
            ItemStack pattern = chest.getInternalInventory().getStackInSlot(0);
            helper.check(!pattern.isEmpty(), "pattern should be present");
            IPatternDetails details = PatternDetailsHelper.decodePattern(pattern, (Level)helper.getLevel());
            helper.check(details == null, "pattern should fail decoding");
            helper.succeed();
        });
    }
}

