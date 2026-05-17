/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package appeng.menu.slot;

import appeng.client.Point;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public interface IOptionalSlot {
    default public boolean isRenderDisabled() {
        return false;
    }

    public boolean isSlotEnabled();

    @OnlyIn(value=Dist.CLIENT)
    public Point getBackgroundPos();
}

