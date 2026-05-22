package com.colonylink.colonylink;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

import static com.colonylink.colonylink.WarehouseLinkTerminalMenu.*;

/**
 * Warehouse Link Terminal — Screen v1.3.8
 *
 * v1.3.8 :
 *  - Compteurs items en police ~75% (taille AE2-like) pour ne plus jamais déborder
 *  - Format AE2 standard pour les grands nombres : 999, 1.0K, 9.9K, 10K, 999K, 1.0M, 1.0B, 999B
 *
 * Pick system (fake cursor, 100% client-side) :
 *  - Clic gauche panel WH/ME  : PICKUP_FROM_WH/ME → serveur extrait + setCarried
 *  - Clic droit panel WH/ME   : PICKUP avec moitié du stack
 *  - Carried non-vide + clic panel : PUT_INTO_WH/ME → insère le carried
 *  - Dépôt dans inventaire    : WH_TO_PLAYER ou ME_TO_PLAYER(item, count)
 *  - Dépôt dans grille craft  : WH_TO_PLAYER ou ME_TO_PLAYER → item va en inventaire,
 *                                le joueur place ensuite manuellement dans la grille
 *  - ESC ou clic ailleurs     : annulation, item reste à la source
 *
 * Le serveur extrait l'item UNIQUEMENT au moment du dépôt (packet deposit).
 * Si le joueur annule (ESC), rien n'est prélevé.
 *
 * Pick depuis inventaire → WH/ME : shift+clic sur slot inventaire → INV_TO_WH/ME
 */
public class WarehouseLinkTerminalScreen extends AbstractContainerScreen<WarehouseLinkTerminalMenu>
{
    // ── Palette ───────────────────────────────────────────────────────────────
    private static final int C_BORDER_HI     = 0xFF8B89B0;
    private static final int C_BORDER_LO     = 0xFF1E1C2E;
    private static final int C_BORDER_MID    = 0xFF3F3E56;
    private static final int C_PANEL_BG      = 0xFF6E6E8A;
    private static final int C_ITEM_AREA_BG  = 0xFF18172A;
    private static final int C_HEADER_BG     = 0xFF32303E;
    private static final int C_CTR_BG        = 0xFF28263A;
    private static final int C_SLOT_BG       = 0xFF8B8FA8;
    private static final int C_SLOT_DARK     = 0xFF373550;
    private static final int C_SLOT_LIGHT    = 0xFFFFFFFF;
    private static final int C_SCROLL_BG     = 0xFF18172A;
    private static final int C_SCROLL_TH     = 0xFF9A9FB4;
    private static final int C_BTN_NORMAL    = 0xFF3C3A50;
    private static final int C_BTN_HOVER     = 0xFF54527A;
    private static final int C_BTN_HI        = 0xFF8888AA;
    private static final int C_BTN_LO        = 0xFF2A2840;
    private static final int C_ARROW         = 0xFFF3F3F3;
    private static final int C_TEXT_MUTED    = 0xFF9A9FB4;
    private static final int C_TEXT_WH       = 0xFF9A9FB4;
    private static final int C_TEXT_ME       = 0xFF6699FF;
    private static final int C_CRAFTABLE_DOT = 0xFF00FF88;
    private static final int C_CRAFTONLY_OV  = 0x4400AAFF;
    private static final int C_OFFLINE_OV    = 0xBB220000;
    private static final int C_NOCARD_OV     = 0xBB111122;
    private static final int C_HOVER_SL      = 0x5500CCFF;
    private static final int C_SELECTED      = 0x8800AAFF;
    private static final int C_SEPARATOR     = 0xFF4A4860;
    private static final int C_SLIDE_TRACK   = 0xFF1A1A2E;
    private static final int C_SLIDE_ACTIVE  = 0xFF6664A0;
    private static final int C_SLIDE_THUMB   = 0xFF9A9FB4;
    private static final int C_SF_OUTER      = 0xFFF2F2F2;
    private static final int C_SF_BG         = 0xFF0E0D1A;
    private static final int C_SF_INNER      = 0xFF3F3E56;
    private static final int C_SF_TEXT       = 0xFFCCCDFF;
    private static final int C_SF_PH         = 0xFF555577;
    private static final int C_ICON_BOX      = 0xFFB8A070;
    private static final int C_ICON_ME       = 0xFF6699FF;
    private static final int C_ICON_PLAYER   = 0xFF88CCAA;

    // ── v1.3.8 — Compteur items ──────────────────────────────────────────────
    /** Échelle de la police du compteur sur les slots (1.0 = taille MC standard ~7px). */
    private static final float COUNT_FONT_SCALE = 0.72f;
    /** Couleur du compteur. */
    private static final int   C_COUNT_TEXT    = 0xFFFFFFFF;
    /** Couleur de l'ombre du compteur (drop shadow). */
    private static final int   C_COUNT_SHADOW  = 0xFF3F3F3F;

    // Spacing boutons centraux
    private static final int BTN_W_CTR = 18;
    private static final int BTN_H_CTR = 14;
    private static final int BTN_SP    = 28;
    private static final int SLIDE_H   = 12;

    // ── État ──────────────────────────────────────────────────────────────────
    private final List<WarehouseTerminalSyncPacket.WarehouseItemEntry> whItems    = new ArrayList<>();
    private final List<WarehouseTerminalSyncPacket.WarehouseItemEntry> whFiltered = new ArrayList<>();
    private final List<MeItemEntry> meItems = new ArrayList<>(), meFiltered = new ArrayList<>();

    private boolean hasWarehouseCard = false;
    private String  whErrorMsg = "";

    // Champs statiques : survivent au remplacement du Screen (ex: retour de CraftAmountMenu AE2)
    private static String savedWhSearch = "";
    private static String savedMeSearch = "";
    // true = l'utilisateur a fermé manuellement → removed() ne doit PAS sauvegarder
    private boolean closedManually = false;

    private String  whSearch = "", meSearch = "";
    private boolean whSearchFocused = false, meSearchFocused = false;
    private int     whScroll = 0, meScroll = 0;

    private boolean warehouseFirst = true;

    // Sélection shift+drag
    private final Set<Integer> whSelected = new LinkedHashSet<>();
    private final Set<Integer> meSelected = new LinkedHashSet<>();
    private boolean shiftWasDown = false;

    // Le curseur est géré par le carried vanilla (server setCarried + broadcastCarriedItem).
    // Le client voit l'item sous la souris via AbstractContainerScreen.renderSlot(-1).
    // Clic sur panel WH/ME → PICKUP_FROM_WH/ME → serveur extrait + setCarried.
    // Clic sur panel WH/ME avec carried non-vide → PUT_INTO_WH/ME → insère le carried.
    // Dépôt dans inventaire/craft : géré automatiquement par vanilla carried.

