/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.crafting;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface IPatternDetailsDecoder {
    public boolean isEncodedPattern(ItemStack var1);

    @Nullable
    public IPatternDetails decodePattern(AEItemKey var1, Level var2);

    @Nullable
    default public IPatternDetails decodePattern(ItemStack what, Level level) {
        return this.decodePattern(AEItemKey.of(what), level);
    }
}

