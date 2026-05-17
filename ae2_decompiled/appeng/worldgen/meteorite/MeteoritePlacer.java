/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Util
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.BlockPos$MutableBlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.tags.BlockTags
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.block.AmethystClusterBlock
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.chunk.ChunkAccess
 *  net.minecraft.world.level.levelgen.structure.BoundingBox
 *  net.minecraft.world.level.material.Fluid
 *  net.minecraft.world.level.material.Fluids
 *  net.minecraft.world.phys.AABB
 */
package appeng.worldgen.meteorite;

import appeng.block.AEBaseBlock;
import appeng.core.AEConfig;
import appeng.core.definitions.AEBlocks;
import appeng.decorative.solid.BuddingCertusQuartzBlock;
import appeng.decorative.solid.CertusQuartzClusterBlock;
import appeng.worldgen.meteorite.CraterType;
import appeng.worldgen.meteorite.MeteoriteBlockPutter;
import appeng.worldgen.meteorite.PlacedMeteoriteSettings;
import appeng.worldgen.meteorite.fallout.Fallout;
import appeng.worldgen.meteorite.fallout.FalloutCopy;
import appeng.worldgen.meteorite.fallout.FalloutMode;
import appeng.worldgen.meteorite.fallout.FalloutSand;
import appeng.worldgen.meteorite.fallout.FalloutSnow;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;

public final class MeteoritePlacer {
    private final BlockState skyStone;
    private final List<BlockState> quartzBlocks;
    private final List<BlockState> quartzBuds;
    private final MeteoriteBlockPutter putter = new MeteoriteBlockPutter();
    private final LevelAccessor level;
    private final RandomSource random;
    private final Fallout type;
    private final BlockPos pos;
    private final int x;
    private final int y;
    private final int z;
    private final double meteoriteSize;
    private final double squaredMeteoriteSize;
    private final double crater;
    private final boolean placeCrater;
    private final CraterType craterType;
    private final boolean pureCrater;
    private final boolean craterLake;
    private final BoundingBox boundingBox;

    public static void place(LevelAccessor level, PlacedMeteoriteSettings settings, BoundingBox boundingBox, RandomSource random) {
        MeteoritePlacer placer = new MeteoritePlacer(level, settings, boundingBox, random);
        placer.place();
    }

    private MeteoritePlacer(LevelAccessor level, PlacedMeteoriteSettings settings, BoundingBox boundingBox, RandomSource random) {
        this.boundingBox = boundingBox;
        this.level = level;
        this.random = random;
        this.pos = settings.getPos();
        this.x = settings.getPos().getX();
        this.y = settings.getPos().getY();
        this.z = settings.getPos().getZ();
        this.meteoriteSize = settings.getMeteoriteRadius();
        this.placeCrater = settings.shouldPlaceCrater();
        this.craterType = settings.getCraterType();
        this.pureCrater = settings.isPureCrater();
        this.craterLake = settings.isCraterLake();
        this.squaredMeteoriteSize = this.meteoriteSize * this.meteoriteSize;
        double realCrater = this.meteoriteSize * 2.0 + 5.0;
        this.crater = realCrater * realCrater;
        this.quartzBlocks = this.getQuartzBudList();
        this.quartzBuds = Stream.of(AEBlocks.SMALL_QUARTZ_BUD, AEBlocks.MEDIUM_QUARTZ_BUD, AEBlocks.LARGE_QUARTZ_BUD).map(def -> ((CertusQuartzClusterBlock)def.block()).defaultBlockState()).toList();
        this.skyStone = AEBlocks.SKY_STONE_BLOCK.block().defaultBlockState();
        this.type = this.getFallout(level, boundingBox.getCenter(), settings.getFallout());
    }

