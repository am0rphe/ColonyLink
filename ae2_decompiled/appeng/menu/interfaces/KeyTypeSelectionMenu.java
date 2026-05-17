/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.neoforged.neoforge.network.PacketDistributor
 *  org.jetbrains.annotations.ApiStatus$NonExtendable
 */
package appeng.menu.interfaces;

import appeng.api.stacks.AEKeyType;
import appeng.api.util.KeyTypeSelection;
import appeng.core.network.serverbound.SelectKeyTypePacket;
import appeng.menu.guisync.PacketWritable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.ApiStatus;

public interface KeyTypeSelectionMenu {
    public KeyTypeSelection getServerKeyTypeSelection();

    public SyncedKeyTypes getClientKeyTypeSelection();

    @ApiStatus.NonExtendable
    default public void selectKeyType(AEKeyType keyType, boolean enabled) {
        SelectKeyTypePacket message = new SelectKeyTypePacket(keyType, enabled);
        PacketDistributor.sendToServer((CustomPacketPayload)message, (CustomPacketPayload[])new CustomPacketPayload[0]);
        this.getClientKeyTypeSelection().keyTypes().put(keyType, enabled);
    }

    public record SyncedKeyTypes(Map<AEKeyType, Boolean> keyTypes) implements PacketWritable
    {
        public SyncedKeyTypes() {
            this(new LinkedHashMap<AEKeyType, Boolean>());
        }

        public SyncedKeyTypes(RegistryFriendlyByteBuf buf) {
            this(buf.readMap(LinkedHashMap::new, b -> AEKeyType.fromRawId(b.readVarInt()), FriendlyByteBuf::readBoolean));
        }

        @Override
        public void writeToPacket(RegistryFriendlyByteBuf buf) {
            buf.writeMap(this.keyTypes, (b, keyType) -> b.writeVarInt((int)keyType.getRawId()), FriendlyByteBuf::writeBoolean);
        }

        public List<AEKeyType> enabledSet() {
            return this.keyTypes.entrySet().stream().filter(Map.Entry::getValue).map(Map.Entry::getKey).toList();
        }
    }
}

