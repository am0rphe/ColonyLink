/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package appeng.client.gui.widgets;

import appeng.client.gui.Icon;
import appeng.client.gui.widgets.ITooltip;
import appeng.client.gui.widgets.IconButton;
import java.util.Collections;
import java.util.List;
import net.minecraft.network.chat.Component;

public class ToggleButton
extends IconButton
implements ITooltip {
    private final Listener listener;
    private final Icon iconOn;
    private final Icon iconOff;
    private List<Component> tooltipOn = Collections.emptyList();
    private List<Component> tooltipOff = Collections.emptyList();
    private boolean state;

    public ToggleButton(Icon on, Icon off, Component displayName, Component displayHint, Listener listener) {
        this(on, off, listener);
        this.setTooltipOn(List.of(displayName, displayHint));
        this.setTooltipOff(List.of(displayName, displayHint));
    }

    public ToggleButton(Icon on, Icon off, Listener listener) {
        super(null);
        this.iconOn = on;
        this.iconOff = off;
        this.listener = listener;
    }

    public void setTooltipOn(List<Component> lines) {
        this.tooltipOn = lines;
    }

    public void setTooltipOff(List<Component> lines) {
        this.tooltipOff = lines;
    }

    public void onPress() {
        this.listener.onChange(!this.state);
    }

    public void setState(boolean isOn) {
        this.state = isOn;
    }

    @Override
    protected Icon getIcon() {
        return this.state ? this.iconOn : this.iconOff;
    }

    @Override
    public List<Component> getTooltipMessage() {
        return this.state ? this.tooltipOn : this.tooltipOff;
    }

    @Override
    public boolean isTooltipAreaVisible() {
        return super.isTooltipAreaVisible() && !this.getTooltipMessage().isEmpty();
    }

    @FunctionalInterface
    public static interface Listener {
        public void onChange(boolean var1);
    }
}

