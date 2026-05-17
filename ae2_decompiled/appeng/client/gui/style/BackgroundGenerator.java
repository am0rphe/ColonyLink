/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 */
package appeng.client.gui.style;

import appeng.client.gui.style.Blitter;
import net.minecraft.client.gui.GuiGraphics;

public final class BackgroundGenerator {
    private static final int BORDER = 4;
    private static final int SIZE = 256;
    private static final int TILED_SIZE = 248;
    private static final Blitter FULL = Blitter.texture("guis/background.png", 256, 256);
    private static final Blitter TOP_LEFT = FULL.copy().src(0, 0, 4, 4);
    private static final Blitter TOP_MIDDLE = FULL.copy().src(4, 0, 248, 4);
    private static final Blitter TOP_RIGHT = FULL.copy().src(252, 0, 4, 4);
    private static final Blitter LEFT = FULL.copy().src(0, 4, 4, 248);
    private static final Blitter MIDDLE = FULL.copy().src(4, 4, 248, 248);
    private static final Blitter RIGHT = FULL.copy().src(252, 4, 4, 248);
    private static final Blitter BOTTOM_LEFT = FULL.copy().src(0, 252, 4, 4);
    private static final Blitter BOTTOM_MIDDLE = FULL.copy().src(4, 252, 248, 4);
    private static final Blitter BOTTOM_RIGHT = FULL.copy().src(252, 252, 4, 4);

    private BackgroundGenerator() {
    }

    public static void draw(int width, int height, GuiGraphics guiGraphics, int x, int y) {
        if (width < 8 || height < 8) {
            return;
        }
        int right = x + width;
        int bottom = y + height;
        TOP_LEFT.dest(x, y).blit(guiGraphics);
        TOP_RIGHT.dest(right - 4, y).blit(guiGraphics);
        BOTTOM_LEFT.dest(x, bottom - 4).blit(guiGraphics);
        BOTTOM_RIGHT.dest(right - 4, bottom - 4).blit(guiGraphics);
        int innerWidth = width - 8;
        int innerHeight = height - 8;
        for (int cx = 0; cx < innerWidth; cx += 248) {
            int tileWidth = Math.min(248, innerWidth - cx);
            TOP_MIDDLE.copy().srcWidth(tileWidth).dest(x + 4 + cx, y).blit(guiGraphics);
            BOTTOM_MIDDLE.copy().srcWidth(tileWidth).dest(x + 4 + cx, y + height - 4).blit(guiGraphics);
            for (int cy = 0; cy < innerHeight; cy += 248) {
                int tileHeight = Math.min(248, innerHeight - cy);
                MIDDLE.copy().srcWidth(tileWidth).srcHeight(tileHeight).dest(x + 4 + cx, y + 4 + cy).blit(guiGraphics);
            }
        }
        for (int cy = 0; cy < innerHeight; cy += 248) {
            int tileHeight = Math.min(248, innerHeight - cy);
            LEFT.copy().srcHeight(tileHeight).dest(x, y + 4 + cy).blit(guiGraphics);
            RIGHT.copy().srcHeight(tileHeight).dest(right - 4, y + 4 + cy).blit(guiGraphics);
        }
    }
}

