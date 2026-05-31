package com.colonylink.colonylink;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI principal de la ColonyLink Wand — v1.1.3.
 *
 * RF : affiché uniquement via barre de durabilité item + tooltip hotbar.
 * Pas de barre RF dans ce GUI.
 *
 * Si rfStored == 0 : le contenu est remplacé par "Out of Power".
 * Les boutons Send/Craft sont grisés (isButtonClickable retourne false).
 */
public class ColonyLinkScreen extends Screen
{
    // ── #5 : Badge hotbar — expose le compte de tabs non lues pour le renderer ──
    // Mis à jour à chaque applyPacket().
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

    // ── #12 : index spécial de la tab Citizens ───────────────────────────────
    private static final int CITIZENS_TAB_INDEX = Integer.MAX_VALUE;

    // ── État ──────────────────────────────────────────────────────────────────
    private List<ColonyLinkPacket.BuilderTabMeta> tabMetas = new ArrayList<>();
    private int activeTabIndex = 0;

    // ── #12 : données de la tab Citizens ─────────────────────────────────────
    private List<CitizensPacket.CitizenRequestEntry> citizenEntries = new ArrayList<>();
    private boolean citizensLoading = false;
    private int citizenPackageCount = 0; // count synced depuis serveur
    // Items déjà envoyés cette session — visuellement grisés mais recliquables
    private final java.util.Set<String> sentCitizenRequests = new java.util.HashSet<>();

    private List<ColonyLinkPacket.ResourceEntry> entries = new ArrayList<>();
    private BlockPos builderPos      = BlockPos.ZERO;
    private String   builderName     = "";
    private String   buildingName    = "";
    private String   workerStatus    = "";
    private String   workerIdleReason = ""; // v1.1.3 — raison IDLE
    private int      availableCpus   = 0;
    private String   redirectorState = "N/A";
    private ColonyLinkPacket.BuilderRequest builderRequest = ColonyLinkPacket.BuilderRequest.NONE;
    private boolean  hasWarehouseCard  = false;
    private boolean  warehousePriority = false;
    private BlockPos redirectorPos   = BlockPos.ZERO;

    // v1.1.3 — RF reçu du serveur (pour éventuels usages futurs, non affiché ici)
    private long rfStored = 0L;
    private long rfMax    = 1_600_000L;

    private int     scrollOffset        = 0;
    private boolean isDraggingScrollbar = false;
    private double  dragStartY          = 0;
    private int     dragStartOffset     = 0;

    // ── #5/#6 : tabs non lues ────────────────────────────────────────────────
    // unreadTabs : index des tabs avec nouvelles requêtes non vues.
    // Se remplit quand une tab inactive reçoit des entrées.
    // Se vide quand le joueur clique sur la tab (passage au premier plan).
    // Static : persiste entre les ouvertures du GUI
    private static final java.util.Set<Integer> unreadTabs = new java.util.HashSet<>();
    private static final java.util.Map<Integer, Integer> lastReadEntryCount = new java.util.HashMap<>();

    // ── Draggable GUI ─────────────────────────────────────────────────────────
    // dragOffsetX/Y = décalage par rapport à la position centrée par défaut.
    // Initialisé à 0 → le GUI s'ouvre centré, puis peut être déplacé.
    private int     dragOffsetX    = 0;
    private int     dragOffsetY    = 0;
    private boolean isDraggingGui  = false;
    private double  guiDragStartX  = 0;
    private double  guiDragStartY  = 0;
    private int     guiDragOriginX = 0;
    private int     guiDragOriginY = 0;

    private WarehouseResultPacket warehouseSnapshot       = null;
    private long warehouseSnapshotReceivedMs              = 0;
    private static final long SNAPSHOT_VALIDITY_MS        = 20_000L; // fallback si config non chargée

    private long getSnapshotValidityMs()
    {
        return ColonyLinkConfig.WAREHOUSE_SNAPSHOT_VALIDITY_TICKS.get() * 50L; // ticks → ms
    }
    private enum WareCheckState { IDLE, LOADING, DONE }
    private WareCheckState wareCheckState = WareCheckState.IDLE;

    // ── #8 : état "Craft All en cours" ───────────────────────────────────────
    private boolean craftInProgress      = false;
    private int     craftInProgressCount = 0;   // nb d'items envoyés au craft

    // ─────────────────────────────────────────────────────────────────────────

    public ColonyLinkScreen(ColonyLinkPacket packet)
    {
        super(Component.literal("Colony Link"));
        applyPacket(packet);
    }

