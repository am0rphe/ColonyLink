package com.colonylink.colonylink;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.nio.file.Path;

/**
 * Configuration visuelle du GUI ColonyLink — persistée côté client uniquement.
 * Fichier : .minecraft/config/colonylink_gui.json
 *
 * Paramètres :
 *   bgColor       — couleur ARGB du fond du GUI (sans la barre de titre)
 *   titleColor    — couleur ARGB de la barre de titre
 *   borderColor   — couleur ARGB des bordures
 *   borderWidth   — épaisseur des bordures (1–4 px)
 *   opacity       — opacité globale du GUI (0.2–1.0, appliquée sur bgColor + titleColor)
 *   scale         — facteur de zoom du GUI (0.5–1.5)
 *
 * Les couleurs sont stockées en int ARGB (0xAARRGGBB).
 * L'opacité modifie le canal alpha avant le rendu (multiplicateur sur l'alpha des couleurs).
 * Le scale est appliqué via PoseStack.scale() centré sur le GUI.
 *
 * Singleton — accès via ColonyLinkGuiConfig.get().
 */
public class ColonyLinkGuiConfig
{
    // ── Thème d'apparence ─────────────────────────────────────────────────────
    // DEFAULT = apparence historique du Clipboard, pilotée par bg/title/border.
    // AE      = réplique du look Warehouse Link Terminal (palette extraite de
    //           TerminalSkin/WarehouseLinkTerminalScreen). Appearance-only : aucun
    //           comportement, packet ou logique n'en dépend.
    public enum Theme { DEFAULT, AE }

    // ── Defaults ──────────────────────────────────────────────────────────────
    public static final int   DEFAULT_BG_COLOR     = 0xFF8B8B8B;
    public static final int   DEFAULT_TITLE_COLOR  = 0xFF6B6B6B;
    public static final int   DEFAULT_BORDER_COLOR = 0xFFFFFFFF;
    public static final int   DEFAULT_BORDER_WIDTH = 2;
    public static final float DEFAULT_OPACITY      = 1.0f;
    public static final float DEFAULT_SCALE        = 0.9f;
    public static final Theme DEFAULT_THEME        = Theme.DEFAULT;

    // Controls (buttons, tabs) keep a minimum alpha so they stay locatable when the
    // background is made very transparent. Effective control alpha = max(opacity, this).
    public static final float MIN_CONTROL_OPACITY  = 0.50f;

    // ── Champs (sérialisés par Gson) ──────────────────────────────────────────
    public int   bgColor     = DEFAULT_BG_COLOR;
    public int   titleColor  = DEFAULT_TITLE_COLOR;
    public int   borderColor = DEFAULT_BORDER_COLOR;
    public int   borderWidth = DEFAULT_BORDER_WIDTH;
    public float opacity     = DEFAULT_OPACITY;
    public float scale       = DEFAULT_SCALE;
    public Theme theme       = DEFAULT_THEME;

    // ── Singleton ─────────────────────────────────────────────────────────────
    private static ColonyLinkGuiConfig INSTANCE = null;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "colonylink_gui.json";

    public static ColonyLinkGuiConfig get()
    {
        if (INSTANCE == null) INSTANCE = load();
        return INSTANCE;
    }

    public static void reload()
    {
        INSTANCE = load();
    }

    // ── Persistance ───────────────────────────────────────────────────────────

    private static Path configPath()
    {
        return Minecraft.getInstance().gameDirectory.toPath()
                .resolve("config").resolve(FILE_NAME);
    }

    private static ColonyLinkGuiConfig load()
    {
        Path path = configPath();
        if (path.toFile().exists())
        {
            try (Reader r = new FileReader(path.toFile()))
            {
                ColonyLinkGuiConfig cfg = GSON.fromJson(r, ColonyLinkGuiConfig.class);
                if (cfg != null)
                {
                    cfg.clamp();
                    return cfg;
                }
            }
            catch (Exception e)
            {
                ColonyLink.LOGGER.warn("[ColonyLink] Failed to load GUI config, using defaults: {}", e.getMessage());
            }
        }
        ColonyLinkGuiConfig defaults = new ColonyLinkGuiConfig();
        defaults.save();
        return defaults;
    }

