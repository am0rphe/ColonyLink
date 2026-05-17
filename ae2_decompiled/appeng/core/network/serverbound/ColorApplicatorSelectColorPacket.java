/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  org.jetbrains.annotations.Nullable
 */
package appeng.core.network.serverbound;

import appeng.api.util.AEColor;
import appeng.core.network.CustomAppEngPayload;
import appeng.core.network.ServerboundPacket;
import appeng.items.tools.powered.ColorApplicatorItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record ColorApplicatorSelectColorPacket(@Nullable AEColor color) implements ServerboundPacket
{
    public static final StreamCodec<RegistryFriendlyByteBuf, ColorApplicatorSelectColorPacket> STREAM_CODEC = StreamCodec.ofMember(ColorApplicatorSelectColorPacket::write, ColorApplicatorSelectColorPacket::decode);
    public static final CustomPacketPayload.Type<ColorApplicatorSelectColorPacket> TYPE = CustomAppEngPayload.createType("color_applicator_select_color");

    public CustomPacketPayload.Type<ColorApplicatorSelectColorPacket> type() {
        return TYPE;
    }

    public static ColorApplicatorSelectColorPacket decode(RegistryFriendlyByteBuf stream) {
        AEColor color = null;
        if (stream.readBoolean()) {
            color = (AEColor)stream.readEnum(AEColor.class);
        }
        return new ColorApplicatorSelectColorPacket(color);
    }

    public void write(RegistryFriendlyByteBuf data) {
        if (this.color != null) {
            data.writeBoolean(true);
            data.writeEnum((Enum)this.color);
        } else {
            data.writeBoolean(false);
        }
    }

    @Override
    public void handleOnServer(ServerPlayer player) {
        ColorApplicatorSelectColorPacket.switchColor(player.getMainHandItem(), this.color);
        ColorApplicatorSelectColorPacket.switchColor(player.getOffhandItem(), this.color);
    }

    private static void switchColor(ItemStack stack, AEColor color) {
        Item item;
        if (!stack.isEmpty() && (item = stack.getItem()) instanceof ColorApplicatorItem) {
            ColorApplicatorItem colorApplicator = (ColorApplicatorItem)item;
            colorApplicator.setActiveColor(stack, color);
        }
    }
}

