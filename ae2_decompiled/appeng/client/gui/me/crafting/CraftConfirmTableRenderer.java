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
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.me.crafting.AbstractTableRenderer;
import appeng.core.localization.GuiText;
import appeng.menu.me.crafting.CraftingPlanSummaryEntry;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;

public class CraftConfirmTableRenderer
extends AbstractTableRenderer<CraftingPlanSummaryEntry> {
    public CraftConfirmTableRenderer(AEBaseScreen<?> screen, int x, int y) {
        super(screen, x, y, 5);
    }

    @Override
    protected List<Component> getEntryDescription(CraftingPlanSummaryEntry entry) {
        String amount;
        ArrayList<Component> lines = new ArrayList<Component>(3);
        if (entry.getStoredAmount() > 0L) {
            amount = entry.getWhat().formatAmount(entry.getStoredAmount(), AmountFormat.SLOT);
            lines.add((Component)GuiText.FromStorage.text(amount));
        }
        if (entry.getMissingAmount() > 0L) {
            amount = entry.getWhat().formatAmount(entry.getMissingAmount(), AmountFormat.SLOT);
            lines.add((Component)GuiText.Missing.text(amount));
        }
        if (entry.getCraftAmount() > 0L) {
            amount = entry.getWhat().formatAmount(entry.getCraftAmount(), AmountFormat.SLOT);
            lines.add((Component)GuiText.ToCraft.text(amount));
        }
        return lines;
    }

    @Override
    protected AEKey getEntryStack(CraftingPlanSummaryEntry entry) {
        return entry.getWhat();
    }

    @Override
    protected List<Component> getEntryTooltip(CraftingPlanSummaryEntry entry) {
        List<Component> lines = AEKeyRendering.getTooltip(entry.getWhat());
        if (entry.getStoredAmount() > 0L) {
            lines.add((Component)GuiText.FromStorage.text(entry.getWhat().formatAmount(entry.getStoredAmount(), AmountFormat.FULL)));
        }
        if (entry.getMissingAmount() > 0L) {
            lines.add((Component)GuiText.Missing.text(entry.getWhat().formatAmount(entry.getMissingAmount(), AmountFormat.FULL)));
        }
        if (entry.getCraftAmount() > 0L) {
            lines.add((Component)GuiText.ToCraft.text(entry.getWhat().formatAmount(entry.getCraftAmount(), AmountFormat.FULL)));
        }
        return lines;
    }

    @Override
    protected int getEntryOverlayColor(CraftingPlanSummaryEntry entry) {
        return entry.getMissingAmount() > 0L ? 452919296 : 0;
    }
}

