/*
 * Decompiled with CFR 0.152.
 */
package appeng.api.util;

public enum AECableSize {
    NONE,
    NORMAL,
    DENSE;


    public static AECableSize min(AECableSize a, AECableSize b) {
        return a.compareTo(b) < 0 ? a : b;
    }

    public static AECableSize max(AECableSize a, AECableSize b) {
        return a.compareTo(b) > 0 ? a : b;
    }
}

