/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.neoforged.neoforge.capabilities.ItemCapability
 */
package appeng.api.features;

import appeng.core.definitions.AEParts;
import appeng.items.parts.PartItem;
import appeng.parts.p2p.P2PTunnelPart;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.capabilities.ItemCapability;

public final class P2PTunnelAttunement {
    private static final int INITIAL_CAPACITY = 40;
    static final Map<TagKey<Item>, Item> tagTunnels = new IdentityHashMap<TagKey<Item>, Item>(40);
    static final List<ApiAttunement> apiAttunements = new ArrayList<ApiAttunement>(40);
    public static final ItemLike ME_TUNNEL = AEParts.ME_P2P_TUNNEL;
    public static final ItemLike ENERGY_TUNNEL = AEParts.FE_P2P_TUNNEL;
    public static final ItemLike REDSTONE_TUNNEL = AEParts.REDSTONE_P2P_TUNNEL;
    public static final ItemLike FLUID_TUNNEL = AEParts.FLUID_P2P_TUNNEL;
    public static final ItemLike ITEM_TUNNEL = AEParts.ITEM_P2P_TUNNEL;
    public static final ItemLike LIGHT_TUNNEL = AEParts.LIGHT_P2P_TUNNEL;

    private P2PTunnelAttunement() {
    }

    public static TagKey<Item> getAttunementTag(ItemLike tunnel) {
        Objects.requireNonNull(tunnel.asItem(), "tunnel.asItem()");
        ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey((Object)tunnel.asItem());
        if (itemKey.equals((Object)BuiltInRegistries.ITEM.getDefaultKey())) {
            throw new IllegalArgumentException("Tunnel item must be registered first.");
        }
        return TagKey.create((ResourceKey)Registries.ITEM, (ResourceLocation)ResourceLocation.fromNamespaceAndPath((String)itemKey.getNamespace(), (String)("p2p_attunements/" + itemKey.getPath())));
    }

    public static synchronized void registerAttunementTag(ItemLike tunnel) {
        Objects.requireNonNull(tunnel.asItem(), "tunnel.asItem()");
        tagTunnels.put(P2PTunnelAttunement.getAttunementTag(tunnel), P2PTunnelAttunement.validateTunnelPartItem(tunnel));
    }

    public static synchronized void registerAttunementApi(ItemLike tunnelPart, ItemCapability<?, Void> cap, Component description) {
        Objects.requireNonNull(cap, "cap");
        apiAttunements.add(new ApiAttunement(cap, P2PTunnelAttunement.validateTunnelPartItem(tunnelPart), description));
    }

    public static synchronized ItemStack getTunnelPartByTriggerItem(ItemStack trigger) {
        if (trigger.isEmpty()) {
            return ItemStack.EMPTY;
        }
        for (TagKey tag : trigger.getTags().toList()) {
            Item tagTunnelItem = tagTunnels.get(tag);
            if (tagTunnelItem == null) continue;
            return new ItemStack((ItemLike)tagTunnelItem);
        }
        for (ApiAttunement apiAttunement : apiAttunements) {
            if (!apiAttunement.hasApi(trigger)) continue;
            return new ItemStack((ItemLike)apiAttunement.tunnelType());
        }
        return ItemStack.EMPTY;
    }

    private static Item validateTunnelPartItem(ItemLike itemLike) {
        Objects.requireNonNull(itemLike, "item");
        Item item = itemLike.asItem();
        Objects.requireNonNull(item, "item");
        if (!(item instanceof PartItem)) {
            throw new IllegalArgumentException("Given tunnel part item is not a part");
        }
        PartItem partItem = (PartItem)item;
        if (!P2PTunnelPart.class.isAssignableFrom(partItem.getPartClass())) {
            throw new IllegalArgumentException("Given tunnel part item results in a part that is not a P2P tunnel: " + String.valueOf(partItem));
        }
        return item;
    }

    record ApiAttunement(ItemCapability<?, Void> capability, Item tunnelType, Component component) {
        public boolean hasApi(ItemStack stack) {
            return stack.getCapability(this.capability) != null;
        }
    }
}

