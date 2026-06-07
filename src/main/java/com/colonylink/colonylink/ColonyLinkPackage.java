package com.colonylink.colonylink;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * ColonyLink Package — token consommable pour la tab Citizens.
 *
 * Stocké dans la wand (NBT "citizen_packages", int).
 * Chaque action Send ou Craft dans la tab Citizens consomme 1 package côté serveur.
 * Si le stock est épuisé, l'action est bloquée avec un message d'erreur.
 *
 * Recette : 4 Oak Slab (tag #c:slabs/wooden) + 4 Paper disposés en anneau.
 * Stackable 16 — coût modéré, intentionnel.
 */
public class ColonyLinkPackage extends Item
{
    public ColonyLinkPackage()
    {
        super(new Item.Properties().stacksTo(64));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag)
    {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.literal("§8──────────────────"));
        tooltip.add(Component.translatable("colonylink.pkg.line1"));
        tooltip.add(Component.literal("§8──────────────────"));
        tooltip.add(Component.translatable("colonylink.pkg.usage"));
        tooltip.add(Component.translatable("colonylink.pkg.load"));
        tooltip.add(Component.translatable("colonylink.pkg.each"));
        tooltip.add(Component.translatable("colonylink.pkg.tab"));
        tooltip.add(Component.literal("§8──────────────────"));
        tooltip.add(Component.translatable("colonylink.pkg.crafted"));
    }
}