package com.colonylink.colonylink;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Warehouse Link Terminal — screen.
 *
 * Faithfully matches the sketch (apercu_terminal.png):
 *
 *  ┌──────────────────┬──────┬──────────────────┐
 *  │ Search...   [WH] │  ?   │ Search... [AE2]  │  ← header row
 *  │                  │  →   │                  │
 *  │  Warehouse 9×7   │  ←   │  Applied  9×7    │  ← item panels
 *  │                  │  ↓   │                  │
 *  │                  │  ⚙   │                  │
 *  ├──────────────────┴──────┴──────────────────┤
 *  │         Crafting Terminal label             │
 *  │  [←WH]  [←INV]  [3×3] [→]  [→ME] [→INV]  │  ← crafting row
 *  ├────────────────────────────────────────────┤
 *  │              Player Inventory              │
 *  └────────────────────────────────────────────┘
 *
 * Textures used:
 *   ae2:textures/guis/crafting.png  — AE2 ME Crafting Terminal atlas
 *   ae2:textures/guis/terminal.png  — AE2 ME Terminal atlas (item rows)
 *
 * All coordinates and srcRect values come verbatim from:
 *   assets/ae2/screens/terminals/crafting_terminal.json
 *   assets/ae2/screens/terminals/base_terminal.json
 *
 * Palette colours (ae2/screens/common/palette.json):
 *   DEFAULT_TEXT_COLOR  = 0x413F54
 *   MUTED_TEXT_COLOR    = 0x878FA5
 *   SELECTION_COLOR     = 0xACE9FF
 *   TEXTFIELD_PLACEHOLDER = 0xDEDFE3
 *   TEXTFIELD_TEXT      = 0xF2F2F2
 */
public class WarehouseLinkTerminalScreen extends AbstractContainerScreen<WarehouseLinkTerminalMenu>
{
    // ── AE2 textures ─────────────────────────────────────────────────────────

    private static final ResourceLocation TEX_CRAFTING =
            ResourceLocation.fromNamespaceAndPath("ae2", "textures/guis/crafting.png");
    private static final ResourceLocation TEX_TERMINAL =
            ResourceLocation.fromNamespaceAndPath("ae2", "textures/guis/terminal.png");

    private static final int TEX_W = 256, TEX_H = 256;

    // ── AE2 band srcRect values (from crafting_terminal.json / base_terminal.json) ──

    // header   [u=0, v=0,  w=195, h=17]
    // firstRow [u=0, v=17, w=195, h=18]
    // row      [u=0, v=35, w=195, h=18]
    // lastRow  [u=0, v=53, w=195, h=18]
    // bottom   [u=0, v=71, w=195, h=180]

    private static final int BAND_W         = 195;
    private static final int V_HEADER       = 0;   private static final int H_HEADER   = 17;
    private static final int V_FIRST_ROW    = 17;  private static final int H_FIRST_ROW = 18;
    private static final int V_ROW          = 35;  private static final int H_ROW       = 18;
    private static final int V_LAST_ROW     = 53;  private static final int H_LAST_ROW  = 18;
    private static final int V_BOTTOM       = 71;  private static final int H_BOTTOM    = 180;

    // ── AE2 palette colours ───────────────────────────────────────────────────

    private static final int C_DEFAULT_TEXT   = 0x413F54;
    private static final int C_MUTED          = 0x878FA5;
    private static final int C_SELECTION      = 0xACE9FF;
    private static final int C_PLACEHOLDER    = 0xDEDFE3;
    private static final int C_FIELD_TEXT     = 0xF2F2F2;

    // ── Layout constants ──────────────────────────────────────────────────────

    /** Number of item rows in each side panel */
    private static final int PANEL_ROWS  = 7;
    /** Columns in each side panel */
    private static final int PANEL_COLS  = 9;
    /** Slot size (px) — same as AE2 */
    private static final int SLOT        = 18;
    /** Width of each side panel = 9 × 18 = 162 */
    private static final int PANEL_W     = PANEL_COLS * SLOT; // 162
    /** Width of the centre column (buttons + card slot) */
    private static final int CTR_W       = 24;
    /** Scrollbar width */
    private static final int SCROLL_W    = 12;

