/*
 * Decompiled with CFR 0.152.
 */
package appeng.server.services.compass;

public class CompassException
extends RuntimeException {
    private static final long serialVersionUID = 8825268683203860877L;
    private final Throwable inner;

    public CompassException(Throwable t) {
        this.inner = t;
    }
}

