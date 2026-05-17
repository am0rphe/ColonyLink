/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  net.minecraft.data.CachedOutput
 *  net.minecraft.data.DataGenerator
 *  net.minecraft.data.DataProvider
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 */
package appeng.datagen.providers.localization;

import appeng.api.config.PowerUnit;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEEntities;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.BlockDefinition;
import appeng.core.definitions.ItemDefinition;
import appeng.core.localization.ButtonToolTips;
import appeng.core.localization.GuiText;
import appeng.core.localization.InGameTooltip;
import appeng.core.localization.ItemModText;
import appeng.core.localization.LocalizationEnum;
import appeng.core.localization.PlayerMessages;
import appeng.datagen.providers.IAE2DataProvider;
import appeng.integration.modules.emi.EmiText;
import appeng.integration.modules.igtooltip.TooltipIds;
import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class LocalizationProvider
implements IAE2DataProvider {
    private final Map<String, String> localizations = new HashMap<String, String>();
    private final DataGenerator generator;
    private boolean wasSaved = false;

    public LocalizationProvider(DataGenerator generator) {
        this.generator = generator;
    }

    public final CompletableFuture<?> run(CachedOutput cache) {
        for (BlockDefinition<?> blockDefinition : AEBlocks.getBlocks()) {
            this.add("block.ae2." + blockDefinition.id().getPath(), blockDefinition.getEnglishName());
        }
        for (ItemDefinition itemDefinition : AEItems.getItems()) {
            this.add("item.ae2." + itemDefinition.id().getPath(), itemDefinition.getEnglishName());
        }
        for (Map.Entry entry : AEEntities.ENTITY_ENGLISH_NAMES.entrySet()) {
            this.add("entity.ae2." + (String)entry.getKey(), (String)entry.getValue());
        }
        this.addEnum(GuiText.class);
        this.addEnum(ButtonToolTips.class);
        this.addEnum(PlayerMessages.class);
        this.addEnum(InGameTooltip.class);
        this.addEnum(ItemModText.class);
        this.addEnum(EmiText.class);
        for (Iterator<Object> iterator : PowerUnit.values()) {
            this.add(((PowerUnit)((Object)iterator)).unlocalizedName, ((PowerUnit)((Object)iterator)).symbolName);
        }
        this.generateJadeLocalizations();
        this.generateLocalizations();
        return this.save(cache, this.localizations);
    }

    private void generateJadeLocalizations() {
        this.addJadeProviderDisplayName(TooltipIds.DEBUG, "AE2 Debug Info");
        this.addJadeProviderDisplayName(TooltipIds.GRID_NODE_STATE, "AE2 Network State");
        this.addJadeProviderDisplayName(TooltipIds.POWER_STORAGE, "AE2 Power State");
        this.addJadeProviderDisplayName(TooltipIds.CRAFTING_MONITOR, "AE2 Crafting Monitor");
        this.addJadeProviderDisplayName(TooltipIds.PATTERN_PROVIDER, "AE2 Pattern Provider");
        this.addJadeProviderDisplayName(TooltipIds.CHARGER, "AE2 Charger");
        this.addJadeProviderDisplayName(TooltipIds.PART_NAME, "AE2 Part Name");
        this.addJadeProviderDisplayName(TooltipIds.PART_ICON, "AE2 Part Icon");
        this.addJadeProviderDisplayName(TooltipIds.PART_MOD_NAME, "AE2 Mod Name");
        this.addJadeProviderDisplayName(TooltipIds.PART_TOOLTIP, "AE2 Part Tooltip");
    }

    private void addJadeProviderDisplayName(ResourceLocation providerId, String name) {
        this.add("config.jade.plugin_" + providerId.getNamespace() + "." + providerId.getPath(), name);
    }

    public <T extends Enum<T>> void addEnum(Class<T> localizedEnum) {
        for (Enum enumConstant : (Enum[])localizedEnum.getEnumConstants()) {
            this.add(((LocalizationEnum)((Object)enumConstant)).getTranslationKey(), ((LocalizationEnum)((Object)enumConstant)).getEnglishText());
        }
    }

    public Component component(String key, String text) {
        this.add(key, text);
        return Component.translatable((String)key);
    }

    public void add(String key, String text) {
        Preconditions.checkState((!this.wasSaved ? 1 : 0) != 0, (Object)"Cannot add more translations after they were already saved");
        String previous = this.localizations.put(key, text);
        if (previous != null) {
            throw new IllegalStateException("Localization key " + key + " is already translated to: " + previous);
        }
    }

    private void generateLocalizations() {
        this.add("ae2.permission_denied", "You lack permission to access this.");
        this.add("dimension.ae2.spatial_storage", "Spatial Storage");
        this.add("biome.ae2.spatial_storage", "Spatial Storage");
        this.add("commands.ae2.ChunkLoggerOff", "Chunk Logging is now off");
        this.add("commands.ae2.ChunkLoggerOn", "Chunk Logging is now on");
        this.add("commands.ae2.permissions", "You do not have adequate permissions to run this command.");
        this.add("commands.ae2.usage", "Commands provided by Applied Energistics 2 - use /ae2 list for a list, and /ae2 help _____ for help with a command.");
        this.add("death.attack.matter_cannon", "%1$s was shot by %2$s");
        this.add("death.attack.matter_cannon.item", "%1$s was shot by %2$s using %3$s");
        this.add("entity.minecraft.villager.ae2.fluix_researcher", "Fluix Researcher");
        this.add("gui.ae2.PatternEncoding.primary_processing_result_hint", "Can be requested through the automated crafting system.");
        this.add("gui.ae2.PatternEncoding.primary_processing_result_tooltip", "Primary Processing Result");
        this.add("gui.ae2.PatternEncoding.secondary_processing_result_hint", "Can not be directly requested through the automated crafting system, but will be used before stored items in multi-step recipes.");
        this.add("gui.ae2.PatternEncoding.secondary_processing_result_tooltip", "Secondary Processing Result");
        this.add("key.ae2.category", "Applied Energistics 2");
        this.add("key.ae2.portable_fluid_cell", "Open Portable Fluid Cell");
        this.add("key.ae2.portable_item_cell", "Open Portable Item Cell");
        this.add("key.ae2.wireless_terminal", "Open Wireless Terminal");
        this.add("key.ae2.guide", "Open Guide for Items");
        this.add("key.ae2.mouse_wheel_item_modifier", "Modifier for Mouse-Wheel Items");
        this.add("key.ae2.part_placement_opposite", "Place Parts on Opposite Side");
        this.add("key.toggle_focus.desc", "Toggle search box focus");
        this.add("stat.ae2.items_extracted", "Items extracted from ME Storage");
        this.add("stat.ae2.items_inserted", "Items added to ME Storage");
        this.add("theoneprobe.ae2.channels", "%1$d Channels");
        this.add("theoneprobe.ae2.channels_of", "%1$d of %2$d Channels");
        this.add("theoneprobe.ae2.contains", "Contains");
        this.add("theoneprobe.ae2.crafting", "Crafting: %1$s");
        this.add("theoneprobe.ae2.device_missing_channel", "Device Missing Channel");
        this.add("theoneprobe.ae2.device_offline", "Device Offline");
        this.add("theoneprobe.ae2.device_online", "Device Online");
        this.add("theoneprobe.ae2.locked", "Locked");
        this.add("theoneprobe.ae2.nested_p2p_tunnel", "Error: Nested P2P Tunnel");
        this.add("theoneprobe.ae2.p2p_frequency", "Frequency: %1$s");
        this.add("theoneprobe.ae2.p2p_input_many_outputs", "Linked (Input Side) - %d Outputs");
        this.add("theoneprobe.ae2.p2p_input_one_output", "Linked (Input Side)");
        this.add("theoneprobe.ae2.p2p_output", "Linked (Output Side)");
        this.add("theoneprobe.ae2.p2p_unlinked", "Unlinked");
        this.add("theoneprobe.ae2.showing", "Showing");
        this.add("theoneprobe.ae2.stored_energy", "%1$d / %2$d");
        this.add("theoneprobe.ae2.unlocked", "Unlocked");
    }

    private CompletableFuture<?> save(CachedOutput cache, Map<String, String> localizations) {
        this.wasSaved = true;
        Path path = this.generator.getPackOutput().getOutputFolder().resolve("assets/ae2/lang/en_us.json");
        TreeMap<String, String> sorted = new TreeMap<String, String>(localizations);
        JsonObject jsonLocalization = new JsonObject();
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            jsonLocalization.addProperty(entry.getKey(), entry.getValue());
        }
        return DataProvider.saveStable((CachedOutput)cache, (JsonElement)jsonLocalization, (Path)path);
    }

    public String getName() {
        return "Localization (en_us)";
    }
}

