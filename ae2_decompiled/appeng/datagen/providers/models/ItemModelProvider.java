/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.data.PackOutput
 *  net.minecraft.resources.ResourceLocation
 *  net.neoforged.neoforge.client.model.generators.ItemModelBuilder
 *  net.neoforged.neoforge.client.model.generators.ItemModelProvider
 *  net.neoforged.neoforge.common.data.ExistingFileHelper
 */
package appeng.datagen.providers.models;

import appeng.api.ids.AEItemIds;
import appeng.api.util.AEColor;
import appeng.client.render.model.MemoryCardModel;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.ItemDefinition;
import appeng.datagen.providers.IAE2DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ItemModelProvider
extends net.neoforged.neoforge.client.model.generators.ItemModelProvider
implements IAE2DataProvider {
    public ItemModelProvider(PackOutput packOutput, ExistingFileHelper existingFileHelper) {
        super(packOutput, "ae2", existingFileHelper);
    }

    protected void registerModels() {
        this.registerPaintballs();
        this.flatSingleLayer(AEItems.MISSING_CONTENT, "minecraft:item/barrier");
        this.flatSingleLayer(MemoryCardModel.MODEL_BASE, "item/memory_card_base").texture("layer1", "item/memory_card_led");
        this.builtInItemModel("memory_card");
        this.builtInItemModel("facade");
        this.builtInItemModel("meteorite_compass");
        this.flatSingleLayer(AEItems.ADVANCED_CARD, "item/advanced_card");
        this.flatSingleLayer(AEItems.VOID_CARD, "item/card_void");
        this.flatSingleLayer(AEItems.ANNIHILATION_CORE, "item/annihilation_core");
        this.flatSingleLayer(AEItems.BASIC_CARD, "item/basic_card");
        this.flatSingleLayer(AEItems.BLANK_PATTERN, "item/blank_pattern");
        this.flatSingleLayer(AEItems.CALCULATION_PROCESSOR, "item/calculation_processor");
        this.flatSingleLayer(AEItems.CALCULATION_PROCESSOR_PRESS, "item/calculation_processor_press");
        this.flatSingleLayer(AEItems.CALCULATION_PROCESSOR_PRINT, "item/printed_calculation_processor");
        this.flatSingleLayer(AEItems.CAPACITY_CARD, "item/card_capacity");
        this.storageCell(AEItems.ITEM_CELL_1K, "item/item_storage_cell_1k");
        this.storageCell(AEItems.ITEM_CELL_4K, "item/item_storage_cell_4k");
        this.storageCell(AEItems.ITEM_CELL_16K, "item/item_storage_cell_16k");
        this.storageCell(AEItems.ITEM_CELL_64K, "item/item_storage_cell_64k");
        this.storageCell(AEItems.ITEM_CELL_256K, "item/item_storage_cell_256k");
        this.flatSingleLayer(AEItems.CERTUS_QUARTZ_CRYSTAL, "item/certus_quartz_crystal");
        this.flatSingleLayer(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED, "item/certus_quartz_crystal_charged");
        this.flatSingleLayer(AEItems.CERTUS_QUARTZ_DUST, "item/certus_quartz_dust");
        this.flatSingleLayer(AEItems.CERTUS_QUARTZ_KNIFE, "item/certus_quartz_cutting_knife");
        this.flatSingleLayer(AEItems.CERTUS_QUARTZ_WRENCH, "item/certus_quartz_wrench");
        this.flatSingleLayer(AEItems.CRAFTING_CARD, "item/card_crafting");
        this.flatSingleLayer(AEItems.CRAFTING_PATTERN, "item/crafting_pattern");
        this.flatSingleLayer(AEItems.DEBUG_CARD, "item/debug_card");
        this.flatSingleLayer(AEItems.DEBUG_ERASER, "item/debug/eraser");
        this.flatSingleLayer(AEItems.DEBUG_METEORITE_PLACER, "item/debug/meteorite_placer");
        this.flatSingleLayer(AEItems.DEBUG_REPLICATOR_CARD, "item/debug/replicator_card");
        this.flatSingleLayer(AEItems.ENDER_DUST, "item/ender_dust");
        this.flatSingleLayer(AEItems.ENERGY_CARD, "item/card_energy");
        this.flatSingleLayer(AEItems.ENGINEERING_PROCESSOR, "item/engineering_processor");
        this.flatSingleLayer(AEItems.ENGINEERING_PROCESSOR_PRESS, "item/engineering_processor_press");
        this.flatSingleLayer(AEItems.ENGINEERING_PROCESSOR_PRINT, "item/printed_engineering_processor");
        this.flatSingleLayer(AEItems.EQUAL_DISTRIBUTION_CARD, "item/card_equal_distribution");
        this.storageCell(AEItems.FLUID_CELL_1K, "item/fluid_storage_cell_1k");
        this.storageCell(AEItems.FLUID_CELL_4K, "item/fluid_storage_cell_4k");
        this.storageCell(AEItems.FLUID_CELL_16K, "item/fluid_storage_cell_16k");
        this.storageCell(AEItems.FLUID_CELL_64K, "item/fluid_storage_cell_64k");
        this.storageCell(AEItems.FLUID_CELL_256K, "item/fluid_storage_cell_256k");
        this.flatSingleLayer(AEItems.FLUID_CELL_HOUSING, "item/fluid_cell_housing");
        this.flatSingleLayer(AEItems.FLUIX_CRYSTAL, "item/fluix_crystal");
        this.flatSingleLayer(AEItems.FLUIX_DUST, "item/fluix_dust");
        this.flatSingleLayer(AEItems.FLUIX_PEARL, "item/fluix_pearl");
        this.flatSingleLayer(AEItems.FLUIX_UPGRADE_SMITHING_TEMPLATE, "item/fluix_upgrade_smithing_template");
        this.flatSingleLayer(AEItems.FORMATION_CORE, "item/formation_core");
        this.flatSingleLayer(AEItems.FUZZY_CARD, "item/card_fuzzy");
        this.flatSingleLayer(AEItems.INVERTER_CARD, "item/card_inverter");
        this.flatSingleLayer(AEItems.CELL_COMPONENT_16K, "item/cell_component_16k");
        this.flatSingleLayer(AEItems.CELL_COMPONENT_1K, "item/cell_component_1k");
        this.flatSingleLayer(AEItems.CELL_COMPONENT_4K, "item/cell_component_4k");
        this.flatSingleLayer(AEItems.CELL_COMPONENT_64K, "item/cell_component_64k");
        this.flatSingleLayer(AEItems.CELL_COMPONENT_256K, "item/cell_component_256k");
        this.flatSingleLayer(AEItems.CREATIVE_CELL, "item/creative_storage_cell");
        this.flatSingleLayer(AEItems.ITEM_CELL_HOUSING, "item/item_cell_housing");
        this.flatSingleLayer(AEItems.LOGIC_PROCESSOR, "item/logic_processor");
        this.flatSingleLayer(AEItems.LOGIC_PROCESSOR_PRESS, "item/logic_processor_press");
        this.flatSingleLayer(AEItems.LOGIC_PROCESSOR_PRINT, "item/printed_logic_processor");
        this.flatSingleLayer(AEItems.MATTER_BALL, "item/matter_ball");
        this.flatSingleLayer(AEItems.NAME_PRESS, "item/name_press");
        this.flatSingleLayer(AEItems.NETHER_QUARTZ_KNIFE, "item/nether_quartz_cutting_knife");
        this.flatSingleLayer(AEItems.NETHER_QUARTZ_WRENCH, "item/nether_quartz_wrench");
        this.portableCell(AEItems.PORTABLE_ITEM_CELL1K, "item", "1k");
        this.portableCell(AEItems.PORTABLE_ITEM_CELL4K, "item", "4k");
        this.portableCell(AEItems.PORTABLE_ITEM_CELL16K, "item", "16k");
        this.portableCell(AEItems.PORTABLE_ITEM_CELL64K, "item", "64k");
        this.portableCell(AEItems.PORTABLE_ITEM_CELL256K, "item", "256k");
        this.portableCell(AEItems.PORTABLE_FLUID_CELL1K, "fluid", "1k");
        this.portableCell(AEItems.PORTABLE_FLUID_CELL4K, "fluid", "4k");
        this.portableCell(AEItems.PORTABLE_FLUID_CELL16K, "fluid", "16k");
        this.portableCell(AEItems.PORTABLE_FLUID_CELL64K, "fluid", "64k");
        this.portableCell(AEItems.PORTABLE_FLUID_CELL256K, "fluid", "256k");
        this.flatSingleLayer(AEItems.PROCESSING_PATTERN, "item/processing_pattern");
        this.flatSingleLayer(AEItems.QUANTUM_ENTANGLED_SINGULARITY, "item/quantum_entangled_singularity");
        this.flatSingleLayer(AEItems.REDSTONE_CARD, "item/card_redstone");
        this.flatSingleLayer(AEItems.SILICON, "item/silicon");
        this.flatSingleLayer(AEItems.SILICON_PRESS, "item/silicon_press");
        this.flatSingleLayer(AEItems.SILICON_PRINT, "item/printed_silicon");
        this.flatSingleLayer(AEItems.SINGULARITY, "item/singularity");
        this.flatSingleLayer(AEItems.SKY_DUST, "item/sky_dust");
        this.flatSingleLayer(AEItems.SPATIAL_2_CELL_COMPONENT, "item/spatial_cell_component_2");
        this.flatSingleLayer(AEItems.SPATIAL_16_CELL_COMPONENT, "item/spatial_cell_component_16");
        this.flatSingleLayer(AEItems.SPATIAL_128_CELL_COMPONENT, "item/spatial_cell_component_128");
        this.flatSingleLayer(AEItems.SPATIAL_CELL2, "item/spatial_storage_cell_2");
        this.flatSingleLayer(AEItems.SPATIAL_CELL16, "item/spatial_storage_cell_16");
        this.flatSingleLayer(AEItems.SPATIAL_CELL128, "item/spatial_storage_cell_128");
        this.flatSingleLayer(AEItems.SPEED_CARD, "item/card_speed");
        this.flatSingleLayer(AEItems.SMITHING_TABLE_PATTERN, "item/smithing_table_pattern");
        this.flatSingleLayer(AEItems.STONECUTTING_PATTERN, "item/stonecutting_pattern");
        this.flatSingleLayer(AEItemIds.GUIDE, "item/guide");
        this.flatSingleLayer(AEItems.VIEW_CELL, "item/view_cell");
        this.flatSingleLayer(AEItems.WIRELESS_BOOSTER, "item/wireless_booster");
        this.flatSingleLayer(AEItems.WIRELESS_CRAFTING_TERMINAL, "item/wireless_crafting_terminal");
        this.flatSingleLayer(AEItems.WIRELESS_RECEIVER, "item/wireless_receiver");
        this.flatSingleLayer(AEItems.WIRELESS_TERMINAL, "item/wireless_terminal");
        this.registerEmptyModel(AEItems.WRAPPED_GENERIC_STACK);
        this.registerEmptyModel(AEBlocks.CABLE_BUS.item());
        this.registerHandheld();
    }

    private void storageCell(ItemDefinition<?> item, String background) {
        String id = item.id().getPath();
        ((ItemModelBuilder)this.singleTexture(id, this.mcLoc("item/generated"), "layer0", ItemModelProvider.makeId(background))).texture("layer1", "item/storage_cell_led");
    }

    private void portableCell(ItemDefinition<?> item, String housingType, String tier) {
        String id = item.id().getPath();
        ((ItemModelBuilder)((ItemModelBuilder)((ItemModelBuilder)this.singleTexture(id, this.mcLoc("item/generated"), "layer0", ItemModelProvider.makeId("item/portable_cell_%s_housing".formatted(housingType)))).texture("layer1", "item/portable_cell_led")).texture("layer2", "item/portable_cell_screen")).texture("layer3", "item/portable_cell_side_%s".formatted(tier));
    }

    private void registerHandheld() {
        this.handheld(AEItems.CERTUS_QUARTZ_AXE);
        this.handheld(AEItems.CERTUS_QUARTZ_HOE);
        this.handheld(AEItems.CERTUS_QUARTZ_SHOVEL);
        this.handheld(AEItems.CERTUS_QUARTZ_PICK);
        this.handheld(AEItems.CERTUS_QUARTZ_SWORD);
        this.handheld(AEItems.CERTUS_QUARTZ_WRENCH);
        this.handheld(AEItems.CERTUS_QUARTZ_KNIFE);
        this.handheld(AEItems.NETHER_QUARTZ_AXE);
        this.handheld(AEItems.NETHER_QUARTZ_HOE);
        this.handheld(AEItems.NETHER_QUARTZ_SHOVEL);
        this.handheld(AEItems.NETHER_QUARTZ_PICK);
        this.handheld(AEItems.NETHER_QUARTZ_SWORD);
        this.handheld(AEItems.NETHER_QUARTZ_WRENCH);
        this.handheld(AEItems.NETHER_QUARTZ_KNIFE);
        this.handheld(AEItems.FLUIX_AXE);
        this.handheld(AEItems.FLUIX_HOE);
        this.handheld(AEItems.FLUIX_SHOVEL);
        this.handheld(AEItems.FLUIX_PICK);
        this.handheld(AEItems.FLUIX_SWORD);
        this.handheld(AEItems.ENTROPY_MANIPULATOR);
        this.handheld(AEItems.CHARGED_STAFF);
    }

    private void handheld(ItemDefinition<?> item) {
        this.singleTexture(item.id().getPath(), ResourceLocation.parse((String)"item/handheld"), "layer0", ItemModelProvider.makeId("item/" + item.id().getPath()));
    }

    private void registerEmptyModel(ItemDefinition<?> item) {
        this.getBuilder(item.id().getPath());
    }

    private void registerPaintballs() {
        ResourceLocation id;
        for (AEColor value : AEColor.values()) {
            id = AEItems.COLORED_PAINT_BALL.id(value);
            if (id == null) continue;
            this.flatSingleLayer(id, "item/paint_ball");
        }
        for (AEColor value : AEColor.values()) {
            id = AEItems.COLORED_LUMEN_PAINT_BALL.id(value);
            if (id == null) continue;
            this.flatSingleLayer(id, "item/paint_ball_shimmer");
        }
    }

    private ItemModelBuilder flatSingleLayer(ItemDefinition<?> item, String texture) {
        String id = item.id().getPath();
        return (ItemModelBuilder)this.singleTexture(id, this.mcLoc("item/generated"), "layer0", ItemModelProvider.makeId(texture));
    }

    private ItemModelBuilder flatSingleLayer(ResourceLocation id, String texture) {
        return (ItemModelBuilder)this.singleTexture(id.getPath(), this.mcLoc("item/generated"), "layer0", ItemModelProvider.makeId(texture));
    }

    private ItemModelBuilder builtInItemModel(String name) {
        ItemModelBuilder model = (ItemModelBuilder)this.getBuilder("item/" + name);
        return model;
    }

    private static ResourceLocation makeId(String id) {
        return id.contains(":") ? ResourceLocation.parse((String)id) : AppEng.makeId(id);
    }
}

