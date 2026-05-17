/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.chat.Style
 *  net.minecraft.network.chat.TextColor
 *  net.minecraft.world.item.ItemStack
 *  org.jetbrains.annotations.NotNull
 */
package appeng.core.localization;

import appeng.api.behaviors.EmptyingAction;
import appeng.api.config.PowerUnit;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AmountFormat;
import appeng.api.stacks.GenericStack;
import appeng.core.AEConfig;
import appeng.core.localization.ButtonToolTips;
import appeng.core.localization.GuiText;
import appeng.core.localization.Side;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class Tooltips {
    private static final char SEP;
    public static final ChatFormatting MUTED_COLOR;
    public static final Style NORMAL_TOOLTIP_TEXT;
    public static final Style QUOTE_TEXT;
    public static final Style NUMBER_TEXT;
    public static final Style UNIT_TEXT;
    public static final Style RED;
    public static final Style GREEN;
    public static final String[] units;
    public static final long[] DECIMAL_NUMS;
    public static final long[] BYTE_NUMS;

    private Tooltips() {
    }

    public static List<Component> slotTooltip(MutableComponent text) {
        return List.of(text.withStyle(MUTED_COLOR));
    }

    public static List<Component> inputSlot(Side ... sides) {
        List<Component> sidesText = Arrays.stream(sides).map(Tooltips::side).toList();
        return List.of(ButtonToolTips.CanInsertFrom.text(Tooltips.conjunction(sidesText)).withStyle(MUTED_COLOR));
    }

    public static List<Component> outputSlot(Side ... sides) {
        List<Component> sidesText = Arrays.stream(sides).map(Tooltips::side).toList();
        return List.of(ButtonToolTips.CanExtractFrom.text(Tooltips.conjunction(sidesText)).withStyle(MUTED_COLOR));
    }

    public static Component side(Side side) {
        return switch (side) {
            default -> throw new MatchException(null, null);
            case Side.BOTTOM -> ButtonToolTips.SideBottom.text();
            case Side.TOP -> ButtonToolTips.SideTop.text();
            case Side.LEFT -> ButtonToolTips.SideLeft.text();
            case Side.RIGHT -> ButtonToolTips.SideRight.text();
            case Side.FRONT -> ButtonToolTips.SideFront.text();
            case Side.BACK -> ButtonToolTips.SideBack.text();
            case Side.ANY -> ButtonToolTips.SideAny.text();
        };
    }

    public static Component conjunction(List<Component> components) {
        return Tooltips.list(components, GuiText.And);
    }

    public static Component disjunction(List<Component> components) {
        return Tooltips.list(components, GuiText.Or);
    }

    @NotNull
    private static Component list(List<Component> components, GuiText lastJoiner) {
        if (components.isEmpty()) {
            return Component.empty();
        }
        if (components.size() == 2) {
            return components.get(0).copy().append(" ").append((Component)lastJoiner.text()).append(" ").append(components.get(1));
        }
        Component current = components.get(0);
        for (int i = 1; i < components.size(); ++i) {
            current = i + 1 < components.size() ? current.copy().append(", ").append(components.get(i)) : current.copy().append(", ").append((Component)lastJoiner.text()).append(" ").append(components.get(i));
        }
        return current;
    }

    public static List<Component> getEmptyingTooltip(ButtonToolTips baseAction, ItemStack carried, EmptyingAction emptyingAction) {
        return List.of(baseAction.text(Tooltips.getMouseButtonText(0), carried.getHoverName().copy().withStyle(NORMAL_TOOLTIP_TEXT)).withStyle(MUTED_COLOR), baseAction.text(Tooltips.getMouseButtonText(1), emptyingAction.description().copy().withStyle(NORMAL_TOOLTIP_TEXT)).withStyle(MUTED_COLOR));
    }

    public static Component getSetAmountTooltip() {
        return ButtonToolTips.ModifyAmountAction.text(Tooltips.getMouseButtonText(2)).withStyle(MUTED_COLOR);
    }

    public static Component getMouseButtonText(int button) {
        return switch (button) {
            case 0 -> ButtonToolTips.LeftClick.text();
            case 1 -> ButtonToolTips.RightClick.text();
            case 2 -> ButtonToolTips.MiddleClick.text();
            default -> ButtonToolTips.MouseButton.text(button);
        };
    }

    public static boolean shouldShowAmountTooltip(AEKey what, long amount) {
        AEItemKey itemKey;
        long bigNumber = AEConfig.instance().isUseLargeFonts() ? 999L : 9999L;
        return amount > bigNumber * (long)what.getAmountPerUnit() || what.getUnitSymbol() != null || what instanceof AEItemKey && (itemKey = (AEItemKey)what).getReadOnlyStack().isBarVisible();
    }

    public static Component getAmountTooltip(ButtonToolTips baseText, GenericStack stack) {
        return Tooltips.getAmountTooltip(baseText, stack.what(), stack.amount());
    }

    public static Component getAmountTooltip(ButtonToolTips baseText, AEKey what, long amount) {
        String amountText = what.formatAmount(amount, AmountFormat.FULL);
        return baseText.text(amountText).withStyle(MUTED_COLOR);
    }

    public static Component ofDuration(long number, TimeUnit unit) {
        long minutes;
        long seconds = TimeUnit.SECONDS.convert(number, unit);
        if (seconds == 0L) {
            if (number > 0L) {
                return Component.literal((String)"~").withStyle(NUMBER_TEXT).append((Component)ButtonToolTips.DurationFormatSeconds.text(0));
            }
            return ButtonToolTips.DurationFormatSeconds.text(0).withStyle(NUMBER_TEXT);
        }
        MutableComponent durationStr = Component.literal((String)"");
        long hours = TimeUnit.HOURS.convert(seconds, TimeUnit.SECONDS);
        if (hours > 0L) {
            durationStr.append(Long.toString(hours)).append("h");
            seconds -= hours * 60L * 60L;
        }
        if ((minutes = TimeUnit.MINUTES.convert(seconds, TimeUnit.SECONDS)) > 0L) {
            durationStr.append(Long.toString(minutes)).append("m");
            seconds -= minutes * 60L;
        }
        if (seconds > 0L) {
            durationStr.append(Long.toString(seconds)).append("s");
        }
        return durationStr.withStyle(NUMBER_TEXT);
    }

    public static Component ofAmount(GenericStack stack) {
        return Component.literal((String)stack.what().formatAmount(stack.amount(), AmountFormat.FULL)).withStyle(NUMBER_TEXT);
    }

    public static String getAmount(double amount, long num) {
        double fract = amount / (double)num;
        String returned = fract < 10.0 ? String.format("%.3f", fract) : (fract < 100.0 ? String.format("%.2f", fract) : String.format("%.1f", fract));
        while (returned.endsWith("0")) {
            returned = returned.substring(0, returned.length() - 1);
        }
        if (returned.endsWith(String.valueOf(SEP))) {
            returned = returned.substring(0, returned.length() - 1);
        }
        return returned;
    }

    public static Amount getAmount(double amount) {
        if (amount < 10000.0) {
            return new Amount(Tooltips.getAmount(amount, 1L), "");
        }
        int i = 0;
        while (amount / (double)DECIMAL_NUMS[i] >= 1000.0) {
            ++i;
        }
        return new Amount(Tooltips.getAmount(amount, DECIMAL_NUMS[i]), units[i]);
    }

    public static MaxedAmount getMaxedAmount(double amount, double max) {
        if (max < 10000.0) {
            return new MaxedAmount(Tooltips.getAmount(amount, 1L), Tooltips.getAmount(max, 1L), "");
        }
        int i = 0;
        while (max / (double)DECIMAL_NUMS[i] >= 1000.0) {
            ++i;
        }
        return new MaxedAmount(Tooltips.getAmount(amount, DECIMAL_NUMS[i]), Tooltips.getAmount(max, DECIMAL_NUMS[i]), units[i]);
    }

    public static Amount getByteAmount(long amount) {
        int i;
        if (amount < BYTE_NUMS[0]) {
            return new Amount(String.valueOf(amount), "");
        }
        for (i = 0; i < BYTE_NUMS.length && amount / BYTE_NUMS[i] >= 1000L; ++i) {
        }
        return new Amount(Tooltips.getAmount(amount, BYTE_NUMS[i]), units[i]);
    }

    public static Amount getAmount(long amount) {
        int i;
        if (amount < 10000L) {
            return new Amount(String.valueOf(amount), "");
        }
        for (i = 0; i < DECIMAL_NUMS.length && amount / DECIMAL_NUMS[i] >= 1000L; ++i) {
        }
        return new Amount(Tooltips.getAmount(amount, DECIMAL_NUMS[i]), units[i]);
    }

    public static MaxedAmount getMaxedAmount(long amount, long max) {
        if (max < 10000L) {
            return new MaxedAmount(String.valueOf(amount), String.valueOf(max), "");
        }
        int i = 0;
        while (max / DECIMAL_NUMS[i] >= 1000L) {
            ++i;
        }
        return new MaxedAmount(Tooltips.getAmount(amount, DECIMAL_NUMS[i]), Tooltips.getAmount(max, DECIMAL_NUMS[i]), units[i]);
    }

    public static MutableComponent of(Component component) {
        return component.copy().withStyle(NORMAL_TOOLTIP_TEXT);
    }

    public static MutableComponent of(ButtonToolTips buttonToolTips, Object ... args) {
        return Tooltips.of(buttonToolTips, NORMAL_TOOLTIP_TEXT, args);
    }

    public static MutableComponent of(ButtonToolTips buttonToolTips, Style style, Object ... args) {
        return buttonToolTips.text(args).copy().withStyle(style);
    }

    public static MutableComponent of(GuiText guiText, Object ... args) {
        return Tooltips.of(guiText, NORMAL_TOOLTIP_TEXT, args);
    }

    public static MutableComponent of(GuiText guiText, Style style, Object ... args) {
        if (args.length > 0 && args[0] instanceof Integer) {
            return guiText.text(Arrays.stream(args).map(o -> Tooltips.ofUnformattedNumber(((Integer)o).intValue())).toArray()).copy().withStyle(style);
        }
        if (args.length > 0 && args[0] instanceof Long) {
            return guiText.text(Arrays.stream(args).map(o -> Tooltips.ofUnformattedNumber((Long)o)).toArray()).copy().withStyle(style);
        }
        return guiText.text(args).copy().withStyle(style);
    }

    public static MutableComponent of(String s) {
        return Component.literal((String)s).withStyle(NORMAL_TOOLTIP_TEXT);
    }

    public static MutableComponent of(PowerUnit pU) {
        return pU.textComponent().copy().withStyle(UNIT_TEXT);
    }

    public static MutableComponent ofPercent(double percent, boolean oneIsGreen) {
        return Component.literal((String)MessageFormat.format("{0,number,#.##%}", percent)).withStyle(Tooltips.colorFromRatio(percent, oneIsGreen));
    }

    public static Style colorFromRatio(double ratio, boolean oneIsGreen) {
        double p = ratio;
        if (!oneIsGreen) {
            p = 1.0 - p;
        }
        int r = (int)(255.0 * Math.max(0.0, Math.min(2.0 - 2.0 * p, 1.0)));
        int g = (int)(255.0 * Math.max(0.0, Math.min(2.0 * p, 1.0)));
        int rgb = -16777216 + (r << 16) + (g << 8);
        return Style.EMPTY.withItalic(Boolean.valueOf(false)).withColor(TextColor.fromRgb((int)rgb));
    }

    public static MutableComponent ofPercent(double percent) {
        return Tooltips.ofPercent(percent, true);
    }

    public static MutableComponent ofUnformattedNumber(long number) {
        return Component.literal((String)String.valueOf(number)).withStyle(NUMBER_TEXT);
    }

    public static MutableComponent ofUnformattedNumberWithRatioColor(long number, double ratio, boolean oneIsGreen) {
        return Component.literal((String)String.valueOf(number)).withStyle(Tooltips.colorFromRatio(ratio, oneIsGreen));
    }

    public static MutableComponent ofBytes(long number) {
        Amount amount = Tooltips.getByteAmount(number);
        return Tooltips.ofNumber(amount);
    }

    public static MutableComponent ofNumber(long number) {
        Amount amount = Tooltips.getAmount(number);
        return Tooltips.ofNumber(amount);
    }

    public static MutableComponent ofNumber(double number) {
        Amount amount = Tooltips.getAmount(number);
        return Tooltips.ofNumber(amount);
    }

    private static MutableComponent ofNumber(Amount number) {
        return Component.literal((String)(number.digit() + number.unit())).withStyle(NUMBER_TEXT);
    }

    public static MutableComponent ofNumber(long number, long max) {
        MaxedAmount amount = Tooltips.getMaxedAmount(number, max);
        return Tooltips.ofNumber(amount);
    }

    public static MutableComponent ofNumber(double number, double max) {
        MaxedAmount amount = Tooltips.getMaxedAmount(number, max);
        return Tooltips.ofNumber(amount);
    }

    private static MutableComponent ofNumber(MaxedAmount number) {
        boolean numberUnit = !number.digit().equals("0");
        return Component.literal((String)(number.digit() + (numberUnit ? number.unit() : ""))).withStyle(NUMBER_TEXT).append((Component)Component.literal((String)"/").withStyle(NORMAL_TOOLTIP_TEXT)).append(number.maxDigit() + number.unit()).withStyle(NUMBER_TEXT);
    }

    public static MutableComponent of(Component ... components) {
        MutableComponent s = Component.literal((String)"");
        for (Component c : components) {
            s = s.append(c);
        }
        return s;
    }

    public static Component energyStorageComponent(double energy, double max) {
        return Tooltips.of(new Component[]{Tooltips.of(GuiText.StoredEnergy, new Object[0]), Tooltips.of(": "), Tooltips.ofNumber(energy, max), Tooltips.of(" "), Tooltips.of(PowerUnit.AE), Tooltips.of(" ("), Tooltips.ofPercent(energy / max), Tooltips.of(")")});
    }

    public static Component bytesUsed(long bytes, long max) {
        return Tooltips.of(GuiText.BytesUsed, Tooltips.of(new Component[]{Tooltips.ofUnformattedNumberWithRatioColor(bytes, (double)bytes / (double)max, false), Tooltips.of(" "), Tooltips.of(GuiText.Of, new Object[0]), Tooltips.of(" "), Tooltips.ofUnformattedNumber(max)}));
    }

    public static Component typesUsed(long types, long max) {
        return Tooltips.of(new Component[]{Tooltips.ofUnformattedNumberWithRatioColor(types, (double)types / (double)max, false), Tooltips.of(" "), Tooltips.of(GuiText.Of, new Object[0]), Tooltips.of(" "), Tooltips.ofUnformattedNumber(max), Tooltips.of(" "), Tooltips.of(GuiText.Types, new Object[0])});
    }

    static {
        DecimalFormat format = (DecimalFormat)DecimalFormat.getInstance();
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
        SEP = symbols.getDecimalSeparator();
        MUTED_COLOR = ChatFormatting.DARK_GRAY;
        NORMAL_TOOLTIP_TEXT = Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(Boolean.valueOf(false));
        QUOTE_TEXT = NORMAL_TOOLTIP_TEXT.withItalic(Boolean.valueOf(true));
        NUMBER_TEXT = Style.EMPTY.withColor(TextColor.fromRgb((int)8941311)).withItalic(Boolean.valueOf(false));
        UNIT_TEXT = Style.EMPTY.withColor(TextColor.fromRgb((int)16768637)).withItalic(Boolean.valueOf(false));
        RED = Style.EMPTY.withColor(ChatFormatting.RED);
        GREEN = Style.EMPTY.withColor(ChatFormatting.GREEN);
        units = new String[]{"k", "M", "G", "T", "P", "E"};
        DECIMAL_NUMS = new long[]{1000L, 1000000L, 1000000000L, 1000000000000L, 1000000000000000L, 1000000000000000000L};
        BYTE_NUMS = new long[]{1024L, 0x100000L, 0x40000000L, 0x10000000000L};
    }

    public record Amount(String digit, String unit) {
    }

    public record MaxedAmount(String digit, String maxDigit, String unit) {
    }
}

