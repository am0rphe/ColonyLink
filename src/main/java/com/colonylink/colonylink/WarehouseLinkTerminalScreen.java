package com.colonylink.colonylink;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

import static com.colonylink.colonylink.WarehouseLinkTerminalMenu.*;

/**
 * WarehouseLinkTerminalScreen — v2.0.0
 *
 * Refonte complète basée sur le fond PNG warehouse_terminalcolony.png (420×432 px).
 *
 * Textures utilisées :
 *   colonylink:textures/gui/warehouse_terminalcolony.png  — fond principal
 *   colonylink:textures/button/button_1.png               — bouton normal  (12×12)
 *   colonylink:textures/button/button_pushed.png          — bouton enfoncé (12×12)
 *   colonylink:textures/button/checkbox_wh.png            — slider WH actif (22×12)
 *   colonylink:textures/button/checkbox_ae.png            — slider AE actif (22×12)
 *   colonylink:textures/button/slider.png                 — thumb scrollbar (13×16)
 *
 * Layout :
 *   Panel WH : x=8..175,  y=31..227  (9 col × 11 lignes, pitch 18px)
 *   Panel AE : x=233..400, y=31..227
 *   Slot card : x=202..217, y=2..17
 *   Grille 3×3 : x=147..200, y=273..326
 *   Output : x=253..274, y=289..310
 *   Inventaire : x=130..289, y=348..401  (3×9)
 *   Hotbar : x=130..289, y=406..423
 *
 * Scrollbar verticale WH : x=176..185, thumb initial y=33, fin y=227
 * Scrollbar verticale AE : x=401..410, thumb initial y=33, fin y=227
 * Slider horizontal WH/ME : x=189..229, y=241..254 (checkbox 22×12)
 * Boutons push ×2 : (204,32) et (204,47), taille 11×11 (crop sur 12×12)
 * Boutons croix :
 *   7 clear→inv  : (136,273) 7×7
 *   8 result→WH  : (252,279) 7×7
 *   9 result→ME  : (268,279) 7×7
 *  10 result→inv : (260,313) 7×7
 */
@net.neoforged.api.distmarker.OnlyIn(net.neoforged.api.distmarker.Dist.CLIENT)
public class WarehouseLinkTerminalScreen extends AbstractContainerScreen<WarehouseLinkTerminalMenu>
{
    // ── Textures ──────────────────────────────────────────────────────────────
    private static final ResourceLocation TEX_BG =
            ResourceLocation.fromNamespaceAndPath("colonylink", "textures/gui/warehouse_terminalcolony.png");
    private static final ResourceLocation TEX_BTN =
            ResourceLocation.fromNamespaceAndPath("colonylink", "textures/button/button_1.png");
    private static final ResourceLocation TEX_BTN_PUSHED =
            ResourceLocation.fromNamespaceAndPath("colonylink", "textures/button/button_pushed.png");
    private static final ResourceLocation TEX_CHK_WH =
            ResourceLocation.fromNamespaceAndPath("colonylink", "textures/button/checkbox_wh.png");
    private static final ResourceLocation TEX_CHK_AE =
            ResourceLocation.fromNamespaceAndPath("colonylink", "textures/button/checkbox_ae.png");
    private static final ResourceLocation TEX_SLIDER =
            ResourceLocation.fromNamespaceAndPath("colonylink", "textures/button/slider.png");

    // ── Couleurs texte / overlays ─────────────────────────────────────────────
    private static final int C_TEXT_WH      = 0xFF9A9FB4;
    private static final int C_TEXT_AE      = 0xFF6699FF;
    private static final int C_TEXT_MUTED   = 0xFF9A9FB4;
    private static final int C_SF_TEXT      = 0xFFCCCDFF;
    private static final int C_SF_PH        = 0xFF555577;
    private static final int C_HOVER_SL     = 0x5500CCFF;
    private static final int C_SELECTED     = 0x8800AAFF;
    private static final int C_CRAFTABLE_DOT= 0xFF00FF88;
    private static final int C_CRAFTONLY_OV = 0x4400AAFF;
    private static final int C_OFFLINE_OV   = 0xBB220000;
    private static final int C_NOCARD_OV    = 0xBB111122;
    private static final int C_COUNT_TEXT   = 0xFFFFFFFF;
    private static final int C_COUNT_SHADOW = 0xFF3F3F3F;
    private static final float COUNT_FONT_SCALE = 0.72f;

    // ── État ──────────────────────────────────────────────────────────────────
    private final List<WarehouseTerminalSyncPacket.WarehouseItemEntry> whItems    = new ArrayList<>();
    private final List<WarehouseTerminalSyncPacket.WarehouseItemEntry> whFiltered = new ArrayList<>();
    private final List<MeItemEntry> meItems = new ArrayList<>(), meFiltered = new ArrayList<>();

    private boolean hasWarehouseCard = false;
    private String  whErrorMsg = "";

    private static String savedWhSearch = "";
    private static String savedMeSearch = "";
    private boolean closedManually = false;

    private String  whSearch = "", meSearch = "";
    private boolean whSearchFocused = false, meSearchFocused = false;
    private int     whScroll = 0, meScroll = 0;

    private boolean warehouseFirst = true;

    private final Set<Integer> whSelected = new LinkedHashSet<>();
    private final Set<Integer> meSelected = new LinkedHashSet<>();
    private boolean shiftWasDown = false;

    // État drag scrollbars
    private boolean draggingWh = false, draggingAe = false;
    private int     dragStartY = 0, dragStartScroll = 0;
    private int     dragStartSlotWh = -1, dragStartSlotAe = -1; // slot où le drag a commencé

    // ── État animation boutons push
    private boolean btn1Pushed = false, btn2Pushed = false;

    private String hoveredTooltip = null;

