/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.item.ItemStack
 */
package appeng.core.network.serverbound;

import appeng.core.network.CustomAppEngPayload;
import appeng.core.network.ServerboundPacket;
import appeng.helpers.InventoryAction;
import appeng.menu.AEBaseMenu;
import appeng.util.Platform;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public record InventoryActionPacket(InventoryAction action, int slot, long extraId, ItemStack slotItem) implements ServerboundPacket
{
    public static final StreamCodec<RegistryFriendlyByteBuf, InventoryActionPacket> STREAM_CODEC = StreamCodec.ofMember(InventoryActionPacket::write, InventoryActionPacket::decode);
    public static final CustomPacketPayload.Type<InventoryActionPacket> TYPE = CustomAppEngPayload.createType("inventory_action");

    public InventoryActionPacket(InventoryAction action, int slot, long id) {
        this(action, slot, id, ItemStack.EMPTY);
    }

    public InventoryActionPacket(InventoryAction action, int slot, ItemStack slotItem) {
        this(action, slot, 0L, slotItem.copy());
        if (Platform.isClient() && action != InventoryAction.SET_FILTER) {
            throw new IllegalStateException("invalid packet, client cannot post inv actions with stacks.");
        }
    }

    public CustomPacketPayload.Type<InventoryActionPacket> type() {
        return TYPE;
    }

    public static InventoryActionPacket decode(RegistryFriendlyByteBuf stream) {
        InventoryAction action = (InventoryAction)stream.readEnum(InventoryAction.class);
        int slot = stream.readInt();
        long extraId = stream.readLong();
        ItemStack slotItem = (ItemStack)ItemStack.OPTIONAL_STREAM_CODEC.decode((Object)stream);
        return new InventoryActionPacket(action, slot, extraId, slotItem);
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeEnum((Enum)this.action);
        data.writeInt(this.slot);
        data.writeLong(this.extraId);
        ItemStack.OPTIONAL_STREAM_CODEC.encode((Object)data, (Object)this.slotItem);
    }

    @Override
    public void handleOnServer(ServerPlayer player) {
        AbstractContainerMenu abstractContainerMenu = player.containerMenu;
        if (abstractContainerMenu instanceof AEBaseMenu) {
            AEBaseMenu baseMenu = (AEBaseMenu)abstractContainerMenu;
            if (this.action == InventoryAction.SET_FILTER) {
                baseMenu.setFilter(this.slot, this.slotItem);
            } else {
                baseMenu.doAction(player, this.action, this.slot, this.extraId);
            }
        }
    }
}

