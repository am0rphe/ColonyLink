/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.world.entity.player.Player
 */
package appeng.integration.modules.igtooltip.parts;

import appeng.api.integrations.igtooltip.TooltipBuilder;
import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.api.integrations.igtooltip.providers.BodyProvider;
import appeng.api.integrations.igtooltip.providers.ServerDataProvider;
import appeng.api.networking.IGridNode;
import appeng.core.localization.InGameTooltip;
import appeng.parts.AEBasePart;
import appeng.parts.p2p.MEP2PTunnelPart;
import appeng.parts.p2p.P2PTunnelPart;
import appeng.util.Platform;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

public final class P2PStateDataProvider
implements BodyProvider<P2PTunnelPart>,
ServerDataProvider<P2PTunnelPart> {
    private static final byte STATE_UNLINKED = 0;
    private static final byte STATE_OUTPUT = 1;
    private static final byte STATE_INPUT = 2;
    public static final String TAG_P2P_STATE = "p2pState";
    public static final String TAG_P2P_OUTPUTS = "p2pOutputs";
    public static final String TAG_P2P_FREQUENCY = "p2pFrequency";
    public static final String TAG_P2P_FREQUENCY_NAME = "p2pFrequencyName";
    public static final String TAG_P2P_ME_CARRIED_CHANNELS = "p2pCarriedChannels";

    @Override
    public void buildTooltip(P2PTunnelPart object, TooltipContext context, TooltipBuilder tooltip) {
        CompoundTag serverData = context.serverData();
        if (serverData.contains(TAG_P2P_STATE, 1)) {
            byte state = serverData.getByte(TAG_P2P_STATE);
            int outputs = serverData.getInt(TAG_P2P_OUTPUTS);
            switch (state) {
                case 0: {
                    tooltip.addLine((Component)InGameTooltip.P2PUnlinked.text());
                    break;
                }
                case 1: {
                    tooltip.addLine((Component)InGameTooltip.P2POutput.text());
                    break;
                }
                case 2: {
                    tooltip.addLine(P2PStateDataProvider.getOutputText(outputs));
                }
            }
            short freq = serverData.getShort(TAG_P2P_FREQUENCY);
            MutableComponent freqTooltip = Platform.p2p().toColoredHexString(freq).withStyle(ChatFormatting.BOLD);
            if (serverData.contains(TAG_P2P_FREQUENCY_NAME, 8)) {
                String freqName = serverData.getString(TAG_P2P_FREQUENCY_NAME);
                freqTooltip = Component.literal((String)freqName).append(" (").append((Component)freqTooltip).append(")");
            }
            tooltip.addLine((Component)InGameTooltip.P2PFrequency.text(freqTooltip));
            if (serverData.contains(TAG_P2P_ME_CARRIED_CHANNELS, 3)) {
                int carriedChannels = serverData.getInt(TAG_P2P_ME_CARRIED_CHANNELS);
                tooltip.addLine((Component)InGameTooltip.P2PMECarriedChannels.text(carriedChannels));
            }
        }
    }

    @Override
    public void provideServerData(Player player, P2PTunnelPart part, CompoundTag serverData) {
        MEP2PTunnelPart meTunnel;
        IGridNode externalNode;
        if (!part.isPowered()) {
            return;
        }
        serverData.putShort(TAG_P2P_FREQUENCY, part.getFrequency());
        byte state = 0;
        if (!part.isOutput()) {
            int outputCount = part.getOutputs().size();
            if (outputCount > 0) {
                state = 2;
                serverData.putInt(TAG_P2P_OUTPUTS, outputCount);
            }
            if (part.getCustomName() != null) {
                serverData.putString(TAG_P2P_FREQUENCY_NAME, part.getCustomName().getString());
            }
        } else {
            Object input = part.getInput();
            if (input != null) {
                state = 1;
                if (((AEBasePart)input).getCustomName() != null) {
                    serverData.putString(TAG_P2P_FREQUENCY_NAME, ((AEBasePart)input).getCustomName().getString());
                }
            }
        }
        serverData.putByte(TAG_P2P_STATE, state);
        if (part instanceof MEP2PTunnelPart && (externalNode = (meTunnel = (MEP2PTunnelPart)part).getExternalFacingNode()) != null) {
            int channels = externalNode.getUsedChannels();
            serverData.putInt(TAG_P2P_ME_CARRIED_CHANNELS, channels);
        }
    }

    private static Component getOutputText(int outputs) {
        if (outputs <= 1) {
            return InGameTooltip.P2PInputOneOutput.text();
        }
        return InGameTooltip.P2PInputManyOutputs.text(outputs);
    }
}

