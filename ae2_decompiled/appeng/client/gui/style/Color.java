/*
 * Decompiled with CFR 0.152.
 */
package appeng.client.gui.style;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Color {
    private static final Pattern PATTERN = Pattern.compile("^#([0-9a-fA-F]{2}){3,4}$");
    private final int r;
    private final int g;
    private final int b;
    private final int a;

    public Color(int r, int g, int b, int a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public static Color parse(String string) {
        int b;
        int g;
        int r;
        Matcher m = PATTERN.matcher(string);
        if (!m.matches()) {
            throw new IllegalArgumentException("Color must have format #AARRGGBB (" + string + ")");
        }
        int a = 255;
        if (string.length() == 7) {
            r = Integer.valueOf(string.substring(1, 3), 16);
            g = Integer.valueOf(string.substring(3, 5), 16);
            b = Integer.valueOf(string.substring(5, 7), 16);
        } else {
            a = Integer.valueOf(string.substring(1, 3), 16);
            r = Integer.valueOf(string.substring(3, 5), 16);
            g = Integer.valueOf(string.substring(5, 7), 16);
            b = Integer.valueOf(string.substring(7, 9), 16);
        }
        return new Color(r, g, b, a);
    }

    public int getR() {
        return this.r;
    }

    public int getG() {
        return this.g;
    }

    public int getB() {
        return this.b;
    }

    public int getA() {
        return this.a;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Color color = (Color)o;
        return this.r == color.r && this.g == color.g && this.b == color.b && this.a == color.a;
    }

    public int hashCode() {
        return Objects.hash(this.r, this.g, this.b, this.a);
    }

    public String toString() {
        StringBuilder result = new StringBuilder(9);
        result.append('#');
        if (this.a <= 15) {
            result.append('0');
        }
        result.append(Integer.toHexString(this.a));
        if (this.r <= 15) {
            result.append('0');
        }
        result.append(Integer.toHexString(this.r));
        if (this.g <= 15) {
            result.append('0');
        }
        result.append(Integer.toHexString(this.g));
        if (this.b <= 15) {
            result.append('0');
        }
        result.append(Integer.toHexString(this.b));
        return result.toString();
    }

    public int toARGB() {
        return this.a << 24 | this.r << 16 | this.g << 8 | this.b;
    }
}

