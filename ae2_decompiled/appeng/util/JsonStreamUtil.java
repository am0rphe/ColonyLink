/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.math.StatsAccumulator
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.stream.JsonWriter
 *  net.minecraft.world.level.ChunkPos
 */
package appeng.util;

import com.google.common.math.StatsAccumulator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Map;
import net.minecraft.world.level.ChunkPos;

public final class JsonStreamUtil {
    private static final Gson GSON = new GsonBuilder().serializeSpecialFloatingPointValues().create();

    private JsonStreamUtil() {
    }

    public static void writeProperties(Map<String, ?> properties, JsonWriter writer) throws IOException {
        for (Map.Entry<String, ?> entry : properties.entrySet()) {
            writer.name(entry.getKey());
            GSON.toJson(entry.getValue(), entry.getValue().getClass(), writer);
        }
    }

    public static JsonElement toJson(ChunkPos pos) {
        JsonArray jsonPos = new JsonArray(2);
        jsonPos.add((Number)pos.x);
        jsonPos.add((Number)pos.z);
        return jsonPos;
    }

    public static Map<String, ?> toMap(StatsAccumulator stats) {
        if (stats.count() == 0L) {
            return Map.of("count", 0);
        }
        return Map.of("count", stats.count(), "min", stats.min(), "max", stats.max(), "mean", stats.mean());
    }
}

