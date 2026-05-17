/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 */
package appeng.integration.modules.igtooltip.blocks;

import appeng.api.integrations.igtooltip.TooltipBuilder;
import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.api.integrations.igtooltip.providers.BodyProvider;
import appeng.blockentity.networking.CrystalResonanceGeneratorBlockEntity;
import appeng.core.localization.InGameTooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public final class CrystalResonanceGeneratorProvider
implements BodyProvider<CrystalResonanceGeneratorBlockEntity> {
    @Override
    public void buildTooltip(CrystalResonanceGeneratorBlockEntity generator, TooltipContext context, TooltipBuilder tooltip) {
        if (generator.isSuppressed()) {
            tooltip.addLine((Component)InGameTooltip.Suppressed.text().withStyle(ChatFormatting.RED));
        }
    }
}

