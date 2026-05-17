/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.data.PackOutput
 *  net.minecraft.data.tags.PoiTypeTagsProvider
 *  net.minecraft.tags.PoiTypeTags
 *  net.neoforged.neoforge.common.data.ExistingFileHelper
 */
package appeng.datagen.providers.tags;

import appeng.datagen.providers.IAE2DataProvider;
import appeng.init.InitVillager;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.PoiTypeTags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class PoiTypeTagsProvider
extends net.minecraft.data.tags.PoiTypeTagsProvider
implements IAE2DataProvider {
    public PoiTypeTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper existingFileHelper) {
        super(packOutput, registries, "ae2", existingFileHelper);
    }

    protected void addTags(HolderLookup.Provider registries) {
        this.tag(PoiTypeTags.ACQUIRABLE_JOB_SITE).add(InitVillager.POI_KEY);
    }
}

