/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package appeng.client.gui.style;

import appeng.client.gui.layout.SlotGridLayout;
import appeng.client.gui.style.Position;
import org.jetbrains.annotations.Nullable;

public class SlotPosition
extends Position {
    @Nullable
    private SlotGridLayout grid;
    private boolean hidden = false;

    @Nullable
    public SlotGridLayout getGrid() {
        return this.grid;
    }

    public void setGrid(@Nullable SlotGridLayout grid) {
        this.grid = grid;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public void setHidden(@Nullable Boolean hidden) {
        this.hidden = Boolean.TRUE.equals(hidden);
    }

    @Override
    public String toString() {
        String result = super.toString();
        return this.grid != null ? result + "grid=" + String.valueOf((Object)this.grid) : result;
    }
}