    // ── Fit-to-window scale ───────────────────────────────────────────────────
    // Si le GUI est plus grand que la fenêtre de jeu (GUI scale 3/4+),
    // on le réduit automatiquement pour qu'il tienne entièrement à l'écran.
    private float guiScale = 1.0f;

    private float computeFitScale()
    {
        if (minecraft == null) return 1.0f;
        // Taille de la fenêtre en coords GUI (après division par guiScale Minecraft)
        int sw = this.width;
        int sh = this.height;
        float fx = (float) sw / GUI_W;
        float fy = (float) sh / GUI_H;
        float fit = Math.min(fx, fy);
        return fit < 1.0f ? fit : 1.0f; // on ne scale jamais au-dessus de 1.0
    }

    private int toGui(double v, float pivot, float scale)
    { return scale == 1.0f ? (int) v : (int)((v - pivot) / scale + pivot); }

    // ── Constructeur ──────────────────────────────────────────────────────────
    public WarehouseLinkTerminalScreen(WarehouseLinkTerminalMenu menu,
                                       Inventory inv, Component title)
    {
        super(menu, inv, title);
        imageWidth  = GUI_W;
        imageHeight = GUI_H;
    }

    @Override protected void init()
    {
        imageWidth  = GUI_W;
        imageHeight = GUI_H;
        super.init();
        guiScale = computeFitScale();
        titleLabelX = inventoryLabelX = -10000;
        whSearch = savedWhSearch;
        meSearch = savedMeSearch;
        rebuildWh();
        rebuildMe();
        WarehouseLinkTerminalMenu.warehouseFirst = warehouseFirst;
        PacketDistributor.sendToServer(new TerminalGuiStatePacket(true, partHostPos(), partSide()));
    }

    @Override public void onClose()
    {
        closedManually = true;
        savedWhSearch = "";
        savedMeSearch = "";
        PacketDistributor.sendToServer(new TerminalGuiStatePacket(false, partHostPos(), partSide()));
        super.onClose();
    }

    @Override public void removed()
    {
        if (!closedManually)
        {
            savedWhSearch = whSearch;
            savedMeSearch = meSearch;
        }
        super.removed();
    }

    // ── Mises à jour depuis packets ───────────────────────────────────────────
    public void updateWarehouseSnapshot(WarehouseTerminalSyncPacket p)
    {
        whItems.clear(); whItems.addAll(p.entries());
        hasWarehouseCard = p.hasWarehouseCard();
        whErrorMsg = p.hasError() ? p.errorMessage() : "";
        rebuildWh();
    }

    public void updateMeSnapshot(TerminalMeSyncPacket p)
    {
        meItems.clear();
        for (var e : p.entries())
            meItems.add(new MeItemEntry(e.stack(), e.count(), e.craftable()));
        rebuildMe();
    }

    private void rebuildWh()
    {
        whFiltered.clear();
        String q = whSearch.toLowerCase();
        for (var e : whItems)
            if (q.isEmpty() || e.stack().getDisplayName().getString().toLowerCase().contains(q))
                whFiltered.add(e);
        whScroll = clamp(whScroll, whFiltered.size());
    }

    private void rebuildMe()
    {
        meFiltered.clear();
        String q = meSearch.toLowerCase();
        for (var e : meItems)
            if (q.isEmpty() || e.stack().getDisplayName().getString().toLowerCase().contains(q))
                meFiltered.add(e);
        meScroll = clamp(meScroll, meFiltered.size());
    }

    // =========================================================================
    // RENDER PRINCIPAL
    // =========================================================================
    @Override public void render(GuiGraphics g, int rawMx, int rawMy, float pt)
    {
        hoveredTooltip = null;
        boolean shiftNow = hasShiftDown();
        if (shiftWasDown && !shiftNow) { whSelected.clear(); meSelected.clear(); }
        shiftWasDown = shiftNow;

        float cx = this.width  / 2f;
        float cy = this.height / 2f;
        int mx = toGui(rawMx, cx, guiScale);
        int my = toGui(rawMy, cy, guiScale);

        // Rectangle opaque plein écran en coords brutes, avant la pose
        // 0xD8000000 = noir à 85% opacité — correspond au voile vanilla renderBackground
        g.fill(0, 0, this.width, this.height, 0xD8000000);

        if (guiScale != 1.0f)
        {
            g.pose().pushPose();
            g.pose().translate(cx, cy, 0);
            g.pose().scale(guiScale, guiScale, 1f);
            g.pose().translate(-cx, -cy, 0);
        }

        super.render(g, mx, my, pt);
        renderPanelItems(g, leftPos + X_ITEMS_WH, topPos, true);
        renderPanelItems(g, leftPos + X_ITEMS_AE, topPos, false);
        renderTooltipForPanel(g, mx, my);

        if (guiScale != 1.0f)
            g.pose().popPose();

        renderTooltip(g, rawMx, rawMy);

        if (hoveredTooltip != null)
        {
            String[] lines = hoveredTooltip.split("\n");
            if (lines.length == 1)
                g.renderTooltip(font, Component.literal(hoveredTooltip), rawMx, rawMy);
            else
            {
                List<Component> ttLines = new ArrayList<>();
                for (String line : lines) ttLines.add(Component.literal(line));
                g.renderComponentTooltip(font, ttLines, rawMx, rawMy);
            }
        }
    }

    @Override protected void renderLabels(GuiGraphics g, int mx, int my) {}

