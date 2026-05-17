/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.Rect2i
 *  net.minecraft.network.chat.Component
 */
package appeng.client.gui.widgets;

import java.util.Collections;
import java.util.List;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;

public interface ITooltip {
    default public List<Component> getTooltipMessage() {
        return Collections.emptyList();
    }

    public Rect2i getTooltipArea();

    public boolean isTooltipAreaVisible();
}