    // Tooltip QoL
    private String hoveredTooltip = null;

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
        super.init();
        titleLabelX = inventoryLabelX = -10000;
        // Restaurer les search bars (survivent à CraftAmountMenu AE2)
        whSearch = savedWhSearch;
        meSearch = savedMeSearch;
        rebuildWh();
        rebuildMe();
        WarehouseLinkTerminalMenu.warehouseFirst = warehouseFirst;
        PacketDistributor.sendToServer(new TerminalGuiStatePacket(true, partHostPos(), partSide()));
    }

    /**
     * Appelé quand le joueur ferme manuellement le GUI (ESC/E).
     * On vide les saved search pour repartir propre à la prochaine ouverture.
     */
    @Override public void onClose()
    {
        closedManually = true; // empêche removed() de sauvegarder
        savedWhSearch = "";
        savedMeSearch = "";
        PacketDistributor.sendToServer(new TerminalGuiStatePacket(false, partHostPos(), partSide()));
        super.onClose();
    }

    /**
     * Appelé quand ce Screen est remplacé par un autre (ex: CraftAmountMenu AE2).
     * C'est ICI qu'on sauvegarde les search bars pour le retour via returnToMainMenu().
     */
    @Override public void removed()
    {
        // Ne sauvegarder que si remplacé par un autre screen (CraftAmountMenu AE2)
        // et non si fermé manuellement par le joueur
        if (!closedManually)
        {
            savedWhSearch = whSearch;
            savedMeSearch = meSearch;
        }
        super.removed();
    }

    // ── Packets ───────────────────────────────────────────────────────────────
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
    { whFiltered.clear(); String q=whSearch.toLowerCase(); for(var e:whItems) if(q.isEmpty()||e.stack().getDisplayName().getString().toLowerCase().contains(q)) whFiltered.add(e); whScroll=clamp(whScroll,whFiltered.size()); }

    private void rebuildMe()
    { meFiltered.clear(); String q=meSearch.toLowerCase(); for(var e:meItems) if(q.isEmpty()||e.stack().getDisplayName().getString().toLowerCase().contains(q)) meFiltered.add(e); meScroll=clamp(meScroll,meFiltered.size()); }

    // =========================================================================
    // RENDER
    // =========================================================================
    @Override public void render(GuiGraphics g, int mx, int my, float pt)
    {
        hoveredTooltip = null;
        boolean shiftNow = hasShiftDown();
        if (shiftWasDown && !shiftNow) { whSelected.clear(); meSelected.clear(); }
        shiftWasDown = shiftNow;

        renderBackground(g, mx, my, pt);
        super.render(g, mx, my, pt);
        renderPanelItems(g, leftPos+X_WH, topPos, true);
        renderPanelItems(g, leftPos+X_ME, topPos, false);
        renderTooltipForPanel(g, mx, my);
        renderTooltip(g, mx, my);

        if (hoveredTooltip != null)
        {
            // Support multi-line tooltips via \n separator
            String[] lines = hoveredTooltip.split("\n");
            if (lines.length == 1)
                g.renderTooltip(font, Component.literal(hoveredTooltip), mx, my);
            else
            {
                java.util.List<net.minecraft.network.chat.Component> ttLines = new java.util.ArrayList<>();
                for (String line : lines) ttLines.add(Component.literal(line));
                g.renderComponentTooltip(font, ttLines, mx, my);
            }
        }

        // Le carried vanilla est rendu automatiquement par AbstractContainerScreen
    }

    @Override protected void renderLabels(GuiGraphics g, int mx, int my) {}

    @Override protected void renderBg(GuiGraphics g, float pt, int mx, int my)
    {
        int x=leftPos, y=topPos;
        drawGuiFill(g, x, y);
        drawItemPanel(g, x+X_WH, y, true, mx, my);
        drawScrollbar(g, x+X_SCROLL_WH, y+H_HEADER, SCROLL_W, ROWS_H, whFiltered.size(), PANEL_ROWS*PANEL_COLS, whScroll);
        drawCentreColumn(g, x+X_CTR, y, mx, my);
        drawScrollbar(g, x+X_SCROLL_ME, y+H_HEADER, SCROLL_W, ROWS_H, meFiltered.size(), PANEL_ROWS*PANEL_COLS, meScroll);
        drawItemPanel(g, x+X_ME, y, false, mx, my);
        drawCraftZone(g, x, y, mx, my);
        drawInventoryZone(g, x, y);
        drawGuiBorder(g, x, y);
    }

    // =========================================================================
    // PRIMITIVES
    // =========================================================================
    private void drawGuiFill(GuiGraphics g, int x, int y)
    { g.fill(x+2, y+2, x+GUI_W-2, y+Y_CRAFT_LABEL, C_PANEL_BG); }

    private void drawGuiBorder(GuiGraphics g, int x, int y)
    {
        int bot = y+Y_CRAFT_LABEL;
        g.fill(x,y,x+GUI_W,y+1,C_BORDER_HI); g.fill(x,y+1,x+1,bot+1,C_BORDER_HI);
        g.fill(x+1,bot,x+GUI_W,bot+1,C_BORDER_LO); g.fill(x+GUI_W-1,y,x+GUI_W,bot,C_BORDER_LO);
        g.fill(x+1,y+1,x+GUI_W-1,y+2,C_BORDER_MID); g.fill(x+1,bot-1,x+GUI_W-1,bot,C_BORDER_MID);
        g.fill(x+1,y+2,x+2,bot-1,C_BORDER_MID); g.fill(x+GUI_W-2,y+2,x+GUI_W-1,bot-1,C_BORDER_MID);
    }

    private void drawSlot(GuiGraphics g, int x, int y)
    {
        g.fill(x,y,x+16,y+16,C_SLOT_BG);
        g.fill(x-1,y-1,x+17,y,C_SLOT_DARK); g.fill(x-1,y,x,y+17,C_SLOT_DARK);
        g.fill(x,y,x+16,y+1,0xFF4A4860); g.fill(x,y+1,x+1,y+16,0xFF4A4860);
        g.fill(x,y+16,x+17,y+17,C_SLOT_LIGHT); g.fill(x+16,y,x+17,y+17,C_SLOT_LIGHT);
    }

    private void drawScrollbar(GuiGraphics g, int x, int y, int w, int h, int total, int visible, int scroll)
    {
        g.fill(x,y,x+w,y+h,C_BORDER_MID); g.fill(x+1,y+1,x+w-1,y+h-1,C_SCROLL_BG);
        if (total<=visible||total==0) return;
        int tH=Math.max(10,(int)(h*(float)visible/total));
        int tY=y+1+(int)((h-2-tH)*(float)scroll/Math.max(1,total-visible));
        g.fill(x+1,tY,x+w-1,tY+tH,C_SCROLL_TH); g.fill(x+1,tY,x+w-1,tY+1,0xFFBBBFD4);
        g.fill(x+1,tY+tH-1,x+w-1,tY+tH,0xFF6A6E88);
    }

    private boolean drawBtn(GuiGraphics g, int mx, int my, int bx, int by, int bw, int bh)
    {
        boolean hov=mx>=bx&&mx<bx+bw&&my>=by&&my<by+bh;
        g.fill(bx,by,bx+bw,by+bh,hov?C_BTN_HOVER:C_BTN_NORMAL);
        g.fill(bx,by,bx+bw,by+1,C_BTN_HI); g.fill(bx,by+1,bx+1,by+bh-1,C_BTN_HI);
        g.fill(bx,by+bh-1,bx+bw,by+bh,C_BTN_LO); g.fill(bx+bw-1,by+1,bx+bw,by+bh-1,C_BTN_LO);
        return hov;
    }

    private void arrowRight(GuiGraphics g, int cx, int cy, int c)
    { g.fill(cx,cy+2,cx+4,cy+3,c); g.fill(cx+2,cy+1,cx+5,cy+4,c); g.fill(cx+4,cy,cx+7,cy+5,c); }
    private void arrowLeft(GuiGraphics g, int cx, int cy, int c)
    { g.fill(cx+3,cy+2,cx+7,cy+3,c); g.fill(cx+2,cy+1,cx+5,cy+4,c); g.fill(cx,cy,cx+3,cy+5,c); }
    private void arrowDown(GuiGraphics g, int cx, int cy, int c)
    { g.fill(cx,cy,cx+7,cy+2,c); g.fill(cx+1,cy+2,cx+6,cy+4,c); g.fill(cx+2,cy+4,cx+5,cy+5,c); g.fill(cx+3,cy+5,cx+4,cy+6,c); }

    private void drawSlide(GuiGraphics g, int mx, int my, int bx, int by, int w, int h)
    {
        int half=(w-2)/2;
        g.fill(bx,by,bx+w,by+h,C_BORDER_MID); g.fill(bx+1,by+1,bx+w-1,by+h-1,C_SLIDE_TRACK);
        if (warehouseFirst) g.fill(bx+1,by+1,bx+1+half,by+h-1,C_SLIDE_ACTIVE);
        else                g.fill(bx+1+half,by+1,bx+w-1,by+h-1,C_SLIDE_ACTIVE);
        g.fill(bx+1+half,by+1,bx+2+half,by+h-1,C_BORDER_MID);
        int tx=warehouseFirst?bx+1:bx+1+half;
        g.fill(tx,by+1,tx+half,by+h-1,C_SLIDE_THUMB); g.fill(tx,by+1,tx+half,by+2,0xFFBBBFD4); g.fill(tx,by+2,tx+1,by+h-1,0xFFBBBFD4);
        if (mx>=bx&&mx<bx+w&&my>=by&&my<by+h)
        { g.fill(bx,by,bx+w,by+1,C_BTN_HI); g.fill(bx,by+h-1,bx+w,by+h,C_BTN_HI);
            hoveredTooltip = warehouseFirst ? "JEI priority: Warehouse first" : "JEI priority: ME first"; }
    }

    private void drawSearch(GuiGraphics g, int x, int y, int w, int h, String txt, boolean focused)
    {
        g.fill(x-1,y-1,x+w+1,y+h+1,C_SF_OUTER); g.fill(x,y,x+w,y+h,C_SF_BG);
        g.fill(x,y,x+w,y+1,C_SF_INNER); g.fill(x,y+1,x+1,y+h,C_SF_INNER);
        if (txt.isEmpty()) g.drawString(font,"Search...",x+2,y+1,C_SF_PH,false);
        else               g.drawString(font,txt,x+2,y+1,C_SF_TEXT,false);
        if (focused&&(System.currentTimeMillis()/500)%2==0)
            g.drawString(font,"|",x+2+font.width(txt),y+1,C_SF_TEXT,false);
    }

    // ── Icônes pixel-art boutons craft ────────────────────────────────────────
    private void drawIconStorage(GuiGraphics g, int bx, int by, int bw, int bh, boolean isWh)
    {
        int ox=bx+(bw-11)/2, oy=by+(bh-7)/2;
        int boxC=C_ICON_BOX, netC=isWh?0xFFC8A060:C_ICON_ME;
        g.fill(ox,oy,ox+4,oy+1,boxC); g.fill(ox,oy+3,ox+4,oy+4,boxC);
        g.fill(ox,oy+1,ox+1,oy+3,boxC); g.fill(ox+3,oy+1,ox+4,oy+3,boxC);
        g.fill(ox+1,oy+1,ox+3,oy+2,0xFF7A6035);
        g.fill(ox+4,oy+2,ox+7,oy+3,C_ARROW); g.fill(ox+7,oy+1,ox+8,oy+4,C_ARROW); g.fill(ox+8,oy+2,ox+9,oy+3,C_ARROW);
        g.fill(ox+9,oy+2,ox+11,oy+3,netC); g.fill(ox+10,oy+1,ox+11,oy+4,netC);
        g.fill(ox+9,oy+1,ox+10,oy+2,netC); g.fill(ox+9,oy+3,ox+10,oy+4,netC);
    }

    private void drawIconInventory(GuiGraphics g, int bx, int by, int bw, int bh)
    {
        int ox=bx+(bw-11)/2, oy=by+(bh-7)/2;
        g.fill(ox,oy,ox+4,oy+1,C_ICON_BOX); g.fill(ox,oy+3,ox+4,oy+4,C_ICON_BOX);
        g.fill(ox,oy+1,ox+1,oy+3,C_ICON_BOX); g.fill(ox+3,oy+1,ox+4,oy+3,C_ICON_BOX);
        g.fill(ox+1,oy+1,ox+3,oy+2,0xFF7A6035);
        g.fill(ox+4,oy+2,ox+7,oy+3,C_ARROW); g.fill(ox+7,oy+1,ox+8,oy+4,C_ARROW); g.fill(ox+8,oy+2,ox+9,oy+3,C_ARROW);
        g.fill(ox+9,oy+1,ox+12,oy+4,C_ICON_PLAYER); g.fill(ox+10,oy,ox+11,oy+1,C_ICON_PLAYER);
        g.fill(ox+9,oy+4,ox+12,oy+5,0xFF5FAA80);
    }

    // =========================================================================
    // SECTIONS
    // =========================================================================
    private void drawItemPanel(GuiGraphics g, int px, int py, boolean isWh, int mx, int my)
    {
        g.fill(px,py,px+PANEL_W,py+H_HEADER,C_HEADER_BG);
        g.fill(px,py+H_HEADER-1,px+PANEL_W,py+H_HEADER,C_BORDER_MID);
        String ttl=isWh?"Warehouse":"Applied";
        g.drawString(font,ttl,px+PANEL_W/2-font.width(ttl)/2,py+3,isWh?C_TEXT_WH:C_TEXT_ME,false);
        String cnt=String.valueOf(isWh?whFiltered.size():meFiltered.size());
        g.drawString(font,cnt,px+PANEL_W-font.width(cnt)-2,py+3,C_TEXT_MUTED,false);
        drawSearch(g,px+2,py+H_HEADER-10,PANEL_W-4,8,isWh?whSearch:meSearch,isWh?whSearchFocused:meSearchFocused);
        Set<Integer> sel=isWh?whSelected:meSelected;
        if (!sel.isEmpty()) g.drawString(font,"§e"+sel.size()+" selected",px+2,py+3,0xFFFFDD00,false);

        int rTop=py+H_HEADER;
        g.fill(px,rTop,px+PANEL_W,rTop+ROWS_H,C_ITEM_AREA_BG);
        for(int r=0;r<PANEL_ROWS;r++) for(int c=0;c<PANEL_COLS;c++)
        { int sx=px+c*SLOT,sy=rTop+r*SLOT; g.fill(sx+1,sy+1,sx+SLOT-1,sy+SLOT-1,C_SLOT_BG); g.fill(sx,sy,sx+SLOT,sy+1,0xFF232234); g.fill(sx,sy,sx+1,sy+SLOT,0xFF232234); }

        if (isWh&&!hasWarehouseCard)
        { g.fill(px,rTop,px+PANEL_W,rTop+ROWS_H,C_NOCARD_OV); g.drawCenteredString(font,"Insert Warehouse",px+PANEL_W/2,rTop+ROWS_H/2-10,0xFF8888BB); g.drawCenteredString(font,"Link Card",px+PANEL_W/2,rTop+ROWS_H/2+2,0xFF8888BB); return; }
        if (isWh&&!whErrorMsg.isEmpty())
        { g.fill(px,rTop,px+PANEL_W,rTop+ROWS_H,C_NOCARD_OV); g.drawCenteredString(font,whErrorMsg,px+PANEL_W/2,rTop+ROWS_H/2,0xFF8888BB); return; }
        if (!isWh&&!menu.getPart().isAe2Active())
        { g.fill(px,rTop,px+PANEL_W,rTop+ROWS_H,C_OFFLINE_OV); g.drawCenteredString(font,"AE2 Offline",px+PANEL_W/2,rTop+ROWS_H/2,0xFFFF4444); return; }

        int scroll=isWh?whScroll:meScroll;
        for(int r=0;r<PANEL_ROWS;r++) for(int c=0;c<PANEL_COLS;c++)
        {
            int idx=(scroll*PANEL_COLS)+r*PANEL_COLS+c;
            int gx=px+c*SLOT, gy=rTop+r*SLOT;
            boolean hov=mx>=gx&&mx<gx+SLOT&&my>=gy&&my<gy+SLOT;
            if (sel.contains(idx)) g.fill(gx+1,gy+1,gx+SLOT-1,gy+SLOT-1,C_SELECTED);
            else if (hov)         g.fill(gx+1,gy+1,gx+SLOT-1,gy+SLOT-1,C_HOVER_SL);
        }
    }

    private void drawCentreColumn(GuiGraphics g, int cx, int cy, int mx, int my)
    {
        g.fill(cx,cy,cx+CTR_W,cy+H_HEADER+ROWS_H,C_CTR_BG);
        g.fill(cx,cy,cx+1,cy+H_HEADER+ROWS_H,C_BORDER_MID); g.fill(cx+CTR_W-1,cy,cx+CTR_W,cy+H_HEADER+ROWS_H,C_BORDER_MID);
        int cX=cx+(CTR_W-16)/2, cY=cy+(H_HEADER-16)/2;
        drawSlot(g,cX,cY);
        ItemStack card=menu.getPart().getWarehouseCardSlot().getStackInSlot(0);
        if (!card.isEmpty()) g.renderItem(card,cX,cY);
        else g.drawCenteredString(font,"?",cx+CTR_W/2,cY+5,0xFF6664A0);

        int totalH=2*BTN_H_CTR+(BTN_SP-BTN_H_CTR)+6+SLIDE_H;
        int sY=cy+H_HEADER+(ROWS_H-totalH)/2;
        int bX=cx+(CTR_W-BTN_W_CTR)/2;
        boolean hasWhSel=!whSelected.isEmpty(), hasMeSel=!meSelected.isEmpty(), hasSel=hasWhSel||hasMeSel;

        if (drawBtn(g,mx,my,bX,sY,BTN_W_CTR,BTN_H_CTR))
        { if(hasWhSel) hoveredTooltip="Transfer selected WH → ME"; else if(hasMeSel) hoveredTooltip="Transfer selected ME → Warehouse"; else hoveredTooltip="Select items first\n§7Shift+click or drag over items to highlight them"; }
        if (hasWhSel)      arrowLeft(g,bX+BTN_W_CTR/2-3,sY+(BTN_H_CTR-5)/2,C_ARROW);
        else if (hasMeSel) arrowRight(g,bX+BTN_W_CTR/2-3,sY+(BTN_H_CTR-5)/2,C_ARROW);
        else               arrowLeft(g,bX+BTN_W_CTR/2-3,sY+(BTN_H_CTR-5)/2,C_TEXT_MUTED);

        if (drawBtn(g,mx,my,bX,sY+BTN_SP,BTN_W_CTR,BTN_H_CTR))
            hoveredTooltip=hasSel?"Transfer selected → Inventory":"Select items first\n§7Shift+click or drag over items to highlight them";
        arrowDown(g,bX+BTN_W_CTR/2-3,sY+BTN_SP+(BTN_H_CTR-6)/2,hasSel?C_ARROW:C_TEXT_MUTED);

        drawSlide(g,mx,my,cx+1,sY+BTN_SP+BTN_H_CTR+6,CTR_W-2,SLIDE_H);
    }

    private void drawCraftZone(GuiGraphics g, int x, int y, int mx, int my)
    {
        int bX=x+BLOC_BAS_X, bW=BLOC_BAS_W, zY=y+Y_CRAFT_LABEL;
        g.fill(bX+2,zY+2,bX+bW-2,y+GUI_H-2,C_PANEL_BG);
        g.fill(bX+2,zY+2,bX+bW-2,y+Y_INV_LABEL-2,C_CTR_BG);
        g.fill(bX,zY,bX+bW,zY+1,C_BORDER_HI); g.fill(bX,zY+1,bX+1,y+GUI_H,C_BORDER_HI);
        g.fill(bX+1,y+GUI_H-1,bX+bW,y+GUI_H,C_BORDER_LO); g.fill(bX+bW-1,zY,bX+bW,y+GUI_H-1,C_BORDER_LO);
        g.fill(bX+1,zY+1,bX+bW-1,zY+2,C_BORDER_MID); g.fill(bX+1,zY+2,bX+2,y+GUI_H-1,C_BORDER_MID);
        g.fill(bX+1,y+GUI_H-2,bX+bW-1,y+GUI_H-1,C_BORDER_MID); g.fill(bX+bW-2,zY+2,bX+bW-1,y+GUI_H-2,C_BORDER_MID);
        g.fill(bX+2,zY+2,bX+bW-2,zY+3,C_BTN_HI);
        String lbl=Component.translatable("gui.ae2.CraftingTerminal").getString();
        g.drawCenteredString(font,lbl,bX+bW/2,zY+4,C_TEXT_MUTED);

        int cY=y+Y_CRAFT_TOP;
        for(int r=0;r<3;r++) for(int c=0;c<3;c++) drawSlot(g,x+CRAFT_GRID_X+c*SLOT,cY+r*SLOT);
        arrowRight(g,x+CRAFT_GRID_X+3*SLOT+5,cY+SLOT+4,0xFF9A9FB4);
        drawSlot(g,x+CRAFT_OUT_X,cY+SLOT);

        int bY=y+BTN_CRAFT_Y;
        if (drawBtn(g,mx,my,x+BTN_STORAGE_X,bY,BTN_W,BTN_H))
            hoveredTooltip="Clear grid \u2192 "+(warehouseFirst?"Warehouse":"ME Network")+" | Shift: also clear output";
        drawIconStorage(g,x+BTN_STORAGE_X,bY,BTN_W,BTN_H,warehouseFirst);
        if (drawBtn(g,mx,my,x+BTN_INV_X,bY,BTN_W,BTN_H))
            hoveredTooltip="Clear grid \u2192 Inventory | Shift: also clear output";
        drawIconInventory(g,x+BTN_INV_X,bY,BTN_W,BTN_H);
    }

    private void drawInventoryZone(GuiGraphics g, int x, int y)
    {
        g.drawCenteredString(font,Component.translatable("container.inventory").getString(),x+BLOC_BAS_X+BLOC_BAS_W/2,y+Y_INV_LABEL+2,C_TEXT_MUTED);
        for(int r=0;r<3;r++) for(int c=0;c<9;c++) drawSlot(g,x+INV_LEFT+c*SLOT,y+Y_INV+r*SLOT);
        g.fill(x+INV_LEFT,y+Y_HOTBAR-3,x+INV_LEFT+9*SLOT,y+Y_HOTBAR-2,C_SEPARATOR);
        for(int c=0;c<9;c++) drawSlot(g,x+INV_LEFT+c*SLOT,y+Y_HOTBAR);
    }

    // ── Items ─────────────────────────────────────────────────────────────────
    private void renderPanelItems(GuiGraphics g, int px, int py, boolean isWh)
    {
        if (isWh&&(!hasWarehouseCard||!whErrorMsg.isEmpty())) return;
        if (!isWh&&!menu.getPart().isAe2Active()) return;
        int rTop=py+H_HEADER, scroll=isWh?whScroll:meScroll, start=scroll*PANEL_COLS;
        List<?> list=isWh?whFiltered:meFiltered;
        for(int r=0;r<PANEL_ROWS;r++) for(int c=0;c<PANEL_COLS;c++)
        {
            int idx=start+r*PANEL_COLS+c; if(idx>=list.size()) return;
            int sx=px+c*SLOT+1, sy=rTop+r*SLOT+1;
            ItemStack stack; long count; boolean craftable=false;
            if(isWh){var e=whFiltered.get(idx);stack=e.stack();count=e.count();}
            else    {var e=meFiltered.get(idx);stack=e.stack();count=e.count();craftable=e.craftable();}

            // Rendu de l'item SEUL (sans le count vanilla, qui est trop gros et déborde)
            g.renderItem(stack,sx,sy);
            // Rendu des décorations vanilla (durabilité, cooldown) mais SANS texte count
            g.renderItemDecorations(font,stack,sx,sy,"");

            // Indicateurs craftable côté ME
            if (!isWh)
            {
                if (craftable && count<=0)
                {
                    // Craftable-only : overlay bleu + "+"
                    g.fill(sx,sy,sx+16,sy+16,C_CRAFTONLY_OV);
                    g.drawString(font,"+",sx+4,sy+4,0xFF00CCFF,true);
                    // Pas de count à afficher
                    continue;
                }
                if (craftable)
                {
                    // Stocked ET craftable : petit dot vert en haut-droite
                    g.fill(sx+13,sy,sx+16,sy+3,C_CRAFTABLE_DOT);
                }
            }

            // ── v1.3.8 — Count compact, police scalée à droite-bas du slot ──
            if (count > 0)
                drawCount(g, sx, sy, count);
        }
    }

    /**
     * v1.3.8 — Dessine le compteur d'items dans le coin bas-droit du slot
     * avec une police réduite (~72% de la taille standard).
     *
     * Format style AE2 : 1-999 brut, 1.0K-9.9K, 10K-999K, 1.0M-999M, 1.0B-999B.
     * Tous les formats tiennent en ≤ 4 caractères.
     *
     * Le texte est rendu avec une matrice scale + un drop shadow manuel
     * (le drawString natif avec shadow rend une ombre à l'échelle 1x qui
     * paraît ridicule sur du texte à 0.72x ; on dessine donc l'ombre
     * manuellement, décalée d'1 px).
     */
    private void drawCount(GuiGraphics g, int slotX, int slotY, long count)
    {
        String txt = fmtCount(count);
        float scale = COUNT_FONT_SCALE;

        // Taille rendue du texte (font.width retourne la largeur à scale 1)
        int wRaw = font.width(txt);
        int hRaw = font.lineHeight; // = 9 px standard

        // Position : coin bas-droit du slot 16x16, ancrée à 1px du bord
        // (slotX, slotY) = coin haut-gauche du slot rendu (sx, sy dans renderPanelItems)
        int slotRight  = slotX + 16;
        int slotBottom = slotY + 16;
        // Position en coords scalées (à diviser par scale dans la matrice scalée)
        float drawX = (slotRight  - 1) / scale - wRaw;
        float drawY = (slotBottom - 1) / scale - hRaw;

        var pose = g.pose();
        pose.pushPose();
        // Z élevé pour que le compteur passe devant l'item rendu
        pose.translate(0f, 0f, 200f);
        pose.scale(scale, scale, 1f);

        // Drop shadow manuel : on draw d'abord en sombre décalé d'1 px (en coords scalées),
        // puis le texte en clair par-dessus. Le shadow=false du drawString évite le shadow
        // vanilla qui serait à l'échelle 1.
        g.drawString(font, txt, (int)Math.round(drawX) + 1, (int)Math.round(drawY) + 1,
                C_COUNT_SHADOW, false);
        g.drawString(font, txt, (int)Math.round(drawX), (int)Math.round(drawY),
                C_COUNT_TEXT, false);

        pose.popPose();
    }

    // ── Tooltips ──────────────────────────────────────────────────────────────
    private void renderTooltipForPanel(GuiGraphics g, int mx, int my)
    {
        int s=slot(mx,my,leftPos+X_WH);
        if(s>=0&&hasWarehouseCard&&whErrorMsg.isEmpty()){
            int i=whScroll*PANEL_COLS+s; if(i<whFiltered.size()){
                var e=whFiltered.get(i);
                g.renderComponentTooltip(font,List.of(e.stack().getDisplayName(),
                        Component.literal("\u00a77In warehouse: \u00a7f"+e.count()),
                        Component.literal("\u00a78Left: pick stack  Right: pick half  Shift+drag: select")),mx,my); return; }}
        s=slot(mx,my,leftPos+X_ME);
        if(s>=0&&menu.getPart().isAe2Active()){
            int i=meScroll*PANEL_COLS+s; if(i<meFiltered.size()){
                var e=meFiltered.get(i);
                List<Component> lines=new ArrayList<>(); lines.add(e.stack().getDisplayName());
                if(e.craftable()&&e.count()<=0) lines.add(Component.literal("\u00a7aCraftable-only \u00a7e(middle-click to autocraft)"));
                else if(e.craftable()) lines.add(Component.literal("\u00a77In ME: \u00a7f"+e.count()+" \u00a7a\u25cf craftable"));
                else lines.add(Component.literal("\u00a77In ME: \u00a7f"+e.count()));
                lines.add(Component.literal("\u00a78Left: pick stack  Right: pick half  Middle: autocraft"));
                g.renderComponentTooltip(font,lines,mx,my); }}
    }

    // =========================================================================
    // INPUT
    // =========================================================================
    @Override public boolean mouseClicked(double mx, double my, int btn)
    {
        boolean shift = hasShiftDown();

        // ── Shift+clic inventaire → WH ou ME selon slider ────────────────────
        // Vérifie que la destination est disponible avant d'envoyer
        if (shift && !hasControlDown()
                && mx >= leftPos+INV_LEFT && mx < leftPos+INV_LEFT+9*SLOT)
        {
            // Destination WH : nécessite une card valide et aucune erreur
            boolean canSendToWh = hasWarehouseCard && whErrorMsg.isEmpty();
            // Destination ME : nécessite AE2 actif
            boolean canSendToMe = menu.getPart().isAe2Active();
            // Destination effective selon le slider
            boolean sendToWh = warehouseFirst ? canSendToWh : (!canSendToMe && canSendToWh);
            boolean sendToMe = !warehouseFirst ? canSendToMe : (!canSendToWh && canSendToMe);
            boolean canSend  = warehouseFirst ? canSendToWh : canSendToMe;

            if (canSend)
            {
                int invSlotIdx = -1;
                if (my >= topPos+Y_INV && my < topPos+Y_INV+3*SLOT)
                { int r=((int)my-topPos-Y_INV)/SLOT, c=((int)mx-leftPos-INV_LEFT)/SLOT; invSlotIdx=PLAYER_INV_START+r*9+c; }
                else if (my >= topPos+Y_HOTBAR && my < topPos+Y_HOTBAR+SLOT)
                { int c=((int)mx-leftPos-INV_LEFT)/SLOT; invSlotIdx=PLAYER_HOTBAR_START+c; }
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
            // Si destination indisponible mais clic dans inventaire avec shift → absorber
            // pour éviter que vanilla fasse un quickMove vers la grille craft
            if (my >= topPos+Y_INV && my < topPos+Y_HOTBAR+SLOT)
                return true;
        }

        // ── Search bars ───────────────────────────────────────────────────────
        int sfY = topPos+H_HEADER-10;
        if (my >= sfY && my < sfY+8) {
            if (mx>=leftPos+X_WH+1&&mx<leftPos+X_WH+PANEL_W-1){whSearchFocused=true;meSearchFocused=false;return true;}
            if (mx>=leftPos+X_ME+1&&mx<leftPos+X_ME+PANEL_W-1){meSearchFocused=true;whSearchFocused=false;return true;}
        }
        whSearchFocused = meSearchFocused = false;

        // ── Panel WH — style AE2 PICKUP_OR_SET_DOWN ───────────────────────────
        // Panel WH :
        // - Si carried non-vide → PUT_INTO_WH (quelle que soit la cible, même slot vide)
        // - Si carried vide ET slot non-vide → PICKUP_FROM_WH
        // - Shift+clic → sélection
        int ws = slot((int)mx,(int)my,leftPos+X_WH);
        if (ws >= 0 && hasWarehouseCard && whErrorMsg.isEmpty()) {
            int idx = whScroll*PANEL_COLS+ws;
            ItemStack carried = minecraft.player.containerMenu.getCarried();
            if (!shift && !carried.isEmpty()) {
                // Carried non-vide : toujours déposer dans le WH (slot vide ou non)
                sendTx(carried.copy(), carried.getCount(), TerminalTransferPacket.Direction.PUT_INTO_WH);
                whSelected.clear(); meSelected.clear();
                return true;
            }
            if (idx < whFiltered.size()) {
                var e = whFiltered.get(idx);
                if (shift) {
                    if (whSelected.contains(idx)) whSelected.remove(idx); else whSelected.add(idx);
                    meSelected.clear();
                } else {
                    // Carried vide : PICKUP depuis le WH
                    int pickCount = (btn == 1)
                            ? (int)Math.min(Math.max(1,(e.count()+1)/2), e.stack().getMaxStackSize())
                            : (int)Math.min(e.count(), e.stack().getMaxStackSize());
                    sendPickup(e.stack(), pickCount, TerminalTransferPacket.Direction.PICKUP_FROM_WH);
                    whSelected.clear(); meSelected.clear();
                }
                return true;
            }
            // Slot vide dans le panel, carried vide aussi → absorber le clic
            return true;
        }

        // Panel ME :
        // - Si carried non-vide → PUT_INTO_ME (quelle que soit la cible, même slot vide)
        // - Si carried vide ET slot non-vide → PICKUP_FROM_ME ou autocraft (middle)
        // - Shift+clic → sélection
        int ms = slot((int)mx,(int)my,leftPos+X_ME);
        if (ms >= 0 && menu.getPart().isAe2Active()) {
            int idx = meScroll*PANEL_COLS+ms;
            ItemStack carried = minecraft.player.containerMenu.getCarried();
            if (!shift && btn != 2 && !carried.isEmpty()) {
                // Carried non-vide : toujours déposer dans le ME (slot vide ou non)
                sendTx(carried.copy(), carried.getCount(), TerminalTransferPacket.Direction.PUT_INTO_ME);
                whSelected.clear(); meSelected.clear();
                return true;
            }
            if (idx < meFiltered.size()) {
                var e = meFiltered.get(idx);
                if (btn == 2) {
                    if (e.craftable()) PacketDistributor.sendToServer(new TerminalCraftPacket(
                            TerminalCraftPacket.Mode.AUTOCRAFT, e.stack().copyWithCount(1), 1, partHostPos(), partSide()));
                    return true;
                }
                if (shift) {
                    if (meSelected.contains(idx)) meSelected.remove(idx); else meSelected.add(idx);
                    whSelected.clear();
                } else {
                    // Carried vide : PICKUP depuis le ME
                    int pickCount;
                    if (btn == 1) {
                        long half=(e.count()+1)/2;
                        pickCount=(int)Math.min(Math.max(1,half),e.stack().getMaxStackSize());
                        if (e.count()<=0) pickCount=0;
                    } else {
                        pickCount = e.count()>0 ? (int)Math.min(e.count(),e.stack().getMaxStackSize()) : 1;
                    }
                    if (pickCount > 0)
                        sendPickup(e.stack(), pickCount, TerminalTransferPacket.Direction.PICKUP_FROM_ME);
                    whSelected.clear(); meSelected.clear();
                }
                return true;
            }
            // Slot vide dans le panel, carried vide aussi → absorber le clic
            return true;
        }

        // ── Boutons centre ────────────────────────────────────────────────────
        int totalH=2*BTN_H_CTR+(BTN_SP-BTN_H_CTR)+6+SLIDE_H;
        int sY=topPos+H_HEADER+(ROWS_H-totalH)/2;
        int bX=leftPos+X_CTR+(CTR_W-BTN_W_CTR)/2;

        if (over(mx,my,bX,sY,BTN_W_CTR,BTN_H_CTR)) {
            if (!whSelected.isEmpty()) { for(int i:new ArrayList<>(whSelected)){ if(i<whFiltered.size()){var e=whFiltered.get(i);sendTx(e.stack(),(int)Math.min(e.count(),e.stack().getMaxStackSize()),TerminalTransferPacket.Direction.WH_TO_ME);} } whSelected.clear(); }
            else if (!meSelected.isEmpty()) { for(int i:new ArrayList<>(meSelected)){ if(i<meFiltered.size()){var e=meFiltered.get(i);sendTx(e.stack(),(int)Math.min(e.count(),e.stack().getMaxStackSize()),TerminalTransferPacket.Direction.ME_TO_WH);} } meSelected.clear(); }
            return true;
        }
        if (over(mx,my,bX,sY+BTN_SP,BTN_W_CTR,BTN_H_CTR)) {
            if (!whSelected.isEmpty()) { for(int i:new ArrayList<>(whSelected)){ if(i<whFiltered.size()){var e=whFiltered.get(i);sendTx(e.stack(),(int)Math.min(e.count(),e.stack().getMaxStackSize()),TerminalTransferPacket.Direction.WH_TO_PLAYER);} } whSelected.clear(); }
            else if (!meSelected.isEmpty()) { for(int i:new ArrayList<>(meSelected)){ if(i<meFiltered.size()){var e=meFiltered.get(i);sendTx(e.stack(),(int)Math.min(e.count(),e.stack().getMaxStackSize()),TerminalTransferPacket.Direction.ME_TO_PLAYER);} } meSelected.clear(); }
            return true;
        }
        if (over(mx,my,leftPos+X_CTR+1,sY+BTN_SP+BTN_H_CTR+6,CTR_W-2,SLIDE_H))
        { warehouseFirst=!warehouseFirst; WarehouseLinkTerminalMenu.warehouseFirst=warehouseFirst; return true; }

        // ── Boutons craft (Option A) ───────────────────────────────────────────
        if (over(mx,my,leftPos+BTN_STORAGE_X,topPos+BTN_CRAFT_Y,BTN_W,BTN_H)) { clearGridToStorage(shift); return true; }
        if (over(mx,my,leftPos+BTN_INV_X,topPos+BTN_CRAFT_Y,BTN_W,BTN_H))     { clearGridToInventory(shift); return true; }

        return super.mouseClicked(mx,my,btn);
    }

    @Override public boolean mouseDragged(double mx, double my, int btn, double dx, double dy)
    {
        if (hasShiftDown())
        {
            int ws=slot((int)mx,(int)my,leftPos+X_WH);
            if(ws>=0&&hasWarehouseCard){ int idx=whScroll*PANEL_COLS+ws; if(idx<whFiltered.size()){ meSelected.clear(); whSelected.add(idx); return true; } }
            int ms=slot((int)mx,(int)my,leftPos+X_ME);
            if(ms>=0&&menu.getPart().isAe2Active()){ int idx=meScroll*PANEL_COLS+ms; if(idx<meFiltered.size()){ whSelected.clear(); meSelected.add(idx); return true; } }
        }
        return super.mouseDragged(mx,my,btn,dx,dy);
    }

    @Override public boolean mouseScrolled(double mx, double my, double dx, double dy)
    {
        int rT=topPos+H_HEADER, rB=rT+ROWS_H;
        if(my>=rT&&my<rB){
            if(mx>=leftPos+X_WH&&mx<leftPos+X_WH+PANEL_W+SCROLL_W){ whScroll=clamp(whScroll-(int)Math.signum(dy),whFiltered.size()); return true; }
            if(mx>=leftPos+X_ME&&mx<leftPos+X_ME+PANEL_W+SCROLL_W){ meScroll=clamp(meScroll-(int)Math.signum(dy),meFiltered.size()); return true; }
        }
        return super.mouseScrolled(mx,my,dx,dy);
    }

    @Override public boolean keyPressed(int key, int scan, int mod)
    {
        if(whSearchFocused){ if(key==259&&!whSearch.isEmpty())whSearch=whSearch.substring(0,whSearch.length()-1); else if(key==256)whSearchFocused=false; rebuildWh(); return true; }
        if(meSearchFocused){ if(key==259&&!meSearch.isEmpty())meSearch=meSearch.substring(0,meSearch.length()-1); else if(key==256)meSearchFocused=false; rebuildMe(); return true; }
        return super.keyPressed(key,scan,mod);
    }

    @Override public boolean charTyped(char c, int mod)
    {
        if(whSearchFocused){ whSearch+=c; rebuildWh(); return true; }
        if(meSearchFocused){ meSearch+=c; rebuildMe(); return true; }
        return super.charTyped(c,mod);
    }

    // =========================================================================
    // ACTIONS BOUTONS CRAFT
    // =========================================================================
    private void clearGridToStorage(boolean shift)
    {
        PacketDistributor.sendToServer(new TerminalTransferPacket(ItemStack.EMPTY,0,
                warehouseFirst ? TerminalTransferPacket.Direction.CRAFT_TO_WH
                        : TerminalTransferPacket.Direction.CRAFT_TO_ME,
                partHostPos(),partSide()));
        if (shift)
        {
            ItemStack r=menu.getCraftResult().getItem(0);
            if (!r.isEmpty()) sendTx(r.copy(),r.getCount(),
                    warehouseFirst ? TerminalTransferPacket.Direction.RESULT_TO_WH
                            : TerminalTransferPacket.Direction.RESULT_TO_ME);
        }
    }

    private void clearGridToInventory(boolean shift)
    {
        PacketDistributor.sendToServer(new TerminalTransferPacket(ItemStack.EMPTY,0,
                TerminalTransferPacket.Direction.CRAFT_TO_PLAYER,partHostPos(),partSide()));
        if (shift)
        {
            ItemStack r=menu.getCraftResult().getItem(0);
            if (!r.isEmpty()) this.slotClicked(menu.getSlot(CRAFT_OUTPUT_SLOT),CRAFT_OUTPUT_SLOT,0,net.minecraft.world.inventory.ClickType.QUICK_MOVE);
        }
    }

    // =========================================================================
    // HELPERS
    // =========================================================================
    /** Envoie un packet PICKUP : template (count=1) + count demandé. */
    private void sendPickup(ItemStack template, int count, TerminalTransferPacket.Direction dir)
    { PacketDistributor.sendToServer(new TerminalTransferPacket(template.copyWithCount(1), count, dir, partHostPos(), partSide())); }

    private void sendTx(ItemStack s, int n, TerminalTransferPacket.Direction d)
    { PacketDistributor.sendToServer(new TerminalTransferPacket(s.copyWithCount(n),n,d,partHostPos(),partSide())); }

    private int slot(int mx, int my, int pX)
    { int rT=topPos+H_HEADER; if(mx<pX||mx>=pX+PANEL_W||my<rT||my>=rT+PANEL_ROWS*SLOT)return -1; int col=(mx-pX)/SLOT,row=(my-rT)/SLOT; if(col>=PANEL_COLS||row>=PANEL_ROWS)return -1; return row*PANEL_COLS+col; }

    private int clamp(int s, int tot) { int max=Math.max(0,(int)Math.ceil(tot/(double)PANEL_COLS)-PANEL_ROWS); return Math.max(0,Math.min(s,max)); }
    private boolean over(double mx, double my, int bx, int by, int bw, int bh) { return mx>=bx&&mx<bx+bw&&my>=by&&my<by+bh; }

    /**
     * v1.3.8 — Format compact style AE2 pour les compteurs d'items.
     *
     * Règles (max 4 caractères) :
     *   0-999          → "0", "1", ..., "999"
     *   1000-9999      → "1.0K", "1.5K", ..., "9.9K"     (1 décimale)
     *   10_000-999_999 → "10K", "11K", ..., "999K"        (pas de décimale)
     *   1M-9.9M        → "1.0M", ..., "9.9M"              (1 décimale)
     *   10M-999M       → "10M", "11M", ..., "999M"
     *   1B-9.9B        → "1.0B", ..., "9.9B"
     *   10B+           → "10B", "11B", ..., "999B" (plafond fonctionnel)
     *   au-delà        → "∞"  (improbable mais on évite tout overflow visuel)
     */
    private static String fmtCount(long n)
    {
        if (n < 0)             return "0";
        if (n < 1_000)         return Long.toString(n);
        if (n < 10_000)        return fmtDecimal(n, 1_000)  + "K";   // 1.0K..9.9K
        if (n < 1_000_000)     return (n / 1_000)           + "K";   // 10K..999K
        if (n < 10_000_000)    return fmtDecimal(n, 1_000_000) + "M";// 1.0M..9.9M
        if (n < 1_000_000_000) return (n / 1_000_000)       + "M";   // 10M..999M
        if (n < 10_000_000_000L)  return fmtDecimal(n, 1_000_000_000L) + "B"; // 1.0B..9.9B
        if (n < 1_000_000_000_000L) return (n / 1_000_000_000L) + "B";        // 10B..999B
        return "\u221e"; // ∞ — au-delà de 1T, indicateur fonctionnel
    }

    /** Helper : formate n/divisor avec 1 décimale, ex: fmtDecimal(1500, 1000) = "1.5". */
    private static String fmtDecimal(long n, long divisor)
    {
        long whole = n / divisor;
        long frac  = (n * 10 / divisor) % 10;
        return whole + "." + frac;
    }

    private net.minecraft.core.BlockPos partHostPos()
    { var be=menu.getPart().getHostBlockEntity(); return be!=null?be.getBlockPos():net.minecraft.core.BlockPos.ZERO; }
    private int partSide() { var s=menu.getPart().getSide(); return s!=null?s.ordinal():0; }

    public record MeItemEntry(ItemStack stack, long count, boolean craftable) {}
}