/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 */
package appeng.integration.modules.itemlists;

import appeng.core.localization.ItemModText;
import appeng.menu.me.items.CraftingTermMenu;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class TransferHelper {
    public static final int BLUE_SLOT_HIGHLIGHT_COLOR = 0x400000FF;
    public static final int RED_SLOT_HIGHLIGHT_COLOR = 0x66FF0000;
    public static final int BLUE_PLUS_BUTTON_COLOR = -2142943745;
    public static final int ORANGE_PLUS_BUTTON_COLOR = -2130729728;

    public static List<Component> createCraftingTooltip(CraftingTermMenu.MissingIngredientSlots missingSlots, boolean craftMissing, boolean withTitle) {
        ArrayList<Component> tooltip = new ArrayList<Component>();
        if (withTitle) {
            tooltip.add((Component)ItemModText.MOVE_ITEMS.text());
        }
        if (missingSlots.anyCraftable()) {
            if (craftMissing) {
                tooltip.add((Component)ItemModText.WILL_CRAFT.text().withStyle(ChatFormatting.BLUE));
            } else {
                tooltip.add((Component)ItemModText.CTRL_CLICK_TO_CRAFT.text().withStyle(ChatFormatting.BLUE));
            }
        }
        if (missingSlots.anyMissing()) {
            tooltip.add((Component)ItemModText.MISSING_ITEMS.text().withStyle(ChatFormatting.RED));
        }
        return tooltip;
    }

    public static List<Component> createEncodingTooltip(boolean hasEncoded, boolean withTitle) {
        ArrayList<Component> tooltip = new ArrayList<Component>();
        if (withTitle) {
            tooltip.add((Component)ItemModText.ENCODE_PATTERN.text());
        }
        if (hasEncoded) {
            tooltip.add((Component)ItemModText.HAS_ENCODED_INGREDIENTS.text().withStyle(ChatFormatting.BLUE));
        }
        return tooltip;
    }
}