    // ── renderBg : fond + tous les widgets ───────────────────────────────────
    @Override protected void renderBg(GuiGraphics g, float pt, int mx, int my)
    {
        int x = leftPos, y = topPos;

        // 1. Fond principal (texture PNG complète)
        g.blit(TEX_BG, x, y, 0, 0, GUI_W, GUI_H, GUI_W, GUI_H);

        // 2. Labels texte WH / AE (au dessus des search bars)
        renderPanelLabels(g, x, y);

        // 3. Search bars
        renderSearch(g, x + SEARCH_WH_X, y + SEARCH_Y, SEARCH_WH_W, SEARCH_H,
                whSearch, whSearchFocused, whFiltered.size(), true);
        renderSearch(g, x + SEARCH_AE_X, y + SEARCH_Y, SEARCH_AE_W, SEARCH_H,
                meSearch, meSearchFocused, meFiltered.size(), false);

        // 4. Scrollbars verticales
        renderScrollbar(g, x + SCROLL_WH_X, y, whFiltered.size(), whScroll);
        renderScrollbar(g, x + SCROLL_AE_X, y, meFiltered.size(), meScroll);

        // 5. Slider horizontal WH/ME
        renderSlider(g, x, y, mx, my);

        // 6. Boutons push centre colonne
        renderPushButton(g, mx, my, x + BTN1_X, y + BTN1_Y, btn1Pushed,
                "Transfer selected WH \u2194 ME\n\u00a77Shift+click to select items");
        renderPushButton(g, mx, my, x + BTN2_X, y + BTN2_Y, btn2Pushed,
                "Transfer selected \u2192 Inventory\n\u00a77Shift+click to select items");

        // 7. Tooltips des boutons croix craft (hitboxes invisibles sur fond PNG)
        if (over(mx, my, x + 136, y + 273, 7, 7))
            hoveredTooltip = "Clear crafting grid \u2192 Inventory\n\u00a77Shift: also clear output";
        if (over(mx, my, x + 252, y + 279, 7, 7))
            hoveredTooltip = "Output \u2192 Warehouse\n\u00a77Shift: full stack";
        if (over(mx, my, x + 268, y + 279, 8, 7))
            hoveredTooltip = "Output \u2192 ME Network\n\u00a77Shift: full stack";
        if (over(mx, my, x + 260, y + 313, 7, 7))
            hoveredTooltip = "Output \u2192 Inventory";

        // 8. Sélection count sur panels
        // WH : x=136..191, y=15..24 — par-dessus la search bar droite
        if (!whSelected.isEmpty())
            g.drawString(font, "\u00a7e" + whSelected.size() + " selected",
                    x + 136, y + 15, 0xFFFFDD00, false);
        // AE : x=361..416, y=15..24 — par-dessus la search bar droite AE
        if (!meSelected.isEmpty())
            g.drawString(font, "\u00a7e" + meSelected.size() + " selected",
                    x + 361, y + 15, 0xFFFFDD00, false);
    }

    // ── Labels WH / AE au dessus des search bars ──────────────────────────────
    private void renderPanelLabels(GuiGraphics g, int x, int y)
    {
        // Zone label WH : x=9..133, y=2..14
        String wh = "Warehouse";
        String whCnt = String.valueOf(whFiltered.size());
        g.drawString(font, wh,    x + 9, y + 3, C_TEXT_WH, false);
        g.drawString(font, whCnt, x + 9 + SEARCH_WH_W - font.width(whCnt) - 1, y + 3, C_TEXT_MUTED, false);

        // Zone label AE : x=233..358, y=2..14
        String ae = "Applied";
        String aeCnt = String.valueOf(meFiltered.size());
        g.drawString(font, ae,    x + SEARCH_AE_X, y + 3, C_TEXT_AE, false);
        g.drawString(font, aeCnt, x + SEARCH_AE_X + SEARCH_AE_W - font.width(aeCnt) - 1, y + 3, C_TEXT_MUTED, false);
    }

    // ── Search bar ────────────────────────────────────────────────────────────
    private void renderSearch(GuiGraphics g, int x, int y, int w, int h,
                              String txt, boolean focused, int count, boolean isWh)
    {
        // Le fond de la search bar est déjà dans le PNG ; on dessine juste le texte
        if (txt.isEmpty())
            g.drawString(font, "Search...", x + 2, y + 1, C_SF_PH, false);
        else
            g.drawString(font, txt, x + 2, y + 1, C_SF_TEXT, false);
        if (focused && (System.currentTimeMillis() / 500) % 2 == 0)
            g.drawString(font, "|", x + 2 + font.width(txt), y + 1, C_SF_TEXT, false);
    }

    // ── Scrollbar verticale ───────────────────────────────────────────────────
    // Track dans le PNG : 10px large (x=176..185 WH, x=401..410 AE)
    // Rail actif central : 4px (x=179..182 WH, x=404..407 AE)
    // slider.png 13×16 : coins noirs x=0 et x=12 ; corps utile x=1..11 = 11px
    // → blit srcX=1, w=11 ; dest trackX-1 pour centrer le thumb 11px sur la track 10px
    private static final int SCROLL_THUMB_H = 16;

    private void renderScrollbar(GuiGraphics g, int trackX, int guiY,
                                 int totalItems, int scroll)
    {
        if (totalItems <= 0) return;
        int maxRows   = (int) Math.ceil(totalItems / (double) PANEL_COLS);
        int maxScroll = maxRows - PANEL_ROWS;
        if (maxScroll <= 0) return;

        int trackH = SCROLL_BOT_Y - SCROLL_TOP_Y; // 194px
        int thumbY = guiY + SCROLL_TOP_Y
                + (int) ((trackH - SCROLL_THUMB_H) * (float) scroll / maxScroll);

        // Corps utile du PNG = cols 1..11 = 11px
        // On décale d'1px à gauche (trackX-1) pour centrer sur la track 10px
        g.blit(TEX_SLIDER,
                trackX - 1, thumbY,  // dest : 1px à gauche de la track
                1, 0,                // src  : skip coin gauche (col 0)
                11,                  // dest width = 11px (corps complet)
                SCROLL_THUMB_H,      // dest height = 16px
                13, 16);             // texture full size
    }

