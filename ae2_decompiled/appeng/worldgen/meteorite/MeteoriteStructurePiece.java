/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.level.ChunkPos
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.StructureManager
 *  net.minecraft.world.level.WorldGenLevel
 *  net.minecraft.world.level.chunk.ChunkGenerator
 *  net.minecraft.world.level.levelgen.structure.BoundingBox
 *  net.minecraft.world.level.levelgen.structure.StructurePiece
 *  net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext
 *  net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType
 *  net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType$ContextlessType
 */
package appeng.worldgen.meteorite;

import appeng.server.services.compass.ServerCompassService;
import appeng.worldgen.meteorite.CraterType;
import appeng.worldgen.meteorite.MeteoritePlacer;
import appeng.worldgen.meteorite.PlacedMeteoriteSettings;
import appeng.worldgen.meteorite.fallout.FalloutMode;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

public class MeteoriteStructurePiece
extends StructurePiece {
    public static final StructurePieceType.ContextlessType TYPE = MeteoriteStructurePiece::new;
    private final PlacedMeteoriteSettings settings;

    protected MeteoriteStructurePiece(BlockPos center, float coreRadius, CraterType craterType, FalloutMode fallout, boolean pureCrater, boolean craterLake) {
        super((StructurePieceType)TYPE, 0, MeteoriteStructurePiece.createBoundingBox(center));
        this.settings = new PlacedMeteoriteSettings(center, coreRadius, craterType, fallout, pureCrater, craterLake);
    }

    private static BoundingBox createBoundingBox(BlockPos origin) {
        int range = 64;
        ChunkPos chunkPos = new ChunkPos(origin);
        return new BoundingBox(chunkPos.getMinBlockX() - range, origin.getY(), chunkPos.getMinBlockZ() - range, chunkPos.getMaxBlockX() + range, origin.getY(), chunkPos.getMaxBlockZ() + range);
    }

    public MeteoriteStructurePiece(CompoundTag tag) {
        super((StructurePieceType)TYPE, tag);
        BlockPos center = BlockPos.of((long)tag.getLong("c"));
        float coreRadius = tag.getFloat("r");
        CraterType craterType = CraterType.values()[tag.getByte("t")];
        FalloutMode fallout = FalloutMode.values()[tag.getByte("f")];
        boolean pureCrater = tag.getBoolean("p");
        boolean craterLake = tag.getBoolean("l");
        this.settings = new PlacedMeteoriteSettings(center, coreRadius, craterType, fallout, pureCrater, craterLake);
    }

    public boolean isFinalized() {
        return this.settings.getCraterType() != null;
    }

    public PlacedMeteoriteSettings getSettings() {
        return this.settings;
    }

    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putFloat("r", this.settings.getMeteoriteRadius());
        tag.putLong("c", this.settings.getPos().asLong());
        tag.putByte("t", (byte)this.settings.getCraterType().ordinal());
        tag.putByte("f", (byte)this.settings.getFallout().ordinal());
        tag.putBoolean("p", this.settings.isPureCrater());
        tag.putBoolean("l", this.settings.isCraterLake());
    }

    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource rand, BoundingBox bounds, ChunkPos chunkPos, BlockPos blockPos) {
        MeteoritePlacer.place((LevelAccessor)level, this.settings, bounds, rand);
        ServerCompassService.updateArea(level.getLevel(), level.getChunk(chunkPos.x, chunkPos.z));
    }
}

