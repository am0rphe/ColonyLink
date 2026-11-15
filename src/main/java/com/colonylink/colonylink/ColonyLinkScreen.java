package com.colonylink.colonylink;

import com.colonylink.colonylink.AeGuiBlits.AeBtnVis;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * Main GUI of the ColonyLink Wand — v1.1.3.
 *
 * RF is shown only via the item durability bar + hotbar tooltip, not in this GUI.
 *
 * When rfStored == 0 the content is replaced by "Out of Power" and the
 * Send/Craft buttons are greyed (isButtonClickable returns false).
 */
public class ColonyLinkScreen extends Screen
{
    // #5: hotbar badge — exposes the unread-tab count to the renderer.
    // Updated on each applyPacket().
    public static int UNREAD_TAB_COUNT = 0;

    private static final int GUI_WIDTH  = 276;
    private static final int GUI_HEIGHT = 320;

    private static final int TAB_WIDTH   = 20;
    private static final int TAB_HEIGHT  = 24;
    private static final int TAB_SPACING = 2;
    private static final int TAB_Y_OFFSET = 30;
    private static final int TAB_OVERLAP  = 4;

    private static final int ENTRY_HEIGHT    = 20;
    private static final int MAX_VISIBLE     = 8;
    private static final int SCROLLBAR_WIDTH = 6;

    // AE theme — frame + button assets (layers 1 & 4)
    // UI pass 1: the AE2 ResourceLocations, nine-slice constants and blit
    // primitives moved to AeGuiBlits (shared with the Redirector screen). This
    // class keeps thin private facades further down so NO call-site changed —
    // zero visual change to the Clipboard. Same legal frame as before: AE2 assets
    // (CC BY-NC-SA) referenced at runtime only, never copied; probed once at
    // init() with a silent procedural fallback (drawAeFrame) — never magenta.
    private boolean aeBackgroundPresent = false;  // probed once in init(), not per frame
    private boolean aeButtonPresent = false;      // probed once in init(), not per frame

    // MineColonies theme — parchment background (layer 1)
    // colonist_paper.png is a MineColonies GPL-3.0 asset, referenced by
    // ResourceLocation at runtime ONLY and NEVER copied into this repo. MineColonies
    // is a required (transitive, via BlockUI) dependency of ColonyLink so the asset is
    // guaranteed present, but the internal path can change between MC versions; we probe
    // once at init() and fall back silently to a procedural parchment fill otherwise.
    // The texture is 190x244; it is stretched over the whole Clipboard body (276x320),
    // exactly as MineColonies stretches its own fixed-size window paper.
    private static final ResourceLocation MC_PAPER =
            ResourceLocation.fromNamespaceAndPath("minecolonies", "textures/gui/citizen/colonist_paper.png");
    private static final int MC_PAPER_TEX_W  = 190;        // colonist_paper.png width
    private static final int MC_PAPER_TEX_H  = 244;        // colonist_paper.png height
    // Internal decorative margin of colonist_paper.png (transparent edge + torn border →
    // flat cream interior), in TEXTURE pixels. Measured by pixel inspection of the source
    // asset (center-line scan: alpha-0 edge + dark torn line before the cream plateau).
    // The paper is blitted OVERSIZED by these margins so the torn border spills OUTSIDE the
    // Clipboard frame and the cream interior aligns with the body rect. Tune if it drifts.
    private static final int MC_PAPER_MARGIN_L = 9;
    private static final int MC_PAPER_MARGIN_R = 10;
    private static final int MC_PAPER_MARGIN_T = 8;
    private static final int MC_PAPER_MARGIN_B = 9;
    private static final int MC_PAPER_FALLBACK = 0xFFE8DCC0; // beige parchment (procedural fallback only)
    private static final int MC_PAPER_BORDER   = 0xFF6B4E2E; // brown edge (procedural fallback only)
    private boolean mcPaperPresent = false;   // probed once in init(), not per frame

    // MineColonies theme — button textures (layer: buttons)
    // builderhut/* GPL-3.0 assets, referenced by ResourceLocation at runtime ONLY and
    // NEVER copied into this repo. Blitted (stretched) to our existing button sizes; only
    // the 4 sizes the validated mapping uses are referenced (large/medium/very_small are
    // unused). Each has a dedicated _disabled variant. No hover sprite exists in MC → hover
    // feedback is carried by DARKENING the texture (see MC_HOVER_DARKEN), label stays black.
    private static ResourceLocation mcGui(String sub)
    { return ResourceLocation.fromNamespaceAndPath("minecolonies", "textures/gui/" + sub); }
    private static final ResourceLocation MC_BTN_ML     = mcGui("builderhut/builder_button_medium_large.png");          // 129x17
    private static final ResourceLocation MC_BTN_ML_D   = mcGui("builderhut/builder_button_medium_large_disabled.png");
    private static final ResourceLocation MC_BTN_S      = mcGui("builderhut/builder_button_small.png");                 // 64x17
    private static final ResourceLocation MC_BTN_S_D    = mcGui("builderhut/builder_button_small_disabled.png");
    private static final ResourceLocation MC_BTN_QS     = mcGui("builderhut/builder_button_quite_small.png");           // 44x16
    private static final ResourceLocation MC_BTN_QS_D   = mcGui("builderhut/builder_button_quite_small_disabled.png");
    private static final ResourceLocation MC_BTN_MINI   = mcGui("builderhut/builder_button_mini.png");                  // 14x15
    private static final ResourceLocation MC_BTN_MINI_D = mcGui("builderhut/builder_button_mini_disabled.png");
    // Pivot probe: one texture stands in for the whole builder-button set (same jar/dir).
    private static final ResourceLocation MC_BTN_PROBE  = MC_BTN_ML;
    private boolean mcButtonPresent = false;  // probed once in init(), not per frame

    // MineColonies button label colours (MC-style: black, no shadow; disabled = grey). The
    // sense is carried by the label TEXT ("Craft"/"Send"…), not its colour. Hover is NOT shown
    // by the label (pale yellow was invisible on the light wood) but by darkening the button
    // BACKGROUND — see MC_HOVER_DARKEN.
    private static final int   MC_LABEL          = 0xFF000000;
    private static final int   MC_LABEL_DISABLED = 0xFFA0A0A0;
    // Hover feedback: multiply the (enabled) button texture by this brightness (~18% darker).
    private static final float MC_HOVER_DARKEN   = 0.82f;
    // Very subtle zebra shade for MineColonies list rows: translucent black (~8% alpha) drawn
    // over even rows so the parchment stays visible through it. NOT an opaque row well.
    private static final int   MC_ROW_SHADE       = 0x14000000;

    // MineColonies theme — side tab textures (layer: tabs)
    // modules/tab_left_side{1,2,3}.png (32×26 cream paper tabs, torn edge on the LEFT, attach
    // edge on the RIGHT). GPL-3.0, blit-only, never copied. The 1/2/3 are visual VARIETY (not
    // states); we cycle them by tab index. Only the LEFT 20px sub-region is blitted (the right
    // 12px would tuck under the panel in MineColonies) → 1:1 horizontally, no mirror. States are
    // done by tint: active = full brightness, inactive = slightly dimmed, hover = darker, and a
    // translucent colour overlay for unread (blue) / unlinked (brown).
    private static final ResourceLocation MC_TAB1 = mcGui("modules/tab_left_side1.png");
    private static final ResourceLocation MC_TAB2 = mcGui("modules/tab_left_side2.png");
    private static final ResourceLocation MC_TAB3 = mcGui("modules/tab_left_side3.png");
    private static final ResourceLocation MC_TAB_PROBE = MC_TAB1;
    private boolean mcTabPresent = false;  // probed once in init(), not per frame
    private static final float MC_TAB_INACTIVE_DIM = 0.90f;   // inactive tabs slightly dimmed
    // State is shown on the tab ICON (not a background overlay, which clashed with the torn paper
    // edge): unread → semBlue, unlinked → this rust colour, normal → dark. Priority unlinked > unread.
    private static final int   MC_TAB_UNLINKED_ICON = 0xFFB03A20; // rust/red icon for "no redirector"
    private static final int   MC_TAB_ICON_DARK     = 0xFF2A1E0E; // normal MC tab icon (dark on cream)
    // Visual-only nudge (+1px right) of the MC tab BLIT (and its icon) so the tab overlaps the
    // parchment by 1px and hides the dark seam left by the texture's transparent right edge. The
    // hitbox/getTabX are NOT shifted — clicks stay put.
    private static final int   MC_TAB_BLIT_DX       = 1;

    // MineColonies theme — WH|AE2 toggle (parchment-recoloured procedural)
    // Procedural (not a texture composite): the active half is a raised lighter beige + a coloured
    // accent bar (green WH / blue AE2) + accent label; the inactive half is neutral beige + muted
    // label; divider/border in brown. Tunable.
    private static final int   MC_TOGGLE_BG     = 0xFFC2AC82; // toggle body / inactive half (wood beige)
    private static final int   MC_TOGGLE_ACTIVE = 0xFFE4D3A8; // raised active half (lighter beige)
    private static final int   MC_TOGGLE_LIGHT  = 0xFFEAD9B0; // top/left bevel highlight

    // #12: special index for the Citizens tab
    private static final int CITIZENS_TAB_INDEX = Integer.MAX_VALUE;
    // v1.8.0 — Stats tab: a second special tab (sentinel index), pinned below Citizens.
    private static final int STATS_TAB_INDEX = Integer.MAX_VALUE - 1;

    // State
    private List<ColonyLinkPacket.BuilderTabMeta> tabMetas = new ArrayList<>();
    private int activeTabIndex = 0;

    // 1.7.0-beta.5 (audit #9) — exactly one GuiStatePacket(false) per open, whichever
    // way the screen goes away (onClose, removed, pairing "+"). Reset in init().
    private boolean closePacketSent = false;

    // #12: Citizens tab data
    private List<CitizensPacket.CitizenRequestEntry> citizenEntries = new ArrayList<>();
    private boolean citizensLoading = false;
    private int citizenPackageCount = 0; // synced from server
    // 1.7.0-beta.3 — the Sent / Crafting marks now come with each CitizensPacket row
    // (server memory per colony, keyed by request UUID); no Clipboard NBT is read.
    // Optimistic overlays, keyed by request UUID: the row shows the mark right after
    // the click, and the server truth takes over on the next sync. Time-bounded so a
    // server-side rejection cannot leave a row stuck (same pattern as craftHoldUntil).
    private static final long PENDING_HOLD_MS = 5_000L;
    private final java.util.Map<java.util.UUID, Long> optimisticCitizenSentUntil = new java.util.HashMap<>();
    private final java.util.Map<java.util.UUID, Long> optimisticCitizenCraftUntil = new java.util.HashMap<>();
    private final java.util.Map<String, Long> pendingSentUntil = new java.util.HashMap<>();

    // v1.8.0 — Stats tab data (loaded once on tab click, no polling)
    private boolean statsLoading = false;
    private boolean statsLoaded  = false;
    private long statItemsSent, statAe2Crafts, statDomumCrafts, statTomesUsed;
    private int  statCitizens, statMaxCitizens, statGuards, statBuilders, statHappiness, statAvgDistance = -1;

    private List<ColonyLinkPacket.ResourceEntry> entries = new ArrayList<>();
    private BlockPos builderPos      = BlockPos.ZERO;
    private String   builderName     = "";
    private String   buildingName    = "";
    private String   workerStatus    = "";
    private String   workerIdleReason = ""; // v1.1.3 — IDLE reason
    private int      availableCpus   = 0;
    private String   redirectorState = "N/A";
    private ColonyLinkPacket.BuilderRequest builderRequest = ColonyLinkPacket.BuilderRequest.NONE;
    private boolean  hasWarehouseCard  = false;
    private boolean  warehousePriority = false;
    private BlockPos redirectorPos   = BlockPos.ZERO;

    // v1.1.3 — RF from server (possible future use, not shown here)
    private long rfStored = 0L;
    private long rfMax    = 1_600_000L;

    private int     scrollOffset        = 0;
    private boolean isDraggingScrollbar = false;
    private double  dragStartY          = 0;
    private int     dragStartOffset     = 0;

    // #5/#6: unread tabs — indices of tabs with new unseen requests.
    // Filled when an inactive tab receives entries, cleared when the player
    // clicks the tab. Static: persists across GUI opens.
    private static final java.util.Set<Integer> unreadTabs = new java.util.HashSet<>();
    private static final java.util.Map<Integer, Integer> lastReadEntryCount = new java.util.HashMap<>();

    // Draggable GUI
    // dragOffsetX/Y = offset from the default centered position (0 = opens centered).
    private int     dragOffsetX    = 0;
    private int     dragOffsetY    = 0;
    private boolean isDraggingGui  = false;
    private double  guiDragStartX  = 0;
    private double  guiDragStartY  = 0;
    private int     guiDragOriginX = 0;
    private int     guiDragOriginY = 0;

    private WarehouseResultPacket warehouseSnapshot       = null;
    private long warehouseSnapshotReceivedMs              = 0;
    private static final long SNAPSHOT_VALIDITY_MS        = 20_000L; // fallback if config not loaded

    private long getSnapshotValidityMs()
    {
        // v1.6.0 — the config is Type.SERVER now: unavailable outside a world.
        // This finally wires the SNAPSHOT_VALIDITY_MS fallback (dead since it
        // was written) instead of throwing on an unloaded spec.
        if (!ColonyLinkConfig.isLoaded()) return SNAPSHOT_VALIDITY_MS;
        return ColonyLinkConfig.WAREHOUSE_SNAPSHOT_VALIDITY_TICKS.get() * 50L; // ticks → ms
    }

    // v1.6.0 — delivery mode helpers (client side, synced SERVER config)

    private static boolean isWarehouseDeliveryMode()
    {
        return ColonyLinkConfig.getSendTarget() == ColonyLinkConfig.SendTarget.WAREHOUSE;
    }

    /**
     * Status actually shown/acted on for a builder resource row. In WAREHOUSE
     * mode two client-side overrides apply on top of the server status:
     *   - optimistic pending: the player just clicked Send (grey immediately,
     *     server truth takes over within PENDING_HOLD_MS);
     *   - finished Domum block already in the warehouse: a courier will deliver
     *     it on its own — the direct warehouse→builder bypass is a BUILDER-mode
     *     feature, so the row shows as pending instead of offering it.
     */
    private ResourceStatus displayStatus(ResourceStatus raw, ItemStack stack)
    {
        if (raw == ResourceStatus.SENT_PENDING || !isWarehouseDeliveryMode()) return raw;
        Long until = pendingSentUntil.get(pendingKey(stack));
        if (until != null && until > System.currentTimeMillis()) return ResourceStatus.SENT_PENDING;
        if (isDomumFinishedInWarehouse(stack)) return ResourceStatus.SENT_PENDING;
        return raw;
    }

    private static String pendingKey(ItemStack s) { return craftKey(s); }

    private void markPendingSent(ItemStack stack)
    {
        pendingSentUntil.put(pendingKey(stack), System.currentTimeMillis() + PENDING_HOLD_MS);
    }
    private enum WareCheckState { IDLE, LOADING, DONE }
    private WareCheckState wareCheckState = WareCheckState.IDLE;

    // #8: "Craft All in progress" state
    private boolean craftInProgress      = false;
    private int     craftInProgressCount = 0;   // items sent to craft


    public ColonyLinkScreen(ColonyLinkPacket packet)
    {
        super(Component.translatable("colonylink.screen.title"));
        applyPacket(packet);
    }

    // Craft-status anti-flicker (client only).
    //
    // AE2 cs.isRequesting() can flicker true/false mid-craft, which made the button
    // oscillate between CRAFTABLE and CRAFTING between ticker cycles. We smooth it
    // client-side: once an item is seen CRAFTING, keep showing CRAFTING for a short
    // window even if a later update reports CRAFTABLE again. AVAILABLE / NO_PATTERN /
    // MISSING stay priority (the craft is truly finished).
    private static final long CRAFT_HOLD_MS = 3000L;
    private final java.util.Map<String, Long> craftHoldUntil = new java.util.HashMap<>();

    private static String craftKey(ItemStack s)
    {
        return net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(s.getItem())
                + "#" + s.getComponents().hashCode();
    }

    private ResourceStatus smoothCraftStatus(ItemStack stack, ResourceStatus raw)
    {
        if (stack == null || stack.isEmpty()) return raw;
        String key = craftKey(stack);
        long now = System.currentTimeMillis();
        switch (raw)
        {
            case CRAFTING -> { craftHoldUntil.put(key, now + CRAFT_HOLD_MS); return ResourceStatus.CRAFTING; }
            case CRAFTABLE -> {
                Long until = craftHoldUntil.get(key);
                return (until != null && until > now) ? ResourceStatus.CRAFTING : ResourceStatus.CRAFTABLE;
            }
            default -> { craftHoldUntil.remove(key); return raw; }
        }
    }

    private List<ColonyLinkPacket.ResourceEntry> applyCraftHysteresis(List<ColonyLinkPacket.ResourceEntry> in)
    {
        if (in == null) return new ArrayList<>();
        long now = System.currentTimeMillis();
        craftHoldUntil.values().removeIf(until -> until <= now); // purge expired holds
        List<ColonyLinkPacket.ResourceEntry> out = new ArrayList<>(in.size());
        for (var e : in)
        {
            ResourceStatus disp = smoothCraftStatus(e.stack(), e.status());
            out.add(disp == e.status() ? e : new ColonyLinkPacket.ResourceEntry(
                    e.stack(), disp, e.realCount(), e.isDomum(), e.redirectorPos(), e.tooltipLines()));
        }
        return out;
    }

    private ColonyLinkPacket.BuilderRequest smoothRequest(ColonyLinkPacket.BuilderRequest r)
    {
        if (r == null || r.stack().isEmpty()) return r;
        ResourceStatus disp = smoothCraftStatus(r.stack(), r.status());
        return disp == r.status() ? r : new ColonyLinkPacket.BuilderRequest(
                r.stack(), r.count(), disp, r.redirectorPos(), r.tooltipLines(), r.cancellable());
    }

    private void applyPacket(ColonyLinkPacket packet)
    {
        this.entries        = applyCraftHysteresis(packet.entries());
        this.builderPos     = packet.builderPos();
        this.builderName    = packet.builderName();
        this.buildingName   = packet.buildingName();
        this.workerStatus   = packet.workerStatus();
        this.workerIdleReason = packet.workerIdleReason() != null ? packet.workerIdleReason() : "";
        this.availableCpus  = packet.availableCpus();
        this.redirectorState = packet.redirectorState();
        this.builderRequest = smoothRequest(packet.builderRequest() != null
                ? packet.builderRequest() : ColonyLinkPacket.BuilderRequest.NONE);
        this.hasWarehouseCard  = packet.hasWarehouseCard();
        this.warehousePriority = packet.warehousePriority();
        this.tabMetas       = packet.tabMetas() != null ? packet.tabMetas() : new ArrayList<>();
        // #12: don't overwrite activeTabIndex while on the Citizens tab
        if (isBuilderTab())
            this.activeTabIndex = packet.activeTabIndex();
        this.rfStored       = packet.rfStored();
        this.rfMax          = packet.rfMax() > 0 ? packet.rfMax() : 1_600_000L;

        // #8: a server refresh means crafts were processed → reset
        this.craftInProgress = false;

        if (!entries.isEmpty() && !entries.get(0).redirectorPos().equals(BlockPos.ZERO))
            this.redirectorPos = entries.get(0).redirectorPos();
        // Fallback: read from the client-side wand NBT when entries is empty (e.g. Citizens tab)
        // ClipboardFinder.findAll also covers Curios slots (CurseForge issue #3).
        if (this.redirectorPos.equals(BlockPos.ZERO) && this.minecraft != null && this.minecraft.player != null)
        {
            for (net.minecraft.world.item.ItemStack s : ClipboardFinder.findAll(this.minecraft.player))
            {
                BlockPos wandRedir = ColonyLinkWandLinkableHandler.getActiveRedirectorPos(s);
                if (wandRedir != null && !wandRedir.equals(BlockPos.ZERO))
                {
                    this.redirectorPos = wandRedir;
                    break;
                }
            }
        }

        int maxOffset = Math.max(0, entries.size() - MAX_VISIBLE);
        if (scrollOffset > maxOffset) scrollOffset = maxOffset;

        // #5: unread-tab handling. A tab is marked unread when left with entries and
        // read when returned to. The server only sends data for the active tab, so we
        // detect new requests at switch time.

        // A packet for the active tab with a count different from the last known one may mean new requests.
        int prevCount = lastReadEntryCount.getOrDefault(this.activeTabIndex, 0);
        int newCount  = this.entries.size();

        // The active tab is always read
        lastReadEntryCount.put(this.activeTabIndex, newCount);
        unreadTabs.remove(this.activeTabIndex);
        UNREAD_TAB_COUNT = unreadTabs.size();
    }

    public void updateFromPacket(ColonyLinkPacket packet)
    {
        applyPacket(packet);
        // 1.7.0-beta.5 (audit #9) — close through onClose(): setScreen(null) alone left
        // the server-side viewer alive, and its next packet reopened the Clipboard.
        if (tabMetas.isEmpty() && this.minecraft != null)
            this.onClose();
    }

    // #12: called by CitizensPacket.handle() when the server replies
    public void updateCitizens(CitizensPacket packet)
    {
        this.citizenEntries = packet.entries();
        this.citizensLoading = false;
        // 1.7.0-beta.3 — drop the optimistic overlays the server confirmed (the row now
        // carries a mark) or that expired. A server-side rejection thus un-greys the
        // row after PENDING_HOLD_MS at the latest.
        java.util.Set<java.util.UUID> marked = new java.util.HashSet<>();
        for (CitizensPacket.CitizenRequestEntry ce : citizenEntries)
            if (ce.mark() != CitizensPacket.MARK_NONE) marked.add(ce.requestId());
        long now = System.currentTimeMillis();
        optimisticCitizenSentUntil.entrySet().removeIf(e -> marked.contains(e.getKey()) || e.getValue() <= now);
        optimisticCitizenCraftUntil.entrySet().removeIf(e -> marked.contains(e.getKey()) || e.getValue() <= now);
    }

    // v1.8.0 — Stats tab: receive the single reply to a StatsRequestPacket.
    public void updateStats(StatsPacket packet)
    {
        this.statItemsSent    = packet.itemsSent();
        this.statAe2Crafts    = packet.ae2Crafts();
        this.statDomumCrafts  = packet.domumCrafts();
        this.statTomesUsed    = packet.tomesUsed();
        this.statCitizens     = packet.citizens();
        this.statMaxCitizens  = packet.maxCitizens();
        this.statGuards       = packet.guards();
        this.statBuilders     = packet.builders();
        this.statHappiness    = packet.happinessPercent();
        this.statAvgDistance  = packet.avgBedToWork();
        this.statsLoading     = false;
        this.statsLoaded      = true;
    }

