/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  io.netty.buffer.Unpooled
 *  net.minecraft.core.RegistryAccess
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 *  net.neoforged.neoforge.network.connection.ConnectionType
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package appeng.core.network.clientbound;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.AEKeyFilter;
import appeng.core.AELog;
import appeng.core.network.ClientboundPacket;
import appeng.core.network.CustomAppEngPayload;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.common.IClientRepo;
import appeng.menu.me.common.IncrementalUpdateHelper;
import appeng.menu.me.common.MEStorageMenu;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.connection.ConnectionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record MEInventoryUpdatePacket(boolean fullUpdate, int containerId, @Nullable List<GridInventoryEntry> entries, int encodedEntryCount, @Nullable RegistryFriendlyByteBuf encodedEntries) implements ClientboundPacket
{
    public static final StreamCodec<RegistryFriendlyByteBuf, MEInventoryUpdatePacket> STREAM_CODEC = StreamCodec.ofMember(MEInventoryUpdatePacket::write, MEInventoryUpdatePacket::decode);
    public static final CustomPacketPayload.Type<MEInventoryUpdatePacket> TYPE = CustomAppEngPayload.createType("me_inventory_update");
    private static final int UNCOMPRESSED_PACKET_BYTE_LIMIT = 524288;
    private static final int INITIAL_BUFFER_CAPACITY = 2048;

    public CustomPacketPayload.Type<MEInventoryUpdatePacket> type() {
        return TYPE;
    }

    public static MEInventoryUpdatePacket decode(RegistryFriendlyByteBuf data) {
        int containerId = data.readVarInt();
        boolean fullUpdate = data.readBoolean();
        int encodedEntryCount = data.readVarInt();
        ArrayList<GridInventoryEntry> entries = MEInventoryUpdatePacket.decodeEntriesPayload(encodedEntryCount, data);
        return new MEInventoryUpdatePacket(fullUpdate, containerId, entries, 0, null);
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeVarInt(this.containerId);
        data.writeBoolean(this.fullUpdate);
        data.writeVarInt(this.encodedEntryCount);
        if (this.encodedEntryCount > 0) {
            if (this.encodedEntries == null) {
                throw new UnsupportedOperationException("Use the builder");
            }
            data.ensureWritable(this.encodedEntries.readableBytes());
            this.encodedEntries.getBytes(this.encodedEntries.readerIndex(), (ByteBuf)data, this.encodedEntries.readableBytes());
        }
    }

    public static Builder builder(int containerId, boolean fullUpdate, RegistryAccess registryAccess) {
        return new Builder(containerId, fullUpdate, registryAccess);
    }

    private static void writeEntry(RegistryFriendlyByteBuf buffer, GridInventoryEntry entry) {
        buffer.writeVarLong(entry.getSerial());
        AEKey.writeOptionalKey(buffer, entry.getWhat());
        buffer.writeVarLong(entry.getStoredAmount());
        buffer.writeVarLong(entry.getRequestableAmount());
        buffer.writeBoolean(entry.isCraftable());
    }

    public static GridInventoryEntry readEntry(RegistryFriendlyByteBuf buffer) {
        long serial = buffer.readVarLong();
        AEKey what = AEKey.readOptionalKey(buffer);
        long storedAmount = buffer.readVarLong();
        long requestableAmount = buffer.readVarLong();
        boolean craftable = buffer.readBoolean();
        return new GridInventoryEntry(serial, what, storedAmount, requestableAmount, craftable);
    }

    @Override
    @OnlyIn(value=Dist.CLIENT)
    public void handleOnClient(Player player) {
        AbstractContainerMenu abstractContainerMenu;
        if (player.containerMenu.containerId == this.containerId && (abstractContainerMenu = player.containerMenu) instanceof MEStorageMenu) {
            MEStorageMenu meMenu = (MEStorageMenu)abstractContainerMenu;
            IClientRepo clientRepo = meMenu.getClientRepo();
            if (clientRepo == null) {
                AELog.info("Ignoring ME inventory update packet because no client repo is available.", new Object[0]);
                return;
            }
            List<GridInventoryEntry> actualEntries = this.entries;
            if (actualEntries == null && this.encodedEntries != null) {
                actualEntries = MEInventoryUpdatePacket.decodeEntriesPayload(this.encodedEntryCount, this.encodedEntries);
            }
            if (actualEntries != null) {
                clientRepo.handleUpdate(this.fullUpdate, actualEntries);
            }
        }
    }

    @NotNull
    private static ArrayList<GridInventoryEntry> decodeEntriesPayload(int entryCount, RegistryFriendlyByteBuf data) {
        ArrayList<GridInventoryEntry> entries = new ArrayList<GridInventoryEntry>(entryCount);
        for (int i = 0; i < entryCount; ++i) {
            entries.add(MEInventoryUpdatePacket.readEntry(data));
        }
        return entries;
    }

    public static class Builder {
        private final List<MEInventoryUpdatePacket> packets = new ArrayList<MEInventoryUpdatePacket>();
        private final int containerId;
        private boolean fullUpdate;
        private final RegistryAccess registryAccess;
        @Nullable
        private RegistryFriendlyByteBuf encodedEntries;
        private int entryCount;
        @Nullable
        private AEKeyFilter filter;

        public Builder(int containerId, boolean fullUpdate, RegistryAccess registryAccess) {
            this.containerId = containerId;
            this.fullUpdate = fullUpdate;
            this.registryAccess = registryAccess;
        }

        public void setFilter(@Nullable AEKeyFilter filter) {
            this.filter = filter;
        }

        public void addFull(IncrementalUpdateHelper updateHelper, KeyCounter networkStorage, Set<AEKey> craftables, KeyCounter requestables) {
            HashSet<AEKey> keys = new HashSet<AEKey>();
            keys.addAll(networkStorage.keySet());
            keys.addAll(craftables);
            keys.addAll(requestables.keySet());
            for (AEKey key : keys) {
                if (this.filter != null && !this.filter.matches(key)) continue;
                long serial = updateHelper.getOrAssignSerial(key);
                this.add(new GridInventoryEntry(serial, key, networkStorage.get(key), requestables.get(key), craftables.contains(key)));
            }
        }

        public void addChanges(IncrementalUpdateHelper updateHelper, KeyCounter networkStorage, Set<AEKey> craftables, KeyCounter requestables) {
            for (AEKey key : updateHelper) {
                AEKey sendKey;
                if (this.filter != null && !this.filter.matches(key)) continue;
                Long serial = updateHelper.getSerial(key);
                if (serial == null) {
                    sendKey = key;
                    serial = updateHelper.getOrAssignSerial(key);
                } else {
                    sendKey = null;
                }
                long storedAmount = networkStorage.get(key);
                boolean craftable = craftables.contains(key);
                long requestable = requestables.get(key);
                if (storedAmount <= 0L && requestable <= 0L && !craftable) {
                    this.add(new GridInventoryEntry(serial, sendKey, 0L, 0L, false));
                    updateHelper.removeSerial(key);
                    continue;
                }
                this.add(new GridInventoryEntry(serial, sendKey, storedAmount, requestable, craftable));
            }
            updateHelper.commitChanges();
        }

        public void add(GridInventoryEntry entry) {
            RegistryFriendlyByteBuf data = this.ensureData();
            MEInventoryUpdatePacket.writeEntry(data, entry);
            ++this.entryCount;
            if (data.writerIndex() >= 524288 || this.entryCount >= Short.MAX_VALUE) {
                this.flushData();
            }
        }

        private void flushData() {
            if (this.encodedEntries != null) {
                MEInventoryUpdatePacket packet = new MEInventoryUpdatePacket(this.fullUpdate, this.containerId, null, this.entryCount, this.encodedEntries);
                this.packets.add(packet);
                this.encodedEntries = null;
                this.entryCount = 0;
                this.fullUpdate = false;
            }
        }

        private RegistryFriendlyByteBuf ensureData() {
            if (this.encodedEntries == null) {
                this.encodedEntries = new RegistryFriendlyByteBuf(Unpooled.buffer((int)2048), this.registryAccess, ConnectionType.NEOFORGE);
            }
            return this.encodedEntries;
        }

        public List<MEInventoryUpdatePacket> build() {
            this.flushData();
            return this.packets;
        }

        public void buildAndSend(Consumer<MEInventoryUpdatePacket> sender) {
            for (MEInventoryUpdatePacket packet : this.build()) {
                sender.accept(packet);
            }
        }
    }
}

