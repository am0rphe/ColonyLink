/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.item.ItemStack
 *  org.jetbrains.annotations.Nullable
 */
package appeng.util.helpers;

import appeng.api.config.FuzzyMode;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class ItemComparisonHelper {
    private ItemComparisonHelper() {
    }

    public static boolean isEqualItemType(ItemStack that, ItemStack other) {
        return !that.isEmpty() && !other.isEmpty() && that.getItem() == other.getItem();
    }

    public boolean isNbtTagEqual(@Nullable CompoundTag left, @Nullable CompoundTag right) {
        boolean isRightEmpty;
        if (left == right) {
            return true;
        }
        boolean isLeftEmpty = left == null || left.isEmpty();
        boolean bl = isRightEmpty = right == null || right.isEmpty();
        if (isLeftEmpty && isRightEmpty) {
            return true;
        }
        if (isLeftEmpty != isRightEmpty) {
            return false;
        }
        return left.equals((Object)right);
    }

    public static boolean isFuzzyEqualItem(ItemStack a, ItemStack b, FuzzyMode mode) {
        if (a.isEmpty() && b.isEmpty()) {
            return true;
        }
        if (a.isEmpty() || b.isEmpty()) {
            return false;
        }
        if (a.getItem() == b.getItem() && a.isDamageableItem()) {
            if (mode == FuzzyMode.IGNORE_ALL) {
                return true;
            }
            if (mode == FuzzyMode.PERCENT_99) {
                return a.getDamageValue() > 0 == b.getDamageValue() > 0;
            }
            float percentDamagedOfA = (float)a.getDamageValue() / (float)a.getMaxDamage();
            float percentDamagedOfB = (float)b.getDamageValue() / (float)b.getMaxDamage();
            return percentDamagedOfA > mode.breakPoint == percentDamagedOfB > mode.breakPoint;
        }
        return ItemStack.isSameItem((ItemStack)a, (ItemStack)b);
    }
}

