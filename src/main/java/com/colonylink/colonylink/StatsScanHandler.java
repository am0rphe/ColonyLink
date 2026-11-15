package com.colonylink.colonylink;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.managers.interfaces.ICitizenManager;
import com.minecolonies.api.colony.permissions.Action;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Set;

/**
 * v1.8.0 — builds and sends the Stats tab payload ({@link StatsPacket}).
 *
 * Colony resolution mirrors {@link CitizensScanHandler}: find the player's wand,
 * resolve the colony from its linked builder entries (dimension-guarded), and
 * require {@code ACCESS_HUTS}. Activity counters come from the persisted
 * {@link ColonyLinkStats}; the Colony Overview is read live from MineColonies.
 *
 * Server-side only (called from the server packet handler).
 */
public final class StatsScanHandler
{
    private StatsScanHandler() {}

    // MineColonies guard job ids (knight = melee, ranger = archer, druid = support).
    private static final Set<String> GUARD_JOBS = Set.of("knight", "ranger", "druid");

    public static void sendStatsPacket(ServerPlayer player)
    {
        final ItemStack wand = ColonyLinkServerTicker.findWandInInventory(player);
        if (wand == null) return;

        final ServerLevel level = player.serverLevel();

        final List<BuilderEntry> entries = ColonyLinkWandLinkableHandler.getBuilderEntries(wand);
        if (entries.isEmpty()) return;

        // Resolve the colony from a builder entry in THIS dimension (same guard as
        // CitizensScanHandler: never resolve across dimensions).
        IColony colony = null;
        for (final BuilderEntry e : entries)
        {
            if (e.dimension() != null && !e.dimension().equals(level.dimension())) continue;
            colony = IColonyManager.getInstance().getClosestColony(level, e.builderPos());
            if (colony != null) break;
        }
        if (colony == null) return;

        if (!colony.getPermissions().hasPermission(player, Action.ACCESS_HUTS))
        {
            player.sendSystemMessage(Component.translatable("colonylink.wand.msg.no_permission"));
            return;
        }

        // Activity counters (persisted, cumulative). Stored in the colony's own
        // dimension storage.
        final long[] act;
        if (colony.getWorld() instanceof ServerLevel colonyLevel)
            act = ColonyLinkStats.get(colonyLevel).snapshot(colony.getID());
        else
            act = new long[4];

        // Colony Overview (live).
        final ICitizenManager cm = colony.getCitizenManager();
        final int citizens    = cm.getCurrentCitizenCount();
        final int maxCitizens = cm.getMaxCitizens();

        int guards = 0, builders = 0;
        double happinessSum = 0.0;
        int happinessCount = 0;
        long distSum = 0;
        int distCount = 0;

        for (final ICitizenData data : cm.getCitizens())
        {
            final IJob<?> job = data.getJob();
            if (job != null && job.getJobRegistryEntry() != null)
            {
                final String jobPath = job.getJobRegistryEntry().getKey().getPath();
                if (jobPath.equals("builder")) builders++;
                else if (GUARD_JOBS.contains(jobPath)) guards++;
            }

            try
            {
                happinessSum += data.getCitizenHappinessHandler().getHappiness(colony, data);
                happinessCount++;
            }
            catch (final Exception ignored) {}

            final IBuilding home = data.getHomeBuilding();
            final IBuilding work = data.getWorkBuilding();
            if (home != null && work != null)
            {
                distSum += Math.round(Math.sqrt(home.getID().distSqr(work.getID())));
                distCount++;
            }
        }

        // Happiness is a 0..10 score; present it as a percentage, clamped.
        int happinessPercent = happinessCount > 0
                ? (int) Math.round(happinessSum / happinessCount / 10.0 * 100.0) : 0;
        happinessPercent = Math.max(0, Math.min(100, happinessPercent));

        final int avgBedToWork = distCount > 0 ? (int) (distSum / distCount) : -1;

        PacketDistributor.sendToPlayer(player, new StatsPacket(
                act[ColonyLinkStats.IDX_ITEMS],
                act[ColonyLinkStats.IDX_AE2],
                act[ColonyLinkStats.IDX_DOMUM],
                act[ColonyLinkStats.IDX_TOMES],
                citizens, maxCitizens, guards, builders,
                happinessPercent, avgBedToWork));
    }
}
