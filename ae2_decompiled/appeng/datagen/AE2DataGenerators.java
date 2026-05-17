/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.RegistrySetBuilder
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.data.DataGenerator
 *  net.minecraft.data.DataGenerator$PackGenerator
 *  net.minecraft.data.DataProvider
 *  net.minecraft.data.DataProvider$Factory
 *  net.minecraft.data.PackOutput
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.fml.common.EventBusSubscriber$Bus
 *  net.neoforged.neoforge.common.data.AdvancementProvider
 *  net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider
 *  net.neoforged.neoforge.common.data.ExistingFileHelper
 *  net.neoforged.neoforge.data.event.GatherDataEvent
 */
package appeng.datagen;

import appeng.core.definitions.AEDamageTypes;
import appeng.datagen.providers.advancements.AdvancementGenerator;
import appeng.datagen.providers.datamaps.RaidHeroGiftsProvider;
import appeng.datagen.providers.localization.LocalizationProvider;
import appeng.datagen.providers.loot.AE2LootTableProvider;
import appeng.datagen.providers.models.BlockModelProvider;
import appeng.datagen.providers.models.CableModelProvider;
import appeng.datagen.providers.models.DecorationModelProvider;
import appeng.datagen.providers.models.ItemModelProvider;
import appeng.datagen.providers.models.PartModelProvider;
import appeng.datagen.providers.recipes.ChargerRecipes;
import appeng.datagen.providers.recipes.CraftingRecipes;
import appeng.datagen.providers.recipes.DecorationBlockRecipes;
import appeng.datagen.providers.recipes.DecorationRecipes;
import appeng.datagen.providers.recipes.EntropyRecipes;
import appeng.datagen.providers.recipes.InscriberRecipes;
import appeng.datagen.providers.recipes.MatterCannonAmmoProvider;
import appeng.datagen.providers.recipes.QuartzCuttingRecipesProvider;
import appeng.datagen.providers.recipes.SmeltingRecipes;
import appeng.datagen.providers.recipes.SmithingRecipes;
import appeng.datagen.providers.recipes.TransformRecipes;
import appeng.datagen.providers.recipes.UpgradeRecipes;
import appeng.datagen.providers.tags.BiomeTagsProvider;
import appeng.datagen.providers.tags.BlockTagsProvider;
import appeng.datagen.providers.tags.DataComponentTypeTagProvider;
import appeng.datagen.providers.tags.FluidTagsProvider;
import appeng.datagen.providers.tags.ItemTagsProvider;
import appeng.datagen.providers.tags.PoiTypeTagsProvider;
import appeng.init.worldgen.InitBiomes;
import appeng.init.worldgen.InitDimensionTypes;
import appeng.init.worldgen.InitStructures;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid="ae2", bus=EventBusSubscriber.Bus.MOD)
public class AE2DataGenerators {
    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        CompletableFuture registries = event.getLookupProvider();
        LocalizationProvider localization = new LocalizationProvider(generator);
        DataGenerator.PackGenerator pack = generator.getVanillaPack(true);
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        pack.addProvider(output -> new DatapackBuiltinEntriesProvider(output, registries, AE2DataGenerators.createDatapackEntriesBuilder(), Set.of("ae2")));
        pack.addProvider(packOutput -> new AE2LootTableProvider(packOutput, registries));
        BlockTagsProvider blockTagsProvider = (BlockTagsProvider)pack.addProvider(packOutput -> new BlockTagsProvider(packOutput, registries, existingFileHelper));
        pack.addProvider(packOutput -> new ItemTagsProvider(packOutput, registries, blockTagsProvider.contentsGetter(), existingFileHelper));
        pack.addProvider(packOutput -> new FluidTagsProvider(packOutput, registries, existingFileHelper));
        pack.addProvider(packOutput -> new BiomeTagsProvider(packOutput, registries, existingFileHelper));
        pack.addProvider(packOutput -> new PoiTypeTagsProvider(packOutput, registries, existingFileHelper));
        pack.addProvider(packOutput -> new DataComponentTypeTagProvider(packOutput, registries, existingFileHelper, localization));
        pack.addProvider(packOutput -> new BlockModelProvider(packOutput, existingFileHelper));
        pack.addProvider(packOutput -> new DecorationModelProvider(packOutput, existingFileHelper));
        pack.addProvider(packOutput -> new ItemModelProvider(packOutput, existingFileHelper));
        pack.addProvider(packOutput -> new CableModelProvider(packOutput, existingFileHelper));
        pack.addProvider(packOutput -> new PartModelProvider(packOutput, existingFileHelper));
        pack.addProvider(packOutput -> new AdvancementProvider(packOutput, registries, existingFileHelper, List.of(new AdvancementGenerator(localization))));
        pack.addProvider(AE2DataGenerators.bindRegistries(DecorationRecipes::new, registries));
        pack.addProvider(AE2DataGenerators.bindRegistries(DecorationBlockRecipes::new, registries));
        pack.addProvider(AE2DataGenerators.bindRegistries(MatterCannonAmmoProvider::new, registries));
        pack.addProvider(AE2DataGenerators.bindRegistries(EntropyRecipes::new, registries));
        pack.addProvider(AE2DataGenerators.bindRegistries(InscriberRecipes::new, registries));
        pack.addProvider(AE2DataGenerators.bindRegistries(SmeltingRecipes::new, registries));
        pack.addProvider(AE2DataGenerators.bindRegistries(CraftingRecipes::new, registries));
        pack.addProvider(AE2DataGenerators.bindRegistries(SmithingRecipes::new, registries));
        pack.addProvider(AE2DataGenerators.bindRegistries(TransformRecipes::new, registries));
        pack.addProvider(AE2DataGenerators.bindRegistries(ChargerRecipes::new, registries));
        pack.addProvider(AE2DataGenerators.bindRegistries(QuartzCuttingRecipesProvider::new, registries));
        pack.addProvider(AE2DataGenerators.bindRegistries(UpgradeRecipes::new, registries));
        pack.addProvider(AE2DataGenerators.bindRegistries(RaidHeroGiftsProvider::new, registries));
        pack.addProvider(packOutput -> localization);
    }

    private static RegistrySetBuilder createDatapackEntriesBuilder() {
        return new RegistrySetBuilder().add(Registries.DIMENSION_TYPE, InitDimensionTypes::init).add(Registries.STRUCTURE, InitStructures::initDatagenStructures).add(Registries.STRUCTURE_SET, InitStructures::initDatagenStructureSets).add(Registries.BIOME, InitBiomes::init).add(Registries.DAMAGE_TYPE, AEDamageTypes::init);
    }

    private static <T extends DataProvider> DataProvider.Factory<T> bindRegistries(BiFunction<PackOutput, CompletableFuture<HolderLookup.Provider>, T> factory, CompletableFuture<HolderLookup.Provider> factories) {
        return packOutput -> (DataProvider)factory.apply(packOutput, factories);
    }
}

