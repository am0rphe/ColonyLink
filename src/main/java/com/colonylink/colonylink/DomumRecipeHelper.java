package com.colonylink.colonylink;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/**
 * DomumRecipeHelper — ColonyLink v1.4.2
 *
 * Retourne le count d'output de la recette Architect's Cutter de Domum.
 * Basé sur les className réels observés en jeu :
 *
 *   shingleblock       → 4   (shingles)
 *   panelblock         → 4   (panels)
 *   timberframeblock   → 4   (timber frames)
 *   framedlightblock   → 2   (lights)
 *   fancydoorblock     → 2   (fancy doors, 2-material)
 *   fancytrapdoorblock → 2   (fancy trapdoors, 2-material)
 *   paperwallblock     → 6   (framed panes / paper walls)
 *   everything else    → 1
 */
public class DomumRecipeHelper
{
    public static int getOutputCount(ItemStack domumStack, ServerLevel level)
    {
        if (domumStack.isEmpty()) return 1;
        if (!(domumStack.getItem() instanceof BlockItem bi)) return 1;

        Block block = bi.getBlock();
        String cls = block.getClass().getSimpleName().toLowerCase();

        return switch (cls)
        {
            case "shingleblock"       -> 4;
            case "panelblock"         -> 4;
            case "timberframeblock"   -> 4;
            case "framedlightblock"   -> 2;
            case "fancydoorblock"     -> 2;
            case "fancytrapdoorblock" -> 2;
            case "paperwallblock"     -> 6;
            default                   -> 1;
        };
    }
}