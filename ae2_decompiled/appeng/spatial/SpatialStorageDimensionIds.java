/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.biome.Biome
 *  net.minecraft.world.level.dimension.DimensionType
 *  net.minecraft.world.level.dimension.LevelStem
 */
package appeng.spatial;

import appeng.core.AppEng;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

public final class SpatialStorageDimensionIds {
    public static final ResourceKey<DimensionType> DIMENSION_TYPE_ID = ResourceKey.create((ResourceKey)Registries.DIMENSION_TYPE, (ResourceLocation)AppEng.makeId("spatial_storage"));
    public static final ResourceLocation CHUNK_GENERATOR_ID = AppEng.makeId("spatial_storage");
    public static final ResourceKey<Biome> BIOME_KEY = ResourceKey.create((ResourceKey)Registries.BIOME, (ResourceLocation)AppEng.makeId("spatial_storage"));
    public static final ResourceKey<LevelStem> DIMENSION_ID = ResourceKey.create((ResourceKey)Registries.LEVEL_STEM, (ResourceLocation)AppEng.makeId("spatial_storage"));
    public static final ResourceKey<Level> WORLD_ID = ResourceKey.create((ResourceKey)Registries.DIMENSION, (ResourceLocation)AppEng.makeId("spatial_storage"));
    public static ResourceLocation SKY_PROPERTIES_ID = AppEng.makeId("spatial_storage");

    private SpatialStorageDimensionIds() {
    }
}

