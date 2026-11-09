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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * LocateBuilderPacket — v1.4.8
 *
 * Sent by the client when the player clicks the Clipboard "Locate" button.
 * The server applies the vanilla Glowing effect to the assigned builder NPC for
 * LOCATE_GLOW_DURATION_SECONDS seconds (configurable, op-only).
 *
 * Design:
 * - Find the NPC via AbstractBuildingStructureBuilder.getAllAssignedCitizen()
 * - Iterate loaded level entities to find the matching UUID
 * - Apply MobEffects.GLOWING without particles (amplifier 0, showParticles false)
 * - If the NPC is in an unloaded chunk → error message to the player
 */
public record LocateBuilderPacket(BlockPos builderPos) implements CustomPacketPayload
{
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
            ColonyLink.MODID, "locate_builder");
    public static final CustomPacketPayload.Type<LocateBuilderPacket> TYPE =
            new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, LocateBuilderPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, packet) -> buf.writeBlockPos(packet.builderPos()),
                    buf -> new LocateBuilderPacket(buf.readBlockPos())
            );

    /** Anti-flood cooldown: player UUID → last-use timestamp (ms). */
    private static final java.util.Map<java.util.UUID, Long> COOLDOWNS =
            new java.util.concurrent.ConcurrentHashMap<>();
    private static final long COOLDOWN_MS = 10_000L; // 10 seconds

    /** v1.7.1 — called from ColonyLinkServerTicker.onServerStopping to prevent leaks. */
    public static void clearCooldowns() { COOLDOWNS.clear(); }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public static void handle(LocateBuilderPacket packet, IPayloadContext context)
    {
        context.enqueueWork(() ->
        {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;

            // Anti-flood cooldown
            long now = System.currentTimeMillis();
            Long last = COOLDOWNS.get(serverPlayer.getUUID());
            if (last != null && now - last < COOLDOWN_MS)
            {
                long remaining = (COOLDOWN_MS - (now - last)) / 1000 + 1;
                serverPlayer.sendSystemMessage(
                        Component.translatable("colonylink.locate.cooldown", remaining));
                return;
            }
            COOLDOWNS.put(serverPlayer.getUUID(), now);

            ServerLevel level = serverPlayer.serverLevel();
            BlockPos builderPos = packet.builderPos();

            // 1.7.0 — the client is not authoritative on builderPos: it must be a
            // builder linked to THIS player's Clipboard (same check as SendToWarehousePacket).
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

            // Find the colony
            IColony colony = IColonyManager.getInstance().getClosestColony(level, builderPos);
            if (colony == null)
            {
                serverPlayer.sendSystemMessage(
                        Component.translatable("colonylink.whs.no_colony"));
                return;
            }

            // 1.7.0 — colony permission, same action as the other Clipboard handlers.
            if (!colony.getPermissions().hasPermission(serverPlayer, Action.ACCESS_HUTS))
            {
                serverPlayer.sendSystemMessage(Component.translatable("colonylink.wand.msg.no_permission"));
                return;
            }

            // Find the building
            IBuilding building = null;
            for (IBuilding b : colony.getServerBuildingManager().getBuildings().values())
            {
                if (b.getPosition().equals(builderPos))
                {
                    building = b;
                    break;
                }
            }

            if (!(building instanceof AbstractBuildingStructureBuilder bb))
            {
                serverPlayer.sendSystemMessage(
                        Component.translatable("colonylink.handler.hut_not_found"));
                return;
            }

            // Find the assigned citizen
            if (bb.getAllAssignedCitizen().isEmpty())
            {
                serverPlayer.sendSystemMessage(
                        Component.translatable("colonylink.locate.no_builder"));
                return;
            }

            var citizenData = bb.getAllAssignedCitizen().iterator().next();
            java.util.UUID citizenUUID = citizenData.getUUID();

            // Find the entity in the level
            Entity targetEntity = null;
            for (Entity entity : level.getAllEntities())
            {
                // MC citizens expose their UUID via the standard getUUID()
                if (entity.getUUID().equals(citizenUUID))
                {
                    targetEntity = entity;
                    break;
                }
            }

            if (targetEntity == null)
            {
                serverPlayer.sendSystemMessage(
                        Component.translatable("colonylink.locate.not_loaded", citizenData.getName()));
                return;
            }

            // Consume RF
            long cost = ColonyLinkConfig.LOCATE_COST_RF.get();
            if (cost > 0 && !ColonyLinkServerTicker.tryConsumeRF(serverPlayer, cost))
            {
                serverPlayer.sendSystemMessage(
                        Component.translatable("colonylink.locate.not_enough_rf", cost));
                return;
            }

            // Apply Glowing
            int durationTicks = ColonyLinkConfig.LOCATE_GLOW_DURATION_SECONDS.get() * 20;

            if (targetEntity instanceof net.minecraft.world.entity.LivingEntity living)
            {
                living.addEffect(new MobEffectInstance(
                        MobEffects.GLOWING,
                        durationTicks,
                        0,       // amplifier (unused for glowing)
                        false,   // ambient
                        false,   // showParticles — none, to keep it clean
                        true     // showIcon in the mob UI
                ));

                serverPlayer.sendSystemMessage(
                        Component.translatable("colonylink.locate.glowing", citizenData.getName(), ColonyLinkConfig.LOCATE_GLOW_DURATION_SECONDS.get()));
            }
            else
            {
                serverPlayer.sendSystemMessage(
                        Component.translatable("colonylink.locate.not_living"));
            }
        });
    }
}