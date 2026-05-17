/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.BlockPos$MutableBlockPos
 *  net.minecraft.core.Holder
 *  net.minecraft.core.HolderGetter
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.RegistryOps
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.server.level.WorldGenRegion
 *  net.minecraft.world.level.LevelHeightAccessor
 *  net.minecraft.world.level.NoiseColumn
 *  net.minecraft.world.level.StructureManager
 *  net.minecraft.world.level.biome.Biome
 *  net.minecraft.world.level.biome.BiomeManager
 *  net.minecraft.world.level.biome.BiomeSource
 *  net.minecraft.world.level.biome.FixedBiomeSource
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.chunk.ChunkAccess
 *  net.minecraft.world.level.chunk.ChunkGenerator
 *  net.minecraft.world.level.levelgen.GenerationStep$Carving
 *  net.minecraft.world.level.levelgen.Heightmap$Types
 *  net.minecraft.world.level.levelgen.RandomState
 *  net.minecraft.world.level.levelgen.blending.Blender
 */
package appeng.spatial;

import appeng.core.definitions.AEBlocks;
import appeng.spatial.SpatialStorageDimensionIds;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;

public class SpatialStorageChunkGenerator
extends ChunkGenerator {
    public static final int MIN_Y = 0;
    public static final int HEIGHT = 256;
    public static final MapCodec<SpatialStorageChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)RegistryOps.retrieveGetter((ResourceKey)Registries.BIOME)).apply((Applicative)instance, instance.stable(SpatialStorageChunkGenerator::new)));
    private final NoiseColumn columnSample;
    private final BlockState defaultBlockState = AEBlocks.MATRIX_FRAME.block().defaultBlockState();

    public SpatialStorageChunkGenerator(HolderGetter<Biome> biomeRegistry) {
        super((BiomeSource)SpatialStorageChunkGenerator.createBiomeSource(biomeRegistry));
        Object[] columnSample = new BlockState[256];
        Arrays.fill(columnSample, this.defaultBlockState);
        this.columnSample = new NoiseColumn(0, (BlockState[])columnSample);
    }

    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    private static FixedBiomeSource createBiomeSource(HolderGetter<Biome> biomeRegistry) {
        return new FixedBiomeSource((Holder)biomeRegistry.getOrThrow(SpatialStorageDimensionIds.BIOME_KEY));
    }

    public int getGenDepth() {
        return 256;
    }

    public int getMinY() {
        return 0;
    }

    public void buildSurface(WorldGenRegion worldGenRegion, StructureManager structureManager, RandomState randomState, ChunkAccess chunk) {
        this.fillChunk(chunk);
        chunk.setUnsaved(false);
    }

    private void fillChunk(ChunkAccess chunk) {
        BlockPos.MutableBlockPos mutPos = new BlockPos.MutableBlockPos();
        for (int cx = 0; cx < 16; ++cx) {
            mutPos.setX(cx);
            for (int cz = 0; cz < 16; ++cz) {
                mutPos.setZ(cz);
                for (int cy = 0; cy < 256; ++cy) {
                    mutPos.setY(cy);
                    chunk.setBlockState((BlockPos)mutPos, this.defaultBlockState, false);
                }
            }
        }
    }

    public int getSeaLevel() {
        return 0;
    }

    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunk) {
        return CompletableFuture.completedFuture(chunk);
    }

    public int getBaseHeight(int i, int j, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        return 0;
    }

    public NoiseColumn getBaseColumn(int i, int j, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        return this.columnSample;
    }

    public void addDebugScreenInfo(List<String> list, RandomState randomState, BlockPos blockPos) {
    }

    public void applyCarvers(WorldGenRegion worldGenRegion, long l, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunkAccess, GenerationStep.Carving carving) {
    }

    public void spawnOriginalMobs(WorldGenRegion level) {
    }
}

