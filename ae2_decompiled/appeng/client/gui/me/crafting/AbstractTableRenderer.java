/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.renderer.Rect2i
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 */
package appeng.client.gui.me.crafting;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.StackWithBounds;
import appeng.client.gui.style.PaletteColor;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

public abstract class AbstractTableRenderer<T> {
    private static final int CELL_WIDTH = 67;
    private static final int CELL_HEIGHT = 22;
    private static final int COLS = 3;
    private static final int CELL_BORDER = 1;
    private static final int LINE_SPACING = 1;
    private static final float TEXT_SCALE = 0.5f;
    private static final float INV_TEXT_SCALE = 2.0f;
    protected final AEBaseScreen<?> screen;
    private final Font fontRenderer;
    private final float lineHeight;
    private final int x;
    private final int y;
    private final int rows;
    private StackWithBounds hoveredStack;

    public AbstractTableRenderer(AEBaseScreen<?> screen, int x, int y, int rows) {
        this.screen = screen;
        this.x = x;
        this.y = y;
        this.fontRenderer = Minecraft.getInstance().font;
        Objects.requireNonNull(this.fontRenderer);
        this.lineHeight = 9.0f * 0.5f;
        this.rows = rows;
    }

    public final void render(GuiGraphics guiGraphics, int mouseX, int mouseY, List<T> entries, int scrollOffset) {
        mouseX -= this.screen.getGuiLeft();
        mouseY -= this.screen.getGuiTop();
        int textColor = this.screen.getStyle().getColor(PaletteColor.DEFAULT_TEXT_COLOR).toARGB();
        List<Component> tooltipLines = null;
        StackWithBounds hovered = null;
        PoseStack pose = guiGraphics.pose();
        for (int row = 0; row < this.rows; ++row) {
            int i;
            for (int col = 0; col < 3 && (i = (row + scrollOffset) * 3 + col) < entries.size(); ++col) {
                T entry = entries.get(i);
                int cellX = this.x + col * 68;
                int cellY = this.y + row * 23;
                int background = this.getEntryBackgroundColor(entry);
                if (background != 0) {
                    guiGraphics.fill(cellX, cellY, cellX + 67, cellY + 22, background);
                }
                List<Component> lines = this.getEntryDescription(entry);
                float textHeight = (float)lines.size() * this.lineHeight;
                if (lines.size() > 1) {
                    textHeight += (float)((lines.size() - 1) * 1);
                }
                float textY = Math.round((float)cellY + (22.0f - textHeight) / 2.0f);
                int itemX = cellX + 67 - 19;
                pose.pushPose();
                pose.scale(0.5f, 0.5f, 1.0f);
                for (Component line : lines) {
                    int w = this.fontRenderer.width((FormattedText)line);
                    guiGraphics.drawString(this.fontRenderer, line, (int)(((float)(itemX - 2) - (float)w * 0.5f) * 2.0f), (int)(textY * 2.0f), textColor, false);
                    textY += this.lineHeight + 1.0f;
                }
                pose.popPose();
                AEKey entryStack = this.getEntryStack(entry);
                int itemY = cellY + 3;
                AEKeyRendering.drawInGui(Minecraft.getInstance(), guiGraphics, itemX, itemY, entryStack);
                int overlay = this.getEntryOverlayColor(entry);
                if (overlay != 0) {
                    guiGraphics.fill(cellX, cellY, cellX + 67, cellY + 22, overlay);
                }
                if (mouseX < cellX || mouseX > cellX + 67 || mouseY < cellY || mouseY > cellY + 22) continue;
                tooltipLines = this.getEntryTooltip(entry);
                hovered = new StackWithBounds(new GenericStack(entryStack, 0L), new Rect2i(this.screen.getGuiLeft() + cellX, this.screen.getGuiTop() + cellY, 67, 22));
            }
        }
        this.hoveredStack = hovered;
        if (tooltipLines != null) {
            this.screen.drawTooltipWithHeader(guiGraphics, mouseX, mouseY, tooltipLines);
        }
    }

    public StackWithBounds getHoveredStack() {
        return this.hoveredStack;
    }

    public int getScrollableRows(int size) {
        return AbstractTableRenderer.getScrollableRows(size, this.rows);
    }

    protected static int getScrollableRows(int size, int rows) {
        return (size + 3 - 1) / 3 - rows;
    }

    protected abstract List<Component> getEntryDescription(T var1);

    protected abstract AEKey getEntryStack(T var1);

    protected abstract List<Component> getEntryTooltip(T var1);

    protected int getEntryBackgroundColor(T entry) {
        return 0;
    }

    protected int getEntryOverlayColor(T entry) {
        return 0;
    }
}