    // ── Slider WH/ME (horizontal, un seul checkbox centré) ───────────────────
    private void renderSlider(GuiGraphics g, int x, int y, int mx, int my)
    {
        int sx = x + SLIDER_X;
        int sy = y + SLIDER_Y;
        // Un seul checkbox 22×12 centré dans la zone 40×13
        // WH first → checkbox_wh.png, AE first → checkbox_ae.png
        ResourceLocation tex = warehouseFirst ? TEX_CHK_WH : TEX_CHK_AE;
        int offsetX = (SLIDER_W - 22) / 2;  // (40-22)/2 = 9 → centré
        g.blit(tex, sx + offsetX, sy, 0, 0, 22, 12, 22, 12);

        if (over(mx, my, sx, sy, SLIDER_W, SLIDER_H))
            hoveredTooltip = warehouseFirst
                    ? "JEI priority: Warehouse first"
                    : "JEI priority: ME first";
    }

    // ── Bouton push (centre colonne) ──────────────────────────────────────────
    private void renderPushButton(GuiGraphics g, int mx, int my,
                                  int bx, int by, boolean pushed, String tooltip)
    {
        boolean hov = over(mx, my, bx, by, BTN_SZ, BTN_SZ);
        ResourceLocation tex = pushed ? TEX_BTN_PUSHED : TEX_BTN;
        // Les textures font 12×12 ; on les dessine à 11×11 (BTN_SZ) en décalant d'1px
        g.blit(tex, bx, by, 0, 0, BTN_SZ, BTN_SZ, 12, 12);
        if (hov) hoveredTooltip = tooltip;
    }

    // ── Items des panels ──────────────────────────────────────────────────────
    private void renderPanelItems(GuiGraphics g, int panelX, int guiY, boolean isWh)
    {
        if (isWh && (!hasWarehouseCard || !whErrorMsg.isEmpty()))
        {
            // Overlay "pas de card"
            g.fill(panelX, guiY + Y_ITEMS,
                    panelX + PANEL_COLS * SLOT_PITCH,
                    guiY + Y_ITEMS + PANEL_ROWS * SLOT_PITCH,
                    C_NOCARD_OV);
            String msg1 = "Insert Warehouse";
            String msg2 = "Link Card";
            int cx = panelX + (PANEL_COLS * SLOT_PITCH) / 2;
            int cy = guiY + Y_ITEMS + (PANEL_ROWS * SLOT_PITCH) / 2;
            g.drawCenteredString(font, msg1, cx, cy - 10, 0xFF8888BB);
            g.drawCenteredString(font, msg2, cx, cy + 2, 0xFF8888BB);
            return;
        }
        if (isWh && !whErrorMsg.isEmpty())
        {
            g.fill(panelX, guiY + Y_ITEMS,
                    panelX + PANEL_COLS * SLOT_PITCH,
                    guiY + Y_ITEMS + PANEL_ROWS * SLOT_PITCH,
                    C_NOCARD_OV);
            g.drawCenteredString(font, whErrorMsg,
                    panelX + (PANEL_COLS * SLOT_PITCH) / 2,
                    guiY + Y_ITEMS + (PANEL_ROWS * SLOT_PITCH) / 2, 0xFF8888BB);
            return;
        }
        if (!isWh && !menu.getPart().isAe2Active())
        {
            g.fill(panelX, guiY + Y_ITEMS,
                    panelX + PANEL_COLS * SLOT_PITCH,
                    guiY + Y_ITEMS + PANEL_ROWS * SLOT_PITCH,
                    C_OFFLINE_OV);
            g.drawCenteredString(font, "AE2 Offline",
                    panelX + (PANEL_COLS * SLOT_PITCH) / 2,
                    guiY + Y_ITEMS + (PANEL_ROWS * SLOT_PITCH) / 2, 0xFFFF4444);
            return;
        }

        int scroll = isWh ? whScroll : meScroll;
        Set<Integer> sel = isWh ? whSelected : meSelected;
        List<?> list = isWh ? whFiltered : meFiltered;

        for (int r = 0; r < PANEL_ROWS; r++)
        {
            for (int c = 0; c < PANEL_COLS; c++)
            {
                int idx = scroll * PANEL_COLS + r * PANEL_COLS + c;
                if (idx >= list.size()) return;

                int sx = panelX + c * SLOT_PITCH;
                int sy = guiY + Y_ITEMS + r * SLOT_PITCH;
                boolean hov = minecraft.mouseHandler.xpos() - leftPos >= sx
                        && minecraft.mouseHandler.xpos() - leftPos < sx + SLOT_SZ
                        && minecraft.mouseHandler.ypos() - topPos >= sy
                        && minecraft.mouseHandler.ypos() - topPos < sy + SLOT_SZ;

                if (sel.contains(idx))      g.fill(sx, sy, sx + SLOT_SZ, sy + SLOT_SZ, C_SELECTED);
                else if (hov)               g.fill(sx, sy, sx + SLOT_SZ, sy + SLOT_SZ, C_HOVER_SL);

                ItemStack stack; long count; boolean craftable = false;
                if (isWh) { var e = whFiltered.get(idx); stack = e.stack(); count = e.count(); }
                else      { var e = meFiltered.get(idx); stack = e.stack(); count = e.count(); craftable = e.craftable(); }

                g.renderItem(stack, sx, sy);
                g.renderItemDecorations(font, stack, sx, sy, "");

                if (!isWh)
                {
                    if (craftable && count <= 0)
                    {
                        g.fill(sx, sy, sx + SLOT_SZ, sy + SLOT_SZ, C_CRAFTONLY_OV);
                        g.drawString(font, "+", sx + 4, sy + 4, 0xFF00CCFF, true);
                        continue;
                    }
                    if (craftable) g.fill(sx + 13, sy, sx + 16, sy + 3, C_CRAFTABLE_DOT);
                }

                if (count > 0) drawCount(g, sx, sy, count);
            }
        }
    }

