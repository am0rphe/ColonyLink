/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 */
package appeng.server.testplots;

import appeng.core.AppEng;
import appeng.server.testworld.PlotBuilder;
import appeng.server.testworld.PlotTestHelper;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;

public class TestPlotCollection {
    private final Map<ResourceLocation, Consumer<PlotBuilder>> plots;

    public TestPlotCollection(Map<ResourceLocation, Consumer<PlotBuilder>> plots) {
        this.plots = plots;
    }

    public void add(String id, Consumer<PlotBuilder> builder) {
        this.add(AppEng.makeId(id), builder);
    }

    public void add(String id, Consumer<PlotBuilder> builder, Consumer<PlotTestHelper> test) {
        this.add(AppEng.makeId(id), builder, test);
    }

    public void add(ResourceLocation id, Consumer<PlotBuilder> builder) {
        this.plots.put(id, builder);
    }

    public void add(ResourceLocation id, Consumer<PlotBuilder> builder, Consumer<PlotTestHelper> test) {
        this.plots.put(id, actualBuilder -> {
            builder.accept((PlotBuilder)actualBuilder);
            actualBuilder.test(test);
        });
    }
}

