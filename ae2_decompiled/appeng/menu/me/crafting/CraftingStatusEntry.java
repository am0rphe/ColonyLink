/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.ByteBufCodecs
 *  net.minecraft.network.codec.StreamCodec
 *  org.jetbrains.annotations.Nullable
 */
package appeng.menu.me.crafting;

import appeng.api.stacks.AEKey;
import java.util.Comparator;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

public class CraftingStatusEntry
implements Comparable<CraftingStatusEntry> {
    public static final StreamCodec<RegistryFriendlyByteBuf, CraftingStatusEntry> STREAM_CODEC = StreamCodec.of(CraftingStatusEntry::write, CraftingStatusEntry::read);
    public static final StreamCodec<RegistryFriendlyByteBuf, List<CraftingStatusEntry>> LIST_STREAM_CODEC = STREAM_CODEC.apply(ByteBufCodecs.list());
    private static final Comparator<CraftingStatusEntry> COMPARATOR = Comparator.comparing(e -> e.getActiveAmount() + e.getPendingAmount()).thenComparing(CraftingStatusEntry::getStoredAmount).reversed();
    private final long serial;
    @Nullable
    private final AEKey what;
    private final long storedAmount;
    private final long activeAmount;
    private final long pendingAmount;

    public CraftingStatusEntry(long serial, @Nullable AEKey what, long storedAmount, long activeAmount, long pendingAmount) {
        this.serial = serial;
        this.what = what;
        this.storedAmount = storedAmount;
        this.activeAmount = activeAmount;
        this.pendingAmount = pendingAmount;
    }

    public long getSerial() {
        return this.serial;
    }

    public long getActiveAmount() {
        return this.activeAmount;
    }

    public long getStoredAmount() {
        return this.storedAmount;
    }

    public long getPendingAmount() {
        return this.pendingAmount;
    }

    public AEKey getWhat() {
        return this.what;
    }

    public static void write(RegistryFriendlyByteBuf buffer, CraftingStatusEntry entry) {
        buffer.writeVarLong(entry.serial);
        buffer.writeVarLong(entry.activeAmount);
        buffer.writeVarLong(entry.storedAmount);
        buffer.writeVarLong(entry.pendingAmount);
        AEKey.writeOptionalKey(buffer, entry.what);
    }

    public static CraftingStatusEntry read(RegistryFriendlyByteBuf buffer) {
        long serial = buffer.readVarLong();
        long missingAmount = buffer.readVarLong();
        long storedAmount = buffer.readVarLong();
        long craftAmount = buffer.readVarLong();
        AEKey what = AEKey.readOptionalKey(buffer);
        return new CraftingStatusEntry(serial, what, storedAmount, missingAmount, craftAmount);
    }

    public boolean isDeleted() {
        return this.storedAmount == 0L && this.activeAmount == 0L && this.pendingAmount == 0L;
    }

    @Override
    public int compareTo(CraftingStatusEntry o) {
        return COMPARATOR.compare(this, o);
    }
}

