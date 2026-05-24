package com.colonylink.colonylink;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * DomumEncodePatternPacket — ColonyLink v1.4.2
 *
 * Envoyé par le client quand le joueur clique "Encode" dans l'onglet Cutter.
 * Utilise RegistryFriendlyByteBuf + ItemStack.STREAM_CODEC (NeoForge 1.21.1).
 *
 * Résultat serveur :
 *   - Vérifie que targetStack est un item Domum valide
 *   - Consomme 1× Blank Pattern de l'inventaire du joueur
 *   - Produit 1× DomumPatternItem encodé → donné au joueur
 *   - Le targetStack N'est PAS consommé (comme AE2 Pattern Encoding Terminal)
 */
public record DomumEncodePatternPacket(
        BlockPos  hostPos,
        int       hostSide,
        ItemStack targetStack
) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<DomumEncodePatternPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(ColonyLink.MODID, "domum_encode_pattern"));

    // RegistryFriendlyByteBuf requis pour ItemStack.STREAM_CODEC (encode les DataComponents avec registries)
    public static final StreamCodec<RegistryFriendlyByteBuf, DomumEncodePatternPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, packet) -> {
                        buf.writeBlockPos(packet.hostPos);
                        buf.writeByte(packet.hostSide);
                        ItemStack.STREAM_CODEC.encode(buf, packet.targetStack);
                    },
                    buf -> new DomumEncodePatternPacket(
                            buf.readBlockPos(),
                            buf.readByte() & 0xFF,
                            ItemStack.STREAM_CODEC.decode(buf)
                    )
            );

    @Override
    public CustomPacketPayload.Type<DomumEncodePatternPacket> type() { return TYPE; }

    // ── Handler serveur ───────────────────────────────────────────────────────

    public static void handle(DomumEncodePatternPacket packet, IPayloadContext ctx)
    {
        ctx.enqueueWork(() -> handleServer((ServerPlayer) ctx.player(), packet));
    }

    private static void handleServer(ServerPlayer player, DomumEncodePatternPacket packet)
    {
        if (!DomumCraftHandler.isDomumItem(packet.targetStack))
        {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§c[ColonyLink] Invalid Domum item — cannot encode pattern."));
            return;
        }

        HolderLookup.Provider provider = player.serverLevel().registryAccess();

        // Le blank pattern est dans le slot 48 du menu (WarehouseLinkTerminalMenu)
        // Les slots Domum sont à x=-2000 mais leur ItemStackHandler est accessible via le menu
        ItemStack blank = findBlankInMenu(player);
        if (blank.isEmpty())
        {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§c[ColonyLink] No Blank Pattern in slot."));
            return;
        }

        // Fix 3: bloquer l'encodage si le slot output contient déjà un pattern
        if (player.containerMenu instanceof WarehouseLinkTerminalMenu tm
                && !tm.domumOutputSlot.getStackInSlot(0).isEmpty())
        {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§e[ColonyLink] Take the encoded pattern from the output slot first."));
            return;
        }

        // Récupère le count depuis la recette Domum via RecipeManager
        int outputCount = DomumRecipeHelper.getOutputCount(packet.targetStack, player.serverLevel());

        ItemStack encoded = DomumPatternItem.encode(packet.targetStack, provider, outputCount);
        if (encoded.isEmpty())
        {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§c[ColonyLink] Pattern encoding failed."));
            return;
        }

        // Consomme le Blank Pattern du slot 48
        blank.shrink(1);

        // Injecte le pattern encodé dans le slot output (49) du menu
        // Il y reste jusqu'à ce que le joueur le prenne manuellement
        if (player.containerMenu instanceof WarehouseLinkTerminalMenu terminalMenu)
        {
            // Slot output = domumOutputSlot du Part (via le menu)
            // Le slot est read-only pour le dépôt mais on peut y écrire directement
            terminalMenu.domumOutputSlot.setStackInSlot(0, encoded);
        }
        else
        {
            // Fallback : donne directement si le menu n'est pas ouvert
            if (!player.getInventory().add(encoded.copy()))
                player.drop(encoded, false);
        }

        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§a[ColonyLink] Domum Pattern encoded — take it from the output slot."));
    }

    /**
     * Cherche le blank pattern dans le slot 48 du menu ouvert par le joueur.
     * Les slots Domum sont à x=-2000 mais leurs ItemStackHandlers sont dans le menu.
     * Retourne l'ItemStack (référence directe) ou EMPTY si absent/mauvais type.
     */
    private static ItemStack findBlankInMenu(ServerPlayer player)
    {
        if (!(player.containerMenu instanceof WarehouseLinkTerminalMenu menu)) return ItemStack.EMPTY;
        // Le slot 48 = BLANK_PATTERN_SLOT dans le menu
        // On accède directement à l'ItemStackHandler du menu
        ItemStack s = menu.blankPatternSlot.getStackInSlot(0);
        if (s.isEmpty()) return ItemStack.EMPTY;
        ResourceLocation id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(s.getItem());
        if (id != null && id.getPath().equals("blank_pattern")) return s;
        return ItemStack.EMPTY;
    }
}