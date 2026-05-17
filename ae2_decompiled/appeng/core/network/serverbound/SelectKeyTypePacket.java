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

import appeng.api.stacks.AEKeyType;
import appeng.core.network.CustomAppEngPayload;
import appeng.core.network.ServerboundPacket;
import appeng.menu.interfaces.KeyTypeSelectionMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

public record SelectKeyTypePacket(AEKeyType keyType, boolean enabled) implements ServerboundPacket
{
    public static final StreamCodec<RegistryFriendlyByteBuf, SelectKeyTypePacket> STREAM_CODEC = StreamCodec.ofMember(SelectKeyTypePacket::write, SelectKeyTypePacket::decode);
    public static final CustomPacketPayload.Type<SelectKeyTypePacket> TYPE = CustomAppEngPayload.createType("select_key_type");

    public CustomPacketPayload.Type<SelectKeyTypePacket> type() {
        return TYPE;
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt((int)this.keyType.getRawId());
        buf.writeBoolean(this.enabled);
    }

    public static SelectKeyTypePacket decode(RegistryFriendlyByteBuf buf) {
        return new SelectKeyTypePacket(AEKeyType.fromRawId(buf.readVarInt()), buf.readBoolean());
    }

    @Override
    public void handleOnServer(ServerPlayer player) {
        AbstractContainerMenu abstractContainerMenu = player.containerMenu;
        if (abstractContainerMenu instanceof KeyTypeSelectionMenu) {
            KeyTypeSelectionMenu menu = (KeyTypeSelectionMenu)abstractContainerMenu;
            menu.getServerKeyTypeSelection().setEnabled(this.keyType, this.enabled);
        }
    }
}

