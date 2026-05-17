/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonDeserializationContext
 *  com.google.gson.JsonDeserializer
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonParseException
 *  com.google.gson.JsonSerializationContext
 *  com.google.gson.JsonSerializer
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  net.minecraft.client.renderer.Rect2i
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.HolderLookup$RegistryLookup
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.Component$SerializerAdapter
 *  net.minecraft.network.chat.Style
 *  net.minecraft.network.chat.Style$Serializer
 *  org.jetbrains.annotations.Nullable
 */
package appeng.client.gui.style;

import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.BlitterDeserializer;
import appeng.client.gui.style.Color;
import appeng.client.gui.style.ColorDeserializer;
import appeng.client.gui.style.GeneratedBackground;
import appeng.client.gui.style.PaletteColor;
import appeng.client.gui.style.Rectangle2dDeserializer;
import appeng.client.gui.style.SlotPosition;
import appeng.client.gui.style.TerminalStyle;
import appeng.client.gui.style.Text;
import appeng.client.gui.style.TooltipArea;
import appeng.client.gui.style.WidgetStyle;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;

public class ScreenStyle {
    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().registerTypeHierarchyAdapter(Component.class, (Object)new Component.SerializerAdapter(HolderLookup.Provider.create(Stream.of(new HolderLookup.RegistryLookup[0])))).registerTypeAdapter(Style.class, (Object)new StyleSerializer()).registerTypeAdapter(Blitter.class, (Object)BlitterDeserializer.INSTANCE).registerTypeAdapter(Rect2i.class, (Object)Rectangle2dDeserializer.INSTANCE).registerTypeAdapter(Color.class, (Object)ColorDeserializer.INSTANCE).create();
    @Nullable
    private String helpTopic;
    private final Map<String, SlotPosition> slots = new HashMap<String, SlotPosition>();
    private final Map<String, Text> text = new HashMap<String, Text>();
    private final Map<PaletteColor, Color> palette = new EnumMap<PaletteColor, Color>(PaletteColor.class);
    private final Map<String, Blitter> images = new HashMap<String, Blitter>();
    @Nullable
    private Blitter background;
    @Nullable
    private GeneratedBackground generatedBackground;
    @Nullable
    private TerminalStyle terminalStyle;
    private final Map<String, WidgetStyle> widgets = new HashMap<String, WidgetStyle>();
    private final Map<String, TooltipArea> tooltips = new HashMap<String, TooltipArea>();

    public Color getColor(PaletteColor color) {
        return this.palette.get((Object)color);
    }

    public Map<String, SlotPosition> getSlots() {
        return this.slots;
    }

    public Map<String, Text> getText() {
        return this.text;
    }

    public Map<String, TooltipArea> getTooltips() {
        return this.tooltips;
    }

    @Nullable
    public Blitter getBackground() {
        return this.background != null ? this.background.copy() : null;
    }

    @Nullable
    public GeneratedBackground getGeneratedBackground() {
        return this.generatedBackground;
    }

    public String getHelpTopic() {
        return this.helpTopic;
    }

    public WidgetStyle getWidget(String id) {
        WidgetStyle widget = this.widgets.get(id);
        if (widget == null) {
            throw new IllegalStateException("Screen is missing required widget: " + id);
        }
        return widget;
    }

    public Blitter getImage(String id) {
        Blitter blitter = this.images.get(id);
        if (blitter == null) {
            throw new IllegalStateException("Screen is missing required image: " + id);
        }
        return blitter;
    }

    @Nullable
    public TerminalStyle getTerminalStyle() {
        return this.terminalStyle;
    }

    public void validate() {
        for (PaletteColor value : PaletteColor.values()) {
            if (this.palette.containsKey((Object)value)) continue;
            throw new RuntimeException("Palette is missing color " + String.valueOf((Object)value));
        }
        if (this.terminalStyle != null) {
            this.terminalStyle.validate();
        }
    }

    private static class StyleSerializer
    implements JsonSerializer<Style>,
    JsonDeserializer<Style> {
        private StyleSerializer() {
        }

        public Style deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return (Style)Style.Serializer.CODEC.parse((DynamicOps)JsonOps.INSTANCE, (Object)json).getOrThrow(JsonParseException::new);
        }

        public JsonElement serialize(Style src, Type typeOfSrc, JsonSerializationContext context) {
            return (JsonElement)Style.Serializer.CODEC.encodeStart((DynamicOps)JsonOps.INSTANCE, (Object)src).getOrThrow(JsonParseException::new);
        }
    }
}

