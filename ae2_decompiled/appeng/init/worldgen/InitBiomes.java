/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderGetter
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.data.worldgen.BootstrapContext
 *  net.minecraft.world.level.biome.Biome
 *  net.minecraft.world.level.biome.Biome$BiomeBuilder
 *  net.minecraft.world.level.biome.BiomeGenerationSettings$Builder
 *  net.minecraft.world.level.biome.BiomeSpecialEffects$Builder
 *  net.minecraft.world.level.biome.MobSpawnSettings$Builder
 */
package appeng.init.worldgen;

import appeng.spatial.SpatialStorageDimensionIds;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;

public final class InitBiomes {
    private InitBiomes() {
    }

    public static void init(BootstrapContext<Biome> context) {
        HolderGetter placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter configuredCarvers = context.lookup(Registries.CONFIGURED_CARVER);
        Biome biome = new Biome.BiomeBuilder().generationSettings(new BiomeGenerationSettings.Builder(placedFeatures, configuredCarvers).build()).hasPrecipitation(false).temperature(0.5f).downfall(0.5f).specialEffects(new BiomeSpecialEffects.Builder().waterColor(4159204).waterFogColor(329011).fogColor(0).skyColor(0x111111).build()).mobSpawnSettings(new MobSpawnSettings.Builder().creatureGenerationProbability(0.0f).build()).build();
        context.register(SpatialStorageDimensionIds.BIOME_KEY, (Object)biome);
    }
}