    /** Total GUI width = WH_panel + scrollbar + centre + scrollbar + ME_panel */
    private static final int GUI_W =
            PANEL_W + SCROLL_W + CTR_W + SCROLL_W + PANEL_W; // 162+12+24+12+162 = 372

    // X offsets relative to leftPos
    private static final int X_WH     = 0;
    private static final int X_SCROLL_WH = PANEL_W;                    // 162
    private static final int X_CTR    = PANEL_W + SCROLL_W;            // 174
    private static final int X_SCROLL_ME = X_CTR + CTR_W;              // 198
    private static final int X_ME     = X_CTR + CTR_W + SCROLL_W;      // 210

    // Y of the header row top
    private static final int Y_HEADER   = 0;
    // Y where item rows start (after header)
    private static final int Y_ROWS     = H_HEADER;
    // Height of all item rows
    private static final int ROWS_H     = H_FIRST_ROW + (PANEL_ROWS - 2) * H_ROW + H_LAST_ROW; // 18+90+18=126
    // Y of bottom section (crafting area label)
    private static final int Y_BOTTOM   = Y_ROWS + ROWS_H; // 17+126=143

    // AE2 bottom band height (from crafting_terminal.json): h=180
    // The crafting grid sits inside the bottom band.
    // From crafting_terminal.json:
    //   CRAFTING_GRID  left=26, bottom=158  → top = H_BOTTOM - 158 = 22  → absolute = Y_BOTTOM+22
    //   CRAFTING_RESULT left=134, bottom=140 → top = H_BOTTOM - 140 = 40 → absolute = Y_BOTTOM+40

    private static final int CRAFT_GRID_LOCAL_TOP   = H_BOTTOM - 158; // =22
    private static final int CRAFT_RESULT_LOCAL_TOP = H_BOTTOM - 140; // =40
    private static final int CRAFT_RESULT_LOCAL_LEFT = 134;

    // Player inventory sits at bottom of the GUI.
    // From player_inventory.json: PLAYER_INVENTORY left=8 bottom=84, PLAYER_HOTBAR left=8 bottom=26
    // We render them inside the bottom band at the correct offsets.

    private static final int GUI_H = H_HEADER + ROWS_H + H_BOTTOM; // 17+126+180=323

    // ── State ─────────────────────────────────────────────────────────────────

    private final List<WarehouseTerminalSyncPacket.WarehouseItemEntry> whItems    = new ArrayList<>();
    private final List<WarehouseTerminalSyncPacket.WarehouseItemEntry> whFiltered = new ArrayList<>();
    private final List<MeItemEntry>                                     meItems    = new ArrayList<>();
    private final List<MeItemEntry>                                     meFiltered = new ArrayList<>();

    private boolean hasWarehouseCard = false;

    private String  whSearch = "", meSearch = "";
    private boolean whSearchFocused = false, meSearchFocused = false;

    private int whScroll = 0, meScroll = 0;
    private boolean draggingWhScroll = false, draggingMeScroll = false;

    private final Set<Integer> whSelected = new LinkedHashSet<>();
    private final Set<Integer> meSelected = new LinkedHashSet<>();

    private boolean warehouseFirst = true;

    // ── Constructor ───────────────────────────────────────────────────────────

    public WarehouseLinkTerminalScreen(WarehouseLinkTerminalMenu menu,
                                       Inventory playerInventory, Component title)
    {
        super(menu, playerInventory, title);
        this.imageWidth  = GUI_W;
        this.imageHeight = GUI_H;
    }

    @Override
    protected void init()
    {
        super.init();
        // Suppress default labels
        this.titleLabelX    = -10000;
        this.inventoryLabelX = -10000;

        PacketDistributor.sendToServer(new TerminalGuiStatePacket(
                true, partHostPos(), partSide()));
    }

    @Override
    public void onClose()
    {
        PacketDistributor.sendToServer(new TerminalGuiStatePacket(
                false, partHostPos(), partSide()));
        super.onClose();
    }

    // ── Packet handlers ───────────────────────────────────────────────────────

    public void updateWarehouseSnapshot(WarehouseTerminalSyncPacket packet)
    {
        whItems.clear();
        whItems.addAll(packet.entries());
        hasWarehouseCard = packet.hasWarehouseCard();
        rebuildWhFiltered();
    }

    public void updateMeSnapshot(TerminalMeSyncPacket packet)
    {
        meItems.clear();
        for (var e : packet.entries())
            meItems.add(new MeItemEntry(e.stack(), e.count(), e.craftable()));
        rebuildMeFiltered();
    }

