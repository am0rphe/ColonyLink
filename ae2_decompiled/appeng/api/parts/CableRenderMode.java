/*
 * Decompiled with CFR 0.152.
 */
package appeng.api.parts;

public enum CableRenderMode {
    STANDARD(false),
    CABLE_VIEW(true);

    public final boolean transparentFacades;
    public final boolean opaqueFacades;

    private CableRenderMode(boolean hideFacades) {
        this.transparentFacades = hideFacades;
        this.opaqueFacades = !hideFacades;
    }
}

