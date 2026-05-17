/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.components.Button$OnPress
 *  net.minecraft.network.chat.Component
 */
package appeng.client.gui.widgets;

import appeng.client.gui.Icon;
import appeng.client.gui.widgets.IconButton;
import appeng.core.localization.ButtonToolTips;
import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class OpenGuideButton
extends IconButton {
    public OpenGuideButton(Button.OnPress onPress) {
        super(onPress);
    }

    @Override
    public List<Component> getTooltipMessage() {
        return List.of(ButtonToolTips.OpenGuide.text(), ButtonToolTips.OpenGuideDetail.text());
    }

    @Override
    protected Icon getIcon() {
        return Icon.HELP;
    }
}

