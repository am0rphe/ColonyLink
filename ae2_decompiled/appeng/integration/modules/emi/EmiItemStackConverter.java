/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  dev.emi.emi.api.stack.EmiStack
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  org.jetbrains.annotations.Nullable
 */
package appeng.integration.modules.emi;

import appeng.api.integrations.emi.EmiStackConverter;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

class EmiItemStackConverter
implements EmiStackConverter {
    EmiItemStackConverter() {
    }

    @Override
    public Class<?> getKeyType() {
        return Item.class;
    }

    @Override
    @Nullable
    public EmiStack toEmiStack(GenericStack stack) {
        AEKey aEKey = stack.what();
        if (aEKey instanceof AEItemKey) {
            AEItemKey itemKey = (AEItemKey)aEKey;
            return EmiStack.of((ItemStack)itemKey.getReadOnlyStack()).setAmount(stack.amount());
        }
        return null;
    }

    @Override
    @Nullable
    public GenericStack toGenericStack(EmiStack stack) {
        Item item = (Item)stack.getKeyOfType(Item.class);
        if (item != null && item != Items.AIR) {
            AEItemKey itemKey = AEItemKey.of(stack.getItemStack());
            return new GenericStack(itemKey, stack.getAmount());
        }
        return null;
    }
}

