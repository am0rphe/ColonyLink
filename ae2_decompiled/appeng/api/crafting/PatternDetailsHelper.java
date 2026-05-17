/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Function
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.CraftingRecipe
 *  net.minecraft.world.item.crafting.RecipeHolder
 *  net.minecraft.world.item.crafting.SmithingRecipe
 *  net.minecraft.world.item.crafting.StonecutterRecipe
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.crafting;

import appeng.api.crafting.EncodedPatternDecoder;
import appeng.api.crafting.EncodedPatternItemBuilder;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.IPatternDetailsDecoder;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.core.definitions.AEItems;
import appeng.crafting.pattern.AECraftingPattern;
import appeng.crafting.pattern.AEPatternDecoder;
import appeng.crafting.pattern.AEProcessingPattern;
import appeng.crafting.pattern.AESmithingTablePattern;
import appeng.crafting.pattern.AEStonecuttingPattern;
import com.google.common.base.Function;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class PatternDetailsHelper {
    private static final List<IPatternDetailsDecoder> DECODERS = new CopyOnWriteArrayList<IPatternDetailsDecoder>();

    public static void registerDecoder(IPatternDetailsDecoder decoder) {
        Objects.requireNonNull(decoder);
        DECODERS.add(decoder);
    }

    public static <T extends IPatternDetails> EncodedPatternItemBuilder<T> encodedPatternItemBuilder(EncodedPatternDecoder<T> decoder) {
        return new EncodedPatternItemBuilder<T>(decoder);
    }

    public static <T extends IPatternDetails> EncodedPatternItemBuilder<T> encodedPatternItemBuilder(Function<AEItemKey, T> decoder) {
        return new EncodedPatternItemBuilder<IPatternDetails>((what, level) -> (IPatternDetails)decoder.apply((Object)what));
    }

    public static boolean isEncodedPattern(ItemStack stack) {
        for (IPatternDetailsDecoder decoder : DECODERS) {
            if (!decoder.isEncodedPattern(stack)) continue;
            return true;
        }
        return false;
    }

    @Nullable
    public static IPatternDetails decodePattern(AEItemKey what, Level level) {
        for (IPatternDetailsDecoder decoder : DECODERS) {
            IPatternDetails decoded = decoder.decodePattern(what, level);
            if (decoded == null) continue;
            return decoded;
        }
        return null;
    }

    @Nullable
    public static IPatternDetails decodePattern(ItemStack stack, Level level) {
        for (IPatternDetailsDecoder decoder : DECODERS) {
            IPatternDetails decoded = decoder.decodePattern(stack, level);
            if (decoded == null) continue;
            return decoded;
        }
        return null;
    }

    public static ItemStack encodeProcessingPattern(List<GenericStack> sparseInputs, List<GenericStack> sparseOutputs) {
        ItemStack stack = AEItems.PROCESSING_PATTERN.stack();
        AEProcessingPattern.encode(stack, sparseInputs, sparseOutputs);
        return stack;
    }

    public static ItemStack encodeCraftingPattern(RecipeHolder<CraftingRecipe> recipe, ItemStack[] in, ItemStack out, boolean allowSubstitutes, boolean allowFluidSubstitutes) {
        ItemStack stack = AEItems.CRAFTING_PATTERN.stack();
        AECraftingPattern.encode(stack, recipe, in, out, allowSubstitutes, allowFluidSubstitutes);
        return stack;
    }

    public static ItemStack encodeStonecuttingPattern(RecipeHolder<StonecutterRecipe> recipe, AEItemKey in, AEItemKey out, boolean allowSubstitutes) {
        ItemStack stack = AEItems.STONECUTTING_PATTERN.stack();
        AEStonecuttingPattern.encode(stack, recipe, in, out, allowSubstitutes);
        return stack;
    }

    public static ItemStack encodeSmithingTablePattern(RecipeHolder<SmithingRecipe> recipe, AEItemKey template, AEItemKey base, AEItemKey addition, AEItemKey out, boolean allowSubstitutes) {
        ItemStack stack = AEItems.SMITHING_TABLE_PATTERN.stack();
        AESmithingTablePattern.encode(stack, recipe, template, base, addition, out, allowSubstitutes);
        return stack;
    }

    static {
        PatternDetailsHelper.registerDecoder(AEPatternDecoder.INSTANCE);
    }
}

