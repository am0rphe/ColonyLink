/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Holder
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.HolderLookup$RegistryLookup
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.item.Item$TooltipContext
 *  net.minecraft.world.item.enchantment.Enchantment
 */
package appeng.items.tools.fluix;

import appeng.core.localization.GuiText;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;

final class IntrinsicEnchantment {
    private final ResourceKey<Enchantment> enchantment;
    private final int level;

    public IntrinsicEnchantment(ResourceKey<Enchantment> enchantment, int level) {
        this.enchantment = enchantment;
        this.level = level;
    }

    public void appendHoverText(Item.TooltipContext context, List<Component> tooltipComponents) {
        HolderLookup.Provider registries = context.registries();
        if (registries == null) {
            return;
        }
        HolderLookup.RegistryLookup registrylookup = registries.lookupOrThrow(Registries.ENCHANTMENT);
        registrylookup.get(this.enchantment).ifPresent(holder -> tooltipComponents.add((Component)GuiText.IntrinsicEnchant.text(Enchantment.getFullname((Holder)holder, (int)this.level))));
    }

    public int getLevel(Holder<Enchantment> enchantment) {
        return enchantment.is(this.enchantment) ? this.level : 0;
    }
}

