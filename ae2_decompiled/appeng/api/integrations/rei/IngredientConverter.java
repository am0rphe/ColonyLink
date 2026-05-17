/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  me.shedaniel.rei.api.common.entry.EntryStack
 *  me.shedaniel.rei.api.common.entry.type.EntryType
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.integrations.rei;

import appeng.api.stacks.GenericStack;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import org.jetbrains.annotations.Nullable;

public interface IngredientConverter<T> {
    public EntryType<T> getIngredientType();

    @Nullable
    public EntryStack<T> getIngredientFromStack(GenericStack var1);

    @Nullable
    public GenericStack getStackFromIngredient(EntryStack<T> var1);
}

