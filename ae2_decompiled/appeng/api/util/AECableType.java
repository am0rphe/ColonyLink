/*
 * Decompiled with CFR 0.152.
 */
package appeng.api.util;

import appeng.api.util.AECableSize;
import appeng.api.util.AECableVariant;

public enum AECableType {
    NONE(AECableVariant.NONE, AECableSize.NONE),
    GLASS(AECableVariant.GLASS, AECableSize.NORMAL),
    COVERED(AECableVariant.COVERED, AECableSize.NORMAL),
    SMART(AECableVariant.SMART, AECableSize.NORMAL),
    DENSE_COVERED(AECableVariant.COVERED, AECableSize.DENSE),
    DENSE_SMART(AECableVariant.SMART, AECableSize.DENSE);

    public static final AECableType[] VALIDCABLES;
    private final AECableVariant variant;
    private final AECableSize size;

    private AECableType(AECableVariant variant, AECableSize size) {
        this.variant = variant;
        this.size = size;
    }

    public AECableSize size() {
        return this.size;
    }

    public AECableVariant variant() {
        return this.variant;
    }

    public boolean isValid() {
        return this.variant != AECableVariant.NONE && this.size != AECableSize.NONE;
    }

    public boolean isDense() {
        return this.size == AECableSize.DENSE;
    }

    public boolean isSmart() {
        return this.variant == AECableVariant.SMART;
    }

    public static AECableType min(AECableType a, AECableType b) {
        AECableVariant v = AECableVariant.min(a.variant(), b.variant());
        AECableSize s = AECableSize.min(a.size(), b.size());
        return AECableType.from(v, s);
    }

    public static AECableType max(AECableType a, AECableType b) {
        AECableVariant v = AECableVariant.max(a.variant(), b.variant());
        AECableSize s = AECableSize.max(a.size(), b.size());
        return AECableType.from(v, s);
    }

    private static AECableType from(AECableVariant variant, AECableSize size) {
        switch (variant) {
            case GLASS: {
                switch (size) {
                    case NORMAL: {
                        return GLASS;
                    }
                }
                break;
            }
            case COVERED: {
                switch (size) {
                    case NORMAL: {
                        return COVERED;
                    }
                    case DENSE: {
                        return DENSE_COVERED;
                    }
                }
                break;
            }
            case SMART: {
                switch (size) {
                    case NORMAL: {
                        return SMART;
                    }
                    case DENSE: {
                        return DENSE_SMART;
                    }
                }
                break;
            }
        }
        return NONE;
    }

    static {
        VALIDCABLES = new AECableType[]{GLASS, COVERED, SMART, DENSE_COVERED, DENSE_SMART};
    }
}

