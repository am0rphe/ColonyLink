package com.colonylink.colonylink;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColonyLinkScreen extends Screen
{
    private List<ColonyLinkPacket.ResourceEntry> entries;
    private final BlockPos builderPos;
    private int scrollOffset = 0;
    private static final int ENTRY_HEIGHT = 20;
    private static final int MAX_VISIBLE = 8;
    private static final int SCROLLBAR_WIDTH = 6;

    private static final int GUI_WIDTH = 276;
    private static final int GUI_HEIGHT = 320;

    private boolean isDraggingScrollbar = false;
    private double dragStartY = 0;
    private int dragStartOffset = 0;

    private String builderName = "";
    private String buildingName = "";
    private String workerStatus = "";
    private int availableCpus = 0;
    private String redirectorState = "N/A";

    // Feature 1 — requête prioritaire
    private ColonyLinkPacket.BuilderRequest builderRequest = ColonyLinkPacket.BuilderRequest.NONE;

    // ── Warehouse snapshot ────────────────────────────────────────────────────
    /** true si le redirector lié a une WarehouseLinkCard insérée. */
    private boolean hasWarehouseCard = false;

    /** État du switch priorité : true = Warehouse first, false = AE2 first. */
    private boolean warehousePriority = false;

    /** Position du redirector lié à la wand (pour envoyer le packet toggle). */
    private BlockPos redirectorPos = BlockPos.ZERO;

    /**
     * Snapshot du dernier scan warehouse.
     * Clé : item (utilise l'identité d'ItemStack via le displayName + item pour simplifier ;
     * en pratique on indexe par Item car la résolution est déjà faite côté serveur).
     * Valeur : WarehouseResultPacket.WarehouseEntry
     */
    private WarehouseResultPacket warehouseSnapshot = null;

    /** Timestamp client (System.currentTimeMillis) du dernier scan reçu. */
    private long warehouseSnapshotReceivedMs = 0;

    /** Durée de validité du snapshot côté client : 400 ticks = 20 secondes. */
    private static final long SNAPSHOT_VALIDITY_MS = 20_000L;

    /** Etat du bouton Check Warehouse : IDLE, LOADING, DONE. */
    private enum WareCheckState { IDLE, LOADING, DONE }
    private WareCheckState wareCheckState = WareCheckState.IDLE;

    // ────────────────────────────────────────────────────────────────────────

    public ColonyLinkScreen(ColonyLinkPacket packet)
    {
        super(Component.literal("Colony Link - Builder Resources"));
        this.entries = packet.entries();
        this.builderPos = packet.builderPos();
        this.builderName = packet.builderName();
        this.buildingName = packet.buildingName();
        this.workerStatus = packet.workerStatus();
        this.availableCpus = packet.availableCpus();
        this.redirectorState = packet.redirectorState();
        this.builderRequest = packet.builderRequest() != null
                ? packet.builderRequest() : ColonyLinkPacket.BuilderRequest.NONE;
        this.hasWarehouseCard = packet.hasWarehouseCard();
        this.warehousePriority = packet.warehousePriority();
        // Récupère la position du redirector depuis la première entrée (ou ZERO si vide)
        this.redirectorPos = packet.entries().isEmpty()
                ? BlockPos.ZERO : packet.entries().get(0).redirectorPos();
    }

    public void updateEntries(List<ColonyLinkPacket.ResourceEntry> newEntries, String builderName,
                              String buildingName, String workerStatus, int availableCpus,
                              String redirectorState, ColonyLinkPacket.BuilderRequest builderRequest,
                              boolean hasWarehouseCard, boolean warehousePriority)
    {
        this.entries = newEntries;
        this.builderName = builderName;
        this.buildingName = buildingName;
        this.workerStatus = workerStatus;
        this.availableCpus = availableCpus;
        this.redirectorState = redirectorState;
        this.builderRequest = builderRequest != null ? builderRequest : ColonyLinkPacket.BuilderRequest.NONE;
        this.hasWarehouseCard = hasWarehouseCard;
        this.warehousePriority = warehousePriority;
        if (!newEntries.isEmpty() && !newEntries.get(0).redirectorPos().equals(BlockPos.ZERO))
            this.redirectorPos = newEntries.get(0).redirectorPos();
        int maxOffset = Math.max(0, entries.size() - MAX_VISIBLE);
        if (scrollOffset > maxOffset) scrollOffset = maxOffset;
    }

    /**
     * Appelé par WarehouseResultPacket.handle() quand le serveur répond au scan.
     */
    public void updateWarehouseSnapshot(WarehouseResultPacket packet)
    {
        this.warehouseSnapshot = packet;
        this.warehouseSnapshotReceivedMs = System.currentTimeMillis();
        this.wareCheckState = packet.scanSuccess() ? WareCheckState.DONE : WareCheckState.IDLE;
    }

    /**
     * Retourne les données warehouse pour un item donné depuis le snapshot actif.
     * Retourne null si le snapshot est absent, expiré, ou ne contient pas cet item.
     */
    private WarehouseResultPacket.WarehouseEntry getWarehouseEntry(ItemStack stack)
    {
        if (warehouseSnapshot == null) return null;
        if (System.currentTimeMillis() - warehouseSnapshotReceivedMs > SNAPSHOT_VALIDITY_MS)
        {
            warehouseSnapshot = null;
            wareCheckState = WareCheckState.IDLE;
            return null;
        }
        for (WarehouseResultPacket.WarehouseEntry entry : warehouseSnapshot.entries())
        {
            if (ItemStack.isSameItem(entry.stack(), stack))
                return entry;
        }
        return null;
    }

    @Override
    protected void init()
    {
        super.init();
        PacketDistributor.sendToServer(new GuiStatePacket(true, builderPos));
    }

    @Override
    public void onClose()
    {
        PacketDistributor.sendToServer(new GuiStatePacket(false, builderPos));
        super.onClose();
    }

    // ── Coordonnées GUI ───────────────────────────────────────────────────

    private int getGuiX() { return (this.width - GUI_WIDTH) / 2; }
    private int getGuiY() { return (this.height - GUI_HEIGHT) / 2; }

    /** Y de départ de la liste de ressources (après panel info + panel requête PNJ). */
    private int getListStartY() { return getGuiY() + 112; }

    private int getScrollbarX() { return getGuiX() + GUI_WIDTH - 16; }
    private int getScrollbarTop() { return getListStartY() + 1; }
    private int getScrollbarBottom() { return getScrollbarTop() + MAX_VISIBLE * ENTRY_HEIGHT; }
    private int getScrollbarHeight() { return getScrollbarBottom() - getScrollbarTop(); }

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

    // ── Bouton Check Warehouse ────────────────────────────────────────────

    private int getWareCheckBtnX() { return getGuiX() + 8; }
    private int getWareCheckBtnY() { return getGuiY() + GUI_HEIGHT - 40; }
    private int getWareCheckBtnW() { return 120; }
    private int getWareCheckBtnH() { return 14; }

    // ── Couleurs boutons ──────────────────────────────────────────────────

    private int getButtonColor(ResourceStatus status)
    {
        return switch (status)
        {
            case AVAILABLE  -> 0xFF004488;
            case CRAFTABLE  -> 0xFF005500;
            case NO_PATTERN -> 0xFF550000;
            case CRAFTING   -> 0xFF885500;
            case MISSING    -> 0xFF5D3A00;
        };
    }

    private int getButtonHoverColor(ResourceStatus status)
    {
        return switch (status)
        {
            case AVAILABLE  -> 0xFF0066CC;
            case CRAFTABLE  -> 0xFF007700;
            case NO_PATTERN -> 0xFF660000;
            case CRAFTING   -> 0xFF885500;
            case MISSING    -> 0xFF8B5E00;
        };
    }

    private int getButtonTextColor(ResourceStatus status)
    {
        return switch (status)
        {
            case AVAILABLE  -> 0x4488FF;
            case CRAFTABLE  -> 0x00FF00;
            case NO_PATTERN -> 0xFF4444;
            case CRAFTING   -> 0xFFAA00;
            case MISSING    -> 0xFFCC66;
        };
    }

    private String getButtonText(ResourceStatus status)
    {
        return switch (status)
        {
            case AVAILABLE  -> "Available";
            case CRAFTABLE  -> "Craft";
            case NO_PATTERN -> "No Pattern";
            case CRAFTING   -> "Crafting...";
            case MISSING    -> "Missing";
        };
    }

    private String getRequestButtonText(ResourceStatus status)
    {
        return switch (status)
        {
            case AVAILABLE  -> "Fulfill";
            case CRAFTABLE  -> "Craft";
            case NO_PATTERN -> "No Pattern";
            case CRAFTING   -> "Crafting...";
            case MISSING    -> "Missing";
        };
    }

    private boolean isButtonClickable(ResourceStatus status)
    {
        return status == ResourceStatus.CRAFTABLE
                || status == ResourceStatus.AVAILABLE
                || status == ResourceStatus.MISSING;
    }

    /**
     * Version avec context warehouse : un item NO_PATTERN devient cliquable
     * si le snapshot warehouse indique qu'il peut être couvert (viaCraft > 0 ou inWarehouse > 0).
     */
    private boolean isButtonClickable(ResourceStatus status, ItemStack stack)
    {
        if (isButtonClickable(status)) return true;
        if (status == ResourceStatus.NO_PATTERN)
        {
            WarehouseResultPacket.WarehouseEntry we = getWarehouseEntry(stack);
            return we != null && (we.inWarehouse() > 0 || we.viaCraft() > 0);
        }
        return false;
    }

    /**
     * Texte du bouton tenant compte du snapshot warehouse.
     */
    private String getButtonTextWithWarehouse(ResourceStatus status, ItemStack stack)
    {
        if (status == ResourceStatus.NO_PATTERN)
        {
            WarehouseResultPacket.WarehouseEntry we = getWarehouseEntry(stack);
            if (we != null && we.inWarehouse() > 0) return "Send (WH)";
            if (we != null && we.viaCraft() > 0) return "Craft (WH)";
        }
        return getButtonText(status);
    }

    /**
     * Couleur du bouton tenant compte du snapshot warehouse.
     */
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

    // ── Bounds boutons liste ──────────────────────────────────────────────

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

    // ── Bounds boutons globaux ────────────────────────────────────────────

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

    private int getReqBtnX() { return getGuiX() + GUI_WIDTH - 76; }
    private int getReqBtnY() { return getGuiY() + 92; }
    private int getReqBtnW() { return 64; }
    private int getReqBtnH() { return 16; }

    // ── Helpers état ──────────────────────────────────────────────────────

    private boolean hasCraftableItems()
    {
        return entries.stream().anyMatch(e ->
                e.status() == ResourceStatus.CRAFTABLE || e.status() == ResourceStatus.MISSING);
    }

    private boolean hasAvailableItems()
    {
        return entries.stream().anyMatch(e -> e.status() == ResourceStatus.AVAILABLE);
    }

    private int getWorkerStatusColor()
    {
        if (workerStatus == null) return 0x888888;
        if (workerStatus.contains("work") || workerStatus.contains("Working")) return 0x00FF00;
        if (workerStatus.contains("sleep") || workerStatus.contains("Sleep")) return 0x4488FF;
        if (workerStatus.contains("eat") || workerStatus.contains("Eat")) return 0xFFAA00;
        if (workerStatus.contains("sick") || workerStatus.contains("Sick")) return 0xFF4444;
        if (workerStatus.contains("Stuck") || workerStatus.contains("STUCK")) return 0xFF0000;
        if (workerStatus.contains("Idle") || workerStatus.contains("IDLE")) return 0xFFFF00;
        return 0xCCCCCC;
    }

    // ── Dessin panel info ─────────────────────────────────────────────────

    private void drawInfoPanel(GuiGraphics graphics, int x, int y)
    {
        int panelH = 58;
        graphics.fill(x + 6, y + 22, x + GUI_WIDTH - 6, y + 22 + panelH, 0xFF3A3A3A);
        graphics.fill(x + 6, y + 22, x + GUI_WIDTH - 6, y + 23, 0xFF8B8B8B);
        graphics.fill(x + 6, y + 22, x + 7, y + 22 + panelH, 0xFF8B8B8B);
        graphics.fill(x + 6, y + 22 + panelH - 1, x + GUI_WIDTH - 6, y + 22 + panelH, 0xFF373737);
        graphics.fill(x + GUI_WIDTH - 7, y + 22, x + GUI_WIDTH - 6, y + 22 + panelH, 0xFF373737);

        graphics.drawString(this.font, "§7Builder: §f" + builderName, x + 10, y + 26, 0xFFFFFF, false);
        graphics.drawString(this.font, "§7Building: §f" + buildingName, x + 10, y + 36, 0xFFFFFF, false);

        String statusLabel = "§7Status: ";
        graphics.drawString(this.font, statusLabel, x + 10, y + 46, 0xFFFFFF, false);
        graphics.drawString(this.font, workerStatus,
                x + 10 + this.font.width(statusLabel), y + 46, getWorkerStatusColor(), false);

        graphics.drawString(this.font, "§7CPUs: §f" + availableCpus, x + 10, y + 58, 0xFFFFFF, false);

        int redirectorColor = switch (redirectorState)
        {
            case "LINKED"      -> 0x00FF00;
            case "STANDBY"     -> 0xFF8800;
            case "NOT_LINKED"  -> 0xAAAAAA;
            default            -> 0x888888;
        };
        String redirectorDisplay = switch (redirectorState)
        {
            case "LINKED"     -> "Linked";
            case "STANDBY"    -> "Standby";
            case "NOT_LINKED" -> "Not Linked";
            default           -> redirectorState;
        };
        String redirectorLabel = "§7Redirector: ";
        graphics.drawString(this.font, redirectorLabel, x + 100, y + 58, 0xFFFFFF, false);
        graphics.drawString(this.font, redirectorDisplay,
                x + 100 + this.font.width(redirectorLabel), y + 58, redirectorColor, false);
    }

    // ── Feature 1 : Dessin panel requête PNJ ─────────────────────────────

    private void drawRequestPanel(GuiGraphics graphics, int x, int y, int mouseX, int mouseY)
    {
        int panelY = y + 80;
        int panelH = 30;
        graphics.fill(x + 6, panelY, x + GUI_WIDTH - 6, panelY + panelH, 0xFF2E2E4A);
        graphics.fill(x + 6, panelY, x + GUI_WIDTH - 6, panelY + 1, 0xFF6666AA);
        graphics.fill(x + 6, panelY, x + 7, panelY + panelH, 0xFF6666AA);
        graphics.fill(x + 6, panelY + panelH - 1, x + GUI_WIDTH - 6, panelY + panelH, 0xFF1A1A3A);
        graphics.fill(x + GUI_WIDTH - 7, panelY, x + GUI_WIDTH - 6, panelY + panelH, 0xFF1A1A3A);
        graphics.fill(x + 7, panelY + 11, x + GUI_WIDTH - 7, panelY + 12, 0xFF3A3A6A);

        graphics.drawString(this.font, "§9Priority Request:", x + 10, panelY + 3, 0xAAAAFF, false);

        boolean hasRequest = builderRequest != null
                && !builderRequest.stack().isEmpty()
                && builderRequest.count() > 0;

        if (!hasRequest)
        {
            graphics.drawString(this.font, "§8None", x + 10, panelY + 14, 0x666666, false);
            return;
        }

        int itemX = x + 10;
        int itemY = panelY + 12;
        graphics.renderItem(builderRequest.stack(), itemX, itemY);

        String reqText = builderRequest.count() + "x " + builderRequest.stack().getDisplayName().getString();
        graphics.drawString(this.font, reqText, itemX + 18, panelY + 17, 0xFFFFFF, false);

        int rbX = getReqBtnX();
        int rbY = getReqBtnY();
        int rbW = getReqBtnW();
        int rbH = getReqBtnH();
        ResourceStatus reqStatus = builderRequest.status();

        boolean hovered = mouseX >= rbX && mouseX <= rbX + rbW
                && mouseY >= rbY && mouseY <= rbY + rbH;

        int btnBg = hovered && isButtonClickable(reqStatus)
                ? getButtonHoverColor(reqStatus)
                : getButtonColor(reqStatus);

        graphics.fill(rbX, rbY, rbX + rbW, rbY + rbH, btnBg);
        graphics.fill(rbX, rbY, rbX + rbW, rbY + 1, 0xFFFFFFFF);
        graphics.fill(rbX, rbY, rbX + 1, rbY + rbH, 0xFFFFFFFF);
        graphics.fill(rbX, rbY + rbH - 1, rbX + rbW, rbY + rbH, 0xFF373737);
        graphics.fill(rbX + rbW - 1, rbY, rbX + rbW, rbY + rbH, 0xFF373737);
        graphics.drawCenteredString(this.font, getRequestButtonText(reqStatus),
                rbX + rbW / 2, rbY + 4, getButtonTextColor(reqStatus));
    }

    // ── Dessin bouton (helper réutilisable) ───────────────────────────────

    private void drawButton(GuiGraphics graphics, int bx, int by, int bw, int bh,
                            int bgColor, String label, int textColor)
    {
        graphics.fill(bx, by, bx + bw, by + bh, bgColor);
        graphics.fill(bx, by, bx + bw, by + 1, 0xFFFFFFFF);
        graphics.fill(bx, by, bx + 1, by + bh, 0xFFFFFFFF);
        graphics.fill(bx, by + bh - 1, bx + bw, by + bh, 0xFF373737);
        graphics.fill(bx + bw - 1, by, bx + bw, by + bh, 0xFF373737);
        graphics.drawCenteredString(this.font, label, bx + bw / 2, by + 3, textColor);
    }

    // ── Dessin switch priorité Warehouse/AE2 ─────────────────────────────

    /**
     * Switch visuel :
     *   [ ● Warehouse    AE2 ]  ← warehousePriority = true
     *   [ Warehouse    AE2 ● ]  ← warehousePriority = false
     *
     * Affiché uniquement si hasWarehouseCard.
     * Placé à droite du bouton Check Warehouse sur la même ligne.
     */
    private void drawPrioritySwitch(GuiGraphics graphics, int mouseX, int mouseY)
    {
        if (!hasWarehouseCard) return;

        int sw = 110; // largeur totale du switch
        int sh = 14;  // hauteur
        int sx = getGuiX() + GUI_WIDTH - sw - 8;
        int sy = getWareCheckBtnY();

        boolean hovered = mouseX >= sx && mouseX <= sx + sw
                && mouseY >= sy && mouseY <= sy + sh;

        // Fond du switch
        graphics.fill(sx, sy, sx + sw, sy + sh, 0xFF2A2A2A);
        graphics.fill(sx, sy, sx + sw, sy + 1, 0xFF555555);
        graphics.fill(sx, sy, sx + 1, sy + sh, 0xFF555555);
        graphics.fill(sx, sy + sh - 1, sx + sw, sy + sh, 0xFF111111);
        graphics.fill(sx + sw - 1, sy, sx + sw, sy + sh, 0xFF111111);

        // Moitié gauche = Warehouse, moitié droite = AE2
        int half = sw / 2;

        if (warehousePriority)
        {
            // Warehouse actif : fond vert à gauche
            graphics.fill(sx + 1, sy + 1, sx + half, sy + sh - 1, 0xFF224422);
            // Indicateur (pastille) à gauche
            graphics.fill(sx + 3, sy + 3, sx + 9, sy + sh - 3, 0xFF00FF88);
        }
        else
        {
            // AE2 actif : fond bleu à droite
            graphics.fill(sx + half, sy + 1, sx + sw - 1, sy + sh - 1, 0xFF112244);
            // Indicateur (pastille) à droite
            graphics.fill(sx + sw - 9, sy + 3, sx + sw - 3, sy + sh - 3, 0xFF4488FF);
        }

        // Séparateur central
        graphics.fill(sx + half, sy + 2, sx + half + 1, sy + sh - 2, 0xFF444444);

        // Labels
        int wareColor = warehousePriority ? 0x00FF88 : 0x556655;
        int ae2Color  = warehousePriority ? 0x334466 : 0x4488FF;
        graphics.drawCenteredString(this.font, "WH", sx + half / 2, sy + 3, wareColor);
        graphics.drawCenteredString(this.font, "AE2", sx + half + half / 2, sy + 3, ae2Color);
    }

    /** Bounds du switch (pour mouseClicked et tooltip). */
    private int getSwitchX() { return getGuiX() + GUI_WIDTH - 118; }
    private int getSwitchY() { return getWareCheckBtnY(); }
    private int getSwitchW() { return 110; }
    private int getSwitchH() { return 14; }

    // ── Dessin bouton Check Warehouse ─────────────────────────────────────

    private void drawWareCheckButton(GuiGraphics graphics, int mouseX, int mouseY)
    {
        if (!hasWarehouseCard) return;

        int bx = getWareCheckBtnX();
        int by = getWareCheckBtnY();
        int bw = getWareCheckBtnW();
        int bh = getWareCheckBtnH();

        boolean hovered = mouseX >= bx && mouseX <= bx + bw && mouseY >= by && mouseY <= by + bh;

        String label;
        int bgColor;
        int textColor;

        switch (wareCheckState)
        {
            case LOADING ->
            {
                label = "Scanning...";
                bgColor = 0xFF554400;
                textColor = 0xFFAA44;
            }
            case DONE ->
            {
                boolean expired = System.currentTimeMillis() - warehouseSnapshotReceivedMs > SNAPSHOT_VALIDITY_MS;
                if (expired)
                {
                    wareCheckState = WareCheckState.IDLE;
                    warehouseSnapshot = null;
                    label = "Check Warehouse";
                    bgColor = hovered ? 0xFF336633 : 0xFF224422;
                    textColor = 0x88FF88;
                }
                else
                {
                    label = "Warehouse ✔";
                    bgColor = hovered ? 0xFF447744 : 0xFF335533;
                    textColor = 0x00FF88;
                }
            }
            default ->
            {
                label = "Check Warehouse";
                bgColor = hovered ? 0xFF336633 : 0xFF224422;
                textColor = 0x88FF88;
            }
        }

        graphics.fill(bx, by, bx + bw, by + bh, bgColor);
        graphics.fill(bx, by, bx + bw, by + 1, 0xFFFFFFFF);
        graphics.fill(bx, by, bx + 1, by + bh, 0xFFFFFFFF);
        graphics.fill(bx, by + bh - 1, bx + bw, by + bh, 0xFF373737);
        graphics.fill(bx + bw - 1, by, bx + bw, by + bh, 0xFF373737);
        graphics.drawCenteredString(this.font, label, bx + bw / 2, by + 3, textColor);
    }

    // ── render() ─────────────────────────────────────────────────────────

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        // Pas de blur
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        int x = getGuiX();
        int y = getGuiY();

        // Background
        graphics.fill(x, y, x + GUI_WIDTH, y + GUI_HEIGHT, 0xFF8B8B8B);
        graphics.fill(x, y, x + GUI_WIDTH, y + 2, 0xFFFFFFFF);
        graphics.fill(x, y, x + 2, y + GUI_HEIGHT, 0xFFFFFFFF);
        graphics.fill(x, y + GUI_HEIGHT - 2, x + GUI_WIDTH, y + GUI_HEIGHT, 0xFF373737);
        graphics.fill(x + GUI_WIDTH - 2, y, x + GUI_WIDTH, y + GUI_HEIGHT, 0xFF373737);

        // Title bar
        graphics.fill(x + 2, y + 2, x + GUI_WIDTH - 2, y + 22, 0xFF6B6B6B);
        graphics.fill(x + 2, y + 2, x + GUI_WIDTH - 2, y + 4, 0xFF8B8B8B);
        graphics.drawString(this.font, this.title, x + 8, y + 7, 0x404040, false);

        // Bouton Restart (haut droite)
        int rbtnX = getRestartBtnX();
        int rbtnY = getRestartBtnY();
        int rbtnW = getRestartBtnW();
        int rbtnH = getRestartBtnH();
        boolean restartHovered = mouseX >= rbtnX && mouseX <= rbtnX + rbtnW
                && mouseY >= rbtnY && mouseY <= rbtnY + rbtnH;
        drawButton(graphics, rbtnX, rbtnY, rbtnW, rbtnH,
                restartHovered ? 0xFF885500 : 0xFF553300,
                "Restart", 0xFFAA44);

        // Info panel
        drawInfoPanel(graphics, x, y);

        // Panel requête PNJ
        drawRequestPanel(graphics, x, y, mouseX, mouseY);

        // List area
        int listWidth = GUI_WIDTH - 26;
        int listStartY = getListStartY();
        graphics.fill(x + 6, listStartY - 1, x + GUI_WIDTH - 18, listStartY - 1 + MAX_VISIBLE * ENTRY_HEIGHT + 1, 0xFF373737);
        graphics.fill(x + 6, listStartY - 1, x + GUI_WIDTH - 18, listStartY, 0xFF8B8B8B);
        graphics.fill(x + 6, listStartY - 1, x + 7, listStartY - 1 + MAX_VISIBLE * ENTRY_HEIGHT + 1, 0xFF8B8B8B);

        int visibleCount = Math.min(MAX_VISIBLE, entries.size() - scrollOffset);

        List<Component> pendingTooltip = null;

        for (int i = 0; i < visibleCount; i++)
        {
            int index = i + scrollOffset;
            ColonyLinkPacket.ResourceEntry entry = entries.get(index);
            ItemStack stack = entry.stack();
            ResourceStatus status = entry.status();
            int realCount = entry.realCount();
            int entryY = listStartY + i * ENTRY_HEIGHT;

            int rowColor = (i % 2 == 0) ? 0xFF4A4A4A : 0xFF424242;
            graphics.fill(x + 7, entryY, x + 7 + listWidth, entryY + ENTRY_HEIGHT, rowColor);

            graphics.renderItem(stack, x + 9, entryY + 2);

            String text = realCount + "x " + stack.getDisplayName().getString();
            if (entry.isDomum())
                text = "§b[DO] §r" + text;
            graphics.drawString(this.font, text, x + 29, entryY + 6, 0xFFFFFF, false);

            // ── Ligne warehouse sous le nom ──────────────────────────────────
            WarehouseResultPacket.WarehouseEntry wareEntry = getWarehouseEntry(stack);
            if (wareEntry != null)
            {
                long total = wareEntry.inWarehouse() + wareEntry.viaCraft();
                String wareText;
                int wareColor;
                if (total >= realCount)
                {
                    wareText = "§aWH: " + total;
                    wareColor = 0x00FF88;
                }
                else if (total > 0)
                {
                    wareText = "§eWH: " + total + "/" + realCount;
                    wareColor = 0xFFCC44;
                }
                else
                {
                    wareText = "§cWH: 0";
                    wareColor = 0xFF4444;
                }
                // Affichage compact sous le nom (entryY + 13)
                graphics.drawString(this.font, wareText, x + 29, entryY + 13, wareColor, false);

                // Tooltip warehouse sur hover de la ligne (pas du bouton)
                boolean lineHovered = mouseX >= x + 7 && mouseX <= x + 7 + listWidth - 65
                        && mouseY >= entryY && mouseY <= entryY + ENTRY_HEIGHT;
                if (lineHovered && !wareEntry.tooltipLines().isEmpty())
                {
                    List<Component> wareTooltip = new ArrayList<>();
                    wareTooltip.add(Component.literal("§6Warehouse availability:"));
                    wareTooltip.add(Component.literal("§7  Direct: §a" + wareEntry.inWarehouse() + "x"));
                    wareTooltip.add(Component.literal("§7  Via craft: §e" + wareEntry.viaCraft() + "x"));
                    wareTooltip.add(Component.literal("§8──────────"));
                    for (String line : wareEntry.tooltipLines())
                        wareTooltip.add(Component.literal(line));
                    pendingTooltip = wareTooltip;
                }
            }

            int[] btn = new int[4];
            getBtnBounds(i, btn);
            int btnX = btn[0], btnY = btn[1], btnW = btn[2], btnH = btn[3];

            boolean hovered = mouseX >= btnX && mouseX <= btnX + btnW
                    && mouseY >= btnY && mouseY <= btnY + btnH;

            if (hovered && !entry.tooltipLines().isEmpty())
            {
                List<Component> tooltipComponents = new ArrayList<>();
                for (String line : entry.tooltipLines())
                    tooltipComponents.add(Component.literal(line));
                pendingTooltip = tooltipComponents;
            }

            int bgColor = getButtonColorWithWarehouse(status, stack, hovered && isButtonClickable(status, stack));

            graphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, bgColor);
            graphics.fill(btnX, btnY, btnX + btnW, btnY + 1, 0xFFFFFFFF);
            graphics.fill(btnX, btnY, btnX + 1, btnY + btnH, 0xFFFFFFFF);
            graphics.fill(btnX, btnY + btnH - 1, btnX + btnW, btnY + btnH, 0xFF373737);
            graphics.fill(btnX + btnW - 1, btnY, btnX + btnW, btnY + btnH, 0xFF373737);

            graphics.drawCenteredString(this.font, getButtonTextWithWarehouse(status, stack),
                    btnX + btnW / 2, btnY + 4, getButtonTextColor(status));
        }

        // Scrollbar
        if (entries.size() > MAX_VISIBLE)
        {
            int sbX = getScrollbarX();
            int sbTop = getScrollbarTop();
            int sbBottom = getScrollbarBottom();

            graphics.fill(sbX, sbTop, sbX + SCROLLBAR_WIDTH, sbBottom, 0xFF373737);
            graphics.fill(sbX, sbTop, sbX + 1, sbBottom, 0xFF8B8B8B);
            graphics.fill(sbX, sbTop, sbX + SCROLLBAR_WIDTH, sbTop + 1, 0xFF8B8B8B);

            int thumbY = getThumbY();
            int thumbH = getThumbHeight();

            graphics.fill(sbX + 1, thumbY, sbX + SCROLLBAR_WIDTH, thumbY + thumbH, 0xFF8B8B8B);
            graphics.fill(sbX + 1, thumbY, sbX + SCROLLBAR_WIDTH, thumbY + 1, 0xFFFFFFFF);
            graphics.fill(sbX + 1, thumbY, sbX + 2, thumbY + thumbH, 0xFFFFFFFF);
            graphics.fill(sbX + 1, thumbY + thumbH - 1, sbX + SCROLLBAR_WIDTH, thumbY + thumbH, 0xFF373737);
            graphics.fill(sbX + SCROLLBAR_WIDTH - 1, thumbY, sbX + SCROLLBAR_WIDTH, thumbY + thumbH, 0xFF373737);
        }

        // Separator
        graphics.fill(x + 6, y + GUI_HEIGHT - 44, x + GUI_WIDTH - 6, y + GUI_HEIGHT - 43, 0xFF555555);

        // Bouton Check Warehouse (ligne -40)
        drawWareCheckButton(graphics, mouseX, mouseY);

        // Switch priorité Warehouse/AE2 (même ligne, côté droit)
        drawPrioritySwitch(graphics, mouseX, mouseY);

        // Separator avant boutons principaux
        graphics.fill(x + 6, y + GUI_HEIGHT - 26, x + GUI_WIDTH - 6, y + GUI_HEIGHT - 25, 0xFF555555);

        // Craft All
        int caX = getCraftAllBtnX();
        int caY = getCraftAllBtnY();
        int caW = getCraftAllBtnW();
        int caH = getCraftAllBtnH();

        boolean craftAllHovered = mouseX >= caX && mouseX <= caX + caW
                && mouseY >= caY && mouseY <= caY + caH;
        boolean hasCraftable = hasCraftableItems();

        int craftAllBg = hasCraftable ? (craftAllHovered ? 0xFF007700 : 0xFF005500) : 0xFF333333;
        graphics.fill(caX, caY, caX + caW, caY + caH, craftAllBg);
        graphics.fill(caX, caY, caX + caW, caY + 1, 0xFFFFFFFF);
        graphics.fill(caX, caY, caX + 1, caY + caH, 0xFFFFFFFF);
        graphics.fill(caX, caY + caH - 1, caX + caW, caY + caH, 0xFF373737);
        graphics.fill(caX + caW - 1, caY, caX + caW, caY + caH, 0xFF373737);
        graphics.drawCenteredString(this.font, "Craft All",
                caX + caW / 2, caY + 4, hasCraftable ? 0x00FF00 : 0x888888);

        if (craftAllHovered && hasCraftable)
        {
            List<Component> craftAllTooltip = new ArrayList<>();
            craftAllTooltip.add(Component.literal("§aCraft All craftable items"));
            craftAllTooltip.add(Component.literal("§7" + availableCpus + " CPU"
                    + (availableCpus != 1 ? "s" : "") + " available"));
            craftAllTooltip.add(Component.literal("§8" + availableCpus
                    + " craft" + (availableCpus != 1 ? "s" : "") + " will run simultaneously"));
            pendingTooltip = craftAllTooltip;
        }

        // Send All
        int saX = getSendAllBtnX();
        int saY = getSendAllBtnY();
        int saW = getSendAllBtnW();
        int saH = getSendAllBtnH();

        boolean sendAllHovered = mouseX >= saX && mouseX <= saX + saW
                && mouseY >= saY && mouseY <= saY + saH;
        boolean hasAvailable = hasAvailableItems();

        int sendAllBg = hasAvailable ? (sendAllHovered ? 0xFF0066CC : 0xFF004488) : 0xFF333333;
        graphics.fill(saX, saY, saX + saW, saY + saH, sendAllBg);
        graphics.fill(saX, saY, saX + saW, saY + 1, 0xFFFFFFFF);
        graphics.fill(saX, saY, saX + 1, saY + saH, 0xFFFFFFFF);
        graphics.fill(saX, saY + saH - 1, saX + saW, saY + saH, 0xFF373737);
        graphics.fill(saX + saW - 1, saY, saX + saW, saY + saH, 0xFF373737);
        graphics.drawCenteredString(this.font, "Send All",
                saX + saW / 2, saY + 4, hasAvailable ? 0x4488FF : 0x888888);

        // Tooltip switch priorité
        if (hasWarehouseCard)
        {
            int sx = getSwitchX(), sy = getSwitchY(), sw = getSwitchW(), sh = getSwitchH();
            if (mouseX >= sx && mouseX <= sx + sw && mouseY >= sy && mouseY <= sy + sh)
            {
                List<Component> switchTooltip = new ArrayList<>();
                switchTooltip.add(Component.literal("§6Send Priority"));
                if (warehousePriority)
                {
                    switchTooltip.add(Component.literal("§a● Warehouse first"));
                    switchTooltip.add(Component.literal("§7Items pulled from Warehouse racks first,"));
                    switchTooltip.add(Component.literal("§7then ME network for the remainder."));
                }
                else
                {
                    switchTooltip.add(Component.literal("§9● AE2 first"));
                    switchTooltip.add(Component.literal("§7Items pulled from ME network first"));
                    switchTooltip.add(Component.literal("§7(default behaviour)."));
                }
                switchTooltip.add(Component.literal("§8Click to toggle."));
                pendingTooltip = switchTooltip;
            }
        }

        // Tooltip bouton Restart
        if (mouseX >= getRestartBtnX() && mouseX <= getRestartBtnX() + getRestartBtnW()
                && mouseY >= getRestartBtnY() && mouseY <= getRestartBtnY() + getRestartBtnH())
        {
            List<Component> restartTooltip = new ArrayList<>();
            restartTooltip.add(Component.literal("§6Restart Builder"));
            restartTooltip.add(Component.literal("§7Cancels current task and restarts the builder PNJ"));
            pendingTooltip = restartTooltip;
        }

        // Tooltip bouton Check Warehouse
        if (hasWarehouseCard)
        {
            int bx = getWareCheckBtnX();
            int by = getWareCheckBtnY();
            int bw = getWareCheckBtnW();
            int bh = getWareCheckBtnH();
            if (mouseX >= bx && mouseX <= bx + bw && mouseY >= by && mouseY <= by + bh)
            {
                List<Component> wareTooltip = new ArrayList<>();
                wareTooltip.add(Component.literal("§6Check Warehouse"));
                wareTooltip.add(Component.literal("§7Scans all Warehouse racks for available items."));
                wareTooltip.add(Component.literal("§7Resolves AE2 craft patterns recursively."));
                wareTooltip.add(Component.literal("§8Cooldown: 400 ticks (20s) between scans."));
                if (warehouseSnapshot != null)
                {
                    long ageMs = System.currentTimeMillis() - warehouseSnapshotReceivedMs;
                    long remainMs = Math.max(0, SNAPSHOT_VALIDITY_MS - ageMs);
                    wareTooltip.add(Component.literal("§7Snapshot expires in: §f" + (remainMs / 1000) + "s"));
                }
                pendingTooltip = wareTooltip;
            }
        }

        super.render(graphics, mouseX, mouseY, partialTick);

        if (pendingTooltip != null && !pendingTooltip.isEmpty())
            graphics.renderComponentTooltip(this.font, pendingTooltip, mouseX, mouseY);
    }

    /**
     * Retourne true si le snapshot warehouse actif couvre cet item via craft
     * (viaCraft > 0), ce qui signifie qu'on doit router vers WarehouseCraftPacket.
     */
    private boolean hasWarehouseCraft(ItemStack stack)
    {
        WarehouseResultPacket.WarehouseEntry entry = getWarehouseEntry(stack);
        return entry != null && (entry.viaCraft() > 0 || entry.inWarehouse() > 0);
    }

    // ── mouseClicked() ────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        // Bouton Restart
        int rbtnX = getRestartBtnX();
        int rbtnY = getRestartBtnY();
        int rbtnW = getRestartBtnW();
        int rbtnH = getRestartBtnH();

        if (mouseX >= rbtnX && mouseX <= rbtnX + rbtnW
                && mouseY >= rbtnY && mouseY <= rbtnY + rbtnH)
        {
            PacketDistributor.sendToServer(new RestartBuilderPacket(builderPos));
            return true;
        }

        // Switch priorité Warehouse/AE2
        if (hasWarehouseCard && !redirectorPos.equals(BlockPos.ZERO))
        {
            int sx = getSwitchX(), sy = getSwitchY(), sw = getSwitchW(), sh = getSwitchH();
            if (mouseX >= sx && mouseX <= sx + sw && mouseY >= sy && mouseY <= sy + sh)
            {
                PacketDistributor.sendToServer(new WarehousePriorityPacket(redirectorPos));
                // Mise à jour optimiste locale pour feedback immédiat sans attendre le ticker
                warehousePriority = !warehousePriority;
                return true;
            }
        }

        // Bouton Check Warehouse
        if (hasWarehouseCard && wareCheckState != WareCheckState.LOADING)
        {
            int bx = getWareCheckBtnX();
            int by = getWareCheckBtnY();
            int bw = getWareCheckBtnW();
            int bh = getWareCheckBtnH();

            if (mouseX >= bx && mouseX <= bx + bw && mouseY >= by && mouseY <= by + bh)
            {
                wareCheckState = WareCheckState.LOADING;
                PacketDistributor.sendToServer(new WarehouseCheckPacket(builderPos));
                return true;
            }
        }

        // Bouton requête PNJ
        boolean hasRequest = builderRequest != null
                && !builderRequest.stack().isEmpty()
                && builderRequest.count() > 0;

        if (hasRequest)
        {
            int reqBtnX = getReqBtnX();
            int reqBtnY = getReqBtnY();
            int reqBtnW = getReqBtnW();
            int reqBtnH = getReqBtnH();

            if (mouseX >= reqBtnX && mouseX <= reqBtnX + reqBtnW
                    && mouseY >= reqBtnY && mouseY <= reqBtnY + reqBtnH
                    && isButtonClickable(builderRequest.status()))
            {
                switch (builderRequest.status())
                {
                    case AVAILABLE ->
                            PacketDistributor.sendToServer(new SendToBuilderPacket(
                                    builderRequest.stack(), builderPos, builderRequest.count()));
                    case CRAFTABLE ->
                            PacketDistributor.sendToServer(new CraftRequestPacket(
                                    builderRequest.stack(), builderRequest.count(),
                                    false, BlockPos.ZERO, ResourceStatus.CRAFTABLE));
                    case MISSING ->
                            PacketDistributor.sendToServer(new CraftRequestPacket(
                                    builderRequest.stack(), builderRequest.count(),
                                    true, builderRequest.redirectorPos(), ResourceStatus.MISSING));
                    default -> {}
                }
                return true;
            }
        }

        // Craft All
        int caX = getCraftAllBtnX();
        int caY = getCraftAllBtnY();
        int caW = getCraftAllBtnW();
        int caH = getCraftAllBtnH();

        if (mouseX >= caX && mouseX <= caX + caW && mouseY >= caY && mouseY <= caY + caH
                && hasCraftableItems())
        {
            List<ItemStack> toCraft = new ArrayList<>();
            List<Integer> counts = new ArrayList<>();

            for (ColonyLinkPacket.ResourceEntry entry : entries)
            {
                if (entry.status() == ResourceStatus.CRAFTABLE)
                {
                    if (entry.isDomum())
                        PacketDistributor.sendToServer(new CraftRequestPacket(
                                entry.stack(), entry.realCount(), true,
                                entry.redirectorPos(), ResourceStatus.CRAFTABLE));
                    else
                    {
                        toCraft.add(entry.stack());
                        counts.add(entry.realCount());
                    }
                }
                else if (entry.status() == ResourceStatus.MISSING)
                {
                    PacketDistributor.sendToServer(new CraftRequestPacket(
                            entry.stack(), entry.realCount(), true,
                            entry.redirectorPos(), ResourceStatus.MISSING));
                }
            }

            if (!toCraft.isEmpty())
                PacketDistributor.sendToServer(new CraftAllRequestPacket(toCraft, counts));

            return true;
        }

        // Send All
        int saX = getSendAllBtnX();
        int saY = getSendAllBtnY();
        int saW = getSendAllBtnW();
        int saH = getSendAllBtnH();

        if (mouseX >= saX && mouseX <= saX + saW && mouseY >= saY && mouseY <= saY + saH
                && hasAvailableItems())
        {
            for (ColonyLinkPacket.ResourceEntry entry : entries)
            {
                if (entry.status() == ResourceStatus.AVAILABLE)
                    PacketDistributor.sendToServer(new SendToBuilderPacket(
                            entry.stack(), builderPos, entry.realCount()));
            }
            return true;
        }

        // Boutons individuels de la liste
        int visibleCount = Math.min(MAX_VISIBLE, entries.size() - scrollOffset);
        for (int i = 0; i < visibleCount; i++)
        {
            int index = i + scrollOffset;
            ColonyLinkPacket.ResourceEntry entry = entries.get(index);

            if (!isButtonClickable(entry.status(), entry.stack())) continue;

            int[] btn = new int[4];
            getBtnBounds(i, btn);
            int btnX = btn[0], btnY = btn[1], btnW = btn[2], btnH = btn[3];

            if (mouseX >= btnX && mouseX <= btnX + btnW
                    && mouseY >= btnY && mouseY <= btnY + btnH)
            {
                if (entry.status() == ResourceStatus.CRAFTABLE && entry.isDomum())
                {
                    // Domum : si snapshot warehouse couvre les composants → craft depuis warehouse
                    if (hasWarehouseCraft(entry.stack()))
                        PacketDistributor.sendToServer(new WarehouseCraftPacket(
                                entry.stack(), entry.realCount(), true, entry.redirectorPos()));
                    else
                        PacketDistributor.sendToServer(new CraftRequestPacket(
                                entry.stack(), entry.realCount(), true,
                                entry.redirectorPos(), ResourceStatus.CRAFTABLE));
                }
                else if (entry.status() == ResourceStatus.CRAFTABLE)
                {
                    // AE2 : si snapshot warehouse couvre les composants → injection ME avant craft
                    if (hasWarehouseCraft(entry.stack()))
                        PacketDistributor.sendToServer(new WarehouseCraftPacket(
                                entry.stack(), entry.realCount(), false, entry.redirectorPos()));
                    else
                        PacketDistributor.sendToServer(new CraftRequestPacket(
                                entry.stack(), entry.realCount(), false,
                                BlockPos.ZERO, ResourceStatus.CRAFTABLE));
                }
                else if (entry.status() == ResourceStatus.MISSING)
                    PacketDistributor.sendToServer(new CraftRequestPacket(
                            entry.stack(), entry.realCount(), true,
                            entry.redirectorPos(), ResourceStatus.MISSING));
                else if (entry.status() == ResourceStatus.AVAILABLE)
                    PacketDistributor.sendToServer(new SendToBuilderPacket(
                            entry.stack(), builderPos, entry.realCount()));
                else if (entry.status() == ResourceStatus.NO_PATTERN)
                {
                    // NO_PATTERN mais couvert par warehouse
                    WarehouseResultPacket.WarehouseEntry we = getWarehouseEntry(entry.stack());
                    if (we != null && we.inWarehouse() > 0)
                        // Item directement disponible en warehouse → Send depuis warehouse
                        PacketDistributor.sendToServer(new WarehouseCraftPacket(
                                entry.stack(), entry.realCount(), entry.isDomum(), entry.redirectorPos()));
                    else if (we != null && we.viaCraft() > 0)
                        // Craft possible depuis composants warehouse
                        PacketDistributor.sendToServer(new WarehouseCraftPacket(
                                entry.stack(), entry.realCount(), entry.isDomum(), entry.redirectorPos()));
                }
                return true;
            }
        }

        // Scrollbar drag
        if (entries.size() > MAX_VISIBLE)
        {
            int sbX = getScrollbarX();
            int thumbY = getThumbY();
            int thumbH = getThumbHeight();

            if (mouseX >= sbX && mouseX <= sbX + SCROLLBAR_WIDTH
                    && mouseY >= thumbY && mouseY <= thumbY + thumbH)
            {
                isDraggingScrollbar = true;
                dragStartY = mouseY;
                dragStartOffset = scrollOffset;
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        isDraggingScrollbar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY)
    {
        if (isDraggingScrollbar && entries.size() > MAX_VISIBLE)
        {
            int maxOffset = entries.size() - MAX_VISIBLE;
            double dragDelta = mouseY - dragStartY;
            double trackHeight = getScrollbarHeight() - getThumbHeight();
            int newOffset = (int) (dragStartOffset + dragDelta / trackHeight * maxOffset);
            scrollOffset = Math.max(0, Math.min(maxOffset, newOffset));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY)
    {
        int maxOffset = entries.size() - MAX_VISIBLE;
        if (scrollY < 0 && scrollOffset < maxOffset)
            scrollOffset++;
        else if (scrollY > 0 && scrollOffset > 0)
            scrollOffset--;
        return true;
    }

    @Override
    public boolean isPauseScreen() { return false; }

    public BlockPos getBuilderPos() { return builderPos; }
}