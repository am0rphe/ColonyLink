/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.component.DataComponentType
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.tags.BlockTags
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.item.DyeColor
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.level.biome.Biome
 *  net.minecraft.world.level.block.Block
 *  net.neoforged.neoforge.common.Tags$Biomes
 *  net.neoforged.neoforge.common.Tags$Blocks
 *  net.neoforged.neoforge.common.Tags$Items
 */
package appeng.datagen.providers.tags;

import appeng.core.AppEng;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;

public final class ConventionTags {
    public static final TagKey<DataComponentType<?>> EXPORTED_SETTINGS = TagKey.create((ResourceKey)Registries.DATA_COMPONENT_TYPE, (ResourceLocation)AppEng.makeId("exported_settings"));
    public static final TagKey<Item> DUSTS = Tags.Items.DUSTS;
    public static final TagKey<Item> GEMS = Tags.Items.GEMS;
    public static final TagKey<Item> SILICON = ConventionTags.tag("c:silicon");
    public static final TagKey<Item> ALL_QUARTZ = ConventionTags.tag("ae2:all_quartz");
    public static final TagKey<Item> ALL_QUARTZ_DUST = ConventionTags.tag("ae2:all_quartz_dust");
    public static final TagKey<Item> ALL_CERTUS_QUARTZ = ConventionTags.tag("ae2:all_certus_quartz");
    public static final TagKey<Item> CERTUS_QUARTZ = ConventionTags.tag("c:gems/certus_quartz");
    public static final TagKey<Block> CERTUS_QUARTZ_STORAGE_BLOCK_BLOCK = ConventionTags.blockTag("c:storage_blocks/certus_quartz");
    public static final TagKey<Item> CERTUS_QUARTZ_DUST = ConventionTags.tag("c:dusts/certus_quartz");
    public static final TagKey<Item> ALL_NETHER_QUARTZ = ConventionTags.tag("ae2:all_nether_quartz");
    public static final TagKey<Item> NETHER_QUARTZ = Tags.Items.GEMS_QUARTZ;
    public static final TagKey<Item> ALL_FLUIX = ConventionTags.tag("ae2:all_fluix");
    public static final TagKey<Item> FLUIX_DUST = ConventionTags.tag("c:dusts/fluix");
    public static final TagKey<Item> FLUIX_CRYSTAL = ConventionTags.tag("c:gems/fluix");
    public static final TagKey<Item> COPPER_INGOT = Tags.Items.INGOTS_COPPER;
    public static final TagKey<Item> GOLD_NUGGET = Tags.Items.NUGGETS_GOLD;
    public static final TagKey<Item> GOLD_INGOT = Tags.Items.INGOTS_GOLD;
    public static final TagKey<Item> IRON_NUGGET = Tags.Items.NUGGETS_IRON;
    public static final TagKey<Item> IRON_INGOT = Tags.Items.INGOTS_IRON;
    public static final TagKey<Item> DIAMOND = Tags.Items.GEMS_DIAMOND;
    public static final TagKey<Item> REDSTONE = Tags.Items.DUSTS_REDSTONE;
    public static final TagKey<Item> GLOWSTONE = Tags.Items.DUSTS_GLOWSTONE;
    public static final TagKey<Item> ENDER_PEARL = Tags.Items.ENDER_PEARLS;
    public static final TagKey<Item> ENDER_PEARL_DUST = ConventionTags.tag("c:dusts/ender_pearl");
    public static final TagKey<Item> SKY_STONE_DUST = ConventionTags.tag("c:dusts/sky_stone");
    public static final TagKey<Item> WOOD_STICK = Tags.Items.RODS_WOODEN;
    public static final TagKey<Item> CHEST = Tags.Items.CHESTS_WOODEN;
    public static final TagKey<Item> STONE = Tags.Items.STONES;
    public static final TagKey<Item> GLASS = Tags.Items.GLASS_BLOCKS;
    public static final TagKey<Item> GLASS_CHEAP = Tags.Items.GLASS_BLOCKS_CHEAP;
    public static final TagKey<Block> GLASS_BLOCK = Tags.Blocks.GLASS_BLOCKS;
    public static final TagKey<Item> GLASS_CABLE = ConventionTags.tag("ae2:glass_cable");
    public static final TagKey<Item> SMART_CABLE = ConventionTags.tag("ae2:smart_cable");
    public static final TagKey<Item> COVERED_CABLE = ConventionTags.tag("ae2:covered_cable");
    public static final TagKey<Item> COVERED_DENSE_CABLE = ConventionTags.tag("ae2:covered_dense_cable");
    public static final TagKey<Item> SMART_DENSE_CABLE = ConventionTags.tag("ae2:smart_dense_cable");
    public static final TagKey<Item> ILLUMINATED_PANEL = ConventionTags.tag("ae2:illuminated_panel");
    public static final TagKey<Item> INTERFACE = ConventionTags.tag("ae2:interface");
    public static final TagKey<Item> PATTERN_PROVIDER = ConventionTags.tag("ae2:pattern_provider");
    public static final TagKey<Item> QUARTZ_AXE = ConventionTags.tag("ae2:quartz_axe");
    public static final TagKey<Item> QUARTZ_HOE = ConventionTags.tag("ae2:quartz_hoe");
    public static final TagKey<Item> QUARTZ_PICK = ConventionTags.tag("ae2:quartz_pickaxe");
    public static final TagKey<Item> QUARTZ_SHOVEL = ConventionTags.tag("ae2:quartz_shovel");
    public static final TagKey<Item> QUARTZ_SWORD = ConventionTags.tag("ae2:quartz_sword");
    public static final TagKey<Item> QUARTZ_WRENCH = ConventionTags.tag("ae2:quartz_wrench");
    public static final TagKey<Item> QUARTZ_KNIFE = ConventionTags.tag("ae2:knife");
    public static final TagKey<Item> PAINT_BALLS = ConventionTags.tag("ae2:paint_balls");
    public static final TagKey<Item> LUMEN_PAINT_BALLS = ConventionTags.tag("ae2:lumen_paint_balls");
    public static final TagKey<Item> INSCRIBER_PRESSES = ConventionTags.tag("ae2:inscriber_presses");
    public static final TagKey<Item> CAN_REMOVE_COLOR = ConventionTags.tag("ae2:can_remove_color");
    public static final TagKey<Item> BUDDING_BLOCKS = Tags.Items.BUDDING_BLOCKS;
    public static final TagKey<Item> BUDS = Tags.Items.BUDS;
    public static final TagKey<Item> CLUSTERS = Tags.Items.CLUSTERS;
    public static final TagKey<Block> BUDDING_BLOCKS_BLOCKS = Tags.Blocks.BUDDING_BLOCKS;
    public static final TagKey<Block> BUDS_BLOCKS = Tags.Blocks.BUDS;
    public static final TagKey<Block> CLUSTERS_BLOCKS = Tags.Blocks.CLUSTERS;
    public static final TagKey<Block> CROPS = BlockTags.CROPS;
    public static final TagKey<Block> SAPLINGS = BlockTags.SAPLINGS;
    public static final TagKey<Block> IMMOVABLE_BLOCKS = Tags.Blocks.RELOCATION_NOT_SUPPORTED;
    public static final TagKey<Biome> METEORITE_OCEAN = Tags.Biomes.IS_OCEAN;
    public static final TagKey<Item> WRENCH = ConventionTags.tag("c:tools/wrench");
    public static final Map<DyeColor, TagKey<Item>> DYES = Arrays.stream(DyeColor.values()).collect(Collectors.toMap(Function.identity(), dye -> ConventionTags.tag("c:dyes/" + dye.getSerializedName())));
    public static final TagKey<Item> CURIOS = ConventionTags.tag("curios:curio");

    private ConventionTags() {
    }

    public static TagKey<Item> dye(DyeColor color) {
        return DYES.get(color);
    }

    private static TagKey<Item> tag(String name) {
        return TagKey.create((ResourceKey)Registries.ITEM, (ResourceLocation)ResourceLocation.parse((String)name));
    }

    private static TagKey<Block> blockTag(String name) {
        return TagKey.create((ResourceKey)Registries.BLOCK, (ResourceLocation)ResourceLocation.parse((String)name));
    }
}

