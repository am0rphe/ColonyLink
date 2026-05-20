package com.colonylink.colonylink;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.AEKeyFilter;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * S→C : sends the AE2 storage snapshot (items + craftable flags) to the client.
 * Sent together with WarehouseTerminalSyncPacket on every scan interval.
 *
 * Fix v1.3.0 : items that are craftable-only (count = 0 in storage but have
 * an AE2 pattern) are now included so they appear in the ME panel and can
 * be autocrafted via middle-click.
 */
public record TerminalMeSyncPacket(
        List<MeEntry> entries,
        BlockPos terminalPos
) implements CustomPacketPayload
{
    public record MeEntry(ItemStack stack, long count, boolean craftable) {}

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
            ColonyLink.MODID, "terminal_me_sync");
    public static final CustomPacketPayload.Type<TerminalMeSyncPacket> TYPE =
            new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, TerminalMeSyncPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, p) -> {
                        buf.writeInt(p.entries().size());
                        for (var e : p.entries())
                        {
                            ItemStack.STREAM_CODEC.encode(buf, e.stack());
                            buf.writeLong(e.count());
                            buf.writeBoolean(e.craftable());
                        }
                        buf.writeBlockPos(p.terminalPos());
                    },
                    buf -> {
                        int size = buf.readInt();
                        List<MeEntry> entries = new ArrayList<>();
                        for (int i = 0; i < size; i++)
                        {
                            ItemStack stack   = ItemStack.STREAM_CODEC.decode(buf);
                            long count        = buf.readLong();
                            boolean craftable = buf.readBoolean();
                            entries.add(new MeEntry(stack, count, craftable));
                        }
                        BlockPos pos = buf.readBlockPos();
                        return new TerminalMeSyncPacket(entries, pos);
                    }
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(TerminalMeSyncPacket packet, IPayloadContext ctx)
    {
        ctx.enqueueWork(() -> {
            if (Minecraft.getInstance().screen instanceof WarehouseLinkTerminalScreen screen)
                screen.updateMeSnapshot(packet);
        });
    }

    /**
     * Builds a TerminalMeSyncPacket from the current AE2 network state.
     *
     * @param grid        The AE2 grid to query.
     * @param terminalPos Block position of the terminal Part (for packet routing).
     *
     * Pass 1 : items physically in storage (KeyCounter).
     * Pass 2 : items with a crafting pattern but zero stock → craftable-only
     *           entries (count=0, craftable=true) for middle-click autocraft.
     *
     * Merged by AEItemKey — items present in both passes get count from storage
     * and craftable=true. Sorted: stocked items first (count desc), then
     * craftable-only alphabetically.
     */
    public static TerminalMeSyncPacket fromGrid(
            appeng.api.networking.IGrid grid,
            BlockPos terminalPos)
    {
        KeyCounter inventory       = grid.getStorageService().getCachedInventory();
        var        craftingService = grid.getCraftingService();

        // AEKeyFilter that accepts only AEItemKey entries (items, not fluids etc.)
        AEKeyFilter itemsOnly = key -> key instanceof AEItemKey;

        // Keyed by AEItemKey → long[2] = { count, craftableFlag (0 or 1) }
        java.util.Map<AEItemKey, long[]> merged = new LinkedHashMap<>();

        // ── Pass 1 : items in storage ─────────────────────────────────────────
        for (var entry : inventory)
        {
            AEKey key = entry.getKey();
            if (!(key instanceof AEItemKey aeKey)) continue;
            long    count     = entry.getLongValue();
            boolean craftable = craftingService.isCraftable(aeKey);
            merged.put(aeKey, new long[]{ count, craftable ? 1L : 0L });
        }

        // ── Pass 2 : craftable-only items (have pattern, zero stock) ──────────
        // getCraftables(AEKeyFilter) returns a Set<AEKey> of all output keys
        // for which at least one pattern exists in the network,
        // filtered by the provided AEKeyFilter.
        for (AEKey key : craftingService.getCraftables(itemsOnly))
        {
            if (!(key instanceof AEItemKey aeKey)) continue;
            if (!merged.containsKey(aeKey))
            {
                // Not in storage → add as count=0, craftable=true
                merged.put(aeKey, new long[]{ 0L, 1L });
            }
            else
            {
                // Already in pass 1 — ensure craftable flag is set
                merged.get(aeKey)[1] = 1L;
            }
        }

        // ── Build entry list ──────────────────────────────────────────────────
        List<MeEntry> entries = new ArrayList<>(merged.size());
        for (var kv : merged.entrySet())
        {
            AEItemKey aeKey = kv.getKey();
            long      count = kv.getValue()[0];
            boolean   craft = kv.getValue()[1] != 0L;
            entries.add(new MeEntry(aeKey.toStack(1), count, craft));
        }

        // Stocked items first (count desc), then craftable-only alphabetically
        entries.sort((a, b) -> {
            if (a.count() > 0 && b.count() <= 0) return -1;
            if (a.count() <= 0 && b.count() > 0) return  1;
            if (a.count() > 0) return Long.compare(b.count(), a.count());
            return a.stack().getDisplayName().getString()
                    .compareToIgnoreCase(b.stack().getDisplayName().getString());
        });

        return new TerminalMeSyncPacket(entries, terminalPos);
    }
}