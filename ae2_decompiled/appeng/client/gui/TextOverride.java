/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  org.jetbrains.annotations.Nullable
 */
package appeng.client.gui;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class TextOverride {
    @Nullable
    private Component content;
    private boolean hidden;

    @Nullable
    public Component getContent() {
        return this.content;
    }

    public void setContent(@Nullable Component content) {
        this.content = content;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
}

