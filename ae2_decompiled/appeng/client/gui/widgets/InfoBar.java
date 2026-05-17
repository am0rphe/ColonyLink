/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.world.level.ItemLike
 */
package appeng.client.gui.widgets;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.client.gui.Icon;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.level.ItemLike;

public class InfoBar {
    private final List<Widget> widgets = new ArrayList<Widget>();

    public void render(GuiGraphics guiGraphics, int x, int y) {
        int maxHeight = this.widgets.stream().mapToInt(Widget::getHeight).max().orElse(0);
        for (Widget widget : this.widgets) {
            widget.render(guiGraphics, x, Math.round((float)y + (float)maxHeight / 2.0f - (float)widget.getHeight() / 2.0f));
            x += widget.getWidth();
        }
    }

    void add(Icon icon, float scale, int xPos, int yPos) {
        this.widgets.add(new IconWidget(icon, scale, xPos, yPos));
    }

    void add(String text, int color, float scale, int xPos, int yPos) {
        this.widgets.add(new TextWidget((Component)Component.literal((String)text), color, scale, xPos, yPos));
    }

    void add(Component text, int color, float scale, int xPos, int yPos) {
        this.widgets.add(new TextWidget(text, color, scale, xPos, yPos));
    }

    void add(AEKey what, float scale, int xPos, int yPos) {
        this.widgets.add(new StackWidget(what, scale, xPos, yPos));
    }

    void add(ItemLike what, float scale, int xPos, int yPos) {
        this.widgets.add(new StackWidget(AEItemKey.of(what), scale, xPos, yPos));
    }

    void addSpace(int width) {
        this.widgets.add(new SpaceWidget(width));
    }

    static interface Widget {
        public int getWidth();

        public int getHeight();

        public void render(GuiGraphics var1, int var2, int var3);
    }

    private record IconWidget(Icon icon, float scale, int xPos, int yPos) implements Widget
    {
        @Override
        public int getWidth() {
            return Math.round(16.0f * this.scale);
        }

        @Override
        public int getHeight() {
            return Math.round(16.0f * this.scale);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int x, int y) {
            PoseStack poseStack = guiGraphics.pose();
            poseStack.pushPose();
            poseStack.translate((float)this.xPos, (float)this.yPos, 0.0f);
            poseStack.scale(this.scale, this.scale, 1.0f);
            this.icon.getBlitter().dest(0, 0).blit(guiGraphics);
            poseStack.popPose();
        }
    }

    private static final class TextWidget
    implements Widget {
        private final Component text;
        private final int color;
        private final float scale;
        private final int xPos;
        private final int yPos;
        private final int width;
        private final int height;

        public TextWidget(Component text, int color, float scale, int xPos, int yPos) {
            this.text = text;
            this.color = color;
            this.scale = scale;
            this.xPos = xPos;
            this.yPos = yPos;
            Font font = Minecraft.getInstance().font;
            this.width = Math.round((float)font.width((FormattedText)text) * scale);
            Objects.requireNonNull(font);
            this.height = Math.round(9.0f * scale);
        }

        @Override
        public int getWidth() {
            return this.width;
        }

        @Override
        public int getHeight() {
            return this.height;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int x, int y) {
            PoseStack poseStack = guiGraphics.pose();
            Font font = Minecraft.getInstance().font;
            poseStack.pushPose();
            poseStack.translate((float)this.xPos, (float)this.yPos, 0.0f);
            poseStack.scale(this.scale, this.scale, 1.0f);
            guiGraphics.drawString(font, this.text, 0, 0, this.color, false);
            poseStack.popPose();
        }
    }

    private record StackWidget(AEKey what, float scale, int xPos, int yPos) implements Widget
    {
        @Override
        public int getWidth() {
            return Math.round(16.0f * this.scale);
        }

        @Override
        public int getHeight() {
            return Math.round(16.0f * this.scale);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int x, int y) {
            PoseStack poseStack = guiGraphics.pose();
            poseStack.pushPose();
            poseStack.translate((float)this.xPos, (float)this.yPos, 0.0f);
            poseStack.scale(this.scale, this.scale, 1.0f);
            AEKeyRendering.drawInGui(Minecraft.getInstance(), guiGraphics, 0, 0, this.what);
            poseStack.popPose();
        }
    }

    private static final class SpaceWidget
    implements Widget {
        private final int width;

        public SpaceWidget(int width) {
            this.width = width;
        }

        @Override
        public int getWidth() {
            return this.width;
        }

        @Override
        public int getHeight() {
            return 0;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int x, int y) {
        }
    }
}

