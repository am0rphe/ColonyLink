/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.Level
 */
package appeng.api.crafting;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import net.minecraft.world.level.Level;

@FunctionalInterface
public interface EncodedPatternDecoder<T extends IPatternDetails> {
    public T decode(AEItemKey var1, Level var2);
}

