/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  mcjty.theoneprobe.api.IProbeInfo
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 */
package appeng.integration.modules.theoneprobe;

import appeng.api.integrations.igtooltip.TooltipBuilder;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class TopTooltipBuilder
implements TooltipBuilder {
    private final IProbeInfo probeInfo;

    public TopTooltipBuilder(IProbeInfo probeInfo) {
        this.probeInfo = probeInfo;
    }

    @Override
    public void addLine(Component line) {
        this.probeInfo.mcText(line);
    }

    @Override
    public void addLine(Component line, ResourceLocation id) {
        this.probeInfo.mcText(line);
    }
}

