/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterables
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.BlockPos$MutableBlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.DyeColor
 *  net.minecraft.world.level.ChunkPos
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.Rotation
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.entity.SignText
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.chunk.LevelChunk
 *  net.minecraft.world.level.chunk.LevelChunkSection
 *  net.minecraft.world.level.levelgen.structure.BoundingBox
 *  org.jetbrains.annotations.Nullable
 */
package appeng.server.testworld;

import appeng.core.definitions.AEBlocks;
import appeng.server.testplots.TestPlots;
import appeng.server.testworld.Plot;
import appeng.server.testworld.RectanglePacking;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jetbrains.annotations.Nullable;

public class TestWorldGenerator {
    private static final int PADDING = 3;
    private static final int OUTER_PADDING = 10;
    private final ServerLevel level;
    private final BlockPos origin;
    private final ServerPlayer player;
    private final List<PositionedPlot> positionedPlots;
    private final BoundingBox overallBounds;
    private final BlockPos suitableStartPos;

    public TestWorldGenerator(ServerLevel level, ServerPlayer player, BlockPos origin, @Nullable ResourceLocation plotId) {
        this.level = level;
        this.origin = origin;
        this.player = player;
        List<Plot> plots = plotId != null ? Collections.singletonList(TestPlots.getById(plotId)) : TestPlots.createPlots();
        RectanglePacking.PositionedArea<Plot> positionedArea = RectanglePacking.pack(plots, plot -> {
            BoundingBox bb = plot.getBounds();
            return new RectanglePacking.Size(bb.getXSpan() + 6, bb.getZSpan() + 6);
        });
        this.positionedPlots = positionedArea.rectangles().stream().map(pp -> {
            BoundingBox relativeBounds = ((Plot)pp.what()).getBounds();
            BlockPos plotOrigin = new BlockPos(pp.x() - ((Plot)pp.what()).getBounds().minX() + 3, origin.getY(), pp.y() - ((Plot)pp.what()).getBounds().minZ() + 3);
            BoundingBox absBoundingBox = relativeBounds.moved(plotOrigin.getX(), plotOrigin.getY(), plotOrigin.getZ());
            return new PositionedPlot(plotOrigin, absBoundingBox, (Plot)pp.what());
        }).toList();
        this.overallBounds = (BoundingBox)BoundingBox.encapsulatingBoxes(this.positionedPlots.stream().map(PositionedPlot::bounds).toList()).orElseThrow();
        this.suitableStartPos = origin.offset(positionedArea.w() / 2, 0, -2);
    }

    public BlockPos getSuitableStartPos() {
        return this.suitableStartPos;
    }

    public boolean isWithinBounds(BlockPos pos) {
        return this.overallBounds.inflatedBy(10).isInside((Vec3i)pos);
    }

    public void generate() {
        this.clearLevel();
        this.buildPlatform();
        ArrayList<Entity> entities = new ArrayList<Entity>();
        this.buildPlots(entities);
        this.clearEntities(entities);
    }

    private void buildPlots(List<Entity> entities) {
        for (PositionedPlot positionedPlot : this.positionedPlots) {
            this.outline(positionedPlot);
            this.placeSign(positionedPlot);
            positionedPlot.plot.build(this.level, (Player)this.player, positionedPlot.origin, entities);
        }
    }

    private void placeSign(PositionedPlot positionedPlot) {
        BlockPos signPos = new BlockPos(positionedPlot.bounds.maxX() + 2, this.origin.getY(), positionedPlot.bounds.minZ() - 2);
        this.level.setBlock(signPos, Blocks.OAK_SIGN.defaultBlockState().rotate(Rotation.CLOCKWISE_180), 3);
        this.level.getBlockEntity(signPos, BlockEntityType.SIGN).ifPresent(sign -> {
            SignText signText = sign.getText(true);
            sign.setAllowedPlayerEditor(null);
            signText.setHasGlowingText(true);
            signText.setColor(DyeColor.WHITE);
            StringBuilder text = new StringBuilder(positionedPlot.plot.getId().getPath());
            int line = 0;
            while (line < 4 && !text.isEmpty()) {
                int lineLength = Math.min(12, text.length());
                String lineText = text.substring(0, lineLength);
                text.delete(0, lineLength);
                signText = signText.setMessage(line++, (Component)Component.literal((String)lineText));
            }
            sign.setText(signText, true);
        });
    }

