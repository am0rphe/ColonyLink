/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.Encoder
 *  com.mojang.serialization.JsonOps
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.HolderLookup$RegistryLookup
 *  net.minecraft.data.CachedOutput
 *  net.minecraft.data.DataProvider
 *  net.minecraft.data.PackOutput
 *  net.minecraft.data.PackOutput$PathProvider
 *  net.minecraft.data.PackOutput$Target
 *  net.minecraft.resources.RegistryDataLoader
 *  net.minecraft.resources.RegistryDataLoader$RegistryData
 *  net.minecraft.resources.RegistryOps
 *  net.minecraft.resources.ResourceKey
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package appeng.datagen.providers;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorldGenProvider
implements DataProvider {
    private static final Logger LOG = LoggerFactory.getLogger(WorldGenProvider.class);
    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> registries;

    public WorldGenProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        this.output = output;
        this.registries = registries;
    }

    public CompletableFuture<?> run(CachedOutput writer) {
        return this.registries.thenComposeAsync(provider -> {
            RegistryOps dynamicOps = RegistryOps.create((DynamicOps)JsonOps.INSTANCE, (HolderLookup.Provider)provider);
            CompletableFuture[] futures = (CompletableFuture[])RegistryDataLoader.WORLDGEN_REGISTRIES.stream().map(info -> this.writeRegistryEntries(writer, (HolderLookup.Provider)provider, (DynamicOps<JsonElement>)dynamicOps, (RegistryDataLoader.RegistryData)info)).toArray(CompletableFuture[]::new);
            return CompletableFuture.allOf(futures);
        });
    }

    private <T> CompletableFuture<Void> writeRegistryEntries(CachedOutput writer, HolderLookup.Provider registries, DynamicOps<JsonElement> ops, RegistryDataLoader.RegistryData<T> registryData) {
        ResourceKey registryKey = registryData.key();
        HolderLookup.RegistryLookup registry = registries.lookup(registryKey).orElse(null);
        if (registry == null) {
            return CompletableFuture.completedFuture(null);
        }
        PackOutput.PathProvider pathResolver = this.output.createPathProvider(PackOutput.Target.DATA_PACK, registryKey.location().getPath());
        CompletableFuture[] futures = (CompletableFuture[])registry.listElements().flatMap(regEntry -> {
            ResourceKey key = regEntry.key();
            if (!key.location().getNamespace().equals("ae2")) {
                return Stream.empty();
            }
            Path path = pathResolver.json(key.location());
            return WorldGenProvider.writeToPath(path, writer, ops, registryData.elementCodec(), regEntry.value()).stream();
        }).toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(futures);
    }

    private static <E> Optional<CompletableFuture<?>> writeToPath(Path path, CachedOutput cache, DynamicOps<JsonElement> json, Encoder<E> encoder, E value) {
        Optional optional = encoder.encodeStart(json, value).resultOrPartial(error -> LOG.error("Couldn't serialize element {}: {}", (Object)path, error));
        return optional.map(data -> DataProvider.saveStable((CachedOutput)cache, (JsonElement)data, (Path)path));
    }

    public String getName() {
        return "AE2 Worldgen";
    }
}