    public void save()
    {
        try
        {
            Path path = configPath();
            path.getParent().toFile().mkdirs();
            try (Writer w = new FileWriter(path.toFile()))
            {
                GSON.toJson(this, w);
            }
        }
        catch (Exception e)
        {
            ColonyLink.LOGGER.warn("[ColonyLink] Failed to save GUI config: {}", e.getMessage());
        }
    }

    /** Applique les bornes minimales/maximales sur tous les champs. */
    public void clamp()
    {
        borderWidth = Math.max(1, Math.min(4, borderWidth));
        opacity     = Math.max(0.1f, Math.min(1.0f, opacity));
        scale       = Math.max(0.5f, Math.min(1.5f, scale));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // PALETTE AE — valeurs structurelles extraites du Warehouse Link Terminal
    // (TerminalSkin.PAL_BG + constantes C_* de WarehouseLinkTerminalScreen). Ne
    // couvre QUE le chrome (fonds, cadres, bevels, tabs neutres, scrollbar,
    // séparateurs, icônes neutres). Les couleurs sémantiques (vert/rouge/bleu/
    // gris-désactivé + getWorkerStatusColor) NE PASSENT JAMAIS par ici.
    // Les valeurs marquées "eye-tune" sont des candidates de départ (Phase C).
    // ══════════════════════════════════════════════════════════════════════════
    private static final int AE_FRAME_BG      = 0xFFADB0C4; // PAL_BG[0]
    private static final int AE_TITLE_BG      = 0xFF878FA5; // PAL_BG[6]
    private static final int AE_TITLE_HI      = 0xFFCBCCD4; // PAL_BG[1]
    private static final int AE_BORDER_LIGHT  = 0xFFF2F2F2; // PAL_BG[3]
    private static final int AE_BORDER_DARK   = 0xFF413F54; // PAL_BG[4] — signature AE
    private static final int AE_TITLE_TEXT    = 0xFF2A2838; // eye-tune (sombre sur lavande)
    private static final int AE_WELL_BG       = 0xFF2B2A38; // eye-tune (puits sombre AE)
    private static final int AE_WELL_LIGHT    = 0xFFADB0C4; // PAL_BG[0]
    private static final int AE_WELL_DARK     = 0xFF413F54; // PAL_BG[4]
    private static final int AE_ROW_A         = 0xFF5A5F74; // eye-tune
    private static final int AE_ROW_B         = 0xFF525668; // eye-tune
    private static final int AE_SCROLL_TRACK  = 0xFF413F54; // PAL_BG[4]
    private static final int AE_SCROLL_THUMB  = 0xFFADB0C4; // PAL_BG[0]
    private static final int AE_SEPARATOR     = 0xFF696D88; // PAL_BG[5]
    private static final int AE_HANDLE        = 0xFF696D88; // PAL_BG[5]
    private static final int AE_HANDLE_HI     = 0xFF9A9FB4; // PAL_BG[2]
    private static final int AE_REQ_BG        = 0xFF413F54; // PAL_BG[4]
    private static final int AE_REQ_LIGHT     = 0xFF878FA5; // PAL_BG[6]
    private static final int AE_REQ_DARK      = 0xFF2A2838; // eye-tune
    private static final int AE_BTN_LIGHT     = 0xFFF2F2F2; // PAL_BG[3]
    private static final int AE_BTN_DARK      = 0xFF413F54; // PAL_BG[4]
    private static final int AE_NEUTRAL_BG    = 0xFF696D88; // PAL_BG[5]
    private static final int AE_NEUTRAL_BG_HI = 0xFF878FA5; // PAL_BG[6]
    private static final int AE_TAB_INACT_BG    = 0xFF696D88; // PAL_BG[5]
    private static final int AE_TAB_INACT_LIGHT = 0xFF878FA5; // PAL_BG[6]
    private static final int AE_TAB_INACT_DARK  = 0xFF413F54; // PAL_BG[4]
    private static final int AE_TAB_ACTIVE_BG   = 0xFFCBCCD4; // PAL_BG[1]
    private static final int AE_ICON_NEUTRAL    = 0xFFCBCCD4; // PAL_BG[1]
    private static final int AE_ICON_DIM        = 0xFF9A9FB4; // PAL_BG[2]

    // ── Cadre AE fin — séquences EXACTES extraites de TerminalSkin.BG (décodé
    // pixel-perfect sur 6 positions propres, hors slot central x=202..217). Ordre
    // extérieur → intérieur. Épaisseurs : haut/gauche/droit = 2px, bas = 4px.
    // Haut/gauche/droit sont IDENTIQUES : 0xFF413F54 → 0xFFF2F2F2. Le 0xCBCCD4 n'y
    // figure PAS (c'est le champ intérieur, géré par la palette de contenu). Les
    // coins restent nets (l'anti-aliasing alpha de la skin n'est pas reproductible
    // proprement en g.fill) — limite connue.
    private static final int[] AE_FRAME_TOP    = { 0xFF413F54, 0xFFF2F2F2 };
    private static final int[] AE_FRAME_LEFT   = { 0xFF413F54, 0xFFF2F2F2 };
    private static final int[] AE_FRAME_RIGHT  = { 0xFF413F54, 0xFFF2F2F2 };
    private static final int[] AE_FRAME_BOTTOM = { 0xFF413F54, 0xFF878FA5, 0xFF878FA5, 0xFFF2F2F2 };

    // ── Defaults structurels du thème DEFAULT (valeurs historiques du Clipboard) ─
    // Exposés via accesseurs pour que le rendu Défaut reste identique au bit près.
    private static final int DEF_TITLE_TEXT    = 0x404040;
    private static final int DEF_WELL_BG       = 0xFF3A3A3A;
    private static final int DEF_LIST_BG       = 0xFF373737;
    private static final int DEF_WELL_LIGHT    = 0xFF8B8B8B;
    private static final int DEF_WELL_DARK     = 0xFF373737;
    private static final int DEF_ROW_A         = 0xFF4A4A4A;
    private static final int DEF_ROW_B         = 0xFF424242;
    private static final int DEF_SCROLL_TRACK  = 0xFF373737;
    private static final int DEF_SCROLL_THUMB  = 0xFF8B8B8B;
    private static final int DEF_SEPARATOR     = 0xFF555555;
    private static final int DEF_HANDLE        = 0xFF888888;
    private static final int DEF_HANDLE_HI     = 0xFFCCCCCC;
    private static final int DEF_REQ_BG        = 0xFF2E2E4A;
    private static final int DEF_REQ_LIGHT     = 0xFF6666AA;
    private static final int DEF_REQ_DARK      = 0xFF1A1A3A;
    private static final int DEF_BTN_LIGHT     = 0xFFFFFFFF;
    private static final int DEF_BTN_DARK      = 0xFF373737;
    private static final int DEF_NEUTRAL_BG    = 0xFF404060;
    private static final int DEF_NEUTRAL_BG_HI = 0xFF505070;
    private static final int DEF_TAB_INACT_BG    = 0xFF4A4A4A;
    private static final int DEF_TAB_INACT_HOVER = 0xFF555555;
    private static final int DEF_TAB_INACT_LIGHT = 0xFF6B6B6B;
    private static final int DEF_TAB_INACT_DARK  = 0xFF222222;
    private static final int DEF_ICON_NEUTRAL    = 0xFFE0E0E0;
    private static final int DEF_ICON_DIM        = 0xFF888888;

    private boolean ae() { return theme == Theme.AE; }

    /** Vrai si le thème AE est actif — pour quelques widgets bespoke du Screen. */
    public boolean isAe() { return ae(); }

    // ── Helpers couleur ───────────────────────────────────────────────────────

    /** Background alpha factor (0..1): the raw slider opacity, 10%-100%. */
    public float alphaBackground() { return opacity; }

    /** Control alpha factor: same slider, floored so buttons/tabs stay visible. */
    public float alphaControl() { return Math.max(opacity, MIN_CONTROL_OPACITY); }

    /** Scales an ARGB's alpha byte by the given 0..1 factor. */
    private static int applyAlpha(int argb, float factor)
    {
        int a  = (argb >> 24) & 0xFF;
        int a2 = (int)(a * factor);
        return (a2 << 24) | (argb & 0x00FFFFFF);
    }

    /**
     * Applique l'opacité configurée sur une couleur ARGB (catégorie fond, opacité pleine).
     * Conservée pour les appelants existants ; équivalente à {@link #applyBackground(int)}.
     */
    public int applyOpacity(int argb) { return applyAlpha(argb, alphaBackground()); }

    /** Background-category alpha (full opacity). */
    public int applyBackground(int argb) { return applyAlpha(argb, alphaBackground()); }

    /** Control-category alpha (floored at MIN_CONTROL_OPACITY). */
    public int applyControl(int argb) { return applyAlpha(argb, alphaControl()); }

    // ── Chrome principal (thème-conscient, opacité appliquée) ──────────────────

    /** Fond du GUI — thémé, avec opacité. */
    public int bg()    { return applyOpacity(ae() ? AE_FRAME_BG   : bgColor); }

    /** Barre de titre — thémée, avec opacité. */
    public int title() { return applyOpacity(ae() ? AE_TITLE_BG   : titleColor); }

    /** Bordure claire (haut/gauche) — thémée, avec opacité. */
    public int border() { return applyOpacity(ae() ? AE_BORDER_LIGHT : borderColor); }

    /** Couleur "shadow" des bordures (côté sombre). En AE : valeur fixe de la palette. */
    public int borderShadow()
    {
        if (ae()) return applyOpacity(AE_BORDER_DARK);
        int c = applyOpacity(borderColor);
        int r = Math.max(0, ((c >> 16) & 0xFF) / 2);
        int gv = Math.max(0, ((c >> 8)  & 0xFF) / 2);
        int b2 = Math.max(0, ( c        & 0xFF) / 2);
        int a  = (c >> 24) & 0xFF;
        return (a << 24) | (r << 16) | (gv << 8) | b2;
    }

    /**
     * Épaisseur de bordure effective. En AE, le cadre définit sa propre géométrie
     * (pixel-perfect) → largeur fixe indépendante du slider (verrouillé en config).
     */
    public int frameBorderWidth() { return ae() ? 2 : borderWidth; }

    // ── Séquences de liserés du cadre AE (extérieur → intérieur, lecture seule) ─
    public int[] aeFrameTop()    { return AE_FRAME_TOP; }
    public int[] aeFrameLeft()   { return AE_FRAME_LEFT; }
    public int[] aeFrameRight()  { return AE_FRAME_RIGHT; }
    public int[] aeFrameBottom() { return AE_FRAME_BOTTOM; }

    // ── Accesseurs palette structurelle ────────────────────────────────────────
    // Renvoient la couleur BRUTE thème-correcte (sans opacité). Les appelants
    // (ColonyLinkScreen) enveloppent avec applyOpacity() là où le code d'origine
    // le faisait, préservant le rendu Défaut à l'identique.

    /** Liseré clair en haut de la barre de titre (raw). */
    public int titleHi()      { return ae() ? AE_TITLE_HI   : lighten(titleColor, 1.3f); }
    /** Couleur du texte de titre (raw). */
    public int titleText()    { return ae() ? AE_TITLE_TEXT : DEF_TITLE_TEXT; }

    /** Onglet actif — fond (raw). Défaut = bgColor éclairci. */
    public int tabActiveBg()  { return ae() ? AE_TAB_ACTIVE_BG : (lighten(bgColor, 1.1f) | 0xFF000000); }

    public int wellBg()       { return ae() ? AE_WELL_BG      : DEF_WELL_BG; }
    /** Fond du panneau liste (0xFF373737 en Défaut ≠ wellBg 0xFF3A3A3A). */
    public int listBg()       { return ae() ? AE_WELL_BG      : DEF_LIST_BG; }
    public int wellLight()    { return ae() ? AE_WELL_LIGHT   : DEF_WELL_LIGHT; }
    public int wellDark()     { return ae() ? AE_WELL_DARK    : DEF_WELL_DARK; }

    public int rowA()         { return ae() ? AE_ROW_A        : DEF_ROW_A; }
    public int rowB()         { return ae() ? AE_ROW_B        : DEF_ROW_B; }

    public int scrollTrack()  { return ae() ? AE_SCROLL_TRACK : DEF_SCROLL_TRACK; }
    public int scrollThumb()  { return ae() ? AE_SCROLL_THUMB : DEF_SCROLL_THUMB; }

    public int separator()    { return ae() ? AE_SEPARATOR    : DEF_SEPARATOR; }

    /** Points de la poignée de drag (raw). {@code hover} → variante claire. */
    public int handleDot(boolean hover)
    { return hover ? (ae() ? AE_HANDLE_HI : DEF_HANDLE_HI) : (ae() ? AE_HANDLE : DEF_HANDLE); }

    public int reqBg()        { return ae() ? AE_REQ_BG    : DEF_REQ_BG; }
    public int reqLight()     { return ae() ? AE_REQ_LIGHT : DEF_REQ_LIGHT; }
    public int reqDark()      { return ae() ? AE_REQ_DARK  : DEF_REQ_DARK; }

    /** Bevel clair/sombre des boutons (chrome, pas la face sémantique). */
    public int btnBevelLight() { return ae() ? AE_BTN_LIGHT : DEF_BTN_LIGHT; }
    public int btnBevelDark()  { return ae() ? AE_BTN_DARK  : DEF_BTN_DARK; }

    /** Fond des boutons neutres (config, etc.). {@code hover} → variante claire. */
    public int neutralBtnBg(boolean hover)
    { return hover ? (ae() ? AE_NEUTRAL_BG_HI : DEF_NEUTRAL_BG_HI) : (ae() ? AE_NEUTRAL_BG : DEF_NEUTRAL_BG); }

    public int tabInactiveBg()    { return ae() ? AE_TAB_INACT_BG    : DEF_TAB_INACT_BG; }
    /** Onglet inactif avec état hover (onglet Citizens). */
    public int tabInactiveBg(boolean hover)
    { return hover ? (ae() ? AE_TAB_INACT_LIGHT : DEF_TAB_INACT_HOVER) : (ae() ? AE_TAB_INACT_BG : DEF_TAB_INACT_BG); }
    public int tabInactiveLight() { return ae() ? AE_TAB_INACT_LIGHT : DEF_TAB_INACT_LIGHT; }
    public int tabInactiveDark()  { return ae() ? AE_TAB_INACT_DARK  : DEF_TAB_INACT_DARK; }

    /** Icônes neutres (engrenage/bonhomme/config). {@code dim} → variante atténuée. */
    public int iconNeutral(boolean dim)
    { return dim ? (ae() ? AE_ICON_DIM : DEF_ICON_DIM) : (ae() ? AE_ICON_NEUTRAL : DEF_ICON_NEUTRAL); }

    /** Éclaircit une couleur ARGB par un facteur (helper local, cf. ColonyLinkScreen). */
    private static int lighten(int argb, float f)
    {
        int a  = (argb >> 24) & 0xFF;
        int r  = Math.min(255, (int)(((argb >> 16) & 0xFF) * f));
        int gv = Math.min(255, (int)(((argb >> 8)  & 0xFF) * f));
        int b  = Math.min(255, (int)(( argb        & 0xFF) * f));
        return (a << 24) | (r << 16) | (gv << 8) | b;
    }

    /**
     * Dessine les bordures du GUI selon la config/thème.
     * Remplace les 4 fills hardcodés (clair haut/gauche, sombre bas/droite).
     * En AE, {@link #border()}/{@link #borderShadow()}/{@link #frameBorderWidth()}
     * renvoient déjà les valeurs de la palette → cadre AE sans changer ce code.
     */
    public void drawBorders(net.minecraft.client.gui.GuiGraphics g, int x, int y, int w, int h)
    {
        int light  = border();
        int shadow = borderShadow();
        int bw     = frameBorderWidth();

        // Haut
        g.fill(x, y, x + w, y + bw, light);
        // Gauche
        g.fill(x, y, x + bw, y + h, light);
        // Bas
        g.fill(x, y + h - bw, x + w, y + h, shadow);
        // Droite
        g.fill(x + w - bw, y, x + w, y + h, shadow);
    }

    // ── Reset ─────────────────────────────────────────────────────────────────

    public void reset()
    {
        bgColor     = DEFAULT_BG_COLOR;
        titleColor  = DEFAULT_TITLE_COLOR;
        borderColor = DEFAULT_BORDER_COLOR;
        borderWidth = DEFAULT_BORDER_WIDTH;
        opacity     = DEFAULT_OPACITY;
        scale       = DEFAULT_SCALE;
    }
}