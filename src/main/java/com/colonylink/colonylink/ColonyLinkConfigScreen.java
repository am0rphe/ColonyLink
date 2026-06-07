package com.colonylink.colonylink;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI de configuration visuelle du ColonyLink — ouvert depuis le bouton engrenage.
 *
 * Paramètres :
 *  - Couleur fond (RGB via 3 sliders R/G/B + preview)
 *  - Couleur barre de titre (RGB)
 *  - Couleur bordures (RGB)
 *  - Épaisseur bordures (1–4, slider discret)
 *  - Opacité (0.2–1.0, slider)
 *  - Scale (0.5–1.5, slider)
 *
 * Navigation : tabs "Background | Title | Border | Layout"
 * Boutons : [Reset] [Cancel] [Apply & Save]
 *
 * Prévisualisation live : un mini-GUI ColonyLink factice dessiné à droite.
 */
public class ColonyLinkConfigScreen extends Screen
{
    private static final int W = 400;
    private static final int H = 260;

    // Tabs
    private static final String[] TABS = {"colonylink.cfg.tab_bg", "colonylink.cfg.tab_title", "colonylink.cfg.tab_border", "colonylink.cfg.tab_layout"};
    private int activeTab = 0;

    // Working copy (modifiée live, appliquée seulement sur "Apply & Save")
    private int   wBgColor;
    private int   wTitleColor;
    private int   wBorderColor;
    private int   wBorderWidth;
    private float wOpacity;
    private float wScale;

    // Slider drag
    private int   draggingSlider = -1; // index du slider en cours de drag
    private final Screen parent;

    // Sliders : [rx, ry, rw, rh, min, max, isFloat]
    // On les calcule dans render() dynamiquement selon la tab active

    public ColonyLinkConfigScreen(Screen parent)
    {
        super(Component.translatable("colonylink.cfg.title"));
        this.parent = parent;
        // Copie de travail depuis la config actuelle
        ColonyLinkGuiConfig cfg = ColonyLinkGuiConfig.get();
        wBgColor     = cfg.bgColor;
        wTitleColor  = cfg.titleColor;
        wBorderColor = cfg.borderColor;
        wBorderWidth = cfg.borderWidth;
        wOpacity     = cfg.opacity;
        wScale       = cfg.scale;
    }

    // ── Coordonnées ───────────────────────────────────────────────────────────

    private int gx() { return (this.width  - W) / 2; }
    private int gy() { return (this.height - H) / 2; }

    // Zone sliders (gauche)
    private int sliderAreaX() { return gx() + 10; }
    private int sliderAreaY() { return gy() + 55; }
    private int sliderW()     { return 210; }

    // Zone preview (droite)
    private int previewX()    { return gx() + 235; }
    private int previewY()    { return gy() + 50; }
    private int previewW()    { return 140; }
    private int previewH()    { return 160; }

    // ── Render ────────────────────────────────────────────────────────────────

