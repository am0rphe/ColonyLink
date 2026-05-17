/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.data.PackOutput
 *  net.minecraft.data.tags.FluidTagsProvider
 *  net.neoforged.neoforge.common.data.ExistingFileHelper
 */
package appeng.datagen.providers.tags;

import appeng.api.ids.AETags;
import appeng.datagen.providers.IAE2DataProvider;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class FluidTagsProvider
extends net.minecraft.data.tags.FluidTagsProvider
implements IAE2DataProvider {
    public FluidTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper existingFileHelper) {
        super(output, registries, "ae2", existingFileHelper);
    }

    protected void addTags(HolderLookup.Provider registries) {
        this.tag(AETags.ANNIHILATION_PLANE_FLUID_BLACKLIST);
    }
}

