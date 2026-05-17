/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  me.shedaniel.math.Rectangle
 *  me.shedaniel.rei.api.client.gui.Renderer
 *  net.minecraft.client.gui.GuiGraphics
 */
package appeng.integration.modules.rei;

import appeng.client.gui.style.BackgroundGenerator;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import net.minecraft.client.gui.GuiGraphics;

public record BackgroundRenderer(int width, int height) implements Renderer
{
    public void render(GuiGraphics graphics, Rectangle bounds, int mouseX, int mouseY, float delta) {
        BackgroundGenerator.draw(this.width, this.height, graphics, bounds.x, bounds.y);
    }
}

