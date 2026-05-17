/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  dev.emi.emi.api.recipe.EmiRecipe
 *  dev.emi.emi.api.stack.EmiIngredient
 *  dev.emi.emi.api.stack.EmiStack
 *  org.jetbrains.annotations.Nullable
 */
package appeng.integration.modules.emi;

import appeng.api.integrations.emi.EmiStackConverter;
import appeng.api.integrations.emi.EmiStackConverters;
import appeng.api.stacks.GenericStack;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;

public final class EmiStackHelper {
    private EmiStackHelper() {
    }

    @Nullable
    public static GenericStack toGenericStack(EmiStack emiStack) {
        for (EmiStackConverter converter : EmiStackConverters.getConverters()) {
            GenericStack stack = converter.toGenericStack(emiStack);
            if (stack == null) continue;
            return stack;
        }
        return null;
    }

    @Nullable
    public static EmiStack toEmiStack(GenericStack stack) {
        for (EmiStackConverter converter : EmiStackConverters.getConverters()) {
            EmiStack emiStack = converter.toEmiStack(stack);
            if (emiStack == null) continue;
            return emiStack;
        }
        return null;
    }

    public static List<List<GenericStack>> ofInputs(EmiRecipe emiRecipe) {
        return emiRecipe.getInputs().stream().map(EmiStackHelper::of).toList();
    }

    public static List<GenericStack> ofOutputs(EmiRecipe emiRecipe) {
        return emiRecipe.getOutputs().stream().map(EmiStackHelper::toGenericStack).filter(Objects::nonNull).toList();
    }

    private static List<GenericStack> of(EmiIngredient emiIngredient) {
        if (emiIngredient.isEmpty()) {
            return Collections.emptyList();
        }
        return emiIngredient.getEmiStacks().stream().map(EmiStackHelper::toGenericStack).filter(Objects::nonNull).map(x -> new GenericStack(x.what(), emiIngredient.getAmount())).toList();
    }
}

