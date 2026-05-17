/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.primitives.Ints
 *  me.shedaniel.rei.api.common.entry.EntryStack
 *  me.shedaniel.rei.api.common.entry.type.EntryType
 *  me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes
 *  net.minecraft.world.item.ItemStack
 *  org.jetbrains.annotations.Nullable
 */
package appeng.integration.modules.rei;

import appeng.api.integrations.rei.IngredientConverter;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import com.google.common.primitives.Ints;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ItemIngredientConverter
implements IngredientConverter<ItemStack> {
    @Override
    public EntryType<ItemStack> getIngredientType() {
        return VanillaEntryTypes.ITEM;
    }

    @Override
    @Nullable
    public EntryStack<ItemStack> getIngredientFromStack(GenericStack stack) {
        AEKey aEKey = stack.what();
        if (aEKey instanceof AEItemKey) {
            AEItemKey itemKey = (AEItemKey)aEKey;
            return EntryStack.of(this.getIngredientType(), (Object)itemKey.toStack(Math.max(1, Ints.saturatedCast((long)stack.amount()))));
        }
        return null;
    }

    @Override
    @Nullable
    public GenericStack getStackFromIngredient(EntryStack<ItemStack> ingredient) {
        ItemStack itemStack;
        AEItemKey itemKey;
        if (ingredient.getType() == this.getIngredientType() && (itemKey = AEItemKey.of(itemStack = (ItemStack)ingredient.castValue())) != null) {
            return new GenericStack(itemKey, itemStack.getCount());
        }
        return null;
    }
}

