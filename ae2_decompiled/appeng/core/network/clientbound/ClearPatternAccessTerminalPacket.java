/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package appeng.core.network.clientbound;

import appeng.client.gui.me.patternaccess.PatternAccessTermScreen;
import appeng.core.network.ClientboundPacket;
import appeng.core.network.CustomAppEngPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public record ClearPatternAccessTerminalPacket() implements ClientboundPacket
{
    public static final StreamCodec<RegistryFriendlyByteBuf, ClearPatternAccessTerminalPacket> STREAM_CODEC = StreamCodec.ofMember(ClearPatternAccessTerminalPacket::write, ClearPatternAccessTerminalPacket::decode);
    public static final CustomPacketPayload.Type<ClearPatternAccessTerminalPacket> TYPE = CustomAppEngPayload.createType("clear_pattern_access_terminal");

    public CustomPacketPayload.Type<ClearPatternAccessTerminalPacket> type() {
        return TYPE;
    }

    public static ClearPatternAccessTerminalPacket decode(RegistryFriendlyByteBuf data) {
        return new ClearPatternAccessTerminalPacket();
    }

    public void write(RegistryFriendlyByteBuf data) {
    }

    @Override
    @OnlyIn(value=Dist.CLIENT)
    public void handleOnClient(Player player) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof PatternAccessTermScreen) {
            PatternAccessTermScreen patternAccessTerminal = (PatternAccessTermScreen)screen;
            patternAccessTerminal.clear();
        }
    }
}

