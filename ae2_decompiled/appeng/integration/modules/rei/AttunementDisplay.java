/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  me.shedaniel.rei.api.common.category.CategoryIdentifier
 *  me.shedaniel.rei.api.common.display.basic.BasicDisplay
 *  me.shedaniel.rei.api.common.entry.EntryIngredient
 *  me.shedaniel.rei.api.common.entry.EntryStack
 *  net.minecraft.network.chat.Component
 */
package appeng.integration.modules.rei;

import appeng.integration.modules.rei.AttunementCategory;
import java.util.List;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.minecraft.network.chat.Component;

public class AttunementDisplay
extends BasicDisplay {
    public AttunementDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Component ... description) {
        super(inputs, outputs);
        for (EntryIngredient input : inputs) {
            for (EntryStack entry : input) {
                entry.tooltip(description);
            }
        }
    }

    public CategoryIdentifier<?> getCategoryIdentifier() {
        return AttunementCategory.ID;
    }
}

