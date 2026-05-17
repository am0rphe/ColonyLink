/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  me.shedaniel.math.Rectangle
 *  me.shedaniel.rei.api.client.gui.Renderer
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.resources.ResourceLocation
 */
package appeng.integration.modules.rei;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public record ProgressBarRenderer(ResourceLocation location, int x, int y, int width, int height, int u, int v) implements Renderer
{
    private static final int ANIMATION_TIME = 2000;

    public void render(GuiGraphics graphics, Rectangle bounds, int mouseX, int mouseY, float delta) {
        int subTime = (int)(System.currentTimeMillis() % 2000L);
        subTime = 2000 - subTime;
        int my = this.y + this.height * subTime / 2000;
        int mv = this.v + this.height * subTime / 2000;
        int mHeight = this.height - (my - this.y);
        graphics.blit(this.location, this.x, my, this.width, mHeight, (float)this.u, (float)mv, this.width, mHeight, 256, 256);
    }
}

