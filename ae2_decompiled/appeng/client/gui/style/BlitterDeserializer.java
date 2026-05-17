/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonDeserializationContext
 *  com.google.gson.JsonDeserializer
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  net.minecraft.client.renderer.Rect2i
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.GsonHelper
 */
package appeng.client.gui.style;

import appeng.client.gui.style.Blitter;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

enum BlitterDeserializer implements JsonDeserializer<Blitter>
{
    INSTANCE;


    public Blitter deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!json.isJsonObject()) {
            throw new JsonParseException("Blitters must be objects");
        }
        JsonObject root = json.getAsJsonObject();
        String texture = GsonHelper.getAsString((JsonObject)root, (String)"texture");
        int textureWidth = GsonHelper.getAsInt((JsonObject)root, (String)"textureWidth", (int)256);
        int textureHeight = GsonHelper.getAsInt((JsonObject)root, (String)"textureHeight", (int)256);
        Blitter blitter = texture.contains(":") ? new Blitter(ResourceLocation.parse((String)texture), textureWidth, textureHeight) : Blitter.texture(texture, textureWidth, textureHeight);
        if (root.has("srcRect")) {
            Rect2i srcRect = (Rect2i)context.deserialize(root.get("srcRect"), Rect2i.class);
            blitter = blitter.src(srcRect);
        }
        return blitter;
    }
}