    private void outline(PositionedPlot positionedPlot) {
        BlockPos from = new BlockPos(positionedPlot.bounds.minX() - 1, positionedPlot.origin.getY() - 1, positionedPlot.bounds.minZ() - 1);
        BlockPos to = new BlockPos(positionedPlot.bounds.maxX() + 1, positionedPlot.origin.getY() - 1, positionedPlot.bounds.maxZ() + 1);
        for (BlockPos pos : BlockPos.betweenClosed((BlockPos)from, (BlockPos)to)) {
            this.level.setBlock(pos, AEBlocks.SKY_STONE_SMALL_BRICK.block().defaultBlockState(), 3);
        }
    }

    private void buildPlatform() {
        ChunkPos from = new ChunkPos(new BlockPos(this.overallBounds.minX() - 10, 0, this.overallBounds.minZ() - 10));
        ChunkPos to = new ChunkPos(new BlockPos(this.overallBounds.maxX() + 10, 0, this.overallBounds.maxZ() + 10));
        BlockState state = AEBlocks.SKY_STONE_BRICK.block().defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        ChunkPos.rangeClosed((ChunkPos)from, (ChunkPos)to).forEach(chunkPos -> {
            LevelChunk chunk = this.level.getChunk(chunkPos.x, chunkPos.z);
            for (int x = 0; x < 16; ++x) {
                pos.setX(chunkPos.getMinBlockX() + x);
                for (int z = 0; z < 16; ++z) {
                    pos.setZ(chunkPos.getMinBlockZ() + z);
                    for (int y = -3; y <= -1; ++y) {
                        pos.setY(this.origin.getY() + y);
                        chunk.setBlockState((BlockPos)pos, state, false);
                    }
                }
            }
        });
    }

    private void clearLevel() {
        ChunkPos from = new ChunkPos(new BlockPos(this.overallBounds.minX() - 10, 0, this.overallBounds.minZ() - 10));
        ChunkPos to = new ChunkPos(new BlockPos(this.overallBounds.maxX() + 10, 0, this.overallBounds.maxZ() + 10));
        ChunkPos.rangeClosed((ChunkPos)from, (ChunkPos)to).forEach(chunkPos -> {
            LevelChunk chunk = this.level.getChunk(chunkPos.x, chunkPos.z);
            if (!chunk.isEmpty()) {
                this.clearChunk(chunk);
            }
        });
    }

    private void clearChunk(LevelChunk chunk) {
        if (chunk.isEmpty()) {
            return;
        }
        int sectionId = 0;
        for (LevelChunkSection sec : chunk.getSections()) {
            if (!sec.hasOnlyAir()) {
                BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
                BlockState air = Blocks.AIR.defaultBlockState();
                int bottomBlock = chunk.getMinBuildHeight() + 16 * sectionId;
                for (int y = 0; y < 16; ++y) {
                    p.setY(bottomBlock + y);
                    for (int x = 0; x < 16; ++x) {
                        p.setX(chunk.getPos().getMinBlockX() + x);
                        for (int z = 0; z < 16; ++z) {
                            p.setZ(chunk.getPos().getMinBlockZ() + z);
                            this.level.setBlock((BlockPos)p, air, 3);
                        }
                    }
                }
            }
            ++sectionId;
        }
    }

    private void clearEntities(List<Entity> plotEntities) {
        Entity[] entities;
        for (Entity entity : entities = (Entity[])Iterables.toArray((Iterable)this.level.getAllEntities(), Entity.class)) {
            if (plotEntities.contains(entity) || entity instanceof Player || !entity.isAlive()) continue;
            entity.discard();
        }
    }

    private record PositionedPlot(BlockPos origin, BoundingBox bounds, Plot plot) {
    }
}