    // ── Compteur items (style AE2) ────────────────────────────────────────────
    private void drawCount(GuiGraphics g, int slotX, int slotY, long count)
    {
        String txt   = fmtCount(count);
        float  scale = COUNT_FONT_SCALE;
        int    wRaw  = font.width(txt);
        int    hRaw  = font.lineHeight;
        float  drawX = (slotX + 15) / scale - wRaw;
        float  drawY = (slotY + 15) / scale - hRaw;

        var pose = g.pose();
        pose.pushPose();
        pose.translate(0f, 0f, 200f);
        pose.scale(scale, scale, 1f);
        g.drawString(font, txt, (int) Math.round(drawX) + 1, (int) Math.round(drawY) + 1, C_COUNT_SHADOW, false);
        g.drawString(font, txt, (int) Math.round(drawX),     (int) Math.round(drawY),     C_COUNT_TEXT,   false);
        pose.popPose();
    }

    // ── Tooltips items ────────────────────────────────────────────────────────
    private void renderTooltipForPanel(GuiGraphics g, int mx, int my)
    {
        // WH
        int s = itemSlot(mx, my, leftPos + X_ITEMS_WH);
        if (s >= 0 && hasWarehouseCard && whErrorMsg.isEmpty())
        {
            int i = whScroll * PANEL_COLS + s;
            if (i < whFiltered.size())
            {
                var e = whFiltered.get(i);
                g.renderComponentTooltip(font, List.of(
                                e.stack().getDisplayName(),
                                Component.literal("\u00a77In warehouse: \u00a7f" + e.count()),
                                Component.literal("\u00a78Left: pick  Right: half  Shift+drag: select")),
                        mx, my);
                return;
            }
        }
        // AE
        s = itemSlot(mx, my, leftPos + X_ITEMS_AE);
        if (s >= 0 && menu.getPart().isAe2Active())
        {
            int i = meScroll * PANEL_COLS + s;
            if (i < meFiltered.size())
            {
                var e = meFiltered.get(i);
                List<Component> lines = new ArrayList<>();
                lines.add(e.stack().getDisplayName());
                if (e.craftable() && e.count() <= 0)
                    lines.add(Component.literal("\u00a7aCraftable-only \u00a7e(middle-click to autocraft)"));
                else if (e.craftable())
                    lines.add(Component.literal("\u00a77In ME: \u00a7f" + e.count() + " \u00a7a\u25cf craftable"));
                else
                    lines.add(Component.literal("\u00a77In ME: \u00a7f" + e.count()));
                lines.add(Component.literal("\u00a78Left: pick  Right: half  Middle: autocraft"));
                g.renderComponentTooltip(font, lines, mx, my);
            }
        }
    }