    /**
     * v1.8.0 — Stats tab body: an "Activity" table (cumulative ColonyLink counters)
     * and a live "Colony Overview". Themed via the same helpers as the rest of the
     * screen (no textures), so it renders under Default / AE / MineColonies.
     */
    private void drawStatsContent(GuiGraphics g, int x, int y, int mx, int my)
    {
        ColonyLinkGuiConfig _c = ColonyLinkGuiConfig.get();
        boolean light = _c.isLightBody();
        int innerX  = x + 12;
        int rightX  = x + GUI_WIDTH - 12;
        int body    = _c.bodyText();
        int muted   = light ? _c.mutedText() : 0xAAAAAA;
        int faint   = light ? _c.mutedText() : 0x888888;

        if (statsLoading && !statsLoaded)
        {
            g.drawCenteredString(this.font, Component.translatable("colonylink.stats.loading").getString(),
                    x + GUI_WIDTH / 2, y + 60, muted);
            return;
        }

        // ---- Section 1: ColonyLink Activity ----
        drawStatsWell(g, _c, x, y + 22, y + 92);
        g.drawString(this.font, Component.translatable("colonylink.stats.activity").getString(),
                innerX, y + 28, body, false);

        int rowY = y + 44;
        drawStatRow(g, innerX, rightX, rowY,      Component.translatable("colonylink.stats.items_sent").getString(),  fmt(statItemsSent),   body, muted); rowY += 12;
        drawStatRow(g, innerX, rightX, rowY,      Component.translatable("colonylink.stats.ae2_crafts").getString(),  fmt(statAe2Crafts),   body, muted); rowY += 12;
        drawStatRow(g, innerX, rightX, rowY,      Component.translatable("colonylink.stats.domum_crafts").getString(),fmt(statDomumCrafts), body, muted); rowY += 12;
        drawStatRow(g, innerX, rightX, rowY,      Component.translatable("colonylink.stats.tomes_used").getString(),  fmt(statTomesUsed),   body, muted);

        // ---- Section 2: Colony Overview ----
        drawStatsWell(g, _c, x, y + 100, y + 196);
        g.drawString(this.font, Component.translatable("colonylink.stats.colony_overview").getString(),
                innerX, y + 106, body, false);

        // Citizens: cur / max
        drawStatRow(g, innerX, rightX, y + 122,
                Component.translatable("colonylink.stats.citizens").getString(),
                statCitizens + " / " + statMaxCitizens, body, muted);
        // Guards / Builders (indented sub-lines)
        drawStatRow(g, innerX + 8, rightX, y + 134,
                Component.translatable("colonylink.stats.guards").getString(),
                String.valueOf(statGuards), faint, faint);
        drawStatRow(g, innerX + 8, rightX, y + 146,
                Component.translatable("colonylink.stats.builders").getString(),
                String.valueOf(statBuilders), faint, faint);

        // Happiness bar
        g.drawString(this.font, Component.translatable("colonylink.stats.happiness").getString(),
                innerX, y + 162, body, false);
        int barX = innerX, barY = y + 172, barW = 120, barH = 6;
        int trackCol = _c.applyOpacity(_c.isAe() ? 0xFF413F54 : 0xFF2A2A2A);
        int fillCol  = light ? _c.semGreen() : 0xFF00E676;
        int filled   = Math.max(0, Math.min(barW, barW * statHappiness / 100));
        g.fill(barX, barY, barX + barW, barY + barH, trackCol);
        if (filled > 0) g.fill(barX, barY, barX + filled, barY + barH, fillCol);
        g.drawString(this.font, statHappiness + "%", barX + barW + 6, barY - 1, body, false);

        // Avg bed-to-work
        String dist = statAvgDistance < 0
                ? "—"
                : statAvgDistance + " " + Component.translatable("colonylink.stats.blocks").getString();
        drawStatRow(g, innerX, rightX, y + 184,
                Component.translatable("colonylink.stats.avg_distance").getString(), dist, body, muted);
    }

    /** One stats line: label at the left, right-aligned value. */
    private void drawStatRow(GuiGraphics g, int leftX, int rightX, int rowY,
                             String label, String value, int labelColor, int valueColor)
    {
        g.drawString(this.font, label, leftX, rowY, labelColor, false);
        g.drawString(this.font, value, rightX - this.font.width(value), rowY, valueColor, false);
    }

    /** Opaque section well on dark/AE bodies; on MineColonies the parchment shows through. */
    private void drawStatsWell(GuiGraphics g, ColonyLinkGuiConfig _c, int x, int top, int bottom)
    {
        if (_c.isMineColonies()) return;
        g.fill(x + 6, top, x + GUI_WIDTH - 6, bottom, _c.applyOpacity(_c.wellBg()));
        g.fill(x + 6, top, x + GUI_WIDTH - 6, top + 1, _c.applyOpacity(_c.wellLight()));
        g.fill(x + 6, top, x + 7, bottom, _c.applyOpacity(_c.wellLight()));
        g.fill(x + 6, bottom - 1, x + GUI_WIDTH - 6, bottom, _c.applyOpacity(_c.wellDark()));
        g.fill(x + GUI_WIDTH - 7, top, x + GUI_WIDTH - 6, bottom, _c.applyOpacity(_c.wellDark()));
    }

    /** Thousands-separated formatting for the activity counters. */
    private static String fmt(long v)
    {
        return String.format(java.util.Locale.ROOT, "%,d", v);
    }

    // 1.7.0-beta.3 — Citizens row button

    /** INFO: grey, disabled (Delivering / In Warehouse / Colony crafting). */
    private enum CitBtnKind { INFO, CRAFTING, SENT, IN_WH, SEND, CRAFT }

    private record CitBtn(CitBtnKind kind, String label, boolean clickable) {}

    private static boolean overlayActive(java.util.Map<java.util.UUID, Long> overlay, java.util.UUID id)
    {
        Long until = overlay.get(id);
        return until != null && until > System.currentTimeMillis();
    }

    /**
     * The button a Citizens row shows, highest priority first: what MineColonies is
     * already doing with the request, then ColonyLink's own mark (server memory or
     * optimistic overlay), then the Warehouse stock, then Send / Craft. Null = no button.
     */
    private CitBtn citizenButton(CitizensPacket.CitizenRequestEntry ce)
    {
        switch (ce.colonyState())
        {
            case CitizensPacket.STATE_DELIVERING ->
            { return new CitBtn(CitBtnKind.INFO, Component.translatable("colonylink.citizens.delivering").getString(), false); }
            case CitizensPacket.STATE_WAREHOUSE ->
            { return new CitBtn(CitBtnKind.INFO, Component.translatable("colonylink.citizens.in_warehouse").getString(), false); }
            case CitizensPacket.STATE_COLONY_CRAFT ->
            { return new CitBtn(CitBtnKind.INFO, Component.translatable("colonylink.citizens.colony_crafting").getString(), false); }
            default -> {}
        }
        boolean hasWH = hasWarehouseCard && !redirectorPos.equals(net.minecraft.core.BlockPos.ZERO);
        boolean canSend  = hasWH && ce.availableInME();
        boolean canCraft = hasWH && ce.craftableInME();
        if (ce.mark() == CitizensPacket.MARK_CRAFTING || overlayActive(optimisticCitizenCraftUntil, ce.requestId()))
            return new CitBtn(CitBtnKind.CRAFTING, Component.translatable("colonylink.citizens.crafting").getString(), false);
        if (ce.mark() == CitizensPacket.MARK_SENT || overlayActive(optimisticCitizenSentUntil, ce.requestId()))
            // Still re-clickable (Craft + Send, 2 packages) as the manual way out.
            return new CitBtn(CitBtnKind.SENT, Component.translatable("colonylink.screen.cit.sent").getString(),
                    canSend || canCraft);
        if (ce.warehouseStock() > 0 && ce.warehouseStock() >= ce.count())
            return new CitBtn(CitBtnKind.IN_WH,
                    Component.translatable("colonylink.citizens.in_wh_count", ce.warehouseStock()).getString(), false);
        if (canSend)
            return new CitBtn(CitBtnKind.SEND, Component.translatable("colonylink.screen.btn.send").getString(), true);
        if (canCraft)
            return new CitBtn(CitBtnKind.CRAFT, Component.translatable("colonylink.screen.btn.craft").getString(), true);
        return null;
    }

    /** Button width: 44 like before, wider when the label needs it (render and click share it). */
    private int citizenButtonWidth(CitBtn b)
    {
        return Math.max(44, this.font.width(b.label()) + 8);
    }

    private void drawCitizenButton(GuiGraphics g, CitBtn b, int bx, int by, int bw, int bh, boolean hov)
    {
        ColonyLinkGuiConfig _cl = ColonyLinkGuiConfig.get();
        boolean aeBtn = _cl.isAe() && aeButtonPresent;
        boolean mcBtn = mcButtonReady();
        switch (b.kind())
        {
            case SEND -> drawCitizenActiveButton(g, b.label(), bx, by, bw, bh, hov, mcBtn, aeBtn,
                    _cl.semBlue(), 0xFF0066CC, 0xFF004488, 0x4488FF);
            case CRAFT -> drawCitizenActiveButton(g, b.label(), bx, by, bw, bh, hov, mcBtn, aeBtn,
                    _cl.semGreen(), 0xFF007700, 0xFF005500, 0x00FF00);
            case SENT ->
            {
                // Grey, but hover feedback when re-clickable (unchanged look).
                boolean h = hov && b.clickable();
                if (mcBtn)
                    drawMcButtonAuto(g, bx, by, bw, bh, b.label(), false, h);
                else if (aeBtn)
                {
                    drawAeButtonBg(g, bx, by, bw, bh, AeBtnVis.DISABLED, _cl.alphaControl());
                    drawCenteredNoShadow(g, b.label(), bx + bw / 2, by + 3, _cl.semGray());
                }
                else
                    drawCitizenFlatButton(g, b.label(), bx, by, bw, bh, h ? 0xFF3A3A3A : 0xFF2A2A2A, 0x888888);
            }
            default ->
            {
                // INFO grey / CRAFTING amber / IN_WH pale green — all disabled, no hover.
                int aeCol   = b.kind() == CitBtnKind.CRAFTING ? _cl.semAmber()
                            : b.kind() == CitBtnKind.IN_WH    ? _cl.semGreen() : _cl.semGray();
                int flatCol = b.kind() == CitBtnKind.CRAFTING ? 0xFFAA00
                            : b.kind() == CitBtnKind.IN_WH    ? 0x88CC88 : 0x888888;
                String mcPrefix = b.kind() == CitBtnKind.CRAFTING ? "§6"
                                : b.kind() == CitBtnKind.IN_WH    ? "§2" : "";
                if (mcBtn)
                    drawMcButtonAuto(g, bx, by, bw, bh, mcPrefix + b.label(), false, false);
                else if (aeBtn)
                {
                    drawAeButtonBg(g, bx, by, bw, bh, AeBtnVis.DISABLED, _cl.alphaControl());
                    drawCenteredNoShadow(g, b.label(), bx + bw / 2, by + 3, aeCol);
                }
                else
                    drawCitizenFlatButton(g, b.label(), bx, by, bw, bh, 0xFF2A2A2A, flatCol);
            }
        }
    }

    private void drawCitizenActiveButton(GuiGraphics g, String label, int bx, int by, int bw, int bh, boolean hov,
                                         boolean mcBtn, boolean aeBtn, int aeCol, int bgHov, int bg, int textCol)
    {
        if (mcBtn)
            drawMcButtonAuto(g, bx, by, bw, bh, label, true, hov);
        else if (aeBtn)
            drawAeButton(g, bx, by, bw, bh, hov, true, label, aeCol);
        else
        {
            g.fill(bx, by, bx + bw, by + bh, hov ? bgHov : bg);
            g.fill(bx, by, bx + bw, by + 1, 0xFFFFFFFF);
            g.fill(bx, by, bx + 1, by + bh, 0xFFFFFFFF);
            g.fill(bx, by + bh - 1, bx + bw, by + bh, 0xFF222222);
            g.fill(bx + bw - 1, by, bx + bw, by + bh, 0xFF222222);
            g.drawCenteredString(this.font, label, bx + bw / 2, by + 3, textCol);
        }
    }

    private void drawCitizenFlatButton(GuiGraphics g, String label, int bx, int by, int bw, int bh, int bg, int textCol)
    {
        g.fill(bx, by, bx + bw, by + bh, bg);
        g.fill(bx, by, bx + bw, by + 1, 0xFF555555);
        g.fill(bx, by, bx + 1, by + bh, 0xFF555555);
        g.fill(bx, by + bh - 1, bx + bw, by + bh, 0xFF1A1A1A);
        g.fill(bx + bw - 1, by, bx + bw, by + bh, 0xFF1A1A1A);
        g.drawCenteredString(this.font, label, bx + bw / 2, by + 3, textCol);
    }

    public void updatePackageCount(int count)
    {
        this.citizenPackageCount = count;
    }

    private net.minecraft.world.item.ItemStack getClientWand()
    {
        if (this.minecraft == null || this.minecraft.player == null) return net.minecraft.world.item.ItemStack.EMPTY;
        // Same search order as the server (inventory, then Curios) — CurseForge issue #3.
        return ClipboardFinder.find(this.minecraft.player);
    }

    private static String stripItemName(String name)
    {
        return (name.startsWith("[") && name.endsWith("]")) ? name.substring(1, name.length() - 1) : name;
    }

    public void updateEntries(List<ColonyLinkPacket.ResourceEntry> newEntries, String builderName,
                              String buildingName, String workerStatus, int availableCpus,
                              String redirectorState, ColonyLinkPacket.BuilderRequest builderRequest,
                              boolean hasWarehouseCard, boolean warehousePriority)
    {
        this.entries        = applyCraftHysteresis(newEntries);
        this.builderName    = builderName;
        this.buildingName   = buildingName;
        this.workerStatus   = workerStatus;
        this.availableCpus  = availableCpus;
        this.redirectorState = redirectorState;
        this.builderRequest = smoothRequest(builderRequest != null ? builderRequest : ColonyLinkPacket.BuilderRequest.NONE);
        this.hasWarehouseCard  = hasWarehouseCard;
        this.warehousePriority = warehousePriority;
        if (!newEntries.isEmpty() && !newEntries.get(0).redirectorPos().equals(BlockPos.ZERO))
            this.redirectorPos = newEntries.get(0).redirectorPos();
        int maxOffset = Math.max(0, entries.size() - MAX_VISIBLE);
        if (scrollOffset > maxOffset) scrollOffset = maxOffset;
    }

    public void updateWarehouseSnapshot(WarehouseResultPacket packet)
    {
        this.warehouseSnapshot           = packet;
        this.warehouseSnapshotReceivedMs = System.currentTimeMillis();
        this.wareCheckState = packet.scanSuccess() ? WareCheckState.DONE : WareCheckState.IDLE;
    }

    // RF helpers
    private boolean isOutOfPower() { return rfStored <= 0; }

    // Coordinates
    private int getGuiX() { return (this.width - GUI_WIDTH - TAB_WIDTH) / 2 + TAB_WIDTH + dragOffsetX; }
    private int getGuiY() { return (this.height - GUI_HEIGHT) / 2 + dragOffsetY; }

    private int getListStartY()      { return getGuiY() + 112; }
    private int getScrollbarX()      { return getGuiX() + GUI_WIDTH - 16; }
    private int getScrollbarTop()    { return getListStartY() + 1; }
    private int getScrollbarBottom() { return getScrollbarTop() + MAX_VISIBLE * ENTRY_HEIGHT; }
    private int getScrollbarHeight() { return getScrollbarBottom() - getScrollbarTop(); }

    private int getTabX(int i) { return getGuiX() - TAB_WIDTH + (i == activeTabIndex ? TAB_OVERLAP : 0); }
    private int getTabY(int i) { return getGuiY() + TAB_Y_OFFSET + i * (TAB_HEIGHT + TAB_SPACING); }
    private int getAddTabY()   { return getTabY(tabMetas.size()); }

    // Config button — just left of the Restart button, in the title bar
    private static final int CFG_BTN_W = 16;
    private static final int CFG_BTN_H = 14;
    private int getCfgBtnX() { return getRestartBtnX() - CFG_BTN_W - 2; }
    private int getCfgBtnY() { return getRestartBtnY(); }

    // #12: Citizens tab — same column as builders, at the bottom with a big gap
    private int getCitizenTabX()  { return getGuiX() - TAB_WIDTH + (activeTabIndex == CITIZENS_TAB_INDEX ? TAB_OVERLAP : 0); }
    // v1.8.0 — Citizens moved up one slot; Stats takes the bottom-most pinned slot.
    private int getCitizenTabY()  { return getGuiY() + GUI_HEIGHT - 2 * TAB_HEIGHT - TAB_SPACING - 8; }
    private int getStatsTabX()    { return getGuiX() - TAB_WIDTH + (activeTabIndex == STATS_TAB_INDEX ? TAB_OVERLAP : 0); }
    private int getStatsTabY()    { return getGuiY() + GUI_HEIGHT - TAB_HEIGHT - 8; }

    // v1.8.0 — tab-class helpers. A "special" tab (Citizens or Stats) is not a builder
    // tab, so builder-specific UI (info/request panels, locate/craft/ware buttons, the
    // resource list) is suppressed for both.
    private boolean isStatsTab()   { return activeTabIndex == STATS_TAB_INDEX; }
    private boolean isBuilderTab() { return activeTabIndex != CITIZENS_TAB_INDEX && activeTabIndex != STATS_TAB_INDEX; }

    private int getWareCheckBtnX() { return getGuiX() + 8; }
    private int getWareCheckBtnY() { return getGuiY() + GUI_HEIGHT - 40; }
    private int getWareCheckBtnW() { return 120; }
    private int getWareCheckBtnH() { return 14; }

    private int getCraftAllBtnX() { return getGuiX() + 8; }
    private int getCraftAllBtnY() { return getGuiY() + GUI_HEIGHT - 22; }
    private int getCraftAllBtnW() { return 120; }
    private int getCraftAllBtnH() { return 16; }

    private int getSendAllBtnX() { return getGuiX() + GUI_WIDTH - 128; }
    private int getSendAllBtnY() { return getGuiY() + GUI_HEIGHT - 22; }
    private int getSendAllBtnW() { return 120; }
    private int getSendAllBtnH() { return 16; }

    private int getRestartBtnX() { return getGuiX() + GUI_WIDTH - 60; }
    private int getRestartBtnY() { return getGuiY() + 4; }
    private int getRestartBtnW() { return 52; }
    private int getRestartBtnH() { return 14; }

    // Locate button dimensions (position computed dynamically in drawInfoPanel / mouseClicked)
    private static final int LOCATE_BTN_W = 40;
    private static final int LOCATE_BTN_H = 14;

    private int getDeleteBtnX() { return getGuiX() + 8; }
    private int getDeleteBtnY() { return getGuiY() + 4; }
    private int getDeleteBtnW() { return 46; }
    private int getDeleteBtnH() { return 14; }

    private int getReqBtnX() { return getGuiX() + GUI_WIDTH - 76; }
    private int getReqBtnY() { return getGuiY() + 92; }
    private int getReqBtnW() { return 64; }
    private int getReqBtnH() { return 16; }

    // v1.6.4 — small "cancel request" square, top-right of the priority line
    // (title strip, above the main action button — no overlap with getReqBtn*).
    private int getCancelBtnX() { return getGuiX() + GUI_WIDTH - 17; }
    private int getCancelBtnY() { return getGuiY() + 82; }
    private int getCancelBtnW() { return 9; }
    private int getCancelBtnH() { return 9; }

    private int getSwitchX() { return getGuiX() + GUI_WIDTH - 118; }
    private int getSwitchY() { return getWareCheckBtnY(); }
    private int getSwitchW() { return 110; }
    private int getSwitchH() { return 14; }

    private int getThumbHeight()
    {
        if (entries.size() <= MAX_VISIBLE) return getScrollbarHeight();
        return Math.max(20, getScrollbarHeight() * MAX_VISIBLE / entries.size());
    }

    private int getThumbY()
    {
        if (entries.size() <= MAX_VISIBLE) return getScrollbarTop();
        int maxOffset = entries.size() - MAX_VISIBLE;
        return getScrollbarTop() + (getScrollbarHeight() - getThumbHeight()) * scrollOffset / maxOffset;
    }

    // Lifecycle
    @Override
    protected void init()
    {
        super.init();
        // 1.7.0-beta.5 (audit #8) — init() also runs after a resize, F11 or a return from
        // the config screen. Sending tab 0 while the Citizens tab was active moved the
        // server-side viewer to builder tab 0: no more Citizens refreshes (frozen list)
        // and tab 0 written to the wand. The Citizens sentinel (Integer.MAX_VALUE) is
        // preserved as-is by GuiStatePacket.sanitizeTab(), so send the active tab itself.
        PacketDistributor.sendToServer(new GuiStatePacket(true, builderPos, activeTabIndex));
        closePacketSent = false; // 1.7.0-beta.5 — this open owes the server one close
        // Read the package count from the client-side wand NBT (read-only)
        net.minecraft.world.item.ItemStack initWand = getClientWand();
        if (!initWand.isEmpty())
            this.citizenPackageCount = ColonyLinkWandLinkableHandler.getCitizenPackages(initWand);

        // Theme asset probes — ONCE per screen open (never per frame), silent
        // procedural fallbacks if an internal asset path is absent (never magenta).
        // UI pass 1: routed through AeGuiBlits.probeTexture (same semantics).
        this.aeBackgroundPresent = AeGuiBlits.probeTexture(AeGuiBlits.AE_BACKGROUND);
        // Layer 4: probe the button-sprite PNG once (proxy for the atlas sprites).
        this.aeButtonPresent = AeGuiBlits.probeTexture(AeGuiBlits.AE_BTN_PROBE);
        // MineColonies theme: parchment, buttons pivot, side-tabs pivot.
        this.mcPaperPresent = AeGuiBlits.probeTexture(MC_PAPER);
        this.mcButtonPresent = AeGuiBlits.probeTexture(MC_BTN_PROBE);
        this.mcTabPresent = AeGuiBlits.probeTexture(MC_TAB_PROBE);
    }

    /**
     * Screen X coordinate → scaled-GUI-space coordinate. Returns the value
     * unchanged when scale == 1.
     */
    private int toGuiX(double screenX)
    {
        float s = ColonyLinkGuiConfig.get().scale;
        if (s == 1.0f) return (int) screenX;
        float cx = this.width / 2f;
        return (int)((screenX - cx) / s + cx);
    }

    private int toGuiY(double screenY)
    {
        float s = ColonyLinkGuiConfig.get().scale;
        if (s == 1.0f) return (int) screenY;
        float cy = this.height / 2f;
        return (int)((screenY - cy) / s + cy);
    }

