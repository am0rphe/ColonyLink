/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.data.PackOutput
 *  net.minecraft.data.tags.BiomeTagsProvider
 *  net.minecraft.tags.BiomeTags
 *  net.neoforged.neoforge.common.data.ExistingFileHelper
 */
package appeng.datagen.providers.tags;

import appeng.datagen.providers.IAE2DataProvider;
import appeng.worldgen.meteorite.MeteoriteStructure;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BiomeTags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class BiomeTagsProvider
extends net.minecraft.data.tags.BiomeTagsProvider
implements IAE2DataProvider {
    public BiomeTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper existingFileHelper) {
        super(packOutput, registries, "ae2", existingFileHelper);
    }

    protected void addTags(HolderLookup.Provider registries) {
        this.tag(MeteoriteStructure.BIOME_TAG_KEY).addOptionalTag(BiomeTags.IS_OVERWORLD.location());
    }
}

