package com.colonylink.colonylink;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
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

            try
            {
                // Equivalent du bouton "Restart" vanilla MineColonies :
                // retire et remet le work order dans la file pour forcer le recalcul
                var workOrder = builderBuilding.getWorkOrder();
                if (workOrder != null)
                {
                    int woId = workOrder.getID();
                    colony.getWorkManager().removeWorkOrder(woId);
                    colony.getWorkManager().addWorkOrder(workOrder, false);
                }

                // Remet a zero la liste de ressources pour forcer un recalcul complet
                builderBuilding.resetNeededResources();

                // Marque le batiment comme modifie pour forcer la sync
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