    /**
     * Inverse of toGuiX/toGuiY: logical GUI coordinate → real screen space (post-pose).
     * Needed for enableScissor(), which applies only the window guiScale and IGNORES the
     * pose() matrix — so a scissor computed in logical coords misses once scale != 1.0.
     */
    private int toScreenX(double guiX)
    {
        float s = ColonyLinkGuiConfig.get().scale;
        if (s == 1.0f) return (int) Math.round(guiX);
        float cx = this.width / 2f;
        return (int) Math.round((guiX - cx) * s + cx);
    }

    private int toScreenY(double guiY)
    {
        float s = ColonyLinkGuiConfig.get().scale;
        if (s == 1.0f) return (int) Math.round(guiY);
        float cy = this.height / 2f;
        return (int) Math.round((guiY - cy) * s + cy);
    }

    /** Drag handle zone: between the Unlink button (end) and the Restart button (start), in the title bar. */
    private boolean isInDragHandle(double mx, double my)
    {
        int x = getGuiX(), y = getGuiY();
        int handleX1 = x + getDeleteBtnW() + 12; // just after Unlink
        // Excludes the cfg button zone (left of Restart) + Restart itself
        int handleX2 = getCfgBtnX() - 4;
        int handleY1 = y + 2;
        int handleY2 = y + 20;
        return mx >= handleX1 && mx <= handleX2 && my >= handleY1 && my <= handleY2;
    }

    @Override
    public void onClose()
    {
        sendClosePacket(activeTabIndex);
        super.onClose();
    }

    /**
     * 1.7.0-beta.5 (audit #9) — Minecraft.setScreen() swaps screens through removed(),
     * never through onClose(): death screen, disconnect, another mod's GUI, our own
     * config screen. The server-side viewer then survived, the ticker kept pushing
     * ColonyLinkPackets and the client handler reopened the Clipboard on its own (even
     * over the death screen). Every open (init) is now matched by exactly one close
     * packet, whichever way the screen goes away.
     */
    @Override
    public void removed()
    {
        sendClosePacket(activeTabIndex);
        super.removed();
    }

    private void sendClosePacket(int tabIndex)
    {
        if (closePacketSent) return;
        closePacketSent = true;
        // Connection already torn down (disconnect): nothing left to tell the server.
        if (this.minecraft == null || this.minecraft.getConnection() == null) return;
        PacketDistributor.sendToServer(new GuiStatePacket(false, builderPos, tabIndex));
    }

    // Button helpers
    private int getButtonColor(ResourceStatus status)
    {
        return switch (status) {
            case AVAILABLE    -> 0xFF004488;
            case CRAFTABLE    -> 0xFF005500;
            case NO_PATTERN   -> 0xFF550000;
            case CRAFTING     -> 0xFF885500;
            case MISSING      -> 0xFF5D3A00;
            case SENT_PENDING -> 0xFF2A2A2A; // grey — same palette as the citizens "Sent" button
        };
    }

    private int getButtonHoverColor(ResourceStatus status)
    {
        return switch (status) {
            case AVAILABLE    -> 0xFF0066CC;
            case CRAFTABLE    -> 0xFF007700;
            case NO_PATTERN   -> 0xFF660000;
            case CRAFTING     -> 0xFF885500;
            case MISSING      -> 0xFF8B5E00;
            case SENT_PENDING -> 0xFF3A3A3A;
        };
    }

    private int getButtonTextColor(ResourceStatus status)
    {
        return switch (status) {
            case AVAILABLE    -> 0x4488FF;
            case CRAFTABLE    -> 0x00FF00;
            case NO_PATTERN   -> 0xFF4444;
            case CRAFTING     -> 0xFFAA00;
            case MISSING      -> 0xFFCC66;
            case SENT_PENDING -> 0x888888;
        };
    }

    private String getButtonText(ResourceStatus status)
    {
        return switch (status) {
            case AVAILABLE    -> Component.translatable("colonylink.screen.btn.send").getString();
            case CRAFTABLE    -> Component.translatable("colonylink.screen.btn.craft").getString();
            case NO_PATTERN   -> Component.translatable("colonylink.screen.btn.no_pattern").getString();
            case CRAFTING     -> Component.translatable("colonylink.screen.btn.crafting").getString();
            case MISSING      -> Component.translatable("colonylink.screen.btn.missing").getString();
            case SENT_PENDING -> Component.translatable("colonylink.screen.btn.sent_pending").getString();
        };
    }

    private String getRequestButtonText(ResourceStatus status)
    {
        return switch (status) {
            case AVAILABLE    -> Component.translatable("colonylink.screen.btn.fulfill").getString();
            case CRAFTABLE    -> Component.translatable("colonylink.screen.btn.craft").getString();
            case NO_PATTERN   -> Component.translatable("colonylink.screen.btn.no_pattern").getString();
            case CRAFTING     -> Component.translatable("colonylink.screen.btn.crafting").getString();
            case MISSING      -> Component.translatable("colonylink.screen.btn.missing").getString();
            case SENT_PENDING -> Component.translatable("colonylink.screen.btn.sent_pending").getString();
        };
    }

    private boolean isButtonClickable(ResourceStatus status)
    {
        if (isOutOfPower()) return false;
        if (status == ResourceStatus.AVAILABLE && !redirectorReady()) return false;
        return status == ResourceStatus.CRAFTABLE
                || status == ResourceStatus.AVAILABLE
                || status == ResourceStatus.MISSING;
    }

    private boolean isButtonClickable(ResourceStatus status, ItemStack stack)
    {
        if (isOutOfPower()) return false;
        if (isButtonClickable(status)) return true;
        if (status == ResourceStatus.NO_PATTERN)
        {
            // Domum items without pattern → always clickable (sent to the terminal queue)
            if (DomumCraftHandler.isDomumItem(stack)) return true;
            WarehouseResultPacket.WarehouseEntry we = getWarehouseEntry(stack);
            return we != null && (we.inWarehouse() > 0 || we.viaCraft() > 0);
        }
        return false;
    }

    private String getButtonTextWithWarehouse(ResourceStatus status, ItemStack stack)
    {
        // v1.6.0 — pending always wins over the warehouse fast-path hints.
        if (status == ResourceStatus.SENT_PENDING) return getButtonText(status);
        // v1.4.9 — finished Domum block in the warehouse → delivered directly (Send).
        if (isDomumFinishedInWarehouse(stack)) return Component.translatable("colonylink.screen.btn.send_wh").getString();
        if (status == ResourceStatus.NO_PATTERN)
        {
            WarehouseResultPacket.WarehouseEntry we = getWarehouseEntry(stack);
            if (we != null && we.inWarehouse() > 0) return Component.translatable("colonylink.screen.btn.send_wh").getString();
            if (we != null && we.viaCraft() > 0)    return Component.translatable("colonylink.screen.btn.craft_wh").getString();
        }
        return getButtonText(status);
    }

    private int getButtonColorWithWarehouse(ResourceStatus status, ItemStack stack, boolean hovered)
    {
        // v1.6.0 — pending always wins over the warehouse fast-path hints.
        if (status == ResourceStatus.SENT_PENDING) return getButtonColor(status);
        // v1.4.9 — finished Domum block in the warehouse → Send color (green).
        if (isDomumFinishedInWarehouse(stack)) return hovered ? 0xFF336655 : 0xFF224433;
        if (status == ResourceStatus.NO_PATTERN)
        {
            WarehouseResultPacket.WarehouseEntry we = getWarehouseEntry(stack);
            if (we != null && (we.inWarehouse() > 0 || we.viaCraft() > 0))
                return hovered ? 0xFF336655 : 0xFF224433;
        }
        return hovered && isButtonClickable(status) ? getButtonHoverColor(status) : getButtonColor(status);
    }

    private WarehouseResultPacket.WarehouseEntry getWarehouseEntry(ItemStack stack)
    {
        if (warehouseSnapshot == null) return null;
        if (System.currentTimeMillis() - warehouseSnapshotReceivedMs > getSnapshotValidityMs())
        {
            warehouseSnapshot = null;
            wareCheckState = WareCheckState.IDLE;
            return null;
        }
        for (WarehouseResultPacket.WarehouseEntry entry : warehouseSnapshot.entries())
            if (ItemStack.isSameItem(entry.stack(), stack)) return entry;
        return null;
    }

    private boolean hasWarehouseCraft(ItemStack stack)
    {
        WarehouseResultPacket.WarehouseEntry e = getWarehouseEntry(stack);
        return e != null && (e.viaCraft() > 0 || e.inWarehouse() > 0);
    }

    private boolean hasCraftableItems()
    {
        if (isOutOfPower()) return false;
        return entries.stream().anyMatch(e ->
                e.status() == ResourceStatus.CRAFTABLE || e.status() == ResourceStatus.MISSING);
    }

    private boolean hasAvailableItems()
    {
        if (isOutOfPower()) return false;
        if (redirectorState.equals("N/A") || redirectorState.equals("NOT_LINKED")) return false;
        return entries.stream().anyMatch(e -> e.status() == ResourceStatus.AVAILABLE);
    }

    /**
     * v1.4.9 — true if {@code stack} is a Domum block whose FINISHED form is directly
     * present in the warehouse snapshot. Such a block is delivered Warehouse → Builder
     * by the Send action, without going through AE2.
     */
    private boolean isDomumFinishedInWarehouse(ItemStack stack)
    {
        if (!DomumCraftHandler.isDomumItem(stack)) return false;
        WarehouseResultPacket.WarehouseEntry we = getWarehouseEntry(stack);
        return we != null && we.inWarehouse() > 0;
    }

    /** v1.4.9 — sendable = AVAILABLE in ME, or a finished Domum block sitting in the warehouse. */
    private boolean hasSendableItems()
    {
        if (isOutOfPower()) return false;
        if (redirectorState.equals("N/A") || redirectorState.equals("NOT_LINKED")) return false;
        // v1.6.0 — WAREHOUSE mode: only ME-available rows are sendable (the
        // Domum-in-warehouse fast path is a BUILDER-mode courier bypass, and
        // pending rows are excluded by displayStatus).
        if (isWarehouseDeliveryMode())
            return entries.stream().anyMatch(e ->
                    displayStatus(e.status(), e.stack()) == ResourceStatus.AVAILABLE);
        return entries.stream().anyMatch(e ->
                e.status() == ResourceStatus.AVAILABLE || isDomumFinishedInWarehouse(e.stack()));
    }

    private boolean redirectorReady()
    {
        return redirectorState.equals("LINKED") || redirectorState.equals("STANDBY");
    }

    private int getWorkerStatusColor()
    {
        // Theme-aware: on any LIGHT body (AE or MineColonies) the status colors map to
        // the light-calibrated semantic tints (green stays green, etc.); in DEFAULT the
        // original bright colors are kept unchanged. The vivid dark-background colors are
        // unreadable on a light parchment/terminal body.
        ColonyLinkGuiConfig c = ColonyLinkGuiConfig.get();
        boolean ae = c.isLightBody();
        if (workerStatus == null) return ae ? c.semGray() : 0x888888;
        if (workerStatus.equals("Working"))                                    return ae ? c.semGreen()  : 0x00FF00;
        if (workerStatus.equals("Idle"))                                       return ae ? c.semAmber()  : 0xFFFF00;
        if (workerStatus.equals("Hungry"))                                     return ae ? c.semOrange() : 0xFFAA00;
        if (workerStatus.equals("Sleeping"))                                   return ae ? c.semBlue()   : 0x4488FF;
        if (workerStatus.equals("Bad weather"))                                return ae ? c.semSlate()  : 0x88AACC;
        if (workerStatus.equals("Sick"))                                       return ae ? c.semRed()    : 0xFF4444;
        if (workerStatus.equals("Mourning"))                                   return ae ? c.semGray()   : 0x888888;
        if (workerStatus.equals("Raided!"))                                    return ae ? c.semRed()    : 0xFF0000;
        if (workerStatus.equals("No home"))                                    return ae ? c.semAmber()  : 0xFFCC44;
        // Fallback for unknown translated statuses
        String low = workerStatus.toLowerCase();
        if (low.contains("work"))                                              return ae ? c.semGreen()  : 0x00FF00;
        if (low.contains("sleep"))                                             return ae ? c.semBlue()   : 0x4488FF;
        if (low.contains("eat") || low.contains("food"))                       return ae ? c.semOrange() : 0xFFAA00;
        if (low.contains("sick"))                                              return ae ? c.semRed()    : 0xFF4444;
        if (low.contains("idle"))                                              return ae ? c.semAmber()  : 0xFFFF00;
        return ae ? c.semGray() : 0xCCCCCC;
    }

    /** Translates the English wire status identifier into the client's language for display. */
    private static String translateStatus(String s)
    {
        if (s == null) return "";
        return switch (s)
        {
            case "Working"     -> Component.translatable("colonylink.status.working").getString();
            case "Idle"        -> Component.translatable("colonylink.status.idle").getString();
            case "Hungry"      -> Component.translatable("colonylink.status.hungry").getString();
            case "Sleeping"    -> Component.translatable("colonylink.status.sleeping").getString();
            case "Bad weather" -> Component.translatable("colonylink.status.bad_weather").getString();
            case "Sick"        -> Component.translatable("colonylink.status.sick").getString();
            case "Mourning"    -> Component.translatable("colonylink.status.mourning").getString();
            case "Raided!"     -> Component.translatable("colonylink.status.raided").getString();
            case "No home"     -> Component.translatable("colonylink.status.no_home").getString();
            default            -> s;
        };
    }

    private void getBtnBounds(int i, int[] out)
    {
        int x = getGuiX();
        int listWidth = GUI_WIDTH - 26;
        int entryY = getListStartY() + i * ENTRY_HEIGHT;
        out[0] = x + 7 + listWidth - 60;
        out[1] = entryY + 2;
        out[2] = 58;
        out[3] = 16;
    }

    // Tabs

    /** True when the MineColonies theme is active AND the tab textures are available. */
    private boolean mcTabReady()
    {
        ColonyLinkGuiConfig c = ColonyLinkGuiConfig.get();
        return c.isMineColonies() && mcPaperPresent && mcTabPresent;
    }

    /** Cycle tab_left_side1/2/3 by index for visual variety (matches MineColonies). */
    private static ResourceLocation mcTabTex(int i)
    {
        return switch (Math.floorMod(i, 3)) { case 0 -> MC_TAB1; case 1 -> MC_TAB2; default -> MC_TAB3; };
    }

    /**
     * MineColonies textured side tab: blits the LEFT 20px of the cream paper tab (no mirror,
     * 1:1 horizontally, stretched only 26→24 vertically), tinted by brightness ONLY. The unread
     * and unlinked states are NOT painted onto the tab background (a rectangular overlay clashed
     * with the torn transparent paper edge); they are carried by the tab ICON colour instead (see
     * the caller). Priority for brightness: active(1.0) > unlinked/unread(0.90, tab stays neutral)
     * > hover(0.82) > inactive(0.90). Blend/tint reset by the blit helper.
     */
    private void drawMcTab(GuiGraphics g, int tx, int ty, int tw, int th, int variantIndex,
                           boolean active, boolean hover, boolean unlinked, boolean unread)
    {
        ColonyLinkGuiConfig c = ColonyLinkGuiConfig.get();
        float alpha = c.alphaControl();
        float bright;
        if (active)                   bright = 1f;
        else if (unlinked || unread)  bright = MC_TAB_INACTIVE_DIM; // state shown via icon, not the tab
        else if (hover)               bright = MC_HOVER_DARKEN;
        else                          bright = MC_TAB_INACTIVE_DIM;
        // Left 20px of the 32×26 texture → the tab rect (tw≈20). No horizontal compression.
        // Blit nudged +1px right (MC_TAB_BLIT_DX) to overlap the parchment and hide the seam;
        // the caller's hitbox uses the un-nudged tx, so clicks are unaffected.
        blitTintedStretched(g, mcTabTex(variantIndex), tx + MC_TAB_BLIT_DX, ty, tw, th,
                0f, 0f, 20, 26, 32, 26, bright, alpha);
    }

    private void drawTabs(GuiGraphics g, int mx, int my, List<Component> tip)
    {
        for (int i = 0; i < tabMetas.size(); i++)
        {
            var meta = tabMetas.get(i);
            boolean active = (i == activeTabIndex);
            int tx = getTabX(i), ty = getTabY(i), tw = TAB_WIDTH, th = TAB_HEIGHT;

            int bg, bl, bd;
            ColonyLinkGuiConfig _tabCfg = ColonyLinkGuiConfig.get();
            boolean hasUnread = unreadTabs.contains(i);

            if (mcTabReady())
            {
                // MineColonies: cream paper tab, tinted by state (unlinked=brown, unread=blue).
                boolean hov = mx >= tx && mx <= tx + tw && my >= ty && my <= ty + th;
                drawMcTab(g, tx, ty, tw, th, i, active, hov, !meta.hasRedirector(), hasUnread);
            }
            else
            {
                if (active)
                {
                    // Active tab: themed chrome (Default = lightened config bg, AE = palette)
                    bg = _tabCfg.tabActiveBg();
                    bl = _tabCfg.border();
                    bd = _tabCfg.borderShadow();
                }
                else if (!meta.hasRedirector())
                {
                    // No redirector → brown (SEMANTIC: encodes the "unlinked" state)
                    bg = 0xFF5A3A10; bl = 0xFF886633; bd = 0xFF221500;
                }
                else if (hasUnread)
                {
                    if (_tabCfg.isAe())
                    {
                        // AE: unread notification accent in blue (semBlue, standing in for
                        // AE2's focus-blue — our tabs are procedural, not the sprite) instead
                        // of amber. Colour only — the unread trigger, hover and geometry are
                        // unchanged; the bevels are lightened/darkened from the same blue.
                        bg = _tabCfg.semBlue();
                        bl = lighten(_tabCfg.semBlue(), 1.5f);
                        bd = darken(_tabCfg.semBlue(), 0.45f);
                    }
                    else
                    {
                        // #6: inactive tab with unread requests → orange (SEMANTIC: notification)
                        bg = 0xFF7A4A1A; bl = 0xFFCC8833; bd = 0xFF3A2008;
                    }
                }
                else
                {
                    // Normal inactive tab → neutral themed chrome
                    bg = _tabCfg.tabInactiveBg(); bl = _tabCfg.tabInactiveLight(); bd = _tabCfg.tabInactiveDark();
                }

                // Control category: tab chrome follows the floored control alpha.
                bg = _tabCfg.applyControl(bg); bl = _tabCfg.applyControl(bl); bd = _tabCfg.applyControl(bd);
                g.fill(tx, ty, tx + tw, ty + th, bg);
                g.fill(tx, ty, tx + tw, ty + 1, bl);
                g.fill(tx, ty, tx + 1, ty + th, bl);
                g.fill(tx, ty + th - 1, tx + tw, ty + th, bd);
                if (!active) g.fill(tx + tw - 1, ty, tx + tw, ty + th, bd);
            }
            // MineColonies: the marker icon carries the state (priority unlinked > unread > normal).
            // AE/DEFAULT ignore mcCol (drawGearIcon keeps their own colour logic).
            int mcCol = !meta.hasRedirector() ? MC_TAB_UNLINKED_ICON
                      : hasUnread            ? _tabCfg.semBlue()
                      :                        MC_TAB_ICON_DARK;
            int iconDx = mcTabReady() ? MC_TAB_BLIT_DX : 0; // follow the +1px MC blit nudge
            drawGearIcon(g, tx + (tw - 10) / 2 + iconDx, ty + (th - 10) / 2, active, meta.hasRedirector(), mcCol);

            if (mx >= tx && mx <= tx + tw && my >= ty && my <= ty + th)
            {
                tip.clear();
                tip.add(Component.literal("§f" + meta.builderName()));
                tip.add(Component.literal("§7" + meta.buildingLabel()));
                tip.add(Component.literal("§8@ " + meta.builderPos().toShortString()));
                tip.add(meta.hasRedirector()
                        ? Component.translatable("colonylink.screen.tip.redirector_linked")
                        : Component.translatable("colonylink.screen.tip.no_redirector"));
            }
        }

        if (tabMetas.size() < ColonyLinkWandLinkableHandler.getMaxBuilders())
        {
            int tx = getGuiX() - TAB_WIDTH, ty = getAddTabY(), tw = TAB_WIDTH, th = TAB_HEIGHT;
            boolean hov = mx >= tx && mx <= tx + tw && my >= ty && my <= ty + th;
            ColonyLinkGuiConfig _addCfg = ColonyLinkGuiConfig.get();
            int cx = tx + tw / 2, cy = ty + th / 2;
            // Control category: add-builder tab follows the floored control alpha.
            if (mcTabReady())
            {
                // MineColonies: cream paper tab + a dark-green "+" that reads on the paper.
                drawMcTab(g, tx, ty, tw, th, tabMetas.size(), false, hov, false, false);
                int pcx = cx + MC_TAB_BLIT_DX; // follow the +1px MC blit nudge
                int plus = _addCfg.applyControl(hov ? 0xFF1F8A34 : 0xFF176B28);
                g.fill(pcx - 3, cy - 1, pcx + 4, cy + 2, plus);
                g.fill(pcx - 1, cy - 3, pcx + 2, cy + 4, plus);
            }
            else
            {
                g.fill(tx, ty, tx + tw, ty + th, _addCfg.applyControl(hov ? 0xFF226622 : 0xFF1A4A1A));
                g.fill(tx, ty, tx + tw, ty + 1, _addCfg.applyControl(0xFF44AA44));
                g.fill(tx, ty, tx + 1, ty + th, _addCfg.applyControl(0xFF44AA44));
                g.fill(tx, ty + th - 1, tx + tw, ty + th, _addCfg.applyControl(0xFF113311));
                g.fill(tx + tw - 1, ty, tx + tw, ty + th, _addCfg.applyControl(0xFF113311));
                g.fill(cx - 3, cy - 1, cx + 4, cy + 2, _addCfg.applyControl(0xFF44FF44));
                g.fill(cx - 1, cy - 3, cx + 2, cy + 4, _addCfg.applyControl(0xFF44FF44));
            }
            if (hov)
            {
                tip.clear();
                tip.add(Component.translatable("colonylink.screen.tip.add_builder"));
                tip.add(Component.translatable("colonylink.screen.tip.start_pairing"));
            }
        }

        // #12: Citizens tab — same style as builder tabs, same column
        {
            int tx = getCitizenTabX(), ty = getCitizenTabY(), tw = TAB_WIDTH, th = TAB_HEIGHT;
            boolean active = (activeTabIndex == CITIZENS_TAB_INDEX);
            boolean hov    = mx >= tx && mx <= tx + tw && my >= ty && my <= ty + th;

            // Same overlap mechanic as builder tabs (shifts right when active)
            int drawTx = active ? tx + TAB_OVERLAP : tx;

            ColonyLinkGuiConfig _tabCfg = ColonyLinkGuiConfig.get();
            if (mcTabReady())
            {
                // MineColonies: same cream paper tab as builders (Citizens is never unlinked/unread).
                drawMcTab(g, drawTx, ty, tw, th, tabMetas.size() + 1, active, hov, false, false);
            }
            else
            {
                int bg, bl, bd;
                if (active)
                {
                    bg = _tabCfg.tabActiveBg();
                    bl = _tabCfg.border();
                    bd = _tabCfg.borderShadow();
                }
                else
                {
                    // Same neutral themed chrome as inactive builder tabs (with hover)
                    bg = _tabCfg.tabInactiveBg(hov);
                    bl = _tabCfg.tabInactiveLight();
                    bd = _tabCfg.tabInactiveDark();
                }

                // Control category: Citizens tab chrome follows the floored control alpha.
                bg = _tabCfg.applyControl(bg); bl = _tabCfg.applyControl(bl); bd = _tabCfg.applyControl(bd);
                g.fill(drawTx, ty, drawTx + tw, ty + th, bg);
                g.fill(drawTx, ty, drawTx + tw, ty + 1, bl);
                g.fill(drawTx, ty, drawTx + 1, ty + th, bl);
                g.fill(drawTx, ty + th - 1, drawTx + tw, ty + th, bd);
                if (!active) g.fill(drawTx + tw - 1, ty, drawTx + tw, ty + th, bd);
            }

            // Centered pixel-art person icon — neutral, themed (dark in MC on the light paper).
            // In MC, nudged +1px to follow the tab blit nudge.
            int cx = drawTx + tw / 2 + (mcTabReady() ? MC_TAB_BLIT_DX : 0), cy = ty + th / 2 - 1;
            int col = _tabCfg.applyControl(_tabCfg.isMineColonies()
                    ? (active ? 0xFF000000 : (hov ? 0xFF3A3A3A : 0xFF5A5040))
                    : _tabCfg.isAe()
                    ? (active ? 0xFFF2F2F2 : (hov ? 0xFFCBCCD4 : 0xFF9A9FB4))
                    : (active ? 0xFFEEEEEE : (hov ? 0xFFCCCCCC : 0xFFAAAAAA)));
            g.fill(cx - 2, cy - 5, cx + 3, cy - 1, col); // head
            g.fill(cx - 3, cy - 1, cx + 4, cy + 3, col); // body
            g.fill(cx - 3, cy + 3, cx - 1, cy + 6, col); // left leg
            g.fill(cx + 1, cy + 3, cx + 4, cy + 6, col); // right leg

            if (hov)
            {
                tip.clear();
                tip.add(Component.translatable("colonylink.screen.tip.citizens"));
                tip.add(Component.translatable("colonylink.screen.tip.citizens_desc"));
                if (!citizenEntries.isEmpty())
                    tip.add(Component.translatable("colonylink.screen.tip.requests_count", citizenEntries.size()));
            }
        }

        // v1.8.0: Stats tab — same chrome as the other tabs, bottom-most slot.
        {
            int tx = getStatsTabX(), ty = getStatsTabY(), tw = TAB_WIDTH, th = TAB_HEIGHT;
            boolean active = (activeTabIndex == STATS_TAB_INDEX);
            boolean hov    = mx >= tx && mx <= tx + tw && my >= ty && my <= ty + th;

            int drawTx = active ? tx + TAB_OVERLAP : tx;

            ColonyLinkGuiConfig _tabCfg = ColonyLinkGuiConfig.get();
            if (mcTabReady())
            {
                drawMcTab(g, drawTx, ty, tw, th, tabMetas.size() + 2, active, hov, false, false);
            }
            else
            {
                int bg, bl, bd;
                if (active)
                {
                    bg = _tabCfg.tabActiveBg();
                    bl = _tabCfg.border();
                    bd = _tabCfg.borderShadow();
                }
                else
                {
                    bg = _tabCfg.tabInactiveBg(hov);
                    bl = _tabCfg.tabInactiveLight();
                    bd = _tabCfg.tabInactiveDark();
                }
                bg = _tabCfg.applyControl(bg); bl = _tabCfg.applyControl(bl); bd = _tabCfg.applyControl(bd);
                g.fill(drawTx, ty, drawTx + tw, ty + th, bg);
                g.fill(drawTx, ty, drawTx + tw, ty + 1, bl);
                g.fill(drawTx, ty, drawTx + 1, ty + th, bl);
                g.fill(drawTx, ty + th - 1, drawTx + tw, ty + th, bd);
                if (!active) g.fill(drawTx + tw - 1, ty, drawTx + tw, ty + th, bd);
            }

            // Centered pixel-art "signal bars" icon — neutral, themed (same colour logic
            // as the Citizens person icon). Four bars rising left→right.
            int cx = drawTx + tw / 2 + (mcTabReady() ? MC_TAB_BLIT_DX : 0), cy = ty + th / 2 - 1;
            int col = _tabCfg.applyControl(_tabCfg.isMineColonies()
                    ? (active ? 0xFF000000 : (hov ? 0xFF3A3A3A : 0xFF5A5040))
                    : _tabCfg.isAe()
                    ? (active ? 0xFFF2F2F2 : (hov ? 0xFFCBCCD4 : 0xFF9A9FB4))
                    : (active ? 0xFFEEEEEE : (hov ? 0xFFCCCCCC : 0xFFAAAAAA)));
            int bx = cx - 5, baseY = cy + 5;
            g.fill(bx,      baseY - 2, bx + 2,  baseY, col); // bar 1 (shortest)
            g.fill(bx + 3,  baseY - 4, bx + 5,  baseY, col); // bar 2
            g.fill(bx + 6,  baseY - 6, bx + 8,  baseY, col); // bar 3
            g.fill(bx + 9,  baseY - 8, bx + 11, baseY, col); // bar 4 (tallest)

            if (hov)
            {
                tip.clear();
                tip.add(Component.translatable("colonylink.tab.stats"));
                tip.add(Component.translatable("colonylink.stats.tab_desc"));
            }
        }
    }

