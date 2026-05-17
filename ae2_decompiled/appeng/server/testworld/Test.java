/*
 * Decompiled with CFR 0.152.
 */
package appeng.server.testworld;

import appeng.server.testworld.PlotTestHelper;
import java.util.function.Consumer;

public final class Test {
    private final Consumer<PlotTestHelper> testFunction;
    public int setupTicks = 21;
    public int maxTicks = 150;
    public boolean skyAccess = false;

    public Test(Consumer<PlotTestHelper> testFunction) {
        this.testFunction = testFunction;
    }

    public Consumer<PlotTestHelper> getTestFunction() {
        return this.testFunction;
    }

    public Test setupTicks(int ticks) {
        this.setupTicks = ticks;
        return this;
    }

    public Test maxTicks(int maxTicks) {
        this.maxTicks = maxTicks;
        return this;
    }

    public Test withSkyAccess() {
        this.skyAccess = true;
        return this;
    }
}

