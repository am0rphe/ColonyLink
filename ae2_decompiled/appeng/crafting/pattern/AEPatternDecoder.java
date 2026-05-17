/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.Nullable
 */
package appeng.crafting.pattern;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.IPatternDetailsDecoder;
import appeng.api.stacks.AEItemKey;
import appeng.crafting.pattern.EncodedPatternItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class AEPatternDecoder
implements IPatternDetailsDecoder {
    public static final AEPatternDecoder INSTANCE = new AEPatternDecoder();

    @Override
    public boolean isEncodedPattern(ItemStack stack) {
        return stack.getItem() instanceof EncodedPatternItem;
    }

    @Override
    @Nullable
    public IPatternDetails decodePattern(AEItemKey what, Level level) {
        Item item;
        if (level == null || what == null || !((item = what.getItem()) instanceof EncodedPatternItem)) {
            return null;
        }
        EncodedPatternItem encodedPatternItem = (EncodedPatternItem)item;
        return encodedPatternItem.decode(what, level);
    }
}

