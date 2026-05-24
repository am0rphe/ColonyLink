package com.colonylink.colonylink;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.IItemDecorator;

/**
 * DomumPatternDecorator — ColonyLink v1.4.2
 *
 * IItemDecorator pour DomumPatternItem.
 * Quand Shift est enfoncé : rend l'item Domum cible PAR-DESSUS le pattern
 * dans le slot, à la position exacte (x, y identiques = pas de décalage).
 *
 * Enregistré via RegisterItemDecorationsEvent dans ColonyLinkClient.
 * Fonctionne dans TOUS les slots : inventaire, hotbar, AE2, WH, etc.
 */
@OnlyIn(Dist.CLIENT)
public class DomumPatternDecorator implements IItemDecorator
{
    public static final DomumPatternDecorator INSTANCE = new DomumPatternDecorator();
    private DomumPatternDecorator() {}

    @Override
    public boolean render(GuiGraphics graphics, Font font,
                          ItemStack stack, int x, int y)
    {
        if (!(stack.getItem() instanceof DomumPatternItem)) return false;
        if (!Screen.hasShiftDown()) return false;

        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.level == null) return false;

        ItemStack target = DomumPatternItem.decodeTarget(
                stack, mc.level.registryAccess());
        if (target == null || target.isEmpty()) return false;

        // Pousse le z-level très haut pour passer au-dessus du rendu pattern
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 300);

        // Fond couleur slot vanilla (#8B8B8B) pour masquer le pattern
        graphics.fill(x, y, x + 16, y + 16, 0xFF8B8B8B);

        // Rend l'item cible au même z-level élevé
        graphics.renderItem(target, x, y);
        graphics.renderItemDecorations(font, target, x, y, null);

        graphics.pose().popPose();

        return false;
    }
}