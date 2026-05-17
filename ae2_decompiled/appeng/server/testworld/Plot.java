/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  net.minecraft.core.BlockPos
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.levelgen.structure.BoundingBox
 */
package appeng.server.testworld;

import appeng.server.testworld.BuildAction;
import appeng.server.testworld.GridInitHelper;
import appeng.server.testworld.PlotBuilder;
import appeng.server.testworld.PlotTestHelper;
import appeng.server.testworld.Test;
import appeng.server.testworld.TransformingPlotBuilder;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class Plot
implements PlotBuilder {
    private final ResourceLocation id;
    private final List<BuildAction> buildActions = new ArrayList<BuildAction>();
    private final List<PlotBuilder.PostBuildAction> postBuildActions = new ArrayList<PlotBuilder.PostBuildAction>();
    private final List<PlotBuilder.PostBuildAction> postInitActions = new ArrayList<PlotBuilder.PostBuildAction>();
    private Test test;
    private static final Pattern RANGE = Pattern.compile("\\[(-?\\d+),(-?\\d+)]");

    public BoundingBox getBounds() {
        if (this.buildActions.isEmpty()) {
            return new BoundingBox(0, 0, 0, 0, 0, 0);
        }
        return (BoundingBox)BoundingBox.encapsulatingBoxes(this.buildActions.stream().map(BuildAction::getBoundingBox).toList()).orElseThrow();
    }

    public Plot(ResourceLocation id) {
        this.id = id;
    }

    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public void addBuildAction(BuildAction action) {
        this.buildActions.add(action);
    }

    @Override
    public void addPostBuildAction(PlotBuilder.PostBuildAction action) {
        this.postBuildActions.add(action);
    }

    @Override
    public void addPostInitAction(PlotBuilder.PostBuildAction action) {
        this.postInitActions.add(action);
    }

    @Override
    public PlotBuilder transform(Function<BoundingBox, BoundingBox> transform) {
        return new TransformingPlotBuilder(this, transform);
    }

    @Override
    public BoundingBox bb(String def) {
        int[] p;
        String[] parts = def.split("\\s+");
        Preconditions.checkArgument((parts.length * 2 == (p = new int[6]).length ? 1 : 0) != 0);
        for (int i = 0; i < parts.length; ++i) {
            String part = parts[i];
            Matcher rangeMatch = RANGE.matcher(part);
            if (rangeMatch.matches()) {
                p[i * 2] = Integer.parseInt(rangeMatch.group(1));
                p[i * 2 + 1] = Integer.parseInt(rangeMatch.group(2));
                continue;
            }
            int n = Integer.parseInt(part);
            p[i * 2 + 1] = n;
            p[i * 2] = n;
        }
        Preconditions.checkArgument((p[0] <= p[1] ? 1 : 0) != 0, (String)"Invalid bb: %s", (Object)def);
        Preconditions.checkArgument((p[2] <= p[3] ? 1 : 0) != 0, (String)"Invalid bb: %s", (Object)def);
        Preconditions.checkArgument((p[4] <= p[5] ? 1 : 0) != 0, (String)"Invalid bb: %s", (Object)def);
        return new BoundingBox(p[0], p[2], p[4], p[1], p[3], p[5]);
    }

    public void build(ServerLevel level, Player player, BlockPos origin) {
        this.build(level, player, origin, new ArrayList<Entity>());
    }

    public void build(ServerLevel level, Player player, BlockPos origin, List<Entity> entities) {
        for (BuildAction buildAction : this.buildActions) {
            buildAction.build(level, player, origin);
        }
        for (BuildAction buildAction : this.buildActions) {
            buildAction.spawnEntities(level, origin, entities);
        }
        for (PlotBuilder.PostBuildAction postBuildAction : this.postBuildActions) {
            postBuildAction.postBuild(level, player, origin);
        }
        if (!this.postInitActions.isEmpty()) {
            List<BlockEntity> blockEntities = BlockPos.betweenClosedStream((BoundingBox)this.getBounds().moved(origin.getX(), origin.getY(), origin.getZ())).map(arg_0 -> ((ServerLevel)level).getBlockEntity(arg_0)).filter(Objects::nonNull).toList();
            GridInitHelper.doAfterGridInit(level, blockEntities, false, () -> {
                for (PlotBuilder.PostBuildAction action : this.postInitActions) {
                    action.postBuild(level, player, origin);
                }
            });
        }
    }

    public Test getTest() {
        return this.test;
    }

    @Override
    public Test test(Consumer<PlotTestHelper> testFunction) {
        this.test = new Test(testFunction);
        return this.test;
    }
}

