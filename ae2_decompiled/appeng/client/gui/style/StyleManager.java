/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Preconditions
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.packs.resources.PreparableReloadListener
 *  net.minecraft.server.packs.resources.ReloadableResourceManager
 *  net.minecraft.server.packs.resources.Resource
 *  net.minecraft.server.packs.resources.ResourceManager
 *  net.minecraft.server.packs.resources.ResourceManagerReloadListener
 */
package appeng.client.gui.style;

import appeng.client.gui.style.ScreenStyle;
import appeng.core.AppEng;
import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public final class StyleManager {
    private static final Map<String, ScreenStyle> styleCache = new HashMap<String, ScreenStyle>();
    public static final String PROP_INCLUDES = "includes";
    private static ResourceManager resourceManager;

    private static String getBasePath(String path) {
        int lastSep = path.lastIndexOf(47);
        if (lastSep == -1) {
            return "";
        }
        return path.substring(0, lastSep + 1);
    }

    public static ScreenStyle loadStyleDoc(String path) {
        ScreenStyle style;
        try {
            style = StyleManager.loadStyleDocInternal(path);
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException("Failed to find Screen JSON file: " + path + ": " + e.getMessage());
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to read Screen JSON file: " + path, e);
        }
        style.validate();
        return style;
    }

    private static JsonObject loadMergedJsonTree(String path, Set<String> loadedFiles, Set<String> resourcePacks) throws IOException {
        JsonObject document;
        Preconditions.checkArgument((boolean)path.startsWith("/"), (Object)"Path needs to start with slash");
        if (path.contains("..")) {
            path = URI.create(path).normalize().toString();
        }
        if (!loadedFiles.add(path)) {
            throw new IllegalStateException("Recursive style includes: " + String.valueOf(loadedFiles));
        }
        if (resourceManager == null) {
            throw new IllegalStateException("ResourceManager was not set. Was initialize called?");
        }
        String basePath = StyleManager.getBasePath(path);
        ResourceLocation resourceId = AppEng.makeId(path.substring(1));
        Resource resource = (Resource)resourceManager.getResource(resourceId).orElseThrow(() -> new FileNotFoundException(resourceId.toString()));
        resourcePacks.add(resource.sourcePackId());
        try (BufferedReader reader = resourceManager.openAsReader(resourceId);){
            document = (JsonObject)ScreenStyle.GSON.fromJson((Reader)reader, JsonObject.class);
        }
        if (document.has(PROP_INCLUDES)) {
            String[] includes = (String[])ScreenStyle.GSON.fromJson(document.get(PROP_INCLUDES), String[].class);
            ArrayList<JsonObject> layers = new ArrayList<JsonObject>();
            for (String include : includes) {
                layers.add(StyleManager.loadMergedJsonTree(basePath + include, loadedFiles, resourcePacks));
            }
            layers.add(document);
            document = StyleManager.combineLayers(layers);
        }
        return document;
    }

    private static JsonObject combineLayers(List<JsonObject> layers) {
        JsonObject result = new JsonObject();
        for (JsonObject layer : layers) {
            for (Map.Entry entry : layer.entrySet()) {
                result.add((String)entry.getKey(), (JsonElement)entry.getValue());
            }
        }
        StyleManager.mergeObjectKeys("slots", layers, result);
        StyleManager.mergeObjectKeys("text", layers, result);
        StyleManager.mergeObjectKeys("palette", layers, result);
        StyleManager.mergeObjectKeys("images", layers, result);
        StyleManager.mergeObjectKeys("terminalStyle", layers, result);
        StyleManager.mergeObjectKeys("widgets", layers, result);
        return result;
    }

    private static void mergeObjectKeys(String propertyName, List<JsonObject> layers, JsonObject target) throws JsonParseException {
        JsonObject mergedObject = null;
        for (JsonObject layer : layers) {
            JsonElement layerEl = layer.get(propertyName);
            if (layerEl == null) continue;
            if (!layerEl.isJsonObject()) {
                throw new JsonParseException("Expected " + propertyName + " to be an object, but was: " + String.valueOf(layerEl));
            }
            JsonObject layerObj = layerEl.getAsJsonObject();
            if (mergedObject == null) {
                mergedObject = new JsonObject();
            }
            for (Map.Entry entry : layerObj.entrySet()) {
                mergedObject.add((String)entry.getKey(), (JsonElement)entry.getValue());
            }
        }
        if (mergedObject != null) {
            target.add(propertyName, mergedObject);
        }
    }

    private static ScreenStyle loadStyleDocInternal(String path) throws IOException {
        ScreenStyle style = styleCache.get(path);
        if (style != null) {
            return style;
        }
        HashSet<String> resourcePacks = new HashSet<String>();
        try {
            JsonObject document = StyleManager.loadMergedJsonTree(path, new HashSet<String>(), resourcePacks);
            style = (ScreenStyle)ScreenStyle.GSON.fromJson((JsonElement)document, ScreenStyle.class);
            style.validate();
        }
        catch (IOException e) {
            throw e;
        }
        catch (Exception e) {
            throw new JsonParseException("Failed to load style from " + path + " (packs: " + String.valueOf(resourcePacks) + ")", (Throwable)e);
        }
        styleCache.put(path, style);
        return style;
    }

    public static void initialize(ResourceManager resourceManager) {
        if (resourceManager instanceof ReloadableResourceManager) {
            ((ReloadableResourceManager)resourceManager).registerReloadListener((PreparableReloadListener)new ReloadListener());
        }
        StyleManager.setResourceManager(resourceManager);
    }

    private static void setResourceManager(ResourceManager resourceManager) {
        StyleManager.resourceManager = resourceManager;
        styleCache.clear();
    }

    private static class ReloadListener
    implements ResourceManagerReloadListener {
        private ReloadListener() {
        }

        public void onResourceManagerReload(ResourceManager p_10758_) {
            StyleManager.setResourceManager(resourceManager);
        }
    }
}

