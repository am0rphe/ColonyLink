/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  org.jetbrains.annotations.Nullable
 */
package appeng.client.gui.widgets;

import appeng.api.config.ActionItems;
import appeng.client.gui.Icon;
import appeng.client.gui.widgets.IconButton;
import appeng.core.localization.ButtonToolTips;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class ActionButton
extends IconButton {
    private static final Pattern PATTERN_NEW_LINE = Pattern.compile("\\n", 16);
    private final Icon icon;

    public ActionButton(ActionItems action, Runnable onPress) {
        this(action, (ActionItems a) -> onPress.run());
    }

    public ActionButton(ActionItems action, Consumer<ActionItems> onPress) {
        super(btn -> onPress.accept(action));
        ButtonToolTips displayName;
        this.setMessage(this.buildMessage(displayName, switch (action) {
            case ActionItems.COG -> {
                this.icon = Icon.COG;
                displayName = ButtonToolTips.PartitionStorage;
                yield ButtonToolTips.PartitionStorageHint;
            }
            case ActionItems.CLOSE -> {
                this.icon = Icon.CLEAR;
                displayName = ButtonToolTips.Clear;
                yield ButtonToolTips.ClearSettings;
            }
            case ActionItems.S_CLOSE -> {
                this.icon = Icon.S_CLEAR;
                displayName = ButtonToolTips.Clear;
                yield ButtonToolTips.ClearSettings;
            }
            case ActionItems.STASH -> {
                this.icon = Icon.ARROW_UP;
                displayName = ButtonToolTips.Stash;
                yield ButtonToolTips.StashDesc;
            }
            case ActionItems.S_STASH -> {
                this.icon = Icon.S_ARROW_UP;
                displayName = ButtonToolTips.Stash;
                yield ButtonToolTips.StashDesc;
            }
            case ActionItems.STASH_TO_PLAYER_INV -> {
                this.icon = Icon.ARROW_DOWN;
                displayName = ButtonToolTips.StashToPlayer;
                yield ButtonToolTips.StashToPlayerDesc;
            }
            case ActionItems.S_STASH_TO_PLAYER_INV -> {
                this.icon = Icon.S_ARROW_DOWN;
                displayName = ButtonToolTips.StashToPlayer;
                yield ButtonToolTips.StashToPlayerDesc;
            }
            case ActionItems.ENCODE -> {
                this.icon = Icon.WHITE_ARROW_DOWN;
                displayName = ButtonToolTips.Encode;
                yield ButtonToolTips.EncodeDescription;
            }
            case ActionItems.CYCLE_PROCESSING_OUTPUT -> {
                this.icon = Icon.SCHEDULING_DEFAULT;
                displayName = ButtonToolTips.CycleProcessingOutput;
                yield ButtonToolTips.CycleProcessingOutputTooltip;
            }
            case ActionItems.S_CYCLE_PROCESSING_OUTPUT -> {
                this.icon = Icon.S_CYCLE;
                displayName = ButtonToolTips.CycleProcessingOutput;
                yield ButtonToolTips.CycleProcessingOutputTooltip;
            }
            case ActionItems.TERMINAL_SETTINGS -> {
                this.icon = Icon.COG;
                displayName = ButtonToolTips.TerminalSettings;
                yield null;
            }
            default -> throw new IllegalArgumentException("Unknown ActionItem: " + String.valueOf((Object)action));
        }));
    }

    @Override
    protected Icon getIcon() {
        return this.icon;
    }

    private Component buildMessage(ButtonToolTips displayName, @Nullable ButtonToolTips displayValue) {
        String name = displayName.text().getString();
        if (displayValue == null) {
            return Component.literal((String)name);
        }
        String value = displayValue.text().getString();
        StringBuilder sb = new StringBuilder(value = PATTERN_NEW_LINE.matcher(value).replaceAll("\n"));
        int i = sb.lastIndexOf("\n");
        if (i <= 0) {
            i = 0;
        }
        while (i + 30 < sb.length() && (i = sb.lastIndexOf(" ", i + 30)) != -1) {
            sb.replace(i, i + 1, "\n");
        }
        return Component.literal((String)(name + "\n" + String.valueOf(sb)));
    }
}

