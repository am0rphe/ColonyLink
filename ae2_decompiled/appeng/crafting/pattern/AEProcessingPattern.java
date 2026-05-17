/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.Nullable
 */
package appeng.crafting.pattern;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsTooltip;
import appeng.api.ids.AEComponents;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.pattern.AEPatternHelper;
import appeng.crafting.pattern.EncodedProcessingPattern;
import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class AEProcessingPattern
implements IPatternDetails {
    public static final int MAX_INPUT_SLOTS = 81;
    public static final int MAX_OUTPUT_SLOTS = 27;
    private final AEItemKey definition;
    private final List<GenericStack> sparseInputs;
    private final List<GenericStack> sparseOutputs;
    private final Input[] inputs;
    private final List<GenericStack> condensedOutputs;

    public AEProcessingPattern(AEItemKey definition) {
        this.definition = definition;
        EncodedProcessingPattern encodedPattern = definition.get(AEComponents.ENCODED_PROCESSING_PATTERN);
        if (encodedPattern == null) {
            throw new IllegalArgumentException("Given item does not encode a processing pattern: " + String.valueOf(definition));
        }
        if (encodedPattern.containsMissingContent()) {
            throw new IllegalArgumentException("Pattern references missing content");
        }
        this.sparseInputs = encodedPattern.sparseInputs();
        this.sparseOutputs = encodedPattern.sparseOutputs();
        List<GenericStack> condensedInputs = AEPatternHelper.condenseStacks(this.sparseInputs);
        this.inputs = new Input[condensedInputs.size()];
        for (int i = 0; i < this.inputs.length; ++i) {
            this.inputs[i] = new Input(condensedInputs.get(i));
        }
        this.condensedOutputs = AEPatternHelper.condenseStacks(this.sparseOutputs);
    }

    public static void encode(ItemStack stack, List<GenericStack> sparseInputs, List<GenericStack> sparseOutputs) {
        if (sparseInputs.stream().noneMatch(Objects::nonNull)) {
            throw new IllegalArgumentException("At least one input must be non-null.");
        }
        Objects.requireNonNull(sparseOutputs.get(0), "The first (primary) output must be non-null.");
        stack.set(AEComponents.ENCODED_PROCESSING_PATTERN, (Object)new EncodedProcessingPattern(sparseInputs, sparseOutputs));
    }

    public int hashCode() {
        return this.definition.hashCode();
    }

    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == this.getClass() && ((AEProcessingPattern)obj).definition.equals(this.definition);
    }

    @Override
    public AEItemKey getDefinition() {
        return this.definition;
    }

    @Override
    public IPatternDetails.IInput[] getInputs() {
        return this.inputs;
    }

    @Override
    public List<GenericStack> getOutputs() {
        return this.condensedOutputs;
    }

    public List<GenericStack> getSparseInputs() {
        return this.sparseInputs;
    }

    public List<GenericStack> getSparseOutputs() {
        return this.sparseOutputs;
    }

    @Override
    public void pushInputsToExternalInventory(KeyCounter[] inputHolder, IPatternDetails.PatternInputSink inputSink) {
        if (this.sparseInputs.size() == this.inputs.length) {
            IPatternDetails.super.pushInputsToExternalInventory(inputHolder, inputSink);
            return;
        }
        KeyCounter allInputs = new KeyCounter();
        for (KeyCounter counter : inputHolder) {
            allInputs.addAll(counter);
        }
        for (GenericStack sparseInput : this.sparseInputs) {
            if (sparseInput == null) continue;
            AEKey key = sparseInput.what();
            long amount = sparseInput.amount();
            long available = allInputs.get(key);
            if (available < amount) {
                throw new RuntimeException("Expected at least %d of %s when pushing pattern, but only %d available".formatted(amount, key, available));
            }
            inputSink.pushInput(key, amount);
            allInputs.remove(key, amount);
        }
    }

    public static PatternDetailsTooltip getInvalidPatternTooltip(ItemStack stack, Level level, @Nullable Exception cause, TooltipFlag flags) {
        PatternDetailsTooltip tooltip = new PatternDetailsTooltip(PatternDetailsTooltip.OUTPUT_TEXT_PRODUCES);
        EncodedProcessingPattern encodedPattern = (EncodedProcessingPattern)stack.get(AEComponents.ENCODED_PROCESSING_PATTERN);
        if (encodedPattern != null) {
            encodedPattern.sparseInputs().stream().filter(Objects::nonNull).forEach(tooltip::addInput);
            encodedPattern.sparseOutputs().stream().filter(Objects::nonNull).forEach(tooltip::addOutput);
        }
        return tooltip;
    }

    private static ListTag encodeStackList(GenericStack[] stacks, HolderLookup.Provider registries) {
        ListTag tag = new ListTag();
        boolean foundStack = false;
        for (GenericStack stack : stacks) {
            tag.add((Object)GenericStack.writeTag(registries, stack));
            if (stack == null || stack.amount() <= 0L) continue;
            foundStack = true;
        }
        Preconditions.checkArgument((boolean)foundStack, (Object)"List passed to pattern must contain at least one stack.");
        return tag;
    }

    private static class Input
    implements IPatternDetails.IInput {
        private final GenericStack[] template;
        private final long multiplier;

        private Input(GenericStack stack) {
            this.template = new GenericStack[]{new GenericStack(stack.what(), 1L)};
            this.multiplier = stack.amount();
        }

        @Override
        public GenericStack[] getPossibleInputs() {
            return this.template;
        }

        @Override
        public long getMultiplier() {
            return this.multiplier;
        }

        @Override
        public boolean isValid(AEKey input, Level level) {
            return input.matches(this.template[0]);
        }

        @Override
        @Nullable
        public AEKey getRemainingKey(AEKey template) {
            return null;
        }
    }
}

