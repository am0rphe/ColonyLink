/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.neoforged.neoforge.capabilities.ItemCapability
 */
package appeng.api.features;

import appeng.api.features.P2PTunnelAttunement;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.capabilities.ItemCapability;

public final class P2PTunnelAttunementInternal {
    private P2PTunnelAttunementInternal() {
    }

    public static AttunementInfo getAttunementInfo(ItemLike tunnelType) {
        Item tunnelItem = tunnelType.asItem();
        HashSet caps = new HashSet();
        for (P2PTunnelAttunement.ApiAttunement entry : P2PTunnelAttunement.apiAttunements) {
            if (entry.tunnelType() != tunnelItem) continue;
            caps.add(entry.capability());
        }
        return new AttunementInfo(caps);
    }

    public static List<Resultant> getApiTunnels() {
        return P2PTunnelAttunement.apiAttunements.stream().map(info -> new Resultant(info.component(), info.tunnelType(), info::hasApi)).toList();
    }

    public static Map<TagKey<Item>, Item> getTagTunnels() {
        return P2PTunnelAttunement.tagTunnels;
    }

    public record AttunementInfo(Set<ItemCapability<?, Void>> apis) {
    }

    public record Resultant(Component description, Item tunnelType, Predicate<ItemStack> stackPredicate) {
    }
}

