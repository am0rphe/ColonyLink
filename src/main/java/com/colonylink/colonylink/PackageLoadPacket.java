package com.colonylink.colonylink;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Packet C→S : le joueur clique sur le slot Package dans la tab Citizens
 * pour charger des packages depuis son inventaire dans la wand.
 *
 * Comportement :
 *   - Cherche des ColonyLink Packages dans l'inventaire du joueur
 *   - Les transfère dans le compteur NBT de la wand (max 64 stockés)
 *   - Sync le nouveau count au client via PackageTokenSyncPacket
 */
public record PackageLoadPacket() implements CustomPacketPayload
{
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
            ColonyLink.MODID, "package_load");
    public static final CustomPacketPayload.Type<PackageLoadPacket> TYPE =
            new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, PackageLoadPacket> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {},
            buf -> new PackageLoadPacket()
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PackageLoadPacket packet, IPayloadContext context)
    {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer sp)) return;
            handleLoad(sp);
        });
    }

    private static void handleLoad(ServerPlayer player)
    {
        ItemStack wand = findWandInInventory(player);
        if (wand == null) return;

        int current = ColonyLinkWandLinkableHandler.getCitizenPackages(wand);
        int maxStore = 64;
        int space = maxStore - current;
        if (space <= 0)
        {
            player.sendSystemMessage(Component.literal(
                    "§e[ColonyLink] Clipboard is full (64/64 packages stored)."));
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,
                    new PackageTokenSyncPacket(current));
            return;
        }

        // Chercher des packages dans l'inventaire
        int loaded = 0;
        for (ItemStack s : player.getInventory().items)
        {
            if (space <= 0) break;
            if (!(s.getItem() instanceof ColonyLinkPackage)) continue;
            int take = Math.min(s.getCount(), space);
            s.shrink(take);
            loaded += take;
            space  -= take;
        }

        if (loaded == 0)
        {
            player.sendSystemMessage(Component.literal(
                    "§c[ColonyLink] No ColonyLink Packages found in inventory!"));
        }
        else
        {
            ColonyLinkWandLinkableHandler.addCitizenPackages(wand, loaded);
            int newCount = ColonyLinkWandLinkableHandler.getCitizenPackages(wand);
            player.sendSystemMessage(Component.literal(
                    "§a[ColonyLink] Loaded §f" + loaded + " package"
                            + (loaded != 1 ? "s" : "") + " §ainto Clipboard (§f" + newCount + "§a stored)."));
        }

        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,
                new PackageTokenSyncPacket(ColonyLinkWandLinkableHandler.getCitizenPackages(wand)));
    }

    private static ItemStack findWandInInventory(ServerPlayer player)
    {
        // Delegate to the shared implementation that also checks Curios slots.
        return ColonyLinkServerTicker.findWandInInventory(player);
    }
}