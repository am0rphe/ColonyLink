package com.colonylink.colonylink;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.common.networking.WirelessTransmitterBlockEntity;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.core.colony.buildings.AbstractBuildingStructureBuilder;
import com.minecolonies.core.colony.buildings.utils.BuildingBuilderResource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ColonyLink Wand RS — v1.1.4
 *
 * Wand pour Refined Storage 2. Comportement strictement identique à ColonyLinkWand (AE2).
 *
 * Différences vs AE2 :
 * - Appairage : Sneak + clic droit sur un Wireless Transmitter RS2 (au lieu du WAP AE2)
 * - Accès réseau : via WirelessTransmitterBlockEntity.getNetworkForItem() (Option C)
 * - Charge : uniquement via IEnergyStorage NeoForge (Powah, Mekanism, FluxNetworks, etc.)
 *   RS2 n'a pas de Charger bloc natif pour les items tiers.
 *
 * Charge FE :
 * La capability IEnergyStorage est enregistrée dans ColonyLinkRegistry sur cet item,
 * exactement comme pour la wand AE2 — même WandEnergyStorage, même RF capacity.
 */
public class ColonyLinkWandRS extends Item
{
    public ColonyLinkWandRS(Properties properties)
    {
        super(properties);
    }

    // ── Anti-pop ──────────────────────────────────────────────────────────────

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
    {
        return slotChanged || !ItemStack.isSameItem(oldStack, newStack);
    }

    // ── Barre durabilité ──────────────────────────────────────────────────────

    @Override
    public boolean isBarVisible(ItemStack stack) { return true; }

    @Override
    public int getBarWidth(ItemStack stack)
    {
        long stored   = WandEnergyStorage.getStoredRF(stack);
        long capacity = ColonyLinkConfig.WAND_RF_CAPACITY.get();
        if (capacity <= 0) return 0;
        return (int)(stored * 13L / capacity);
    }

    @Override
    public int getBarColor(ItemStack stack)
    {
        long stored   = WandEnergyStorage.getStoredRF(stack);
        long capacity = ColonyLinkConfig.WAND_RF_CAPACITY.get();
        int pct       = capacity > 0 ? (int)(stored * 100L / capacity) : 0;
        int threshold = ColonyLinkConfig.LOW_POWER_THRESHOLD_PERCENT.get();
        if (pct <= threshold) return 0xFF2222;
        if (pct <= 30)        return 0xFFAA00;
        return 0x22CC22;
    }

