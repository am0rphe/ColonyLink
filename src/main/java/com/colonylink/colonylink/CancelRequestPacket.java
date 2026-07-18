package com.colonylink.colonylink;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.core.colony.buildings.AbstractBuildingStructureBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * C→S — annule la Priority Request affichée pour le builder ciblé.
 *
 * Modèle de sécurité : identique à {@link WarehouseCraftHandler} (PAS
 * {@link RestartBuilderPacket}, qui ne fait que ACCESS_HUTS). Le client n'est
 * jamais autoritatif : {@code redirectorPos} DOIT correspondre à un redirector
 * réellement lié à la wand de CE joueur. L'entry ainsi validé fournit le
 * {@code builderPos} — on ne fait pas confiance à une position brute du client.
 *
 * La request annulée est re-dérivée serveur (première open request non
 * CANCELLED/OVERRULED de type IDeliverable), exactement comme la passe 1 de
 * {@code ColonyLinkServerTicker.fetchBuilderRequest}. On ne matche JAMAIS par
 * l'item affiché : si la substitution d'outils est active, l'item affiché est le
 * substitut, pas l'item de la request. Le bouton est sur la ligne prioritaire =
 * la première open request, donc la cible reste cohérente.
 *
 * L'appel d'annulation mirror 1:1 le message natif du GUI builder
 * (UpdateRequestStateMessage → updateRequestState(token, CANCELLED)). Le
 * IRequestManager interne gère la cascade (child-requests, resolver).
 */
public record CancelRequestPacket(BlockPos redirectorPos) implements CustomPacketPayload
{
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
            ColonyLink.MODID, "cancel_request");
    public static final CustomPacketPayload.Type<CancelRequestPacket> TYPE =
            new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, CancelRequestPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeBlockPos(packet.redirectorPos()),
            buf -> new CancelRequestPacket(buf.readBlockPos())
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public static void handle(CancelRequestPacket packet, IPayloadContext context)
    {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;

            ServerLevel level = serverPlayer.serverLevel();
            BlockPos redirectorPos = packet.redirectorPos();

            // ── Détection wand (inclut Curios) ────────────────────────────────
            ItemStack wandStack = ColonyLinkServerTicker.findWandInInventory(serverPlayer);
            if (wandStack == null || !ColonyLinkWandLinkableHandler.isLinked(wandStack))
            {
                serverPlayer.sendSystemMessage(
                        Component.translatable("colonylink.whc.clipboard_not_linked"));
                return;
            }

            // ── Validation redirector↔wand (client NON autoritatif) ───────────
            // redirectorPos doit être un redirector réellement lié à CETTE wand.
            // L'entry validé nous donne le builderPos — on ne fait pas confiance
            // à une position de builder envoyée par le client.
            BlockPos builderPos = null;
            for (BuilderEntry e : ColonyLinkWandLinkableHandler.getBuilderEntries(wandStack))
            {
                if (e.hasRedirector() && e.redirectorPos().equals(redirectorPos))
                {
                    // Garde dimension : refuse un cancel cross-dimension mal résolu.
                    if (e.dimension() != null && !e.dimension().equals(level.dimension()))
                        break;
                    builderPos = e.builderPos();
                    break;
                }
            }
            if (builderPos == null)
            {
                serverPlayer.sendSystemMessage(
                        Component.translatable("colonylink.stw.invalid_request"));
                return;
            }

            // ── Colonie (résolue via le builderPos wand-validé) ───────────────
            IColony colony = IColonyManager.getInstance().getClosestColony(level, builderPos);
            if (colony == null)
            {
                serverPlayer.sendSystemMessage(
                        Component.translatable("colonylink.whs.no_colony"));
                return;
            }

            // ── Permission colonie, côté serveur ──────────────────────────────
            if (!colony.getPermissions().hasPermission(serverPlayer, Action.ACCESS_HUTS))
            {
                serverPlayer.sendSystemMessage(
                        Component.translatable("colonylink.wand.msg.no_permission"));
                return;
            }

            // ── Résolution du Builder's Hut ───────────────────────────────────
            IBuilding building = null;
            for (IBuilding b : colony.getServerBuildingManager().getBuildings().values())
            {
                if (b.getPosition().equals(builderPos))
                {
                    building = b;
                    break;
                }
            }
            if (!(building instanceof AbstractBuildingStructureBuilder builderBuilding))
            {
                serverPlayer.sendSystemMessage(
                        Component.translatable("colonylink.handler.hut_not_found"));
                return;
            }

            try
            {
                // Re-dérive la Priority Request = première open request non
                // CANCELLED/OVERRULED de type IDeliverable (mirror fetchBuilderRequest
                // passe 1). PAS de match par item (évite le décalage substitution).
                IRequest<?> target = null;
                if (!builderBuilding.getAllAssignedCitizen().isEmpty())
                {
                    var citizen = builderBuilding.getAllAssignedCitizen().iterator().next();
                    var reqs = builderBuilding.getOpenRequests(citizen.getId());
                    if (reqs != null)
                    {
                        for (IRequest<?> req : reqs)
                        {
                            if (req.getState() == RequestState.CANCELLED
                                    || req.getState() == RequestState.OVERRULED) continue;
                            if (!(req.getRequest() instanceof IDeliverable)) continue;
                            target = req;
                            break;
                        }
                    }
                }

                if (target == null)
                {
                    serverPlayer.sendSystemMessage(
                            Component.translatable("colonylink.priority.cancel_none"));
                    return;
                }

                // Annulation native 1:1 (UpdateRequestStateMessage → CANCELLED).
                // Le IRequestManager gère la cascade (child-requests, resolver).
                colony.getRequestManager().updateRequestState(target.getId(), RequestState.CANCELLED);
                building.markDirty();

                serverPlayer.sendSystemMessage(
                        Component.translatable("colonylink.priority.cancelled"));
            }
            catch (Exception e)
            {
                ColonyLink.LOGGER.error("[ColonyLink] Error cancelling builder request: {}", e.getMessage());
                serverPlayer.sendSystemMessage(
                        Component.translatable("colonylink.priority.cancel_none"));
            }
        });
    }
}