    private void rebuildWhFiltered()
    {
        whFiltered.clear();
        String q = whSearch.toLowerCase();
        for (var e : whItems)
            if (q.isEmpty() || e.stack().getDisplayName().getString().toLowerCase().contains(q))
                whFiltered.add(e);
        whScroll = 0;
    }

    private void rebuildMeFiltered()
    {
        meFiltered.clear();
        String q = meSearch.toLowerCase();
        for (var e : meItems)
            if (q.isEmpty() || e.stack().getDisplayName().getString().toLowerCase().contains(q))
                meFiltered.add(e);
        meScroll = 0;
    }

    // ── renderBg ─────────────────────────────────────────────────────────────

    @Override
    protected void renderBg(GuiGraphics g, float pt, int mx, int my)
    {
        int x = leftPos, y = topPos;

        // ── 1. Warehouse panel (left) — terminal.png bands clipped to PANEL_W ──
        renderItemPanel(g, x + X_WH, y, true, mx, my);

        // ── 2. Centre column ────────────────────────────────────────────────
        renderCentreColumn(g, x + X_CTR, y, mx, my);

        // ── 3. ME panel (right) ─────────────────────────────────────────────
        renderItemPanel(g, x + X_ME, y, false, mx, my);

        // ── 4. Bottom band from crafting.png (crafting area + player inv) ──
        renderBottomBand(g, x, y, mx, my);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Item panel (Warehouse or ME)
    // Uses terminal.png bands clipped to PANEL_W
    // ─────────────────────────────────────────────────────────────────────────

    private void renderItemPanel(GuiGraphics g, int px, int py,
                                 boolean isWh, int mx, int my)
    {
        int cy = py;

        // header band (contains search bar area)
        g.blit(TEX_TERMINAL, px, cy, 0, V_HEADER, PANEL_W, H_HEADER, TEX_W, TEX_H);
        cy += H_HEADER;

        // firstRow
        g.blit(TEX_TERMINAL, px, cy, 0, V_FIRST_ROW, PANEL_W, H_FIRST_ROW, TEX_W, TEX_H);
        cy += H_FIRST_ROW;

        // middle rows
        for (int i = 0; i < PANEL_ROWS - 2; i++)
        {
            g.blit(TEX_TERMINAL, px, cy, 0, V_ROW, PANEL_W, H_ROW, TEX_W, TEX_H);
            cy += H_ROW;
        }

        // lastRow
        g.blit(TEX_TERMINAL, px, cy, 0, V_LAST_ROW, PANEL_W, H_LAST_ROW, TEX_W, TEX_H);

        // ── Panel title in header ─────────────────────────────────────────
        String title = isWh ? "Warehouse" : "Applied";
        // Center in header, same style as AE2 terminal titles
        g.drawString(font, title, px + PANEL_W / 2 - font.width(title) / 2, py + 5, C_DEFAULT_TEXT, false);

        // ── Search bar — base_terminal.json: left=80, top=4, width=89 ────
        // We adapt: left=4 (flush), width=PANEL_W-8
        int searchX = px + 4, searchY = py + 4;
        String search  = isWh ? whSearch : meSearch;
        boolean focused = isWh ? whSearchFocused : meSearchFocused;
        if (search.isEmpty())
            g.drawString(font, "Search...", searchX, searchY, C_PLACEHOLDER, false);
        else
            g.drawString(font, search, searchX, searchY, C_FIELD_TEXT, false);
        if (focused && (System.currentTimeMillis() / 500) % 2 == 0)
            g.drawString(font, "|", searchX + font.width(search), searchY, C_FIELD_TEXT, false);

        // ── Item count label (entriesShown) — right-aligned, top=8 ───────
        int total = isWh ? whFiltered.size() : meFiltered.size();
        String countStr = String.valueOf(total);
        g.drawString(font, countStr, px + PANEL_W - font.width(countStr) - 2, py + 8, C_MUTED, false);

        // ── Offline / no-card overlays ────────────────────────────────────
        int rowsTop = py + H_HEADER;
        int rowsH   = ROWS_H;

        if (isWh && !hasWarehouseCard)
        {
            g.fill(px, rowsTop, px + PANEL_W, rowsTop + rowsH, 0xBB111111);
            g.drawCenteredString(font, "Insert", px + PANEL_W / 2, rowsTop + rowsH / 2 - 10, 0xAAAAAA);
            g.drawCenteredString(font, "Warehouse Link Card",
                    px + PANEL_W / 2, rowsTop + rowsH / 2, 0xAAAAAA);
            // scrollbar area
            renderScrollbar(g, px + PANEL_W, rowsTop, SCROLL_W, rowsH, 0, 1, 0);
            return;
        }
        if (!isWh && !menu.getPart().isAe2Active())
        {
            g.fill(px, rowsTop, px + PANEL_W, rowsTop + rowsH, 0xBB220000);
            g.drawCenteredString(font, "AE2 Offline",
                    px + PANEL_W / 2, rowsTop + rowsH / 2, 0xFF4444);
            renderScrollbar(g, px + PANEL_W, rowsTop, SCROLL_W, rowsH, 0, 1, 0);
            return;
        }

        // ── Selection highlights ──────────────────────────────────────────
        Set<Integer> sel  = isWh ? whSelected : meSelected;
        int scroll        = isWh ? whScroll   : meScroll;
        int startIdx      = scroll * PANEL_COLS;
        int visibleRows   = PANEL_ROWS;

        for (int r = 0; r < visibleRows; r++)
            for (int c = 0; c < PANEL_COLS; c++)
            {
                int idx = startIdx + r * PANEL_COLS + c;
                if (sel.contains(idx))
                    g.fill(px + c * SLOT + 1, rowsTop + r * SLOT + 1,
                            px + c * SLOT + 17, rowsTop + r * SLOT + 17, 0x88 << 24 | C_SELECTION);
            }

        // ── Scrollbar ─────────────────────────────────────────────────────
        renderScrollbar(g, px + PANEL_W, rowsTop, SCROLL_W, rowsH,
                total, visibleRows * PANEL_COLS, scroll);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Centre column
    // ─────────────────────────────────────────────────────────────────────────

    private void renderCentreColumn(GuiGraphics g, int cx, int cy, int mx, int my)
    {
        // Background — fill with AE2 panel grey to blend in
        g.fill(cx, cy, cx + CTR_W, cy + GUI_H - H_BOTTOM, 0xFF969696);

        int btnY = cy + H_HEADER + 4;

        // ── Warehouse Link Card slot (?) ──────────────────────────────────
        // Shown at top of centre column, in the header area
        int cardX = cx + (CTR_W - 16) / 2;
        int cardY = cy + 1;
        // Slot background from terminal.png [0,35,16,16]
        g.blit(TEX_TERMINAL, cardX, cardY, 0, V_ROW, 16, 16, TEX_W, TEX_H);
        // Render card if present
        ItemStack card = menu.getPart().getWarehouseCardSlot().getStackInSlot(0);
        if (!card.isEmpty())
            g.renderItem(card, cardX, cardY);
        else
        {
            // "?" label
            g.drawCenteredString(font, "§8?", cx + CTR_W / 2, cardY + 4, C_MUTED);
        }

        // ── Transfer buttons ──────────────────────────────────────────────
        int bx = cx + (CTR_W - 16) / 2;

        // → (WH → ME)
        renderCtrBtn(g, mx, my, bx, btnY,      "→", 0);
        // ← (ME → WH)
        renderCtrBtn(g, mx, my, bx, btnY + 22, "←", 1);
        // ↓ (selected → INV)
        renderCtrBtn(g, mx, my, bx, btnY + 44, "↓", 2);
        // ⚙ (priority toggle)
        renderCtrBtn(g, mx, my, bx, btnY + 66, warehouseFirst ? "W" : "M", 3);

        // Small priority label below gear button
        String prioLabel = warehouseFirst ? "WH" : "ME";
        g.drawCenteredString(font, prioLabel, cx + CTR_W / 2, btnY + 66 + 17, C_MUTED);
    }

    private void renderCtrBtn(GuiGraphics g, int mx, int my,
                              int bx, int by, String label, int id)
    {
        boolean hov = mx >= bx && mx < bx + 16 && my >= by && my < by + 16;
        // Use terminal.png slot sprite as button background
        g.blit(TEX_TERMINAL, bx, by, 0, V_ROW, 16, 16, TEX_W, TEX_H);
        if (hov) g.fill(bx, by, bx + 16, by + 16, 0x55 << 24 | C_SELECTION);
        g.drawCenteredString(font, label, bx + 8, by + 4, C_DEFAULT_TEXT);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bottom band — crafting.png [0, 71, 195, 180], tiled to full GUI width
    // then crafting grid slots + player inventory drawn on top
    // ─────────────────────────────────────────────────────────────────────────

    private void renderBottomBand(GuiGraphics g, int x, int y, int mx, int my)
    {
        int by = y + Y_BOTTOM;

        // Tile the bottom band across the full GUI width
        // The 195-px band is tiled; we blit it in sections.
        int remaining = GUI_W;
        int bx        = x;
        while (remaining > 0)
        {
            int w = Math.min(remaining, BAND_W);
            g.blit(TEX_CRAFTING, bx, by, 0, V_BOTTOM, w, H_BOTTOM, TEX_W, TEX_H);
            bx        += w;
            remaining -= w;
        }

        // ── "Crafting Terminal" label ─────────────────────────────────────
        // crafting_terminal.json: text "gui.ae2.CraftingTerminal" left=8 bottom=177
        // bottom=177 → top = H_BOTTOM - 177 = 3
        g.drawString(font,
                Component.translatable("gui.ae2.CraftingTerminal").getString(),
                x + 8, by + 3, C_DEFAULT_TEXT, false);

        // ── Crafting grid (3×3) ───────────────────────────────────────────
        // crafting_terminal.json: left=26, bottom=158 → local top = 180-158=22
        // We centre the crafting area horizontally in the GUI
        int craftCentreX = x + GUI_W / 2;
        int craft3x3X    = craftCentreX - (3 * SLOT) / 2 - 30; // offset left to make room for result
        int craft3x3Y    = by + CRAFT_GRID_LOCAL_TOP;

        // Small arrow buttons to the left of the crafting grid
        // From sketch: [←WH] [←INV] on left side, [→ME] [→INV] on right side
        int arrLX = craft3x3X - 36;
        int arrRX = craft3x3X + 3 * SLOT + 8 + 20 + 4; // after output slot

        renderSmallArrow(g, mx, my, arrLX,      craft3x3Y + 9, "↑", 10); // → WH
        renderSmallArrow(g, mx, my, arrLX + 14, craft3x3Y + 9, "↓", 11); // → INV (left)
        renderSmallArrow(g, mx, my, arrRX,      craft3x3Y + 9, "↑", 12); // → ME
        renderSmallArrow(g, mx, my, arrRX + 14, craft3x3Y + 9, "↓", 13); // → INV (right)

        // crafting grid slot backgrounds (visual only — actual slots registered in menu)
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                g.blit(TEX_TERMINAL, craft3x3X + c * SLOT, craft3x3Y + r * SLOT,
                        0, V_ROW, 16, 16, TEX_W, TEX_H);

        // Arrow → output
        int arrowMidY = craft3x3Y + 3 * SLOT / 2 - 3;
        g.fill(craft3x3X + 3 * SLOT + 2, arrowMidY + 3,
                craft3x3X + 3 * SLOT + 16, arrowMidY + 4, 0xFF555555);
        g.fill(craft3x3X + 3 * SLOT + 13, arrowMidY + 1,
                craft3x3X + 3 * SLOT + 16, arrowMidY + 6, 0xFF555555);

        // Output slot
        int outX = craft3x3X + 3 * SLOT + 18;
        int outY = craft3x3Y + SLOT;
        g.blit(TEX_TERMINAL, outX, outY, 0, V_ROW, 16, 16, TEX_W, TEX_H);

        // ── Player inventory ──────────────────────────────────────────────
        // player_inventory.json: PLAYER_INVENTORY left=8 bottom=84, PLAYER_HOTBAR left=8 bottom=26
        // local top of inv  = H_BOTTOM - 84 = 96
        // local top of hotbar = H_BOTTOM - 26 = 154
        int invTop    = by + H_BOTTOM - 84;
        int hotbarTop = by + H_BOTTOM - 26;
        int invLeft   = x + 8;

        // "Inventory" label
        g.drawString(font,
                Component.translatable("container.inventory").getString(),
                invLeft, invTop - 10, C_DEFAULT_TEXT, false);

        // Inventory slots
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 9; c++)
                g.blit(TEX_TERMINAL,
                        invLeft + c * SLOT, invTop + r * SLOT,
                        0, V_ROW, 16, 16, TEX_W, TEX_H);

        // Hotbar separator line
        g.fill(invLeft, hotbarTop - 3, invLeft + 9 * SLOT, hotbarTop - 2, 0xFF888888);

        // Hotbar slots
        for (int c = 0; c < 9; c++)
            g.blit(TEX_TERMINAL,
                    invLeft + c * SLOT, hotbarTop,
                    0, V_ROW, 16, 16, TEX_W, TEX_H);
    }

    private void renderSmallArrow(GuiGraphics g, int mx, int my,
                                  int bx, int by, String label, int id)
    {
        boolean hov = mx >= bx && mx < bx + 12 && my >= by && my < by + 12;
        g.fill(bx, by, bx + 12, by + 12, hov ? 0xFF556677 : 0xFF445566);
        g.fill(bx, by, bx + 12, by + 1, 0xFF8899AA);
        g.drawCenteredString(font, label, bx + 6, by + 2, 0xFFFFFF);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Scrollbar
    // ─────────────────────────────────────────────────────────────────────────

    private void renderScrollbar(GuiGraphics g, int x, int y, int w, int h,
                                 int totalItems, int visible, int scroll)
    {
        g.fill(x, y, x + w, y + h, 0xFF1A1A1A);
        if (totalItems <= visible || totalItems == 0) return;
        float ratio  = (float) visible / totalItems;
        int   thumbH = Math.max(6, (int)(h * ratio));
        float maxS   = Math.max(1, totalItems - visible);
        int   thumbY = y + (int)((h - thumbH) * (scroll / maxS));
        g.fill(x + 1, thumbY, x + w - 1, thumbY + thumbH, 0xFF888888);
        g.fill(x + 1, thumbY, x + w - 1, thumbY + 1, 0xFFAAAAAA);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // render — items + tooltips on top
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt)
    {
        this.renderBackground(g, mx, my, pt);
        super.render(g, mx, my, pt);

        renderPanelItems(g, leftPos + X_WH, topPos, true);
        renderPanelItems(g, leftPos + X_ME,  topPos, false);

        renderTooltipForPanel(g, mx, my);
        this.renderTooltip(g, mx, my);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mx, int my) { /* suppressed */ }

    private void renderPanelItems(GuiGraphics g, int px, int py, boolean isWh)
    {
        if (isWh && !hasWarehouseCard) return;
        if (!isWh && !menu.getPart().isAe2Active()) return;

        int rowsTop  = py + H_HEADER;
        int scroll   = isWh ? whScroll : meScroll;
        int startIdx = scroll * PANEL_COLS;
        List<?> list = isWh ? whFiltered : meFiltered;

        for (int r = 0; r < PANEL_ROWS; r++)
        {
            for (int c = 0; c < PANEL_COLS; c++)
            {
                int idx = startIdx + r * PANEL_COLS + c;
                if (idx >= list.size()) return;

                int sx = px + c * SLOT + 1;
                int sy = rowsTop + r * SLOT + 1;

                ItemStack stack; long count; boolean craftable = false;
                if (isWh) { var e = whFiltered.get(idx); stack = e.stack(); count = e.count(); }
                else       { var e = meFiltered.get(idx); stack = e.stack(); count = e.count(); craftable = e.craftable(); }

                g.renderItem(stack, sx, sy);
                if (craftable && count <= 0)
                    g.fill(sx, sy, sx + 16, sy + 16, 0x4400CCAA);
                else
                    g.renderItemDecorations(font, stack, sx, sy, fmtCount(count));
            }
        }
    }

    private void renderTooltipForPanel(GuiGraphics g, int mx, int my)
    {
        // WH panel
        int slot = getPanelSlot(mx, my, leftPos + X_WH);
        if (slot >= 0 && hasWarehouseCard)
        {
            int idx = whScroll * PANEL_COLS + slot;
            if (idx < whFiltered.size())
            {
                var e = whFiltered.get(idx);
                g.renderComponentTooltip(font, List.of(
                        e.stack().getDisplayName(),
                        Component.literal("§7In warehouse: §f" + e.count())), mx, my);
                return;
            }
        }
        // ME panel
        slot = getPanelSlot(mx, my, leftPos + X_ME);
        if (slot >= 0 && menu.getPart().isAe2Active())
        {
            int idx = meScroll * PANEL_COLS + slot;
            if (idx < meFiltered.size())
            {
                var e = meFiltered.get(idx);
                g.renderComponentTooltip(font, List.of(
                        e.stack().getDisplayName(),
                        e.craftable() && e.count() <= 0
                                ? Component.literal("§aCraftable")
                                : Component.literal("§7In ME: §f" + e.count())), mx, my);
            }
        }
    }

    // ── Input ─────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mx, double my, int btn)
    {
        boolean shift = hasShiftDown();
        boolean ctrl  = hasControlDown();

        // ── Search bars ───────────────────────────────────────────────────
        if (my >= topPos + 4 && my < topPos + 4 + 9)
        {
            if (mx >= leftPos + X_WH + 4 && mx < leftPos + X_WH + PANEL_W - 4)
            { whSearchFocused = true; meSearchFocused = false; return true; }
            if (mx >= leftPos + X_ME + 4 && mx < leftPos + X_ME + PANEL_W - 4)
            { meSearchFocused = true; whSearchFocused = false; return true; }
        }
        whSearchFocused = false; meSearchFocused = false;

        // ── Warehouse panel ────────────────────────────────────────────────
        int whSlot = getPanelSlot((int)mx, (int)my, leftPos + X_WH);
        if (whSlot >= 0 && hasWarehouseCard)
        {
            int idx = whScroll * PANEL_COLS + whSlot;
            if (idx < whFiltered.size())
            {
                if (shift) { if (!whSelected.remove(idx)) whSelected.add(idx); meSelected.clear(); }
                else
                {
                    var e = whFiltered.get(idx);
                    TerminalTransferPacket.Direction dir = ctrl
                            ? TerminalTransferPacket.Direction.WH_TO_ME
                            : TerminalTransferPacket.Direction.WH_TO_PLAYER;
                    sendTransfer(e.stack(), (int)Math.min(e.count(), 64), dir);
                }
                return true;
            }
        }

        // ── ME panel ──────────────────────────────────────────────────────
        int meSlot = getPanelSlot((int)mx, (int)my, leftPos + X_ME);
        if (meSlot >= 0 && menu.getPart().isAe2Active())
        {
            int idx = meScroll * PANEL_COLS + meSlot;
            if (idx < meFiltered.size())
            {
                if (shift) { if (!meSelected.remove(idx)) meSelected.add(idx); whSelected.clear(); }
                else
                {
                    var e = meFiltered.get(idx);
                    TerminalTransferPacket.Direction dir = ctrl
                            ? TerminalTransferPacket.Direction.ME_TO_WH
                            : TerminalTransferPacket.Direction.ME_TO_PLAYER;
                    sendTransfer(e.stack(), (int)Math.min(e.count(), 64), dir);
                }
                return true;
            }
        }

        // ── Centre column buttons ─────────────────────────────────────────
        int bx  = leftPos + X_CTR + (CTR_W - 16) / 2;
        int btnY = topPos + H_HEADER + 4;

        if (over(mx, my, bx, btnY,      16, 16)) { sendBulk(true,  false, false); return true; } // WH→ME
        if (over(mx, my, bx, btnY + 22, 16, 16)) { sendBulk(false, true,  false); return true; } // ME→WH
        if (over(mx, my, bx, btnY + 44, 16, 16)) { sendBulk(true,  false, true);  return true; } // →INV
        if (over(mx, my, bx, btnY + 66, 16, 16)) { warehouseFirst = !warehouseFirst; return true; } // priority

        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double dx, double dy)
    {
        int rowsTop = topPos + H_HEADER, rowsBot = rowsTop + ROWS_H;

        if (my >= rowsTop && my < rowsBot)
        {
            if (mx >= leftPos + X_WH && mx < leftPos + X_WH + PANEL_W + SCROLL_W)
            {
                int maxS = maxScroll(whFiltered.size());
                whScroll = Math.max(0, Math.min(whScroll - (int)Math.signum(dy), maxS));
                return true;
            }
            if (mx >= leftPos + X_ME && mx < leftPos + X_ME + PANEL_W + SCROLL_W)
            {
                int maxS = maxScroll(meFiltered.size());
                meScroll = Math.max(0, Math.min(meScroll - (int)Math.signum(dy), maxS));
                return true;
            }
        }
        return super.mouseScrolled(mx, my, dx, dy);
    }

    @Override
    public boolean keyPressed(int key, int scan, int mod)
    {
        if (whSearchFocused)
        {
            if (key == 259 && !whSearch.isEmpty()) whSearch = whSearch.substring(0, whSearch.length() - 1);
            else if (key == 256) whSearchFocused = false;
            rebuildWhFiltered(); return true;
        }
        if (meSearchFocused)
        {
            if (key == 259 && !meSearch.isEmpty()) meSearch = meSearch.substring(0, meSearch.length() - 1);
            else if (key == 256) meSearchFocused = false;
            rebuildMeFiltered(); return true;
        }
        return super.keyPressed(key, scan, mod);
    }

    @Override
    public boolean charTyped(char c, int mod)
    {
        if (whSearchFocused) { whSearch += c; rebuildWhFiltered(); return true; }
        if (meSearchFocused) { meSearch += c; rebuildMeFiltered(); return true; }
        return super.charTyped(c, mod);
    }

    // ── Transfer helpers ──────────────────────────────────────────────────────

    private void sendTransfer(ItemStack stack, int count, TerminalTransferPacket.Direction dir)
    {
        PacketDistributor.sendToServer(new TerminalTransferPacket(
                stack, count, dir, partHostPos(), partSide()));
    }

    private void sendBulk(boolean fromWh, boolean toWh, boolean toPlayer)
    {
        Set<Integer> sel  = fromWh ? whSelected : meSelected;
        if (sel.isEmpty()) return;

        TerminalTransferPacket.Direction dir;
        if      (fromWh && !toPlayer) dir = TerminalTransferPacket.Direction.WH_TO_ME;
        else if (fromWh)              dir = TerminalTransferPacket.Direction.WH_TO_PLAYER;
        else if (toWh)                dir = TerminalTransferPacket.Direction.ME_TO_WH;
        else                          dir = TerminalTransferPacket.Direction.ME_TO_PLAYER;

        for (int idx : sel)
        {
            ItemStack stack; long count;
            if (fromWh)
            { if (idx >= whFiltered.size()) continue; var e = whFiltered.get(idx); stack = e.stack(); count = e.count(); }
            else
            { if (idx >= meFiltered.size()) continue; var e = meFiltered.get(idx); stack = e.stack(); count = e.count(); }
            sendTransfer(stack, (int)Math.min(count, 64), dir);
        }
        sel.clear();
    }

    // ── Geometry helpers ──────────────────────────────────────────────────────

    /** Returns flat slot index within the panel item grid, or -1 if outside. */
    private int getPanelSlot(int mx, int my, int panelX)
    {
        int rowsTop = topPos + H_HEADER;
        if (mx < panelX || mx >= panelX + PANEL_W) return -1;
        if (my < rowsTop || my >= rowsTop + PANEL_ROWS * SLOT) return -1;
        int col = (mx - panelX) / SLOT;
        int row = (my - rowsTop) / SLOT;
        if (col >= PANEL_COLS || row >= PANEL_ROWS) return -1;
        return row * PANEL_COLS + col;
    }

    private int maxScroll(int total)
    { return Math.max(0, (int)Math.ceil(total / (double)PANEL_COLS) - PANEL_ROWS); }

    private boolean over(double mx, double my, int bx, int by, int bw, int bh)
    { return mx >= bx && mx < bx + bw && my >= by && my < by + bh; }

    private static String fmtCount(long n)
    {
        if (n >= 1_000_000) return (n / 1_000_000) + "M";
        if (n >= 1_000)     return (n / 1_000) + "K";
        return String.valueOf(n);
    }

    // ── Part location helpers ─────────────────────────────────────────────────

    private net.minecraft.core.BlockPos partHostPos()
    {
        var be = menu.getPart().getHostBlockEntity();
        return be != null ? be.getBlockPos() : net.minecraft.core.BlockPos.ZERO;
    }

    private int partSide()
    {
        var side = menu.getPart().getSide();
        return side != null ? side.ordinal() : 0;
    }

    // ── Internal record ───────────────────────────────────────────────────────

    public record MeItemEntry(ItemStack stack, long count, boolean craftable) {}
}