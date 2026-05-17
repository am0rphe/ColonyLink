/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Registry
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.stats.StatFormatter
 *  net.minecraft.stats.Stats
 */
package appeng.init;

import appeng.core.stats.AeStats;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;

public final class InitStats {
    private InitStats() {
    }

    public static void init(Registry<ResourceLocation> registry) {
        for (AeStats stat : AeStats.values()) {
            ResourceLocation registryName = stat.getRegistryName();
            Registry.register(registry, (String)registryName.getPath(), (Object)registryName);
            Stats.CUSTOM.get((Object)registryName, StatFormatter.DEFAULT);
        }
    }
}