    // =========================================================================
    // INPUT
    // =========================================================================
    @Override public boolean mouseClicked(double mx, double my, int btn)
    {
        float cx = this.width / 2f, cy = this.height / 2f;
        mx = toGui(mx, cx, guiScale); my = toGui(my, cy, guiScale);

        boolean shift = hasShiftDown();

        // ── Shift+clic inventaire → WH ou ME ────────────────────────────────
        if (shift && !hasControlDown()
                && mx >= leftPos + INV_X && mx < leftPos + INV_X + 9 * SLOT_PITCH)
        {
            boolean canSendToWh = hasWarehouseCard && whErrorMsg.isEmpty();
            boolean canSendToMe = menu.getPart().isAe2Active();
            boolean canSend     = warehouseFirst ? canSendToWh : canSendToMe;

            if (canSend)
            {
                int invSlotIdx = -1;
                if (my >= topPos + INV_Y && my < topPos + INV_Y + 3 * SLOT_PITCH)
                {
                    int r = ((int) my - topPos - INV_Y) / SLOT_PITCH;
                    int c = ((int) mx - leftPos - INV_X) / SLOT_PITCH;
                    invSlotIdx = PLAYER_INV_START + r * 9 + c;
                }
                else if (my >= topPos + HOTBAR_Y && my < topPos + HOTBAR_Y + SLOT_PITCH)
                {
                    int c = ((int) mx - leftPos - INV_X) / SLOT_PITCH;
                    invSlotIdx = PLAYER_HOTBAR_START + c;
                }
                if (invSlotIdx >= 0 && invSlotIdx < menu.slots.size())
                {
                    ItemStack invStack = menu.slots.get(invSlotIdx).getItem();
                    if (!invStack.isEmpty())
                    {
                        TerminalTransferPacket.Direction dir = warehouseFirst
                                ? TerminalTransferPacket.Direction.INV_TO_WH
                                : TerminalTransferPacket.Direction.INV_TO_ME;
                        PacketDistributor.sendToServer(new TerminalTransferPacket(
                                invStack.copy(), invStack.getCount(), dir, partHostPos(), partSide()));
                        menu.slots.get(invSlotIdx).set(ItemStack.EMPTY);
                        return true;
                    }
                }
            }
            if (my >= topPos + INV_Y && my < topPos + HOTBAR_Y + SLOT_PITCH) return true;
        }

        // ── Drag scrollbar WH ─────────────────────────────────────────────────
        // Hitbox alignée sur le thumb rendu : trackX-1, width=11
        if (over(mx, my, leftPos + SCROLL_WH_X - 1, topPos + SCROLL_TOP_Y,
                11, SCROLL_BOT_Y - SCROLL_TOP_Y))
        {
            draggingWh = true; draggingAe = false;
            dragStartY = (int) my;
            dragStartScroll = whScroll;
            return true;
        }

        // ── Drag scrollbar AE ─────────────────────────────────────────────────
        if (over(mx, my, leftPos + SCROLL_AE_X - 1, topPos + SCROLL_TOP_Y,
                11, SCROLL_BOT_Y - SCROLL_TOP_Y))
        {
            draggingAe = true; draggingWh = false;
            dragStartY = (int) my;
            dragStartScroll = meScroll;
            return true;
        }

        // ── Search bars ───────────────────────────────────────────────────────
        if (my >= topPos + SEARCH_Y && my < topPos + SEARCH_Y + SEARCH_H)
        {
            if (mx >= leftPos + SEARCH_WH_X && mx < leftPos + SEARCH_WH_X + SEARCH_WH_W)
            { whSearchFocused = true; meSearchFocused = false; return true; }
            if (mx >= leftPos + SEARCH_AE_X && mx < leftPos + SEARCH_AE_X + SEARCH_AE_W)
            { meSearchFocused = true; whSearchFocused = false; return true; }
        }
        whSearchFocused = meSearchFocused = false;

        // ── Slider WH/ME ──────────────────────────────────────────────────────
        if (over(mx, my, leftPos + SLIDER_X, topPos + SLIDER_Y, SLIDER_W, SLIDER_H))
        {
            warehouseFirst = !warehouseFirst;
            WarehouseLinkTerminalMenu.warehouseFirst = warehouseFirst;
            return true;
        }

        // ── Bouton B1 : transférer sélection WH↔ME ───────────────────────────
        if (over(mx, my, leftPos + BTN1_X, topPos + BTN1_Y, BTN_SZ, BTN_SZ))
        {
            btn1Pushed = true;
            if (!whSelected.isEmpty())
            {
                for (int i : new ArrayList<>(whSelected))
                    if (i < whFiltered.size())
                    { var e = whFiltered.get(i); sendTx(e.stack(), (int) Math.min(e.count(), e.stack().getMaxStackSize()), TerminalTransferPacket.Direction.WH_TO_ME); }
                whSelected.clear();
            }
            else if (!meSelected.isEmpty())
            {
                for (int i : new ArrayList<>(meSelected))
                    if (i < meFiltered.size())
                    { var e = meFiltered.get(i); sendTx(e.stack(), (int) Math.min(e.count(), e.stack().getMaxStackSize()), TerminalTransferPacket.Direction.ME_TO_WH); }
                meSelected.clear();
            }
            return true;
        }

        // ── Bouton B2 : transférer sélection → inventaire ────────────────────
        if (over(mx, my, leftPos + BTN2_X, topPos + BTN2_Y, BTN_SZ, BTN_SZ))
        {
            btn2Pushed = true;
            if (!whSelected.isEmpty())
            {
                for (int i : new ArrayList<>(whSelected))
                    if (i < whFiltered.size())
                    { var e = whFiltered.get(i); sendTx(e.stack(), (int) Math.min(e.count(), e.stack().getMaxStackSize()), TerminalTransferPacket.Direction.WH_TO_PLAYER); }
                whSelected.clear();
            }
            else if (!meSelected.isEmpty())
            {
                for (int i : new ArrayList<>(meSelected))
                    if (i < meFiltered.size())
                    { var e = meFiltered.get(i); sendTx(e.stack(), (int) Math.min(e.count(), e.stack().getMaxStackSize()), TerminalTransferPacket.Direction.ME_TO_PLAYER); }
                meSelected.clear();
            }
            return true;
        }

        // ── Bouton 7 : clear grille → inventaire (hitbox invisible sur fond PNG) ───
        if (over(mx, my, leftPos + 136, topPos + 273, 7, 7))
        {
            clearGridToInventory(shift);
            return true;
        }

        // ── Bouton 8 : result → WH (hitbox invisible sur fond PNG) ──────────────
        // count=1 → 1 craft ; count>1 (shift) → boucle max côté serveur
        if (over(mx, my, leftPos + 252, topPos + 279, 7, 7))
        {
            ItemStack r = menu.getCraftResult().getItem(0);
            if (!r.isEmpty())
                sendTx(r.copy(), shift ? r.getMaxStackSize() : 1,
                        TerminalTransferPacket.Direction.RESULT_TO_WH);
            return true;
        }

        // ── Bouton 9 : result → ME (hitbox invisible sur fond PNG) ──────────────
        if (over(mx, my, leftPos + 268, topPos + 279, 8, 7))
        {
            ItemStack r = menu.getCraftResult().getItem(0);
            if (!r.isEmpty())
                sendTx(r.copy(), shift ? r.getMaxStackSize() : 1,
                        TerminalTransferPacket.Direction.RESULT_TO_ME);
            return true;
        }

        // ── Bouton 10 : result → inventaire (hitbox invisible sur fond PNG) ──────
        if (over(mx, my, leftPos + 260, topPos + 313, 7, 7))
        {
            ItemStack r = menu.getCraftResult().getItem(0);
            if (!r.isEmpty())
                this.slotClicked(menu.getSlot(CRAFT_OUTPUT_SLOT), CRAFT_OUTPUT_SLOT, 0,
                        net.minecraft.world.inventory.ClickType.QUICK_MOVE);
            return true;
        }

        // ── Panel WH — pick / set-down / select ──────────────────────────────
        int ws = itemSlot((int) mx, (int) my, leftPos + X_ITEMS_WH);
        if (ws >= 0 && hasWarehouseCard && whErrorMsg.isEmpty())
        {
            int idx = whScroll * PANEL_COLS + ws;
            ItemStack carried = minecraft.player.containerMenu.getCarried();
            if (!shift && !carried.isEmpty())
            {
                sendTx(carried.copy(), carried.getCount(), TerminalTransferPacket.Direction.PUT_INTO_WH);
                whSelected.clear(); meSelected.clear();
                return true;
            }
            if (idx < whFiltered.size())
            {
                var e = whFiltered.get(idx);
                if (shift)
                {
                    // Shift+clic simple → transfert 1 stack vers inventaire
                    // (si drag démarre, dragStartSlotWh permettra de distinguer)
                    dragStartSlotWh = idx;
                    dragStartSlotAe = -1;
                    // On ne transfère pas encore — on attend mouseReleased
                    // pour savoir si c'était un clic (1 slot) ou un drag (2+ slots)
                    meSelected.clear();
                }
                else
                {
                    int pickCount = (btn == 1)
                            ? (int) Math.min(Math.max(1, (e.count() + 1) / 2), e.stack().getMaxStackSize())
                            : (int) Math.min(e.count(), e.stack().getMaxStackSize());
                    sendPickup(e.stack(), pickCount, TerminalTransferPacket.Direction.PICKUP_FROM_WH);
                    whSelected.clear(); meSelected.clear();
                }
            }
            return true;
        }

        // ── Panel AE — pick / set-down / select / autocraft ──────────────────
        int ms = itemSlot((int) mx, (int) my, leftPos + X_ITEMS_AE);
        if (ms >= 0 && menu.getPart().isAe2Active())
        {
            int idx = meScroll * PANEL_COLS + ms;
            ItemStack carried = minecraft.player.containerMenu.getCarried();
            if (!shift && btn != 2 && !carried.isEmpty())
            {
                sendTx(carried.copy(), carried.getCount(), TerminalTransferPacket.Direction.PUT_INTO_ME);
                whSelected.clear(); meSelected.clear();
                return true;
            }
            if (idx < meFiltered.size())
            {
                var e = meFiltered.get(idx);
                if (btn == 2)
                {
                    if (e.craftable())
                        PacketDistributor.sendToServer(new TerminalCraftPacket(
                                TerminalCraftPacket.Mode.AUTOCRAFT, e.stack().copyWithCount(1), 1,
                                partHostPos(), partSide()));
                    return true;
                }
                if (shift)
                {
                    // Shift+clic simple → transfert 1 stack vers inventaire
                    // On attend mouseReleased pour distinguer clic (1 slot) vs drag (2+ slots)
                    dragStartSlotAe = idx;
                    dragStartSlotWh = -1;
                    whSelected.clear();
                }
                else
                {
                    int pickCount;
                    if (btn == 1)
                    {
                        long half = (e.count() + 1) / 2;
                        pickCount = (int) Math.min(Math.max(1, half), e.stack().getMaxStackSize());
                        if (e.count() <= 0) pickCount = 0;
                    }
                    else
                    {
                        pickCount = e.count() > 0
                                ? (int) Math.min(e.count(), e.stack().getMaxStackSize()) : 1;
                    }
                    if (pickCount > 0)
                        sendPickup(e.stack(), pickCount, TerminalTransferPacket.Direction.PICKUP_FROM_ME);
                    whSelected.clear(); meSelected.clear();
                }
            }
            return true;
        }

        return super.mouseClicked(mx, my, btn);
    }

