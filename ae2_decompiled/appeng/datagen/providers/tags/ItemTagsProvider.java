/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.data.PackOutput
 *  net.minecraft.data.tags.ItemTagsProvider
 *  net.minecraft.data.tags.TagsProvider$TagLookup
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.tags.BlockTags
 *  net.minecraft.tags.ItemTags
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.block.Block
 *  net.neoforged.neoforge.common.Tags$Blocks
 *  net.neoforged.neoforge.common.Tags$Items
 *  net.neoforged.neoforge.common.data.ExistingFileHelper
 */
package appeng.datagen.providers.tags;

import appeng.api.features.P2PTunnelAttunement;
import appeng.api.ids.AETags;
import appeng.api.util.AEColor;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.datagen.providers.IAE2DataProvider;
import appeng.datagen.providers.tags.ConventionTags;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ItemTagsProvider
extends net.minecraft.data.tags.ItemTagsProvider
implements IAE2DataProvider {
    public ItemTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries, CompletableFuture<TagsProvider.TagLookup<Block>> blockTagsProvider, ExistingFileHelper existingFileHelper) {
        super(packOutput, registries, blockTagsProvider, "ae2", existingFileHelper);
    }

    protected void addTags(HolderLookup.Provider registries) {
        this.copyBlockTags();
        this.tag(ItemTags.DURABILITY_ENCHANTABLE).add(AEParts.ANNIHILATION_PLANE.asItem());
        this.tag(ItemTags.MINING_ENCHANTABLE).add(AEParts.ANNIHILATION_PLANE.asItem());
        this.tag(ItemTags.MINING_LOOT_ENCHANTABLE).add(AEParts.ANNIHILATION_PLANE.asItem());
        this.tag(AETags.ANNIHILATION_PLANE_ITEM_BLACKLIST);
        this.tag(ConventionTags.BUDDING_BLOCKS).add((Object)AEBlocks.FLAWLESS_BUDDING_QUARTZ.asItem()).add((Object)AEBlocks.FLAWED_BUDDING_QUARTZ.asItem()).add((Object)AEBlocks.CHIPPED_BUDDING_QUARTZ.asItem()).add((Object)AEBlocks.DAMAGED_BUDDING_QUARTZ.asItem());
        this.tag(ConventionTags.BUDS).add((Object)AEBlocks.SMALL_QUARTZ_BUD.asItem()).add((Object)AEBlocks.MEDIUM_QUARTZ_BUD.asItem()).add((Object)AEBlocks.LARGE_QUARTZ_BUD.asItem());
        this.tag(ConventionTags.CLUSTERS).add((Object)AEBlocks.QUARTZ_CLUSTER.asItem());
        this.tag(ConventionTags.CERTUS_QUARTZ_DUST).add((Object)AEItems.CERTUS_QUARTZ_DUST.asItem());
        this.tag(ConventionTags.ENDER_PEARL_DUST).add((Object)AEItems.ENDER_DUST.asItem());
        this.tag(ConventionTags.SKY_STONE_DUST).add((Object)AEItems.SKY_DUST.asItem());
        this.tag(ConventionTags.ALL_QUARTZ_DUST).addTag(ConventionTags.CERTUS_QUARTZ_DUST);
        this.tag(ConventionTags.ALL_CERTUS_QUARTZ).addTag(ConventionTags.CERTUS_QUARTZ).add((Object)AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED.asItem());
        this.tag(ConventionTags.ALL_FLUIX).add((Object)AEItems.FLUIX_CRYSTAL.asItem());
        this.tag(ConventionTags.ALL_NETHER_QUARTZ).addTag(ConventionTags.NETHER_QUARTZ);
        this.tag(ConventionTags.ALL_QUARTZ).addTag(ConventionTags.NETHER_QUARTZ).addTag(ConventionTags.CERTUS_QUARTZ).add((Object)AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED.asItem());
        for (AEColor color : AEColor.values()) {
            this.tag(ConventionTags.SMART_DENSE_CABLE).add(AEParts.SMART_DENSE_CABLE.item(color));
            this.tag(ConventionTags.SMART_CABLE).add(AEParts.SMART_CABLE.item(color));
            this.tag(ConventionTags.GLASS_CABLE).add(AEParts.GLASS_CABLE.item(color));
            this.tag(ConventionTags.COVERED_CABLE).add(AEParts.COVERED_CABLE.item(color));
            this.tag(ConventionTags.COVERED_DENSE_CABLE).add(AEParts.COVERED_DENSE_CABLE.item(color));
        }
        this.tag(ConventionTags.INSCRIBER_PRESSES).add((Object)AEItems.CALCULATION_PROCESSOR_PRESS.asItem()).add((Object)AEItems.ENGINEERING_PROCESSOR_PRESS.asItem()).add((Object)AEItems.LOGIC_PROCESSOR_PRESS.asItem()).add((Object)AEItems.SILICON_PRESS.asItem());
        for (AEColor color : AEColor.VALID_COLORS) {
            this.tag(ConventionTags.PAINT_BALLS).add((Object)AEItems.COLORED_PAINT_BALL.item(color));
            this.tag(ConventionTags.LUMEN_PAINT_BALLS).add((Object)AEItems.COLORED_LUMEN_PAINT_BALL.item(color));
        }
        this.tag(ConventionTags.SILICON).add((Object)AEItems.SILICON.asItem());
        this.tag(ConventionTags.QUARTZ_AXE).add((Object)AEItems.CERTUS_QUARTZ_AXE.asItem()).add((Object)AEItems.NETHER_QUARTZ_AXE.asItem());
        this.tag(ConventionTags.QUARTZ_HOE).add((Object)AEItems.CERTUS_QUARTZ_HOE.asItem()).add((Object)AEItems.NETHER_QUARTZ_HOE.asItem());
        this.tag(ConventionTags.QUARTZ_PICK).add((Object)AEItems.CERTUS_QUARTZ_PICK.asItem()).add((Object)AEItems.NETHER_QUARTZ_PICK.asItem());
        this.tag(ConventionTags.QUARTZ_SHOVEL).add((Object)AEItems.CERTUS_QUARTZ_SHOVEL.asItem()).add((Object)AEItems.NETHER_QUARTZ_SHOVEL.asItem());
        this.tag(ConventionTags.QUARTZ_SWORD).add((Object)AEItems.CERTUS_QUARTZ_SWORD.asItem()).add((Object)AEItems.NETHER_QUARTZ_SWORD.asItem());
        this.tag(ConventionTags.QUARTZ_WRENCH).add((Object)AEItems.CERTUS_QUARTZ_WRENCH.asItem()).add((Object)AEItems.NETHER_QUARTZ_WRENCH.asItem());
        this.tag(ConventionTags.QUARTZ_KNIFE).add((Object)AEItems.CERTUS_QUARTZ_KNIFE.asItem()).add((Object)AEItems.NETHER_QUARTZ_KNIFE.asItem());
        this.tag(ItemTags.AXES).add((Object)AEItems.CERTUS_QUARTZ_AXE.asItem()).add((Object)AEItems.NETHER_QUARTZ_AXE.asItem()).add((Object)AEItems.FLUIX_AXE.asItem());
        this.tag(ItemTags.HOES).add((Object)AEItems.CERTUS_QUARTZ_HOE.asItem()).add((Object)AEItems.NETHER_QUARTZ_HOE.asItem()).add((Object)AEItems.FLUIX_HOE.asItem());
        this.tag(ItemTags.PICKAXES).add((Object)AEItems.CERTUS_QUARTZ_PICK.asItem()).add((Object)AEItems.NETHER_QUARTZ_PICK.asItem()).add((Object)AEItems.FLUIX_PICK.asItem());
        this.tag(ItemTags.SHOVELS).add((Object)AEItems.CERTUS_QUARTZ_SHOVEL.asItem()).add((Object)AEItems.NETHER_QUARTZ_SHOVEL.asItem()).add((Object)AEItems.FLUIX_SHOVEL.asItem());
        this.tag(ItemTags.SWORDS).add((Object)AEItems.CERTUS_QUARTZ_SWORD.asItem()).add((Object)AEItems.NETHER_QUARTZ_SWORD.asItem()).add((Object)AEItems.FLUIX_SWORD.asItem());
        this.tag(ConventionTags.WRENCH).add((Object[])new Item[]{AEItems.CERTUS_QUARTZ_WRENCH.asItem(), AEItems.NETHER_QUARTZ_WRENCH.asItem(), AEItems.NETWORK_TOOL.asItem()});
        this.tag(AETags.METAL_INGOTS).addOptionalTag(ResourceLocation.parse((String)"c:ingots/copper")).addOptionalTag(ResourceLocation.parse((String)"c:ingots/tin")).addOptionalTag(ResourceLocation.parse((String)"c:ingots/iron")).addOptionalTag(ResourceLocation.parse((String)"c:ingots/gold")).addOptionalTag(ResourceLocation.parse((String)"c:ingots/brass")).addOptionalTag(ResourceLocation.parse((String)"c:ingots/nickel")).addOptionalTag(ResourceLocation.parse((String)"c:ingots/aluminium"));
        this.tag(ConventionTags.PATTERN_PROVIDER).add(AEParts.PATTERN_PROVIDER.asItem()).add((Object)AEBlocks.PATTERN_PROVIDER.asItem());
        this.tag(ConventionTags.INTERFACE).add(AEParts.INTERFACE.asItem()).add((Object)AEBlocks.INTERFACE.asItem());
        this.tag(ConventionTags.ILLUMINATED_PANEL).add(AEParts.MONITOR.asItem()).add(AEParts.SEMI_DARK_MONITOR.asItem()).add(AEParts.DARK_MONITOR.asItem());
        this.tag(ConventionTags.FLUIX_DUST).add((Object)AEItems.FLUIX_DUST.asItem());
        this.tag(ConventionTags.CERTUS_QUARTZ_DUST).add((Object)AEItems.CERTUS_QUARTZ_DUST.asItem());
        this.tag(ConventionTags.FLUIX_CRYSTAL).add((Object)AEItems.FLUIX_CRYSTAL.asItem());
        this.tag(ConventionTags.CERTUS_QUARTZ).add((Object)AEItems.CERTUS_QUARTZ_CRYSTAL.asItem()).add((Object)AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED.asItem());
        this.tag(ConventionTags.DUSTS).add((Object)AEItems.CERTUS_QUARTZ_DUST.asItem()).add((Object)AEItems.ENDER_DUST.asItem()).add((Object)AEItems.FLUIX_DUST.asItem()).add((Object)AEItems.SKY_DUST.asItem());
        this.tag(ConventionTags.GEMS).add((Object)AEItems.CERTUS_QUARTZ_CRYSTAL.asItem()).add((Object)AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED.asItem()).add((Object)AEItems.FLUIX_CRYSTAL.asItem());
        this.tag(ConventionTags.WRENCH).add((Object[])new Item[]{AEItems.CERTUS_QUARTZ_WRENCH.asItem(), AEItems.NETHER_QUARTZ_WRENCH.asItem(), AEItems.NETWORK_TOOL.asItem()});
        this.tag(ConventionTags.CURIOS).add((Object[])new Item[]{AEItems.WIRELESS_TERMINAL.asItem(), AEItems.WIRELESS_CRAFTING_TERMINAL.asItem(), AEItems.PORTABLE_ITEM_CELL1K.asItem(), AEItems.PORTABLE_ITEM_CELL4K.asItem(), AEItems.PORTABLE_ITEM_CELL16K.asItem(), AEItems.PORTABLE_ITEM_CELL64K.asItem(), AEItems.PORTABLE_ITEM_CELL256K.asItem(), AEItems.PORTABLE_FLUID_CELL1K.asItem(), AEItems.PORTABLE_FLUID_CELL4K.asItem(), AEItems.PORTABLE_FLUID_CELL16K.asItem(), AEItems.PORTABLE_FLUID_CELL64K.asItem(), AEItems.PORTABLE_FLUID_CELL256K.asItem()});
        this.tag(ConventionTags.CAN_REMOVE_COLOR).add((Object[])new Item[]{Items.WATER_BUCKET, Items.SNOWBALL});
        this.tag(ConventionTags.WRENCH).addOptional(ResourceLocation.parse((String)"immersiveengineering:hammer"));
        this.addP2pAttunementTags();
    }

    private void copyBlockTags() {
        this.mirrorBlockTag(Tags.Blocks.STORAGE_BLOCKS.location());
        this.mirrorBlockTag(ResourceLocation.parse((String)"c:storage_blocks/certus_quartz"));
        this.copy(BlockTags.WALLS, ItemTags.WALLS);
        this.copy(Tags.Blocks.CHESTS, Tags.Items.CHESTS);
        this.copy(ConventionTags.GLASS_BLOCK, ConventionTags.GLASS);
    }

    private void mirrorBlockTag(ResourceLocation tagName) {
        this.copy(TagKey.create((ResourceKey)Registries.BLOCK, (ResourceLocation)tagName), TagKey.create((ResourceKey)Registries.ITEM, (ResourceLocation)tagName));
    }

    private void addP2pAttunementTags() {
        this.tag(P2PTunnelAttunement.getAttunementTag(P2PTunnelAttunement.LIGHT_TUNNEL)).add((Object[])new Item[]{Items.TORCH, Items.GLOWSTONE});
        this.tag(P2PTunnelAttunement.getAttunementTag(P2PTunnelAttunement.ENERGY_TUNNEL)).add((Object[])new Item[]{AEBlocks.DENSE_ENERGY_CELL.asItem(), AEBlocks.ENERGY_ACCEPTOR.asItem(), AEBlocks.ENERGY_CELL.asItem(), AEBlocks.CREATIVE_ENERGY_CELL.asItem()});
        this.tag(P2PTunnelAttunement.getAttunementTag(P2PTunnelAttunement.REDSTONE_TUNNEL)).add((Object[])new Item[]{Items.REDSTONE, Items.REPEATER, Items.REDSTONE_LAMP, Items.COMPARATOR, Items.DAYLIGHT_DETECTOR, Items.REDSTONE_TORCH, Items.REDSTONE_BLOCK, Items.LEVER});
        this.tag(P2PTunnelAttunement.getAttunementTag(P2PTunnelAttunement.ITEM_TUNNEL)).add((Object[])new Item[]{AEParts.STORAGE_BUS.asItem(), AEParts.EXPORT_BUS.asItem(), AEParts.IMPORT_BUS.asItem(), Items.HOPPER, Items.CHEST, Items.TRAPPED_CHEST}).addTag(ConventionTags.INTERFACE);
        this.tag(P2PTunnelAttunement.getAttunementTag(P2PTunnelAttunement.FLUID_TUNNEL)).add((Object[])new Item[]{Items.BUCKET, Items.MILK_BUCKET, Items.WATER_BUCKET, Items.LAVA_BUCKET});
        this.tag(P2PTunnelAttunement.getAttunementTag(P2PTunnelAttunement.ME_TUNNEL)).addTag(ConventionTags.COVERED_CABLE).addTag(ConventionTags.COVERED_DENSE_CABLE).addTag(ConventionTags.GLASS_CABLE).addTag(ConventionTags.SMART_CABLE).addTag(ConventionTags.SMART_DENSE_CABLE);
    }
}

