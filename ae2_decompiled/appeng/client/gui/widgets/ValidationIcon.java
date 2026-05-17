/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.ComponentPath
 *  net.minecraft.client.gui.navigation.FocusNavigationEvent
 *  net.minecraft.network.chat.Component
 *  org.jetbrains.annotations.Nullable
 */
package appeng.client.gui.widgets;

import appeng.client.gui.Icon;
import appeng.client.gui.widgets.IconButton;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class ValidationIcon
extends IconButton {
    private final List<Component> tooltip = new ArrayList<Component>();

    public ValidationIcon() {
        super(btn -> {});
        this.setDisableBackground(true);
        this.setDisableClickSound(true);
        this.setHalfSize(true);
    }

    public void setValid(boolean valid) {
        this.setVisibility(!valid);
        if (valid) {
            this.tooltip.clear();
        }
    }

    @Override
    public List<Component> getTooltipMessage() {
        return this.tooltip;
    }

    public void setTooltip(List<Component> lines) {
        this.tooltip.clear();
        this.tooltip.addAll(lines);
    }

    @Override
    protected Icon getIcon() {
        return Icon.INVALID;
    }

    @Nullable
    public ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
        return null;
    }
}

