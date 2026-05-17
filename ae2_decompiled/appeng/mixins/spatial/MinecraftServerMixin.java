/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  net.minecraft.core.Holder
 *  net.minecraft.core.HolderGetter
 *  net.minecraft.core.LayeredRegistryAccess
 *  net.minecraft.core.RegistryAccess$Frozen
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.RegistryLayer
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.progress.ChunkProgressListener
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.biome.Biome
 *  net.minecraft.world.level.biome.BiomeManager
 *  net.minecraft.world.level.chunk.ChunkGenerator
 *  net.minecraft.world.level.dimension.LevelStem
 *  net.minecraft.world.level.storage.DerivedLevelData
 *  net.minecraft.world.level.storage.LevelStorageSource$LevelStorageAccess
 *  net.minecraft.world.level.storage.ServerLevelData
 *  net.minecraft.world.level.storage.WorldData
 *  net.neoforged.bus.api.Event
 *  net.neoforged.neoforge.common.NeoForge
 *  net.neoforged.neoforge.event.level.LevelEvent$Load
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package appeng.mixins.spatial;

import appeng.spatial.SpatialStorageChunkGenerator;
import appeng.spatial.SpatialStorageDimensionIds;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={MinecraftServer.class})
public abstract class MinecraftServerMixin {
    @Shadow
    private Map<ResourceKey<Level>, ServerLevel> levels;
    @Shadow
    protected WorldData worldData;
    @Shadow
    protected Executor executor;
    @Shadow
    protected LevelStorageSource.LevelStorageAccess storageSource;
    @Shadow
    protected LayeredRegistryAccess<RegistryLayer> registries;

    @Inject(method={"createLevels"}, at={@At(value="TAIL")})
    public void injectSpatialLevel(ChunkProgressListener chunkProgressListener, CallbackInfo ci) {
        RegistryAccess.Frozen registryHolder = this.registries.compositeAccess();
        LevelStem levelStem = new LevelStem((Holder)registryHolder.registryOrThrow(Registries.DIMENSION_TYPE).getHolderOrThrow(SpatialStorageDimensionIds.DIMENSION_TYPE_ID), (ChunkGenerator)new SpatialStorageChunkGenerator((HolderGetter<Biome>)registryHolder.lookupOrThrow(Registries.BIOME)));
        long seed = BiomeManager.obfuscateSeed((long)this.worldData.worldGenOptions().seed());
        ServerLevelData serverLevelData = this.worldData.overworldData();
        DerivedLevelData derivedLevelData = new DerivedLevelData(this.worldData, serverLevelData);
        ServerLevel level = new ServerLevel((MinecraftServer)this, this.executor, this.storageSource, (ServerLevelData)derivedLevelData, SpatialStorageDimensionIds.WORLD_ID, levelStem, chunkProgressListener, false, seed, (List)ImmutableList.of(), false, null);
        this.levels.put(SpatialStorageDimensionIds.WORLD_ID, level);
        NeoForge.EVENT_BUS.post((Event)new LevelEvent.Load((LevelAccessor)level));
    }
}

