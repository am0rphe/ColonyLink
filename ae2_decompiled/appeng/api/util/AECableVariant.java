/*
 * Decompiled with CFR 0.152.
 */
package appeng.api.util;

public enum AECableVariant {
    NONE,
    GLASS,
    COVERED,
    SMART;


    public static AECableVariant min(AECableVariant a, AECableVariant b) {
        return a.compareTo(b) < 0 ? a : b;
    }

    public static AECableVariant max(AECableVariant a, AECableVariant b) {
        return a.compareTo(b) > 0 ? a : b;
    }
}

