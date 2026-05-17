/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  me.shedaniel.rei.api.common.display.Display
 *  me.shedaniel.rei.api.common.entry.EntryIngredient
 *  me.shedaniel.rei.api.common.entry.EntryStack
 *  org.jetbrains.annotations.Nullable
 */
package appeng.integration.modules.rei;

import appeng.api.integrations.rei.IngredientConverter;
import appeng.api.integrations.rei.IngredientConverters;
import appeng.api.stacks.GenericStack;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import org.jetbrains.annotations.Nullable;

public final class GenericEntryStackHelper {
    private GenericEntryStackHelper() {
    }

    @Nullable
    public static GenericStack ingredientToStack(EntryStack<?> entryStack) {
        for (IngredientConverter<?> converter : IngredientConverters.getConverters()) {
            GenericStack stack = GenericEntryStackHelper.tryConvertToStack(converter, entryStack);
            if (stack == null) continue;
            return stack;
        }
        return null;
    }

    @Nullable
    private static <T> GenericStack tryConvertToStack(IngredientConverter<T> converter, EntryStack<?> ingredient) {
        if (ingredient.getType() == converter.getIngredientType()) {
            return converter.getStackFromIngredient(ingredient.cast());
        }
        return null;
    }

    public static List<List<GenericStack>> ofInputs(Display display) {
        return display.getInputEntries().stream().map(GenericEntryStackHelper::of).toList();
    }

    public static List<GenericStack> ofOutputs(Display display) {
        return display.getOutputEntries().stream().map(entryIngredient -> entryIngredient.stream().map(GenericEntryStackHelper::ingredientToStack).filter(Objects::nonNull).findFirst().orElse(null)).filter(Objects::nonNull).toList();
    }

    private static List<GenericStack> of(EntryIngredient entryIngredient) {
        if (entryIngredient.isEmpty()) {
            return Collections.emptyList();
        }
        return entryIngredient.stream().map(GenericEntryStackHelper::ingredientToStack).filter(Objects::nonNull).toList();
    }
}

