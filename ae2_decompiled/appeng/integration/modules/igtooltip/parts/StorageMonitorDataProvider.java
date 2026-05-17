/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package appeng.integration.modules.igtooltip.parts;

import appeng.api.integrations.igtooltip.TooltipBuilder;
import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.api.integrations.igtooltip.providers.BodyProvider;
import appeng.api.stacks.AEKey;
import appeng.core.localization.InGameTooltip;
import appeng.parts.reporting.AbstractMonitorPart;
import net.minecraft.network.chat.Component;

public final class StorageMonitorDataProvider
implements BodyProvider<AbstractMonitorPart> {
    @Override
    public void buildTooltip(AbstractMonitorPart monitor, TooltipContext context, TooltipBuilder tooltip) {
        AEKey displayed = monitor.getDisplayed();
        boolean isLocked = monitor.isLocked();
        if (displayed != null) {
            tooltip.addLine((Component)InGameTooltip.Showing.text().append(": ").append(displayed.getDisplayName()));
        }
        tooltip.addLine((Component)(isLocked ? InGameTooltip.Locked.text() : InGameTooltip.Unlocked.text()));
    }
}

