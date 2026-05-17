/*
 * Decompiled with CFR 0.152.
 */
package appeng.api.networking;

import appeng.api.networking.GridServices;
import appeng.api.networking.IGrid;
import appeng.me.helpers.GridServiceContainer;

public class GridServicesInternal {
    public static GridServiceContainer createServices(IGrid g) {
        return GridServices.createServices(g);
    }
}

