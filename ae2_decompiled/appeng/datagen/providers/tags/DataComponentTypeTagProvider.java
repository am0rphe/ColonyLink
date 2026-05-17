/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.component.DataComponentType
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.data.PackOutput
 *  net.minecraft.data.tags.TagsProvider
 *  net.minecraft.resources.ResourceKey
 *  net.neoforged.neoforge.common.data.ExistingFileHelper
 *  net.neoforged.neoforge.registries.DeferredHolder
 *  org.jetbrains.annotations.Nullable
 */
package appeng.datagen.providers.tags;

import appeng.api.ids.AEComponents;
import appeng.datagen.providers.localization.LocalizationProvider;
import appeng.datagen.providers.tags.ConventionTags;
import appeng.items.tools.MemoryCardItem;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Nullable;

public class DataComponentTypeTagProvider
extends TagsProvider<DataComponentType<?>> {
    private final LocalizationProvider localization;
    private final HashSet<DataComponentType<?>> translated = new HashSet();

    public DataComponentTypeTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, @Nullable ExistingFileHelper existingFileHelper, LocalizationProvider localization) {
        super(output, Registries.DATA_COMPONENT_TYPE, registries, "ae2", existingFileHelper);
        this.localization = localization;
    }

    protected void addTags(HolderLookup.Provider registries) {
        IdentityHashMap<DataComponentType, ResourceKey> componentKeys = new IdentityHashMap<DataComponentType, ResourceKey>();
        for (DeferredHolder entry : AEComponents.DR.getEntries()) {
            componentKeys.put((DataComponentType)entry.get(), entry.getKey());
        }
        this.addExportedComponentCategory("Filter", AEComponents.EXPORTED_CONFIG_INV);
        this.addExportedComponentCategory("Patterns", AEComponents.EXPORTED_PATTERNS);
        this.addExportedComponentCategory("Custom Name", AEComponents.EXPORTED_CUSTOM_NAME);
        this.addExportedComponentCategory("Level Emitter Value", AEComponents.EXPORTED_LEVEL_EMITTER_VALUE);
        this.addExportedComponentCategory("P2P Frequency", AEComponents.EXPORTED_P2P_FREQUENCY);
        this.addExportedComponentCategory("P2P Type", AEComponents.EXPORTED_P2P_TYPE);
        this.addExportedComponentCategory("Priority", AEComponents.EXPORTED_PRIORITY);
        this.addExportedComponentCategory("Push Direction", AEComponents.EXPORTED_PUSH_DIRECTION);
        this.addExportedComponentCategory("Settings", AEComponents.EXPORTED_SETTINGS);
        this.addExportedComponentCategory("Upgrades", AEComponents.EXPORTED_UPGRADES);
        this.tag(ConventionTags.EXPORTED_SETTINGS).add(BuiltInRegistries.DATA_COMPONENT_TYPE.wrapAsHolder(AEComponents.EXPORTED_SETTINGS_SOURCE).getKey());
    }

    private void addExportedComponentCategory(String englishCategoryName, DataComponentType<?> ... types) {
        for (DataComponentType<?> type : types) {
            this.translated.add(type);
            ResourceKey key = (ResourceKey)BuiltInRegistries.DATA_COMPONENT_TYPE.getResourceKey(type).get();
            this.tag(ConventionTags.EXPORTED_SETTINGS).add(key);
            this.localization.add(MemoryCardItem.getSettingTranslationKey(type), englishCategoryName);
        }
    }
}

