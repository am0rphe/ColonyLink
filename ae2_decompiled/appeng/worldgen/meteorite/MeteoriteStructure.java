/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.math.StatsAccumulator
 *  com.mojang.serialization.MapCodec
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.BlockPos$MutableBlockPos
 *  net.minecraft.core.Holder
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.tags.TagKey
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.level.ChunkPos
 *  net.minecraft.world.level.LevelHeightAccessor
 *  net.minecraft.world.level.biome.Biome
 *  net.minecraft.world.level.chunk.ChunkGenerator
 *  net.minecraft.world.level.levelgen.Heightmap$Types
 *  net.minecraft.world.level.levelgen.LegacyRandomSource
 *  net.minecraft.world.level.levelgen.WorldgenRandom
 *  net.minecraft.world.level.levelgen.structure.Structure
 *  net.minecraft.world.level.levelgen.structure.Structure$GenerationContext
 *  net.minecraft.world.level.levelgen.structure.Structure$GenerationStub
 *  net.minecraft.world.level.levelgen.structure.Structure$StructureSettings
 *  net.minecraft.world.level.levelgen.structure.StructurePiece
 *  net.minecraft.world.level.levelgen.structure.StructureSet
 *  net.minecraft.world.level.levelgen.structure.StructureType
 *  net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder
 */
package appeng.worldgen.meteorite;

