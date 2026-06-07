package com.colonylink.colonylink;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlock;
import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlockComponent;
import com.ldtteam.domumornamentum.client.model.data.MaterialTextureData;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

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
    // Chrome (fond, boutons, onglets, slider, checkbox, overlay Cutter) rendu 100%
    // en code via TerminalSkin — pixel-perfect, sans texture. Seules les 2 icônes
    // d'onglet restent en PNG.
    private static final ResourceLocation TEX_ICO_CRAFT =
            ResourceLocation.fromNamespaceAndPath("colonylink", "textures/button/craft_ico.png");
    private static final ResourceLocation TEX_ICO_CUTTER =
            ResourceLocation.fromNamespaceAndPath("colonylink", "textures/button/cutter_ico.png");

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

    // ── v1.4.2 — Onglets ─────────────────────────────────────────────────────
    // Onglet 0 = Crafting Table (défaut), Onglet 1 = Cutter Domum
    private static final int TAB_X        = 295;
    private static final int TAB_CRAFT_Y  = 269;
    private static final int TAB_CUTTER_Y = 291;
    private static final int TAB_W        = 22;
    private static final int TAB_H        = 22;
    private static final int ICO_SIZE     = 16;
    private static final int ICO_CRAFT_X  = TAB_X + 3;
    private static final int ICO_CRAFT_Y  = TAB_CRAFT_Y + 3;
    private static final int ICO_CUT_X    = TAB_X + 3;
    private static final int ICO_CUT_Y    = TAB_CUTTER_Y + 3;

    // pattern_gui.png overlay coords
    private static final int PG_X = 129;
    private static final int PG_Y = 266;
    private static final int PG_W = 166;
    private static final int PG_H = 68;

    // Slots Domum — coords visuelles (slots réels à -2000 dans le Menu)
    private static final int DOMUM_TARGET_RENDER_X = 139;
    private static final int DOMUM_TARGET_RENDER_Y = 283;
    private static final int MAT_START_X = 162;
    private static final int MAT_Y       = 283;
    private static final int MAT_PITCH   = 18;
    private static final int MAT_MAX     = 4;
    private static final int BLANK_RENDER_X  = 269;
    private static final int BLANK_RENDER_Y  = 267;
    private static final int ENCODE_BTN_X    = 271;
    private static final int ENCODE_BTN_Y    = 292;
    private static final int ENCODE_BTN_W    = 12;
    private static final int ENCODE_BTN_H    = 12;
    private static final int OUTPUT_RENDER_X = 269;
    private static final int OUTPUT_RENDER_Y = 314;

    private int     activeTab = 0;
    private boolean encodePushed = false;
    private final List<ItemStack> cachedMaterials = new ArrayList<>();
    private ItemStack lastTargetForCache = ItemStack.EMPTY;

    // ── Queue Domum (reçue via DomumQueueSyncPacket) ──────────────────────────
    private final List<ItemStack> domumQueue     = new ArrayList<>();
    private int                   domumQueueScroll    = 0;
    private int                   domumQueueSelected  = -1; // index sélectionné

    // ── Cutter tab — nouvelle disposition (coordonnées relatives au panneau PG_X/PG_Y)
    // Slot item de référence (posé manuellement) : x3 y26
    private static final int CUT_REF_X     = 3;
    private static final int CUT_REF_Y     = 26;
    // Aperçu matériau 1 : x3 y5
    private static final int CUT_MAT1_X    = 3;
    private static final int CUT_MAT1_Y    = 5;
    // Aperçu matériau 2 : x3 y46
    private static final int CUT_MAT2_X    = 3;
    private static final int CUT_MAT2_Y    = 46;
    // Zone liste scrollable : x25 y3, largeur 96 (3px marge droite avant bord panneau)
    private static final int CUT_LIST_X    = 25;
    private static final int CUT_LIST_Y    = 3;
    private static final int CUT_LIST_W    = 96;
    private static final int CUT_LIST_H    = 63;
    // Hauteur d'une ligne de liste
    private static final int CUT_ROW_H     = 18;
    // Nombre de lignes visibles
    private static final int CUT_ROWS_VIS  = CUT_LIST_H / CUT_ROW_H; // = 3

    // État drag scrollbars
    private boolean draggingWh = false, draggingAe = false;
    private int     dragStartY = 0, dragStartScroll = 0;
    private int     dragStartSlotWh = -1, dragStartSlotAe = -1; // slot où le drag a commencé

    // ── État animation boutons push
    private boolean btn1Pushed = false, btn2Pushed = false;
    private int[] origSlotY;   // Y d'origine des slots vanilla (capture 1x, resize-safe)

    private String hoveredTooltip = null;

    // ── Fit-to-window scale ───────────────────────────────────────────────────

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
        imageHeight = (GUI_H - PANEL_ROWS * SLOT_PITCH) + visRows() * SLOT_PITCH; // hauteur fluide
        super.init();
        titleLabelX = inventoryLabelX = -10000;
        // Remonte les slots vanilla (craft + sortie + inventaire) de rowDY() px quand on
        // retire des lignes (facon AE2). Capture des Y d'origine une seule fois (resize-safe).
        if (origSlotY == null)
        {
            origSlotY = new int[menu.slots.size()];
            for (int i = 0; i < menu.slots.size(); i++) origSlotY[i] = menu.slots.get(i).y;
        }
        int slotDY  = rowDY();
        int slotCut = Y_ITEMS + PANEL_ROWS * SLOT_PITCH; // 229 — seuil du bloc bas
        for (int i = 0; i < menu.slots.size(); i++)
        {
            int oy = origSlotY[i];
            setSlotY(menu.slots.get(i), (oy >= slotCut) ? oy - slotDY : oy);
        }
        whSearch = savedWhSearch;
        meSearch = savedMeSearch;
        rebuildWh();
        rebuildMe();
        whScroll = clamp(whScroll, whFiltered.size()); // re-clamp apres changement de visRows (resize)
        meScroll = clamp(meScroll, meFiltered.size());
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

    /** Reçu via DomumQueueSyncPacket — met à jour la liste côté client. */
    public void updateDomumQueue(java.util.List<ItemStack> queue)
    {
        domumQueue.clear();
        domumQueue.addAll(queue);
        // Si la sélection était sur un index maintenant hors liste, on la réinitialise
        if (domumQueueSelected >= domumQueue.size()) domumQueueSelected = -1;
        domumQueueScroll = Math.max(0, Math.min(domumQueueScroll,
                Math.max(0, domumQueue.size() - CUT_ROWS_VIS)));
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

        int mx = rawMx;
        int my = rawMy;

        // Voile vanilla plein écran (renderBackground) — noir à 85% (0xD8000000)
        g.fill(0, 0, this.width, this.height, 0xD8000000);


        super.render(g, mx, my, pt);
        renderPanelItems(g, leftPos + X_ITEMS_WH, topPos, true,  mx, my);
        renderPanelItems(g, leftPos + X_ITEMS_AE, topPos, false, mx, my);


        // Tooltips à la position brute du curseur (taille réelle, placement correct)
        renderTooltip(g, rawMx, rawMy);
        renderTooltipForPanel(g, mx, my, rawMx, rawMy);

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
    // ── Layout fluide facon AE2 : nb de lignes visibles selon la hauteur fenetre ──
    private int visRows()
    {
        int fixedH = GUI_H - PANEL_ROWS * SLOT_PITCH;   // hauteur hors-lignes (= 234)
        int n = (this.height - fixedH) / SLOT_PITCH;
        return Math.max(2, Math.min(PANEL_ROWS, n));
    }
    private int rowDY() { return (PANEL_ROWS - visRows()) * SLOT_PITCH; }

    // Slot.x/y sont final en 1.21.1 -> on repositionne via reflection (champ Mojmap "y",
    // valide en dev comme en prod NeoForge). Le rendu et le hit-test vanilla utilisent
    // ensuite la position mise a jour automatiquement.
    private static java.lang.reflect.Field SLOT_Y;
    private static void setSlotY(net.minecraft.world.inventory.Slot slot, int y)
    {
        try
        {
            if (SLOT_Y == null)
            {
                SLOT_Y = net.minecraft.world.inventory.Slot.class.getDeclaredField("y");
                SLOT_Y.setAccessible(true);
            }
            SLOT_Y.setInt(slot, y);
        }
        catch (ReflectiveOperationException e)
        {
            throw new RuntimeException("ColonyLink: impossible de repositionner le slot", e);
        }
    }

    @Override protected void renderBg(GuiGraphics g, float pt, int mx, int my)
    {
        int x = leftPos, y = topPos;
        int dy     = rowDY();
        int yb     = y - dy;                              // base des elements ancres en bas
        int cutTop = Y_ITEMS + visRows() * SLOT_PITCH;    // coords locales du skin BG
        int cutBot = Y_ITEMS + PANEL_ROWS * SLOT_PITCH;   // = 229

        // 1. Fond principal (texture PNG complète)
        TerminalSkin.drawCut(g, TerminalSkin.BG, x, y, cutTop, cutBot, dy);

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
        renderSlider(g, x, yb, mx, my);

        // 6. Boutons push centre colonne — visibles dans les DEUX onglets : les
        //    panneaux WH/ME et leurs transferts restent actifs en Craft comme en Cutter.
        renderPushButton(g, mx, my, x + BTN1_X, y + BTN1_Y, btn1Pushed,
                Component.translatable("colonylink.term.btn_transfer_wh_me").getString());
        renderPushButton(g, mx, my, x + BTN2_X, y + BTN2_Y, btn2Pushed,
                Component.translatable("colonylink.term.btn_transfer_inv").getString());

        // 7. Tooltips des croix de la grille craft (onglet Craft uniquement)
        if (activeTab == 0)
        {
            if (over(mx, my, x + 136, yb + 273, 7, 7))
                hoveredTooltip = Component.translatable("colonylink.term.clear_grid").getString();
            if (over(mx, my, x + 252, yb + 279, 7, 7))
                hoveredTooltip = Component.translatable("colonylink.term.output_wh").getString();
            if (over(mx, my, x + 268, yb + 279, 8, 7))
                hoveredTooltip = Component.translatable("colonylink.term.output_me").getString();
            if (over(mx, my, x + 260, yb + 313, 7, 7))
                hoveredTooltip = Component.translatable("colonylink.term.output_inv").getString();
        }

        // 8. Sélection count sur panels
        if (!whSelected.isEmpty())
            g.drawString(font, Component.translatable("colonylink.term.selected", whSelected.size()).getString(),
                    x + 136, y + 15, 0xFFFFDD00, false);
        if (!meSelected.isEmpty())
            g.drawString(font, Component.translatable("colonylink.term.selected", meSelected.size()).getString(),
                    x + 361, y + 15, 0xFFFFDD00, false);

        // 9. v1.4.2 — Onglets
        renderTabs(g, x, yb, mx, my);

        // 10. v1.4.2 — Zone Cutter (sur-impression si onglet 1 actif)
        if (activeTab == 1)
            renderCutterOverlay(g, x, yb, mx, my);

        // 11. Tooltip slot Warehouse Link Card (quand vide)
        if (!hasWarehouseCard)
            if (over(mx, my, x + WarehouseLinkTerminalMenu.CARD_SLOT_X,
                    y + WarehouseLinkTerminalMenu.CARD_SLOT_Y, 16, 16))
                hoveredTooltip = Component.translatable("colonylink.term.card_slot_tip").getString();
    }

    // ── Onglets (x=295, y=269 craft / y=291 cutter) ───────────────────────────
    private void renderTabs(GuiGraphics g, int x, int y, int mx, int my)
    {
        boolean craftActive = (activeTab == 0);
        TerminalSkin.draw(g, craftActive ? TerminalSkin.TAB_ON : TerminalSkin.TAB_OFF,
                x + TAB_X, y + TAB_CRAFT_Y);
        g.blit(TEX_ICO_CRAFT,
                x + ICO_CRAFT_X, y + ICO_CRAFT_Y, 0, 0, ICO_SIZE, ICO_SIZE, ICO_SIZE, ICO_SIZE);
        if (over(mx, my, x + TAB_X, y + TAB_CRAFT_Y, TAB_W, TAB_H))
            hoveredTooltip = Component.translatable("colonylink.term.tab_craft").getString();

        boolean cutActive = (activeTab == 1);
        TerminalSkin.draw(g, cutActive ? TerminalSkin.TAB_ON : TerminalSkin.TAB_OFF,
                x + TAB_X, y + TAB_CUTTER_Y);
        g.blit(TEX_ICO_CUTTER,
                x + ICO_CUT_X, y + ICO_CUT_Y, 0, 0, ICO_SIZE, ICO_SIZE, ICO_SIZE, ICO_SIZE);
        if (over(mx, my, x + TAB_X, y + TAB_CUTTER_Y, TAB_W, TAB_H))
            hoveredTooltip = Component.translatable("colonylink.term.tab_cutter").getString();
    }

    // ── Overlay Cutter ────────────────────────────────────────────────────────
    private void renderCutterOverlay(GuiGraphics g, int x, int y, int mx, int my)
    {
        TerminalSkin.draw(g, TerminalSkin.PATTERN, x + PG_X, y + PG_Y);

        // Origine du panneau cutter (coin haut-gauche de l'overlay PG)
        int ox = x + PG_X;
        int oy = y + PG_Y;

        // ── Slot item de référence (x3 y26) ─────────────────────────────────
        renderSlotOutline(g, ox + CUT_REF_X - 1, oy + CUT_REF_Y - 1);
        ItemStack target = menu.getDomumTarget();
        if (!target.isEmpty())
        {
            g.renderItem(target, ox + CUT_REF_X, oy + CUT_REF_Y);
            g.renderItemDecorations(font, target, ox + CUT_REF_X, oy + CUT_REF_Y);
            if (over(mx, my, ox + CUT_REF_X, oy + CUT_REF_Y, 16, 16))
                hoveredTooltip = Component.translatable("colonylink.term.ref_model", target.getDisplayName()).getString();
            updateMaterialCache(target);
        }
        else
        {
            if (over(mx, my, ox + CUT_REF_X, oy + CUT_REF_Y, 16, 16))
                hoveredTooltip = Component.translatable("colonylink.term.preview_domum").getString();
            cachedMaterials.clear();
            lastTargetForCache = ItemStack.EMPTY;
        }

        // ── Aperçu matériau 1 (x3 y5) ───────────────────────────────────────
        renderSlotOutline(g, ox + CUT_MAT1_X - 1, oy + CUT_MAT1_Y - 1);
        if (cachedMaterials.size() >= 1 && !cachedMaterials.get(0).isEmpty())
        {
            ItemStack mat1 = cachedMaterials.get(0);
            g.renderItem(mat1, ox + CUT_MAT1_X, oy + CUT_MAT1_Y);
            if (over(mx, my, ox + CUT_MAT1_X, oy + CUT_MAT1_Y, 16, 16))
                hoveredTooltip = Component.translatable("colonylink.term.material1", mat1.getDisplayName()).getString();
        }
        else if (over(mx, my, ox + CUT_MAT1_X, oy + CUT_MAT1_Y, 16, 16))
            hoveredTooltip = Component.translatable("colonylink.term.material_slot1").getString();

        // ── Aperçu matériau 2 (x3 y46) ──────────────────────────────────────
        renderSlotOutline(g, ox + CUT_MAT2_X - 1, oy + CUT_MAT2_Y - 1);
        if (cachedMaterials.size() >= 2 && !cachedMaterials.get(1).isEmpty())
        {
            ItemStack mat2 = cachedMaterials.get(1);
            g.renderItem(mat2, ox + CUT_MAT2_X, oy + CUT_MAT2_Y);
            if (over(mx, my, ox + CUT_MAT2_X, oy + CUT_MAT2_Y, 16, 16))
                hoveredTooltip = Component.translatable("colonylink.term.material2", mat2.getDisplayName()).getString();
        }
        else if (over(mx, my, ox + CUT_MAT2_X, oy + CUT_MAT2_Y, 16, 16))
            hoveredTooltip = Component.translatable("colonylink.term.material_slot2").getString();

        // ── Blank Pattern (réutilise BLANK_RENDER_X/Y existants) ─────────────
        ItemStack blank = menu.getSlot(BLANK_PATTERN_SLOT).getItem();
        renderSlotOutline(g, x + BLANK_RENDER_X - 1, y + BLANK_RENDER_Y - 1);
        if (!blank.isEmpty())
        {
            g.renderItem(blank, x + BLANK_RENDER_X, y + BLANK_RENDER_Y);
            g.renderItemDecorations(font, blank, x + BLANK_RENDER_X, y + BLANK_RENDER_Y);
        }
        if (over(mx, my, x + BLANK_RENDER_X, y + BLANK_RENDER_Y, 16, 16))
            hoveredTooltip = blank.isEmpty() ? Component.translatable("colonylink.term.insert_blank").getString() : blank.getDisplayName().getString();

        // ── Slot output (réutilise OUTPUT_RENDER_X/Y existants) ──────────────
        ItemStack output = menu.getDomumOutput();
        renderSlotOutline(g, x + OUTPUT_RENDER_X - 1, y + OUTPUT_RENDER_Y - 1);
        if (!output.isEmpty())
        {
            g.renderItem(output, x + OUTPUT_RENDER_X, y + OUTPUT_RENDER_Y);
            g.renderItemDecorations(font, output, x + OUTPUT_RENDER_X, y + OUTPUT_RENDER_Y);
            if (over(mx, my, x + OUTPUT_RENDER_X, y + OUTPUT_RENDER_Y, 16, 16))
                hoveredTooltip = output.getDisplayName().getString() + Component.translatable("colonylink.term.pickup_pattern").getString();
        }
        else if (over(mx, my, x + OUTPUT_RENDER_X, y + OUTPUT_RENDER_Y, 16, 16))
            hoveredTooltip = Component.translatable("colonylink.term.encoded_appears").getString();

        // ── Bouton Encode ─────────────────────────────────────────────────────
        boolean canEncode = domumQueueSelected >= 0
                && domumQueueSelected < domumQueue.size()
                && !blank.isEmpty()
                && output.isEmpty();
        TerminalSkin.draw(g, encodePushed ? TerminalSkin.BTN_PUSH : TerminalSkin.BTN,
                x + ENCODE_BTN_X, y + ENCODE_BTN_Y);
        if (!canEncode)
            g.fill(x + ENCODE_BTN_X, y + ENCODE_BTN_Y,
                    x + ENCODE_BTN_X + ENCODE_BTN_W, y + ENCODE_BTN_Y + ENCODE_BTN_H, 0xAA111133);
        if (over(mx, my, x + ENCODE_BTN_X, y + ENCODE_BTN_Y, ENCODE_BTN_W, ENCODE_BTN_H))
        {
            boolean queueReady = domumQueueSelected >= 0 && domumQueueSelected < domumQueue.size();
            boolean slotReady  = !target.isEmpty();
            boolean blankReady = !blank.isEmpty();
            boolean outFree    = output.isEmpty();
            if (queueReady && blankReady && outFree)
                hoveredTooltip = Component.translatable("colonylink.term.encode_queue").getString();
            else if (slotReady && blankReady && outFree)
                hoveredTooltip = Component.translatable("colonylink.term.encode_ref").getString();
            else if (!blankReady)
                hoveredTooltip = Component.translatable("colonylink.term.encode_need_blank").getString();
            else if (!outFree)
                hoveredTooltip = Component.translatable("colonylink.term.encode_take_first").getString();
            else
                hoveredTooltip = Component.translatable("colonylink.term.encode_select_first").getString();
        }

        // ── Liste scrollable (x33 y3 → x124 y66) ─────────────────────────────
        renderDomumQueueList(g, ox, oy, mx, my);
    }

    /**
     * Rendu de la liste scrollable des items Domum en attente d'encodage.
     * Coordonnées relatives à l'origine du panneau cutter (ox, oy).
     */
    private void renderDomumQueueList(GuiGraphics g, int ox, int oy, int mx, int my)
    {
        // Fond de la zone liste
        g.fill(ox + CUT_LIST_X, oy + CUT_LIST_Y,
                ox + CUT_LIST_X + CUT_LIST_W, oy + CUT_LIST_Y + CUT_LIST_H, 0x88111111);

        // Clip pour éviter que le texte déborde hors de la zone liste
        g.enableScissor(ox + CUT_LIST_X, oy + CUT_LIST_Y,
                ox + CUT_LIST_X + CUT_LIST_W, oy + CUT_LIST_Y + CUT_LIST_H);

        if (domumQueue.isEmpty())
        {
            g.drawString(font, Component.translatable("colonylink.term.no_pending").getString(),
                    ox + CUT_LIST_X + 4, oy + CUT_LIST_Y + 4, 0xFFAAAAAA, false);
            g.drawString(font, Component.translatable("colonylink.term.click_nopattern").getString(),
                    ox + CUT_LIST_X + 4, oy + CUT_LIST_Y + 14, 0xFFAAAAAA, false);
            g.drawString(font, Component.translatable("colonylink.term.in_clipboard").getString(),
                    ox + CUT_LIST_X + 4, oy + CUT_LIST_Y + 24, 0xFFAAAAAA, false);
            g.disableScissor();
            return;
        }

        int maxScroll = Math.max(0, domumQueue.size() - CUT_ROWS_VIS);
        domumQueueScroll = Math.min(domumQueueScroll, maxScroll);

        for (int row = 0; row < CUT_ROWS_VIS; row++)
        {
            int idx = domumQueueScroll + row;
            if (idx >= domumQueue.size()) break;

            ItemStack stack = domumQueue.get(idx);
            int rx = ox + CUT_LIST_X;
            int ry = oy + CUT_LIST_Y + row * CUT_ROW_H;

            boolean selected = (idx == domumQueueSelected);
            boolean hovered  = over(mx, my, rx, ry, CUT_LIST_W, CUT_ROW_H);
            if (selected)
                g.fill(rx, ry, rx + CUT_LIST_W, ry + CUT_ROW_H, 0x881A3A6A);
            else if (hovered)
                g.fill(rx, ry, rx + CUT_LIST_W, ry + CUT_ROW_H, 0x44FFFFFF);

            g.fill(rx, ry + CUT_ROW_H - 1, rx + CUT_LIST_W, ry + CUT_ROW_H, 0x44AAAAAA);

            int itemY = ry + (CUT_ROW_H - 16) / 2;
            g.renderItem(stack, rx + 2, itemY);

            String name = stack.getDisplayName().getString();
            int maxChars = (CUT_LIST_W - 22) / 5;
            if (name.length() > maxChars) name = name.substring(0, maxChars - 1) + "…";
            g.drawString(font, name, rx + 20, ry + (CUT_ROW_H - 8) / 2, 0xFFDDDDDD, false);

            if (hovered)
            {
                StringBuilder tip = new StringBuilder();
                tip.append("§f").append(stack.getDisplayName().getString());

                // Variant (BLOCK_STATE)
                net.minecraft.world.item.component.BlockItemStateProperties bs =
                        stack.get(net.minecraft.core.component.DataComponents.BLOCK_STATE);
                if (bs != null && !bs.properties().isEmpty())
                    for (var e : bs.properties().entrySet())
                        tip.append("\n§8").append(e.getKey()).append(": §7").append(e.getValue());

                // Matériaux
                if (stack.getItem() instanceof net.minecraft.world.item.BlockItem bi
                        && bi.getBlock() instanceof com.ldtteam.domumornamentum.block.IMateriallyTexturedBlock tb)
                {
                    com.ldtteam.domumornamentum.client.model.data.MaterialTextureData td =
                            com.ldtteam.domumornamentum.client.model.data.MaterialTextureData.readFromItemStack(stack);
                    for (var comp : tb.getComponents())
                    {
                        net.minecraft.world.level.block.Block mat = td.getTexturedComponents().get(comp.getId());
                        if (mat != null)
                            tip.append("\n§7• §f").append(new ItemStack(mat).getDisplayName().getString());
                    }
                }

                tip.append(Component.translatable("colonylink.term.click_select").getString());
                hoveredTooltip = tip.toString();
            }
        }

        g.disableScissor();

        // Indicateur scroll hors clip (pour ne pas être coupé)
        if (maxScroll > 0)
        {
            String scrollInfo = (domumQueueScroll + 1) + "-"
                    + Math.min(domumQueueScroll + CUT_ROWS_VIS, domumQueue.size())
                    + "/" + domumQueue.size();
            g.drawString(font, scrollInfo,
                    ox + CUT_LIST_X + CUT_LIST_W - font.width(scrollInfo) - 2,
                    oy + CUT_LIST_Y + CUT_LIST_H - 8, 0xFF777777, false);
        }
    }

    // ── Matériaux lecture seule ───────────────────────────────────────────────
    private void updateMaterialCache(ItemStack target)
    {
        if (ItemStack.isSameItemSameComponents(target, lastTargetForCache)) return;
        lastTargetForCache = target.copy();
        cachedMaterials.clear();
        if (!(target.getItem() instanceof BlockItem bi)) return;
        Block block = bi.getBlock();
        if (!(block instanceof IMateriallyTexturedBlock tb)) return;
        MaterialTextureData td = MaterialTextureData.readFromItemStack(target);
        for (IMateriallyTexturedBlockComponent comp : tb.getComponents())
        {
            Block mat = td.getTexturedComponents().get(comp.getId());
            cachedMaterials.add(mat != null ? new ItemStack(mat, 1) : ItemStack.EMPTY);
        }
    }

    private void renderMaterials(GuiGraphics g, int x, int y, int mx, int my)
    {
        int count = Math.min(cachedMaterials.size(), MAT_MAX);
        for (int i = 0; i < count; i++)
        {
            int ix = x + MAT_START_X + i * MAT_PITCH;
            int iy = y + MAT_Y;
            ItemStack mat = cachedMaterials.get(i);
            renderSlotOutline(g, ix - 1, iy - 1);
            if (mat.isEmpty())
            {
                g.fill(ix, iy, ix + 16, iy + 16, 0x88FF0000);
                if (over(mx, my, ix, iy, 16, 16))
                    hoveredTooltip = Component.translatable("colonylink.term.missing_material").getString();
            }
            else
            {
                g.renderItem(mat, ix, iy);
                if (over(mx, my, ix, iy, 16, 16))
                    hoveredTooltip = Component.translatable("colonylink.term.material_required", mat.getDisplayName()).getString();
            }
        }
        if (cachedMaterials.size() > MAT_MAX)
            g.drawString(font, "+" + (cachedMaterials.size() - MAT_MAX),
                    x + MAT_START_X + MAT_MAX * MAT_PITCH, y + MAT_Y + 4, 0xFF9999BB, false);
    }

    // ── Outline slot ──────────────────────────────────────────────────────────
    private void renderSlotOutline(GuiGraphics g, int x, int y)
    {
        g.fill(x,      y,      x + 18, y + 1,  0xFF373737);
        g.fill(x,      y,      x + 1,  y + 18, 0xFF373737);
        g.fill(x,      y + 17, x + 18, y + 18, 0xFFFFFFFF);
        g.fill(x + 17, y,      x + 18, y + 18, 0xFFFFFFFF);
    }

    // ── Labels WH / AE au dessus des search bars ──────────────────────────────
    private void renderPanelLabels(GuiGraphics g, int x, int y)
    {
        // Zone label WH : x=9..133, y=2..14
        String wh = Component.translatable("colonylink.term.label_warehouse").getString();
        String whCnt = String.valueOf(whFiltered.size());
        g.drawString(font, wh,    x + 9, y + 3, C_TEXT_WH, false);
        g.drawString(font, whCnt, x + 9 + SEARCH_WH_W - font.width(whCnt) - 1, y + 3, C_TEXT_MUTED, false);

        // Zone label AE : x=233..358, y=2..14
        String ae = Component.translatable("colonylink.term.label_applied").getString();
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
            g.drawString(font, Component.translatable("colonylink.term.search").getString(), x + 2, y + 1, C_SF_PH, false);
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
        int maxScroll = maxRows - visRows();
        if (maxScroll <= 0) return;

        int trackH = (SCROLL_BOT_Y - SCROLL_TOP_Y) - rowDY(); // piste visible
        int thumbY = guiY + SCROLL_TOP_Y
                + (int) ((trackH - SCROLL_THUMB_H) * (float) scroll / maxScroll);

        // Corps utile du PNG = cols 1..11 = 11px
        // On décale d'1px à gauche (trackX-1) pour centrer sur la track 10px
        TerminalSkin.draw(g, TerminalSkin.THUMB, trackX - 1, thumbY);
    }

    // ── Slider WH/ME (horizontal, un seul checkbox centré) ───────────────────
    private void renderSlider(GuiGraphics g, int x, int y, int mx, int my)
    {
        int sx = x + SLIDER_X;
        int sy = y + SLIDER_Y;
        // Un seul checkbox 22×12 centré dans la zone 40×13
        // WH first → checkbox_wh.png, AE first → checkbox_ae.png
        int offsetX = (SLIDER_W - 22) / 2;  // (40-22)/2 = 9 → centré
        TerminalSkin.draw(g, warehouseFirst ? TerminalSkin.CHK_WH : TerminalSkin.CHK_AE, sx + offsetX, sy);

        if (over(mx, my, sx, sy, SLIDER_W, SLIDER_H))
            hoveredTooltip = warehouseFirst
                    ? Component.translatable("colonylink.term.jei_wh").getString()
                    : Component.translatable("colonylink.term.jei_me").getString();
    }

    // ── Bouton push (centre colonne) ──────────────────────────────────────────
    private void renderPushButton(GuiGraphics g, int mx, int my,
                                  int bx, int by, boolean pushed, String tooltip)
    {
        boolean hov = over(mx, my, bx, by, BTN_SZ, BTN_SZ);
        TerminalSkin.draw(g, pushed ? TerminalSkin.BTN_PUSH : TerminalSkin.BTN, bx, by);
        if (hov) hoveredTooltip = tooltip;
    }

    // ── Items des panels ──────────────────────────────────────────────────────
    private void renderPanelItems(GuiGraphics g, int panelX, int guiY, boolean isWh, int mx, int my)
    {
        // Slot survolé, calculé comme les tooltips (itemSlot) → highlight toujours
        // aligné sur le tooltip, à n'importe quel GUI Scale.
        int hovIdx = itemSlot(mx, my, panelX);
        if (isWh && (!hasWarehouseCard || !whErrorMsg.isEmpty()))
        {
            // Overlay "pas de card"
            g.fill(panelX, guiY + Y_ITEMS,
                    panelX + PANEL_COLS * SLOT_PITCH,
                    guiY + Y_ITEMS + visRows() * SLOT_PITCH,
                    C_NOCARD_OV);
            String msg1 = Component.translatable("colonylink.term.nocard1").getString();
            String msg2 = Component.translatable("colonylink.term.nocard2").getString();
            int cx = panelX + (PANEL_COLS * SLOT_PITCH) / 2;
            int cy = guiY + Y_ITEMS + (visRows() * SLOT_PITCH) / 2;
            g.drawCenteredString(font, msg1, cx, cy - 10, 0xFF8888BB);
            g.drawCenteredString(font, msg2, cx, cy + 2, 0xFF8888BB);
            return;
        }
        if (isWh && !whErrorMsg.isEmpty())
        {
            g.fill(panelX, guiY + Y_ITEMS,
                    panelX + PANEL_COLS * SLOT_PITCH,
                    guiY + Y_ITEMS + visRows() * SLOT_PITCH,
                    C_NOCARD_OV);
            g.drawCenteredString(font, whErrorMsg,
                    panelX + (PANEL_COLS * SLOT_PITCH) / 2,
                    guiY + Y_ITEMS + (visRows() * SLOT_PITCH) / 2, 0xFF8888BB);
            return;
        }
        if (!isWh && !menu.getPart().isAe2Active())
        {
            g.fill(panelX, guiY + Y_ITEMS,
                    panelX + PANEL_COLS * SLOT_PITCH,
                    guiY + Y_ITEMS + visRows() * SLOT_PITCH,
                    C_OFFLINE_OV);
            g.drawCenteredString(font, Component.translatable("colonylink.term.ae2_offline").getString(),
                    panelX + (PANEL_COLS * SLOT_PITCH) / 2,
                    guiY + Y_ITEMS + (visRows() * SLOT_PITCH) / 2, 0xFFFF4444);
            return;
        }

        int scroll = isWh ? whScroll : meScroll;
        Set<Integer> sel = isWh ? whSelected : meSelected;
        List<?> list = isWh ? whFiltered : meFiltered;

        for (int r = 0; r < visRows(); r++)
        {
            for (int c = 0; c < PANEL_COLS; c++)
            {
                int idx = scroll * PANEL_COLS + r * PANEL_COLS + c;
                if (idx >= list.size()) return;

                int sx = panelX + c * SLOT_PITCH;
                int sy = guiY + Y_ITEMS + r * SLOT_PITCH;
                boolean hov = (hovIdx == r * PANEL_COLS + c);

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
    private void renderTooltipForPanel(GuiGraphics g, int mx, int my, int rawMx, int rawMy)
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
                                Component.translatable("colonylink.term.in_warehouse", e.count()),
                                Component.translatable("colonylink.term.controls_wh")),
                        rawMx, rawMy);
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
                    lines.add(Component.translatable("colonylink.term.craftable_only"));
                else if (e.craftable())
                    lines.add(Component.translatable("colonylink.term.in_me_craftable", e.count()));
                else
                    lines.add(Component.translatable("colonylink.term.in_me", e.count()));
                lines.add(Component.translatable("colonylink.term.controls_me"));
                g.renderComponentTooltip(font, lines, rawMx, rawMy);
            }
        }
    }

    // =========================================================================
    // INPUT
    // =========================================================================
    @Override public boolean mouseClicked(double mx, double my, int btn)
    {

        boolean shift = hasShiftDown();
        int yb = topPos - rowDY();   // base des elements ancres en bas

        // ── v1.4.2 — Clic onglet Crafting ────────────────────────────────────
        if (over(mx, my, leftPos + TAB_X, yb + TAB_CRAFT_Y, TAB_W, TAB_H))
        {
            activeTab = 0;
            menu.setTab(0);
            return true;
        }

        // ── v1.4.2 — Clic onglet Cutter ──────────────────────────────────────
        if (over(mx, my, leftPos + TAB_X, yb + TAB_CUTTER_Y, TAB_W, TAB_H))
        {
            activeTab = 1;
            menu.setTab(1);
            return true;
        }

        // ── v1.4.8 — Cutter : bouton Encode depuis la queue ───────────────────
        if (activeTab == 1 && over(mx, my, leftPos + ENCODE_BTN_X, yb + ENCODE_BTN_Y,
                ENCODE_BTN_W, ENCODE_BTN_H))
        {
            ItemStack blank = menu.getSlot(BLANK_PATTERN_SLOT).getItem();
            boolean outputFree = menu.getDomumOutput().isEmpty();

            // Chemin 1 : sélection dans la queue
            if (domumQueueSelected >= 0 && domumQueueSelected < domumQueue.size()
                    && !blank.isEmpty() && outputFree)
            {
                encodePushed = true;
                ItemStack selectedStack = domumQueue.get(domumQueueSelected);
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                        new DomumEncodePatternPacket(menu.getHostPos(), menu.getHostSide(), selectedStack));
            }
            // Chemin 2 : ancien chemin via domumTargetSlot (item posé manuellement)
            else if (menu.canEncode())
            {
                encodePushed = true;
                menu.sendEncodeRequest();
            }
            return true;
        }

        // ── v1.4.8 — Cutter : clic sur la liste queue ────────────────────────
        if (activeTab == 1)
        {
            int ox = leftPos + PG_X;
            int oy = topPos - rowDY() + PG_Y;

            // Clic dans la zone liste
            if (over(mx, my, ox + CUT_LIST_X, oy + CUT_LIST_Y, CUT_LIST_W, CUT_LIST_H))
            {
                int relY = (int) my - (oy + CUT_LIST_Y);
                int row  = relY / CUT_ROW_H;
                int idx  = domumQueueScroll + row;
                if (idx >= 0 && idx < domumQueue.size())
                {
                    domumQueueSelected = (domumQueueSelected == idx) ? -1 : idx; // toggle
                    // Aperçu matériaux depuis l'item sélectionné
                    if (domumQueueSelected >= 0)
                        updateMaterialCache(domumQueue.get(domumQueueSelected));
                }
                return true;
            }

            // Slot Blank Pattern
            if (over(mx, my, leftPos + BLANK_RENDER_X, yb + BLANK_RENDER_Y, 16, 16))
            {
                this.slotClicked(menu.getSlot(BLANK_PATTERN_SLOT), BLANK_PATTERN_SLOT, btn,
                        net.minecraft.world.inventory.ClickType.PICKUP);
                return true;
            }

            // Slot output — récupérer le pattern encodé
            if (over(mx, my, leftPos + OUTPUT_RENDER_X, yb + OUTPUT_RENDER_Y, 16, 16))
            {
                if (!menu.getDomumOutput().isEmpty())
                {
                    this.slotClicked(menu.getSlot(DOMUM_OUTPUT_SLOT), DOMUM_OUTPUT_SLOT, btn,
                            net.minecraft.world.inventory.ClickType.PICKUP);
                    // La ligne disparaît via tickDomumQueueCraftableCheck au prochain scan AE2
                }
                return true;
            }

            // Slot item de référence (pour les matériaux preview)
            if (over(mx, my, ox + CUT_REF_X, oy + CUT_REF_Y, 16, 16))
            {
                this.slotClicked(menu.getSlot(DOMUM_TARGET_SLOT), DOMUM_TARGET_SLOT, btn,
                        net.minecraft.world.inventory.ClickType.PICKUP);
                return true;
            }
        }

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
                if (my >= yb + INV_Y && my < yb + INV_Y + 3 * SLOT_PITCH)
                {
                    int r = ((int) my - yb - INV_Y) / SLOT_PITCH;
                    int c = ((int) mx - leftPos - INV_X) / SLOT_PITCH;
                    invSlotIdx = PLAYER_INV_START + r * 9 + c;
                }
                else if (my >= yb + HOTBAR_Y && my < yb + HOTBAR_Y + SLOT_PITCH)
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
            if (my >= yb + INV_Y && my < yb + HOTBAR_Y + SLOT_PITCH) return true;
        }

        // ── Drag scrollbar WH ─────────────────────────────────────────────────
        // Hitbox alignée sur le thumb rendu : trackX-1, width=11
        if (over(mx, my, leftPos + SCROLL_WH_X - 1, topPos + SCROLL_TOP_Y,
                11, (SCROLL_BOT_Y - SCROLL_TOP_Y) - rowDY()))
        {
            draggingWh = true; draggingAe = false;
            dragStartY = (int) my;
            dragStartScroll = whScroll;
            return true;
        }

        // ── Drag scrollbar AE ─────────────────────────────────────────────────
        if (over(mx, my, leftPos + SCROLL_AE_X - 1, topPos + SCROLL_TOP_Y,
                11, (SCROLL_BOT_Y - SCROLL_TOP_Y) - rowDY()))
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
        if (over(mx, my, leftPos + SLIDER_X, yb + SLIDER_Y, SLIDER_W, SLIDER_H))
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
        if (over(mx, my, leftPos + 136, yb + 273, 7, 7))
        {
            clearGridToInventory(shift);
            return true;
        }

        // ── Bouton 8 : result → WH (hitbox invisible sur fond PNG) ──────────────
        // count=1 → 1 craft ; count>1 (shift) → boucle max côté serveur
        if (over(mx, my, leftPos + 252, yb + 279, 7, 7))
        {
            ItemStack r = menu.getCraftResult().getItem(0);
            if (!r.isEmpty())
                sendTx(r.copy(), shift ? r.getMaxStackSize() : 1,
                        TerminalTransferPacket.Direction.RESULT_TO_WH);
            return true;
        }

        // ── Bouton 9 : result → ME (hitbox invisible sur fond PNG) ──────────────
        if (over(mx, my, leftPos + 268, yb + 279, 8, 7))
        {
            ItemStack r = menu.getCraftResult().getItem(0);
            if (!r.isEmpty())
                sendTx(r.copy(), shift ? r.getMaxStackSize() : 1,
                        TerminalTransferPacket.Direction.RESULT_TO_ME);
            return true;
        }

        // ── Bouton 10 : result → inventaire (hitbox invisible sur fond PNG) ──────
        if (over(mx, my, leftPos + 260, yb + 313, 7, 7))
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
        encodePushed = false;

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

        // ── Drag scrollbar ────────────────────────────────────────────────────
        if (draggingWh || draggingAe)
        {
            int trackH    = (SCROLL_BOT_Y - SCROLL_TOP_Y) - rowDY(); // piste visible
            int deltaPx   = (int) my - dragStartY;
            List<?> list  = draggingWh ? whFiltered : meFiltered;
            int maxRows   = (int) Math.ceil(list.size() / (double) PANEL_COLS);
            int maxScroll = Math.max(1, maxRows - visRows());
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

        // Scroll liste Domum (onglet Cutter actif)
        if (activeTab == 1)
        {
            int ox = leftPos + PG_X;
            int oy = topPos - rowDY() + PG_Y;
            if (mx >= ox + CUT_LIST_X && mx < ox + CUT_LIST_X + CUT_LIST_W
                    && my >= oy + CUT_LIST_Y && my < oy + CUT_LIST_Y + CUT_LIST_H)
            {
                int maxScroll = Math.max(0, domumQueue.size() - CUT_ROWS_VIS);
                domumQueueScroll = Math.max(0, Math.min(domumQueueScroll - (int) Math.signum(dy), maxScroll));
                return true;
            }
        }

        int rT = topPos + Y_ITEMS;
        int rB = rT + visRows() * SLOT_PITCH;

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
        int rB = rT + visRows() * SLOT_PITCH;
        if (mx < panelX || mx >= panelX + PANEL_COLS * SLOT_PITCH || my < rT || my >= rB) return -1;
        int col = (mx - panelX) / SLOT_PITCH;
        int row = (my - rT)     / SLOT_PITCH;
        if (col >= PANEL_COLS || row >= visRows()) return -1;
        return row * PANEL_COLS + col;
    }

    private int clamp(int s, int tot)
    {
        int max = Math.max(0, (int) Math.ceil(tot / (double) PANEL_COLS) - visRows());
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