/*
 * Decompiled with CFR 0.152.
 */
package appeng.me.service.helpers;

import appeng.api.networking.IGridConnection;

public class ConnectionWrapper {
    private IGridConnection connection;

    public ConnectionWrapper(IGridConnection gc) {
        this.setConnection(gc);
    }

    public IGridConnection getConnection() {
        return this.connection;
    }

    public void setConnection(IGridConnection connection) {
        this.connection = connection;
    }
}

