/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  net.minecraft.core.Holder
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.data.loot.BlockLootSubProvider
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.flag.FeatureFlags
 *  net.minecraft.world.item.enchantment.Enchantment
 *  net.minecraft.world.item.enchantment.Enchantments
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.storage.loot.LootPool
 *  net.minecraft.world.level.storage.loot.LootPool$Builder
 *  net.minecraft.world.level.storage.loot.LootTable
 *  net.minecraft.world.level.storage.loot.LootTable$Builder
 *  net.minecraft.world.level.storage.loot.entries.LootItem
 *  net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer$Builder
 *  net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer$Builder
 *  net.minecraft.world.level.storage.loot.entries.TagEntry
 *  net.minecraft.world.level.storage.loot.functions.ApplyBonusCount
 *  net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay
 *  net.minecraft.world.level.storage.loot.functions.LootItemFunction$Builder
 *  net.minecraft.world.level.storage.loot.functions.SetItemCountFunction
 *  net.minecraft.world.level.storage.loot.predicates.ExplosionCondition
 *  net.minecraft.world.level.storage.loot.providers.number.ConstantValue
 *  net.minecraft.world.level.storage.loot.providers.number.NumberProvider
 *  org.jetbrains.annotations.NotNull
 */
package appeng.datagen.providers.loot;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.BlockDefinition;
import appeng.datagen.providers.tags.ConventionTags;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.entries.TagEntry;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.NotNull;

public class BlockDropProvider
extends BlockLootSubProvider {
    private final Map<Block, Function<Block, LootTable.Builder>> overrides = this.createOverrides();

    @NotNull
    private ImmutableMap<Block, Function<Block, LootTable.Builder>> createOverrides() {
        return ImmutableMap.builder().put((Object)AEBlocks.MATRIX_FRAME.block(), $ -> LootTable.lootTable()).put((Object)AEBlocks.MYSTERIOUS_CUBE.block(), this::mysteriousCube).put((Object)AEBlocks.FLAWLESS_BUDDING_QUARTZ.block(), this.flawlessBuddingQuartz()).put((Object)AEBlocks.FLAWED_BUDDING_QUARTZ.block(), this.buddingQuartz(AEBlocks.CHIPPED_BUDDING_QUARTZ)).put((Object)AEBlocks.CHIPPED_BUDDING_QUARTZ.block(), this.buddingQuartz(AEBlocks.DAMAGED_BUDDING_QUARTZ)).put((Object)AEBlocks.DAMAGED_BUDDING_QUARTZ.block(), this.buddingQuartz(AEBlocks.QUARTZ_BLOCK)).put((Object)AEBlocks.SMALL_QUARTZ_BUD.block(), this::quartzBud).put((Object)AEBlocks.MEDIUM_QUARTZ_BUD.block(), this::quartzBud).put((Object)AEBlocks.LARGE_QUARTZ_BUD.block(), this::quartzBud).put((Object)AEBlocks.QUARTZ_CLUSTER.block(), this::quartzCluster).build();
    }

    public BlockDropProvider(HolderLookup.Provider providers) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), providers);
    }

    protected Iterable<Block> getKnownBlocks() {
        return BuiltInRegistries.BLOCK.stream().filter(entry -> entry.getLootTable().location().getNamespace().equals("ae2")).toList();
    }

    public void generate() {
        for (Block block : this.getKnownBlocks()) {
            this.add(block, this.overrides.getOrDefault(block, this::defaultBuilder).apply(block));
        }
    }

    private LootTable.Builder defaultBuilder(Block block) {
        LootPoolSingletonContainer.Builder entry = LootItem.lootTableItem((ItemLike)block);
        LootPool.Builder pool = LootPool.lootPool().setRolls((NumberProvider)ConstantValue.exactly((float)1.0f)).add((LootPoolEntryContainer.Builder)entry).when(ExplosionCondition.survivesExplosion());
        return LootTable.lootTable().withPool(pool);
    }

    private Function<Block, LootTable.Builder> flawlessBuddingQuartz() {
        return b -> this.createSingleItemTable((ItemLike)AEBlocks.FLAWED_BUDDING_QUARTZ.block());
    }

    private Function<Block, LootTable.Builder> buddingQuartz(BlockDefinition<?> degradedVersion) {
        return b -> this.createSingleItemTableWithSilkTouch((Block)b, degradedVersion);
    }

    private LootTable.Builder quartzBud(Block bud) {
        return this.createSingleItemTableWithSilkTouch(bud, AEItems.CERTUS_QUARTZ_DUST);
    }

    private LootTable.Builder quartzCluster(Block cluster) {
        return this.createSilkTouchDispatchTable(cluster, (LootPoolEntryContainer.Builder)LootItem.lootTableItem(AEItems.CERTUS_QUARTZ_CRYSTAL).apply((LootItemFunction.Builder)SetItemCountFunction.setCount((NumberProvider)ConstantValue.exactly((float)4.0f))).apply((LootItemFunction.Builder)ApplyBonusCount.addUniformBonusCount(this.getEnchantment((ResourceKey<Enchantment>)Enchantments.FORTUNE))).apply((LootItemFunction.Builder)ApplyExplosionDecay.explosionDecay()));
    }

    private LootTable.Builder mysteriousCube(Block block) {
        return this.createSilkTouchDispatchTable(block, TagEntry.tagContents(ConventionTags.INSCRIBER_PRESSES).when(ExplosionCondition.survivesExplosion())).withPool(LootPool.lootPool().when(this.doesNotHaveSilkTouch()).setRolls((NumberProvider)ConstantValue.exactly((float)1.0f)).add((LootPoolEntryContainer.Builder)LootItem.lootTableItem(AEItems.TABLET)));
    }

    protected final Holder<Enchantment> getEnchantment(ResourceKey<Enchantment> key) {
        return this.registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
    }
}

