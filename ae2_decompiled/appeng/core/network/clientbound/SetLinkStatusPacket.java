/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.chat.ComponentSerialization
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 */
package appeng.core.network.clientbound;

import appeng.api.storage.ILinkStatus;
import appeng.api.storage.LinkStatus;
import appeng.core.network.ClientboundPacket;
import appeng.core.network.CustomAppEngPayload;
import appeng.menu.guisync.LinkStatusAwareMenu;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public record SetLinkStatusPacket(ILinkStatus linkStatus) implements ClientboundPacket
{
    public static final StreamCodec<RegistryFriendlyByteBuf, SetLinkStatusPacket> STREAM_CODEC = StreamCodec.ofMember(SetLinkStatusPacket::write, SetLinkStatusPacket::decode);
    public static final CustomPacketPayload.Type<SetLinkStatusPacket> TYPE = CustomAppEngPayload.createType("set_link_status");

    public CustomPacketPayload.Type<SetLinkStatusPacket> type() {
        return TYPE;
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBoolean(this.linkStatus.connected());
        ComponentSerialization.TRUSTED_OPTIONAL_STREAM_CODEC.encode((Object)buffer, Optional.ofNullable(this.linkStatus.statusDescription()));
    }

    public static SetLinkStatusPacket decode(RegistryFriendlyByteBuf buffer) {
        return new SetLinkStatusPacket(new LinkStatus(buffer.readBoolean(), ((Optional)ComponentSerialization.TRUSTED_OPTIONAL_STREAM_CODEC.decode((Object)buffer)).orElse(null)));
    }

    @Override
    public void handleOnClient(Player player) {
        AbstractContainerMenu abstractContainerMenu = player.containerMenu;
        if (abstractContainerMenu instanceof LinkStatusAwareMenu) {
            LinkStatusAwareMenu linkStatusAwareMenu = (LinkStatusAwareMenu)abstractContainerMenu;
            linkStatusAwareMenu.setLinkStatus(this.linkStatus);
        }
    }
}