    private List<BlockState> getQuartzBudList() {
        if (AEConfig.instance().isSpawnFlawlessOnlyEnabled()) {
            return Stream.of(AEBlocks.FLAWLESS_BUDDING_QUARTZ).map(def -> ((BuddingCertusQuartzBlock)def.block()).defaultBlockState()).toList();
        }
        return Stream.of(AEBlocks.QUARTZ_BLOCK, AEBlocks.DAMAGED_BUDDING_QUARTZ, AEBlocks.CHIPPED_BUDDING_QUARTZ, AEBlocks.FLAWED_BUDDING_QUARTZ, AEBlocks.FLAWLESS_BUDDING_QUARTZ).map(def -> ((AEBaseBlock)def.block()).defaultBlockState()).toList();
    }

    public void place() {
        if (this.placeCrater) {
            this.placeCrater();
        }
        this.placeMeteorite();
        if (this.placeCrater) {
            this.decay();
        }
        if (this.craterLake) {
            this.placeCraterLake();
        }
    }

    private int minX(int x) {
        if (x < this.boundingBox.minX()) {
            return this.boundingBox.minX();
        }
        if (x > this.boundingBox.maxX()) {
            return this.boundingBox.maxX();
        }
        return x;
    }

    private int minZ(int x) {
        if (x < this.boundingBox.minZ()) {
            return this.boundingBox.minZ();
        }
        if (x > this.boundingBox.maxZ()) {
            return this.boundingBox.maxZ();
        }
        return x;
    }

    private int maxX(int x) {
        if (x < this.boundingBox.minX()) {
            return this.boundingBox.minX();
        }
        if (x > this.boundingBox.maxX()) {
            return this.boundingBox.maxX();
        }
        return x;
    }

    private int maxZ(int x) {
        if (x < this.boundingBox.minZ()) {
            return this.boundingBox.minZ();
        }
        if (x > this.boundingBox.maxZ()) {
            return this.boundingBox.maxZ();
        }
        return x;
    }

    private void placeCrater() {
        int maxY = this.level.getMaxBuildHeight();
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
        BlockState filler = this.craterType.getFiller().defaultBlockState();
        for (int j = this.y - 5; j <= maxY; ++j) {
            blockPos.setY(j);
            for (int i = this.boundingBox.minX(); i <= this.boundingBox.maxX(); ++i) {
                blockPos.setX(i);
                for (int k = this.boundingBox.minZ(); k <= this.boundingBox.maxZ(); ++k) {
                    blockPos.setZ(k);
                    double dx = i - this.x;
                    double dz = k - this.z;
                    double h = (double)this.y - this.meteoriteSize + 1.0 + (double)this.type.adjustCrater();
                    double distanceFrom = dx * dx + dz * dz;
                    if (!((double)j > h + distanceFrom * 0.02)) continue;
                    BlockState currentBlock = this.level.getBlockState((BlockPos)blockPos);
                    if (this.craterType != CraterType.NORMAL && j < this.y && currentBlock.isSolid()) {
                        if (!((double)j > h + distanceFrom * 0.02)) continue;
                        this.putter.put(this.level, (BlockPos)blockPos, filler);
                        continue;
                    }
                    this.putter.put(this.level, (BlockPos)blockPos, Blocks.AIR.defaultBlockState());
                }
            }
        }
        for (ItemEntity e : this.level.getEntitiesOfClass(ItemEntity.class, new AABB((double)this.minX(this.x - 30), (double)(this.y - 5), (double)this.minZ(this.z - 30), (double)this.maxX(this.x + 30), (double)(this.y + 30), (double)this.maxZ(this.z + 30)))) {
            e.discard();
        }
    }

    private void placeMeteorite() {
        this.placeMeteoriteSkyStone();
        if (this.boundingBox.isInside((Vec3i)this.pos)) {
            this.placeChest();
        }
    }

    private void placeChest() {
        if (AEConfig.instance().isSpawnPressesInMeteoritesEnabled()) {
            this.putter.put(this.level, this.pos, AEBlocks.MYSTERIOUS_CUBE.block().defaultBlockState());
        }
    }

