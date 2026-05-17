/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Holder
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.enchantment.Enchantment
 *  net.minecraft.world.item.enchantment.EnchantmentHelper
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package appeng.mixins;

import appeng.hooks.IntrinsicEnchantItem;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={EnchantmentHelper.class})
public class EnchantmentHelperMixin {
    @Inject(at={@At(value="RETURN")}, method={"getItemEnchantmentLevel"}, cancellable=true)
    private static void hookGetItemEnchantmentLevel(Holder<Enchantment> enchantment, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        IntrinsicEnchantItem item;
        int level;
        Item item2;
        if (cir.getReturnValueI() == 0 && (item2 = stack.getItem()) instanceof IntrinsicEnchantItem && (level = (item = (IntrinsicEnchantItem)item2).getIntrinsicEnchantLevel(stack, enchantment)) != 0) {
            cir.setReturnValue((Object)level);
        }
    }
}

