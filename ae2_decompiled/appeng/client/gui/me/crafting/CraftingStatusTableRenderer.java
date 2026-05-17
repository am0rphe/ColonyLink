/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package appeng.client.gui.me.crafting;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AmountFormat;
import appeng.api.util.AEColor;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.me.crafting.AbstractTableRenderer;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import appeng.menu.me.crafting.CraftingStatusEntry;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;

public class CraftingStatusTableRenderer
extends AbstractTableRenderer<CraftingStatusEntry> {
    private static final int BACKGROUND_ALPHA = 0x5A000000;

    public CraftingStatusTableRenderer(AEBaseScreen<?> screen, int x, int y) {
        super(screen, x, y, 6);
    }

    @Override
    protected List<Component> getEntryDescription(CraftingStatusEntry entry) {
        String amount;
        ArrayList<Component> lines = new ArrayList<Component>(3);
        if (entry.getStoredAmount() > 0L) {
            amount = entry.getWhat().formatAmount(entry.getStoredAmount(), AmountFormat.SLOT);
            lines.add((Component)GuiText.FromStorage.text(amount));
        }
        if (entry.getActiveAmount() > 0L) {
            amount = entry.getWhat().formatAmount(entry.getActiveAmount(), AmountFormat.SLOT);
            lines.add((Component)GuiText.Crafting.text(amount));
        }
        if (entry.getPendingAmount() > 0L) {
            amount = entry.getWhat().formatAmount(entry.getPendingAmount(), AmountFormat.SLOT);
            lines.add((Component)GuiText.Scheduled.text(amount));
        }
        return lines;
    }

    @Override
    protected AEKey getEntryStack(CraftingStatusEntry entry) {
        return entry.getWhat();
    }

    @Override
    protected List<Component> getEntryTooltip(CraftingStatusEntry entry) {
        List<Component> lines = AEKeyRendering.getTooltip(entry.getWhat());
        if (entry.getStoredAmount() > 0L) {
            lines.add((Component)GuiText.FromStorage.text(entry.getWhat().formatAmount(entry.getStoredAmount(), AmountFormat.FULL)));
        }
        if (entry.getActiveAmount() > 0L) {
            lines.add((Component)GuiText.Crafting.text(entry.getWhat().formatAmount(entry.getActiveAmount(), AmountFormat.FULL)));
        }
        if (entry.getPendingAmount() > 0L) {
            lines.add((Component)GuiText.Scheduled.text(entry.getWhat().formatAmount(entry.getPendingAmount(), AmountFormat.FULL)));
        }
        return lines;
    }

    @Override
    protected int getEntryBackgroundColor(CraftingStatusEntry entry) {
        if (AEConfig.instance().isUseColoredCraftingStatus()) {
            if (entry.getActiveAmount() > 0L) {
                return AEColor.GREEN.blackVariant | 0x5A000000;
            }
            if (entry.getPendingAmount() > 0L) {
                return AEColor.YELLOW.blackVariant | 0x5A000000;
            }
        }
        return 0;
    }
}