    @Override public boolean mouseReleased(double mx, double my, int btn)
    {
        draggingWh = draggingAe = false;
        btn1Pushed = btn2Pushed = false;

        // Shift+clic simple (pas de drag vers un autre slot) → transfert 1 stack → inventaire
        if (dragStartSlotWh >= 0 && dragStartSlotWh < whFiltered.size())
        {
            var e = whFiltered.get(dragStartSlotWh);
            int count = (int) Math.min(e.count(), e.stack().getMaxStackSize());
            if (count > 0)
                sendTx(e.stack().copy(), count, TerminalTransferPacket.Direction.WH_TO_PLAYER);
        }
        dragStartSlotWh = -1;

        if (dragStartSlotAe >= 0 && dragStartSlotAe < meFiltered.size())
        {
            var e = meFiltered.get(dragStartSlotAe);
            int count = e.count() > 0 ? (int) Math.min(e.count(), e.stack().getMaxStackSize()) : 0;
            if (count > 0)
                sendTx(e.stack().copy(), count, TerminalTransferPacket.Direction.ME_TO_PLAYER);
        }
        dragStartSlotAe = -1;

        return super.mouseReleased(mx, my, btn);
    }

    @Override public boolean mouseDragged(double mx, double my, int btn, double dx, double dy)
    {
        float cx = this.width / 2f, cy = this.height / 2f;
        mx = toGui(mx, cx, guiScale); my = toGui(my, cy, guiScale);

        // ── Drag scrollbar ────────────────────────────────────────────────────
        if (draggingWh || draggingAe)
        {
            int trackH    = SCROLL_BOT_Y - SCROLL_TOP_Y; // 194px
            int deltaPx   = (int) my - dragStartY;
            List<?> list  = draggingWh ? whFiltered : meFiltered;
            int maxRows   = (int) Math.ceil(list.size() / (double) PANEL_COLS);
            int maxScroll = Math.max(1, maxRows - PANEL_ROWS);
            int pixPerRow = Math.max(1, (trackH - SCROLL_THUMB_H) / maxScroll);
            int newScroll = dragStartScroll + deltaPx / pixPerRow;
            if (draggingWh) whScroll = clamp(newScroll, whFiltered.size());
            else             meScroll = clamp(newScroll, meFiltered.size());
            return true;
        }

        // ── Shift+drag sélection items (commence à 2 slots différents) ──────────
        if (hasShiftDown())
        {
            int ws = itemSlot((int) mx, (int) my, leftPos + X_ITEMS_WH);
            if (ws >= 0 && hasWarehouseCard)
            {
                int idx = whScroll * PANEL_COLS + ws;
                if (idx < whFiltered.size() && idx != dragStartSlotWh)
                {
                    // On est sur un slot différent du départ → mode sélection activé
                    if (dragStartSlotWh >= 0) whSelected.add(dragStartSlotWh); // ajouter le slot de départ
                    whSelected.add(idx);
                    meSelected.clear();
                    dragStartSlotWh = -1; // neutralise le transfert au release
                    return true;
                }
            }
            int ms = itemSlot((int) mx, (int) my, leftPos + X_ITEMS_AE);
            if (ms >= 0 && menu.getPart().isAe2Active())
            {
                int idx = meScroll * PANEL_COLS + ms;
                if (idx < meFiltered.size() && idx != dragStartSlotAe)
                {
                    if (dragStartSlotAe >= 0) meSelected.add(dragStartSlotAe);
                    meSelected.add(idx);
                    whSelected.clear();
                    dragStartSlotAe = -1;
                    return true;
                }
            }
        }
        return super.mouseDragged(mx, my, btn, dx, dy);
    }

