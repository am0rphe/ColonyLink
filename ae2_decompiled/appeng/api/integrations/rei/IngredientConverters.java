/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 */
package appeng.api.integrations.rei;

import appeng.api.integrations.rei.IngredientConverter;
import com.google.common.collect.ImmutableList;
import java.util.List;

public final class IngredientConverters {
    private static List<IngredientConverter<?>> converters = ImmutableList.of();

    private IngredientConverters() {
    }

    public static synchronized boolean register(IngredientConverter<?> converter) {
        for (IngredientConverter<?> existingConverter : converters) {
            if (existingConverter.getIngredientType() != converter.getIngredientType()) continue;
            return false;
        }
        converters = ImmutableList.builder().addAll(converters).add(converter).build();
        return true;
    }

    public static synchronized List<IngredientConverter<?>> getConverters() {
        return converters;
    }
}