    /** Lightens an ARGB colour by a factor (e.g. 1.3f). */
    private static int lighten(int argb, float f)
    {
        int a  = (argb >> 24) & 0xFF;
        int r  = Math.min(255, (int)(((argb >> 16) & 0xFF) * f));
        int gv = Math.min(255, (int)(((argb >> 8)  & 0xFF) * f));
        int b  = Math.min(255, (int)(( argb        & 0xFF) * f));
        return (a << 24) | (r << 16) | (gv << 8) | b;
    }

    private static int darken(int argb, float f)
    {
        int a  = (argb >> 24) & 0xFF;
        int r  = Math.max(0, (int)(((argb >> 16) & 0xFF) * f));
        int gv = Math.max(0, (int)(((argb >> 8)  & 0xFF) * f));
        int b  = Math.max(0, (int)(( argb        & 0xFF) * f));
        return (a << 24) | (r << 16) | (gv << 8) | b;
    }

    /** Blends two colours (r/g/b only, ignores source alpha). */
    private static int blendColor(int base, int blueHint, float blueWeight)
    {
        int r  = (int)(((base >> 16) & 0xFF) * (1 - blueWeight));
        int gv = (int)(((base >> 8)  & 0xFF) * (1 - blueWeight));
        int b  = Math.min(255, (int)(((base & 0xFF) * (1 - blueWeight)) + (blueHint * blueWeight)));
        return 0xFF000000 | (r << 16) | (gv << 8) | b;
    }

    /** Draws the config button — integrated into the title bar, consistent background. */
    private void drawCfgButton(GuiGraphics g, int mx, int my, List<Component> tip)
    {
        ColonyLinkGuiConfig _c = ColonyLinkGuiConfig.get();
        boolean ae = _c.isAe();
        int bx = getCfgBtnX(), by = getCfgBtnY();
        int bw = CFG_BTN_W, bh = CFG_BTN_H;
        boolean hov = mx >= bx && mx <= bx + bw && my >= by && my <= by + bh;

        // Background — AE + sprite present: AE2 nine-slice under the icon; otherwise the
        // current render (neutral face + bevels). The icon is drawn on top in both cases.
        // Control category: face, bevels and icon follow the floored control alpha.
        if (mcButtonReady())
        {
            // MineColonies mini wood button under the gear glyph (no label).
            drawMcButtonAuto(g, bx, by, bw, bh, "", true, hov);
        }
        else if (ae && aeButtonPresent)
        {
            drawAeButtonBg(g, bx, by, bw, bh, hov ? AeBtnVis.HOVER : AeBtnVis.NORMAL, _c.alphaControl());
        }
        else
        {
            int bg = _c.applyControl(ae ? _c.neutralBtnBg(hov) : (hov ? 0xFF505070 : 0xFF404060));
            g.fill(bx, by, bx + bw, by + bh, bg);
            // Thin border consistent with the rest of the GUI
            int _bl = _c.applyControl(ae ? _c.btnBevelLight() : 0xFF8888AA);
            int _bd = _c.applyControl(ae ? _c.btnBevelDark()  : 0xFF222244);
            g.fill(bx, by, bx + bw, by + 1, _bl);
            g.fill(bx, by, bx + 1, by + bh, _bl);
            g.fill(bx, by + bh - 1, bx + bw, by + bh, _bd);
            g.fill(bx + bw - 1, by, bx + bw, by + bh, _bd);
        }

        // "Settings" icon: 3 horizontal lines with a square (≠ the tabs' gear)
        // AE: dark glyph (AE_BODY_TEXT) so it reads on the light nine-slice button bg;
        // DEFAULT colour unchanged. Icon glyph only — the button background is untouched.
        // AE + MineColonies: dark glyph so it reads on the light button bg; DEFAULT unchanged.
        int ic = _c.applyControl((ae || _c.isMineColonies()) ? _c.bodyText() : (hov ? 0xFFDDDDFF : 0xFF9999CC));
        int ox = bx + 3, oy = by + 3;
        // Line 1: ─ ■ ─
        g.fill(ox,     oy,     ox + 4, oy + 1, ic);
        g.fill(ox + 5, oy,     ox + 9, oy + 1, ic);
        g.fill(ox + 4, oy - 1, ox + 6, oy + 2, ic); // square cursor
        // Line 2: ─ ─ ■
        g.fill(ox,     oy + 4, ox + 7, oy + 5, ic);
        g.fill(ox + 7, oy + 3, ox + 9, oy + 6, ic);
        // Line 3: ■ ─ ─
        g.fill(ox + 2, oy + 7, ox + 9, oy + 8, ic);
        g.fill(ox,     oy + 7, ox + 3, oy + 9, ic);

        if (hov)
        {
            tip.clear();
            tip.add(Component.translatable("colonylink.screen.tip.gui_config"));
            tip.add(Component.translatable("colonylink.screen.tip.gui_config_desc"));
        }
    }

    private void drawGearIcon(GuiGraphics g, int ox, int oy, boolean active, boolean hasRedir, int mcCol)
    {
        ColonyLinkGuiConfig _c = ColonyLinkGuiConfig.get();
        boolean ae = _c.isAe();
        int col, hole;
        if (_c.isMineColonies())
        {
            // MineColonies: the glyph colour carries the state (dark normal / blue unread / rust
            // unlinked — chosen by the caller); light centre so it reads on the cream paper tab.
            col  = mcCol;
            hole = 0xFFD8C8A8;
        }
        else
        {
            // Neutral (active / linked) themed; "no redirector" stays orange (SEMANTIC)
            col  = active ? _c.iconNeutral(false) : (hasRedir ? _c.iconNeutral(true) : 0xFFBB7722);
            hole = active ? (ae ? 0xFF413F54 : 0xFF8B8B8B)
                          : (hasRedir ? (ae ? 0xFF2B2A38 : 0xFF4A4A4A) : 0xFF5A3A10);
        }
        // Control category: gear icon follows the floored control alpha.
        col = _c.applyControl(col); hole = _c.applyControl(hole);
        g.fill(ox + 3, oy + 1, ox + 7, oy + 9, col);
        g.fill(ox + 1, oy + 3, ox + 9, oy + 7, col);
        g.fill(ox + 4, oy,     ox + 6, oy + 2,  col);
        g.fill(ox + 4, oy + 8, ox + 6, oy + 10, col);
        g.fill(ox,     oy + 4, ox + 2, oy + 6,  col);
        g.fill(ox + 8, oy + 4, ox + 10, oy + 6, col);
        g.fill(ox + 4, oy + 4, ox + 6, oy + 6, hole);
    }

    // Info panel
    private void drawInfoPanel(GuiGraphics g, int x, int y, int mx, int my)
    {
        ColonyLinkGuiConfig _c = ColonyLinkGuiConfig.get();
        int panelH = 58;
        // MineColonies: no opaque well — text sits directly on the parchment.
        if (!_c.isMineColonies())
        {
            g.fill(x + 6, y + 22, x + GUI_WIDTH - 6, y + 22 + panelH, _c.applyOpacity(_c.wellBg()));
            g.fill(x + 6, y + 22, x + GUI_WIDTH - 6, y + 23, _c.applyOpacity(_c.wellLight()));
            g.fill(x + 6, y + 22, x + 7, y + 22 + panelH, _c.applyOpacity(_c.wellLight()));
            g.fill(x + 6, y + 22 + panelH - 1, x + GUI_WIDTH - 6, y + 22 + panelH, _c.applyOpacity(_c.wellDark()));
            g.fill(x + GUI_WIDTH - 7, y + 22, x + GUI_WIDTH - 6, y + 22 + panelH, _c.applyOpacity(_c.wellDark()));
        }

        if (!isOutOfPower())
        {
            drawInfoLine(g, x + 10, y + 26, Component.translatable("colonylink.screen.info.builder", builderName).getString());

            // Locate button — right of the Builder line, hidden on the Citizens/Stats tabs
            if (isBuilderTab())
            {
                int lbX = x + GUI_WIDTH - 6 - LOCATE_BTN_W - 4;
                int lbY = y + 22;
                boolean lHov = mx >= lbX && mx <= lbX + LOCATE_BTN_W
                        && my >= lbY && my <= lbY + LOCATE_BTN_H;
                drawButton(g, lbX, lbY, LOCATE_BTN_W, LOCATE_BTN_H,
                        lHov ? 0xFF1A5C2E : 0xFF0F3A1E, Component.translatable("colonylink.screen.btn.locate").getString(), 0xFF44DD88,
                        lHov, true, _c.semGreen());
            }
            drawInfoLine(g, x + 10, y + 36, Component.translatable("colonylink.screen.info.building", buildingName).getString());

            String sl = Component.translatable("colonylink.screen.info.status").getString();
            drawInfoLine(g, x + 10, y + 46, sl);
            g.drawString(this.font, translateStatus(workerStatus),
                    x + 10 + this.font.width(sl), y + 46, getWorkerStatusColor(), false);

            // v1.1.3 — IDLE reason below the status
            if (!workerIdleReason.isEmpty())
            {
                // Multiple reasons (separated by " | ") are shown on one condensed line
                String reasonDisplay = workerIdleReason.length() > 40
                        ? workerIdleReason.substring(0, 38) + "…"
                        : workerIdleReason;
                g.drawString(this.font, reasonDisplay, x + 10, y + 56, _c.bodyText(), false);
            }

            int cpuY = workerIdleReason.isEmpty() ? 58 : 66;
            drawInfoLine(g, x + 10, y + cpuY, Component.translatable("colonylink.screen.info.cpus", availableCpus).getString());

            boolean _ae = _c.isLightBody();
            int rColor = switch (redirectorState) {
                case "LINKED"     -> _ae ? _c.semGreen() : 0x00FF00;
                case "STANDBY"    -> _ae ? _c.semAmber() : 0xFF8800;
                case "NOT_LINKED" -> _ae ? _c.semGray()  : 0xAAAAAA;
                default           -> _ae ? _c.semGray()  : 0x888888;
            };
            String rDisplay = switch (redirectorState) {
                case "LINKED"     -> Component.translatable("colonylink.screen.redir.linked").getString();
                case "STANDBY"    -> Component.translatable("colonylink.screen.redir.standby").getString();
                case "NOT_LINKED" -> Component.translatable("colonylink.screen.redir.not_linked").getString();
                default           -> redirectorState;
            };
            String rl = Component.translatable("colonylink.screen.info.redirector").getString();
            drawInfoLine(g, x + 100, y + cpuY, rl);
            g.drawString(this.font, rDisplay, x + 100 + this.font.width(rl), y + cpuY, rColor, false);
        }
        else
        {
            // Out of Power
            int cx = x + GUI_WIDTH / 2;
            boolean _aeP = _c.isLightBody();
            g.drawCenteredString(this.font, Component.translatable("colonylink.screen.power.title").getString(),          cx, y + 30, _aeP ? _c.semRed() : 0xFF4444);
            g.drawCenteredString(this.font, Component.translatable("colonylink.screen.power.charge").getString(), cx, y + 42, _aeP ? _c.mutedText() : 0xAAAAAA);
            g.drawCenteredString(this.font, Component.translatable("colonylink.screen.power.mods").getString(),  cx, y + 52, _aeP ? _c.mutedText() : 0xAAAAAA);
        }
    }

    // Request panel
    private void drawRequestPanel(GuiGraphics g, int x, int y, int mx, int my,
                                  List<Component> pendingTooltipOut)
    {
        ColonyLinkGuiConfig _cr = ColonyLinkGuiConfig.get();
        int pY = y + 80, pH = 30;
        // MineColonies: no opaque request well — content sits on the parchment.
        if (!_cr.isMineColonies())
        {
            g.fill(x + 6, pY, x + GUI_WIDTH - 6, pY + pH, _cr.applyOpacity(_cr.reqBg()));
            g.fill(x + 6, pY, x + GUI_WIDTH - 6, pY + 1, _cr.applyOpacity(_cr.reqLight()));
            g.fill(x + 6, pY, x + 7, pY + pH, _cr.applyOpacity(_cr.reqLight()));
            g.fill(x + 6, pY + pH - 1, x + GUI_WIDTH - 6, pY + pH, _cr.applyOpacity(_cr.reqDark()));
            g.fill(x + GUI_WIDTH - 7, pY, x + GUI_WIDTH - 6, pY + pH, _cr.applyOpacity(_cr.reqDark()));
        }
        g.fill(x + 7, pY + 11, x + GUI_WIDTH - 7, pY + 12, _cr.applyOpacity(_cr.isAe() ? 0xFF878FA5 : 0xFF3A3A6A));
        g.drawString(this.font, Component.translatable("colonylink.screen.req.title").getString(), x + 10, pY + 3, _cr.isAe() ? _cr.semBlue() : 0xAAAAFF, false);

        boolean hasReq = builderRequest != null && !builderRequest.stack().isEmpty()
                && builderRequest.count() > 0;

        if (!hasReq || isOutOfPower())
        {
            g.drawString(this.font, isOutOfPower() ? Component.translatable("colonylink.screen.req.no_power").getString() : Component.translatable("colonylink.screen.req.none").getString(),
                    x + 10, pY + 14, _cr.isAe() ? _cr.mutedText() : 0x666666, false);
            return;
        }

        g.renderItem(builderRequest.stack(), x + 10, pY + 12);
        g.drawString(this.font, builderRequest.count() + "x "
                + builderRequest.stack().getDisplayName().getString(), x + 28, pY + 17, _cr.bodyText(), false);

        int rbX = getReqBtnX(), rbY = getReqBtnY(), rbW = getReqBtnW(), rbH = getReqBtnH();
        ResourceStatus rs = displayStatus(builderRequest.status(), builderRequest.stack());
        boolean hov = mx >= rbX && mx <= rbX + rbW && my >= rbY && my <= rbY + rbH;
        if (mcButtonReady())
        {
            drawMcButtonAuto(g, rbX, rbY, rbW, rbH, getRequestButtonText(rs), isButtonClickable(rs), hov);
        }
        else if (_cr.isAe() && aeButtonPresent)
        {
            drawAeButton(g, rbX, rbY, rbW, rbH, hov, isButtonClickable(rs), getRequestButtonText(rs), aeStatusTextColor(rs));
        }
        else
        {
            int bg = _cr.applyControl(hov && isButtonClickable(rs) ? getButtonHoverColor(rs) : getButtonColor(rs));
            g.fill(rbX, rbY, rbX + rbW, rbY + rbH, bg);
            g.fill(rbX, rbY, rbX + rbW, rbY + 1, _cr.applyControl(_cr.btnBevelLight()));
            g.fill(rbX, rbY, rbX + 1, rbY + rbH, _cr.applyControl(_cr.btnBevelLight()));
            g.fill(rbX, rbY + rbH - 1, rbX + rbW, rbY + rbH, _cr.applyControl(_cr.btnBevelDark()));
            g.fill(rbX + rbW - 1, rbY, rbX + rbW, rbY + rbH, _cr.applyControl(_cr.btnBevelDark()));
            g.drawCenteredString(this.font, getRequestButtonText(rs), rbX + rbW / 2, rbY + 4, getButtonTextColor(rs));
        }

        // Tooltip on button/row hover — shows substitution info if present
        boolean lineHov = mx >= x + 10 && mx <= rbX - 2 && my >= pY + 10 && my <= pY + 30;
        if ((hov || lineHov) && rs == ResourceStatus.SENT_PENDING
                && builderRequest.status() != ResourceStatus.SENT_PENDING)
        {
            // v1.6.0 — client-side pending overlay (see displayStatus).
            pendingTooltipOut.clear();
            pendingTooltipOut.add(Component.translatable("colonylink.screen.tip.sent_pending"));
        }
        else if ((hov || lineHov) && !builderRequest.tooltipLines().isEmpty())
        {
            pendingTooltipOut.clear();
            for (Component line : builderRequest.tooltipLines())
                pendingTooltipOut.add(line);
        }

        // v1.6.4 — Cancel Request button (only when a priority request exists).
        // Drawn last so it sits on top; its tooltip wins over the line/button ones.
        // v1.6.6 — greyed out (non-interactive, no hover, no tooltip) when the line
        // is a pass-2 needed-resource with no formal request to cancel.
        int cbX = getCancelBtnX(), cbY = getCancelBtnY(), cbW = getCancelBtnW(), cbH = getCancelBtnH();
        boolean cCancellable = builderRequest.cancellable();
        boolean cHov = cCancellable && mx >= cbX && mx <= cbX + cbW && my >= cbY && my <= cbY + cbH;
        if (mcButtonReady())
        {
            // Mini wood button + our "×" (black / hover pale-yellow / disabled grey).
            drawMcButtonAuto(g, cbX, cbY, cbW, cbH, "×", cCancellable, cHov);
        }
        else if (_cr.isAe() && aeButtonPresent)
        {
            drawAeButtonBg(g, cbX, cbY, cbW, cbH,
                    cCancellable ? aeBtnVis(true, cHov) : AeBtnVis.DISABLED, _cr.alphaControl());
            drawCenteredNoShadow(g, "×", cbX + cbW / 2, cbY + 1, cCancellable ? _cr.semRed() : _cr.bodyText());
        }
        else
        {
            int cFill = cCancellable ? (cHov ? 0xFFCC4444 : 0xFF992222) : 0xFF555555;
            g.fill(cbX, cbY, cbX + cbW, cbY + cbH, _cr.applyControl(cFill));
            g.fill(cbX, cbY, cbX + cbW, cbY + 1, _cr.btnBevelLight());
            g.fill(cbX, cbY, cbX + 1, cbY + cbH, _cr.btnBevelLight());
            g.fill(cbX, cbY + cbH - 1, cbX + cbW, cbY + cbH, _cr.btnBevelDark());
            g.fill(cbX + cbW - 1, cbY, cbX + cbW, cbY + cbH, _cr.btnBevelDark());
            g.drawCenteredString(this.font, "×", cbX + cbW / 2, cbY + 1, cCancellable ? 0xFFFFFFFF : 0xFF999999);
        }
        if (cHov)
        {
            pendingTooltipOut.clear();
            pendingTooltipOut.add(Component.translatable("colonylink.priority.cancel_tooltip"));
        }
    }

