/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  net.minecraft.core.Registry
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.world.entity.ai.village.poi.PoiType
 *  net.minecraft.world.entity.npc.VillagerProfession
 *  net.minecraft.world.entity.npc.VillagerTrades$EmeraldForItems
 *  net.minecraft.world.entity.npc.VillagerTrades$ItemListing
 *  net.minecraft.world.entity.npc.VillagerTrades$ItemsForEmeralds
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.storage.loot.LootTable
 *  net.neoforged.neoforge.event.village.VillagerTradesEvent
 */
package appeng.init;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

public class InitVillager {
    public static final ResourceLocation ID = AppEng.makeId("fluix_researcher");
    public static PoiType POI_TYPE = new PoiType(Set.copyOf(AEBlocks.CHARGER.block().getStateDefinition().getPossibleStates()), 1, 1);
    public static final ResourceKey<PoiType> POI_KEY = ResourceKey.create((ResourceKey)Registries.POINT_OF_INTEREST_TYPE, (ResourceLocation)ID);
    public static final VillagerProfession PROFESSION = new VillagerProfession(ID.toString(), e -> e.is(POI_KEY), e -> e.is(POI_KEY), ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_LIBRARIAN);
    public static final ResourceKey<LootTable> LOOT_TABLE_KEY = ResourceKey.create((ResourceKey)Registries.LOOT_TABLE, (ResourceLocation)AppEng.makeId("gameplay/hero_of_the_village/fluix_researcher_gifts"));

    private InitVillager() {
    }

    public static void initProfession(Registry<VillagerProfession> registry) {
        Registry.register(registry, (ResourceLocation)ID, (Object)PROFESSION);
    }

    public static void initPointOfInterestType(Registry<PoiType> registry) {
        Registry.register(registry, (ResourceLocation)ID, (Object)POI_TYPE);
    }

    public static void initTrades(VillagerTradesEvent event) {
        if (!event.getType().name().equals(PROFESSION.name())) {
            return;
        }
        Int2ObjectMap trades = event.getTrades();
        InitVillager.buyItems((Int2ObjectMap<List<VillagerTrades.ItemListing>>)trades, 1, AEItems.CERTUS_QUARTZ_CRYSTAL, 3, 4, 10);
        InitVillager.buyItems((Int2ObjectMap<List<VillagerTrades.ItemListing>>)trades, 1, AEItems.METEORITE_COMPASS, 2, 1, 5);
        InitVillager.sellItems((Int2ObjectMap<List<VillagerTrades.ItemListing>>)trades, 2, AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED, 3, 10, 15);
        InitVillager.sellItems((Int2ObjectMap<List<VillagerTrades.ItemListing>>)trades, 2, AEItems.SILICON, 5, 8, 13);
        InitVillager.buyItems((Int2ObjectMap<List<VillagerTrades.ItemListing>>)trades, 2, AEBlocks.SKY_STONE_BLOCK, 5, 8, 20);
        InitVillager.sellItems((Int2ObjectMap<List<VillagerTrades.ItemListing>>)trades, 3, AEBlocks.QUARTZ_GLASS, 2, 10, 10);
        InitVillager.buyItems((Int2ObjectMap<List<VillagerTrades.ItemListing>>)trades, 3, AEItems.FLUIX_CRYSTAL, 5, 4, 14);
        InitVillager.sellItems((Int2ObjectMap<List<VillagerTrades.ItemListing>>)trades, 4, AEItems.MATTER_BALL, 5, 8, 12);
        InitVillager.buyItems((Int2ObjectMap<List<VillagerTrades.ItemListing>>)trades, 4, AEItems.CALCULATION_PROCESSOR_PRESS, 10, 1, 20);
        InitVillager.buyItems((Int2ObjectMap<List<VillagerTrades.ItemListing>>)trades, 4, AEItems.ENGINEERING_PROCESSOR_PRESS, 10, 1, 20);
        InitVillager.buyItems((Int2ObjectMap<List<VillagerTrades.ItemListing>>)trades, 4, AEItems.LOGIC_PROCESSOR_PRESS, 10, 1, 20);
        InitVillager.buyItems((Int2ObjectMap<List<VillagerTrades.ItemListing>>)trades, 4, AEItems.SILICON_PRESS, 10, 1, 20);
        InitVillager.buyItems((Int2ObjectMap<List<VillagerTrades.ItemListing>>)trades, 5, (ItemLike)Items.SLIME_BALL, 8, 5, 12);
    }

    private static void sellItems(Int2ObjectMap<List<VillagerTrades.ItemListing>> trades, int minLevel, ItemLike soldItem, int numberOfItems, int maxUses, int xp) {
        InitVillager.addOffers(trades, minLevel, new VillagerTrades.ItemListing[]{new VillagerTrades.EmeraldForItems(soldItem, numberOfItems, maxUses, xp)});
    }

    private static void buyItems(Int2ObjectMap<List<VillagerTrades.ItemListing>> trades, int minLevel, ItemLike boughtItem, int emeraldCost, int numberOfItems, int xp) {
        InitVillager.addOffers(trades, minLevel, new VillagerTrades.ItemListing[]{new VillagerTrades.ItemsForEmeralds(boughtItem.asItem(), emeraldCost, numberOfItems, xp)});
    }

    private static void addOffers(Int2ObjectMap<List<VillagerTrades.ItemListing>> offersByLevel, int minLevel, VillagerTrades.ItemListing ... newOffers) {
        List entries = (List)offersByLevel.computeIfAbsent(minLevel, key -> new ArrayList());
        Collections.addAll(entries, newOffers);
        offersByLevel.put(minLevel, (Object)entries);
    }
}

