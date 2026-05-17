/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  mcp.mobius.waila.api.ITooltip
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 */
package appeng.integration.modules.wthit;

import appeng.api.integrations.igtooltip.TooltipBuilder;
import mcp.mobius.waila.api.ITooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

class WthitTooltipBuilder
implements TooltipBuilder {
    private final ITooltip tooltip;

    public WthitTooltipBuilder(ITooltip tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    public void addLine(Component line) {
        this.tooltip.addLine(line);
    }

    @Override
    public void addLine(Component line, ResourceLocation id) {
        this.tooltip.addLine(line);
    }
}

