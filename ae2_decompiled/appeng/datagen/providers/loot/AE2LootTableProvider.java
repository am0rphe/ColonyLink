/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.WritableRegistry
 *  net.minecraft.data.PackOutput
 *  net.minecraft.data.loot.LootTableProvider
 *  net.minecraft.data.loot.LootTableProvider$SubProviderEntry
 *  net.minecraft.util.ProblemReporter$Collector
 *  net.minecraft.world.level.storage.loot.LootTable
 *  net.minecraft.world.level.storage.loot.ValidationContext
 *  net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
 */
package appeng.datagen.providers.loot;

import appeng.datagen.providers.loot.BlockDropProvider;
import appeng.datagen.providers.loot.RaidHeroGiftLootProvider;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.WritableRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public class AE2LootTableProvider
extends LootTableProvider {
    private static final List<LootTableProvider.SubProviderEntry> SUB_PROVIDERS = List.of(new LootTableProvider.SubProviderEntry(BlockDropProvider::new, LootContextParamSets.BLOCK), new LootTableProvider.SubProviderEntry(RaidHeroGiftLootProvider::new, LootContextParamSets.GIFT));

    public AE2LootTableProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> provider) {
        super(packOutput, Set.of(), SUB_PROVIDERS, provider);
    }

    protected void validate(WritableRegistry<LootTable> writableregistry, ValidationContext validationcontext, ProblemReporter.Collector collector) {
    }
}

