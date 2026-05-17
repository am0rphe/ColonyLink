/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  snownee.jade.api.ITooltip
 */
package appeng.integration.modules.jade;

import appeng.api.integrations.igtooltip.TooltipBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.ITooltip;

class JadeTooltipBuilder
implements TooltipBuilder {
    private final ITooltip tooltip;

    public JadeTooltipBuilder(ITooltip tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    public void addLine(Component line) {
        this.tooltip.add(line);
    }

    @Override
    public void addLine(Component line, ResourceLocation id) {
        this.tooltip.add(line, id);
    }
}

