/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText$StyledContentConsumer
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.chat.Style
 */
package appeng.client.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public final class Tooltip {
    private final List<Component> content;

    public Tooltip(List<Component> unprocessedLines) {
        this.content = new ArrayList<Component>(unprocessedLines.size());
        for (Component unprocessedLine : unprocessedLines) {
            Tooltip.splitLine(unprocessedLine, this.content);
        }
    }

    private static void splitLine(Component unprocessedLine, List<Component> lines) {
        LineSplittingVisitor visitor = new LineSplittingVisitor(lines);
        unprocessedLine.visit((FormattedText.StyledContentConsumer)visitor, Style.EMPTY);
        visitor.flush();
    }

    public Tooltip(Component ... content) {
        this(Arrays.asList(content));
    }

    public List<Component> getContent() {
        return this.content;
    }

    private static class LineSplittingVisitor
    implements FormattedText.StyledContentConsumer<Object> {
        private final List<Component> lines;
        private MutableComponent currentPart;

        public LineSplittingVisitor(List<Component> lines) {
            this.lines = lines;
        }

        public Optional<Object> accept(Style style, String text) {
            String[] parts = text.split("\n", -1);
            for (int i = 0; i < parts.length; ++i) {
                if (i > 0) {
                    this.flush();
                }
                String line = parts[i];
                MutableComponent part = Component.literal((String)line).setStyle(style);
                this.currentPart = this.currentPart != null ? this.currentPart.append((Component)part) : part;
            }
            return Optional.empty();
        }

        public void flush() {
            if (this.currentPart != null) {
                if (this.currentPart.getStyle() == Style.EMPTY) {
                    if (this.lines.isEmpty()) {
                        this.currentPart.setStyle(Style.EMPTY.applyFormat(ChatFormatting.WHITE));
                    } else {
                        this.currentPart.setStyle(Style.EMPTY.applyFormat(ChatFormatting.GRAY));
                    }
                }
                this.lines.add((Component)this.currentPart);
                this.currentPart = null;
            }
        }
    }
}

