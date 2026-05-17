/*
 * Decompiled with CFR 0.152.
 */
package appeng.thirdparty.fabric;

import java.nio.ByteOrder;

public abstract class ColorHelper {
    private static final boolean BIG_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;

    private ColorHelper() {
    }

    public static int toVanillaColor(int color) {
        if (color == -1) {
            return -1;
        }
        if (BIG_ENDIAN) {
            return (color & 0xFFFFFF) << 8 | (color & 0xFF000000) >>> 24;
        }
        return color & 0xFF00FF00 | (color & 0xFF0000) >>> 16 | (color & 0xFF) << 16;
    }

    public static int fromVanillaColor(int color) {
        if (color == -1) {
            return -1;
        }
        if (BIG_ENDIAN) {
            return (color & 0xFFFFFF00) >>> 8 | (color & 0xFF) << 24;
        }
        return color & 0xFF00FF00 | (color & 0xFF0000) >>> 16 | (color & 0xFF) << 16;
    }
}

