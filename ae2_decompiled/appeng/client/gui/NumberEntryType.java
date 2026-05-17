/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package appeng.client.gui;

import appeng.api.config.PowerUnit;
import appeng.api.stacks.AEKey;
import org.jetbrains.annotations.Nullable;

public record NumberEntryType(int amountPerUnit, @Nullable String unit) {
    public static final NumberEntryType ENERGY = new NumberEntryType(1, PowerUnit.AE.getSymbolName());
    public static final NumberEntryType UNITLESS = new NumberEntryType(1, null);

    public static NumberEntryType of(@Nullable AEKey key) {
        if (key == null) {
            return UNITLESS;
        }
        return new NumberEntryType(key.getAmountPerUnit(), key.getUnitSymbol());
    }
}

