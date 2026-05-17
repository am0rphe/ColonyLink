/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 */
package appeng.core.network.serverbound;

import appeng.core.network.CustomAppEngPayload;
import appeng.core.network.ServerboundPacket;
import appeng.helpers.IMouseWheelItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record MouseWheelPacket(boolean wheelUp) implements ServerboundPacket
{
    public static final StreamCodec<RegistryFriendlyByteBuf, MouseWheelPacket> STREAM_CODEC = StreamCodec.ofMember(MouseWheelPacket::write, MouseWheelPacket::decode);
    public static final CustomPacketPayload.Type<MouseWheelPacket> TYPE = CustomAppEngPayload.createType("mouse_wheel");

    public CustomPacketPayload.Type<MouseWheelPacket> type() {
        return TYPE;
    }

    public static MouseWheelPacket decode(RegistryFriendlyByteBuf byteBuf) {
        boolean wheelUp = byteBuf.readBoolean();
        return new MouseWheelPacket(wheelUp);
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeBoolean(this.wheelUp);
    }

    @Override
    public void handleOnServer(ServerPlayer player) {
        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);
        Item item = mainHand.getItem();
        if (item instanceof IMouseWheelItem) {
            IMouseWheelItem mouseWheelItem = (IMouseWheelItem)item;
            mouseWheelItem.onWheel(mainHand, this.wheelUp);
        } else {
            item = offHand.getItem();
            if (item instanceof IMouseWheelItem) {
                IMouseWheelItem mouseWheelItem = (IMouseWheelItem)item;
                mouseWheelItem.onWheel(offHand, this.wheelUp);
            }
        }
    }
}

