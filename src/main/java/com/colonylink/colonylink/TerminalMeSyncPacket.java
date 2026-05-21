package com.colonylink.colonylink;

import appeng.api.stacks.AEItemKey;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * S→C : snapshot du ME storage vers le client (items + craftables).
 *
 * Le handler client est dans TerminalClientPacketHandler (@OnlyIn(CLIENT))
 * pour éviter que RuntimeDistCleaner ne crashe le serveur dédié.
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
                        buf.writeBlockPos(p.terminalPos());
                        buf.writeInt(p.entries().size());
                        for (var e : p.entries())
                        {
                            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, e.stack());
                            buf.writeLong(e.count());
                            buf.writeBoolean(e.craftable());
                        }
                    },
                    buf -> {
                        BlockPos pos = buf.readBlockPos();
                        int size = buf.readInt();
                        List<MeEntry> entries = new ArrayList<>();
                        for (int i = 0; i < size; i++)
                            entries.add(new MeEntry(
                                    ItemStack.OPTIONAL_STREAM_CODEC.decode(buf),
                                    buf.readLong(),
                                    buf.readBoolean()));
                        return new TerminalMeSyncPacket(entries, pos);
                    }
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    /**
     * Construit le snapshot ME depuis la grille AE2.
     *
     * Utilise getCachedInventory() (KeyCounter) pour itérer les items stockés,
     * et isCraftable() pour marquer les items autocratable.
     * Pour les items craftables non stockés : getCraftables(AEKeyFilter)
     * avec un filtre passthrough (lambda acceptant tous les items).
     */
    public static TerminalMeSyncPacket fromGrid(appeng.api.networking.IGrid grid, BlockPos pos)
    {
        appeng.api.stacks.KeyCounter inv = grid.getStorageService().getCachedInventory();
        appeng.api.networking.crafting.ICraftingService crafting = grid.getCraftingService();
        List<MeEntry> entries = new ArrayList<>();

        // Items en stock — KeyCounter est Iterable<Map.Entry<AEKey, Long>>
        for (var entry : inv)
        {
            if (!(entry.getKey() instanceof AEItemKey aeKey)) continue;
            boolean craftable = crafting.isCraftable(aeKey);
            entries.add(new MeEntry(aeKey.toStack(1), entry.getLongValue(), craftable));
        }

        // Items craftables non stockés — getCraftables(AEKeyFilter) en AE2 19.x
        try
        {
            var craftables = crafting.getCraftables(k -> true);
            for (var key : craftables)
            {
                if (!(key instanceof AEItemKey aeKey)) continue;
                if (entries.stream().anyMatch(e ->
                        ItemStack.isSameItemSameComponents(e.stack(), aeKey.toStack(1))))
                    continue;
                entries.add(new MeEntry(aeKey.toStack(1), 0, true));
            }
        }
        catch (Exception ignored) {} // sécurité si l'API change

        entries.sort((a, b) -> Long.compare(b.count(), a.count()));
        return new TerminalMeSyncPacket(entries, pos);
    }
}