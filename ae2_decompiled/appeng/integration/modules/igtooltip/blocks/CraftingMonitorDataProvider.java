/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package appeng.integration.modules.igtooltip.blocks;

import appeng.api.integrations.igtooltip.TooltipBuilder;
import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.api.integrations.igtooltip.providers.BodyProvider;
import appeng.api.stacks.GenericStack;
import appeng.blockentity.crafting.CraftingMonitorBlockEntity;
import appeng.core.localization.InGameTooltip;
import net.minecraft.network.chat.Component;

public final class CraftingMonitorDataProvider
implements BodyProvider<CraftingMonitorBlockEntity> {
    @Override
    public void buildTooltip(CraftingMonitorBlockEntity monitor, TooltipContext context, TooltipBuilder tooltip) {
        GenericStack displayStack = monitor.getJobProgress();
        if (displayStack != null) {
            tooltip.addLine((Component)InGameTooltip.Crafting.text(displayStack.what().getDisplayName()));
        }
    }
}

