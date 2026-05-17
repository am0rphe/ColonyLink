/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.serialization.Codec
 *  net.minecraft.core.GlobalPos
 *  net.minecraft.core.Holder
 *  net.minecraft.core.component.DataComponentType
 *  net.minecraft.core.component.DataComponentType$Builder
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.ComponentSerialization
 *  net.minecraft.network.codec.ByteBufCodecs
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.component.CustomData
 *  net.minecraft.world.item.component.ItemContainerContents
 *  net.neoforged.neoforge.registries.DeferredRegister
 *  org.jetbrains.annotations.ApiStatus$Internal
 */
package appeng.api.ids;

import appeng.api.components.ExportedUpgrades;
import appeng.api.config.FuzzyMode;
import appeng.api.implementations.items.MemoryCardColors;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.api.util.AEColor;
import appeng.block.crafting.PushDirection;
import appeng.crafting.pattern.EncodedCraftingPattern;
import appeng.crafting.pattern.EncodedProcessingPattern;
import appeng.crafting.pattern.EncodedSmithingTablePattern;
import appeng.crafting.pattern.EncodedStonecuttingPattern;
import appeng.items.storage.SpatialPlotInfo;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.ApiStatus;

public final class AEComponents {
    @ApiStatus.Internal
    public static final DeferredRegister<DataComponentType<?>> DR = DeferredRegister.create((ResourceKey)Registries.DATA_COMPONENT_TYPE, (String)"ae2");
    public static final DataComponentType<Component> EXPORTED_SETTINGS_SOURCE = AEComponents.register("exported_settings_source", builder -> builder.persistent(ComponentSerialization.CODEC).networkSynchronized(ComponentSerialization.STREAM_CODEC));
    public static final DataComponentType<Component> EXPORTED_CUSTOM_NAME = AEComponents.register("exported_custom_name", builder -> builder.persistent(ComponentSerialization.CODEC).networkSynchronized(ComponentSerialization.STREAM_CODEC));
    public static final DataComponentType<ExportedUpgrades> EXPORTED_UPGRADES = AEComponents.register("exported_upgrades", builder -> builder.persistent(ExportedUpgrades.CODEC).networkSynchronized(ExportedUpgrades.STREAM_CODEC));
    public static final DataComponentType<Map<String, String>> EXPORTED_SETTINGS = AEComponents.register("exported_settings", builder -> builder.persistent((Codec)Codec.unboundedMap((Codec)Codec.STRING, (Codec)Codec.STRING)).networkSynchronized(ByteBufCodecs.map(Maps::newHashMapWithExpectedSize, (StreamCodec)ByteBufCodecs.STRING_UTF8, (StreamCodec)ByteBufCodecs.STRING_UTF8)));
    public static final DataComponentType<Integer> EXPORTED_PRIORITY = AEComponents.register("exported_priority", builder -> builder.persistent((Codec)Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));
    public static final DataComponentType<Item> EXPORTED_P2P_TYPE = AEComponents.register("exported_p2p_type", builder -> builder.persistent(BuiltInRegistries.ITEM.byNameCodec()).networkSynchronized(ByteBufCodecs.registry((ResourceKey)Registries.ITEM)));
    public static final DataComponentType<Short> EXPORTED_P2P_FREQUENCY = AEComponents.register("exported_p2p_frequency", builder -> builder.persistent((Codec)Codec.SHORT).networkSynchronized(ByteBufCodecs.SHORT));
    public static final DataComponentType<MemoryCardColors> MEMORY_CARD_COLORS = AEComponents.register("memory_card_colors", builder -> builder.persistent(MemoryCardColors.CODEC).networkSynchronized(MemoryCardColors.STREAM_CODEC));
    public static final DataComponentType<List<GenericStack>> EXPORTED_CONFIG_INV = AEComponents.register("exported_config_inv", builder -> builder.persistent(GenericStack.FAULT_TOLERANT_NULLABLE_LIST_CODEC).networkSynchronized(GenericStack.STREAM_CODEC.apply(ByteBufCodecs.list())));
    public static final DataComponentType<Long> EXPORTED_LEVEL_EMITTER_VALUE = AEComponents.register("exported_level_emitter_value", builder -> builder.persistent((Codec)Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG));
    public static final DataComponentType<ItemContainerContents> EXPORTED_PATTERNS = AEComponents.register("exported_patterns", builder -> builder.persistent(ItemContainerContents.CODEC).networkSynchronized(ItemContainerContents.STREAM_CODEC));
    public static final DataComponentType<PushDirection> EXPORTED_PUSH_DIRECTION = AEComponents.register("exported_push_direction", builder -> builder.persistent(PushDirection.CODEC).networkSynchronized(PushDirection.STREAM_CODEC));
    public static final DataComponentType<Component> NAME_PRESS_NAME = AEComponents.register("name_press_name", builder -> builder.persistent(ComponentSerialization.CODEC).networkSynchronized(ComponentSerialization.TRUSTED_STREAM_CODEC));
    public static final DataComponentType<ItemContainerContents> UPGRADES = AEComponents.register("upgrades", builder -> builder.persistent(ItemContainerContents.CODEC).networkSynchronized(ItemContainerContents.STREAM_CODEC));
    public static final DataComponentType<Long> ENTANGLED_SINGULARITY_ID = AEComponents.register("entangled_singularity_id", builder -> builder.persistent((Codec)Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG));
    public static final DataComponentType<Double> STORED_ENERGY = AEComponents.register("stored_energy", builder -> builder.persistent((Codec)Codec.DOUBLE).networkSynchronized(ByteBufCodecs.DOUBLE));
    public static final DataComponentType<Double> ENERGY_CAPACITY = AEComponents.register("energy_capacity", builder -> builder.persistent((Codec)Codec.DOUBLE).networkSynchronized(ByteBufCodecs.DOUBLE));
    public static final DataComponentType<EncodedCraftingPattern> ENCODED_CRAFTING_PATTERN = AEComponents.register("encoded_crafting_pattern", builder -> builder.persistent(EncodedCraftingPattern.CODEC).networkSynchronized(EncodedCraftingPattern.STREAM_CODEC));
    public static final DataComponentType<EncodedProcessingPattern> ENCODED_PROCESSING_PATTERN = AEComponents.register("encoded_processing_pattern", builder -> builder.persistent(EncodedProcessingPattern.CODEC).networkSynchronized(EncodedProcessingPattern.STREAM_CODEC));
    public static final DataComponentType<EncodedStonecuttingPattern> ENCODED_STONECUTTING_PATTERN = AEComponents.register("encoded_stonecutting_pattern", builder -> builder.persistent(EncodedStonecuttingPattern.CODEC).networkSynchronized(EncodedStonecuttingPattern.STREAM_CODEC));
    public static final DataComponentType<EncodedSmithingTablePattern> ENCODED_SMITHING_TABLE_PATTERN = AEComponents.register("encoded_smithing_table_pattern", builder -> builder.persistent(EncodedSmithingTablePattern.CODEC).networkSynchronized(EncodedSmithingTablePattern.STREAM_CODEC));
    public static final DataComponentType<List<AEKeyType>> ENABLED_KEY_TYPES = AEComponents.register("enabled_key_types", builder -> builder.persistent(AEKeyType.CODEC.listOf()).networkSynchronized(AEKeyType.STREAM_CODEC.apply(ByteBufCodecs.list())));
    public static final DataComponentType<GlobalPos> WIRELESS_LINK_TARGET = AEComponents.register("wireless_link_target", builder -> builder.persistent(GlobalPos.CODEC).networkSynchronized(GlobalPos.STREAM_CODEC));
    public static final DataComponentType<AEColor> SELECTED_COLOR = AEComponents.register("selected_color", builder -> builder.persistent(AEColor.CODEC).networkSynchronized(AEColor.STREAM_CODEC));
    public static final DataComponentType<FuzzyMode> STORAGE_CELL_FUZZY_MODE = AEComponents.register("storage_cell_fuzzy_mode", builder -> builder.persistent(FuzzyMode.CODEC).networkSynchronized(FuzzyMode.STREAM_CODEC));
    public static final DataComponentType<List<GenericStack>> STORAGE_CELL_INV = AEComponents.register("storage_cell_inv", builder -> builder.persistent(GenericStack.FAULT_TOLERANT_LIST_CODEC).networkSynchronized(GenericStack.STREAM_CODEC.apply(ByteBufCodecs.list())));
    public static final DataComponentType<List<GenericStack>> STORAGE_CELL_CONFIG_INV = AEComponents.register("storage_cell_config_inv", builder -> builder.persistent(GenericStack.FAULT_TOLERANT_NULLABLE_LIST_CODEC).networkSynchronized(GenericStack.STREAM_CODEC.apply(ByteBufCodecs.list())));
    public static final DataComponentType<Holder<Item>> FACADE_ITEM = AEComponents.register("facade_item", builder -> builder.persistent(BuiltInRegistries.ITEM.holderByNameCodec()).networkSynchronized(ByteBufCodecs.holderRegistry((ResourceKey)Registries.ITEM)));
    public static final DataComponentType<String> FACADE_CYCLE_PROPERTY = AEComponents.register("facade_cycle_property", builder -> builder.persistent((Codec)Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));
    public static final DataComponentType<GenericStack> WRAPPED_STACK = AEComponents.register("wrapped_stack", builder -> builder.persistent(GenericStack.CODEC).networkSynchronized(GenericStack.STREAM_CODEC));
    public static final DataComponentType<ItemContainerContents> CRAFTING_INV = AEComponents.register("crafting_inv", builder -> builder.persistent(ItemContainerContents.CODEC).networkSynchronized(ItemContainerContents.STREAM_CODEC));
    public static final DataComponentType<SpatialPlotInfo> SPATIAL_PLOT_INFO = AEComponents.register("spatial_plot_info", builder -> builder.persistent(SpatialPlotInfo.CODEC).networkSynchronized(SpatialPlotInfo.STREAM_CODEC));
    public static final DataComponentType<CustomData> MISSING_CONTENT_ITEMSTACK_DATA = AEComponents.register("missing_content_itemstack_data", builder -> builder.persistent(CustomData.CODEC).networkSynchronized(CustomData.STREAM_CODEC));
    public static final DataComponentType<CustomData> MISSING_CONTENT_AEKEY_DATA = AEComponents.register("missing_content_aekey_data", builder -> builder.persistent(CustomData.CODEC).networkSynchronized(CustomData.STREAM_CODEC));
    public static final DataComponentType<String> MISSING_CONTENT_ERROR = AEComponents.register("missing_content_error", builder -> builder.persistent((Codec)Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

    private AEComponents() {
    }

    private static <T> DataComponentType<T> register(String name, Consumer<DataComponentType.Builder<T>> customizer) {
        DataComponentType.Builder builder = DataComponentType.builder();
        customizer.accept(builder);
        DataComponentType componentType = builder.build();
        DR.register(name, () -> componentType);
        return componentType;
    }
}

