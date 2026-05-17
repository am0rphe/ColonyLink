/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Holder
 *  net.minecraft.core.HolderLookup$RegistryLookup
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.CreativeModeTab$ItemDisplayParameters
 *  net.minecraft.world.item.CreativeModeTab$Output
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.Item$TooltipContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.item.enchantment.Enchantments
 *  net.minecraft.world.item.enchantment.ItemEnchantments
 *  net.minecraft.world.item.enchantment.ItemEnchantments$Mutable
 *  net.minecraft.world.level.ItemLike
 */
package appeng.parts.automation;

import appeng.core.localization.GuiText;
import appeng.core.localization.Tooltips;
import appeng.items.parts.PartItem;
import appeng.parts.automation.AnnihilationPlanePart;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.ItemLike;

public class AnnihilationPlanePartItem
extends PartItem<AnnihilationPlanePart> {
    public AnnihilationPlanePartItem(Item.Properties properties) {
        super(properties, AnnihilationPlanePart.class, AnnihilationPlanePart::new);
    }

    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    public int getEnchantmentValue() {
        return 10;
    }

    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return true;
    }

    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> lines, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, context, lines, isAdvanced);
        ItemEnchantments enchantments = (ItemEnchantments)stack.getOrDefault(DataComponents.ENCHANTMENTS, (Object)ItemEnchantments.EMPTY);
        if (enchantments.isEmpty()) {
            lines.add((Component)Tooltips.of(GuiText.CanBeEnchanted, new Object[0]));
        } else {
            lines.add((Component)Tooltips.of(GuiText.IncreasedEnergyUseFromEnchants, new Object[0]));
        }
    }

    @Override
    public void addToMainCreativeTab(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        super.addToMainCreativeTab(parameters, output);
        HolderLookup.RegistryLookup enchantmentRegistry = parameters.holders().lookupOrThrow(Registries.ENCHANTMENT);
        ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        enchantments.set((Holder)enchantmentRegistry.getOrThrow(Enchantments.SILK_TOUCH), 1);
        ItemStack silkTouch = new ItemStack((ItemLike)this);
        silkTouch.set(DataComponents.ENCHANTMENTS, (Object)enchantments.toImmutable());
        output.accept(silkTouch);
    }
}