    // ── Tooltip ───────────────────────────────────────────────────────────────

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag)
    {
        long stored   = WandEnergyStorage.getStoredRF(stack);
        long capacity = ColonyLinkConfig.WAND_RF_CAPACITY.get();
        int pct       = capacity > 0 ? (int)(stored * 100L / capacity) : 0;
        int threshold = ColonyLinkConfig.LOW_POWER_THRESHOLD_PERCENT.get();
        String rfColor = pct <= threshold ? "§c" : (pct <= 30 ? "§e" : "§a");

        tooltip.add(Component.literal(rfColor + "⚡ " + formatRF(stored)
                + " §7/ §f" + formatRF(capacity) + " RF §7(" + pct + "%)"));

        if (stored <= 0)
            tooltip.add(Component.literal("§c⚠ OUT OF POWER — charge in any FE charger (Powah, Mekanism, etc.)"));

        boolean linkedRS2 = ColonyLinkWandRSLinkableHandler.isLinked(stack);
        List<BuilderEntry> entries = ColonyLinkWandRSLinkableHandler.getBuilderEntries(stack);
        int activeTab = ColonyLinkWandRSLinkableHandler.getActiveTab(stack);

        if (!linkedRS2)
        {
            tooltip.add(Component.literal("§c✘ Not linked to RS2 network"));
            tooltip.add(Component.literal("§8  → Sneak + Right-click a §fWireless Transmitter"));
        }
        else
        {
            tooltip.add(Component.literal("§a✔ Linked to RS2 network"));
            if (entries.isEmpty())
            {
                tooltip.add(Component.literal("§e⚠ No Builder linked"));
                tooltip.add(Component.literal("§8  → §fSneak + Right-click §8a Builder's Hut"));
            }
            else
            {
                tooltip.add(Component.literal("§a✔ " + entries.size() + "/"
                        + ColonyLinkConfig.MAX_BUILDERS_PER_WAND.get() + " Builder(s) linked"));
                BuilderEntry active = entries.get(Math.min(activeTab, entries.size() - 1));
                tooltip.add(Component.literal("§7Active: §f" + active.builderName()
                        + " §8@ " + active.builderPos().toShortString()));
                if (!active.hasRedirector())
                    tooltip.add(Component.literal("§e⚠ No Redirector linked for this builder"));
            }
        }

        tooltip.add(Component.literal("§8──────────────────"));
        tooltip.add(Component.literal("§7Right-click §8(air) → open resource GUI"));
        tooltip.add(Component.literal("§7Sneak + Right-click §8Wireless Transmitter → link to RS2 network"));
        tooltip.add(Component.literal("§7Sneak + Right-click §8Builder's Hut → add/link builder"));
        tooltip.add(Component.literal("§7Sneak + Right-click §8Redirector → link to active builder"));
        tooltip.add(Component.literal("§8Charge: §fany FE charger mod §8(Powah, Mekanism, FluxNetworks…)"));
    }

    private static String formatRF(long rf)
    {
        if (rf >= 1_000_000L) return String.format("%.1fM", rf / 1_000_000.0);
        if (rf >= 1_000L)     return String.format("%.1fK", rf / 1_000.0);
        return String.valueOf(rf);
    }

    // ── use() — clic droit AIR ────────────────────────────────────────────────

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        ItemStack wandStack = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResultHolder.pass(wandStack);
        if (player.isShiftKeyDown()) return InteractionResultHolder.pass(wandStack);
        if (!(player instanceof ServerPlayer sp)) return InteractionResultHolder.pass(wandStack);

        if (WandEnergyStorage.getStoredRF(wandStack) <= 0)
        {
            player.sendSystemMessage(Component.literal(
                    "§c[ColonyLink RS] Out of Power! Charge with any FE charger."));
            return InteractionResultHolder.fail(wandStack);
        }

        if (!ColonyLinkWandRSLinkableHandler.isLinked(wandStack))
        {
            player.sendSystemMessage(Component.literal(
                    "§c[ColonyLink RS] Not linked to a RS2 network! Sneak + Right-click a Wireless Transmitter."));
            return InteractionResultHolder.fail(wandStack);
        }

        List<BuilderEntry> entries = ColonyLinkWandRSLinkableHandler.getBuilderEntries(wandStack);
        if (entries.isEmpty())
        {
            player.sendSystemMessage(Component.literal(
                    "§e[ColonyLink RS] No Builder's Hut linked yet. Sneak + Right-click a Builder's Hut first."));
            return InteractionResultHolder.fail(wandStack);
        }

        int activeTab = ColonyLinkWandRSLinkableHandler.getActiveTab(wandStack);
        BuilderEntry active = entries.get(Math.min(activeTab, entries.size() - 1));

        return openGUI(wandStack, sp, (ServerLevel) level, active.builderPos(), activeTab)
                ? InteractionResultHolder.success(wandStack)
                : InteractionResultHolder.fail(wandStack);
    }

    // ── useOn() — clic droit sur BLOC ─────────────────────────────────────────

    @Override
    public net.minecraft.world.InteractionResult useOn(UseOnContext context)
    {
        Level level   = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos  = context.getClickedPos();

        if (level.isClientSide() || player == null)
            return net.minecraft.world.InteractionResult.PASS;
        if (!(player instanceof ServerPlayer sp))
            return net.minecraft.world.InteractionResult.PASS;

        ItemStack wandStack = null;
        for (ItemStack s : player.getInventory().items)
            if (s.getItem() instanceof ColonyLinkWandRS) { wandStack = s; break; }
        if (wandStack == null) wandStack = context.getItemInHand();

        var be = level.getBlockEntity(pos);

        // Clic sur Redirector RS2 sans sneak → pas d'action (le GUI buffer s'ouvre via le bloc)
        if (be instanceof ColonyLinkRedirectorBlockEntityRS && !player.isShiftKeyDown())
            return net.minecraft.world.InteractionResult.SUCCESS;

        if (!player.isShiftKeyDown())
            return net.minecraft.world.InteractionResult.PASS;

        // ── Sneak + Wireless Transmitter → appairage RS2 ─────────────────────
        if (be instanceof WirelessTransmitterBlockEntity transmitter)
        {
            Network network = transmitter.getNetworkForItem();
            if (network == null)
            {
                player.sendSystemMessage(Component.literal(
                        "§c[ColonyLink RS] Wireless Transmitter is not connected to a RS2 network!"));
                return net.minecraft.world.InteractionResult.FAIL;
            }

            GlobalPos globalPos = GlobalPos.of(
                    level.dimension(), pos);
            ColonyLinkWandRSLinkableHandler.link(wandStack, globalPos);

            player.sendSystemMessage(Component.literal(
                    "§a[ColonyLink RS] Linked to RS2 network via Wireless Transmitter @ " + pos.toShortString()));
            return net.minecraft.world.InteractionResult.SUCCESS;
        }

        // ── Sneak + Redirector RS2 → link redirector au builder actif ─────────
        if (be instanceof ColonyLinkRedirectorBlockEntityRS redirector)
        {
            if (!redirector.isRs2Active())
            {
                player.sendSystemMessage(Component.literal(
                        "§c[ColonyLink RS] Redirector RS2 is not connected to the RS2 network!"));
                return net.minecraft.world.InteractionResult.FAIL;
            }

            List<BuilderEntry> entries = ColonyLinkWandRSLinkableHandler.getBuilderEntries(wandStack);
            if (entries.isEmpty())
            {
                player.sendSystemMessage(Component.literal("§c[ColonyLink RS] No Builder's Hut linked to this wand!"));
                player.sendSystemMessage(Component.literal("§7Sneak + Right-click a Builder's Hut first."));
                return net.minecraft.world.InteractionResult.FAIL;
            }

            // Vérifie que ce redirector n'est pas déjà lié
            for (BuilderEntry e : entries)
            {
                if (e.redirectorPos().equals(pos))
                {
                    player.sendSystemMessage(Component.literal(
                            "§c[ColonyLink RS] This Redirector is already paired with builder §f"
                                    + e.builderName() + "§c!"));
                    return net.minecraft.world.InteractionResult.FAIL;
                }
            }

            // Trouve le builder cible (tab active sans redirector, ou tab active)
            int targetIndex = -1;
            int activeTab = ColonyLinkWandRSLinkableHandler.getActiveTab(wandStack);
            if (activeTab < entries.size() && !entries.get(activeTab).hasRedirector())
                targetIndex = activeTab;
            if (targetIndex == -1)
                for (int i = 0; i < entries.size(); i++)
                    if (!entries.get(i).hasRedirector()) { targetIndex = i; break; }
            if (targetIndex == -1)
                targetIndex = Math.min(activeTab, entries.size() - 1);

            BuilderEntry target = entries.get(targetIndex);
            redirector.setTargetInventoryPos(target.builderPos());
            redirector.setLinkedBuilderPos(target.builderPos());
            redirector.updateState();
            entries.set(targetIndex, target.withRedirector(pos));
            ColonyLinkWandRSLinkableHandler.setBuilderEntries(wandStack, entries);

            player.sendSystemMessage(Component.literal(
                    "§a[ColonyLink RS] Redirector linked to builder §f" + target.builderName()
                            + " §7@ " + target.builderPos().toShortString()));
            return net.minecraft.world.InteractionResult.SUCCESS;
        }

        // ── Sneak + Builder's Hut → ajouter builder ───────────────────────────
        if (!ColonyLinkWandRSLinkableHandler.isLinked(wandStack))
        {
            player.sendSystemMessage(Component.literal(
                    "§c[ColonyLink RS] Not linked to RS2 network! Sneak + Right-click a Wireless Transmitter first."));
            return net.minecraft.world.InteractionResult.FAIL;
        }

        ServerLevel sl = (ServerLevel) level;
        Network network = ColonyLinkWandRSLinkableHandler.getNetwork(wandStack, sl);
        if (network == null)
        {
            player.sendSystemMessage(Component.literal("§c[ColonyLink RS] Cannot connect to RS2 network!"));
            return net.minecraft.world.InteractionResult.FAIL;
        }

        IColony colony = IColonyManager.getInstance().getClosestColony(sl, pos);
        if (colony == null) return net.minecraft.world.InteractionResult.PASS;

        if (!colony.getPermissions().hasPermission(sp, Action.ACCESS_HUTS))
        {
            player.sendSystemMessage(Component.literal("§c[ColonyLink RS] No permission for this colony!"));
            return net.minecraft.world.InteractionResult.FAIL;
        }

        IBuilding building = null;
        for (IBuilding b : colony.getServerBuildingManager().getBuildings().values())
            if (b.getPosition().equals(pos)) { building = b; break; }

        if (building == null) return net.minecraft.world.InteractionResult.PASS;
        if (!building.getBuildingType().getRegistryName().getPath().contains("builder"))
            return net.minecraft.world.InteractionResult.PASS;
        if (!(building instanceof AbstractBuildingStructureBuilder bb))
            return net.minecraft.world.InteractionResult.PASS;

        String builderName = "Builder";
        if (!bb.getAllAssignedCitizen().isEmpty())
            builderName = bb.getAllAssignedCitizen().iterator().next().getName();

        String buildingLabel = "Builder's Hut";
        var wo = bb.getWorkOrder();
        if (wo != null) buildingLabel = wo.getDisplayName().getString();

        BuilderEntry newEntry = new BuilderEntry(pos, BlockPos.ZERO, builderName, buildingLabel);
        if (!ColonyLinkWandRSLinkableHandler.addOrUpdateEntry(wandStack, newEntry))
        {
            player.sendSystemMessage(Component.literal(
                    "§c[ColonyLink RS] Max builders reached (" + ColonyLinkConfig.MAX_BUILDERS_PER_WAND.get() + ")!"));
            return net.minecraft.world.InteractionResult.FAIL;
        }

        List<BuilderEntry> entries = ColonyLinkWandRSLinkableHandler.getBuilderEntries(wandStack);
        int newIdx = entries.size() - 1;
        for (int i = 0; i < entries.size(); i++)
            if (entries.get(i).builderPos().equals(pos)) { newIdx = i; break; }
        ColonyLinkWandRSLinkableHandler.setActiveTab(wandStack, newIdx);

        player.sendSystemMessage(Component.literal(
                "§a[ColonyLink RS] Builder §f" + builderName + " §aadded! (slot "
                        + (newIdx + 1) + "/" + ColonyLinkConfig.MAX_BUILDERS_PER_WAND.get() + ")"));
        player.sendSystemMessage(Component.literal(
                "§7Now §fSneak + Right-click a Colony Link Redirector RS §7to pair it."));
        return net.minecraft.world.InteractionResult.SUCCESS;
    }

    // ── openGUI ───────────────────────────────────────────────────────────────

    private boolean openGUI(ItemStack wandStack, ServerPlayer player, ServerLevel sl,
                            BlockPos builderPos, int activeTabIndex)
    {
        Network network = ColonyLinkWandRSLinkableHandler.getNetwork(wandStack, sl);
        if (network == null)
        {
            player.sendSystemMessage(Component.literal("§c[ColonyLink RS] Cannot connect to RS2 network!"));
            return false;
        }

        IColony colony = IColonyManager.getInstance().getClosestColony(sl, builderPos);
        if (colony == null)
        {
            player.sendSystemMessage(Component.literal("§c[ColonyLink RS] No colony found nearby!"));
            return false;
        }
        if (!colony.getPermissions().hasPermission(player, Action.ACCESS_HUTS))
        {
            player.sendSystemMessage(Component.literal("§c[ColonyLink RS] No permission!"));
            return false;
        }

        IBuilding building = null;
        for (IBuilding b : colony.getServerBuildingManager().getBuildings().values())
            if (b.getPosition().equals(builderPos)) { building = b; break; }
        if (!(building instanceof AbstractBuildingStructureBuilder bb))
        {
            player.sendSystemMessage(Component.literal("§c[ColonyLink RS] Builder's Hut not found — re-link."));
            return false;
        }

        String builderName = "N/A", workerStatus = "IDLE";
        if (!bb.getAllAssignedCitizen().isEmpty())
        {
            var c = bb.getAllAssignedCitizen().iterator().next();
            builderName = c.getName();
            var vs = c.getStatus();
            workerStatus = vs != null
                    ? Component.translatable(vs.getTranslationKey()).getString()
                    : c.getJobStatus().name();
        }

        String buildingName = "N/A";
        var wo = bb.getWorkOrder();
        if (wo != null)
        {
            buildingName = wo.getDisplayName().getString();
            if (wo.getStage() != null) buildingName += " [" + wo.getStage().name() + "]";
        }

        StorageNetworkComponent storage = network.getComponent(StorageNetworkComponent.class);
        AutocraftingNetworkComponent crafting = network.getComponent(AutocraftingNetworkComponent.class);

        // Compte les autocrafters disponibles (pas d'équivalent CPU RS2 — on retourne 0 si null)
        int freeCrafters = crafting != null ? crafting.getPatterns().size() : 0;

        List<BuilderEntry> allEntries = ColonyLinkWandRSLinkableHandler.getBuilderEntries(wandStack);
        BuilderEntry ae = allEntries.isEmpty() ? null
                : allEntries.get(Math.min(activeTabIndex, allEntries.size() - 1));
        BlockPos rPos = (ae != null && ae.hasRedirector()) ? ae.redirectorPos() : null;

        String rState = "N/A"; boolean hasCard = false, whPrio = false;
        if (rPos != null)
        {
            var rbe = sl.getBlockEntity(rPos);
            if (rbe instanceof ColonyLinkRedirectorBlockEntityRS r)
            {
                rState = r.isRs2Active() ? switch (r.getState()) {
                    case STANDBY    -> "STANDBY";
                    case NOT_LINKED -> "NOT_LINKED";
                    default         -> "LINKED";
                } : "NOT_LINKED";
                hasCard = r.hasWarehouseCard();
                whPrio  = r.isWarehousePriority();
            }
        }

        Map<String, BuildingBuilderResource> needed = bb.getNeededResources();
        List<ColonyLinkPacket.ResourceEntry> entries = new ArrayList<>();
        if (needed != null && storage != null)
        {
            for (var res : needed.values())
            {
                var st = res.getItemStack();
                int miss = res.getAmount() - res.getAvailable();
                if (miss <= 0) continue;
                var ms = st.copy(); ms.setCount(Math.min(miss, 64));

                var rsKey = com.refinedmods.refinedstorage.common.support.resource.ItemResource.ofItemStack(st);
                long inSt = storage.get(rsKey);
                ResourceStatus stat;
                if (inSt >= miss)            stat = ResourceStatus.AVAILABLE;
                else if (crafting != null && crafting.getOutputs().contains(rsKey)) stat = ResourceStatus.CRAFTABLE;
                else                         stat = ResourceStatus.NO_PATTERN;

                entries.add(new ColonyLinkPacket.ResourceEntry(ms, stat, miss, false,
                        rPos != null ? rPos : BlockPos.ZERO, new ArrayList<>()));
            }
        }

        long rfStored = WandEnergyStorage.getStoredRF(wandStack);
        long rfMax    = ColonyLinkConfig.WAND_RF_CAPACITY.get();

        PacketDistributor.sendToPlayer(player, new ColonyLinkPacket(
                entries, builderPos, builderName, buildingName, workerStatus, "", freeCrafters,
                rState, ColonyLinkPacket.BuilderRequest.NONE,
                hasCard, whPrio, buildTabMetas(allEntries), activeTabIndex,
                rfStored, rfMax, true));
        return true;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public static List<ColonyLinkPacket.BuilderTabMeta> buildTabMetas(List<BuilderEntry> entries)
    {
        List<ColonyLinkPacket.BuilderTabMeta> metas = new ArrayList<>();
        for (BuilderEntry e : entries)
            metas.add(new ColonyLinkPacket.BuilderTabMeta(
                    e.builderPos(), e.builderName(), e.buildingLabel(), e.hasRedirector()));
        return metas;
    }
}