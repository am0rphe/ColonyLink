/*
 * Decompiled with CFR 0.152.
 */
package appeng.api.config;

public enum AccessRestriction {
    NO_ACCESS(false, false),
    READ(true, false),
    WRITE(false, true),
    READ_WRITE(true, true);

    private final boolean allowExtraction;
    private final boolean allowInsertion;

    private AccessRestriction(boolean allowExtraction, boolean allowInsertion) {
        this.allowExtraction = allowExtraction;
        this.allowInsertion = allowInsertion;
    }

    public boolean isAllowExtraction() {
        return this.allowExtraction;
    }

    public boolean isAllowInsertion() {
        return this.allowInsertion;
    }
}

