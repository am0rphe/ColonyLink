/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.Unpooled
 *  net.minecraft.core.RegistryAccess
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package appeng.core.network.clientbound;

import appeng.core.network.ClientboundPacket;
import appeng.core.network.CustomAppEngPayload;
import appeng.menu.AEBaseMenu;
import io.netty.buffer.Unpooled;
import java.util.function.Consumer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public record GuiDataSyncPacket(int containerId, byte[] syncData) implements ClientboundPacket
{
    public static final StreamCodec<RegistryFriendlyByteBuf, GuiDataSyncPacket> STREAM_CODEC = StreamCodec.ofMember(GuiDataSyncPacket::write, GuiDataSyncPacket::decode);
    public static final CustomPacketPayload.Type<GuiDataSyncPacket> TYPE = CustomAppEngPayload.createType("");

    public GuiDataSyncPacket(int containerId, Consumer<RegistryFriendlyByteBuf> writer, RegistryAccess registryAccess) {
        this(containerId, GuiDataSyncPacket.createSyncData(writer, registryAccess));
    }

    public CustomPacketPayload.Type<GuiDataSyncPacket> type() {
        return TYPE;
    }

    private static byte[] createSyncData(Consumer<RegistryFriendlyByteBuf> writer, RegistryAccess registryAccess) {
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), registryAccess);
        writer.accept(buffer);
        byte[] result = new byte[buffer.readableBytes()];
        buffer.readBytes(result);
        return result;
    }

    public static GuiDataSyncPacket decode(RegistryFriendlyByteBuf data) {
        int containerId = data.readVarInt();
        byte[] syncData = data.readByteArray();
        return new GuiDataSyncPacket(containerId, syncData);
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeVarInt(this.containerId);
        data.writeByteArray(this.syncData);
    }

    @Override
    @OnlyIn(value=Dist.CLIENT)
    public void handleOnClient(Player player) {
        AbstractContainerMenu c = player.containerMenu;
        if (c instanceof AEBaseMenu) {
            AEBaseMenu baseMenu = (AEBaseMenu)c;
            if (c.containerId == this.containerId) {
                baseMenu.receiveServerSyncData(new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer((byte[])this.syncData), player.registryAccess()));
            }
        }
    }
}

