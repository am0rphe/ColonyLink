/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 */
package appeng.client.gui.me.search;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AEKey;
import appeng.core.AEConfig;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.util.Platform;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

final class TooltipsSearchPredicate
implements Predicate<GridInventoryEntry> {
    private final String tooltip;
    private final Map<AEKey, String> tooltipCache;

    public TooltipsSearchPredicate(String tooltip, Map<AEKey, String> tooltipCache) {
        this.tooltip = TooltipsSearchPredicate.normalize(tooltip.toLowerCase());
        this.tooltipCache = tooltipCache;
    }

    @Override
    public boolean test(GridInventoryEntry gridInventoryEntry) {
        AEKey entryInfo = Objects.requireNonNull(gridInventoryEntry.getWhat());
        String tooltipText = this.getTooltipText(entryInfo);
        return tooltipText.contains(this.tooltip);
    }

    private String getTooltipText(AEKey what) {
        return this.tooltipCache.computeIfAbsent(what, key -> {
            List<Component> lines = AEKeyRendering.getTooltip(key);
            StringBuilder tooltipText = new StringBuilder();
            for (int i = 0; i < lines.size(); ++i) {
                Component line = lines.get(i);
                if (i > 0 && i >= lines.size() - 1 && !AEConfig.instance().isSearchModNameInTooltips()) {
                    String text2 = line.getString();
                    boolean hadFormatting = false;
                    if (text2.indexOf(167) != -1) {
                        text2 = ChatFormatting.stripFormatting((String)text2);
                        hadFormatting = true;
                    } else {
                        boolean bl = hadFormatting = !line.getStyle().isEmpty();
                    }
                    if (hadFormatting && Objects.equals(text2, Platform.getModName(what.getModId()))) continue;
                    tooltipText.append('\n').append(text2);
                    continue;
                }
                if (i > 0) {
                    tooltipText.append('\n');
                }
                line.visit(text -> {
                    if (text.indexOf(167) != -1) {
                        text = ChatFormatting.stripFormatting((String)text);
                    }
                    tooltipText.append(text);
                    return Optional.empty();
                });
            }
            return TooltipsSearchPredicate.normalize(tooltipText.toString());
        });
    }

    private static String normalize(String input) {
        return input.toLowerCase().replace(" ", "");
    }
}

