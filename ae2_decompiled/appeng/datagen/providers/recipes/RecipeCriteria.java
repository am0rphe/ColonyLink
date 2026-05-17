/*
 * Decompiled with CFR 0.152.
 */
package appeng.datagen.providers.recipes;

import appeng.core.definitions.BlockDefinition;
import appeng.core.definitions.ItemDefinition;

final class RecipeCriteria {
    private RecipeCriteria() {
    }

    public static String criterionName(BlockDefinition<?> block) {
        return String.format("has_%s", block.id().getPath());
    }

    public static String criterionName(ItemDefinition<?> item) {
        return String.format("has_%s", item.id().getPath());
    }
}

