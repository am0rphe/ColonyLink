/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonDeserializationContext
 *  com.google.gson.JsonDeserializer
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  net.minecraft.client.renderer.Rect2i
 *  net.minecraft.util.GsonHelper
 */
package appeng.client.gui.style;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.util.GsonHelper;

public enum Rectangle2dDeserializer implements JsonDeserializer<Rect2i>
{
    INSTANCE;


    public Rect2i deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonArray()) {
            JsonArray arr = json.getAsJsonArray();
            if (arr.size() != 4) {
                throw new JsonParseException("Rectangles expressed as arrays must have 4 elements.");
            }
            int x = arr.get(0).getAsInt();
            int y = arr.get(1).getAsInt();
            int width = arr.get(2).getAsInt();
            int height = arr.get(3).getAsInt();
            return new Rect2i(x, y, width, height);
        }
        JsonObject obj = json.getAsJsonObject();
        int x = GsonHelper.getAsInt((JsonObject)obj, (String)"x", (int)0);
        int y = GsonHelper.getAsInt((JsonObject)obj, (String)"y", (int)0);
        int width = GsonHelper.getAsInt((JsonObject)obj, (String)"width");
        int height = GsonHelper.getAsInt((JsonObject)obj, (String)"height");
        return new Rect2i(x, y, width, height);
    }
}

