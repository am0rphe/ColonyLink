/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2LongMap$Entry
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.crafting;

import appeng.api.crafting.PatternDetailsTooltip;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import java.util.List;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface IPatternDetails {
    public AEItemKey getDefinition();

    public IInput[] getInputs();

    default public GenericStack getPrimaryOutput() {
        return this.getOutputs().get(0);
    }

    public List<GenericStack> getOutputs();

    default public boolean supportsPushInputsToExternalInventory() {
        return true;
    }

    default public void pushInputsToExternalInventory(KeyCounter[] inputHolder, PatternInputSink inputSink) {
        for (KeyCounter inputList : inputHolder) {
            for (Object2LongMap.Entry<AEKey> input : inputList) {
                inputSink.pushInput((AEKey)input.getKey(), input.getLongValue());
            }
        }
    }

    default public PatternDetailsTooltip getTooltip(Level level, TooltipFlag flags) {
        PatternDetailsTooltip tooltip = new PatternDetailsTooltip(PatternDetailsTooltip.OUTPUT_TEXT_PRODUCES);
        tooltip.addInputsAndOutputs(this);
        return tooltip;
    }

    public static interface PatternInputSink {
        public void pushInput(AEKey var1, long var2);
    }

    public static interface IInput {
        public GenericStack[] getPossibleInputs();

        public long getMultiplier();

        public boolean isValid(AEKey var1, Level var2);

        @Nullable
        public AEKey getRemainingKey(AEKey var1);
    }
}

