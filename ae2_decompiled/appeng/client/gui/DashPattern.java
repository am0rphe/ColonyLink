/*
 * Decompiled with CFR 0.152.
 */
package appeng.client.gui;

public record DashPattern(float width, float onLength, float offLength, int color, float animationCycleMs) {
    float length() {
        return this.onLength + this.offLength;
    }
}

