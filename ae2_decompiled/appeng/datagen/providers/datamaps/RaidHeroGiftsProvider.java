/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.data.PackOutput
 *  net.neoforged.neoforge.common.conditions.ICondition
 *  net.neoforged.neoforge.common.data.DataMapProvider
 *  net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps
 *  net.neoforged.neoforge.registries.datamaps.builtin.RaidHeroGift
 */
package appeng.datagen.providers.datamaps;

import appeng.datagen.providers.IAE2DataProvider;
import appeng.init.InitVillager;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import net.neoforged.neoforge.registries.datamaps.builtin.RaidHeroGift;

public class RaidHeroGiftsProvider
extends DataMapProvider
implements IAE2DataProvider {
    public RaidHeroGiftsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    public void gather(HolderLookup.Provider provider) {
        this.builder(NeoForgeDataMaps.RAID_HERO_GIFTS).add(InitVillager.ID, (Object)new RaidHeroGift(InitVillager.LOOT_TABLE_KEY), false, new ICondition[0]);
    }
}

