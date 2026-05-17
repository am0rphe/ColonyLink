/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  guideme.PageAnchor
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Inventory
 *  org.jetbrains.annotations.Nullable
 */
package appeng.client.gui.implementations;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.NumberEntryType;
import appeng.client.gui.implementations.AESubScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.NumberEntryWidget;
import appeng.menu.implementations.PriorityMenu;
import guideme.PageAnchor;
import java.util.OptionalInt;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.Nullable;

public class PriorityScreen
extends AEBaseScreen<PriorityMenu> {
    private final NumberEntryWidget priority;

    public PriorityScreen(PriorityMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        AESubScreen.addBackButton(menu, "back", this.widgets);
        this.priority = this.widgets.addNumberEntryWidget("priority", NumberEntryType.UNITLESS);
        this.priority.setTextFieldStyle(style.getWidget("priorityInput"));
        this.priority.setMinValue(Integer.MIN_VALUE);
        this.priority.setLongValue(((PriorityMenu)this.menu).getPriorityValue());
        this.priority.setOnChange(this::savePriority);
        this.priority.setOnConfirm(() -> {
            this.savePriority();
            AESubScreen.goBack();
        });
    }

    private void savePriority() {
        OptionalInt priority = this.priority.getIntValue();
        if (priority.isPresent()) {
            ((PriorityMenu)this.menu).setPriority(priority.getAsInt());
        }
    }

    @Override
    @Nullable
    protected PageAnchor getHelpTopic() {
        PageAnchor topic = super.getHelpTopic();
        return topic != null && topic.anchor() == null ? new PageAnchor(topic.pageId(), "priority") : null;
    }
}

