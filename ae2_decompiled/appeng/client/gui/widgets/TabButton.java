/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.Button$OnPress
 *  net.minecraft.client.renderer.Rect2i
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.ItemStack
 */
package appeng.client.gui.widgets;

import appeng.client.gui.Icon;
import appeng.client.gui.widgets.ITooltip;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class TabButton
extends Button
implements ITooltip {
    private Style style = Style.BOX;
    private Icon icon = null;
    private ItemStack item;
    private boolean selected;
    private boolean disableBackground = false;

    public TabButton(Icon ico, Component message, Button.OnPress onPress) {
        super(0, 0, 22, 22, message, onPress, Button.DEFAULT_NARRATION);
        this.icon = ico;
    }

    public TabButton(ItemStack ico, Component message, Button.OnPress onPress) {
        super(0, 0, 22, 22, message, onPress, Button.DEFAULT_NARRATION);
        this.item = ico;
    }

    public void renderWidget(GuiGraphics guiGraphics, int x, int y, float partial) {
        if (this.visible) {
            int iconY;
            Icon backdrop;
            switch (this.style.ordinal()) {
                default: {
                    throw new MatchException(null, null);
                }
                case 0: {
                    Icon icon;
                    if (this.isFocused()) {
                        icon = Icon.TAB_BUTTON_BACKGROUND_BORDERLESS_FOCUS;
                        break;
                    }
                    icon = Icon.TAB_BUTTON_BACKGROUND_BORDERLESS;
                    break;
                }
                case 1: {
                    Icon icon;
                    if (this.isFocused()) {
                        icon = Icon.TAB_BUTTON_BACKGROUND_FOCUS;
                        break;
                    }
                    icon = Icon.TAB_BUTTON_BACKGROUND;
                    break;
                }
                case 2: {
                    Icon icon = this.isFocused() ? Icon.HORIZONTAL_TAB_FOCUS : (backdrop = this.selected ? Icon.HORIZONTAL_TAB_SELECTED : Icon.HORIZONTAL_TAB);
                }
            }
            if (!this.disableBackground) {
                backdrop.getBlitter().dest(this.getX(), this.getY()).blit(guiGraphics);
            }
            int iconX = switch (this.style.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> 1;
                case 1 -> 2;
                case 2 -> 3;
            };
            switch (this.style.ordinal()) {
                default: {
                    throw new MatchException(null, null);
                }
                case 0: {
                    int n = 1;
                    break;
                }
                case 1: {
                    int n = 2;
                    break;
                }
                case 2: {
                    int n = iconY = 3;
                }
            }
            if (this.icon != null) {
                this.icon.getBlitter().dest(this.getX() + iconX, this.getY() + iconY - 1).blit(guiGraphics);
            }
            if (this.item != null) {
                PoseStack pose = guiGraphics.pose();
                pose.pushPose();
                pose.translate(0.0f, -1.0f, 100.0f);
                guiGraphics.renderItem(this.item, this.getX() + iconX, this.getY() + iconY);
                Font font = Minecraft.getInstance().font;
                guiGraphics.renderItemDecorations(font, this.item, this.getX() + iconX, this.getY() + iconY);
                pose.popPose();
            }
        }
    }

    @Override
    public List<Component> getTooltipMessage() {
        return Collections.singletonList(this.getMessage());
    }

    @Override
    public Rect2i getTooltipArea() {
        return new Rect2i(this.getX(), this.getY(), this.width, this.height);
    }

    @Override
    public boolean isTooltipAreaVisible() {
        return this.visible;
    }

    public Style getStyle() {
        return this.style;
    }

    public void setStyle(Style style) {
        this.style = style;
    }

    public boolean isSelected() {
        return this.selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isDisableBackground() {
        return this.disableBackground;
    }

    public void setDisableBackground(boolean disableBackground) {
        this.disableBackground = disableBackground;
    }

    public static enum Style {
        CORNER,
        BOX,
        HORIZONTAL;

    }
}

