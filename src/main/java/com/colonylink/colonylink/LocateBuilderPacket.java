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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * LocateBuilderPacket — v1.4.8
 *
 * Envoyé par le client quand le joueur clique sur le bouton "Locate" du Clipboard.
 * Le serveur applique l'effet Glowing vanilla au NPC builder assigné pendant
 * LOCATE_GLOW_DURATION_SECONDS secondes (configurable, op-only).
 *
 * Design :
 * - Cherche le NPC via AbstractBuildingStructureBuilder.getAllAssignedCitizen()
 * - Parcourt les entités chargées du level pour trouver l'UUID correspondant
 * - Applique MobEffects.GLOWING sans particule (amplifier 0, showParticles false)
 * - Si le NPC est dans un chunk non chargé → message d'erreur au joueur
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

    /** Cooldown anti-flood : UUID joueur → timestamp last use (ms). */
    private static final java.util.Map<java.util.UUID, Long> COOLDOWNS =
            new java.util.concurrent.ConcurrentHashMap<>();
    private static final long COOLDOWN_MS = 10_000L; // 10 secondes

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

            // ── Cooldown anti-flood ───────────────────────────────────────────
            long now = System.currentTimeMillis();
            Long last = COOLDOWNS.get(serverPlayer.getUUID());
            if (last != null && now - last < COOLDOWN_MS)
            {
                long remaining = (COOLDOWN_MS - (now - last)) / 1000 + 1;
                serverPlayer.sendSystemMessage(
                        Component.literal("§e[ColonyLink] Locate on cooldown — wait §f"
                                + remaining + "s§e."));
                return;
            }
            COOLDOWNS.put(serverPlayer.getUUID(), now);

            ServerLevel level = serverPlayer.serverLevel();
            BlockPos builderPos = packet.builderPos();

            // ── Trouve la colonie ─────────────────────────────────────────────
            IColony colony = IColonyManager.getInstance().getClosestColony(level, builderPos);
            if (colony == null)
            {
                serverPlayer.sendSystemMessage(
                        Component.literal("§c[ColonyLink] No colony found!"));
                return;
            }

            // ── Trouve le bâtiment ────────────────────────────────────────────
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
                        Component.literal("§c[ColonyLink] Builder's Hut not found!"));
                return;
            }

            // ── Trouve le citoyen assigné ─────────────────────────────────────
            if (bb.getAllAssignedCitizen().isEmpty())
            {
                serverPlayer.sendSystemMessage(
                        Component.literal("§e[ColonyLink] No builder assigned to this hut."));
                return;
            }

            var citizenData = bb.getAllAssignedCitizen().iterator().next();
            java.util.UUID citizenUUID = citizenData.getUUID();

            // ── Cherche l'entité dans le level ────────────────────────────────
            // MineColonies expose l'UUID du citoyen via ICitizenData.getId().
            // On itère les entités chargées pour trouver le mob correspondant.
            Entity targetEntity = null;
            for (Entity entity : level.getAllEntities())
            {
                // Les citoyens MC exposent leur UUID dans getUUID() standard
                if (entity.getUUID().equals(citizenUUID))
                {
                    targetEntity = entity;
                    break;
                }
            }

            if (targetEntity == null)
            {
                serverPlayer.sendSystemMessage(
                        Component.literal("§e[ColonyLink] Builder §f" + citizenData.getName()
                                + "§e is not in a loaded chunk."));
                return;
            }

            // ── Consomme le RF ────────────────────────────────────────────────
            long cost = ColonyLinkConfig.LOCATE_COST_RF.get();
            if (cost > 0 && !ColonyLinkServerTicker.tryConsumeRF(serverPlayer, cost))
            {
                serverPlayer.sendSystemMessage(
                        Component.literal("§c[ColonyLink] Not enough RF to locate! (need "
                                + cost + " RF)"));
                return;
            }

            // ── Applique le Glowing ───────────────────────────────────────────
            int durationTicks = ColonyLinkConfig.LOCATE_GLOW_DURATION_SECONDS.get() * 20;

            if (targetEntity instanceof net.minecraft.world.entity.LivingEntity living)
            {
                living.addEffect(new MobEffectInstance(
                        MobEffects.GLOWING,
                        durationTicks,
                        0,       // amplifier (unused for glowing)
                        false,   // ambient
                        false,   // showParticles — pas de particules pour être propre
                        true     // showIcon dans l'UI du mob
                ));

                serverPlayer.sendSystemMessage(
                        Component.literal("§a[ColonyLink] §f" + citizenData.getName()
                                + "§a is glowing for "
                                + ColonyLinkConfig.LOCATE_GLOW_DURATION_SECONDS.get()
                                + "s!"));
            }
            else
            {
                serverPlayer.sendSystemMessage(
                        Component.literal("§c[ColonyLink] Builder entity is not a LivingEntity (unexpected)."));
            }
        });
    }
}