    // AE button rendering (layer 4)
    // AeBtnVis now lives in AeGuiBlits (imported) — call-sites unchanged.

    private static AeBtnVis aeBtnVis(boolean enabled, boolean hovered)
    { return !enabled ? AeBtnVis.DISABLED : (hovered ? AeBtnVis.HOVER : AeBtnVis.NORMAL); }

    /**
     * AE button background — neutral nine-slice sprite (button / _highlighted /
     * _disabled) stretched to any w/h via g.blitSprite (honours the .mcmeta nine-slice).
     * Honours the control alpha; blend + tint are reset on exit so nothing later is
     * tinted. Background only — the caller draws the label on top.
     */
    private void drawAeButtonBg(GuiGraphics g, int x, int y, int w, int h, AeBtnVis vis, float alpha)
    {
        // UI pass 1: body moved verbatim to AeGuiBlits — facade kept so call-sites
        // are untouched (zero visual change).
        AeGuiBlits.drawAeButtonBg(g, x, y, w, h, vis, alpha);
    }

    /**
     * AE full button: neutral nine-slice background + centered label. The label keeps
     * the semantic sense color (AE_SEM_*); a disabled button uses AE_BODY_TEXT — the
     * same dark text AE2Button itself pairs with button_disabled, so it stays legible.
     * Layer-4 control → alphaControl() (floored); the label stays opaque.
     * Label y = (h-8)/2 reproduces the legacy by+3 (h=14) / by+4 (h=16) baseline.
     */
    /**
     * Centered label WITHOUT the drop shadow — AE buttons/toggle draw flat labels
     * (AE2 does the same; the shadow looks wrong on the light body). Reproduces
     * drawCenteredString's placement exactly: same center x (cx - width/2, same integer
     * division) and same y baseline, only the shadow flag flips to false. AE only —
     * DEFAULT keeps drawCenteredString (shadow) untouched.
     */
    private void drawCenteredNoShadow(GuiGraphics g, String text, int cx, int y, int color)
    {
        g.drawString(this.font, text, cx - this.font.width(text) / 2, y, color, false);
    }

    private void drawAeButton(GuiGraphics g, int x, int y, int w, int h,
                              boolean hovered, boolean enabled, String label, int aeLabelColor)
    {
        ColonyLinkGuiConfig c = ColonyLinkGuiConfig.get();
        drawAeButtonBg(g, x, y, w, h, aeBtnVis(enabled, hovered), c.alphaControl());
        int col = enabled ? aeLabelColor : c.bodyText(); // disabled = AE_BODY_TEXT (matches AE2Button)
        drawCenteredNoShadow(g, label, x + w / 2, y + (h - 8) / 2, col);
    }

    // MineColonies textured button (cousin of drawAeButton, AE code untouched)

    /** True when the MineColonies theme is active AND its button textures are available. */
    private boolean mcButtonReady()
    {
        ColonyLinkGuiConfig c = ColonyLinkGuiConfig.get();
        return c.isMineColonies() && mcPaperPresent && mcButtonPresent;
    }

    /**
     * MineColonies button: the mapped texture (enabled/disabled) blitted STRETCHED to the
     * existing button rect via blitTintedStretched. {@code srcW/srcH} are the texture's NATIVE
     * size (needed so the whole texture maps 0..src → 0..dest). Hover DARKENS the wood texture
     * (MineColonies has no hover sprite); the label is drawn black (no shadow), grey when
     * disabled, always opaque. Sense colour is intentionally dropped (the text says it).
     */
    private void drawMcButton(GuiGraphics g, int x, int y, int w, int h,
                              ResourceLocation tex, ResourceLocation texDisabled, int srcW, int srcH,
                              String label, boolean enabled, boolean hovered, float alpha)
    {
        // Hover = darken the enabled wood texture (~18%); label stays black. Disabled uses its
        // own _disabled texture at full brightness + grey label.
        float bright = (enabled && hovered) ? MC_HOVER_DARKEN : 1f;
        blitTintedStretched(g, enabled ? tex : texDisabled, x, y, w, h,
                0f, 0f, srcW, srcH, srcW, srcH, bright, alpha);
        if (label != null && !label.isEmpty())
        {
            int col = enabled ? MC_LABEL : MC_LABEL_DISABLED;
            drawCenteredNoShadow(g, label, x + w / 2, y + (h - 8) / 2, col);
        }
    }

    /**
     * Convenience: pick the best-fit MineColonies builder-button texture by width (with its
     * native size for correct UV) and draw at the floored control alpha. Buckets reproduce the
     * validated mapping (≥100→medium_large, ≥50→small, ≥30→quite_small, else mini).
     */
    private void drawMcButtonAuto(GuiGraphics g, int x, int y, int w, int h,
                                  String label, boolean enabled, boolean hovered)
    {
        float a = ColonyLinkGuiConfig.get().alphaControl();
        ResourceLocation te, td; int sw, sh;
        if (w >= 100)     { te = MC_BTN_ML;   td = MC_BTN_ML_D;   sw = 129; sh = 17; } // Check WH / Craft All / Send All
        else if (w >= 50) { te = MC_BTN_S;    td = MC_BTN_S_D;    sw = 64;  sh = 17; } // Req / Restart
        else if (w >= 30) { te = MC_BTN_QS;   td = MC_BTN_QS_D;   sw = 44;  sh = 16; } // Unlink / Locate / line buttons
        else              { te = MC_BTN_MINI; td = MC_BTN_MINI_D; sw = 14;  sh = 15; } // config gear / cancel ×
        drawMcButton(g, x, y, w, h, te, td, sw, sh, label, enabled, hovered, a);
    }

    /**
     * Draws a builder-info line ("§7Label: §fValue"). AE/DEFAULT keep the original colour codes
     * (grey label / white value read fine on their dark bodies). MineColonies STRIPS the codes —
     * they'd force an unreadable grey/white on the cream parchment — and draws the label black
     * (bodyText) + the value in the dark muted tint. Lines with no "§f" (label-only, e.g.
     * "§7Status: " whose value is drawn separately in a semantic colour) are drawn black.
     */
    private void drawInfoLine(GuiGraphics g, int x, int y, String full)
    {
        ColonyLinkGuiConfig c = ColonyLinkGuiConfig.get();
        if (!c.isMineColonies())
        {
            g.drawString(this.font, full, x, y, c.bodyText(), false);
            return;
        }
        int sep = full.indexOf("§f");
        if (sep < 0)
        {
            g.drawString(this.font, full.replaceAll("§.", ""), x, y, c.bodyText(), false);
            return;
        }
        String label = full.substring(0, sep).replaceAll("§.", "");
        String value = full.substring(sep + 2).replaceAll("§.", "");
        g.drawString(this.font, label, x, y, c.bodyText(), false);
        g.drawString(this.font, value, x + this.font.width(label), y, c.mutedText(), false);
    }

    /** AE-mode label color for a resource-request status (semantic tints). */
    private int aeStatusTextColor(ResourceStatus s)
    {
        ColonyLinkGuiConfig c = ColonyLinkGuiConfig.get();
        return switch (s) {
            case AVAILABLE    -> c.semBlue();
            case CRAFTABLE    -> c.semGreen();
            case NO_PATTERN   -> c.semRed();
            case CRAFTING     -> c.semAmber();
            case MISSING      -> c.semAmber();
            case SENT_PENDING -> c.semGray();
        };
    }

    private void drawButton(GuiGraphics g, int bx, int by, int bw, int bh,
                            int bg, String label, int tc,
                            boolean hovered, boolean enabled, int aeLabel)
    {
        ColonyLinkGuiConfig _c = ColonyLinkGuiConfig.get();
        // MineColonies theme + textures present: mapped wood button, black label.
        if (mcButtonReady())
        {
            drawMcButtonAuto(g, bx, by, bw, bh, label, enabled, hovered);
            return;
        }
        // AE theme + sprite present: neutral nine-slice bg, sense carried by the label.
        if (_c.isAe() && aeButtonPresent)
        {
            drawAeButton(g, bx, by, bw, bh, hovered, enabled, label, aeLabel);
            return;
        }
        // DEFAULT / sprite absent — unchanged: the face (bg) carries the sense;
        // only the bevel (chrome) follows the theme.
        // Control category: face + bevels follow the floored control alpha; label stays opaque.
        g.fill(bx, by, bx + bw, by + bh, _c.applyControl(bg));
        g.fill(bx, by, bx + bw, by + 1, _c.applyControl(_c.btnBevelLight()));
        g.fill(bx, by, bx + 1, by + bh, _c.applyControl(_c.btnBevelLight()));
        g.fill(bx, by + bh - 1, bx + bw, by + bh, _c.applyControl(_c.btnBevelDark()));
        g.fill(bx + bw - 1, by, bx + bw, by + bh, _c.applyControl(_c.btnBevelDark()));
        g.drawCenteredString(this.font, label, bx + bw / 2, by + 3, tc);
    }

    private void drawPrioritySwitch(GuiGraphics g, int mx, int my)
    {
        if (!hasWarehouseCard || isOutOfPower()) return;
        int sw = 110, sh = 14, sx = getGuiX() + GUI_WIDTH - sw - 8, sy = getWareCheckBtnY();
        // Control category: the whole toggle follows the floored control alpha; labels stay opaque.
        ColonyLinkGuiConfig _cSw = ColonyLinkGuiConfig.get();
        int half = sw / 2;

        // MineColonies theme: procedural two-half toggle recoloured for the parchment (no texture
        // composite — a stretched modular sprite would be fragile for little gain). Active half =
        // raised lighter beige + coloured accent bar (green WH / blue AE2) + accent label; inactive
        // half = neutral beige + muted label; divider/border brown. Geometry/hitbox unchanged.
        if (_cSw.isMineColonies())
        {
            g.fill(sx, sy, sx + sw, sy + sh, _cSw.applyControl(MC_TOGGLE_BG));
            g.fill(sx, sy, sx + sw, sy + 1, _cSw.applyControl(MC_TOGGLE_LIGHT));
            g.fill(sx, sy, sx + 1, sy + sh, _cSw.applyControl(MC_TOGGLE_LIGHT));
            g.fill(sx, sy + sh - 1, sx + sw, sy + sh, _cSw.applyControl(MC_PAPER_BORDER));
            g.fill(sx + sw - 1, sy, sx + sw, sy + sh, _cSw.applyControl(MC_PAPER_BORDER));
            if (warehousePriority)
            {
                g.fill(sx + 1, sy + 1, sx + half, sy + sh - 1, _cSw.applyControl(MC_TOGGLE_ACTIVE));   // WH active (raised)
                g.fill(sx + 3, sy + 3, sx + 9,    sy + sh - 3, _cSw.applyControl(_cSw.semGreen()));     // WH accent bar
            }
            else
            {
                g.fill(sx + half, sy + 1, sx + sw - 1, sy + sh - 1, _cSw.applyControl(MC_TOGGLE_ACTIVE)); // AE2 active (raised)
                g.fill(sx + sw - 9, sy + 3, sx + sw - 3, sy + sh - 3, _cSw.applyControl(_cSw.semBlue())); // AE2 accent bar
            }
            g.fill(sx + half, sy + 2, sx + half + 1, sy + sh - 2, _cSw.applyControl(MC_PAPER_BORDER));    // divider
            int whCol  = warehousePriority ? _cSw.semGreen() : _cSw.mutedText();
            int ae2Col = warehousePriority ? _cSw.mutedText() : _cSw.semBlue();
            drawCenteredNoShadow(g, Component.translatable("colonylink.screen.toggle.wh").getString(), sx + half / 2,        sy + 3, whCol);
            drawCenteredNoShadow(g, "AE2",                                                             sx + half + half / 2, sy + 3, ae2Col);
            return;
        }

        // AE theme: same procedural two-half toggle, recoloured for the light body.
        // The active half is bright (raised), the inactive half a recessed grey, plus a
        // semantic accent bar (green WH / blue AE2) so the active side reads at a glance.
        // Labels dark, no shadow. (The stretched tab-sprite approach was dropped: at
        // 22x22 → 55x14 it was unreadable.) DEFAULT falls through to the legacy toggle.
        if (_cSw.isAe())
        {
            g.fill(sx, sy, sx + sw, sy + sh, _cSw.applyControl(0xFFDDDEE3));
            g.fill(sx, sy, sx + sw, sy + 1, _cSw.applyControl(0xFFF2F2F2));
            g.fill(sx, sy, sx + 1, sy + sh, _cSw.applyControl(0xFFF2F2F2));
            g.fill(sx, sy + sh - 1, sx + sw, sy + sh, _cSw.applyControl(0xFF878FA5));
            g.fill(sx + sw - 1, sy, sx + sw, sy + sh, _cSw.applyControl(0xFF878FA5));
            if (warehousePriority)
            {
                g.fill(sx + 1,    sy + 1, sx + half,   sy + sh - 1, _cSw.applyControl(0xFFF2F2F2)); // WH active (raised)
                g.fill(sx + half, sy + 1, sx + sw - 1, sy + sh - 1, _cSw.applyControl(0xFFC5C7D0)); // AE2 inactive (recessed)
                g.fill(sx + 3, sy + 3, sx + 9, sy + sh - 3, _cSw.applyControl(_cSw.semGreen()));    // WH accent
            }
            else
            {
                g.fill(sx + 1,    sy + 1, sx + half,   sy + sh - 1, _cSw.applyControl(0xFFC5C7D0)); // WH inactive (recessed)
                g.fill(sx + half, sy + 1, sx + sw - 1, sy + sh - 1, _cSw.applyControl(0xFFF2F2F2)); // AE2 active (raised)
                g.fill(sx + sw - 9, sy + 3, sx + sw - 3, sy + sh - 3, _cSw.applyControl(_cSw.semBlue())); // AE2 accent
            }
            g.fill(sx + half, sy + 2, sx + half + 1, sy + sh - 2, _cSw.applyControl(0xFF878FA5)); // divider
            drawCenteredNoShadow(g, Component.translatable("colonylink.screen.toggle.wh").getString(), sx + half / 2,        sy + 3, _cSw.bodyText());
            drawCenteredNoShadow(g, "AE2",                                                             sx + half + half / 2, sy + 3, _cSw.bodyText());
            return;
        }

        // DEFAULT — legacy toggle, strictly unchanged.
        g.fill(sx, sy, sx + sw, sy + sh, _cSw.applyControl(0xFF2A2A2A));
        g.fill(sx, sy, sx + sw, sy + 1, _cSw.applyControl(0xFF555555));
        g.fill(sx, sy, sx + 1, sy + sh, _cSw.applyControl(0xFF555555));
        g.fill(sx, sy + sh - 1, sx + sw, sy + sh, _cSw.applyControl(0xFF111111));
        g.fill(sx + sw - 1, sy, sx + sw, sy + sh, _cSw.applyControl(0xFF111111));
        if (warehousePriority)
        {
            g.fill(sx + 1, sy + 1, sx + half, sy + sh - 1, _cSw.applyControl(0xFF224422));
            g.fill(sx + 3, sy + 3, sx + 9, sy + sh - 3, _cSw.applyControl(0xFF00FF88));
        }
        else
        {
            g.fill(sx + half, sy + 1, sx + sw - 1, sy + sh - 1, _cSw.applyControl(0xFF112244));
            g.fill(sx + sw - 9, sy + 3, sx + sw - 3, sy + sh - 3, _cSw.applyControl(0xFF4488FF));
        }
        g.fill(sx + half, sy + 2, sx + half + 1, sy + sh - 2, _cSw.applyControl(0xFF444444));
        String networkLabel = "AE2";
        g.drawCenteredString(this.font, Component.translatable("colonylink.screen.toggle.wh").getString(),          sx + half / 2,        sy + 3, warehousePriority ? 0x00FF88 : 0x556655);
        g.drawCenteredString(this.font, networkLabel,  sx + half + half / 2, sy + 3, warehousePriority ? 0x334466 : 0x4488FF);
    }

