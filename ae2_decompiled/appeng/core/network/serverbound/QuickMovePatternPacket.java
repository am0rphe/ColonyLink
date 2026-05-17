/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.ByteBufCodecs
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.inventory.AbstractContainerMenu
 */
package appeng.core.network.serverbound;

import appeng.core.network.CustomAppEngPayload;
import appeng.core.network.ServerboundPacket;
import appeng.menu.implementations.PatternAccessTermMenu;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

public record QuickMovePatternPacket(int containerId, int clickedSlot, List<Long> allowedPatternContainers) implements ServerboundPacket
{
    public static final StreamCodec<RegistryFriendlyByteBuf, QuickMovePatternPacket> STREAM_CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.VAR_INT, QuickMovePatternPacket::containerId, (StreamCodec)ByteBufCodecs.VAR_INT, QuickMovePatternPacket::clickedSlot, (StreamCodec)ByteBufCodecs.VAR_LONG.apply(ByteBufCodecs.list()), QuickMovePatternPacket::allowedPatternContainers, QuickMovePatternPacket::new);
    public static final CustomPacketPayload.Type<QuickMovePatternPacket> TYPE = CustomAppEngPayload.createType("quick_move_pattern");

    @Override
    public void handleOnServer(ServerPlayer player) {
        AbstractContainerMenu abstractContainerMenu;
        if (player.containerMenu.containerId == this.containerId && (abstractContainerMenu = player.containerMenu) instanceof PatternAccessTermMenu) {
            PatternAccessTermMenu menu = (PatternAccessTermMenu)abstractContainerMenu;
            menu.quickMovePattern(player, this.clickedSlot, this.allowedPatternContainers);
        }
    }

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

