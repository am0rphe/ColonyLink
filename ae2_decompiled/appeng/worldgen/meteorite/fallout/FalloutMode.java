/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  net.minecraft.core.Holder
 *  net.minecraft.tags.BiomeTags
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.level.biome.Biome
 *  net.neoforged.neoforge.common.Tags$Biomes
 */
package appeng.worldgen.meteorite.fallout;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.Tags;

public enum FalloutMode {
    NONE(new TagKey[0]),
    DEFAULT(new TagKey[0]),
    SAND(Tags.Biomes.IS_SANDY, BiomeTags.IS_BEACH),
    TERRACOTTA(BiomeTags.IS_BADLANDS),
    ICE_SNOW(Tags.Biomes.IS_COLD);

    private final List<TagKey<Biome>> biomeTags;

    @SafeVarargs
    private FalloutMode(TagKey<Biome> ... biomeTags) {
        this.biomeTags = ImmutableList.copyOf((Object[])biomeTags);
    }

    public boolean matches(Holder<Biome> biome) {
        for (TagKey<Biome> biomeTag : this.biomeTags) {
            if (!biome.is(biomeTag)) continue;
            return true;
        }
        return false;
    }

    public static FalloutMode fromBiome(Holder<Biome> biome) {
        for (FalloutMode mode : FalloutMode.values()) {
            if (!mode.matches(biome)) continue;
            return mode;
        }
        return DEFAULT;
    }
}