    @Override
    public void renderBackground(GuiGraphics g, int mx, int my, float pt) {}

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt)
    {
        // Fond sombre semi-transparent
        g.fill(0, 0, this.width, this.height, 0xAA000000);

        int x = gx(), y = gy();

        // Fond GUI config
        g.fill(x, y, x + W, y + H, 0xFF2A2A2A);
        g.fill(x, y, x + W, y + 2, 0xFF888888);
        g.fill(x, y, x + 2, y + H, 0xFF888888);
        g.fill(x, y + H - 2, x + W, y + H, 0xFF111111);
        g.fill(x + W - 2, y, x + W, y + H, 0xFF111111);

        // Barre de titre
        g.fill(x + 2, y + 2, x + W - 2, y + 20, 0xFF3A3A3A);
        g.drawCenteredString(this.font, Component.translatable("colonylink.cfg.header").getString(), x + W / 2, y + 6, 0xFFFFAA);

        // Tabs
        drawTabs(g, mx, my);

        // Contenu tab
        drawTabContent(g, mx, my);

        // Preview
        drawPreview(g);

        // Boutons bas
        drawBottomButtons(g, mx, my);

        super.render(g, mx, my, pt);
    }

    private void drawTabs(GuiGraphics g, int mx, int my)
    {
        int x = gx() + 10, y = gy() + 22;
        // tw dynamique : largeur du GUI / nombre de tabs avec marges
        int tw = (W - 20) / TABS.length - 2, th = 16, sp = 2;
        for (int i = 0; i < TABS.length; i++)
        {
            int tx = x + 10 + i * (tw + sp);
            boolean active = i == activeTab;
            boolean hov = mx >= tx && mx <= tx + tw && my >= y && my <= y + th;
            int bg = active ? 0xFF555555 : (hov ? 0xFF404040 : 0xFF333333);
            int tc = active ? 0xFFFFAA : 0xAAAAAA;
            g.fill(tx, y, tx + tw, y + th, bg);
            g.fill(tx, y, tx + tw, y + 1, active ? 0xFFFFAA44 : 0xFF555555);
            g.drawCenteredString(this.font, Component.translatable(TABS[i]).getString(), tx + tw / 2, y + 3, tc);
        }
    }

    private void drawTabContent(GuiGraphics g, int mx, int my)
    {
        int sx = sliderAreaX(), sy = sliderAreaY();
        switch (activeTab)
        {
            case 0 -> drawColorSliders(g, mx, my, sx, sy, Component.translatable("colonylink.cfg.bg_color").getString(), wBgColor, 0);
            case 1 -> drawColorSliders(g, mx, my, sx, sy, Component.translatable("colonylink.cfg.title_color").getString(),  wTitleColor, 3);
            case 2 -> drawColorSliders(g, mx, my, sx, sy, Component.translatable("colonylink.cfg.border_color").getString(),     wBorderColor, 6);
            case 3 -> drawLayoutSliders(g, mx, my, sx, sy);
        }
    }

    /**
     * Dessine 3 sliders R/G/B pour une couleur.
     * sliderBase = index de base dans le système de slots (0, 3 ou 6).
     */
    private void drawColorSliders(GuiGraphics g, int mx, int my,
                                  int sx, int sy, String label, int color, int sliderBase)
    {
        g.drawString(this.font, "§7" + label, sx, sy - 8, 0xCCCCCC, false);

        int r = (color >> 16) & 0xFF;
        int gv = (color >> 8)  & 0xFF;
        int b  =  color        & 0xFF;
        int[] vals = {r, gv, b};
        String[] names = {"§cR", "§aG", "§9B"};
        int[] colors = {0xFF4444, 0x44FF44, 0x4488FF};

        for (int i = 0; i < 3; i++)
        {
            int slotY = sy + i * 36;
            drawSlider(g, mx, my, sx, slotY, sliderW(), 0, 255, vals[i],
                    names[i] + " §f" + vals[i], colors[i], sliderBase + i);
        }
    }

    private void drawLayoutSliders(GuiGraphics g, int mx, int my, int sx, int sy)
    {
        g.drawString(this.font, Component.translatable("colonylink.cfg.section_layout").getString(), sx, sy - 8, 0xCCCCCC, false);

        // Opacity : slot 9
        int opPct = Math.round(wOpacity * 100);
        drawSlider(g, mx, my, sx, sy, sliderW(), 10, 100, opPct,
                Component.translatable("colonylink.cfg.opacity", opPct, (opPct <= 10 ? Component.translatable("colonylink.cfg.opacity_min").getString() : opPct < 30 ? Component.translatable("colonylink.cfg.opacity_verylow").getString() : "")).getString(),
                0xFFCC44, 9);

        // Scale : slot 10 (50–150 = 0.5–1.5 stocké ×100)
        int scalePct = Math.round(wScale * 100);
        drawSlider(g, mx, my, sx, sy + 36, sliderW(), 50, 150, scalePct,
                Component.translatable("colonylink.cfg.scale", (scalePct / 100), String.format("%02d", scalePct % 100)).getString(),
                0xFFCC44, 10);

        // Border width : slot 11
        drawSlider(g, mx, my, sx, sy + 72, sliderW(), 1, 4, wBorderWidth,
                Component.translatable("colonylink.cfg.border_width", wBorderWidth).getString(), 0xFFCC44, 11);
    }

    /**
     * Dessine un slider horizontal.
     * slotId : identifiant unique pour le drag.
     */
    private void drawSlider(GuiGraphics g, int mx, int my,
                            int sx, int sy, int sw,
                            int min, int max, int value,
                            String label, int trackColor, int slotId)
    {
        int trackY = sy + 16;
        int trackH = 6;

        // Label
        g.drawString(this.font, label, sx, sy + 2, 0xDDDDDD, false);

        // Track fond
        g.fill(sx, trackY, sx + sw, trackY + trackH, 0xFF1A1A1A);
        g.fill(sx, trackY, sx + sw, trackY + 1, 0xFF555555);

        // Track rempli
        int filled = (value - min) * sw / Math.max(1, max - min);
        g.fill(sx, trackY, sx + filled, trackY + trackH, trackColor & 0xBBFFFFFF | 0x88000000);
        g.fill(sx, trackY, sx + filled, trackY + trackH, trackColor);

        // Thumb
        int thumbX = sx + filled;
        boolean hov = mx >= thumbX - 4 && mx <= thumbX + 4
                && my >= trackY - 2 && my <= trackY + trackH + 2;
        g.fill(thumbX - 3, trackY - 2, thumbX + 3, trackY + trackH + 2,
                hov || draggingSlider == slotId ? 0xFFFFFFFF : 0xFFCCCCCC);

        // Si en drag sur ce slot, update la valeur
        if (draggingSlider == slotId)
        {
            int newVal = min + (mx - sx) * (max - min) / sw;
            newVal = Math.max(min, Math.min(max, newVal));
            applySliderValue(slotId, newVal);
        }
    }

    /** Applique la valeur d'un slider sur la copie de travail. */
    private void applySliderValue(int slotId, int val)
    {
        switch (slotId)
        {
            // Background R/G/B
            case 0 -> wBgColor = (wBgColor & 0xFF00FFFF) | (val << 16);
            case 1 -> wBgColor = (wBgColor & 0xFFFF00FF) | (val << 8);
            case 2 -> wBgColor = (wBgColor & 0xFFFFFF00) | val;
            // Title R/G/B
            case 3 -> wTitleColor = (wTitleColor & 0xFF00FFFF) | (val << 16);
            case 4 -> wTitleColor = (wTitleColor & 0xFFFF00FF) | (val << 8);
            case 5 -> wTitleColor = (wTitleColor & 0xFFFFFF00) | val;
            // Border R/G/B
            case 6 -> wBorderColor = (wBorderColor & 0xFF00FFFF) | (val << 16);
            case 7 -> wBorderColor = (wBorderColor & 0xFFFF00FF) | (val << 8);
            case 8 -> wBorderColor = (wBorderColor & 0xFFFFFF00) | val;
            // Layout
            case 9  -> wOpacity     = Math.max(0.1f, val / 100f);
            case 10 -> wScale       = val / 100f;
            case 11 -> wBorderWidth = val;
        }
    }

    private void drawPreview(GuiGraphics g)
    {
        int px = previewX(), py = previewY(), pw = previewW(), ph = previewH();

        // Label
        g.drawString(this.font, Component.translatable("colonylink.cfg.preview").getString(), px, py - 10, 0x888888, false);

        // Calcul couleurs avec opacité de travail
        int bgC     = applyOpacity(wBgColor, wOpacity);
        int titleC  = applyOpacity(wTitleColor, wOpacity);
        int borderC = applyOpacity(wBorderColor, wOpacity);
        int shadowC = darken(borderC, 0.5f);
        int bw      = wBorderWidth;

        // Fond
        g.fill(px + bw, py + bw, px + pw - bw, py + ph - bw, bgC);

        // Bordures
        g.fill(px, py, px + pw, py + bw, borderC);
        g.fill(px, py, px + bw, py + ph, borderC);
        g.fill(px, py + ph - bw, px + pw, py + ph, shadowC);
        g.fill(px + pw - bw, py, px + pw, py + ph, shadowC);

        // Barre de titre
        int titleH = 16;
        g.fill(px + bw, py + bw, px + pw - bw, py + bw + titleH, titleC);

        // Lignes simulées (contenu)
        int lineY = py + bw + titleH + 4;
        int[] lineColors = {0x66FFFFFF, 0x44FFFFFF, 0x55FFFFFF, 0x33FFFFFF};
        int[] lineWidths = {60, 45, 70, 30};
        for (int i = 0; i < 4 && lineY + 6 < py + ph - bw - 4; i++)
        {
            g.fill(px + bw + 4, lineY, px + bw + 4 + lineWidths[i], lineY + 4, lineColors[i % lineColors.length]);
            lineY += 10;
        }

        // Mini bouton simulé
        int btnY = py + ph - bw - 14;
        g.fill(px + bw + 4, btnY, px + bw + 40, btnY + 10, 0x88004488);
        g.fill(px + bw + 4, btnY, px + bw + 40, btnY + 1, 0x88FFFFFF);
    }

    private void drawBottomButtons(GuiGraphics g, int mx, int my)
    {
        int y = gy() + H - 30;
        int x = gx();

        // [Reset]
        drawBtn(g, mx, my, x + 10,      y, 80, 20, Component.translatable("colonylink.cfg.btn_reset").getString(),         0xFF330000, 0xFF550000, 0xFF884444, 12);
        // [Cancel]
        drawBtn(g, mx, my, x + 100,     y, 80, 20, Component.translatable("colonylink.cfg.btn_cancel").getString(),        0xFF333333, 0xFF444444, 0xFFAAAAAA, 13);
        // [Apply & Save]
        drawBtn(g, mx, my, x + W - 130, y, 120, 20, Component.translatable("colonylink.cfg.btn_apply").getString(), 0xFF004400, 0xFF006600, 0xFF44FF44, 14);
    }

    private void drawBtn(GuiGraphics g, int mx, int my,
                         int bx, int by, int bw, int bh,
                         String label, int bgNormal, int bgHover, int tc, int id)
    {
        boolean hov = mx >= bx && mx <= bx + bw && my >= by && my <= by + bh;
        g.fill(bx, by, bx + bw, by + bh, hov ? bgHover : bgNormal);
        g.fill(bx, by, bx + bw, by + 1, 0xFF888888);
        g.fill(bx, by, bx + 1, by + bh, 0xFF888888);
        g.fill(bx, by + bh - 1, bx + bw, by + bh, 0xFF222222);
        g.fill(bx + bw - 1, by, bx + bw, by + bh, 0xFF222222);
        g.drawCenteredString(this.font, label, bx + bw / 2, by + 4, tc);
    }

    // ── Mouse ─────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mx, double my, int btn)
    {
        int x = gx(), y = gy();

        // Tabs
        int tw = (W - 20) / TABS.length - 2, th = 16, sp = 2, tx0 = x + 10, ty = y + 22;
        for (int i = 0; i < TABS.length; i++)
        {
            int txI = tx0 + i * (tw + sp);
            if (mx >= txI && mx <= txI + tw && my >= ty && my <= ty + th)
            { activeTab = i; return true; }
        }

        // Sliders — détecte si le clic est sur un track
        draggingSlider = detectSliderClick(mx, my);
        if (draggingSlider >= 0) return true;

        // Boutons bas
        int by = y + H - 30;
        if (my >= by && my <= by + 20)
        {
            // Reset
            if (mx >= x + 10 && mx <= x + 90)
            {
                wBgColor = ColonyLinkGuiConfig.DEFAULT_BG_COLOR;
                wTitleColor = ColonyLinkGuiConfig.DEFAULT_TITLE_COLOR;
                wBorderColor = ColonyLinkGuiConfig.DEFAULT_BORDER_COLOR;
                wBorderWidth = ColonyLinkGuiConfig.DEFAULT_BORDER_WIDTH;
                wOpacity = ColonyLinkGuiConfig.DEFAULT_OPACITY;
                wScale = ColonyLinkGuiConfig.DEFAULT_SCALE;
                return true;
            }
            // Cancel
            if (mx >= x + 100 && mx <= x + 180)
            { this.minecraft.setScreen(parent); return true; }
            // Apply & Save
            if (mx >= x + W - 130 && mx <= x + W - 10)
            {
                applyAndSave();
                this.minecraft.setScreen(parent);
                return true;
            }
        }

        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy)
    {
        if (draggingSlider >= 0)
        {
            // applySliderValue est appelé dans drawSlider lors du prochain render
            return true;
        }
        return super.mouseDragged(mx, my, btn, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int btn)
    {
        draggingSlider = -1;
        return super.mouseReleased(mx, my, btn);
    }

    /**
     * Détecte si le clic tombe sur l'un des tracks de sliders de la tab active.
     * Retourne le slotId du slider cliqué, ou -1.
     */
    private int detectSliderClick(double mx, double my)
    {
        int sx = sliderAreaX(), sy = sliderAreaY(), sw = sliderW();
        int trackH = 6;

        List<int[]> slots = getSlotsForTab(); // [slotId, trackY]
        for (int[] slot : slots)
        {
            int slotId = slot[0], trackY = slot[1];
            if (mx >= sx && mx <= sx + sw && my >= trackY - 2 && my <= trackY + trackH + 2)
                return slotId;
        }
        return -1;
    }

    /** Retourne les [slotId, trackY] des sliders de la tab active. */
    private List<int[]> getSlotsForTab()
    {
        List<int[]> slots = new ArrayList<>();
        int sy = sliderAreaY();
        switch (activeTab)
        {
            case 0 -> { for (int i = 0; i < 3; i++) slots.add(new int[]{i,     sy + i * 36 + 16}); }
            case 1 -> { for (int i = 0; i < 3; i++) slots.add(new int[]{3 + i, sy + i * 36 + 16}); }
            case 2 -> { for (int i = 0; i < 3; i++) slots.add(new int[]{6 + i, sy + i * 36 + 16}); }
            case 3 ->
            {
                slots.add(new int[]{9,  sy       + 16});
                slots.add(new int[]{10, sy + 36  + 16});
                slots.add(new int[]{11, sy + 72  + 16});
            }
        }
        return slots;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void applyAndSave()
    {
        ColonyLinkGuiConfig cfg = ColonyLinkGuiConfig.get();
        cfg.bgColor     = wBgColor;
        cfg.titleColor  = wTitleColor;
        cfg.borderColor = wBorderColor;
        cfg.borderWidth = wBorderWidth;
        cfg.opacity     = wOpacity;
        cfg.scale       = wScale;
        cfg.clamp();
        cfg.save();
    }

    private static int applyOpacity(int argb, float opacity)
    {
        int a  = (argb >> 24) & 0xFF;
        int a2 = (int)(a * opacity);
        return (a2 << 24) | (argb & 0x00FFFFFF);
    }

    private static int darken(int argb, float factor)
    {
        int a  = (argb >> 24) & 0xFF;
        int r  = (int)(((argb >> 16) & 0xFF) * factor);
        int gv = (int)(((argb >> 8)  & 0xFF) * factor);
        int b  = (int)(( argb        & 0xFF) * factor);
        return (a << 24) | (r << 16) | (gv << 8) | b;
    }

    /**
     * Appelé par ColonyLinkPacket.handle() quand ce screen est ouvert par-dessus
     * le GUI wand. Met à jour le parent ColonyLinkScreen sans fermer ce screen.
     */
    public void updateParentPacket(ColonyLinkPacket packet)
    {
        if (parent instanceof ColonyLinkScreen cls)
            cls.updateFromPacket(packet);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}