    // ── Anti-flicker du statut de craft (client only) ─────────────────────────
    //
    // AE2 cs.isRequesting() peut renvoyer true/false par intermittence pendant un
    // craft, ce qui faisait osciller le bouton entre "Craft" (CRAFTABLE) et
    // "Crafting..." (CRAFTING) d'un cycle de ticker à l'autre. On lisse côté client :
    // dès qu'un item est vu CRAFTING, on maintient l'affichage CRAFTING pendant une
    // courte fenêtre, même si une mise à jour suivante le repasse CRAFTABLE.
    // AVAILABLE / NO_PATTERN / MISSING restent prioritaires (le craft est vraiment fini).
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
        craftHoldUntil.values().removeIf(until -> until <= now); // purge des holds expirés
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
                r.stack(), r.count(), disp, r.redirectorPos(), r.tooltipLines());
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
        // #12 : ne pas écraser activeTabIndex si on est sur la tab Citizens
        if (this.activeTabIndex != CITIZENS_TAB_INDEX)
            this.activeTabIndex = packet.activeTabIndex();
        this.rfStored       = packet.rfStored();
        this.rfMax          = packet.rfMax() > 0 ? packet.rfMax() : 1_600_000L;

        // #8 : un refresh serveur signifie que les crafts ont été traités → reset
        this.craftInProgress = false;

        if (!entries.isEmpty() && !entries.get(0).redirectorPos().equals(BlockPos.ZERO))
            this.redirectorPos = entries.get(0).redirectorPos();
        // Fallback : lire depuis la wand NBT côté client si entries vide (ex. tab Citizens)
        if (this.redirectorPos.equals(BlockPos.ZERO) && this.minecraft != null && this.minecraft.player != null)
        {
            for (net.minecraft.world.item.ItemStack s : this.minecraft.player.getInventory().items)
            {
                if (s.getItem() instanceof ColonyLinkWand)
                {
                    BlockPos wandRedir = ColonyLinkWandLinkableHandler.getActiveRedirectorPos(s);
                    if (wandRedir != null && !wandRedir.equals(BlockPos.ZERO))
                    {
                        this.redirectorPos = wandRedir;
                        break;
                    }
                }
            }
        }

        int maxOffset = Math.max(0, entries.size() - MAX_VISIBLE);
        if (scrollOffset > maxOffset) scrollOffset = maxOffset;

        // #5 : gestion tabs non lues
        // Logique : une tab est marquée non lue quand on la quitte avec des entrées,
        // et lue quand on y revient. Le serveur ne nous envoie des données que pour
        // la tab active — donc on détecte les nouvelles requêtes au moment du switch.

        // Si on reçoit un packet pour la tab active et qu'elle a des entrées,
        // et que c'est différent du dernier compte connu → potentiellement des nouveautés
        int prevCount = lastReadEntryCount.getOrDefault(this.activeTabIndex, 0);
        int newCount  = this.entries.size();

        // La tab active est toujours lue
        lastReadEntryCount.put(this.activeTabIndex, newCount);
        unreadTabs.remove(this.activeTabIndex);
        UNREAD_TAB_COUNT = unreadTabs.size();
    }

    public void updateFromPacket(ColonyLinkPacket packet)
    {
        applyPacket(packet);
        if (tabMetas.isEmpty() && this.minecraft != null)
            this.minecraft.setScreen(null);
    }

    // #12 : appelé par CitizensPacket.handle() quand le serveur répond
    public void updateCitizens(CitizensPacket packet)
    {
        this.citizenEntries = packet.entries();
        this.citizensLoading = false;
        // Reconstruire les clés actives pour pruner les requests résolues
        java.util.Set<String> activeKeys = new java.util.HashSet<>();
        for (CitizensPacket.CitizenRequestEntry ce : packet.entries())
            activeKeys.add(sentKey(ce));
        // Pruner la NBT wand + resynchroniser le cache
        net.minecraft.world.item.ItemStack wand = getClientWand();
        if (!wand.isEmpty())
        {
            ColonyLinkWandLinkableHandler.pruneSentRequestKeys(wand, activeKeys);
            this.sentCitizenRequests.clear();
            this.sentCitizenRequests.addAll(ColonyLinkWandLinkableHandler.getSentRequestKeys(wand));
        }
    }

    public void updatePackageCount(int count)
    {
        this.citizenPackageCount = count;
    }

    private net.minecraft.world.item.ItemStack getClientWand()
    {
        if (this.minecraft == null || this.minecraft.player == null) return net.minecraft.world.item.ItemStack.EMPTY;
        for (net.minecraft.world.item.ItemStack s : this.minecraft.player.getInventory().items)
            if (s.getItem() instanceof ColonyLinkWand) return s;
        return net.minecraft.world.item.ItemStack.EMPTY;
    }

    private static String stripItemName(String name)
    {
        return (name.startsWith("[") && name.endsWith("]")) ? name.substring(1, name.length() - 1) : name;
    }

    private static String sentKey(CitizensPacket.CitizenRequestEntry ce)
    {
        String itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM
                .getKey(ce.stack().getItem()).toString();
        return ce.citizenName() + "|" + itemId;
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

    // ── RF helpers ────────────────────────────────────────────────────────────
    private boolean isOutOfPower() { return rfStored <= 0; }

    // ── Coordonnées ───────────────────────────────────────────────────────────
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

    // Bouton config — juste à gauche du bouton Restart, dans la barre de titre
    private static final int CFG_BTN_W = 16;
    private static final int CFG_BTN_H = 14;
    private int getCfgBtnX() { return getRestartBtnX() - CFG_BTN_W - 2; }
    private int getCfgBtnY() { return getRestartBtnY(); }

    // #12 : tab Citizens — même colonne que les builders, mais tout en bas avec un grand écart
    private int getCitizenTabX()  { return getGuiX() - TAB_WIDTH + (activeTabIndex == CITIZENS_TAB_INDEX ? TAB_OVERLAP : 0); }
    private int getCitizenTabY()  { return getGuiY() + GUI_HEIGHT - TAB_HEIGHT - 8; }

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

    // Locate button dimensions (position calculée dynamiquement dans drawInfoPanel / mouseClicked)
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

    // ── Lifecycle ─────────────────────────────────────────────────────────────
    @Override
    protected void init()
    {
        super.init();
        int serverTabIndex = (activeTabIndex == CITIZENS_TAB_INDEX) ? 0 : activeTabIndex;
        PacketDistributor.sendToServer(new GuiStatePacket(true, builderPos, serverTabIndex));
        // Lire le count packages + sent keys depuis la wand NBT côté client
        if (this.minecraft != null && this.minecraft.player != null)
        {
            for (net.minecraft.world.item.ItemStack s : this.minecraft.player.getInventory().items)
            {
                if (s.getItem() instanceof ColonyLinkWand)
                {
                    this.citizenPackageCount = ColonyLinkWandLinkableHandler.getCitizenPackages(s);
                    this.sentCitizenRequests.clear();
                    this.sentCitizenRequests.addAll(ColonyLinkWandLinkableHandler.getSentRequestKeys(s));
                    break;
                }
            }
        }
    }

    /**
     * Transforme une coordonnée X écran → coordonnée dans l'espace du GUI scalé.
     * Quand scale=1, retourne la valeur inchangée.
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
     * Transformation inverse de toGuiX/toGuiY : convertit une coordonnée logique GUI
     * vers l'espace écran réel (post-pose). Indispensable pour enableScissor(), qui
     * applique uniquement le guiScale de la fenêtre et IGNORE la matrice pose() —
     * donc un scissor calculé en coords logiques tombe à côté dès que scale != 1.0.
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

    /** Zone handle drag : entre le bouton Unlink (fin) et le bouton Restart (début), dans la barre de titre. */
    private boolean isInDragHandle(double mx, double my)
    {
        int x = getGuiX(), y = getGuiY();
        int handleX1 = x + getDeleteBtnW() + 12; // juste après Unlink
        // Exclut zone bouton cfg (à gauche de Restart) + Restart lui-même
        int handleX2 = getCfgBtnX() - 4;
        int handleY1 = y + 2;
        int handleY2 = y + 20;
        return mx >= handleX1 && mx <= handleX2 && my >= handleY1 && my <= handleY2;
    }

    @Override
    public void onClose()
    {
        PacketDistributor.sendToServer(new GuiStatePacket(false, builderPos, activeTabIndex));
        super.onClose();
    }

    // ── Helpers boutons ───────────────────────────────────────────────────────
    private int getButtonColor(ResourceStatus status)
    {
        return switch (status) {
            case AVAILABLE  -> 0xFF004488;
            case CRAFTABLE  -> 0xFF005500;
            case NO_PATTERN -> 0xFF550000;
            case CRAFTING   -> 0xFF885500;
            case MISSING    -> 0xFF5D3A00;
        };
    }

    private int getButtonHoverColor(ResourceStatus status)
    {
        return switch (status) {
            case AVAILABLE  -> 0xFF0066CC;
            case CRAFTABLE  -> 0xFF007700;
            case NO_PATTERN -> 0xFF660000;
            case CRAFTING   -> 0xFF885500;
            case MISSING    -> 0xFF8B5E00;
        };
    }

    private int getButtonTextColor(ResourceStatus status)
    {
        return switch (status) {
            case AVAILABLE  -> 0x4488FF;
            case CRAFTABLE  -> 0x00FF00;
            case NO_PATTERN -> 0xFF4444;
            case CRAFTING   -> 0xFFAA00;
            case MISSING    -> 0xFFCC66;
        };
    }

    private String getButtonText(ResourceStatus status)
    {
        return switch (status) {
            case AVAILABLE  -> "Send";
            case CRAFTABLE  -> "Craft";
            case NO_PATTERN -> "No Pattern";
            case CRAFTING   -> "Crafting...";
            case MISSING    -> "Missing";
        };
    }

    private String getRequestButtonText(ResourceStatus status)
    {
        return switch (status) {
            case AVAILABLE  -> "Fulfill";
            case CRAFTABLE  -> "Craft";
            case NO_PATTERN -> "No Pattern";
            case CRAFTING   -> "Crafting...";
            case MISSING    -> "Missing";
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
            // Items Domum sans pattern → toujours cliquable (envoie dans la queue terminal)
            if (DomumCraftHandler.isDomumItem(stack)) return true;
            WarehouseResultPacket.WarehouseEntry we = getWarehouseEntry(stack);
            return we != null && (we.inWarehouse() > 0 || we.viaCraft() > 0);
        }
        return false;
    }

    private String getButtonTextWithWarehouse(ResourceStatus status, ItemStack stack)
    {
        if (status == ResourceStatus.NO_PATTERN)
        {
            WarehouseResultPacket.WarehouseEntry we = getWarehouseEntry(stack);
            if (we != null && we.inWarehouse() > 0) return "Send (WH)";
            if (we != null && we.viaCraft() > 0)    return "Craft (WH)";
        }
        return getButtonText(status);
    }

    private int getButtonColorWithWarehouse(ResourceStatus status, ItemStack stack, boolean hovered)
    {
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

    private boolean redirectorReady()
    {
        return redirectorState.equals("LINKED") || redirectorState.equals("STANDBY");
    }

    private int getWorkerStatusColor()
    {
        if (workerStatus == null) return 0x888888;
        if (workerStatus.equals("Working"))                                    return 0x00FF00;
        if (workerStatus.equals("Idle"))                                       return 0xFFFF00;
        if (workerStatus.equals("Hungry"))                                     return 0xFFAA00;
        if (workerStatus.equals("Sleeping"))                                   return 0x4488FF;
        if (workerStatus.equals("Bad weather"))                                return 0x88AACC;
        if (workerStatus.equals("Sick"))                                       return 0xFF4444;
        if (workerStatus.equals("Mourning"))                                   return 0x888888;
        if (workerStatus.equals("Raided!"))                                    return 0xFF0000;
        if (workerStatus.equals("No home"))                                    return 0xFFCC44;
        // Fallback pour statuts traduits inconnus
        if (workerStatus.toLowerCase().contains("work"))                       return 0x00FF00;
        if (workerStatus.toLowerCase().contains("sleep"))                      return 0x4488FF;
        if (workerStatus.toLowerCase().contains("eat") || workerStatus.toLowerCase().contains("food")) return 0xFFAA00;
        if (workerStatus.toLowerCase().contains("sick"))                       return 0xFF4444;
        if (workerStatus.toLowerCase().contains("idle"))                       return 0xFFFF00;
        return 0xCCCCCC;
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

    // ── Tabs ──────────────────────────────────────────────────────────────────
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

            if (active)
            {
                // Tab active : couleur fond config légèrement éclaircie
                bg = lighten(_tabCfg.bgColor, 1.1f) | 0xFF000000;
                bl = _tabCfg.border();
                bd = _tabCfg.borderShadow();
            }
            else if (!meta.hasRedirector())
            {
                // Pas de redirecteur → brun
                bg = 0xFF5A3A10; bl = 0xFF886633; bd = 0xFF221500;
            }
            else if (hasUnread)
            {
                // #6 : tab inactive avec nouvelles requêtes non lues → pastel orange
                bg = 0xFF7A4A1A; bl = 0xFFCC8833; bd = 0xFF3A2008;
            }
            else
            {
                // Tab inactive normale → gris
                bg = 0xFF4A4A4A; bl = 0xFF6B6B6B; bd = 0xFF222222;
            }

            g.fill(tx, ty, tx + tw, ty + th, bg);
            g.fill(tx, ty, tx + tw, ty + 1, bl);
            g.fill(tx, ty, tx + 1, ty + th, bl);
            g.fill(tx, ty + th - 1, tx + tw, ty + th, bd);
            if (!active) g.fill(tx + tw - 1, ty, tx + tw, ty + th, bd);
            drawGearIcon(g, tx + (tw - 10) / 2, ty + (th - 10) / 2, active, meta.hasRedirector());

            if (mx >= tx && mx <= tx + tw && my >= ty && my <= ty + th)
            {
                tip.clear();
                tip.add(Component.literal("§f" + meta.builderName()));
                tip.add(Component.literal("§7" + meta.buildingLabel()));
                tip.add(Component.literal("§8@ " + meta.builderPos().toShortString()));
                tip.add(meta.hasRedirector()
                        ? Component.literal("§aRedirector linked")
                        : Component.literal("§e⚠ No Redirector linked"));
            }
        }

        if (tabMetas.size() < ColonyLinkWandLinkableHandler.getMaxBuilders())
        {
            int tx = getGuiX() - TAB_WIDTH, ty = getAddTabY(), tw = TAB_WIDTH, th = TAB_HEIGHT;
            boolean hov = mx >= tx && mx <= tx + tw && my >= ty && my <= ty + th;
            g.fill(tx, ty, tx + tw, ty + th, hov ? 0xFF226622 : 0xFF1A4A1A);
            g.fill(tx, ty, tx + tw, ty + 1, 0xFF44AA44);
            g.fill(tx, ty, tx + 1, ty + th, 0xFF44AA44);
            g.fill(tx, ty + th - 1, tx + tw, ty + th, 0xFF113311);
            g.fill(tx + tw - 1, ty, tx + tw, ty + th, 0xFF113311);
            int cx = tx + tw / 2, cy = ty + th / 2;
            g.fill(cx - 3, cy - 1, cx + 4, cy + 2, 0xFF44FF44);
            g.fill(cx - 1, cy - 3, cx + 2, cy + 4, 0xFF44FF44);
            if (hov)
            {
                tip.clear();
                tip.add(Component.literal("§aAdd a new builder"));
                tip.add(Component.literal("§7Click to start pairing mode"));
            }
        }

        // #12 : tab Citizens — même style que les tabs builders, même colonne
        {
            int tx = getCitizenTabX(), ty = getCitizenTabY(), tw = TAB_WIDTH, th = TAB_HEIGHT;
            boolean active = (activeTabIndex == CITIZENS_TAB_INDEX);
            boolean hov    = mx >= tx && mx <= tx + tw && my >= ty && my <= ty + th;

            // Même mécanique d'overlap que les tabs builders (vers la droite quand active)
            int drawTx = active ? tx + TAB_OVERLAP : tx;

            ColonyLinkGuiConfig _tabCfg = ColonyLinkGuiConfig.get();
            int bg, bl, bd;
            if (active)
            {
                bg = lighten(_tabCfg.bgColor, 1.1f) | 0xFF000000;
                bl = _tabCfg.border();
                bd = _tabCfg.borderShadow();
            }
            else
            {
                // Même couleur que les tabs builders inactives normales
                bg = hov ? 0xFF555555 : 0xFF4A4A4A;
                bl = 0xFF6B6B6B;
                bd = 0xFF222222;
            }

            g.fill(drawTx, ty, drawTx + tw, ty + th, bg);
            g.fill(drawTx, ty, drawTx + tw, ty + 1, bl);
            g.fill(drawTx, ty, drawTx + 1, ty + th, bl);
            g.fill(drawTx, ty + th - 1, drawTx + tw, ty + th, bd);
            if (!active) g.fill(drawTx + tw - 1, ty, drawTx + tw, ty + th, bd);

            // Icône bonhomme pixel-art centrée
            int cx = drawTx + tw / 2, cy = ty + th / 2 - 1;
            int col = active ? 0xFFEEEEEE : (hov ? 0xFFCCCCCC : 0xFFAAAAAA);
            g.fill(cx - 2, cy - 5, cx + 3, cy - 1, col); // tête
            g.fill(cx - 3, cy - 1, cx + 4, cy + 3, col); // corps
            g.fill(cx - 3, cy + 3, cx - 1, cy + 6, col); // jambe gauche
            g.fill(cx + 1, cy + 3, cx + 4, cy + 6, col); // jambe droite

            if (hov)
            {
                tip.clear();
                tip.add(Component.literal("§fCitizens"));
                tip.add(Component.literal("§7All open requests from non-builder citizens"));
                if (!citizenEntries.isEmpty())
                    tip.add(Component.literal("§7" + citizenEntries.size() + " request" + (citizenEntries.size() != 1 ? "s" : "")));
            }
        }
    }

    /** Éclaircit une couleur ARGB par un facteur (ex: 1.3f). */
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

    /** Mélange deux couleurs (r/g/b seulement, ignore alpha source). */
    private static int blendColor(int base, int blueHint, float blueWeight)
    {
        int r  = (int)(((base >> 16) & 0xFF) * (1 - blueWeight));
        int gv = (int)(((base >> 8)  & 0xFF) * (1 - blueWeight));
        int b  = Math.min(255, (int)(((base & 0xFF) * (1 - blueWeight)) + (blueHint * blueWeight)));
        return 0xFF000000 | (r << 16) | (gv << 8) | b;
    }

    /** Dessine le bouton config — intégré dans la barre de titre, fond cohérent. */
    private void drawCfgButton(GuiGraphics g, int mx, int my, List<Component> tip)
    {
        int bx = getCfgBtnX(), by = getCfgBtnY();
        int bw = CFG_BTN_W, bh = CFG_BTN_H;
        boolean hov = mx >= bx && mx <= bx + bw && my >= by && my <= by + bh;

        // Fond — légèrement différent de la barre de titre pour être visible
        int bg = hov ? 0xFF505070 : 0xFF404060;
        g.fill(bx, by, bx + bw, by + bh, bg);
        // Bordure fine cohérente avec le reste du GUI
        g.fill(bx, by, bx + bw, by + 1, 0xFF8888AA);
        g.fill(bx, by, bx + 1, by + bh, 0xFF8888AA);
        g.fill(bx, by + bh - 1, bx + bw, by + bh, 0xFF222244);
        g.fill(bx + bw - 1, by, bx + bw, by + bh, 0xFF222244);

        // Icône "settings" : 3 lignes horizontales avec un carré (≠ engrenage des tabs)
        int ic = hov ? 0xFFDDDDFF : 0xFF9999CC;
        int ox = bx + 3, oy = by + 3;
        // Ligne 1 : ─ ■ ─
        g.fill(ox,     oy,     ox + 4, oy + 1, ic);
        g.fill(ox + 5, oy,     ox + 9, oy + 1, ic);
        g.fill(ox + 4, oy - 1, ox + 6, oy + 2, ic); // curseur carré
        // Ligne 2 : ─ ─ ■
        g.fill(ox,     oy + 4, ox + 7, oy + 5, ic);
        g.fill(ox + 7, oy + 3, ox + 9, oy + 6, ic);
        // Ligne 3 : ■ ─ ─
        g.fill(ox + 2, oy + 7, ox + 9, oy + 8, ic);
        g.fill(ox,     oy + 7, ox + 3, oy + 9, ic);

        if (hov)
        {
            tip.clear();
            tip.add(Component.literal("§eGUI Config"));
            tip.add(Component.literal("§7Customize colors, borders, opacity and scale"));
        }
    }

    private void drawGearIcon(GuiGraphics g, int ox, int oy, boolean active, boolean hasRedir)
    {
        int col  = active ? 0xFFE0E0E0 : (hasRedir ? 0xFF888888 : 0xFFBB7722);
        int hole = active ? 0xFF8B8B8B : (hasRedir ? 0xFF4A4A4A : 0xFF5A3A10);
        g.fill(ox + 3, oy + 1, ox + 7, oy + 9, col);
        g.fill(ox + 1, oy + 3, ox + 9, oy + 7, col);
        g.fill(ox + 4, oy,     ox + 6, oy + 2,  col);
        g.fill(ox + 4, oy + 8, ox + 6, oy + 10, col);
        g.fill(ox,     oy + 4, ox + 2, oy + 6,  col);
        g.fill(ox + 8, oy + 4, ox + 10, oy + 6, col);
        g.fill(ox + 4, oy + 4, ox + 6, oy + 6, hole);
    }

    // ── Info panel ────────────────────────────────────────────────────────────
    private void drawInfoPanel(GuiGraphics g, int x, int y, int mx, int my)
    {
        ColonyLinkGuiConfig _c = ColonyLinkGuiConfig.get();
        int panelH = 58;
        g.fill(x + 6, y + 22, x + GUI_WIDTH - 6, y + 22 + panelH, _c.applyOpacity(0xFF3A3A3A));
        g.fill(x + 6, y + 22, x + GUI_WIDTH - 6, y + 23, _c.applyOpacity(0xFF8B8B8B));
        g.fill(x + 6, y + 22, x + 7, y + 22 + panelH, _c.applyOpacity(0xFF8B8B8B));
        g.fill(x + 6, y + 22 + panelH - 1, x + GUI_WIDTH - 6, y + 22 + panelH, _c.applyOpacity(0xFF373737));
        g.fill(x + GUI_WIDTH - 7, y + 22, x + GUI_WIDTH - 6, y + 22 + panelH, _c.applyOpacity(0xFF373737));

        if (!isOutOfPower())
        {
            g.drawString(this.font, "§7Builder: §f" + builderName,   x + 10, y + 26, 0xFFFFFF, false);

            // Bouton Locate — à droite sur la ligne Builder, masqué sur l'onglet Citizens
            if (activeTabIndex != CITIZENS_TAB_INDEX)
            {
                int lbX = x + GUI_WIDTH - 6 - LOCATE_BTN_W - 4;
                int lbY = y + 22;
                boolean lHov = mx >= lbX && mx <= lbX + LOCATE_BTN_W
                        && my >= lbY && my <= lbY + LOCATE_BTN_H;
                drawButton(g, lbX, lbY, LOCATE_BTN_W, LOCATE_BTN_H,
                        lHov ? 0xFF1A5C2E : 0xFF0F3A1E, "Locate", 0xFF44DD88);
            }
            g.drawString(this.font, "§7Building: §f" + buildingName, x + 10, y + 36, 0xFFFFFF, false);

            String sl = "§7Status: ";
            g.drawString(this.font, sl, x + 10, y + 46, 0xFFFFFF, false);
            g.drawString(this.font, workerStatus,
                    x + 10 + this.font.width(sl), y + 46, getWorkerStatusColor(), false);

            // v1.1.3 — Raison IDLE sous le statut
            if (!workerIdleReason.isEmpty())
            {
                // Si plusieurs raisons (séparées par " | "), on les affiche sur une ligne condensée
                String reasonDisplay = workerIdleReason.length() > 40
                        ? workerIdleReason.substring(0, 38) + "…"
                        : workerIdleReason;
                g.drawString(this.font, reasonDisplay, x + 10, y + 56, 0xFFFFFF, false);
            }

            int cpuY = workerIdleReason.isEmpty() ? 58 : 66;
            g.drawString(this.font, "§7CPUs: §f" + availableCpus, x + 10, y + cpuY, 0xFFFFFF, false);

            int rColor = switch (redirectorState) {
                case "LINKED"     -> 0x00FF00;
                case "STANDBY"    -> 0xFF8800;
                case "NOT_LINKED" -> 0xAAAAAA;
                default           -> 0x888888;
            };
            String rDisplay = switch (redirectorState) {
                case "LINKED"     -> "Linked";
                case "STANDBY"    -> "Standby";
                case "NOT_LINKED" -> "Not Linked";
                default           -> redirectorState;
            };
            String rl = "§7Redirector: ";
            g.drawString(this.font, rl, x + 100, y + cpuY, 0xFFFFFF, false);
            g.drawString(this.font, rDisplay, x + 100 + this.font.width(rl), y + cpuY, rColor, false);
        }
        else
        {
            // Out of Power
            int cx = x + GUI_WIDTH / 2;
            g.drawCenteredString(this.font, "§cOUT OF POWER",          cx, y + 30, 0xFF4444);
            g.drawCenteredString(this.font, "§7Charge via any FE charger", cx, y + 42, 0xAAAAAA);
            g.drawCenteredString(this.font, "§7(Powah, Mekanism, IE...)",  cx, y + 52, 0xAAAAAA);
        }
    }

    // ── Request panel ─────────────────────────────────────────────────────────
    private void drawRequestPanel(GuiGraphics g, int x, int y, int mx, int my,
                                  List<Component> pendingTooltipOut)
    {
        ColonyLinkGuiConfig _cr = ColonyLinkGuiConfig.get();
        int pY = y + 80, pH = 30;
        g.fill(x + 6, pY, x + GUI_WIDTH - 6, pY + pH, _cr.applyOpacity(0xFF2E2E4A));
        g.fill(x + 6, pY, x + GUI_WIDTH - 6, pY + 1, _cr.applyOpacity(0xFF6666AA));
        g.fill(x + 6, pY, x + 7, pY + pH, _cr.applyOpacity(0xFF6666AA));
        g.fill(x + 6, pY + pH - 1, x + GUI_WIDTH - 6, pY + pH, _cr.applyOpacity(0xFF1A1A3A));
        g.fill(x + GUI_WIDTH - 7, pY, x + GUI_WIDTH - 6, pY + pH, _cr.applyOpacity(0xFF1A1A3A));
        g.fill(x + 7, pY + 11, x + GUI_WIDTH - 7, pY + 12, _cr.applyOpacity(0xFF3A3A6A));
        g.drawString(this.font, "§9Priority Request:", x + 10, pY + 3, 0xAAAAFF, false);

        boolean hasReq = builderRequest != null && !builderRequest.stack().isEmpty()
                && builderRequest.count() > 0;

        if (!hasReq || isOutOfPower())
        {
            g.drawString(this.font, isOutOfPower() ? "§8— no power —" : "§8None",
                    x + 10, pY + 14, 0x666666, false);
            return;
        }

        g.renderItem(builderRequest.stack(), x + 10, pY + 12);
        g.drawString(this.font, builderRequest.count() + "x "
                + builderRequest.stack().getDisplayName().getString(), x + 28, pY + 17, 0xFFFFFF, false);

        int rbX = getReqBtnX(), rbY = getReqBtnY(), rbW = getReqBtnW(), rbH = getReqBtnH();
        ResourceStatus rs = builderRequest.status();
        boolean hov = mx >= rbX && mx <= rbX + rbW && my >= rbY && my <= rbY + rbH;
        int bg = _cr.applyOpacity(hov && isButtonClickable(rs) ? getButtonHoverColor(rs) : getButtonColor(rs));
        g.fill(rbX, rbY, rbX + rbW, rbY + rbH, bg);
        g.fill(rbX, rbY, rbX + rbW, rbY + 1, 0xFFFFFFFF);
        g.fill(rbX, rbY, rbX + 1, rbY + rbH, 0xFFFFFFFF);
        g.fill(rbX, rbY + rbH - 1, rbX + rbW, rbY + rbH, 0xFF373737);
        g.fill(rbX + rbW - 1, rbY, rbX + rbW, rbY + rbH, 0xFF373737);
        g.drawCenteredString(this.font, getRequestButtonText(rs), rbX + rbW / 2, rbY + 4, getButtonTextColor(rs));

        // Tooltip survol bouton ou ligne item — affiche les infos de substitution si présentes
        boolean lineHov = mx >= x + 10 && mx <= rbX - 2 && my >= pY + 10 && my <= pY + 30;
        if ((hov || lineHov) && !builderRequest.tooltipLines().isEmpty())
        {
            pendingTooltipOut.clear();
            for (String line : builderRequest.tooltipLines())
                pendingTooltipOut.add(Component.literal(line));
        }
    }

    private void drawButton(GuiGraphics g, int bx, int by, int bw, int bh,
                            int bg, String label, int tc)
    {
        g.fill(bx, by, bx + bw, by + bh, bg);
        g.fill(bx, by, bx + bw, by + 1, 0xFFFFFFFF);
        g.fill(bx, by, bx + 1, by + bh, 0xFFFFFFFF);
        g.fill(bx, by + bh - 1, bx + bw, by + bh, 0xFF373737);
        g.fill(bx + bw - 1, by, bx + bw, by + bh, 0xFF373737);
        g.drawCenteredString(this.font, label, bx + bw / 2, by + 3, tc);
    }

    private void drawPrioritySwitch(GuiGraphics g, int mx, int my)
    {
        if (!hasWarehouseCard || isOutOfPower()) return;
        int sw = 110, sh = 14, sx = getGuiX() + GUI_WIDTH - sw - 8, sy = getWareCheckBtnY();
        g.fill(sx, sy, sx + sw, sy + sh, 0xFF2A2A2A);
        g.fill(sx, sy, sx + sw, sy + 1, 0xFF555555);
        g.fill(sx, sy, sx + 1, sy + sh, 0xFF555555);
        g.fill(sx, sy + sh - 1, sx + sw, sy + sh, 0xFF111111);
        g.fill(sx + sw - 1, sy, sx + sw, sy + sh, 0xFF111111);
        int half = sw / 2;
        if (warehousePriority)
        {
            g.fill(sx + 1, sy + 1, sx + half, sy + sh - 1, 0xFF224422);
            g.fill(sx + 3, sy + 3, sx + 9, sy + sh - 3, 0xFF00FF88);
        }
        else
        {
            g.fill(sx + half, sy + 1, sx + sw - 1, sy + sh - 1, 0xFF112244);
            g.fill(sx + sw - 9, sy + 3, sx + sw - 3, sy + sh - 3, 0xFF4488FF);
        }
        g.fill(sx + half, sy + 2, sx + half + 1, sy + sh - 2, 0xFF444444);
        String networkLabel = "AE2";
        g.drawCenteredString(this.font, "WH",          sx + half / 2,        sy + 3, warehousePriority ? 0x00FF88 : 0x556655);
        g.drawCenteredString(this.font, networkLabel,  sx + half + half / 2, sy + 3, warehousePriority ? 0x334466 : 0x4488FF);
    }

    private void drawWareCheckButton(GuiGraphics g, int mx, int my)
    {
        if (!hasWarehouseCard || isOutOfPower()) return;
        int bx = getWareCheckBtnX(), by = getWareCheckBtnY(), bw = getWareCheckBtnW(), bh = getWareCheckBtnH();
        boolean hov = mx >= bx && mx <= bx + bw && my >= by && my <= by + bh;
        String label; int bg, tc;
        switch (wareCheckState)
        {
            case LOADING -> { label = "Scanning..."; bg = 0xFF554400; tc = 0xFFAA44; }
            case DONE ->
            {
                boolean exp = System.currentTimeMillis() - warehouseSnapshotReceivedMs > getSnapshotValidityMs();
                if (exp) { wareCheckState = WareCheckState.IDLE; warehouseSnapshot = null; }
                label = exp ? "Check Warehouse" : "Warehouse ✔";
                bg = exp ? (hov ? 0xFF336633 : 0xFF224422) : (hov ? 0xFF447744 : 0xFF335533);
                tc = exp ? 0x88FF88 : 0x00FF88;
            }
            default -> { label = "Check Warehouse"; bg = hov ? 0xFF336633 : 0xFF224422; tc = 0x88FF88; }
        }
        g.fill(bx, by, bx + bw, by + bh, bg);
        g.fill(bx, by, bx + bw, by + 1, 0xFFFFFFFF);
        g.fill(bx, by, bx + 1, by + bh, 0xFFFFFFFF);
        g.fill(bx, by + bh - 1, bx + bw, by + bh, 0xFF373737);
        g.fill(bx + bw - 1, by, bx + bw, by + bh, 0xFF373737);
        g.drawCenteredString(this.font, label, bx + bw / 2, by + 3, tc);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {}

    // ── render() ──────────────────────────────────────────────────────────────
    @Override
    public void render(GuiGraphics g, int rawMx, int rawMy, float pt)
    {
        // ── Scale config ──────────────────────────────────────────────────────
        float _scale = ColonyLinkGuiConfig.get().scale;
        if (_scale != 1.0f)
        {
            float cx = this.width / 2f, cy = this.height / 2f;
            g.pose().pushPose();
            g.pose().translate(cx, cy, 0);
            g.pose().scale(_scale, _scale, 1f);
            g.pose().translate(-cx, -cy, 0);
        }
        // Transformer mx/my dans l'espace du GUI scalé pour les hover effects
        int mx = toGuiX(rawMx), my = toGuiY(rawMy);
        int x = getGuiX(), y = getGuiY();

        // ── Couleurs depuis ColonyLinkGuiConfig ──────────────────────────────
        ColonyLinkGuiConfig _cfg = ColonyLinkGuiConfig.get();

        // Fond
        g.fill(x, y, x + GUI_WIDTH, y + GUI_HEIGHT, _cfg.bg());

        // Bordures via config (épaisseur variable)
        _cfg.drawBorders(g, x, y, GUI_WIDTH, GUI_HEIGHT);

        // Barre de titre
        g.fill(x + _cfg.borderWidth, y + _cfg.borderWidth,
                x + GUI_WIDTH - _cfg.borderWidth, y + 22, _cfg.title());
        // Liseré haut de titre
        int _tc2 = _cfg.applyOpacity(lighten(_cfg.titleColor, 1.3f));
        g.fill(x + _cfg.borderWidth, y + _cfg.borderWidth,
                x + GUI_WIDTH - _cfg.borderWidth, y + _cfg.borderWidth + 2, _tc2);
        g.drawString(this.font, this.title, x + 58, y + 7, 0x404040, false);

        // ── Curseur handle drag ✥ — centré entre Unlink et Restart ──────────
        {
            int handleX1 = x + getDeleteBtnW() + 12;
            int handleX2 = x + GUI_WIDTH - getRestartBtnW() - 12;
            int handleCX = (handleX1 + handleX2) / 2;
            int handleCY = y + 11;
            boolean hoverHandle = isInDragHandle(mx, my);
            int dotColor = hoverHandle ? 0xFFCCCCCC : 0xFF888888;
            // Motif ⠿ : 3 colonnes × 2 lignes de points espacés
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
                "Unlink", canDel ? 0xFF4444 : 0x888888);

        int rbX = getRestartBtnX(), rbY = getRestartBtnY(), rbW = getRestartBtnW(), rbH = getRestartBtnH();
        boolean restHov = mx >= rbX && mx <= rbX + rbW && my >= rbY && my <= rbY + rbH;
        drawButton(g, rbX, rbY, rbW, rbH, restHov ? 0xFF885500 : 0xFF553300, "Restart", 0xFFAA44);

        List<Component> tip = new ArrayList<>();

        // #12 : tab Citizens → header simplifié au lieu du builder info
        if (activeTabIndex == CITIZENS_TAB_INDEX)
        {
            ColonyLinkGuiConfig _c = ColonyLinkGuiConfig.get();
            g.fill(x + 6, y + 22, x + GUI_WIDTH - 6, y + 80, _c.applyOpacity(0xFF3A3A3A));
            g.fill(x + 6, y + 22, x + GUI_WIDTH - 6, y + 23, _c.applyOpacity(0xFF8B8B8B));
            g.fill(x + 6, y + 22, x + 7, y + 80, _c.applyOpacity(0xFF8B8B8B));
            g.fill(x + 6, y + 79, x + GUI_WIDTH - 6, y + 80, _c.applyOpacity(0xFF373737));
            g.fill(x + GUI_WIDTH - 7, y + 22, x + GUI_WIDTH - 6, y + 80, _c.applyOpacity(0xFF373737));
            g.drawCenteredString(this.font, "§fCitizen Requests", x + GUI_WIDTH / 2 - 12, y + 30, 0xFFFFFF);
            String countStr = citizensLoading ? "§7Loading..."
                    : citizenEntries.isEmpty() ? "§7No open requests"
                      : "§7" + citizenEntries.size() + " open request" + (citizenEntries.size() != 1 ? "s" : "");
            g.drawCenteredString(this.font, countStr, x + GUI_WIDTH / 2 - 12, y + 44, 0xAAAAAA);
            String pkgDesc = citizenPackageCount > 0
                    ? "§7" + citizenPackageCount + " package" + (citizenPackageCount != 1 ? "s" : "") + " loaded"
                    : "§cNo packages — click slot to load";
            g.drawCenteredString(this.font, pkgDesc, x + GUI_WIDTH / 2 - 12, y + 57, 0x888888);

            // ── Slot Package (haut droite du header) ─────────────────────────
            int pkgSlotX = x + GUI_WIDTH - 26, pkgSlotY = y + 26;
            boolean pkgHov = mx >= pkgSlotX && mx <= pkgSlotX + 18 && my >= pkgSlotY && my <= pkgSlotY + 18;
            // Fond du slot : doré si packages présents, gris sinon
            int pkgBorderColor = citizenPackageCount > 0 ? 0xFF996600 : 0xFF665544;
            int pkgFillColor   = citizenPackageCount > 0 ? (pkgHov ? 0xFF5A3A00 : 0xFF3A2A00) : (pkgHov ? 0xFF4A4A4A : 0xFF3A3A3A);
            g.fill(pkgSlotX - 1, pkgSlotY - 1, pkgSlotX + 19, pkgSlotY + 19, pkgBorderColor);
            g.fill(pkgSlotX, pkgSlotY, pkgSlotX + 18, pkgSlotY + 18, pkgFillColor);
            // Icône : item normal si packages dispo, slot vide sinon
            if (citizenPackageCount > 0)
            {
                net.minecraft.world.item.ItemStack pkgDisplayStack = new net.minecraft.world.item.ItemStack(ColonyLink.COLONY_LINK_PACKAGE.get());
                g.renderItem(pkgDisplayStack, pkgSlotX + 1, pkgSlotY + 1);
                // Count badge APRÈS renderItem pour ne pas être recouvert
                String badge = citizenPackageCount >= 100 ? "99+" : String.valueOf(citizenPackageCount);
                g.renderItemDecorations(this.font, pkgDisplayStack.copyWithCount(citizenPackageCount), pkgSlotX + 1, pkgSlotY + 1, badge);
            }
            else
            {
                // Slot vide : "+" centré pour indiquer qu'il faut charger
                g.drawCenteredString(this.font, "§8+", pkgSlotX + 9, pkgSlotY + 5, 0x666666);
            }
            // Tooltip slot Package
            if (pkgHov)
            {
                tip.clear();
                tip.add(net.minecraft.network.chat.Component.literal("§6ColonyLink Package slot"));
                tip.add(net.minecraft.network.chat.Component.literal("§7Stored: §f" + citizenPackageCount + " §7/ §f64"));
                tip.add(net.minecraft.network.chat.Component.literal("§8──────────────────"));
                tip.add(net.minecraft.network.chat.Component.literal("§7Each §fSend §7or §fCraft §7consumes §f1 Package§7."));
                tip.add(net.minecraft.network.chat.Component.literal("§7Re-sending (§fSent ↺§7) consumes §f2 Packages§7."));
                tip.add(net.minecraft.network.chat.Component.literal("§8──────────────────"));
                if (citizenPackageCount < 64)
                    tip.add(net.minecraft.network.chat.Component.literal("§aClick to load packages from inventory."));
                else
                    tip.add(net.minecraft.network.chat.Component.literal("§8Full — no more packages can be loaded."));
            }
        }
        else
        {
            drawInfoPanel(g, x, y, mx, my);
            if (!isOutOfPower())
                drawRequestPanel(g, x, y, mx, my, tip);
        }

        // Liste
        ColonyLinkGuiConfig _cl = ColonyLinkGuiConfig.get();
        int listW = GUI_WIDTH - 26, listY = getListStartY();
        g.fill(x + 6, listY - 1, x + GUI_WIDTH - 18, listY - 1 + MAX_VISIBLE * ENTRY_HEIGHT + 1, _cl.applyOpacity(0xFF373737));
        g.fill(x + 6, listY - 1, x + GUI_WIDTH - 18, listY, _cl.applyOpacity(0xFF8B8B8B));
        g.fill(x + 6, listY - 1, x + 7, listY - 1 + MAX_VISIBLE * ENTRY_HEIGHT + 1, _cl.applyOpacity(0xFF8B8B8B));

        // #12 : tab Citizens active → liste lecture seule des requêtes citoyens non-builders
        if (activeTabIndex == CITIZENS_TAB_INDEX)
        {
            if (citizensLoading)
            {
                g.drawCenteredString(this.font, "§7Loading citizens...",
                        x + GUI_WIDTH / 2, listY + MAX_VISIBLE * ENTRY_HEIGHT / 2 - 4, 0x888888);
            }
            else if (citizenEntries.isEmpty())
            {
                g.drawCenteredString(this.font, "§7No open requests from citizens",
                        x + GUI_WIDTH / 2, listY + MAX_VISIBLE * ENTRY_HEIGHT / 2 - 4, 0x888888);
            }
            else
            {
                int vis = Math.min(MAX_VISIBLE, citizenEntries.size() - scrollOffset);
                for (int i = 0; i < vis; i++)
                {
                    var ce  = citizenEntries.get(i + scrollOffset);
                    int ey  = listY + i * ENTRY_HEIGHT;
                    int rowBg = _cl.applyOpacity((i % 2 == 0) ? 0xFF4A4A4A : 0xFF424242);
                    g.fill(x + 7, ey, x + 7 + listW, ey + ENTRY_HEIGHT, rowBg);
                    g.renderItem(ce.stack(), x + 9, ey + 2);

                    String itemName = ce.stack().getDisplayName().getString();
                    if (itemName.startsWith("[") && itemName.endsWith("]"))
                        itemName = itemName.substring(1, itemName.length() - 1);

                    // Bouton Craft ou Send selon disponibilité (warehouse card requise dans les deux cas)
                    boolean ceHasWHlocal = hasWarehouseCard && !redirectorPos.equals(net.minecraft.core.BlockPos.ZERO);
                    boolean ceCanSend  = ceHasWHlocal && ce.availableInME();
                    boolean ceCanCraft = ceHasWHlocal && ce.craftableInME();
                    boolean hasBtn     = ceCanSend || ceCanCraft;
                    int btnW = 44, btnH = 14;
                    int btnX = getGuiX() + 7 + listW - btnW - 2;
                    int btnY = ey + (ENTRY_HEIGHT - btnH) / 2;
                    boolean btnHov = hasBtn && mx >= btnX && mx <= btnX + btnW && my >= btnY && my <= btnY + btnH;

                    // Texte tronqué si bouton présent
                    int maxTextW = hasBtn ? listW - btnW - 28 : listW - 10;
                    String truncName = ce.count() + "x " + itemName;
                    while (truncName.length() > 2 && this.font.width("§f" + truncName) > maxTextW)
                        truncName = truncName.substring(0, truncName.length() - 1);

                    g.drawString(this.font, "§f" + truncName, x + 29, ey + 3, 0xFFFFFF, false);
                    g.drawString(this.font, "§7" + ce.citizenName() + " §8· §7" + ce.jobName(),
                            x + 29, ey + 12, 0xAAAAAA, false);

                    boolean alreadySent = sentCitizenRequests.contains(sentKey(ce));
                    if (alreadySent && hasBtn)
                    {
                        // Grisé — déjà envoyé, mais recliquable pour renvoyer
                        int btnBg = btnHov ? 0xFF3A3A3A : 0xFF2A2A2A;
                        g.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnBg);
                        g.fill(btnX, btnY, btnX + btnW, btnY + 1, 0xFF555555);
                        g.fill(btnX, btnY, btnX + 1, btnY + btnH, 0xFF555555);
                        g.fill(btnX, btnY + btnH - 1, btnX + btnW, btnY + btnH, 0xFF1A1A1A);
                        g.fill(btnX + btnW - 1, btnY, btnX + btnW, btnY + btnH, 0xFF1A1A1A);
                        g.drawCenteredString(this.font, "§7Sent ↺", btnX + btnW / 2, btnY + 3, 0x888888);
                    }
                    else if (ceCanSend)
                    {
                        int btnBg = btnHov ? 0xFF0066CC : 0xFF004488;
                        g.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnBg);
                        g.fill(btnX, btnY, btnX + btnW, btnY + 1, 0xFFFFFFFF);
                        g.fill(btnX, btnY, btnX + 1, btnY + btnH, 0xFFFFFFFF);
                        g.fill(btnX, btnY + btnH - 1, btnX + btnW, btnY + btnH, 0xFF222222);
                        g.fill(btnX + btnW - 1, btnY, btnX + btnW, btnY + btnH, 0xFF222222);
                        g.drawCenteredString(this.font, "Send", btnX + btnW / 2, btnY + 3, 0x4488FF);
                    }
                    else if (ceCanCraft)
                    {
                        int btnBg = btnHov ? 0xFF007700 : 0xFF005500;
                        g.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnBg);
                        g.fill(btnX, btnY, btnX + btnW, btnY + 1, 0xFFFFFFFF);
                        g.fill(btnX, btnY, btnX + 1, btnY + btnH, 0xFFFFFFFF);
                        g.fill(btnX, btnY + btnH - 1, btnX + btnW, btnY + btnH, 0xFF222222);
                        g.fill(btnX + btnW - 1, btnY, btnX + btnW, btnY + btnH, 0xFF222222);
                        g.drawCenteredString(this.font, "Craft", btnX + btnW / 2, btnY + 3, 0x00FF00);
                    }

                    if (mx >= x + 7 && mx <= x + 7 + listW && my >= ey && my <= ey + ENTRY_HEIGHT)
                    {
                        tip.clear();
                        tip.add(Component.literal("§f" + ce.count() + "x " + itemName));
                        tip.add(Component.literal("§7Citizen: §f" + ce.citizenName()));
                        tip.add(Component.literal("§7Job: §f" + ce.jobName()));
                        if (btnHov && alreadySent)
                        {
                            tip.add(Component.literal("§7Already sent — click to craft + send again"));
                            tip.add(Component.literal("§8Costs §f2 Packages §8(craft + send)"));
                        }
                        else if (btnHov && ceCanSend)
                            tip.add(Component.literal("§7Send to warehouse for pickup"));
                        else if (btnHov && ceCanCraft)
                            tip.add(Component.literal("§7Craft via AE2 — item will appear in ME, then click Send"));

                    }
                }

                if (citizenEntries.size() > MAX_VISIBLE)
                {
                    int sbX = getScrollbarX(), sbH = MAX_VISIBLE * ENTRY_HEIGHT;
                    g.fill(sbX, listY, sbX + SCROLLBAR_WIDTH, listY + sbH, _cl.applyOpacity(0xFF2A2A2A));
                    int thumbH = Math.max(16, sbH * MAX_VISIBLE / citizenEntries.size());
                    int thumbY = listY + (sbH - thumbH) * scrollOffset
                            / Math.max(1, citizenEntries.size() - MAX_VISIBLE);
                    g.fill(sbX + 1, thumbY, sbX + SCROLLBAR_WIDTH - 1, thumbY + thumbH, _cl.applyOpacity(0xFF8B8B8B));
                }
            }
        }
        else if (isOutOfPower())
        {
            g.drawCenteredString(this.font, "§cOut of Power — charge Clipboard to use",
                    x + GUI_WIDTH / 2, listY + MAX_VISIBLE * ENTRY_HEIGHT / 2 - 4, 0xFF4444);
        }
        else
        {
            int vis = Math.min(MAX_VISIBLE, entries.size() - scrollOffset);
            for (int i = 0; i < vis; i++)
            {
                int idx = i + scrollOffset;
                var entry = entries.get(idx);
                ItemStack stack = entry.stack();
                ResourceStatus status = entry.status();
                int rc = entry.realCount();
                int ey = listY + i * ENTRY_HEIGHT;

                int _rowBg = _cl.applyOpacity((i % 2 == 0) ? 0xFF4A4A4A : 0xFF424242);
                g.fill(x + 7, ey, x + 7 + listW, ey + ENTRY_HEIGHT, _rowBg);
                g.renderItem(stack, x + 9, ey + 2);

                // ── Nom avec défilement si trop long ──────────────────────────────
                String rawName = stack.getDisplayName().getString();
                String prefix  = entry.isDomum() ? "§b[DO] §r" : "";
                String fullText = rc + "x " + rawName;
                int nameAreaW = listW - 65 - 20; // largeur dispo pour le texte
                int fullW = this.font.width(prefix + fullText);
                String displayText;
                if (fullW > nameAreaW)
                {
                    // Défilement : offset basé sur le temps, reset en début de ligne
                    long t = System.currentTimeMillis();
                    int scrollRange = fullW - nameAreaW + 10;
                    int period = scrollRange * 120 + 2000; // ms pour un aller-retour
                    long phase = t % period;
                    int offset;
                    if (phase < 1000)                offset = 0;                            // pause début
                    else if (phase < 1000 + scrollRange * 60L) offset = (int)((phase - 1000) / 60);
                    else if (phase < 1000 + scrollRange * 60L + 1000) offset = scrollRange; // pause fin
                    else offset = scrollRange - (int)((phase - 2000 - scrollRange * 60L) / 60);

                    // Clip + translate.
                    // enableScissor() ignore la matrice pose() : on transforme donc le
                    // rectangle de clip en espace écran réel (post-scale) pour qu'il
                    // tombe pile sur la ligne, quel que soit le scale GUI configuré.
                    g.enableScissor(toScreenX(x + 29), toScreenY(ey),
                            toScreenX(x + 29 + nameAreaW), toScreenY(ey + ENTRY_HEIGHT));
                    g.drawString(this.font, prefix + fullText, x + 29 - offset, ey + 6, 0xFFFFFF, false);
                    g.disableScissor();
                }
                else
                {
                    g.drawString(this.font, prefix + fullText, x + 29, ey + 6, 0xFFFFFF, false);
                }

                // ── Zone hover de la ligne (hors bouton) ──────────────────────────
                boolean lineHov = mx >= x + 7 && mx <= x + 7 + listW - 65
                        && my >= ey && my <= ey + ENTRY_HEIGHT;

                var we = getWarehouseEntry(stack);
                if (we != null)
                {
                    long tot = we.inWarehouse() + we.viaCraft();
                    String wt; int wc;
                    if (tot >= rc)     { wt = "§aWH: " + tot;             wc = 0x00FF88; }
                    else if (tot > 0)  { wt = "§eWH: " + tot + "/" + rc; wc = 0xFFCC44; }
                    else               { wt = "§cWH: 0";                  wc = 0xFF4444; }
                    g.drawString(this.font, wt, x + 29, ey + 13, wc, false);

                    if (lineHov && !we.tooltipLines().isEmpty())
                    {
                        tip.clear();
                        tip.add(Component.literal("§6Warehouse availability:"));
                        tip.add(Component.literal("§7  Direct: §a" + we.inWarehouse() + "x"));
                        tip.add(Component.literal("§7  Via craft: §e" + we.viaCraft() + "x"));
                        tip.add(Component.literal("§8──────────"));
                        for (String ln : we.tooltipLines()) tip.add(Component.literal(ln));
                    }
                }

                // ── Tooltip Domum au survol de la ligne ───────────────────────────
                if (lineHov && entry.isDomum() && !entry.tooltipLines().isEmpty())
                {
                    tip.clear();
                    tip.add(Component.literal("§b" + rawName));
                    for (String ln : entry.tooltipLines()) tip.add(Component.literal(ln));
                }

                int[] btn = new int[4];
                getBtnBounds(i, btn);
                int bx2 = btn[0], by2 = btn[1], bw2 = btn[2], bh2 = btn[3];
                boolean hov = mx >= bx2 && mx <= bx2 + bw2 && my >= by2 && my <= by2 + bh2;

                if (hov && !entry.tooltipLines().isEmpty())
                {
                    tip.clear();
                    for (String ln : entry.tooltipLines()) tip.add(Component.literal(ln));
                }

                int bg2 = _cl.applyOpacity(getButtonColorWithWarehouse(status, stack, hov && isButtonClickable(status, stack)));
                g.fill(bx2, by2, bx2 + bw2, by2 + bh2, bg2);
                g.fill(bx2, by2, bx2 + bw2, by2 + 1, 0xFFFFFFFF);
                g.fill(bx2, by2, bx2 + 1, by2 + bh2, 0xFFFFFFFF);
                g.fill(bx2, by2 + bh2 - 1, bx2 + bw2, by2 + bh2, 0xFF373737);
                g.fill(bx2 + bw2 - 1, by2, bx2 + bw2, by2 + bh2, 0xFF373737);
                g.drawCenteredString(this.font, getButtonTextWithWarehouse(status, stack),
                        bx2 + bw2 / 2, by2 + 4, getButtonTextColor(status));
            }

            if (entries.size() > MAX_VISIBLE)
            {
                int sbX = getScrollbarX(), sbT = getScrollbarTop(), sbB = getScrollbarBottom();
                g.fill(sbX, sbT, sbX + SCROLLBAR_WIDTH, sbB, 0xFF373737);
                g.fill(sbX, sbT, sbX + 1, sbB, 0xFF8B8B8B);
                g.fill(sbX, sbT, sbX + SCROLLBAR_WIDTH, sbT + 1, 0xFF8B8B8B);
                int ty2 = getThumbY(), th2 = getThumbHeight();
                g.fill(sbX + 1, ty2, sbX + SCROLLBAR_WIDTH, ty2 + th2, 0xFF8B8B8B);
                g.fill(sbX + 1, ty2, sbX + SCROLLBAR_WIDTH, ty2 + 1, 0xFFFFFFFF);
                g.fill(sbX + 1, ty2, sbX + 2, ty2 + th2, 0xFFFFFFFF);
                g.fill(sbX + 1, ty2 + th2 - 1, sbX + SCROLLBAR_WIDTH, ty2 + th2, 0xFF373737);
                g.fill(sbX + SCROLLBAR_WIDTH - 1, ty2, sbX + SCROLLBAR_WIDTH, ty2 + th2, 0xFF373737);
            }
        }

        g.fill(x + 6, y + GUI_HEIGHT - 44, x + GUI_WIDTH - 6, y + GUI_HEIGHT - 43, ColonyLinkGuiConfig.get().applyOpacity(0xFF555555));
        // #12 : WareCheck, Priority et boutons masqués sur tab Citizens
        if (activeTabIndex != CITIZENS_TAB_INDEX)
        {
            drawWareCheckButton(g, mx, my);
            drawPrioritySwitch(g, mx, my);
        }
        g.fill(x + 6, y + GUI_HEIGHT - 26, x + GUI_WIDTH - 6, y + GUI_HEIGHT - 25, ColonyLinkGuiConfig.get().applyOpacity(0xFF555555));

        if (activeTabIndex != CITIZENS_TAB_INDEX)
        {
            ColonyLinkGuiConfig _cBtn = ColonyLinkGuiConfig.get();
            int caX = getCraftAllBtnX(), caY = getCraftAllBtnY(), caW = getCraftAllBtnW(), caH = getCraftAllBtnH();
            boolean caHov = mx >= caX && mx <= caX + caW && my >= caY && my <= caY + caH;
            boolean hasCraft = hasCraftableItems();

            // #8 : état visuel du bouton Craft All
            int caBg, caTextColor;
            String caLabel;
            if (craftInProgress)
            {
                // En cours : fond bleu foncé, texte animé (clignote via gameTicks)
                long ticks = (System.currentTimeMillis() / 400) % 3;
                String dots = ticks == 0 ? "." : ticks == 1 ? ".." : "...";
                caLabel     = "Crafting" + dots;
                caBg        = _cBtn.applyOpacity(caHov ? 0xFF003355 : 0xFF002244);
                caTextColor = 0x55AAFF;
            }
            else if (hasCraft)
            {
                caLabel     = "Craft All";
                caBg        = _cBtn.applyOpacity(caHov ? 0xFF007700 : 0xFF005500);
                caTextColor = 0x00FF00;
            }
            else
            {
                caLabel     = "Craft All";
                caBg        = _cBtn.applyOpacity(0xFF333333);
                caTextColor = 0x888888;
            }

            g.fill(caX, caY, caX + caW, caY + caH, caBg);
            g.fill(caX, caY, caX + caW, caY + 1, 0xFFFFFFFF); g.fill(caX, caY, caX + 1, caY + caH, 0xFFFFFFFF);
            g.fill(caX, caY + caH - 1, caX + caW, caY + caH, 0xFF373737); g.fill(caX + caW - 1, caY, caX + caW, caY + caH, 0xFF373737);
            g.drawCenteredString(this.font, caLabel, caX + caW / 2, caY + 4, caTextColor);

            if (caHov)
            {
                tip.clear();
                if (craftInProgress)
                {
                    tip.add(Component.literal("§bCrafting in progress..."));
                    tip.add(Component.literal("§7" + craftInProgressCount + " item type" + (craftInProgressCount != 1 ? "s" : "") + " submitted"));
                    tip.add(Component.literal("§8Waiting for next refresh..."));
                }
                else if (hasCraft)
                {
                    long craftCount = entries.stream()
                            .filter(e -> e.status() == ResourceStatus.CRAFTABLE || e.status() == ResourceStatus.MISSING)
                            .count();
                    tip.add(Component.literal("§aCraft All craftable items"));
                    tip.add(Component.literal("§7" + craftCount + " item type" + (craftCount != 1 ? "s" : "") + " to craft"));
                    tip.add(Component.literal("§7" + availableCpus + " CPU" + (availableCpus != 1 ? "s" : "") + " available"));
                }
                else
                {
                    tip.add(Component.literal("§8No craftable items"));
                }
            }

            int saX = getSendAllBtnX(), saY = getSendAllBtnY(), saW = getSendAllBtnW(), saH = getSendAllBtnH();
            boolean saHov = mx >= saX && mx <= saX + saW && my >= saY && my <= saY + saH;
            boolean hasAvail = hasAvailableItems();
            g.fill(saX, saY, saX + saW, saY + saH, _cBtn.applyOpacity(hasAvail ? (saHov ? 0xFF0066CC : 0xFF004488) : 0xFF333333));
            g.fill(saX, saY, saX + saW, saY + 1, 0xFFFFFFFF); g.fill(saX, saY, saX + 1, saY + saH, 0xFFFFFFFF);
            g.fill(saX, saY + saH - 1, saX + saW, saY + saH, 0xFF373737); g.fill(saX + saW - 1, saY, saX + saW, saY + saH, 0xFF373737);
            g.drawCenteredString(this.font, "Send All", saX + saW / 2, saY + 4, hasAvail ? 0x4488FF : 0x888888);

        } // fin du bloc non-Citizens

        drawCfgButton(g, mx, my, tip);
        drawTabs(g, mx, my, tip);

        if (hasWarehouseCard && !isOutOfPower())
        {
            int sx = getSwitchX(), sy = getSwitchY(), sw2 = getSwitchW(), sh2 = getSwitchH();
            if (mx >= sx && mx <= sx + sw2 && my >= sy && my <= sy + sh2)
            {
                tip.clear();
                tip.add(Component.literal("§6Send Priority"));
                String netLabel = "AE2";
                String netDesc  = "ME network";
                tip.add(warehousePriority
                        ? Component.literal("§a● Warehouse first\n§7Items pulled from Warehouse racks first.")
                        : Component.literal("§9● " + netLabel + " first\n§7Items pulled from " + netDesc + " first."));
                tip.add(Component.literal("§8Click to toggle."));
            }
        }
        if (mx >= rbX && mx <= rbX + rbW && my >= rbY && my <= rbY + rbH)
        {
            tip.clear();
            tip.add(Component.literal("§6Restart Builder"));
            tip.add(Component.literal("§7Cancels current task and restarts the builder PNJ"));
        }
        if (mx >= dbX && mx <= dbX + dbW && my >= dbY && my <= dbY + dbH)
        {
            tip.clear();
            tip.add(Component.literal("§cUnlink active builder"));
            tip.add(Component.literal("§7Removes the current tab from the Clipboard."));
            tip.add(Component.literal("§8The Redirector itself is not affected."));
        }

        // Tooltip bouton Locate (dans le panel info)
        if (activeTabIndex != CITIZENS_TAB_INDEX && !isOutOfPower())
        {
            int lbXT = x + GUI_WIDTH - 6 - LOCATE_BTN_W - 4;
            int lbYT = y + 22;
            if (mx >= lbXT && mx <= lbXT + LOCATE_BTN_W && my >= lbYT && my <= lbYT + LOCATE_BTN_H)
            {
                tip.clear();
                tip.add(Component.literal("§aLocate Builder"));
                tip.add(Component.literal("§7Applies §fGlowing§7 to the assigned builder NPC"));
                tip.add(Component.literal("§7for §f" + ColonyLinkConfig.LOCATE_GLOW_DURATION_SECONDS.get() + "s§7. Visible through walls."));
            }
        }

        if (ColonyLinkGuiConfig.get().scale != 1.0f)
            g.pose().popPose();

        super.render(g, rawMx, rawMy, pt);
        if (!tip.isEmpty()) g.renderComponentTooltip(this.font, tip, mx, my);
    }

    // ── mouseClicked() ────────────────────────────────────────────────────────
    @Override
    public boolean mouseClicked(double rawMx, double rawMy, int btn)
    {
        double mx = toGuiX(rawMx), my = toGuiY(rawMy);
        // ── Bouton config engrenage ───────────────────────────────────────────
        if (btn == 0)
        {
            int bx = getCfgBtnX(), by = getCfgBtnY();
            if (mx >= bx && mx <= bx + CFG_BTN_W && my >= by && my <= by + CFG_BTN_H)
            {
                this.minecraft.setScreen(new ColonyLinkConfigScreen(this));
                return true;
            }
        }

        // ── Drag GUI : clic dans la zone handle de la barre de titre ──────────
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
                    // #5 : avant de quitter la tab active, si elle avait des entrées,
                    // on la mémorise — mais on ne la marque PAS non lue (on vient de la voir)
                    lastReadEntryCount.put(activeTabIndex, entries.size());

                    activeTabIndex = i;
                    BlockPos nb = tabMetas.get(i).builderPos();
                    builderPos = nb;
                    // Fix 2 : on garde les données précédentes affichées jusqu'à réception
                    // du nouveau packet serveur — évite le GUI vide pendant le round-trip
                    builderName   = tabMetas.get(i).builderName();
                    buildingName  = tabMetas.get(i).buildingLabel();
                    workerStatus  = "Loading...";
                    warehouseSnapshot = null;
                    wareCheckState    = WareCheckState.IDLE;
                    scrollOffset      = 0;
                    // #5 : marquer la nouvelle tab active comme lue
                    unreadTabs.remove(i);
                    lastReadEntryCount.put(i, 0);
                    UNREAD_TAB_COUNT = unreadTabs.size();
                    PacketDistributor.sendToServer(new GuiStatePacket(true, nb, activeTabIndex));
                }
                return true;
            }
        }

        // #12 : clic sur la tab Citizens (droite, en bas)
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
                    // Rafraîchir le count + sent keys depuis la wand NBT
                    if (this.minecraft != null && this.minecraft.player != null)
                        for (net.minecraft.world.item.ItemStack _ws : this.minecraft.player.getInventory().items)
                            if (_ws.getItem() instanceof ColonyLinkWand)
                            {
                                this.citizenPackageCount = ColonyLinkWandLinkableHandler.getCitizenPackages(_ws);
                                this.sentCitizenRequests.clear();
                                this.sentCitizenRequests.addAll(ColonyLinkWandLinkableHandler.getSentRequestKeys(_ws));
                                break;
                            }
                }
                return true;
            }
        }

        if (tabMetas.size() < ColonyLinkWandLinkableHandler.getMaxBuilders())
        {
            int tx = getGuiX() - TAB_WIDTH, ty = getAddTabY();
            if (mx >= tx && mx <= tx + TAB_WIDTH && my >= ty && my <= ty + TAB_HEIGHT)
            {
                PacketDistributor.sendToServer(new GuiStatePacket(false, builderPos, -1));
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

        // Bouton Locate — dans le panel info, uniquement hors onglet Citizens
        if (activeTabIndex != CITIZENS_TAB_INDEX && !isOutOfPower())
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
            int rbX2 = getReqBtnX(), rbY2 = getReqBtnY(), rbW2 = getReqBtnW(), rbH2 = getReqBtnH();
            if (mx >= rbX2 && mx <= rbX2 + rbW2 && my >= rbY2 && my <= rbY2 + rbH2
                    && isButtonClickable(builderRequest.status(), builderRequest.stack()))
            {
                switch (builderRequest.status())
                {
                    case AVAILABLE ->
                    {
                        PacketDistributor.sendToServer(new SendToBuilderPacket(
                                builderRequest.stack(), builderPos, builderRequest.count()));
                    }
                    case CRAFTABLE ->
                    {
                        PacketDistributor.sendToServer(new CraftRequestPacket(
                                builderRequest.stack(), builderRequest.count(),
                                false, BlockPos.ZERO, ResourceStatus.CRAFTABLE));
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
                // #8 : déjà en cours → message informatif, pas de double envoi
                net.minecraft.client.Minecraft.getInstance().player.sendSystemMessage(
                        net.minecraft.network.chat.Component.literal(
                                "§e[ColonyLink] Craft already in progress (" + craftInProgressCount
                                        + " item type" + (craftInProgressCount != 1 ? "s" : "") + ")..."));
                return true;
            }

            if (!hasCraftableItems()) return true;

            // #8 : lancer les crafts et passer en mode "en cours"
            List<ItemStack> toCraft = new ArrayList<>();
            List<Integer> counts = new ArrayList<>();
            int submitted = 0;
            for (var entry : entries)
            {
                if (entry.status() == ResourceStatus.CRAFTABLE)
                {
                    submitted++;
                    // Domum CRAFTABLE depuis v1.4.3 : même chemin que standard (ICraftingProvider)
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

            // Basculer en mode "en cours" si au moins 1 craft envoyé
            if (submitted > 0)
            {
                craftInProgress      = true;
                craftInProgressCount = submitted;
            }
            return true;
        }

        int saX = getSendAllBtnX(), saY = getSendAllBtnY(), saW = getSendAllBtnW(), saH = getSendAllBtnH();
        if (mx >= saX && mx <= saX + saW && my >= saY && my <= saY + saH && hasAvailableItems())
        {
            // Envoyer la Priority Request en premier si elle est AVAILABLE
            if (builderRequest != null && !builderRequest.stack().isEmpty()
                    && builderRequest.status() == ResourceStatus.AVAILABLE)
            {
                PacketDistributor.sendToServer(new SendToBuilderPacket(
                        builderRequest.stack(), builderPos, builderRequest.count()));
            }
            // Puis envoyer le reste de la liste (sauf la priority request si déjà envoyée)
            for (var entry : entries)
            {
                if (entry.status() != ResourceStatus.AVAILABLE) continue;
                // Éviter le double envoi si la priority request est aussi dans la liste
                if (builderRequest != null && !builderRequest.stack().isEmpty()
                        && builderRequest.status() == ResourceStatus.AVAILABLE
                        && ItemStack.isSameItemSameComponents(entry.stack(), builderRequest.stack()))
                    continue;
                PacketDistributor.sendToServer(new SendToBuilderPacket(
                        entry.stack(), builderPos, entry.realCount()));
            }
            return true;
        }

        // #12 : clic bouton Send/Craft + slot Package dans la tab Citizens
        if (activeTabIndex == CITIZENS_TAB_INDEX)
        {
            // Clic sur le slot Package (haut droite du header Citizens)
            int pkgSlotX = getGuiX() + GUI_WIDTH - 26, pkgSlotY = getGuiY() + 26;
            if (mx >= pkgSlotX && mx <= pkgSlotX + 18 && my >= pkgSlotY && my <= pkgSlotY + 18)
            {
                // Charger des packages depuis l'inventaire
                PacketDistributor.sendToServer(new PackageLoadPacket());
                return true;
            }

            boolean ceHasWH = hasWarehouseCard && !redirectorPos.equals(net.minecraft.core.BlockPos.ZERO);
            if (ceHasWH)
            {
                int listW = GUI_WIDTH - 26, listY = getListStartY();
                int btnW = 44, btnH = 14;
                int vis2 = Math.min(MAX_VISIBLE, citizenEntries.size() - scrollOffset);
                for (int i = 0; i < vis2; i++)
                {
                    var ce  = citizenEntries.get(i + scrollOffset);
                    boolean canSend  = ce.availableInME();
                    boolean canCraft = ce.craftableInME();
                    if (!canSend && !canCraft) continue;
                    int ey  = listY + i * ENTRY_HEIGHT;
                    int btnX = getGuiX() + 7 + listW - btnW - 2;
                    int btnY = ey + (ENTRY_HEIGHT - btnH) / 2;
                    if (mx >= btnX && mx <= btnX + btnW && my >= btnY && my <= btnY + btnH)
                    {
                        if (citizenPackageCount <= 0)
                        {
                            if (this.minecraft != null && this.minecraft.player != null)
                                this.minecraft.player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                        "§c[ColonyLink] No Packages! Load ColonyLink Packages into the slot first."));
                            return true;
                        }
                        boolean wasAlreadySent = sentCitizenRequests.contains(sentKey(ce));
                        String ceKey = sentKey(ce);
                        String itemLabel = "§f" + ce.count() + "x " + stripItemName(ce.stack().getDisplayName().getString());

                        if (!wasAlreadySent)
                        {
                            // Premier clic : action normale (Send ou Craft selon disponibilité)
                            sentCitizenRequests.add(ceKey);
                            net.minecraft.world.item.ItemStack wandForSent = getClientWand();
                            if (!wandForSent.isEmpty())
                                ColonyLinkWandLinkableHandler.addSentRequestKey(wandForSent, ceKey);
                            PacketDistributor.sendToServer(new PackageTokenPacket(
                                    ce.stack(), ce.count(), redirectorPos, !canSend));
                            citizenPackageCount = Math.max(0, citizenPackageCount - 1);
                        }
                        else
                        {
                            // Reclique sur "Sent ↺" : craft + send en séquence (2 packages)
                            if (citizenPackageCount < 2)
                            {
                                if (this.minecraft != null && this.minecraft.player != null)
                                    this.minecraft.player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                            "§c[ColonyLink] Need 2 Packages to re-send (craft + send). Only "
                                                    + citizenPackageCount + " remaining."));
                                return true;
                            }
                            // Craft d'abord
                            PacketDistributor.sendToServer(new PackageTokenPacket(
                                    ce.stack(), ce.count(), redirectorPos, true));
                            // Puis send
                            PacketDistributor.sendToServer(new PackageTokenPacket(
                                    ce.stack(), ce.count(), redirectorPos, false));
                            citizenPackageCount = Math.max(0, citizenPackageCount - 2);
                            if (this.minecraft != null && this.minecraft.player != null)
                                this.minecraft.player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                        "§e[ColonyLink] Re-sending " + itemLabel + "§e: craft + send queued."));
                        }
                        return true;
                    }
                }
            }
            return true; // clic dans la zone citizens → toujours consommé
        }

        int vis = Math.min(MAX_VISIBLE, entries.size() - scrollOffset);
        for (int i = 0; i < vis; i++)
        {
            int idx = i + scrollOffset;
            var entry = entries.get(idx);
            if (!isButtonClickable(entry.status(), entry.stack())) continue;
            int[] b = new int[4]; getBtnBounds(i, b);
            if (mx >= b[0] && mx <= b[0] + b[2] && my >= b[1] && my <= b[1] + b[3])
            {
                if (entry.status() == ResourceStatus.CRAFTABLE && entry.isDomum())
                {
                    // Domum CRAFTABLE = les composants bruts sont en stock (AE2 ou RS2)
                    // → craft virtuel via WarehouseCraftPacket (extrait composants → buffer redirector)
                    // Pas de CraftRequestPacket/RS car il n'y a pas de pattern pour les items DO
                    if (hasWarehouseCraft(entry.stack()))
                        PacketDistributor.sendToServer(new WarehouseCraftPacket(
                                entry.stack(), entry.realCount(), true, entry.redirectorPos()));
                    else
                        // Fallback : composants pas en WH mais en réseau → craft direct
                        PacketDistributor.sendToServer(new CraftRequestPacket(
                                entry.stack(), entry.realCount(), true, entry.redirectorPos(), ResourceStatus.CRAFTABLE));
                }
                else if (entry.status() == ResourceStatus.CRAFTABLE)
                {
                    PacketDistributor.sendToServer(hasWarehouseCraft(entry.stack())
                            ? new WarehouseCraftPacket(entry.stack(), entry.realCount(), false, entry.redirectorPos())
                            : new CraftRequestPacket(entry.stack(), entry.realCount(), false, BlockPos.ZERO, ResourceStatus.CRAFTABLE));
                }
                else if (entry.status() == ResourceStatus.MISSING)
                {
                    PacketDistributor.sendToServer(new CraftRequestPacket(
                            entry.stack(), entry.realCount(), true, entry.redirectorPos(), ResourceStatus.MISSING));
                }
                else if (entry.status() == ResourceStatus.AVAILABLE)
                {
                    PacketDistributor.sendToServer(new SendToBuilderPacket(
                            entry.stack(), builderPos, entry.realCount()));
                }
                else if (entry.status() == ResourceStatus.NO_PATTERN)
                {
                    if (entry.isDomum())
                    {
                        // v1.4.8 — Envoie l'item Domum dans la queue du terminal
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
            // Contrainte : garder le GUI dans les limites de l'écran
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
        // #12 : scroll adapté selon la tab active
        int listSize = (activeTabIndex == CITIZENS_TAB_INDEX) ? citizenEntries.size() : entries.size();
        int max = listSize - MAX_VISIBLE;
        if (sy < 0 && scrollOffset < max) scrollOffset++;
        else if (sy > 0 && scrollOffset > 0) scrollOffset--;
        return true;
    }

    /**
     * Autorise l'accès à la hotbar pendant que le GUI est ouvert.
     * Les touches 1-9 swappent le slot hotbar sélectionné normalement.
     * La touche E ferme le GUI (comportement standard).
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        // Touches hotbar 1-9 (GLFW : 49-57)
        if (keyCode >= 49 && keyCode <= 57)
        {
            if (this.minecraft != null && this.minecraft.player != null)
                this.minecraft.player.getInventory().selected = keyCode - 49;
            return true;
        }
        // Déléguer le reste (Escape ferme, etc.)
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    /**
     * Marque une tab comme non lue (nouvelles requêtes détectées).
     * Appelé par ColonyLinkServerTicker quand le nombre d'entrées change
     * sur une tab qui n'est pas active dans le GUI ouvert.
     */
    /**
     * Marque une tab comme non lue UNIQUEMENT si le joueur ne l'a pas déjà
     * vue avec ce nombre de requêtes (ou plus).
     * lastReadEntryCount[i] = dernier count vu par le joueur sur la tab i.
     * Si le serveur signale count > lastRead → nouvelles requêtes → badge.
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

    /** Compat — appelé sans count (marque inconditionnellement). */
    public static void markTabUnread(int tabIndex)
    {
        unreadTabs.add(tabIndex);
        UNREAD_TAB_COUNT = unreadTabs.size();
    }

    /** Remet à zéro toutes les marques non lues (ex: à la déconnexion). */
    public static void clearAllUnread()
    {
        unreadTabs.clear();
        lastReadEntryCount.clear();
        UNREAD_TAB_COUNT = 0;
    }

    public BlockPos getBuilderPos() { return builderPos; }
}