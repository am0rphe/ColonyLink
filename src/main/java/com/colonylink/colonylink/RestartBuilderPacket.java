package com.colonylink.colonylink;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.core.colony.buildings.AbstractBuildingStructureBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RestartBuilderPacket(BlockPos builderPos) implements CustomPacketPayload
{
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
            ColonyLink.MODID, "restart_builder");
    public static final CustomPacketPayload.Type<RestartBuilderPacket> TYPE =
            new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, RestartBuilderPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeBlockPos(packet.builderPos()),
            buf -> new RestartBuilderPacket(buf.readBlockPos())
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public static void handle(RestartBuilderPacket packet, IPayloadContext context)
    {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;

            ServerLevel level = serverPlayer.serverLevel();
            BlockPos builderPos = packet.builderPos();

            // v1.7.1 — validate builderPos against the player's wand (same guard as LocateBuilderPacket).
            net.minecraft.world.item.ItemStack wandStack = ColonyLinkServerTicker.findWandInInventory(serverPlayer);
            if (wandStack == null)
            {
                serverPlayer.sendSystemMessage(Component.translatable("colonylink.whc.clipboard_not_found"));
                return;
            }
            boolean builderLinked = false;
            for (BuilderEntry e : ColonyLinkWandLinkableHandler.getBuilderEntries(wandStack))
            {
                if (e.builderPos().equals(builderPos)) { builderLinked = true; break; }
            }
            if (!builderLinked)
            {
                serverPlayer.sendSystemMessage(Component.translatable("colonylink.stw.invalid_request"));
                return;
            }

            IColony colony = IColonyManager.getInstance().getClosestColony(level, builderPos);
            if (colony == null)
            {
                serverPlayer.sendSystemMessage(
                        Component.translatable("colonylink.whs.no_colony"));
                return;
            }

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

            // v1.6.2 — server-side permission: builderPos is client-supplied and
            // mutating another colony's work order (restart / resetNeededResources)
            // is griefing. Require ACCESS_HUTS, like every other colony mutation.
            if (!colony.getPermissions().hasPermission(serverPlayer, Action.ACCESS_HUTS))
            {
                serverPlayer.sendSystemMessage(
                        Component.translatable("colonylink.wand.msg.no_permission"));
                return;
            }

            try
            {
                // Equivalent to vanilla MineColonies' "Restart" button: remove and
                // re-add the work order to the queue to force a recompute.
                var workOrder = builderBuilding.getWorkOrder();
                if (workOrder != null)
                {
                    int woId = workOrder.getID();
                    colony.getWorkManager().removeWorkOrder(woId);
                    colony.getWorkManager().addWorkOrder(workOrder, false);
                }

                // Reset the resource list to force a full recompute.
                builderBuilding.resetNeededResources();

                // Mark the building dirty to force a sync.
                building.markDirty();

                serverPlayer.sendSystemMessage(
                        Component.translatable("colonylink.restart.success"));
            }
            catch (Exception e)
            {
                ColonyLink.LOGGER.error("[ColonyLink] Error restarting builder: {}", e.getMessage());
                serverPlayer.sendSystemMessage(
                        Component.translatable("colonylink.restart.failed", e.getMessage()));
            }
        });
    }
}