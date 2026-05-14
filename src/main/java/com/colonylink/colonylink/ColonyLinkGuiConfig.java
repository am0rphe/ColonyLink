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
    // ── Defaults ──────────────────────────────────────────────────────────────
    public static final int   DEFAULT_BG_COLOR     = 0xFF8B8B8B;
    public static final int   DEFAULT_TITLE_COLOR  = 0xFF6B6B6B;
    public static final int   DEFAULT_BORDER_COLOR = 0xFFFFFFFF;
    public static final int   DEFAULT_BORDER_WIDTH = 2;
    public static final float DEFAULT_OPACITY      = 1.0f;
    public static final float DEFAULT_SCALE        = 0.9f;

    // ── Champs (sérialisés par Gson) ──────────────────────────────────────────
    public int   bgColor     = DEFAULT_BG_COLOR;
    public int   titleColor  = DEFAULT_TITLE_COLOR;
    public int   borderColor = DEFAULT_BORDER_COLOR;
    public int   borderWidth = DEFAULT_BORDER_WIDTH;
    public float opacity     = DEFAULT_OPACITY;
    public float scale       = DEFAULT_SCALE;

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

    // ── Helpers couleur ───────────────────────────────────────────────────────

    /**
     * Applique l'opacité configurée sur une couleur ARGB.
     * L'alpha de la couleur source est multiplié par l'opacité globale.
     */
    public int applyOpacity(int argb)
    {
        int a = (argb >> 24) & 0xFF;
        int a2 = (int)(a * opacity);
        return (a2 << 24) | (argb & 0x00FFFFFF);
    }

    /** Retourne bgColor avec opacité appliquée. */
    public int bg()    { return applyOpacity(bgColor); }

    /** Retourne titleColor avec opacité appliquée. */
    public int title() { return applyOpacity(titleColor); }

    /** Retourne borderColor avec opacité appliquée. */
    public int border() { return applyOpacity(borderColor); }

    /** Retourne la couleur "shadow" des bordures (côté sombre, 50% plus foncé). */
    public int borderShadow()
    {
        int c = border();
        int r = Math.max(0, ((c >> 16) & 0xFF) / 2);
        int gv = Math.max(0, ((c >> 8)  & 0xFF) / 2);
        int b2 = Math.max(0, ( c        & 0xFF) / 2);
        int a  = (c >> 24) & 0xFF;
        return (a << 24) | (r << 16) | (gv << 8) | b2;
    }

    /**
     * Dessine les bordures du GUI selon la config.
     * Remplace les 4 fills hardcodés (blanc haut/gauche, gris bas/droite).
     */
    public void drawBorders(net.minecraft.client.gui.GuiGraphics g, int x, int y, int w, int h)
    {
        int light  = border();
        int shadow = borderShadow();
        int bw     = borderWidth;

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