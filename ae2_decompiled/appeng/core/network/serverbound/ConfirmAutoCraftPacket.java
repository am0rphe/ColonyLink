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
import appeng.menu.me.crafting.CraftAmountMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

public record ConfirmAutoCraftPacket(int amount, boolean craftMissingAmount, boolean autoStart) implements ServerboundPacket
{
    public static final StreamCodec<RegistryFriendlyByteBuf, ConfirmAutoCraftPacket> STREAM_CODEC = StreamCodec.ofMember(ConfirmAutoCraftPacket::write, ConfirmAutoCraftPacket::decode);
    public static final CustomPacketPayload.Type<ConfirmAutoCraftPacket> TYPE = CustomAppEngPayload.createType("confirm_auto_craft");

    public CustomPacketPayload.Type<ConfirmAutoCraftPacket> type() {
        return TYPE;
    }

    public static ConfirmAutoCraftPacket decode(RegistryFriendlyByteBuf stream) {
        int amount = stream.readInt();
        boolean craftMissingAmount = stream.readBoolean();
        boolean autoStart = stream.readBoolean();
        return new ConfirmAutoCraftPacket(amount, craftMissingAmount, autoStart);
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeInt(this.amount);
        data.writeBoolean(this.craftMissingAmount);
        data.writeBoolean(this.autoStart);
    }

    @Override
    public void handleOnServer(ServerPlayer player) {
        AbstractContainerMenu abstractContainerMenu = player.containerMenu;
        if (abstractContainerMenu instanceof CraftAmountMenu) {
            CraftAmountMenu menu = (CraftAmountMenu)abstractContainerMenu;
            menu.confirm(this.amount, this.craftMissingAmount, this.autoStart);
        }
    }
}

