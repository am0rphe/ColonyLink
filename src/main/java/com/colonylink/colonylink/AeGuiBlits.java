package com.colonylink.colonylink;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * AeGuiBlits — shared client-only rendering primitives for AE2-themed screens.
 *
 * <p>UI passe 1: extracted VERBATIM from {@link ColonyLinkScreen} (which now
 * delegates through thin private facades — zero visual change to the Clipboard),
 * so a second screen (the Redirector) reuses the exact same primitives. Adds two
 * helpers new to this chantier: a generic states.png sprite blit (slot wells) and
 * the AE2 upgrade-panel cell (the overflowing side box).
 *
 * <p>Legal frame (validated on the Clipboard): AE2 assets are CC BY-NC-SA and are
 * referenced by ResourceLocation AT RUNTIME ONLY — never copied into this repo.
 * AE2 is a required dependency so they are guaranteed present, but internal asset
 * paths are not API: callers probe once at init() ({@link #probeTexture}) and fall
 * back to a procedural painter — never a magenta missing-texture.
 */
@OnlyIn(Dist.CLIENT)
public final class AeGuiBlits
{
    private AeGuiBlits() {}

    // ── AE2 text palette (published values from ae2:screens/common/palette.json) ──
    /** AE2 DEFAULT_TEXT — normal text on the light AE body. */
    public static final int AE_TEXT  = 0xFF413F54;
    /** AE2 MUTED_TEXT_COLOR — secondary / muted text. */
    public static final int AE_MUTED = 0xFF878FA5;

    // ── AE2 frame + light body (nine-slice source) ────────────────────────────
    public static final ResourceLocation AE_BACKGROUND =
            ResourceLocation.fromNamespaceAndPath("ae2", "textures/guis/background.png");
    public static final int AE_BG_BORDER = 4;    // nine-slice border, px
    public static final int AE_BG_TEX    = 256;  // background.png is 256x256
    public static final int AE_BG_TILE   = 248;  // source edge/center band = 256 - 2*4

    // ── AE2 button sprites (atlas ids + probe file) ───────────────────────────
    public static final ResourceLocation AE_BTN_NORMAL   = ResourceLocation.fromNamespaceAndPath("ae2", "button");
    public static final ResourceLocation AE_BTN_HOVER    = ResourceLocation.fromNamespaceAndPath("ae2", "button_highlighted");
    public static final ResourceLocation AE_BTN_DISABLED = ResourceLocation.fromNamespaceAndPath("ae2", "button_disabled");
    public static final ResourceLocation AE_BTN_PROBE    = ResourceLocation.fromNamespaceAndPath("ae2", "textures/gui/sprites/button.png");

    // ── AE2 states.png (generic GUI sprites — slot wells, tabs, icons) ────────
    public static final ResourceLocation AE_STATES =
            ResourceLocation.fromNamespaceAndPath("ae2", "textures/guis/states.png");
    public static final int AE_STATES_TEX = 256;  // states.png is 256x256
    /** AE2 Icon.SLOT_BACKGROUND — the standard 18x18 slot well inside states.png. */
    public static final int SLOT_BG_U = 192;
    public static final int SLOT_BG_V = 192;
    public static final int SLOT_BG_SIZE = 18;

    // ── AE2 extra_panels.png (upgrade side-box cells) ─────────────────────────
    public static final ResourceLocation AE_EXTRA_PANELS =
            ResourceLocation.fromNamespaceAndPath("ae2", "textures/guis/extra_panels.png");
    public static final int AE_EXTRA_PANELS_TEX = 128;  // extra_panels.png is 128x128 (measured)
    /** AE2's src rect for one all-borders cell: src(0,0,28,30) — matches UpgradesPanel.drawSlot
     *  (18 + 5 left/top + 5 right, +7 bottom). NOTE (measured from the real PNG): the cell's
     *  OPAQUE content is only 23x30 (texel x=0..22); the right 5px of the 28-wide src are
     *  transparent. The left side has NO border (it merges into the host panel's right frame). */
    public static final int UPGRADE_CELL_W = 28;
    public static final int UPGRADE_CELL_H = 30;
    /** Offset (measured) of the 16x16 item area inside the cell src rect: the well interior sits
     *  at texel (1,6), NOT (6,6) — this cell has no 5px left border. Anchor the box at
     *  (slotX - ITEM_DX, slotY - ITEM_DY) so the well lands exactly on the slot. */
    public static final int UPGRADE_CELL_ITEM_DX = 1;
    public static final int UPGRADE_CELL_ITEM_DY = 6;

    /** Visual state of an AE nine-slice button background. */
    public enum AeBtnVis { NORMAL, HOVER, DISABLED }

    /**
     * Probes a texture's presence ONCE (call from a screen's init(), never per
     * frame). Internal AE2/MineColonies asset paths are not API — probe + silent
     * procedural fallback is the validated pattern.
     */
    public static boolean probeTexture(ResourceLocation tex)
    {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.getResourceManager().getResource(tex).isPresent();
    }

    /**
     * Tinted texture blit that actually honours alpha. GuiGraphics' raw-ResourceLocation
     * blit routes to a no-blend, no-colour innerBlit (POSITION_TEX shader), so setColor's
     * alpha is written but never composited — the blit stays opaque. We reproduce MC's own
     * tinted-blit discipline (enableBlend -> shader colour -> blit -> disableBlend, cf. the
     * per-vertex-colour GuiGraphics.innerBlit) around the raw blit. Blend + shader colour are
     * reset on exit so no later render is tinted.
     */
    public static void blitTinted(GuiGraphics g, ResourceLocation tex, int x, int y,
                                  int u, int v, int w, int h, int texW, int texH, float alpha)
    {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        g.setColor(1f, 1f, 1f, alpha);
        g.blit(tex, x, y, u, v, w, h, texW, texH);
        g.setColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
    }

    /**
     * Like {@link #blitTinted}, but STRETCHES a source region (sw×sh at u,v within a
     * texW×texH texture) to the destination rect (w×h). Honours opacity/blend.
     */
    public static void blitTintedStretched(GuiGraphics g, ResourceLocation tex, int x, int y,
                                           int w, int h, float u, float v, int sw, int sh,
                                           int texW, int texH, float alpha)
    {
        blitTintedStretched(g, tex, x, y, w, h, u, v, sw, sh, texW, texH, 1f, alpha);
    }

    /**
     * As above, but multiplies the texture by a grey {@code bright} factor (1.0 = unchanged,
     * &lt;1.0 = darker). Used e.g. for hover states of textured buttons that have no hover
     * sprite. Blend + tint are reset on all paths.
     */
    public static void blitTintedStretched(GuiGraphics g, ResourceLocation tex, int x, int y,
                                           int w, int h, float u, float v, int sw, int sh,
                                           int texW, int texH, float bright, float alpha)
    {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        g.setColor(bright, bright, bright, alpha);
        g.blit(tex, x, y, w, h, u, v, sw, sh, texW, texH);
        g.setColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
    }

    /**
     * AE2 frame drawn by blitting AE2's own background.png as a nine-slice,
     * re-implemented with GuiGraphics (AE2's BackgroundGenerator/Blitter are
     * internal, unpublished API and are NOT called). BORDER=4px corners at native
     * size; edges tiled 1:1 in {@code AE_BG_TILE}-wide chunks (no scaling, so a
     * non-integer GUI scale magnifies with nearest-neighbour, not blur).
     *
     * When {@code withCenter} is true the tiled texture CENTRE is also painted,
     * filling the inner region with AE2's light terminal body, drawn before any
     * content so it sits underneath panels/text/buttons. When false, only the
     * border RING is drawn, leaving the body untouched.
     *
     * {@code alpha} is the effective opacity (config opacity multiplied over the
     * texture's own alpha). The tint is reset to opaque on every exit path.
     */
    public static void drawAeNineSlice(GuiGraphics g, int x, int y, int w, int h, float alpha, boolean withCenter)
    {
        // Degenerate: no room for a border ring on both sides — draw nothing.
        if (w < AE_BG_BORDER * 2 || h < AE_BG_BORDER * 2) return;

        final int b = AE_BG_BORDER;
        final int innerW = w - b * 2;
        final int innerH = h - b * 2;

        // Light body: tile the texture centre (src 4,4,248,248) across the inner
        // region, drawn first so the border ring below stays crisp at the seam.
        if (withCenter)
        {
            for (int cy = 0; cy < innerH; cy += AE_BG_TILE)
            {
                int th = Math.min(AE_BG_TILE, innerH - cy);
                for (int cx = 0; cx < innerW; cx += AE_BG_TILE)
                {
                    int tw = Math.min(AE_BG_TILE, innerW - cx);
                    blitTinted(g, AE_BACKGROUND, x + b + cx, y + b + cy, b, b, tw, th, AE_BG_TEX, AE_BG_TEX, alpha);
                }
            }
        }

        // 4 corners, native 4x4.
        blitTinted(g, AE_BACKGROUND, x,         y,         0,           0,           b, b, AE_BG_TEX, AE_BG_TEX, alpha);
        blitTinted(g, AE_BACKGROUND, x + w - b, y,         AE_BG_TEX-b, 0,           b, b, AE_BG_TEX, AE_BG_TEX, alpha);
        blitTinted(g, AE_BACKGROUND, x,         y + h - b, 0,           AE_BG_TEX-b, b, b, AE_BG_TEX, AE_BG_TEX, alpha);
        blitTinted(g, AE_BACKGROUND, x + w - b, y + h - b, AE_BG_TEX-b, AE_BG_TEX-b, b, b, AE_BG_TEX, AE_BG_TEX, alpha);

        // Top / bottom edges, tiled 1:1 (source band is AE_BG_TILE px wide).
        for (int cx = 0; cx < innerW; cx += AE_BG_TILE)
        {
            int tw = Math.min(AE_BG_TILE, innerW - cx);
            blitTinted(g, AE_BACKGROUND, x + b + cx, y,         b, 0,           tw, b, AE_BG_TEX, AE_BG_TEX, alpha);
            blitTinted(g, AE_BACKGROUND, x + b + cx, y + h - b, b, AE_BG_TEX-b, tw, b, AE_BG_TEX, AE_BG_TEX, alpha);
        }
        // Left / right edges, tiled 1:1.
        for (int cy = 0; cy < innerH; cy += AE_BG_TILE)
        {
            int th = Math.min(AE_BG_TILE, innerH - cy);
            blitTinted(g, AE_BACKGROUND, x,         y + b + cy, 0,           b, b, th, AE_BG_TEX, AE_BG_TEX, alpha);
            blitTinted(g, AE_BACKGROUND, x + w - b, y + b + cy, AE_BG_TEX-b, b, b, th, AE_BG_TEX, AE_BG_TEX, alpha);
        }
    }

    /**
     * AE button background — neutral nine-slice sprite (button / _highlighted /
     * _disabled) stretched to any w/h via g.blitSprite (honours the .mcmeta nine-slice).
     * Honours the given alpha; blend + tint are reset on exit so nothing later is
     * tinted. Background only — the caller draws the label on top.
     */
    public static void drawAeButtonBg(GuiGraphics g, int x, int y, int w, int h, AeBtnVis vis, float alpha)
    {
        ResourceLocation sprite = switch (vis) {
            case HOVER    -> AE_BTN_HOVER;
            case DISABLED -> AE_BTN_DISABLED;
            case NORMAL   -> AE_BTN_NORMAL;
        };
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        g.setColor(1f, 1f, 1f, alpha);
        g.blitSprite(sprite, x, y, w, h);
        g.setColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
    }

    // ── New for the Redirector chantier ───────────────────────────────────────

    /**
     * Generic tinted blit of a sprite region from AE2's states.png (256x256) —
     * e.g. the standard slot well {@code SLOT_BG_*} at (slotX-1, slotY-1).
     */
    public static void blitStatesSprite(GuiGraphics g, int srcX, int srcY, int w, int h,
                                        int destX, int destY, float alpha)
    {
        blitTinted(g, AE_STATES, destX, destY, srcX, srcY, w, h, AE_STATES_TEX, AE_STATES_TEX, alpha);
    }

    /**
     * ONE empty upgrade-panel cell — faithful port of AE2's UpgradesPanel.drawSlot
     * (decompiled appeng/client/gui/widgets/UpgradesPanel.java:156-178) for the
     * single-cell case, where all four edges are box borders: one blit of the src
     * rect (0,0,28,30) from extra_panels.png. This blit is byte-for-byte what AE2
     * itself renders ({@code BACKGROUND.src(0,0,28,30).dest(...).blit()} with
     * {@code Blitter.texture("guis/extra_panels.png",128,128)}).
     *
     * <p>{@code x,y} = top-left of the rendered BOX. The 16x16 item area lands at
     * {@code (x + UPGRADE_CELL_ITEM_DX, y + UPGRADE_CELL_ITEM_DY)} = {@code (x+1, y+6)}
     * — MEASURED from the real PNG. This cell has NO 5px left border (the left edge is
     * meant to abut the host panel's right frame), so callers MUST anchor the box at
     * {@code slot - (ITEM_DX, ITEM_DY)}, not {@code slot - (6,6)}.
     *
     * <p>Deliberately NEVER blits Icon.BACKGROUND_UPGRADE (240,208,16x16) — the
     * ghost upgrade-card icon AE2 draws inside empty AppEngSlots. Our vanilla
     * SlotItemHandlers draw no icon either, so the cell renders as Fab specified:
     * an empty AE2 upgrade slot.
     */
    public static void drawUpgradeCell(GuiGraphics g, int x, int y, float alpha)
    {
        blitTinted(g, AE_EXTRA_PANELS, x, y, 0, 0, UPGRADE_CELL_W, UPGRADE_CELL_H,
                AE_EXTRA_PANELS_TEX, AE_EXTRA_PANELS_TEX, alpha);
    }
}
