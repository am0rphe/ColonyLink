/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.levelgen.structure.BoundingBox
 */
package appeng.server.testworld;

import appeng.server.testworld.BuildAction;
import appeng.server.testworld.Plot;
import appeng.server.testworld.PlotBuilder;
import appeng.server.testworld.PlotTestHelper;
import appeng.server.testworld.Test;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

class TransformingPlotBuilder
implements PlotBuilder {
    private final Plot plot;
    private final Function<BoundingBox, BoundingBox> transform;

    TransformingPlotBuilder(Plot plot, Function<BoundingBox, BoundingBox> transform) {
        this.plot = plot;
        this.transform = transform;
    }

    @Override
    public void addBuildAction(BuildAction action) {
        this.plot.addBuildAction(action);
    }

    @Override
    public void addPostBuildAction(PlotBuilder.PostBuildAction action) {
        this.plot.addPostBuildAction(action);
    }

    @Override
    public void addPostInitAction(PlotBuilder.PostBuildAction action) {
        this.plot.addPostInitAction(action);
    }

    @Override
    public BoundingBox bb(String def) {
        return this.transform.apply(this.plot.bb(def));
    }

    @Override
    public PlotBuilder transform(Function<BoundingBox, BoundingBox> transform) {
        return new TransformingPlotBuilder(this.plot, this.transform.andThen(transform));
    }

    @Override
    public Test test(Consumer<PlotTestHelper> assertion) {
        return this.plot.test(assertion);
    }
}

