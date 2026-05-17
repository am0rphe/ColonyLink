/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  dev.emi.emi.api.stack.EmiStack
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.integrations.emi;

import appeng.api.stacks.GenericStack;
import dev.emi.emi.api.stack.EmiStack;
import org.jetbrains.annotations.Nullable;

public interface EmiStackConverter {
    public Class<?> getKeyType();

    @Nullable
    public EmiStack toEmiStack(GenericStack var1);

    @Nullable
    public GenericStack toGenericStack(EmiStack var1);
}

