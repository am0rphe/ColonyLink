package com.colonylink.colonylink;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
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
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ColonyLink Wand — v1.2.0
 *
 * Fix #2 : sneak+clic sur un Builder's Hut déjà lié affiche un message
 *          indiquant dans quelle tab il est déjà présent.
 */
public class ColonyLinkWand extends Item implements IAEItemPowerStorage
{
    private static final double AE_TO_RF = 2.0;

    public ColonyLinkWand(Properties properties) { super(properties); }

    // ── Anti-pop ──────────────────────────────────────────────────────────────

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
    {
        return slotChanged || !ItemStack.isSameItem(oldStack, newStack);
    }

    // ── IAEItemPowerStorage ───────────────────────────────────────────────────

    @Override
    public double injectAEPower(ItemStack stack, double amount, Actionable mode)
    {
        long capacity = ColonyLinkConfig.WAND_RF_CAPACITY.get();
        long stored   = WandEnergyStorage.getStoredRF(stack);
        long space    = capacity - stored;
        if (space <= 0) return amount;

        long rfToAdd     = (long)(amount * AE_TO_RF);
        long rfAdded     = Math.min(rfToAdd, space);
        double aeConsumed = rfAdded / AE_TO_RF;

        if (mode == Actionable.MODULATE)
            WandEnergyStorage.setStoredRF(stack, stored + rfAdded);

        return Math.max(0.0, amount - aeConsumed);
    }

    @Override
    public double extractAEPower(ItemStack stack, double amount, Actionable mode) { return 0.0; }

    @Override
    public double getAEMaxPower(ItemStack stack)
    { return ColonyLinkConfig.WAND_RF_CAPACITY.get() / AE_TO_RF; }

    @Override
    public double getAECurrentPower(ItemStack stack)
    { return WandEnergyStorage.getStoredRF(stack) / AE_TO_RF; }

    @Override
    public AccessRestriction getPowerFlow(ItemStack stack) { return AccessRestriction.WRITE; }

    @Override
    public double getChargeRate(ItemStack stack)
    { return ColonyLinkConfig.WAND_RF_TRANSFER_RATE.get() / AE_TO_RF; }

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
            tooltip.add(Component.literal("§c⚠ OUT OF POWER — charge Clipboard in AE2 Charger or FE charger"));

        boolean linkedAE2 = ColonyLinkWandLinkableHandler.isLinked(stack);
        List<BuilderEntry> entries = ColonyLinkWandLinkableHandler.getBuilderEntries(stack);
        int activeTab = ColonyLinkWandLinkableHandler.getActiveTab(stack);

        if (!linkedAE2)
        {
            tooltip.add(Component.literal("§c✘ Clipboard not linked to AE2"));
            tooltip.add(Component.literal("§8  → Insert Clipboard into a §fWireless Access Point"));
        }
        else
        {
            tooltip.add(Component.literal("§a✔ Linked to AE2 network"));
            if (entries.isEmpty())
            {
                tooltip.add(Component.literal("§e⚠ No Builder linked"));
                tooltip.add(Component.literal("§8  → §fSneak + Right-click §8a Builder's Hut"));
            }
            else
            {
                tooltip.add(Component.literal("§a✔ " + entries.size() + "/"
                        + ColonyLinkWandLinkableHandler.getMaxBuilders() + " Builder(s) linked"));
                BuilderEntry active = entries.get(Math.min(activeTab, entries.size() - 1));
                tooltip.add(Component.literal("§7Active: §f" + active.builderName()
                        + " §8@ " + active.builderPos().toShortString()));
                if (!active.hasRedirector())
                    tooltip.add(Component.literal("§e⚠ No Redirector linked for this builder"));
            }
        }

