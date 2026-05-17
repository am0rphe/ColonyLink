/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterables
 *  it.unimi.dsi.fastutil.objects.Object2LongMap$Entry
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.Nullable
 */
package appeng.crafting.execution;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.crafting.execution.InputTemplate;
import appeng.crafting.inv.ICraftingInventory;
import appeng.crafting.inv.ListCraftingInventory;
import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import java.util.ArrayList;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class CraftingCpuHelper {
    @Nullable
    public static GenericStack tryExtractInitialItems(ICraftingPlan plan, IGrid grid, ListCraftingInventory cpuInventory, IActionSource src) {
        MEStorage storage = grid.getStorageService().getInventory();
        for (Object2LongMap.Entry<AEKey> entry : plan.usedItems()) {
            AEKey what = (AEKey)entry.getKey();
            long toExtract = entry.getLongValue();
            long extracted = storage.extract(what, toExtract, Actionable.MODULATE, src);
            cpuInventory.insert(what, extracted, Actionable.MODULATE);
            if (extracted >= toExtract) continue;
            for (Object2LongMap.Entry<AEKey> stored : cpuInventory.list) {
                storage.insert((AEKey)stored.getKey(), stored.getLongValue(), Actionable.MODULATE, src);
            }
            cpuInventory.clear();
            return new GenericStack(what, toExtract - extracted);
        }
        return null;
    }

    public static CompoundTag generateLinkData(UUID craftId, boolean standalone, boolean req) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("craftId", craftId);
        tag.putBoolean("canceled", false);
        tag.putBoolean("done", false);
        tag.putBoolean("standalone", standalone);
        tag.putBoolean("req", req);
        return tag;
    }

    public static double calculatePatternPower(KeyCounter[] craftingContainer) {
        double sum = 0.0;
        for (KeyCounter itemHolder : craftingContainer) {
            for (Object2LongMap.Entry<AEKey> anInput : itemHolder) {
                sum += (double)anInput.getLongValue() / (double)((AEKey)anInput.getKey()).getAmountPerOperation();
            }
        }
        return sum;
    }

    @Nullable
    public static KeyCounter[] extractPatternInputs(IPatternDetails details, ICraftingInventory sourceInv, Level level, KeyCounter expectedOutputs, KeyCounter expectedContainerItems) {
        IPatternDetails.IInput[] inputs = details.getInputs();
        KeyCounter[] inputHolder = new KeyCounter[inputs.length];
        boolean found = true;
        for (int x = 0; x < inputs.length; ++x) {
            KeyCounter list = inputHolder[x] = new KeyCounter();
            long remainingMultiplier = inputs[x].getMultiplier();
            for (InputTemplate template : CraftingCpuHelper.getValidItemTemplates(sourceInv, inputs[x], level)) {
                long extracted = CraftingCpuHelper.extractTemplates(sourceInv, template, remainingMultiplier);
                list.add(template.key(), extracted * template.amount());
                AEKey containerItem = inputs[x].getRemainingKey(template.key());
                if (containerItem != null) {
                    expectedContainerItems.add(containerItem, extracted);
                }
                if ((remainingMultiplier -= extracted) != 0L) continue;
                break;
            }
            if (remainingMultiplier <= 0L) continue;
            found = false;
            break;
        }
        if (!found) {
            CraftingCpuHelper.reinjectPatternInputs(sourceInv, inputHolder);
            return null;
        }
        for (GenericStack output : details.getOutputs()) {
            expectedOutputs.add(output.what(), output.amount());
        }
        return inputHolder;
    }

    public static void reinjectPatternInputs(ICraftingInventory sourceInv, KeyCounter[] inputHolder) {
        for (KeyCounter list : inputHolder) {
            if (list == null) continue;
            for (Object2LongMap.Entry<AEKey> entry : list) {
                sourceInv.insert((AEKey)entry.getKey(), entry.getLongValue(), Actionable.MODULATE);
            }
        }
    }

    public static Iterable<InputTemplate> getValidItemTemplates(ICraftingInventory inv, IPatternDetails.IInput input, Level level) {
        GenericStack[] possibleInputs = input.getPossibleInputs();
        ArrayList<InputTemplate> substitutes = new ArrayList<InputTemplate>(possibleInputs.length);
        for (GenericStack stack2 : possibleInputs) {
            for (AEKey fuzz : inv.findFuzzyTemplates(stack2.what())) {
                substitutes.add(new InputTemplate(fuzz, stack2.amount()));
            }
        }
        return Iterables.filter(substitutes, stack -> input.isValid(stack.key(), level));
    }

    public static long extractTemplates(ICraftingInventory inv, InputTemplate template, long multiplier) {
        long maxTotal = template.amount() * multiplier;
        long extracted = inv.extract(template.key(), maxTotal, Actionable.SIMULATE);
        if (extracted == 0L) {
            return 0L;
        }
        multiplier = extracted / template.amount();
        maxTotal = template.amount() * multiplier;
        if (maxTotal == 0L) {
            return 0L;
        }
        extracted = inv.extract(template.key(), maxTotal, Actionable.MODULATE);
        if (extracted == 0L || extracted != maxTotal) {
            throw new IllegalStateException("Failed to correctly extract whole number. Invalid simulation!");
        }
        return multiplier;
    }

    private CraftingCpuHelper() {
    }
}

