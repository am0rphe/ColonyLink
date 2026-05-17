/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.renderer.Rect2i
 *  net.minecraft.network.chat.Component
 *  org.jetbrains.annotations.Nullable
 */
package appeng.client.gui.widgets;

import appeng.client.Point;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.Tooltip;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.ScreenStyle;
import appeng.core.localization.GuiText;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class ToolboxPanel
implements ICompositeWidget {
    private final Blitter background;
    private final Component toolbeltName;
    private Rect2i bounds = new Rect2i(0, 0, 0, 0);

    public ToolboxPanel(ScreenStyle style, Component toolbeltName) {
        this.background = style.getImage("toolbox");
        this.toolbeltName = toolbeltName;
    }

    @Override
    public void setPosition(Point position) {
        this.bounds = new Rect2i(position.getX(), position.getY(), this.bounds.getWidth(), this.bounds.getHeight());
    }

    @Override
    public void setSize(int width, int height) {
        this.bounds = new Rect2i(this.bounds.getX(), this.bounds.getY(), width, height);
    }

    @Override
    public Rect2i getBounds() {
        return this.bounds;
    }

    @Override
    public void drawBackgroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
        this.background.dest(bounds.getX() + this.bounds.getX(), bounds.getY() + this.bounds.getY(), this.bounds.getWidth(), this.bounds.getHeight()).blit(guiGraphics);
    }

    @Override
    @Nullable
    public Tooltip getTooltip(int mouseX, int mouseY) {
        return new Tooltip(new Component[]{this.toolbeltName, GuiText.UpgradeToolbelt.text().plainCopy().withStyle(ChatFormatting.GRAY)});
    }
}