        tooltip.add(Component.literal("§8──────────────────"));
        tooltip.add(Component.literal("§7Right-click §8(air) → open resource GUI"));
        tooltip.add(Component.literal("§7Sneak + Right-click §8Builder's Hut → add/link builder"));
        tooltip.add(Component.literal("§7Sneak + Right-click §8Redirector → link to active builder"));
        tooltip.add(Component.literal("§8Charge Clipboard: §fAE2 Charger §8· §fany FE charger mod"));
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
                    "§c[ColonyLink] Out of Power! Charge Clipboard in AE2 Charger or FE charger."));
            return InteractionResultHolder.fail(wandStack);
        }

        if (!ColonyLinkWandLinkableHandler.isLinked(wandStack))
        {
            player.sendSystemMessage(Component.literal("§cThis Clipboard is not linked to an AE2 network!"));
            return InteractionResultHolder.fail(wandStack);
        }

        List<BuilderEntry> entries = ColonyLinkWandLinkableHandler.getBuilderEntries(wandStack);
        if (entries.isEmpty())
        {
            player.sendSystemMessage(Component.literal(
                    "§eNo Builder's Hut linked yet. Sneak + Right-click a Builder's Hut first."));
            return InteractionResultHolder.fail(wandStack);
        }

        int activeTab = ColonyLinkWandLinkableHandler.getActiveTab(wandStack);
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
            if (s.getItem() instanceof ColonyLinkWand) { wandStack = s; break; }
        if (wandStack == null) wandStack = context.getItemInHand();

        var be = level.getBlockEntity(pos);

        if (be instanceof ColonyLinkRedirectorBlockEntity && !player.isShiftKeyDown())
            return net.minecraft.world.InteractionResult.SUCCESS;

        if (!player.isShiftKeyDown())
            return net.minecraft.world.InteractionResult.PASS;

        // ── Sneak + Redirector ────────────────────────────────────────────────
        if (be instanceof ColonyLinkRedirectorBlockEntity redirector)
        {
            if (redirector.getManagedGridNode().getNode() == null)
            {
                player.sendSystemMessage(Component.literal("§cRedirector is not adjacent to a ME Controller!"));
                return net.minecraft.world.InteractionResult.FAIL;
            }

            List<BuilderEntry> entries = ColonyLinkWandLinkableHandler.getBuilderEntries(wandStack);
            if (entries.isEmpty())
            {
                player.sendSystemMessage(Component.literal("§cNo Builder's Hut linked to this Clipboard!"));
                player.sendSystemMessage(Component.literal("§7Sneak + Right-click a Builder's Hut first."));
                return net.minecraft.world.InteractionResult.FAIL;
            }

            for (BuilderEntry e : entries)
            {
                if (e.redirectorPos().equals(pos))
                {
                    player.sendSystemMessage(Component.literal(
                            "§c[ColonyLink] This Redirector is already paired with builder §f"
                                    + e.builderName() + "§c!"));
                    return net.minecraft.world.InteractionResult.FAIL;
                }
            }

            int targetIndex = -1;
            int activeTab = ColonyLinkWandLinkableHandler.getActiveTab(wandStack);
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
            redirector.setLinkedBuilderName(target.builderName());
            redirector.updateState();
            entries.set(targetIndex, target.withRedirector(pos));
            ColonyLinkWandLinkableHandler.setBuilderEntries(wandStack, entries);

            player.sendSystemMessage(Component.literal(
                    "§aRedirector linked to builder §f" + target.builderName()
                            + " §7@ " + target.builderPos().toShortString()));
            return net.minecraft.world.InteractionResult.SUCCESS;
        }

        // ── Sneak + Builder's Hut ─────────────────────────────────────────────
        if (!ColonyLinkWandLinkableHandler.isLinked(wandStack))
        {
            player.sendSystemMessage(Component.literal("§cThis Clipboard is not linked to an AE2 network!"));
            return net.minecraft.world.InteractionResult.FAIL;
        }

        ServerLevel sl = (ServerLevel) level;
        IWirelessAccessPoint wap = getWap(wandStack, sl);
        if (wap == null)
        {
            player.sendSystemMessage(Component.literal("§cCannot connect to AE2 network!"));
            return net.minecraft.world.InteractionResult.FAIL;
        }
        IGrid grid = wap.getGrid();
        if (grid == null)
        {
            player.sendSystemMessage(Component.literal("§cAE2 network is offline!"));
            return net.minecraft.world.InteractionResult.FAIL;
        }

        IColony colony = IColonyManager.getInstance().getClosestColony(sl, pos);
        if (colony == null) return net.minecraft.world.InteractionResult.PASS;

        if (!colony.getPermissions().hasPermission(sp, Action.ACCESS_HUTS))
        {
            player.sendSystemMessage(Component.literal("§c[ColonyLink] No permission for this colony!"));
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

        // ── Fix #2 : vérifie si ce hut est déjà lié ──────────────────────────
        List<BuilderEntry> existingEntries = ColonyLinkWandLinkableHandler.getBuilderEntries(wandStack);
        for (int i = 0; i < existingEntries.size(); i++)
        {
            if (existingEntries.get(i).builderPos().equals(pos))
            {
                BuilderEntry existing = existingEntries.get(i);
                player.sendSystemMessage(Component.literal(
                        "§e[ColonyLink] This Builder's Hut is already linked in tab §f"
                                + (i + 1) + " §e(§f" + existing.builderName() + "§e)!"));
                return net.minecraft.world.InteractionResult.FAIL;
            }
        }
        // ─────────────────────────────────────────────────────────────────────

        String builderName = "Builder";
        if (!bb.getAllAssignedCitizen().isEmpty())
            builderName = bb.getAllAssignedCitizen().iterator().next().getName();

        String buildingLabel = "Builder's Hut";
        var wo = bb.getWorkOrder();
        if (wo != null) buildingLabel = wo.getDisplayName().getString();

        BuilderEntry newEntry = new BuilderEntry(pos, BlockPos.ZERO, builderName, buildingLabel);
        if (!ColonyLinkWandLinkableHandler.addOrUpdateEntry(wandStack, newEntry))
        {
            player.sendSystemMessage(Component.literal(
                    "§cMax builders reached (" + ColonyLinkWandLinkableHandler.getMaxBuilders() + ")!"));
            return net.minecraft.world.InteractionResult.FAIL;
        }

        List<BuilderEntry> entries = ColonyLinkWandLinkableHandler.getBuilderEntries(wandStack);
        int newIdx = entries.size() - 1;
        for (int i = 0; i < entries.size(); i++)
            if (entries.get(i).builderPos().equals(pos)) { newIdx = i; break; }
        ColonyLinkWandLinkableHandler.setActiveTab(wandStack, newIdx);

        player.sendSystemMessage(Component.literal(
                "§aBuilder §f" + builderName + " §aadded! (slot "
                        + (newIdx + 1) + "/" + ColonyLinkWandLinkableHandler.getMaxBuilders() + ")"));
        player.sendSystemMessage(Component.literal(
                "§7Now §fSneak + Right-click a Colony Link Redirector §7to pair it."));
        return net.minecraft.world.InteractionResult.SUCCESS;
    }

    // ── openGUI ───────────────────────────────────────────────────────────────

    private boolean openGUI(ItemStack wandStack, ServerPlayer player, ServerLevel sl,
                            BlockPos builderPos, int activeTabIndex)
    {
        IWirelessAccessPoint wap = getWap(wandStack, sl);
        if (wap == null) { player.sendSystemMessage(Component.literal("§cCannot connect to AE2 network!")); return false; }
        IGrid grid = wap.getGrid();
        if (grid == null) { player.sendSystemMessage(Component.literal("§cAE2 network is offline!")); return false; }

        IColony colony = IColonyManager.getInstance().getClosestColony(sl, builderPos);
        if (colony == null) { player.sendSystemMessage(Component.literal("§cNo colony found nearby!")); return false; }
        if (!colony.getPermissions().hasPermission(player, Action.ACCESS_HUTS))
        { player.sendSystemMessage(Component.literal("§c[ColonyLink] No permission!")); return false; }

        IBuilding building = null;
        for (IBuilding b : colony.getServerBuildingManager().getBuildings().values())
            if (b.getPosition().equals(builderPos)) { building = b; break; }
        if (!(building instanceof AbstractBuildingStructureBuilder bb))
        { player.sendSystemMessage(Component.literal("§cBuilder's Hut not found — re-link.")); return false; }

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

        ICraftingService cs = grid.getCraftingService();
        int cpus = 0; for (var cpu : cs.getCpus()) if (!cpu.isBusy()) cpus++;

        List<BuilderEntry> allEntries = ColonyLinkWandLinkableHandler.getBuilderEntries(wandStack);
        BuilderEntry ae = allEntries.isEmpty() ? null
                : allEntries.get(Math.min(activeTabIndex, allEntries.size() - 1));
        BlockPos rPos = (ae != null && ae.hasRedirector()) ? ae.redirectorPos() : null;

        String rState = "N/A"; boolean hasCard = false, whPrio = false;
        if (rPos != null)
        {
            var rbe = sl.getBlockEntity(rPos);
            if (rbe instanceof ColonyLinkRedirectorBlockEntity r)
            {
                var rn = r.getManagedGridNode().getNode();
                rState = rn != null ? switch (r.getState()) {
                    case STANDBY    -> "STANDBY";
                    case NOT_LINKED -> "NOT_LINKED";
                    default         -> "LINKED";
                } : "NOT_LINKED";
                hasCard = r.hasWarehouseCard();
                whPrio  = r.isWarehousePriority();
            }
        }

        IStorageService ss = grid.getStorageService();
        KeyCounter inv = ss.getCachedInventory();
        BlockPos safeR = rPos != null ? rPos : BlockPos.ZERO;

        Map<String, BuildingBuilderResource> needed = bb.getNeededResources();
        List<ColonyLinkPacket.ResourceEntry> entries = new ArrayList<>();
        if (needed != null)
        {
            for (var res : needed.values())
            {
                ItemStack st = res.getItemStack();
                int miss = res.getAmount() - res.getAvailable();
                if (miss <= 0) continue;
                ItemStack ms = st.copy(); ms.setCount(Math.min(miss, 64));
                AEItemKey k = AEItemKey.of(st); long inSt = inv.get(k);
                ResourceStatus stat;
                if (inSt >= miss)              stat = ResourceStatus.AVAILABLE;
                else if (cs.isRequesting(k))   stat = ResourceStatus.CRAFTING;
                else if (cs.isCraftable(k))    stat = ResourceStatus.CRAFTABLE;
                else                           stat = ResourceStatus.NO_PATTERN;
                entries.add(new ColonyLinkPacket.ResourceEntry(ms, stat, miss, false, safeR, new ArrayList<>()));
            }
        }

        long rfStored = WandEnergyStorage.getStoredRF(wandStack);
        long rfMax    = ColonyLinkConfig.WAND_RF_CAPACITY.get();

        PacketDistributor.sendToPlayer(player, new ColonyLinkPacket(
                entries, builderPos, builderName, buildingName, workerStatus, "", cpus,
                rState, ColonyLinkPacket.BuilderRequest.NONE,
                hasCard, whPrio, buildTabMetas(allEntries), activeTabIndex,
                rfStored, rfMax));
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

    public static BlockPos getLastBuilderPos(ItemStack stack)
    { return ColonyLinkWandLinkableHandler.getActiveBuilderPos(stack); }

    private IWirelessAccessPoint getWap(ItemStack wandStack, ServerLevel level)
    {
        GlobalPos lp = ColonyLinkWandLinkableHandler.getLinkedPos(wandStack);
        if (lp == null) return null;
        ServerLevel tl = level.getServer().getLevel(lp.dimension());
        if (tl == null) return null;
        var be = tl.getBlockEntity(lp.pos());
        if (be instanceof IWirelessAccessPoint wap) return wap;
        return null;
    }
}