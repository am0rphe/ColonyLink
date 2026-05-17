/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.AbstractWidget
 *  net.minecraft.client.gui.narration.NarrationElementOutput
 *  net.minecraft.client.renderer.Rect2i
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 */
package appeng.client.gui.widgets;

import appeng.client.gui.style.Blitter;
import appeng.client.gui.widgets.ITooltip;
import appeng.core.localization.GuiText;
import appeng.menu.interfaces.IProgressProvider;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ProgressBar
extends AbstractWidget
implements ITooltip {
    private final IProgressProvider source;
    private final Blitter blitter;
    private final Direction layout;
    private final Rect2i sourceRect;
    private final Component titleName;
    private Component fullMsg;

    public ProgressBar(IProgressProvider source, Blitter blitter, Direction dir) {
        this(source, blitter, dir, null);
    }

    public ProgressBar(IProgressProvider source, Blitter blitter, Direction dir, Component title) {
        super(0, 0, blitter.getSrcWidth(), blitter.getSrcHeight(), (Component)Component.empty());
        this.source = source;
        this.blitter = blitter.copy();
        this.layout = dir;
        this.titleName = title;
        this.sourceRect = new Rect2i(blitter.getSrcX(), blitter.getSrcY(), blitter.getSrcWidth(), blitter.getSrcHeight());
    }

    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            int max = this.source.getMaxProgress();
            int current = this.source.getCurrentProgress();
            if (current > max) {
                current = max;
            }
            int srcX = this.sourceRect.getX();
            int srcY = this.sourceRect.getY();
            int srcW = this.sourceRect.getWidth();
            int srcH = this.sourceRect.getHeight();
            int destX = this.getX();
            int destY = this.getY();
            if (this.layout == Direction.VERTICAL) {
                int diff = this.height - (max > 0 ? this.height * current / max : 0);
                destY += diff;
                srcY += diff;
                srcH -= diff;
            } else {
                int diff = this.width - (max > 0 ? this.width * current / max : 0);
                srcX += diff;
                srcW -= diff;
            }
            this.blitter.src(srcX, srcY, srcW, srcH).dest(destX, destY).blit(guiGraphics);
        }
    }

    public void setFullMsg(Component msg) {
        this.fullMsg = msg;
    }

    @Override
    public List<Component> getTooltipMessage() {
        if (this.fullMsg != null) {
            return Collections.singletonList(this.fullMsg);
        }
        MutableComponent result = this.titleName != null ? this.titleName : Component.empty();
        return Arrays.asList(result, Component.literal((String)(this.source.getCurrentProgress() + " ")).append((Component)GuiText.Of.text().copy().append(" " + this.source.getMaxProgress())));
    }

    @Override
    public Rect2i getTooltipArea() {
        return new Rect2i(this.getX() - 2, this.getY() - 2, this.width + 4, this.height + 4);
    }

    @Override
    public boolean isTooltipAreaVisible() {
        return true;
    }

    public void updateWidgetNarration(NarrationElementOutput output) {
    }

    public static enum Direction {
        HORIZONTAL,
        VERTICAL;

    }
}

