/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.data.loot.LootTableSubProvider
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.level.storage.loot.LootPool
 *  net.minecraft.world.level.storage.loot.LootTable
 *  net.minecraft.world.level.storage.loot.LootTable$Builder
 *  net.minecraft.world.level.storage.loot.entries.LootItem
 *  net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer$Builder
 *  net.minecraft.world.level.storage.loot.providers.number.ConstantValue
 *  net.minecraft.world.level.storage.loot.providers.number.NumberProvider
 */
package appeng.datagen.providers.loot;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.init.InitVillager;
import java.util.function.BiConsumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public class RaidHeroGiftLootProvider
implements LootTableSubProvider {
    public RaidHeroGiftLootProvider(HolderLookup.Provider lookupProvider) {
    }

    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> biConsumer) {
        biConsumer.accept(InitVillager.LOOT_TABLE_KEY, LootTable.lootTable().withPool(LootPool.lootPool().setRolls((NumberProvider)ConstantValue.exactly((float)1.0f)).add((LootPoolEntryContainer.Builder)LootItem.lootTableItem(AEItems.CERTUS_QUARTZ_CRYSTAL)).add((LootPoolEntryContainer.Builder)LootItem.lootTableItem(AEItems.FLUIX_CRYSTAL)).add((LootPoolEntryContainer.Builder)LootItem.lootTableItem(AEBlocks.SKY_STONE_BLOCK))));
    }
}