import appeng.core.AppEng;
import appeng.datagen.providers.tags.ConventionTags;
import appeng.worldgen.meteorite.CraterType;
import appeng.worldgen.meteorite.MeteoriteStructurePiece;
import appeng.worldgen.meteorite.fallout.FalloutMode;
import com.google.common.math.StatsAccumulator;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class MeteoriteStructure
extends Structure {
    public static final ResourceLocation ID = AppEng.makeId("meteorite");
    public static final ResourceKey<StructureSet> STRUCTURE_SET_KEY = ResourceKey.create((ResourceKey)Registries.STRUCTURE_SET, (ResourceLocation)ID);
    public static final MapCodec<MeteoriteStructure> CODEC = MeteoriteStructure.simpleCodec(MeteoriteStructure::new);
    public static final ResourceKey<Structure> KEY = ResourceKey.create((ResourceKey)Registries.STRUCTURE, (ResourceLocation)ID);
    public static final TagKey<Biome> BIOME_TAG_KEY = TagKey.create((ResourceKey)Registries.BIOME, (ResourceLocation)AppEng.makeId("has_meteorites"));
    public static StructureType<MeteoriteStructure> TYPE = () -> CODEC;

    public MeteoriteStructure(Structure.StructureSettings settings) {
        super(settings);
    }

    public StructureType<?> type() {
        return TYPE;
    }

    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
        WorldgenRandom worldgenRandom = new WorldgenRandom((RandomSource)new LegacyRandomSource(0L));
        worldgenRandom.setLargeFeatureSeed(context.seed(), context.chunkPos().x, context.chunkPos().z);
        if (!worldgenRandom.nextBoolean()) {
            return Optional.empty();
        }
        return MeteoriteStructure.onTopOfChunkCenter((Structure.GenerationContext)context, (Heightmap.Types)Heightmap.Types.OCEAN_FLOOR_WG, structurePiecesBuilder -> MeteoriteStructure.generatePieces(structurePiecesBuilder, context));
    }

    private static void generatePieces(StructurePiecesBuilder piecesBuilder, Structure.GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        WorldgenRandom random = context.random();
        LevelHeightAccessor heightAccessor = context.heightAccessor();
        ChunkGenerator generator = context.chunkGenerator();
        int centerX = chunkPos.getMinBlockX() + random.nextInt(16);
        int centerZ = chunkPos.getMinBlockZ() + random.nextInt(16);
        float meteoriteRadius = random.nextFloat() * 6.0f + 2.0f;
        int yOffset = (int)Math.ceil(meteoriteRadius) + 1;
        Set t2 = generator.getBiomeSource().getBiomesWithin(centerX, generator.getSeaLevel(), centerZ, 0, context.randomState().sampler());
        Holder spawnBiome = (Holder)t2.stream().findFirst().orElseThrow();
        boolean isOcean = spawnBiome.is(ConventionTags.METEORITE_OCEAN);
        Heightmap.Types heightmapType = isOcean ? Heightmap.Types.OCEAN_FLOOR_WG : Heightmap.Types.WORLD_SURFACE_WG;
        StatsAccumulator stats = new StatsAccumulator();
        int scanRadius = (int)Math.max(1.0f, meteoriteRadius * 2.0f);
        for (int x = -scanRadius; x <= scanRadius; ++x) {
            for (int z = -scanRadius; z <= scanRadius; ++z) {
                int h = generator.getBaseHeight(centerX + x, centerZ + z, heightmapType, heightAccessor, context.randomState());
                stats.add((double)h);
            }
        }
        int centerY = (int)stats.mean();
        if (stats.populationVariance() > 5.0) {
            centerY = (int)((double)centerY - (stats.mean() - stats.min()) * 0.75);
        }
        centerY -= yOffset;
        centerY = Math.max(heightAccessor.getMinBuildHeight() + yOffset, centerY);
        BlockPos actualPos = new BlockPos(centerX, centerY, centerZ);
        boolean craterLake = MeteoriteStructure.locateWaterAroundTheCrater(actualPos, meteoriteRadius, context);
        CraterType craterType = MeteoriteStructure.determineCraterType(actualPos, (Holder<Biome>)spawnBiome, random);
        boolean pureCrater = random.nextFloat() > 0.9f;
        FalloutMode fallout = FalloutMode.fromBiome((Holder<Biome>)spawnBiome);
        piecesBuilder.addPiece((StructurePiece)new MeteoriteStructurePiece(actualPos, meteoriteRadius, craterType, fallout, pureCrater, craterLake));
    }

    private static boolean locateWaterAroundTheCrater(BlockPos pos, float radius, Structure.GenerationContext context) {
        ChunkGenerator generator = context.chunkGenerator();
        LevelHeightAccessor heightAccessor = context.heightAccessor();
        int seaLevel = generator.getSeaLevel();
        int maxY = seaLevel - 1;
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
        blockPos.setY(maxY);
        for (int i = pos.getX() - 32; i <= pos.getX() + 32; ++i) {
            blockPos.setX(i);
            for (int k = pos.getZ() - 32; k <= pos.getZ() + 32; ++k) {
                int heigth;
                blockPos.setZ(k);
                double dx = i - pos.getX();
                double dz = k - pos.getZ();
                double h = (float)pos.getY() - radius + 1.0f;
                double distanceFrom = dx * dx + dz * dz;
                if (!((double)maxY > h + distanceFrom * 0.0175) || !((double)maxY < h + distanceFrom * 0.02) || (heigth = generator.getBaseHeight(blockPos.getX(), blockPos.getZ(), Heightmap.Types.OCEAN_FLOOR, heightAccessor, context.randomState())) >= seaLevel) continue;
                return true;
            }
        }
        return false;
    }

    private static CraterType determineCraterType(BlockPos pos, Holder<Biome> biomeHolder, WorldgenRandom random) {
        boolean lake;
        boolean specialMeteor;
        Biome biome = (Biome)biomeHolder.value();
        float temp = biome.getBaseTemperature();
        if (biomeHolder.is(ConventionTags.METEORITE_OCEAN)) {
            return CraterType.NONE;
        }
        boolean bl = specialMeteor = random.nextFloat() > 0.5f;
        if (!specialMeteor) {
            return CraterType.NORMAL;
        }
        boolean canSnow = biome.coldEnoughToSnow(pos);
        if (temp >= 1.0f) {
            boolean lava;
            boolean bl2 = lava = random.nextFloat() > 0.5f;
            if (!biome.hasPrecipitation()) {
                return lava ? CraterType.LAVA : CraterType.NORMAL;
            }
            if (!canSnow) {
                boolean obsidian = random.nextFloat() > 0.75f;
                CraterType alternativObsidian = obsidian ? CraterType.OBSIDIAN : CraterType.LAVA;
                return lava ? alternativObsidian : CraterType.NORMAL;
            }
        }
        if (temp < 1.0f && (double)temp >= 0.2) {
            boolean lava;
            lake = random.nextFloat() > 0.25f;
            boolean bl3 = lava = random.nextFloat() > 0.8f;
            if (!biome.hasPrecipitation()) {
                return lava ? CraterType.LAVA : CraterType.NORMAL;
            }
            if (!canSnow) {
                boolean obsidian = random.nextFloat() > 0.75f;
                CraterType alternativObsidian = obsidian ? CraterType.OBSIDIAN : CraterType.LAVA;
                CraterType craterLake = lake ? CraterType.WATER : CraterType.NORMAL;
                return lava ? alternativObsidian : craterLake;
            }
            boolean snow = random.nextFloat() > 0.75f;
            CraterType water = lake ? CraterType.WATER : CraterType.NORMAL;
            return snow ? CraterType.SNOW : water;
        }
        if ((double)temp < 0.2) {
            boolean frozen;
            lake = random.nextFloat() > 0.25f;
            boolean lava = random.nextFloat() > 0.95f;
            boolean bl4 = frozen = random.nextFloat() > 0.25f;
            if (!biome.hasPrecipitation()) {
                return lava ? CraterType.LAVA : CraterType.NORMAL;
            }
            if (!canSnow) {
                CraterType frozenLake = frozen ? CraterType.ICE : CraterType.WATER;
                CraterType craterLake = lake ? frozenLake : CraterType.NORMAL;
                return lava ? CraterType.LAVA : craterLake;
            }
            CraterType snowCovered = lake ? CraterType.SNOW : CraterType.NORMAL;
            return lava ? CraterType.LAVA : snowCovered;
        }
        return CraterType.NORMAL;
    }
}

