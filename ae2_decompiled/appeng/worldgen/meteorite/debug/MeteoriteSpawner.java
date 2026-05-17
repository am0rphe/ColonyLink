/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.BlockPos$MutableBlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Holder
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.biome.Biome
 *  net.minecraft.world.level.block.Block
 *  org.jetbrains.annotations.Nullable
 */
package appeng.worldgen.meteorite.debug;

import appeng.worldgen.meteorite.CraterType;
import appeng.worldgen.meteorite.PlacedMeteoriteSettings;
import appeng.worldgen.meteorite.fallout.FalloutMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public class MeteoriteSpawner {
    public PlacedMeteoriteSettings trySpawnMeteoriteAtSuitableHeight(LevelReader level, BlockPos startPos, float coreRadius, CraterType craterType, boolean pureCrater) {
        int stepSize = Math.min(5, (int)Math.ceil(coreRadius) + 1);
        int minY = 10 + stepSize;
        BlockPos.MutableBlockPos mutablePos = startPos.mutable();
        mutablePos.move(Direction.DOWN, stepSize);
        while (mutablePos.getY() > minY) {
            PlacedMeteoriteSettings spawned = this.trySpawnMeteorite(level, (BlockPos)mutablePos, coreRadius, craterType, pureCrater);
            if (spawned != null) {
                return spawned;
            }
            mutablePos.setY(mutablePos.getY() - stepSize);
        }
        return null;
    }

    @Nullable
    public PlacedMeteoriteSettings trySpawnMeteorite(LevelReader level, BlockPos pos, float coreRadius, CraterType craterType, boolean pureCrater) {
        if (!this.areSurroundingsSuitable(level, pos)) {
            return null;
        }
        FalloutMode fallout = FalloutMode.fromBiome((Holder<Biome>)level.getBiome(pos));
        boolean craterLake = false;
        return new PlacedMeteoriteSettings(pos, coreRadius, craterType, fallout, pureCrater, craterLake);
    }

    private boolean areSurroundingsSuitable(LevelReader level, BlockPos pos) {
        int realValidBlocks = 0;
        BlockPos.MutableBlockPos testPos = new BlockPos.MutableBlockPos();
        for (int i = pos.getX() - 6; i < pos.getX() + 6; ++i) {
            testPos.setX(i);
            for (int j = pos.getY() - 6; j < pos.getY() + 6; ++j) {
                testPos.setY(j);
                for (int k = pos.getZ() - 6; k < pos.getZ() + 6; ++k) {
                    testPos.setZ(k);
                    Block block = level.getBlockState((BlockPos)testPos).getBlock();
                    ++realValidBlocks;
                }
            }
        }
        int validBlocks = 0;
        for (int i = pos.getX() - 15; i < pos.getX() + 15; ++i) {
            testPos.setX(i);
            for (int j = pos.getY() - 15; j < pos.getY() + 15; ++j) {
                testPos.setY(j);
                for (int k = pos.getZ() - 15; k < pos.getZ() + 15; ++k) {
                    testPos.setZ(k);
                    ++validBlocks;
                }
            }
        }
        int minBlocks = 200;
        return validBlocks > 200 && realValidBlocks > 80;
    }
}

