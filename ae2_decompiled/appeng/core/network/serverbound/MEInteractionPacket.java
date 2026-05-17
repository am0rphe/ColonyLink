/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.inventory.AbstractContainerMenu
 */
package appeng.core.network.serverbound;

import appeng.core.network.CustomAppEngPayload;
import appeng.core.network.ServerboundPacket;
import appeng.helpers.InventoryAction;
import appeng.menu.me.common.IMEInteractionHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

public record MEInteractionPacket(int containerId, long serial, InventoryAction action) implements ServerboundPacket
{
    public static final StreamCodec<RegistryFriendlyByteBuf, MEInteractionPacket> STREAM_CODEC = StreamCodec.ofMember(MEInteractionPacket::write, MEInteractionPacket::decode);
    public static final CustomPacketPayload.Type<MEInteractionPacket> TYPE = CustomAppEngPayload.createType("me_interaction");

    public CustomPacketPayload.Type<MEInteractionPacket> type() {
        return TYPE;
    }

    public static MEInteractionPacket decode(RegistryFriendlyByteBuf buffer) {
        int containerId = buffer.readInt();
        long serial = buffer.readVarLong();
        InventoryAction action = (InventoryAction)buffer.readEnum(InventoryAction.class);
        return new MEInteractionPacket(containerId, serial, action);
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeInt(this.containerId);
        data.writeVarLong(this.serial);
        data.writeEnum((Enum)this.action);
    }

    @Override
    public void handleOnServer(ServerPlayer player) {
        AbstractContainerMenu abstractContainerMenu = player.containerMenu;
        if (abstractContainerMenu instanceof IMEInteractionHandler) {
            IMEInteractionHandler handler = (IMEInteractionHandler)abstractContainerMenu;
            if (player.containerMenu.containerId != this.containerId) {
                return;
            }
            handler.handleInteraction(this.serial, this.action);
        }
    }
}

