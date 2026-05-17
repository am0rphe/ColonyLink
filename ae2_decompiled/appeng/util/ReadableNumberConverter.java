/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 */
package appeng.util;

import com.google.common.base.Preconditions;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public final class ReadableNumberConverter {
    private static final int DIVISION_BASE = 1000;
    private static final char[] ENCODED_POSTFIXES = "KMGTPE".toCharArray();

    private ReadableNumberConverter() {
    }

    public static String format(long number, int width) {
        String slimResult;
        Preconditions.checkArgument((number >= 0L ? 1 : 0) != 0, (Object)"Non-negative numbers cannot be formatted by this method");
        String numberString = Long.toString(number);
        int numberSize = numberString.length();
        if (numberSize <= width) {
            return numberString;
        }
        long base = number;
        double last = base * 1000L;
        int exponent = -1;
        String postFix = "";
        while (numberSize > width) {
            last = base;
            numberSize = Long.toString(base /= 1000L).length() + 1;
            postFix = String.valueOf(ENCODED_POSTFIXES[++exponent]);
        }
        String withPrecision = ReadableNumberConverter.getFormat().format(last / 1000.0) + postFix;
        String withoutPrecision = base + postFix;
        String string = slimResult = withPrecision.length() <= width ? withPrecision : withoutPrecision;
        assert (slimResult.length() <= width);
        return slimResult;
    }

    public static String format(double number, int width) {
        Preconditions.checkArgument((number >= 0.0 ? 1 : 0) != 0, (Object)"Non-negative numbers cannot be formatted by this method");
        int integerDigits = (int)Math.max(0.0, Math.log10(number) + 1.0);
        int fractionalDigits = width - integerDigits - 1;
        double minFractional = Math.pow(10.0, -fractionalDigits);
        double fractional = number - Math.floor(number);
        if (fractional < 1.0E-9 || integerDigits > width - 1) {
            return ReadableNumberConverter.format((long)number, width);
        }
        if (fractional + 1.0E-9 < minFractional && integerDigits - 1 <= width) {
            return "~" + ReadableNumberConverter.format((long)number, width - 1);
        }
        DecimalFormat format = ReadableNumberConverter.getFormat();
        format.setMaximumFractionDigits(fractionalDigits);
        return format.format(number);
    }

    private static DecimalFormat getFormat() {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        DecimalFormat format = new DecimalFormat(".#;0.#");
        format.setDecimalSeparatorAlwaysShown(false);
        format.setDecimalFormatSymbols(symbols);
        format.setRoundingMode(RoundingMode.DOWN);
        return format;
    }
}