    private void drawWareCheckButton(GuiGraphics g, int mx, int my)
    {
        if (!hasWarehouseCard || isOutOfPower()) return;
        int bx = getWareCheckBtnX(), by = getWareCheckBtnY(), bw = getWareCheckBtnW(), bh = getWareCheckBtnH();
        boolean hov = mx >= bx && mx <= bx + bw && my >= by && my <= by + bh;
        ColonyLinkGuiConfig _cw = ColonyLinkGuiConfig.get();
        String label; int bg, tc, aeLabel;
        switch (wareCheckState)
        {
            case LOADING -> { label = Component.translatable("colonylink.screen.btn.scanning").getString(); bg = 0xFF554400; tc = 0xFFAA44; aeLabel = _cw.semAmber(); }
            case DONE ->
            {
                boolean exp = System.currentTimeMillis() - warehouseSnapshotReceivedMs > getSnapshotValidityMs();
                if (exp) { wareCheckState = WareCheckState.IDLE; warehouseSnapshot = null; }
                label = exp ? Component.translatable("colonylink.screen.btn.check_warehouse").getString() : Component.translatable("colonylink.screen.btn.warehouse_ok").getString();
                bg = exp ? (hov ? 0xFF336633 : 0xFF224422) : (hov ? 0xFF447744 : 0xFF335533);
                tc = exp ? 0x88FF88 : 0x00FF88;
                aeLabel = _cw.semGreen();
            }
            default -> { label = Component.translatable("colonylink.screen.btn.check_warehouse").getString(); bg = hov ? 0xFF336633 : 0xFF224422; tc = 0x88FF88; aeLabel = _cw.semGreen(); }
        }
        // Control category: face + bevels follow the floored control alpha; label stays opaque.
        if (mcButtonReady())
        {
            drawMcButtonAuto(g, bx, by, bw, bh, label, true, hov);
        }
        else if (_cw.isAe() && aeButtonPresent)
        {
            drawAeButton(g, bx, by, bw, bh, hov, true, label, aeLabel);
        }
        else
        {
            g.fill(bx, by, bx + bw, by + bh, _cw.applyControl(bg));
            g.fill(bx, by, bx + bw, by + 1, _cw.applyControl(_cw.btnBevelLight()));
            g.fill(bx, by, bx + 1, by + bh, _cw.applyControl(_cw.btnBevelLight()));
            g.fill(bx, by + bh - 1, bx + bw, by + bh, _cw.applyControl(_cw.btnBevelDark()));
            g.fill(bx + bw - 1, by, bx + bw, by + bh, _cw.applyControl(_cw.btnBevelDark()));
            g.drawCenteredString(this.font, label, bx + bw / 2, by + 3, tc);
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {}

    /**
     * Thin AE frame — pixel-perfect replica of the Warehouse Link Terminal's outer
     * border (sequences from TerminalSkin.BG, outer → inner). Thickness: top/left/right
     * = 2px, bottom = 4px. Each step is a 1px g.fill rule with the global opacity applied.
     * The vertical edges (left/right) are drawn last → outer corner pixel = 0xFF413F54
     * (dark), sharp corners (not the skin's alpha anti-aliasing — known limitation).
     * AE mode only; DEFAULT continues via {@link ColonyLinkGuiConfig#drawBorders}.
     */
    private void drawAeFrame(GuiGraphics g, ColonyLinkGuiConfig cfg, int x, int y, int w, int h)
    {
        int[] top = cfg.aeFrameTop(), bot = cfg.aeFrameBottom();
        int[] left = cfg.aeFrameLeft(), right = cfg.aeFrameRight();

        // Top (outer = topmost line)
        for (int k = 0; k < top.length; k++)
            g.fill(x, y + k, x + w, y + k + 1, cfg.applyOpacity(top[k]));
        // Bottom — outer = very bottom line. bot[0] goes on y+h-1 (last line), then
        // moves inward: k=0→413F54(bottom), 1→878FA5, 2→878FA5, 3→F2F2F2(band top).
        // Bottom row = dark, matching the Terminal.
        for (int k = 0; k < bot.length; k++)
        {
            int ry = y + h - 1 - k;
            g.fill(x, ry, x + w, ry + 1, cfg.applyOpacity(bot[k]));
        }
        // Left (outer = left column) — drawn after top/bottom
        for (int k = 0; k < left.length; k++)
            g.fill(x + k, y, x + k + 1, y + h, cfg.applyOpacity(left[k]));
        // Right (outer = right column)
        for (int k = 0; k < right.length; k++)
        {
            int rx = x + w - 1 - k;
            g.fill(rx, y, rx + 1, y + h, cfg.applyOpacity(right[k]));
        }
    }

    /**
     * Layer 1 — AE2 frame drawn by blitting AE2's own background.png as a nine-slice,
     * re-implemented here with GuiGraphics (AE2's BackgroundGenerator/Blitter are
     * internal, unpublished API and are NOT called). BORDER=4px corners at native
     * size; edges tiled 1:1 in {@code AE_BG_TILE}-wide chunks (no scaling, so a
     * non-integer GUI scale magnifies with nearest-neighbour, not blur).
     *
     * When {@code withCenter} is true (layer 2) the tiled texture CENTRE is also
     * painted, filling the inner region with AE2's light terminal body — a drop-in
     * replacement for the dark {@code g.fill(bg())}, drawn here (before any content)
     * so the light body sits underneath the panels/text/buttons. When false, only
     * the border RING is drawn (layer 1), leaving the body untouched.
     *
     * {@code alpha} is the effective opacity (config opacity multiplied over the
     * texture's own alpha). The tint is reset to opaque on every exit path.
     */
    private static void drawAeNineSlice(GuiGraphics g, int x, int y, int w, int h, float alpha, boolean withCenter)
    {
        // UI pass 1: body moved verbatim to AeGuiBlits — facade kept so call-sites
        // are untouched (zero visual change).
        AeGuiBlits.drawAeNineSlice(g, x, y, w, h, alpha, withCenter);
    }

    /**
     * Tinted texture blit that actually honours alpha. GuiGraphics' raw-ResourceLocation
     * blit routes to a no-blend, no-colour innerBlit (POSITION_TEX shader), so setColor's
     * alpha is written but never composited — the blit stays opaque. We reproduce MC's own
     * tinted-blit discipline (enableBlend -> shader colour -> blit -> disableBlend, cf. the
     * per-vertex-colour GuiGraphics.innerBlit) around the raw blit. Blend + shader colour are
     * reset on exit so no later render is tinted. Reusable by AE theme layers 3-5 (tabs and
     * buttons as textures).
     */
    private static void blitTinted(GuiGraphics g, ResourceLocation tex, int x, int y,
                                   int u, int v, int w, int h, int texW, int texH, float alpha)
    {
        // UI pass 1: body moved verbatim to AeGuiBlits — facade kept (zero visual change).
        AeGuiBlits.blitTinted(g, tex, x, y, u, v, w, h, texW, texH, alpha);
    }

    /**
     * Like {@link #blitTinted}, but STRETCHES a source region (sw×sh at u,v within a
     * texW×texH texture) to the destination rect (w×h). Used to blit the MineColonies
     * parchment (190×244) over the Clipboard body (276×320). Honours opacity/blend.
     */
    private static void blitTintedStretched(GuiGraphics g, ResourceLocation tex, int x, int y,
                                            int w, int h, float u, float v, int sw, int sh,
                                            int texW, int texH, float alpha)
    {
        // UI pass 1: facade — body in AeGuiBlits (zero visual change).
        AeGuiBlits.blitTintedStretched(g, tex, x, y, w, h, u, v, sw, sh, texW, texH, alpha);
    }

    /**
     * As above, but multiplies the texture by a grey {@code bright} factor (1.0 = unchanged,
     * &lt;1.0 = darker). Used for the MineColonies button hover (no hover sprite exists, so we
     * darken the wood texture instead). Blend + tint are reset on all paths.
     */
    private static void blitTintedStretched(GuiGraphics g, ResourceLocation tex, int x, int y,
                                            int w, int h, float u, float v, int sw, int sh,
                                            int texW, int texH, float bright, float alpha)
    {
        // UI pass 1: body moved verbatim to AeGuiBlits — facade kept (zero visual change).
        AeGuiBlits.blitTintedStretched(g, tex, x, y, w, h, u, v, sw, sh, texW, texH, bright, alpha);
    }

    // render()
    @Override
    public void render(GuiGraphics g, int rawMx, int rawMy, float pt)
    {
        // Scale config
        float _scale = ColonyLinkGuiConfig.get().scale;
        if (_scale != 1.0f)
        {
            float cx = this.width / 2f, cy = this.height / 2f;
            g.pose().pushPose();
            g.pose().translate(cx, cy, 0);
            g.pose().scale(_scale, _scale, 1f);
            g.pose().translate(-cx, -cy, 0);
        }
        // Transform mx/my into scaled-GUI space for hover effects
        int mx = toGuiX(rawMx), my = toGuiY(rawMy);
        int x = getGuiX(), y = getGuiY();

        // Colours from ColonyLinkGuiConfig
        ColonyLinkGuiConfig _cfg = ColonyLinkGuiConfig.get();

        // Background + frame per theme. DEFAULT and AE unchanged (same branches).
        // MINECOLONIES (layer 1): full-body blitted parchment, else beige fallback.
        boolean _aeTex = _cfg.isAe() && aeBackgroundPresent;
        boolean _mcTex = _cfg.isMineColonies() && mcPaperPresent;
        if (_aeTex)
        {
            // AE: the nine-slice paints body + frame in one pass (no prior fill).
            drawAeNineSlice(g, x, y, GUI_WIDTH, GUI_HEIGHT, _cfg.alphaBackground(), true);
        }
        else if (_mcTex)
        {
            // MineColonies: parchment blitted OVERSIZED. We overflow its torn inner margin
            // outward so the flat cream area aligns with the body (x, y, GUI_WIDTH, GUI_HEIGHT)
            // and the torn edges spill outside the frame.
            // Screen margin = texture margin × stretch ratio (body / texture).
            float _rx = (float) GUI_WIDTH  / MC_PAPER_TEX_W;   // ≈ 1.453
            float _ry = (float) GUI_HEIGHT / MC_PAPER_TEX_H;   // ≈ 1.311
            int _ml = Math.round(MC_PAPER_MARGIN_L * _rx);
            int _mr = Math.round(MC_PAPER_MARGIN_R * _rx);
            int _mt = Math.round(MC_PAPER_MARGIN_T * _ry);
            int _mb = Math.round(MC_PAPER_MARGIN_B * _ry);
            blitTintedStretched(g, MC_PAPER, x - _ml, y - _mt,
                    GUI_WIDTH + _ml + _mr, GUI_HEIGHT + _mt + _mb,
                    0f, 0f, MC_PAPER_TEX_W, MC_PAPER_TEX_H, MC_PAPER_TEX_W, MC_PAPER_TEX_H,
                    _cfg.alphaBackground());
        }
        else if (_cfg.isMineColonies())
        {
            // Procedural fallback (MC texture absent — never seen when MC is present):
            // solid beige bg + simple brown border, approximate parchment.
            g.fill(x, y, x + GUI_WIDTH, y + GUI_HEIGHT, _cfg.applyBackground(MC_PAPER_FALLBACK));
            int _mcb = _cfg.applyBackground(MC_PAPER_BORDER);
            g.fill(x, y, x + GUI_WIDTH, y + 2, _mcb);
            g.fill(x, y, x + 2, y + GUI_HEIGHT, _mcb);
            g.fill(x, y + GUI_HEIGHT - 2, x + GUI_WIDTH, y + GUI_HEIGHT, _mcb);
            g.fill(x + GUI_WIDTH - 2, y, x + GUI_WIDTH, y + GUI_HEIGHT, _mcb);
        }
        else
        {
            // DEFAULT + AE-without-texture: config fill then frame (historical order).
            g.fill(x, y, x + GUI_WIDTH, y + GUI_HEIGHT, _cfg.bg());
            if (_cfg.isAe())
                drawAeFrame(g, _cfg, x, y, GUI_WIDTH, GUI_HEIGHT);
            else
                _cfg.drawBorders(g, x, y, GUI_WIDTH, GUI_HEIGHT);
        }

        // Title bar
        int _bw = _cfg.frameBorderWidth();
        if (_cfg.isMineColonies())
        {
            // MineColonies: no grey band (it clashes with the parchment). The title sits
            // directly on the paper in black, with a thin brown rule under the title zone to
            // separate it from the content. Buttons (Unlink/Restart) and the drag handle keep
            // their exact positions — nothing is moved or masked.
            g.drawString(this.font, this.title, x + 58, y + 7, _cfg.bodyText(), false);
            g.fill(x + _bw, y + 21, x + GUI_WIDTH - _bw, y + 22, _cfg.applyBackground(MC_PAPER_BORDER));
        }
        else
        {
            g.fill(x + _bw, y + _bw,
                    x + GUI_WIDTH - _bw, y + 22, _cfg.title());
            // Title top rule (themed)
            int _tc2 = _cfg.applyOpacity(_cfg.titleHi());
            g.fill(x + _bw, y + _bw,
                    x + GUI_WIDTH - _bw, y + _bw + 2, _tc2);
            g.drawString(this.font, this.title, x + 58, y + 7, _cfg.titleText(), false);
        }

        // Drag handle cursor ✥ — centered between Unlink and Restart
        {
            int handleX1 = x + getDeleteBtnW() + 12;
            int handleX2 = x + GUI_WIDTH - getRestartBtnW() - 12;
            int handleCX = (handleX1 + handleX2) / 2;
            int handleCY = y + 11;
            boolean hoverHandle = isInDragHandle(mx, my);
            int dotColor = _cfg.handleDot(hoverHandle);
            // ⠿ pattern: 3 columns × 2 rows of spaced dots
            int[] dotsX = { -4, 0, 4, -4, 0, 4 };
            int[] dotsY = { -3, -3, -3,  3,  3,  3 };
            for (int d = 0; d < dotsX.length; d++)
                g.fill(handleCX + dotsX[d], handleCY + dotsY[d],
                        handleCX + dotsX[d] + 2, handleCY + dotsY[d] + 2, dotColor);
        }

        int dbX = getDeleteBtnX(), dbY = getDeleteBtnY(), dbW = getDeleteBtnW(), dbH = getDeleteBtnH();
        boolean delHov = mx >= dbX && mx <= dbX + dbW && my >= dbY && my <= dbY + dbH;
        boolean canDel = !tabMetas.isEmpty();
        drawButton(g, dbX, dbY, dbW, dbH,
                canDel ? (delHov ? 0xFF880000 : 0xFF550000) : 0xFF333333,
                Component.translatable("colonylink.screen.btn.unlink").getString(), canDel ? 0xFF4444 : 0x888888,
                delHov, canDel, _cfg.semRed());

        int rbX = getRestartBtnX(), rbY = getRestartBtnY(), rbW = getRestartBtnW(), rbH = getRestartBtnH();
        boolean restHov = mx >= rbX && mx <= rbX + rbW && my >= rbY && my <= rbY + rbH;
        drawButton(g, rbX, rbY, rbW, rbH, restHov ? 0xFF885500 : 0xFF553300, Component.translatable("colonylink.screen.btn.restart").getString(), 0xFFAA44,
                restHov, true, _cfg.semAmber());

        List<Component> tip = new ArrayList<>();

        // #12: Citizens tab → simplified header instead of builder info
        if (activeTabIndex == CITIZENS_TAB_INDEX)
        {
            ColonyLinkGuiConfig _c = ColonyLinkGuiConfig.get();
            // MineColonies: no opaque header well — text sits on the parchment.
            if (!_c.isMineColonies())
            {
                g.fill(x + 6, y + 22, x + GUI_WIDTH - 6, y + 80, _c.applyOpacity(_c.wellBg()));
                g.fill(x + 6, y + 22, x + GUI_WIDTH - 6, y + 23, _c.applyOpacity(_c.wellLight()));
                g.fill(x + 6, y + 22, x + 7, y + 80, _c.applyOpacity(_c.wellLight()));
                g.fill(x + 6, y + 79, x + GUI_WIDTH - 6, y + 80, _c.applyOpacity(_c.wellDark()));
                g.fill(x + GUI_WIDTH - 7, y + 22, x + GUI_WIDTH - 6, y + 80, _c.applyOpacity(_c.wellDark()));
            }
            // Light-body themes (AE + MineColonies) use the dark muted tint; DEFAULT keeps its
            // light-grey literal on the dark body.
            boolean _aeC = _c.isLightBody();
            g.drawCenteredString(this.font, Component.translatable("colonylink.screen.cit.header").getString(), x + GUI_WIDTH / 2 - 12, y + 30, _c.bodyText());
            String countStr = citizensLoading ? Component.translatable("colonylink.screen.cit.loading").getString()
                    : citizenEntries.isEmpty() ? Component.translatable("colonylink.screen.cit.no_requests").getString()
                      : Component.translatable("colonylink.screen.cit.open_requests", citizenEntries.size()).getString();
            g.drawCenteredString(this.font, countStr, x + GUI_WIDTH / 2 - 12, y + 44, _aeC ? _c.mutedText() : 0xAAAAAA);
            String pkgDesc = citizenPackageCount > 0
                    ? Component.translatable("colonylink.screen.cit.packages_loaded", citizenPackageCount).getString()
                    : Component.translatable("colonylink.screen.cit.no_packages").getString();
            g.drawCenteredString(this.font, pkgDesc, x + GUI_WIDTH / 2 - 12, y + 57, _aeC ? _c.mutedText() : 0x888888);

            // Package slot (top-right of the header)
            int pkgSlotX = x + GUI_WIDTH - 26, pkgSlotY = y + 26;
            boolean pkgHov = mx >= pkgSlotX && mx <= pkgSlotX + 18 && my >= pkgSlotY && my <= pkgSlotY + 18;
            // Slot background: gold if packages present, grey otherwise
            int pkgBorderColor = citizenPackageCount > 0 ? 0xFF996600 : 0xFF665544;
            int pkgFillColor   = citizenPackageCount > 0 ? (pkgHov ? 0xFF5A3A00 : 0xFF3A2A00) : (pkgHov ? 0xFF4A4A4A : 0xFF3A3A3A);
            g.fill(pkgSlotX - 1, pkgSlotY - 1, pkgSlotX + 19, pkgSlotY + 19, pkgBorderColor);
            g.fill(pkgSlotX, pkgSlotY, pkgSlotX + 18, pkgSlotY + 18, pkgFillColor);
            // Icon: normal item if packages available, empty slot otherwise
            if (citizenPackageCount > 0)
            {
                net.minecraft.world.item.ItemStack pkgDisplayStack = new net.minecraft.world.item.ItemStack(ColonyLink.COLONY_LINK_PACKAGE.get());
                g.renderItem(pkgDisplayStack, pkgSlotX + 1, pkgSlotY + 1);
                // Count badge AFTER renderItem so it isn't covered
                String badge = citizenPackageCount >= 100 ? "99+" : String.valueOf(citizenPackageCount);
                g.renderItemDecorations(this.font, pkgDisplayStack.copyWithCount(citizenPackageCount), pkgSlotX + 1, pkgSlotY + 1, badge);
            }
            else
            {
                // Empty slot: centered "+" to indicate loading is needed
                g.drawCenteredString(this.font, "§8+", pkgSlotX + 9, pkgSlotY + 5, 0x666666);
            }
            // Package slot tooltip
            if (pkgHov)
            {
                tip.clear();
                tip.add(net.minecraft.network.chat.Component.translatable("colonylink.screen.pkg.slot"));
                tip.add(net.minecraft.network.chat.Component.translatable("colonylink.screen.pkg.stored", citizenPackageCount));
                tip.add(net.minecraft.network.chat.Component.literal("§8──────────────────"));
                tip.add(net.minecraft.network.chat.Component.translatable("colonylink.screen.pkg.cost1"));
                tip.add(net.minecraft.network.chat.Component.translatable("colonylink.screen.pkg.cost2"));
                tip.add(net.minecraft.network.chat.Component.literal("§8──────────────────"));
                if (citizenPackageCount < 64)
                    tip.add(net.minecraft.network.chat.Component.translatable("colonylink.screen.pkg.load"));
                else
                    tip.add(net.minecraft.network.chat.Component.translatable("colonylink.screen.pkg.full"));
            }
        }
        else if (activeTabIndex == STATS_TAB_INDEX)
        {
            drawStatsContent(g, x, y, mx, my);
        }
        else
        {
            drawInfoPanel(g, x, y, mx, my);
            if (!isOutOfPower())
                drawRequestPanel(g, x, y, mx, my, tip);
        }

        // List
        ColonyLinkGuiConfig _cl = ColonyLinkGuiConfig.get();
        int listW = GUI_WIDTH - 26, listY = getListStartY();
        // MineColonies: no opaque list well — rows sit directly on the parchment.
        // v1.8.0 — the Stats tab draws its own full-body content; no list well.
        if (!_cl.isMineColonies() && !isStatsTab())
        {
            g.fill(x + 6, listY - 1, x + GUI_WIDTH - 18, listY - 1 + MAX_VISIBLE * ENTRY_HEIGHT + 1, _cl.applyOpacity(_cl.listBg()));
            g.fill(x + 6, listY - 1, x + GUI_WIDTH - 18, listY, _cl.applyOpacity(_cl.wellLight()));
            g.fill(x + 6, listY - 1, x + 7, listY - 1 + MAX_VISIBLE * ENTRY_HEIGHT + 1, _cl.applyOpacity(_cl.wellLight()));
        }

        // #12: Citizens tab active → read-only list of non-builder citizen requests
        if (activeTabIndex == CITIZENS_TAB_INDEX)
        {
            if (citizensLoading)
            {
                g.drawCenteredString(this.font, Component.translatable("colonylink.screen.cit.loading_citizens").getString(),
                        x + GUI_WIDTH / 2, listY + MAX_VISIBLE * ENTRY_HEIGHT / 2 - 4, 0x888888);
            }
            else if (citizenEntries.isEmpty())
            {
                g.drawCenteredString(this.font, Component.translatable("colonylink.screen.cit.no_requests_citizens").getString(),
                        x + GUI_WIDTH / 2, listY + MAX_VISIBLE * ENTRY_HEIGHT / 2 - 4, 0x888888);
            }
            else
            {
                int vis = Math.min(MAX_VISIBLE, citizenEntries.size() - scrollOffset);
                for (int i = 0; i < vis; i++)
                {
                    var ce  = citizenEntries.get(i + scrollOffset);
                    int ey  = listY + i * ENTRY_HEIGHT;
                    // DEFAULT/AE: opaque zebra wells. MineColonies: very subtle translucent
                    // shade on even rows so the parchment stays visible through it.
                    if (!_cl.isMineColonies())
                    {
                        int rowBg = _cl.applyOpacity((i % 2 == 0) ? _cl.rowA() : _cl.rowB());
                        g.fill(x + 7, ey, x + 7 + listW, ey + ENTRY_HEIGHT, rowBg);
                    }
                    else if (i % 2 == 0)
                    {
                        g.fill(x + 7, ey, x + 7 + listW, ey + ENTRY_HEIGHT, _cl.applyOpacity(MC_ROW_SHADE));
                    }
                    g.renderItem(ce.stack(), x + 9, ey + 2);

                    String itemName = ce.stack().getDisplayName().getString();
                    if (itemName.startsWith("[") && itemName.endsWith("]"))
                        itemName = itemName.substring(1, itemName.length() - 1);

                    // 1.7.0-beta.3 — one button per row from citizenButton(): MineColonies
                    // state first, then our mark, Warehouse stock, Send / Craft (Send and
                    // Craft still need the Warehouse card, as before).
                    CitBtn citBtn  = citizenButton(ce);
                    boolean hasBtn = citBtn != null;
                    int btnW = hasBtn ? citizenButtonWidth(citBtn) : 44, btnH = 14;
                    int btnX = getGuiX() + 7 + listW - btnW - 2;
                    int btnY = ey + (ENTRY_HEIGHT - btnH) / 2;
                    boolean btnHov = hasBtn && mx >= btnX && mx <= btnX + btnW && my >= btnY && my <= btnY + btnH;

                    // Text truncated when a button is present
                    int maxTextW = hasBtn ? listW - btnW - 28 : listW - 10;
                    String truncName = ce.count() + "x " + itemName;
                    while (truncName.length() > 2 && this.font.width("§f" + truncName) > maxTextW)
                        truncName = truncName.substring(0, truncName.length() - 1);

                    // Drop the §f prefix (it would force white and override bodyText).
                    g.drawString(this.font, truncName, x + 29, ey + 3, _cl.bodyText(), false);
                    // Sub-line (name · job). The §7/§8 codes force a light grey unreadable on
                    // the parchment, so MineColonies drops them and uses the dark muted tint;
                    // AE and DEFAULT keep the original coloured string.
                    if (_cl.isMineColonies())
                        g.drawString(this.font, ce.citizenName() + " · " + ce.jobName(),
                                x + 29, ey + 12, _cl.mutedText(), false);
                    else
                        g.drawString(this.font, "§7" + ce.citizenName() + " §8· §7" + ce.jobName(),
                                x + 29, ey + 12, 0xAAAAAA, false);

                    if (hasBtn)
                        drawCitizenButton(g, citBtn, btnX, btnY, btnW, btnH, btnHov);

                    if (mx >= x + 7 && mx <= x + 7 + listW && my >= ey && my <= ey + ENTRY_HEIGHT)
                    {
                        tip.clear();
                        tip.add(Component.literal("§f" + ce.count() + "x " + itemName));
                        tip.add(Component.translatable("colonylink.screen.tip.citizen", ce.citizenName()));
                        tip.add(Component.translatable("colonylink.screen.tip.job", ce.jobName()));
                        // 1.7.0-beta.3 — MineColonies state, Warehouse stock, who marked it.
                        String stateKey = switch (ce.colonyState())
                        {
                            case CitizensPacket.STATE_DELIVERING   -> "colonylink.citizens.tip.delivering";
                            case CitizensPacket.STATE_WAREHOUSE    -> "colonylink.citizens.tip.warehouse";
                            case CitizensPacket.STATE_COLONY_CRAFT -> "colonylink.citizens.tip.colony_craft";
                            case CitizensPacket.STATE_RETRYING     -> "colonylink.citizens.tip.retrying";
                            default                                -> "colonylink.citizens.tip.player";
                        };
                        tip.add(Component.literal("§7").append(Component.translatable(stateKey)));
                        if (ce.warehouseStock() >= 0)
                            tip.add(Component.literal("§7").append(Component.translatable(
                                    "colonylink.citizens.tip.wh_stock", ce.warehouseStock(), ce.count())));
                        if (ce.mark() == CitizensPacket.MARK_CRAFTING)
                        {
                            tip.add(Component.literal("§6").append(Component.translatable("colonylink.citizens.tip.crafting")));
                            if (!ce.markedBy().isEmpty())
                                tip.add(Component.literal("§7").append(Component.translatable(
                                        "colonylink.citizens.tip.crafted_by", ce.markedBy())));
                        }
                        else if (ce.mark() == CitizensPacket.MARK_SENT && !ce.markedBy().isEmpty())
                            tip.add(Component.literal("§7").append(Component.translatable(
                                    "colonylink.citizens.tip.sent_by", ce.markedBy())));
                        if (btnHov && citBtn.kind() == CitBtnKind.SENT && citBtn.clickable())
                        {
                            tip.add(Component.translatable("colonylink.screen.tip.already_sent"));
                            tip.add(Component.translatable("colonylink.screen.tip.cost_2pkg"));
                        }
                        else if (btnHov && citBtn.kind() == CitBtnKind.SEND)
                            tip.add(Component.translatable("colonylink.screen.tip.send_wh"));
                        else if (btnHov && citBtn.kind() == CitBtnKind.CRAFT)
                            tip.add(Component.translatable("colonylink.screen.tip.craft_ae2"));
                    }
                }

                if (citizenEntries.size() > MAX_VISIBLE)
                {
                    int sbX = getScrollbarX(), sbH = MAX_VISIBLE * ENTRY_HEIGHT;
                    g.fill(sbX, listY, sbX + SCROLLBAR_WIDTH, listY + sbH, _cl.applyOpacity(_cl.isAe() ? 0xFF413F54 : 0xFF2A2A2A));
                    int thumbH = Math.max(16, sbH * MAX_VISIBLE / citizenEntries.size());
                    int thumbY = listY + (sbH - thumbH) * scrollOffset
                            / Math.max(1, citizenEntries.size() - MAX_VISIBLE);
                    g.fill(sbX + 1, thumbY, sbX + SCROLLBAR_WIDTH - 1, thumbY + thumbH, _cl.applyOpacity(_cl.scrollThumb()));
                }
            }
        }
        else if (activeTabIndex == STATS_TAB_INDEX)
        {
            // Stats tab: the body was fully drawn by drawStatsContent(); no list here.
        }
        else if (isOutOfPower())
        {
            g.drawCenteredString(this.font, Component.translatable("colonylink.screen.power.list").getString(),
                    x + GUI_WIDTH / 2, listY + MAX_VISIBLE * ENTRY_HEIGHT / 2 - 4, _cl.isAe() ? _cl.semRed() : 0xFF4444);
        }
        else
        {
            int vis = Math.min(MAX_VISIBLE, entries.size() - scrollOffset);
            for (int i = 0; i < vis; i++)
            {
                int idx = i + scrollOffset;
                var entry = entries.get(idx);
                ItemStack stack = entry.stack();
                // v1.6.0 — client-side pending overlays (WAREHOUSE mode only).
                ResourceStatus status = displayStatus(entry.status(), stack);
                int rc = entry.realCount();
                int ey = listY + i * ENTRY_HEIGHT;

                // DEFAULT/AE: opaque zebra wells. MineColonies: very subtle translucent
                // shade on even rows so the parchment stays visible through it.
                if (!_cl.isMineColonies())
                {
                    int _rowBg = _cl.applyOpacity((i % 2 == 0) ? _cl.rowA() : _cl.rowB());
                    g.fill(x + 7, ey, x + 7 + listW, ey + ENTRY_HEIGHT, _rowBg);
                }
                else if (i % 2 == 0)
                {
                    g.fill(x + 7, ey, x + 7 + listW, ey + ENTRY_HEIGHT, _cl.applyOpacity(MC_ROW_SHADE));
                }
                g.renderItem(stack, x + 9, ey + 2);

                // Name with scrolling when too long
                String rawName = stack.getDisplayName().getString();
                // Light-body themes (AE + MineColonies): plain "[DO] " (same width — the § codes
                // are zero-width, so line measurement/truncation below is unchanged), then the tag
                // is over-drawn in semBlue on top. DEFAULT keeps the cyan §b tag.
                boolean domumTag = entry.isDomum();
                String prefix  = domumTag ? (_cl.isLightBody() ? "[DO] " : "§b[DO] §r") : "";
                String fullText = rc + "x " + rawName;
                int nameAreaW = listW - 65 - 20; // available width for the text
                int fullW = this.font.width(prefix + fullText);
                String displayText;
                if (fullW > nameAreaW)
                {
                    // Scrolling: time-based offset, reset at the line start
                    long t = System.currentTimeMillis();
                    int scrollRange = fullW - nameAreaW + 10;
                    int period = scrollRange * 120 + 2000; // ms for a round trip
                    long phase = t % period;
                    int offset;
                    if (phase < 1000)                offset = 0;                            // start pause
                    else if (phase < 1000 + scrollRange * 60L) offset = (int)((phase - 1000) / 60);
                    else if (phase < 1000 + scrollRange * 60L + 1000) offset = scrollRange; // end pause
                    else offset = scrollRange - (int)((phase - 2000 - scrollRange * 60L) / 60);

                    // Clip + translate.
                    // enableScissor() ignores the pose() matrix, so we transform the clip
                    // rect into real screen space (post-scale) so it lands exactly on the
                    // line whatever GUI scale is configured.
                    g.enableScissor(toScreenX(x + 29), toScreenY(ey),
                            toScreenX(x + 29 + nameAreaW), toScreenY(ey + ENTRY_HEIGHT));
                    g.drawString(this.font, prefix + fullText, x + 29 - offset, ey + 6, _cl.bodyText(), false);
                    if (domumTag && _cl.isLightBody())
                        g.drawString(this.font, "[DO]", x + 29 - offset, ey + 6, _cl.semBlue(), false);
                    g.disableScissor();
                }
                else
                {
                    g.drawString(this.font, prefix + fullText, x + 29, ey + 6, _cl.bodyText(), false);
                    if (domumTag && _cl.isLightBody())
                        g.drawString(this.font, "[DO]", x + 29, ey + 6, _cl.semBlue(), false);
                }

                // Row hover zone (excluding the button)
                boolean lineHov = mx >= x + 7 && mx <= x + 7 + listW - 65
                        && my >= ey && my <= ey + ENTRY_HEIGHT;

                var we = getWarehouseEntry(stack);
                if (we != null)
                {
                    long tot = we.inWarehouse() + we.viaCraft();
                    // Warehouse availability colours: on a LIGHT body (AE + MineColonies) the bright
                    // green/yellow/red are unreadable → use the light-calibrated semantic tints.
                    // DEFAULT (dark body) keeps its original bright colours.
                    boolean _whLight = _cl.isLightBody();
                    String wt; int wc;
                    if (tot >= rc)     { wt = Component.translatable("colonylink.screen.wh.have", tot).getString();             wc = _whLight ? _cl.semGreen() : 0x00FF88; }
                    else if (tot > 0)  { wt = Component.translatable("colonylink.screen.wh.partial", tot, rc).getString(); wc = _whLight ? _cl.semAmber() : 0xFFCC44; }
                    else               { wt = Component.translatable("colonylink.screen.wh.none").getString();                  wc = _whLight ? _cl.semRed()   : 0xFF4444; }
                    g.drawString(this.font, wt, x + 29, ey + 13, wc, false);

                    if (lineHov && !we.tooltipLines().isEmpty())
                    {
                        tip.clear();
                        tip.add(Component.translatable("colonylink.screen.wh.avail_title"));
                        tip.add(Component.translatable("colonylink.screen.wh.direct", we.inWarehouse()));
                        tip.add(Component.translatable("colonylink.screen.wh.via_craft", we.viaCraft()));
                        tip.add(Component.literal("§8──────────"));
                        for (Component ln : we.tooltipLines()) tip.add(ln);
                    }
                }

                // Domum tooltip on row hover
                if (lineHov && entry.isDomum() && !entry.tooltipLines().isEmpty())
                {
                    tip.clear();
                    tip.add(Component.literal("§b" + rawName));
                    for (Component ln : entry.tooltipLines()) tip.add(ln);
                }

                int[] btn = new int[4];
                getBtnBounds(i, btn);
                int bx2 = btn[0], by2 = btn[1], bw2 = btn[2], bh2 = btn[3];
                boolean hov = mx >= bx2 && mx <= bx2 + bw2 && my >= by2 && my <= by2 + bh2;

                if (hov && status == ResourceStatus.SENT_PENDING && entry.status() != ResourceStatus.SENT_PENDING)
                {
                    // v1.6.0 — client-side pending overlay: the server tooltip
                    // doesn't know yet, show the pending explanation instead.
                    tip.clear();
                    tip.add(Component.translatable("colonylink.screen.tip.sent_pending"));
                }
                else if (hov && !entry.tooltipLines().isEmpty())
                {
                    tip.clear();
                    for (Component ln : entry.tooltipLines()) tip.add(ln);
                }

                if (mcButtonReady())
                {
                    drawMcButtonAuto(g, bx2, by2, bw2, bh2,
                            getButtonTextWithWarehouse(status, stack), isButtonClickable(status, stack), hov);
                }
                else if (_cl.isAe() && aeButtonPresent)
                {
                    drawAeButton(g, bx2, by2, bw2, bh2, hov, isButtonClickable(status, stack),
                            getButtonTextWithWarehouse(status, stack), aeStatusTextColor(status));
                }
                else
                {
                    int bg2 = _cl.applyControl(getButtonColorWithWarehouse(status, stack, hov && isButtonClickable(status, stack)));
                    g.fill(bx2, by2, bx2 + bw2, by2 + bh2, bg2);
                    g.fill(bx2, by2, bx2 + bw2, by2 + 1, _cl.applyControl(_cl.btnBevelLight()));
                    g.fill(bx2, by2, bx2 + 1, by2 + bh2, _cl.applyControl(_cl.btnBevelLight()));
                    g.fill(bx2, by2 + bh2 - 1, bx2 + bw2, by2 + bh2, _cl.applyControl(_cl.btnBevelDark()));
                    g.fill(bx2 + bw2 - 1, by2, bx2 + bw2, by2 + bh2, _cl.applyControl(_cl.btnBevelDark()));
                    g.drawCenteredString(this.font, getButtonTextWithWarehouse(status, stack),
                            bx2 + bw2 / 2, by2 + 4, getButtonTextColor(status));
                }
            }

            if (entries.size() > MAX_VISIBLE)
            {
                int sbX = getScrollbarX(), sbT = getScrollbarTop(), sbB = getScrollbarBottom();
                g.fill(sbX, sbT, sbX + SCROLLBAR_WIDTH, sbB, _cl.scrollTrack());
                g.fill(sbX, sbT, sbX + 1, sbB, _cl.wellLight());
                g.fill(sbX, sbT, sbX + SCROLLBAR_WIDTH, sbT + 1, _cl.wellLight());
                int ty2 = getThumbY(), th2 = getThumbHeight();
                g.fill(sbX + 1, ty2, sbX + SCROLLBAR_WIDTH, ty2 + th2, _cl.scrollThumb());
                g.fill(sbX + 1, ty2, sbX + SCROLLBAR_WIDTH, ty2 + 1, _cl.btnBevelLight());
                g.fill(sbX + 1, ty2, sbX + 2, ty2 + th2, _cl.btnBevelLight());
                g.fill(sbX + 1, ty2 + th2 - 1, sbX + SCROLLBAR_WIDTH, ty2 + th2, _cl.btnBevelDark());
                g.fill(sbX + SCROLLBAR_WIDTH - 1, ty2, sbX + SCROLLBAR_WIDTH, ty2 + th2, _cl.btnBevelDark());
            }
        }

        g.fill(x + 6, y + GUI_HEIGHT - 44, x + GUI_WIDTH - 6, y + GUI_HEIGHT - 43, ColonyLinkGuiConfig.get().applyOpacity(ColonyLinkGuiConfig.get().separator()));
        // #12: WareCheck, Priority and buttons hidden on the Citizens/Stats tabs
        if (isBuilderTab())
        {
            drawWareCheckButton(g, mx, my);
            drawPrioritySwitch(g, mx, my);
        }
        g.fill(x + 6, y + GUI_HEIGHT - 26, x + GUI_WIDTH - 6, y + GUI_HEIGHT - 25, ColonyLinkGuiConfig.get().applyOpacity(ColonyLinkGuiConfig.get().separator()));

        if (isBuilderTab())
        {
            ColonyLinkGuiConfig _cBtn = ColonyLinkGuiConfig.get();
            int caX = getCraftAllBtnX(), caY = getCraftAllBtnY(), caW = getCraftAllBtnW(), caH = getCraftAllBtnH();
            boolean caHov = mx >= caX && mx <= caX + caW && my >= caY && my <= caY + caH;
            boolean hasCraft = hasCraftableItems();

            // #8: visual state of the Craft All button
            int caBg, caTextColor;
            String caLabel;
            if (craftInProgress)
            {
                // In progress: dark blue bg, animated text (blinks via gameTicks)
                long ticks = (System.currentTimeMillis() / 400) % 3;
                String dots = ticks == 0 ? "." : ticks == 1 ? ".." : "...";
                caLabel     = Component.translatable("colonylink.screen.btn.crafting_anim", dots).getString();
                caBg        = _cBtn.applyControl(caHov ? 0xFF003355 : 0xFF002244);
                caTextColor = 0x55AAFF;
            }
            else if (hasCraft)
            {
                caLabel     = Component.translatable("colonylink.screen.btn.craft_all").getString();
                caBg        = _cBtn.applyControl(caHov ? 0xFF007700 : 0xFF005500);
                caTextColor = 0x00FF00;
            }
            else
            {
                caLabel     = Component.translatable("colonylink.screen.btn.craft_all").getString();
                caBg        = _cBtn.applyControl(0xFF333333);
                caTextColor = 0x888888;
            }

            boolean caEnabled = craftInProgress || hasCraft;
            int caAeLabel = craftInProgress ? _cBtn.semBlue() : _cBtn.semGreen();
            if (mcButtonReady())
            {
                drawMcButtonAuto(g, caX, caY, caW, caH, caLabel, caEnabled, caHov);
            }
            else if (_cBtn.isAe() && aeButtonPresent)
            {
                drawAeButton(g, caX, caY, caW, caH, caHov, caEnabled, caLabel, caAeLabel);
            }
            else
            {
                g.fill(caX, caY, caX + caW, caY + caH, caBg);
                g.fill(caX, caY, caX + caW, caY + 1, _cBtn.applyControl(_cBtn.btnBevelLight())); g.fill(caX, caY, caX + 1, caY + caH, _cBtn.applyControl(_cBtn.btnBevelLight()));
                g.fill(caX, caY + caH - 1, caX + caW, caY + caH, _cBtn.applyControl(_cBtn.btnBevelDark())); g.fill(caX + caW - 1, caY, caX + caW, caY + caH, _cBtn.applyControl(_cBtn.btnBevelDark()));
                g.drawCenteredString(this.font, caLabel, caX + caW / 2, caY + 4, caTextColor);
            }

            if (caHov)
            {
                tip.clear();
                if (craftInProgress)
                {
                    tip.add(Component.translatable("colonylink.screen.tip.crafting_progress"));
                    tip.add(Component.translatable("colonylink.screen.tip.types_submitted", craftInProgressCount));
                    tip.add(Component.translatable("colonylink.screen.tip.waiting_refresh"));
                }
                else if (hasCraft)
                {
                    long craftCount = entries.stream()
                            .filter(e -> e.status() == ResourceStatus.CRAFTABLE || e.status() == ResourceStatus.MISSING)
                            .count();
                    tip.add(Component.translatable("colonylink.screen.tip.craft_all_desc"));
                    tip.add(Component.translatable("colonylink.screen.tip.types_to_craft", craftCount));
                    tip.add(Component.translatable("colonylink.screen.tip.cpus_available", availableCpus));
                }
                else
                {
                    tip.add(Component.translatable("colonylink.screen.tip.no_craftable"));
                }
            }

            int saX = getSendAllBtnX(), saY = getSendAllBtnY(), saW = getSendAllBtnW(), saH = getSendAllBtnH();
            boolean saHov = mx >= saX && mx <= saX + saW && my >= saY && my <= saY + saH;
            boolean hasAvail = hasSendableItems();
            if (mcButtonReady())
            {
                drawMcButtonAuto(g, saX, saY, saW, saH, Component.translatable("colonylink.screen.btn.send_all").getString(), hasAvail, saHov);
            }
            else if (_cBtn.isAe() && aeButtonPresent)
            {
                drawAeButton(g, saX, saY, saW, saH, saHov, hasAvail, Component.translatable("colonylink.screen.btn.send_all").getString(), _cBtn.semBlue());
            }
            else
            {
                g.fill(saX, saY, saX + saW, saY + saH, _cBtn.applyControl(hasAvail ? (saHov ? 0xFF0066CC : 0xFF004488) : 0xFF333333));
                g.fill(saX, saY, saX + saW, saY + 1, _cBtn.applyControl(_cBtn.btnBevelLight())); g.fill(saX, saY, saX + 1, saY + saH, _cBtn.applyControl(_cBtn.btnBevelLight()));
                g.fill(saX, saY + saH - 1, saX + saW, saY + saH, _cBtn.applyControl(_cBtn.btnBevelDark())); g.fill(saX + saW - 1, saY, saX + saW, saY + saH, _cBtn.applyControl(_cBtn.btnBevelDark()));
                g.drawCenteredString(this.font, Component.translatable("colonylink.screen.btn.send_all").getString(), saX + saW / 2, saY + 4, hasAvail ? 0x4488FF : 0x888888);
            }

        } // end of the non-Citizens block

        drawCfgButton(g, mx, my, tip);
        drawTabs(g, mx, my, tip);

        if (hasWarehouseCard && !isOutOfPower())
        {
            int sx = getSwitchX(), sy = getSwitchY(), sw2 = getSwitchW(), sh2 = getSwitchH();
            if (mx >= sx && mx <= sx + sw2 && my >= sy && my <= sy + sh2)
            {
                tip.clear();
                tip.add(Component.translatable("colonylink.screen.tip.send_priority"));
                String netLabel = "AE2";
                tip.add(warehousePriority
                        ? Component.translatable("colonylink.screen.tip.priority_wh")
                        : Component.translatable("colonylink.screen.tip.priority_net", netLabel));
                tip.add(Component.translatable("colonylink.screen.tip.click_toggle"));
            }
        }
        if (mx >= rbX && mx <= rbX + rbW && my >= rbY && my <= rbY + rbH)
        {
            tip.clear();
            tip.add(Component.translatable("colonylink.screen.tip.restart_builder"));
            tip.add(Component.translatable("colonylink.screen.tip.restart_desc"));
        }
        if (mx >= dbX && mx <= dbX + dbW && my >= dbY && my <= dbY + dbH)
        {
            tip.clear();
            tip.add(Component.translatable("colonylink.screen.tip.unlink_builder"));
            tip.add(Component.translatable("colonylink.screen.tip.unlink_desc"));
            tip.add(Component.translatable("colonylink.screen.tip.unlink_note"));
        }

        // Locate button tooltip (in the info panel)
        if (isBuilderTab() && !isOutOfPower())
        {
            int lbXT = x + GUI_WIDTH - 6 - LOCATE_BTN_W - 4;
            int lbYT = y + 22;
            if (mx >= lbXT && mx <= lbXT + LOCATE_BTN_W && my >= lbYT && my <= lbYT + LOCATE_BTN_H)
            {
                tip.clear();
                tip.add(Component.translatable("colonylink.screen.tip.locate_builder"));
                tip.add(Component.translatable("colonylink.screen.tip.locate_desc"));
                tip.add(Component.translatable("colonylink.screen.tip.locate_dur",
                        ColonyLinkConfig.safeGet(ColonyLinkConfig.LOCATE_GLOW_DURATION_SECONDS, 8)));
            }
        }

        if (ColonyLinkGuiConfig.get().scale != 1.0f)
            g.pose().popPose();

        super.render(g, rawMx, rawMy, pt);
        if (!tip.isEmpty()) {
            // renderComponentTooltip renders each Component as a single visual line and does
            // not break on '\n' (it would show up as a missing-glyph box). Flatten any
            // newline-containing line into separate literals so multi-line tooltips wrap
            // correctly. Components without '\n' are left untouched to preserve their styling
            // (e.g. item-name sub-components in availability tooltips).
            List<Component> flatTip = new ArrayList<>();
            for (Component c : tip) {
                String s = c.getString();
                if (s.indexOf('\n') >= 0) {
                    for (String line : s.split("\n", -1)) flatTip.add(Component.literal(line));
                } else {
                    flatTip.add(c);
                }
            }
            g.renderComponentTooltip(this.font, flatTip, mx, my);
        }
    }

    // mouseClicked()
    @Override
    public boolean mouseClicked(double rawMx, double rawMy, int btn)
    {
        double mx = toGuiX(rawMx), my = toGuiY(rawMy);
        // Config gear button
        if (btn == 0)
        {
            int bx = getCfgBtnX(), by = getCfgBtnY();
            if (mx >= bx && mx <= bx + CFG_BTN_W && my >= by && my <= by + CFG_BTN_H)
            {
                this.minecraft.setScreen(new ColonyLinkConfigScreen(this));
                return true;
            }
        }

        // GUI drag: click in the title-bar handle zone
        if (btn == 0 && isInDragHandle(mx, my))
        {
            isDraggingGui  = true;
            guiDragStartX  = mx;
            guiDragStartY  = my;
            guiDragOriginX = dragOffsetX;
            guiDragOriginY = dragOffsetY;
            return true;
        }
        for (int i = 0; i < tabMetas.size(); i++)
        {
            int tx = getTabX(i), ty = getTabY(i);
            if (mx >= tx && mx <= tx + TAB_WIDTH && my >= ty && my <= ty + TAB_HEIGHT)
            {
                if (i != activeTabIndex)
                {
                    // #5: before leaving the active tab, if it had entries we remember it —
                    // but do NOT mark it unread (we just saw it)
                    lastReadEntryCount.put(activeTabIndex, entries.size());

                    activeTabIndex = i;
                    BlockPos nb = tabMetas.get(i).builderPos();
                    builderPos = nb;
                    // Fix 2: keep the previous data displayed until the new server packet
                    // arrives — avoids an empty GUI during the round-trip
                    builderName   = tabMetas.get(i).builderName();
                    buildingName  = tabMetas.get(i).buildingLabel();
                    workerStatus  = Component.translatable("colonylink.screen.status.loading").getString();
                    warehouseSnapshot = null;
                    wareCheckState    = WareCheckState.IDLE;
                    scrollOffset      = 0;
                    // #5: mark the new active tab as read
                    unreadTabs.remove(i);
                    lastReadEntryCount.put(i, 0);
                    UNREAD_TAB_COUNT = unreadTabs.size();
                    PacketDistributor.sendToServer(new GuiStatePacket(true, nb, activeTabIndex));
                }
                return true;
            }
        }

        // #12: click on the Citizens tab (bottom right)
        {
            int tx = getCitizenTabX(), ty = getCitizenTabY();
            if (mx >= tx && mx <= tx + TAB_WIDTH && my >= ty && my <= ty + TAB_HEIGHT)
            {
                if (activeTabIndex != CITIZENS_TAB_INDEX)
                {
                    lastReadEntryCount.put(activeTabIndex, entries.size());
                    activeTabIndex   = CITIZENS_TAB_INDEX;
                    scrollOffset     = 0;
                    citizensLoading  = true;
                    citizenEntries   = new java.util.ArrayList<>();
                    PacketDistributor.sendToServer(new CitizensRequestPacket());
                    // Refresh the count from the wand NBT (read-only)
                    net.minecraft.world.item.ItemStack tabWand = getClientWand();
                    if (!tabWand.isEmpty())
                        this.citizenPackageCount = ColonyLinkWandLinkableHandler.getCitizenPackages(tabWand);
                }
                return true;
            }
        }

        // v1.8.0: click on the Stats tab (bottom-most). Mirrors the Citizens tab:
        // switch to the special tab and request its data once (no polling).
        {
            int tx = getStatsTabX(), ty = getStatsTabY();
            if (mx >= tx && mx <= tx + TAB_WIDTH && my >= ty && my <= ty + TAB_HEIGHT)
            {
                if (activeTabIndex != STATS_TAB_INDEX)
                {
                    lastReadEntryCount.put(activeTabIndex, entries.size());
                    activeTabIndex = STATS_TAB_INDEX;
                    scrollOffset   = 0;
                    statsLoading   = true;
                    statsLoaded    = false;
                    PacketDistributor.sendToServer(new StatsRequestPacket());
                }
                return true;
            }
        }

        if (tabMetas.size() < ColonyLinkWandLinkableHandler.getMaxBuilders())
        {
            int tx = getGuiX() - TAB_WIDTH, ty = getAddTabY();
            if (mx >= tx && mx <= tx + TAB_WIDTH && my >= ty && my <= ty + TAB_HEIGHT)
            {
                sendClosePacket(-1); // pairing-mode sentinel; removed() will not send a second one
                this.minecraft.setScreen(null);
                return true;
            }
        }

        int dbX = getDeleteBtnX(), dbY = getDeleteBtnY(), dbW = getDeleteBtnW(), dbH = getDeleteBtnH();
        if (!tabMetas.isEmpty() && mx >= dbX && mx <= dbX + dbW && my >= dbY && my <= dbY + dbH)
        {
            PacketDistributor.sendToServer(new RemoveBuilderPacket(activeTabIndex));
            return true;
        }

        int rbX = getRestartBtnX(), rbY = getRestartBtnY(), rbW = getRestartBtnW(), rbH = getRestartBtnH();
        if (mx >= rbX && mx <= rbX + rbW && my >= rbY && my <= rbY + rbH)
        {
            PacketDistributor.sendToServer(new RestartBuilderPacket(builderPos));
            return true;
        }

        // Locate button — in the info panel, only on builder tabs
        if (isBuilderTab() && !isOutOfPower())
        {
            int lbX = getGuiX() + GUI_WIDTH - 6 - LOCATE_BTN_W - 4;
            int lbY = getGuiY() + 22;
            if (mx >= lbX && mx <= lbX + LOCATE_BTN_W && my >= lbY && my <= lbY + LOCATE_BTN_H)
            {
                PacketDistributor.sendToServer(new LocateBuilderPacket(builderPos));
                return true;
            }
        }

        if (isOutOfPower()) return super.mouseClicked(mx, my, btn);

        if (hasWarehouseCard && !redirectorPos.equals(BlockPos.ZERO))
        {
            int sx = getSwitchX(), sy = getSwitchY(), sw2 = getSwitchW(), sh2 = getSwitchH();
            if (mx >= sx && mx <= sx + sw2 && my >= sy && my <= sy + sh2)
            {
                PacketDistributor.sendToServer(new WarehousePriorityPacket(redirectorPos));
                warehousePriority = !warehousePriority;
                return true;
            }
        }

        if (hasWarehouseCard && wareCheckState != WareCheckState.LOADING)
        {
            int bx = getWareCheckBtnX(), by = getWareCheckBtnY(), bw = getWareCheckBtnW(), bh = getWareCheckBtnH();
            if (mx >= bx && mx <= bx + bw && my >= by && my <= by + bh)
            {
                wareCheckState = WareCheckState.LOADING;
                PacketDistributor.sendToServer(new WarehouseCheckPacket(builderPos));
                return true;
            }
        }

        boolean hasReq = builderRequest != null && !builderRequest.stack().isEmpty() && builderRequest.count() > 0;
        if (hasReq)
        {
            // v1.6.4 — Cancel Request button (checked before the main action button).
            // Server re-derives and cancels the priority request; redirectorPos is
            // validated against the player's wand server-side.
            int cbX = getCancelBtnX(), cbY = getCancelBtnY(), cbW = getCancelBtnW(), cbH = getCancelBtnH();
            // v1.6.6 — ignore the click when the button is greyed out (pass-2 line
            // with no formal request); the click falls through to the action button.
            if (builderRequest.cancellable()
                    && mx >= cbX && mx <= cbX + cbW && my >= cbY && my <= cbY + cbH)
            {
                PacketDistributor.sendToServer(new CancelRequestPacket(builderRequest.redirectorPos()));
                return true;
            }

            // v1.6.0 — act on the DISPLAYED status: pending rows are never
            // clickable, and the mode routes Send to the right packet.
            ResourceStatus reqStatus = displayStatus(builderRequest.status(), builderRequest.stack());
            int rbX2 = getReqBtnX(), rbY2 = getReqBtnY(), rbW2 = getReqBtnW(), rbH2 = getReqBtnH();
            if (mx >= rbX2 && mx <= rbX2 + rbW2 && my >= rbY2 && my <= rbY2 + rbH2
                    && isButtonClickable(reqStatus, builderRequest.stack()))
            {
                // v1.4.9 — finished Domum block in the warehouse → deliver straight to the
                // builder (Warehouse -> Builder), bypassing AE2 and the terminal queue.
                // v1.6.0 — BUILDER mode only: in WAREHOUSE mode the courier handles it
                // (the row shows as pending via displayStatus and is not clickable).
                if (!isWarehouseDeliveryMode() && isDomumFinishedInWarehouse(builderRequest.stack()))
                {
                    PacketDistributor.sendToServer(new SendToBuilderPacket(
                            builderRequest.stack(), builderPos, builderRequest.count()));
                    return true;
                }
                switch (reqStatus)
                {
                    case AVAILABLE ->
                    {
                        if (isWarehouseDeliveryMode())
                        {
                            PacketDistributor.sendToServer(new SendToWarehousePacket(
                                    builderRequest.stack(), builderPos, builderRequest.count()));
                            markPendingSent(builderRequest.stack());
                        }
                        else
                            PacketDistributor.sendToServer(new SendToBuilderPacket(
                                    builderRequest.stack(), builderPos, builderRequest.count()));
                    }
                    case CRAFTABLE ->
                    {
                        // Route exactly like the normal request row: use the warehouse craft
                        // path when the components are available in the warehouse snapshot,
                        // otherwise the ME path. The priority row only ever sent
                        // CraftRequestPacket (ME path), so a warehouse-only craft failed with
                        // "missing primary ingredients". isDomum is derived the same way the
                        // normal row's stored flag is (DomumCraftHandler.isDomumItem).
                        boolean craftDomum = DomumCraftHandler.isDomumItem(builderRequest.stack());
                        if (hasWarehouseCraft(builderRequest.stack()))
                            PacketDistributor.sendToServer(new WarehouseCraftPacket(
                                    builderRequest.stack(), builderRequest.count(), craftDomum, builderRequest.redirectorPos()));
                        else
                            PacketDistributor.sendToServer(new CraftRequestPacket(
                                    builderRequest.stack(), builderRequest.count(), craftDomum,
                                    craftDomum ? builderRequest.redirectorPos() : BlockPos.ZERO, ResourceStatus.CRAFTABLE));
                    }
                    case MISSING ->
                    {
                        PacketDistributor.sendToServer(new CraftRequestPacket(
                                builderRequest.stack(), builderRequest.count(),
                                DomumCraftHandler.isDomumItem(builderRequest.stack()), builderRequest.redirectorPos(), ResourceStatus.MISSING));
                    }
                    case NO_PATTERN ->
                    {
                        if (DomumCraftHandler.isDomumItem(builderRequest.stack()))
                            PacketDistributor.sendToServer(new DomumQueuePacket(
                                    builderRequest.redirectorPos(), builderRequest.stack()));
                    }
                    default -> {}
                }
                return true;
            }
        }

        int caX = getCraftAllBtnX(), caY = getCraftAllBtnY(), caW = getCraftAllBtnW(), caH = getCraftAllBtnH();
        if (mx >= caX && mx <= caX + caW && my >= caY && my <= caY + caH)
        {
            if (craftInProgress)
            {
                // #8: already in progress → informative message, no double send
                net.minecraft.client.Minecraft.getInstance().player.sendSystemMessage(
                        net.minecraft.network.chat.Component.translatable("colonylink.screen.msg.craft_in_progress", craftInProgressCount));
                return true;
            }

            if (!hasCraftableItems()) return true;

            // #8: launch the crafts and switch to "in progress" mode
            List<ItemStack> toCraft = new ArrayList<>();
            List<Integer> counts = new ArrayList<>();
            int submitted = 0;
            for (var entry : entries)
            {
                if (entry.status() == ResourceStatus.CRAFTABLE)
                {
                    submitted++;
                    // Domum CRAFTABLE since v1.4.3: same path as standard (ICraftingProvider)
                    toCraft.add(entry.stack());
                    counts.add(entry.realCount());
                }
                else if (entry.status() == ResourceStatus.MISSING)
                {
                    submitted++;
                    PacketDistributor.sendToServer(new CraftRequestPacket(
                            entry.stack(), entry.realCount(), true, entry.redirectorPos(), ResourceStatus.MISSING));
                }
            }
            if (!toCraft.isEmpty())
                PacketDistributor.sendToServer(new CraftAllRequestPacket(toCraft, counts));

            // Switch to "in progress" mode if at least 1 craft was sent
            if (submitted > 0)
            {
                craftInProgress      = true;
                craftInProgressCount = submitted;
            }
            return true;
        }

        int saX = getSendAllBtnX(), saY = getSendAllBtnY(), saW = getSendAllBtnW(), saH = getSendAllBtnH();
        if (mx >= saX && mx <= saX + saW && my >= saY && my <= saY + saH && hasSendableItems())
        {
            boolean warehouseAll = isWarehouseDeliveryMode();
            // v1.4.9 — Send All also includes finished Domum blocks detected in the warehouse
            // (besides AVAILABLE items in ME). Everything goes via SendToBuilderPacket; the
            // server picks the source (ME / warehouse) per the priority toggle.
            // v1.6.0 — WAREHOUSE mode: only AVAILABLE rows (displayed status — pending rows
            // excluded), via SendToWarehousePacket.
            boolean prioritySent = builderRequest != null && !builderRequest.stack().isEmpty()
                    && (displayStatus(builderRequest.status(), builderRequest.stack()) == ResourceStatus.AVAILABLE
                    || (!warehouseAll && isDomumFinishedInWarehouse(builderRequest.stack())));
            if (prioritySent)
            {
                if (warehouseAll)
                {
                    PacketDistributor.sendToServer(new SendToWarehousePacket(
                            builderRequest.stack(), builderPos, builderRequest.count()));
                    markPendingSent(builderRequest.stack());
                }
                else
                    PacketDistributor.sendToServer(new SendToBuilderPacket(
                            builderRequest.stack(), builderPos, builderRequest.count()));
            }
            for (var entry : entries)
            {
                if (warehouseAll)
                {
                    if (displayStatus(entry.status(), entry.stack()) != ResourceStatus.AVAILABLE) continue;
                }
                else if (entry.status() != ResourceStatus.AVAILABLE
                        && !isDomumFinishedInWarehouse(entry.stack())) continue;
                // Avoid double send when the priority request is also in the list
                if (prioritySent
                        && ItemStack.isSameItemSameComponents(entry.stack(), builderRequest.stack()))
                    continue;
                if (warehouseAll)
                {
                    PacketDistributor.sendToServer(new SendToWarehousePacket(
                            entry.stack(), builderPos, entry.realCount()));
                    markPendingSent(entry.stack());
                }
                else
                    PacketDistributor.sendToServer(new SendToBuilderPacket(
                            entry.stack(), builderPos, entry.realCount()));
            }
            return true;
        }

        // #12: Send/Craft button + Package slot click on the Citizens tab
        if (activeTabIndex == CITIZENS_TAB_INDEX)
        {
            // Click on the Package slot (top-right of the Citizens header)
            int pkgSlotX = getGuiX() + GUI_WIDTH - 26, pkgSlotY = getGuiY() + 26;
            if (mx >= pkgSlotX && mx <= pkgSlotX + 18 && my >= pkgSlotY && my <= pkgSlotY + 18)
            {
                // Load packages from the inventory
                PacketDistributor.sendToServer(new PackageLoadPacket());
                return true;
            }

            // 1.7.0-beta.3 — the clickable buttons are the ones citizenButton() marks so:
            // Send, Craft, and a re-clickable "Sent ↺" (which already require the
            // Warehouse card). Delivering / In Warehouse / Colony crafting / Crafting… /
            // In WH (N) are display-only.
            {
                int listW = GUI_WIDTH - 26, listY = getListStartY();
                int btnH = 14;
                int vis2 = Math.min(MAX_VISIBLE, citizenEntries.size() - scrollOffset);
                for (int i = 0; i < vis2; i++)
                {
                    var ce  = citizenEntries.get(i + scrollOffset);
                    CitBtn citBtn = citizenButton(ce);
                    if (citBtn == null || !citBtn.clickable()) continue;
                    int btnW = citizenButtonWidth(citBtn);
                    int ey  = listY + i * ENTRY_HEIGHT;
                    int btnX = getGuiX() + 7 + listW - btnW - 2;
                    int btnY = ey + (ENTRY_HEIGHT - btnH) / 2;
                    if (mx >= btnX && mx <= btnX + btnW && my >= btnY && my <= btnY + btnH)
                    {
                        if (citizenPackageCount <= 0)
                        {
                            if (this.minecraft != null && this.minecraft.player != null)
                                this.minecraft.player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("colonylink.screen.msg.no_packages"));
                            return true;
                        }
                        String itemLabel = "§f" + ce.count() + "x " + stripItemName(ce.stack().getDisplayName().getString());

                        if (citBtn.kind() != CitBtnKind.SENT)
                        {
                            // Premier clic : Send ou Craft. The SERVER records the mark on
                            // success (PackageTokenPacket → CitizenRequestMemory). The
                            // optimistic overlay (by request UUID) shows it during the
                            // round-trip and expires if the server rejected (no mark).
                            boolean craft = citBtn.kind() == CitBtnKind.CRAFT;
                            (craft ? optimisticCitizenCraftUntil : optimisticCitizenSentUntil)
                                    .put(ce.requestId(), System.currentTimeMillis() + PENDING_HOLD_MS);
                            PacketDistributor.sendToServer(new PackageTokenPacket(
                                    ce.stack(), ce.count(), redirectorPos, craft, ce.requestId()));
                            citizenPackageCount = Math.max(0, citizenPackageCount - 1);
                        }
                        else
                        {
                            // Re-click on "Sent ↺": craft + send in sequence (2 packages)
                            if (citizenPackageCount < 2)
                            {
                                if (this.minecraft != null && this.minecraft.player != null)
                                    this.minecraft.player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("colonylink.screen.msg.need_2pkg", citizenPackageCount));
                                return true;
                            }
                            // Craft first
                            PacketDistributor.sendToServer(new PackageTokenPacket(
                                    ce.stack(), ce.count(), redirectorPos, true, ce.requestId()));
                            // Then send
                            PacketDistributor.sendToServer(new PackageTokenPacket(
                                    ce.stack(), ce.count(), redirectorPos, false, ce.requestId()));
                            citizenPackageCount = Math.max(0, citizenPackageCount - 2);
                            if (this.minecraft != null && this.minecraft.player != null)
                                this.minecraft.player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("colonylink.screen.msg.resending", itemLabel));
                        }
                        return true;
                    }
                }
            }
            return true; // click in the citizens zone → always consumed
        }