    @Override public boolean mouseScrolled(double mx, double my, double dx, double dy)
    {
        float cx = this.width / 2f, cy = this.height / 2f;
        mx = toGui(mx, cx, guiScale); my = toGui(my, cy, guiScale);

        int rT = topPos + Y_ITEMS;
        int rB = rT + PANEL_ROWS * SLOT_PITCH;

        // Scroll WH : souris sur le panel WH uniquement (pas sur la track scrollbar)
        if (my >= rT && my < rB
                && mx >= leftPos + X_ITEMS_WH
                && mx < leftPos + X_ITEMS_WH + PANEL_COLS * SLOT_PITCH)
        { whScroll = clamp(whScroll - (int) Math.signum(dy), whFiltered.size()); return true; }

        // Scroll AE : souris sur le panel AE uniquement (pas sur la track scrollbar)
        if (my >= rT && my < rB
                && mx >= leftPos + X_ITEMS_AE
                && mx < leftPos + X_ITEMS_AE + PANEL_COLS * SLOT_PITCH)
        { meScroll = clamp(meScroll - (int) Math.signum(dy), meFiltered.size()); return true; }

        return super.mouseScrolled(mx, my, dx, dy);
    }

    @Override public boolean keyPressed(int key, int scan, int mod)
    {
        if (whSearchFocused)
        {
            if (key == 259 && !whSearch.isEmpty()) whSearch = whSearch.substring(0, whSearch.length() - 1);
            else if (key == 256) whSearchFocused = false;
            rebuildWh(); return true;
        }
        if (meSearchFocused)
        {
            if (key == 259 && !meSearch.isEmpty()) meSearch = meSearch.substring(0, meSearch.length() - 1);
            else if (key == 256) meSearchFocused = false;
            rebuildMe(); return true;
        }
        return super.keyPressed(key, scan, mod);
    }

    @Override public boolean charTyped(char c, int mod)
    {
        if (whSearchFocused) { whSearch += c; rebuildWh(); return true; }
        if (meSearchFocused) { meSearch += c; rebuildMe(); return true; }
        return super.charTyped(c, mod);
    }

    // =========================================================================
    // ACTIONS BOUTONS CRAFT
    // =========================================================================
    private void clearGridToInventory(boolean shift)
    {
        PacketDistributor.sendToServer(new TerminalTransferPacket(
                ItemStack.EMPTY, 0,
                TerminalTransferPacket.Direction.CRAFT_TO_PLAYER,
                partHostPos(), partSide()));
        if (shift)
        {
            ItemStack r = menu.getCraftResult().getItem(0);
            if (!r.isEmpty())
                this.slotClicked(menu.getSlot(CRAFT_OUTPUT_SLOT), CRAFT_OUTPUT_SLOT, 0,
                        net.minecraft.world.inventory.ClickType.QUICK_MOVE);
        }
    }

    // =========================================================================
    // HELPERS
    // =========================================================================
    private void sendPickup(ItemStack template, int count, TerminalTransferPacket.Direction dir)
    { PacketDistributor.sendToServer(new TerminalTransferPacket(template.copyWithCount(1), count, dir, partHostPos(), partSide())); }

    private void sendTx(ItemStack s, int n, TerminalTransferPacket.Direction d)
    { PacketDistributor.sendToServer(new TerminalTransferPacket(s.copyWithCount(n), n, d, partHostPos(), partSide())); }

    /** Retourne l'index linéaire (row*PANEL_COLS+col) du slot survolé dans un panel, ou -1. */
    private int itemSlot(int mx, int my, int panelX)
    {
        int rT = topPos + Y_ITEMS;
        int rB = rT + PANEL_ROWS * SLOT_PITCH;
        if (mx < panelX || mx >= panelX + PANEL_COLS * SLOT_PITCH || my < rT || my >= rB) return -1;
        int col = (mx - panelX) / SLOT_PITCH;
        int row = (my - rT)     / SLOT_PITCH;
        if (col >= PANEL_COLS || row >= PANEL_ROWS) return -1;
        return row * PANEL_COLS + col;
    }

    private int clamp(int s, int tot)
    {
        int max = Math.max(0, (int) Math.ceil(tot / (double) PANEL_COLS) - PANEL_ROWS);
        return Math.max(0, Math.min(s, max));
    }

    private boolean over(double mx, double my, int bx, int by, int bw, int bh)
    { return mx >= bx && mx < bx + bw && my >= by && my < by + bh; }

    private static String fmtCount(long n)
    {
        if (n < 0)               return "0";
        if (n < 1_000)           return Long.toString(n);
        if (n < 10_000)          return fmtDecimal(n, 1_000)       + "K";
        if (n < 1_000_000)       return (n / 1_000)                + "K";
        if (n < 10_000_000)      return fmtDecimal(n, 1_000_000)   + "M";
        if (n < 1_000_000_000)   return (n / 1_000_000)            + "M";
        if (n < 10_000_000_000L) return fmtDecimal(n, 1_000_000_000L) + "B";
        if (n < 1_000_000_000_000L) return (n / 1_000_000_000L)   + "B";
        return "\u221e";
    }

    private static String fmtDecimal(long n, long divisor)
    { return (n / divisor) + "." + ((n * 10 / divisor) % 10); }

    private net.minecraft.core.BlockPos partHostPos()
    { var be = menu.getPart().getHostBlockEntity(); return be != null ? be.getBlockPos() : net.minecraft.core.BlockPos.ZERO; }

    private int partSide()
    { var s = menu.getPart().getSide(); return s != null ? s.ordinal() : 0; }

    public record MeItemEntry(ItemStack stack, long count, boolean craftable) {}
}