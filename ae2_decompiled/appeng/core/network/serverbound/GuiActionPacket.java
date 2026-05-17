/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  org.jetbrains.annotations.Nullable
 */
package appeng.core.network.serverbound;

import appeng.core.network.CustomAppEngPayload;
import appeng.core.network.ServerboundPacket;
import appeng.menu.AEBaseMenu;
import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

public record GuiActionPacket(int containerId, String actionName, @Nullable String jsonPayload) implements ServerboundPacket
{
    public static final StreamCodec<RegistryFriendlyByteBuf, GuiActionPacket> STREAM_CODEC = StreamCodec.ofMember(GuiActionPacket::write, GuiActionPacket::decode);
    public static final CustomPacketPayload.Type<GuiActionPacket> TYPE = CustomAppEngPayload.createType("gui_action");

    public CustomPacketPayload.Type<GuiActionPacket> type() {
        return TYPE;
    }

    public static GuiActionPacket decode(RegistryFriendlyByteBuf data) {
        int containerId = data.readVarInt();
        String actionName = data.readUtf();
        String jsonPayload = data.readOptional(FriendlyByteBuf::readUtf).orElse(null);
        return new GuiActionPacket(containerId, actionName, jsonPayload);
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeVarInt(this.containerId);
        data.writeUtf(this.actionName);
        data.writeOptional(Optional.ofNullable(this.jsonPayload), FriendlyByteBuf::writeUtf);
    }

    @Override
    public void handleOnServer(ServerPlayer player) {
        AbstractContainerMenu c = player.containerMenu;
        if (c instanceof AEBaseMenu) {
            AEBaseMenu baseMenu = (AEBaseMenu)c;
            if (c.containerId == this.containerId) {
                baseMenu.receiveClientAction(this.actionName, this.jsonPayload);
            }
        }
    }
}