        int vis = Math.min(MAX_VISIBLE, entries.size() - scrollOffset);
        for (int i = 0; i < vis; i++)
        {
            int idx = i + scrollOffset;
            var entry = entries.get(idx);
            // v1.6.0 — act on the DISPLAYED status: pending rows are never clickable.
            ResourceStatus ds = displayStatus(entry.status(), entry.stack());
            if (!isButtonClickable(ds, entry.stack())) continue;
            int[] b = new int[4]; getBtnBounds(i, b);
            if (mx >= b[0] && mx <= b[0] + b[2] && my >= b[1] && my <= b[1] + b[3])
            {
                // v1.4.9 — finished Domum block in the warehouse → deliver straight to the
                // builder (Warehouse -> Builder), bypassing AE2 and the terminal queue.
                // v1.6.0 — BUILDER mode only (in WAREHOUSE mode the courier handles it).
                if (!isWarehouseDeliveryMode() && isDomumFinishedInWarehouse(entry.stack()))
                {
                    PacketDistributor.sendToServer(new SendToBuilderPacket(
                            entry.stack(), builderPos, entry.realCount()));
                    return true;
                }
                if (ds == ResourceStatus.CRAFTABLE && entry.isDomum())
                {
                    // Domum CRAFTABLE = raw components are in stock (AE2 or RS2)
                    // → virtual craft via WarehouseCraftPacket (extract components → redirector buffer)
                    // No CraftRequestPacket/RS since there is no pattern for DO items
                    if (hasWarehouseCraft(entry.stack()))
                        PacketDistributor.sendToServer(new WarehouseCraftPacket(
                                entry.stack(), entry.realCount(), true, entry.redirectorPos()));
                    else
                        // Fallback: components not in WH but in network → direct craft
                        PacketDistributor.sendToServer(new CraftRequestPacket(
                                entry.stack(), entry.realCount(), true, entry.redirectorPos(), ResourceStatus.CRAFTABLE));
                }
                else if (ds == ResourceStatus.CRAFTABLE)
                {
                    PacketDistributor.sendToServer(hasWarehouseCraft(entry.stack())
                            ? new WarehouseCraftPacket(entry.stack(), entry.realCount(), false, entry.redirectorPos())
                            : new CraftRequestPacket(entry.stack(), entry.realCount(), false, BlockPos.ZERO, ResourceStatus.CRAFTABLE));
                }
                else if (ds == ResourceStatus.MISSING)
                {
                    PacketDistributor.sendToServer(new CraftRequestPacket(
                            entry.stack(), entry.realCount(), true, entry.redirectorPos(), ResourceStatus.MISSING));
                }
                else if (ds == ResourceStatus.AVAILABLE)
                {
                    // v1.6.0 — delivery target routing (server-decreed mode).
                    if (isWarehouseDeliveryMode())
                    {
                        PacketDistributor.sendToServer(new SendToWarehousePacket(
                                entry.stack(), builderPos, entry.realCount()));
                        markPendingSent(entry.stack());
                    }
                    else
                        PacketDistributor.sendToServer(new SendToBuilderPacket(
                                entry.stack(), builderPos, entry.realCount()));
                }
                else if (ds == ResourceStatus.NO_PATTERN)
                {
                    if (entry.isDomum())
                    {
                        // v1.4.8 — Send the Domum item to the terminal queue
                        PacketDistributor.sendToServer(new DomumQueuePacket(
                                entry.redirectorPos(), entry.stack()));
                    }
                    else
                    {
                        var we = getWarehouseEntry(entry.stack());
                        if (we != null && (we.inWarehouse() > 0 || we.viaCraft() > 0))
                            PacketDistributor.sendToServer(new WarehouseCraftPacket(
                                    entry.stack(), entry.realCount(), entry.isDomum(), entry.redirectorPos()));
                    }
                }
                return true;
            }
        }

