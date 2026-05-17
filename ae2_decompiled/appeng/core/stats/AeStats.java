/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.player.Player
 */
package appeng.core.stats;

import appeng.core.AppEng;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public enum AeStats {
    ItemsInserted("items_inserted"),
    ItemsExtracted("items_extracted");

    private final ResourceLocation registryName;

    private AeStats(String id) {
        this.registryName = AppEng.makeId(id);
    }

    public void addToPlayer(Player player, int howMany) {
        player.awardStat(this.registryName, howMany);
    }

    public ResourceLocation getRegistryName() {
        return this.registryName;
    }
}