    private void placeMeteoriteSkyStone() {
        int meteorXLength = this.minX(this.x - 8);
        int meteorXHeight = this.maxX(this.x + 8);
        int meteorZLength = this.minZ(this.z - 8);
        int meteorZHeight = this.maxZ(this.z + 8);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int i = meteorXLength; i <= meteorXHeight; ++i) {
            pos.setX(i);
            for (int j = this.y - 8; j < this.y + 8; ++j) {
                pos.setY(j);
                for (int k = meteorZLength; k <= meteorZHeight; ++k) {
                    pos.setZ(k);
                    int dx = i - this.x;
                    int dy = j - this.y;
                    int dz = k - this.z;
                    double d = (double)(dx * dx) * 0.7;
                    double d2 = dy * dy;
                    double d3 = j > this.y ? 1.4 : 0.8;
                    if (!(d + d2 * d3 + (double)(dz * dz) * 0.7 < this.squaredMeteoriteSize)) continue;
                    if (Math.abs(dx) <= 1 && Math.abs(dy) <= 1 && Math.abs(dz) <= 1) {
                        if (dy != -1) continue;
                        int certusIndex = this.random.nextInt(this.quartzBlocks.size());
                        this.putter.put(this.level, (BlockPos)pos, this.quartzBlocks.get(certusIndex));
                        if (certusIndex == 0 || dx == 0 && dz == 0 || !((double)this.random.nextFloat() <= 0.7)) continue;
                        BlockState bud = (BlockState)Util.getRandom(this.quartzBuds, (RandomSource)this.random);
                        BlockState budState = (BlockState)bud.setValue((Property)AmethystClusterBlock.FACING, (Comparable)Direction.UP);
                        this.putter.put(this.level, pos.offset(0, 1, 0), budState);
                        continue;
                    }
                    this.putter.put(this.level, (BlockPos)pos, this.skyStone);
                }
            }
        }
    }

    private void decay() {
        double randomShit = 0.0;
        int meteorXLength = this.minX(this.x - 30);
        int meteorXHeight = this.maxX(this.x + 30);
        int meteorZLength = this.minZ(this.z - 30);
        int meteorZHeight = this.maxZ(this.z + 30);
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos blockPosUp = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos blockPosDown = new BlockPos.MutableBlockPos();
        for (int i = meteorXLength; i <= meteorXHeight; ++i) {
            blockPos.setX(i);
            blockPosUp.setX(i);
            blockPosDown.setX(i);
            for (int k = meteorZLength; k <= meteorZHeight; ++k) {
                blockPos.setZ(k);
                blockPosUp.setZ(k);
                blockPosDown.setZ(k);
                for (int j = this.y - 9; j < this.y + 30; ++j) {
                    double dz;
                    double dy;
                    blockPos.setY(j);
                    blockPosUp.setY(j + 1);
                    blockPosDown.setY(j - 1);
                    BlockState state = this.level.getBlockState((BlockPos)blockPos);
                    Block blk = this.level.getBlockState((BlockPos)blockPos).getBlock();
                    if (this.pureCrater && blk == this.craterType.getFiller()) continue;
                    if (state.canBeReplaced()) {
                        if (!this.level.isEmptyBlock((BlockPos)blockPosUp)) {
                            BlockState stateUp = this.level.getBlockState((BlockPos)blockPosUp);
                            this.level.setBlock((BlockPos)blockPos, stateUp, 3);
                            continue;
                        }
                        if (!(randomShit < 100.0 * this.crater)) continue;
                        double dx = i - this.x;
                        dy = j - this.y;
                        dz = k - this.z;
                        double dist = dx * dx + dy * dy + dz * dz;
                        BlockState xf = this.level.getBlockState((BlockPos)blockPosDown);
                        if (xf.canBeReplaced()) continue;
                        double extraRange = this.random.nextDouble() * 0.6;
                        double height = this.crater * (extraRange + 0.2) - Math.abs(dist - this.crater * 1.7);
                        if (xf.isAir() || !(height > 0.0) || !(this.random.nextDouble() > 0.6)) continue;
                        randomShit += 1.0;
                        this.type.getRandomFall(this.level, (BlockPos)blockPos);
                        continue;
                    }
                    if (!this.level.isEmptyBlock((BlockPos)blockPosUp) || !(this.random.nextDouble() > 0.4)) continue;
                    double dx = i - this.x;
                    dy = j - this.y;
                    dz = k - this.z;
                    double dr2 = dx * dx + dy * dy + dz * dz;
                    if (Math.abs(dx) <= 1.0 && Math.abs(dy) <= 1.0 && Math.abs(dz) <= 1.0 || !(dr2 < this.crater * 1.6)) continue;
                    this.type.getRandomInset(this.level, (BlockPos)blockPos);
                }
            }
        }
    }

    private void placeCraterLake() {
        int maxY = this.level.getSeaLevel() - 1;
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
        for (int currentX = this.boundingBox.minX(); currentX <= this.boundingBox.maxX(); ++currentX) {
            blockPos.setX(currentX);
            for (int currentZ = this.boundingBox.minZ(); currentZ <= this.boundingBox.maxZ(); ++currentZ) {
                blockPos.setZ(currentZ);
                ChunkAccess currentChunk = this.level.getChunk((BlockPos)blockPos);
                for (int currentY = this.y - 5; currentY <= maxY; ++currentY) {
                    blockPos.setY(currentY);
                    double dx = currentX - this.x;
                    double dz = currentZ - this.z;
                    double h = (double)this.y - this.meteoriteSize + 1.0 + (double)this.type.adjustCrater();
                    double distanceFrom = dx * dx + dz * dz;
                    if ((double)currentY > h + distanceFrom * 0.02) {
                        BlockState currentBlock = currentChunk.getBlockState((BlockPos)blockPos);
                        if (currentBlock.getBlock() != Blocks.AIR) continue;
                        this.putter.put(this.level, (BlockPos)blockPos, Blocks.WATER.defaultBlockState());
                        if (currentY != maxY) continue;
                        this.level.scheduleTick((BlockPos)blockPos, (Fluid)Fluids.WATER, 0);
                        continue;
                    }
                    if (!((double)(maxY + (maxY - currentY) * 2 + 2) > h + distanceFrom * 0.02)) continue;
                    this.pillarDownSlopeBlocks(currentChunk, blockPos);
                }
            }
        }
    }

    private void pillarDownSlopeBlocks(ChunkAccess currentChunk, BlockPos.MutableBlockPos blockPos) {
        BlockPos.MutableBlockPos enclosingBlockPos = new BlockPos.MutableBlockPos();
        enclosingBlockPos.set((Vec3i)blockPos);
        for (int i = 0; i < 20 && !this.placeEnclosingBlock(currentChunk, enclosingBlockPos); ++i) {
            enclosingBlockPos.move(Direction.DOWN);
        }
    }

    private boolean placeEnclosingBlock(ChunkAccess currentChunk, BlockPos.MutableBlockPos enclosingBlockPos) {
        BlockState currentState = currentChunk.getBlockState((BlockPos)enclosingBlockPos);
        if (currentState.getBlock() == Blocks.AIR || currentState.getFluidState().isEmpty() && (currentState.canBeReplaced() || currentState.is(BlockTags.REPLACEABLE))) {
            if (this.craterType == CraterType.LAVA && this.level.getRandom().nextFloat() < 0.075f) {
                this.putter.put(this.level, (BlockPos)enclosingBlockPos, Blocks.MAGMA_BLOCK.defaultBlockState());
            } else {
                this.type.getRandomFall(this.level, (BlockPos)enclosingBlockPos);
            }
        } else {
            return true;
        }
        return false;
    }

    private Fallout getFallout(LevelAccessor level, BlockPos pos, FalloutMode mode) {
        return switch (mode) {
            case FalloutMode.SAND -> new FalloutSand(level, pos, this.putter, this.skyStone, this.random);
            case FalloutMode.TERRACOTTA -> new FalloutCopy(level, pos, this.putter, this.skyStone, this.random);
            case FalloutMode.ICE_SNOW -> new FalloutSnow(level, pos, this.putter, this.skyStone, this.random);
            default -> new Fallout(this.putter, this.skyStone, this.random);
        };
    }
}

