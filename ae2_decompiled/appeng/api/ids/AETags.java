/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.material.Fluid
 */
package appeng.api.ids;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public final class AETags {
    public static final TagKey<Block> SPATIAL_BLACKLIST = AETags.blockTag("ae2:blacklisted/spatial");
    public static final TagKey<Block> ANNIHILATION_PLANE_BLOCK_BLACKLIST = AETags.blockTag("ae2:blacklisted/annihilation_plane");
    public static final TagKey<Item> ANNIHILATION_PLANE_ITEM_BLACKLIST = AETags.itemTag("ae2:blacklisted/annihilation_plane");
    public static final TagKey<Fluid> ANNIHILATION_PLANE_FLUID_BLACKLIST = AETags.fluidTag("ae2:blacklisted/annihilation_plane");
    public static TagKey<Item> METAL_INGOTS = AETags.itemTag("ae2:metal_ingots");
    public static final TagKey<Block> FACADE_BLOCK_WHITELIST = AETags.blockTag("ae2:whitelisted/facades");
    public static final TagKey<Block> GROWTH_ACCELERATABLE = AETags.blockTag("ae2:growth_acceleratable");

    private AETags() {
    }

    private static TagKey<Item> itemTag(String name) {
        return TagKey.create((ResourceKey)Registries.ITEM, (ResourceLocation)ResourceLocation.parse((String)name));
    }

    private static TagKey<Fluid> fluidTag(String name) {
        return TagKey.create((ResourceKey)Registries.FLUID, (ResourceLocation)ResourceLocation.parse((String)name));
    }

    private static TagKey<Block> blockTag(String name) {
        return TagKey.create((ResourceKey)Registries.BLOCK, (ResourceLocation)ResourceLocation.parse((String)name));
    }
}