        if (entries.size() > MAX_VISIBLE)
        {
            int sbX = getScrollbarX(), ty = getThumbY(), th = getThumbHeight();
            if (mx >= sbX && mx <= sbX + SCROLLBAR_WIDTH && my >= ty && my <= ty + th)
            {
                isDraggingScrollbar = true; dragStartY = my; dragStartOffset = scrollOffset;
                return true;
            }
        }

        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseReleased(double rawMx, double rawMy, int btn)
    {
        if (isDraggingGui) { isDraggingGui = false; return true; }
        isDraggingScrollbar = false;
        return super.mouseReleased(rawMx, rawMy, btn);
    }

    @Override
    public boolean mouseDragged(double rawMx, double rawMy, int btn, double dx, double dy)
    {
        double mx = toGuiX(rawMx), my = toGuiY(rawMy);
        if (isDraggingGui)
        {
            dragOffsetX = guiDragOriginX + (int)(mx - guiDragStartX);
            dragOffsetY = guiDragOriginY + (int)(my - guiDragStartY);
            // Constraint: keep the GUI within the screen bounds
            int guiX = (this.width - GUI_WIDTH - TAB_WIDTH) / 2 + TAB_WIDTH + dragOffsetX;
            int guiY = (this.height - GUI_HEIGHT) / 2 + dragOffsetY;
            if (guiX < 0) dragOffsetX -= guiX;
            if (guiY < 0) dragOffsetY -= guiY;
            if (guiX + GUI_WIDTH > this.width)  dragOffsetX -= (guiX + GUI_WIDTH - this.width);
            if (guiY + GUI_HEIGHT > this.height) dragOffsetY -= (guiY + GUI_HEIGHT - this.height);
            return true;
        }
        if (isDraggingScrollbar && entries.size() > MAX_VISIBLE)
        {
            int max = entries.size() - MAX_VISIBLE;
            scrollOffset = Math.max(0, Math.min(max,
                    (int)(dragStartOffset + (my - dragStartY) / (getScrollbarHeight() - getThumbHeight()) * max)));
            return true;
        }
        return super.mouseDragged(rawMx, rawMy, btn, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double rawMx, double rawMy, double sx, double sy)
    {
        // #12: scroll adapted to the active tab
        int listSize = isStatsTab() ? 0
                : (activeTabIndex == CITIZENS_TAB_INDEX) ? citizenEntries.size() : entries.size();
        int max = listSize - MAX_VISIBLE;
        if (sy < 0 && scrollOffset < max) scrollOffset++;
        else if (sy > 0 && scrollOffset > 0) scrollOffset--;
        return true;
    }

    /**
     * Allows hotbar access while the GUI is open. Keys 1-9 swap the selected hotbar
     * slot normally; E closes the GUI (standard behaviour).
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        // Hotbar keys 1-9 (GLFW: 49-57)
        if (keyCode >= 49 && keyCode <= 57)
        {
            if (this.minecraft != null && this.minecraft.player != null)
                this.minecraft.player.getInventory().selected = keyCode - 49;
            return true;
        }
        // Delegate the rest (Escape closes, etc.)
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    /**
     * Marks a tab as unread (new requests detected). Called by ColonyLinkServerTicker
     * when the entry count changes on a tab that is not active in the open GUI.
     */
    /**
     * Marks a tab as unread ONLY if the player has not already seen it with this
     * request count (or more). lastReadEntryCount[i] = last count the player saw on
     * tab i. If the server reports count > lastRead → new requests → badge.
     */
    public static void markTabUnread(int tabIndex, int serverCount)
    {
        int lastRead = lastReadEntryCount.getOrDefault(tabIndex, -1);
        if (serverCount > 0 && serverCount > lastRead)
        {
            unreadTabs.add(tabIndex);
            UNREAD_TAB_COUNT = unreadTabs.size();
        }
    }

    /** Compat — called without a count (marks unconditionally). */
    public static void markTabUnread(int tabIndex)
    {
        unreadTabs.add(tabIndex);
        UNREAD_TAB_COUNT = unreadTabs.size();
    }

    /** Resets all unread marks (e.g. on disconnect). */
    public static void clearAllUnread()
    {
        unreadTabs.clear();
        lastReadEntryCount.clear();
        UNREAD_TAB_COUNT = 0;
    }

    public BlockPos getBuilderPos() { return builderPos; }
}