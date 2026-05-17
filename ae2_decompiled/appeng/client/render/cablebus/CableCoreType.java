/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  net.minecraft.client.renderer.texture.TextureAtlas
 *  net.minecraft.client.resources.model.Material
 */
package appeng.client.render.cablebus;

import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.core.AppEng;
import com.google.common.collect.ImmutableMap;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;

public enum CableCoreType {
    GLASS("part/cable/core/glass"),
    COVERED("part/cable/core/covered"),
    DENSE("part/cable/core/dense_smart");

    private static final Map<AECableType, CableCoreType> cableMapping;
    private final String textureFolder;

    private static Map<AECableType, CableCoreType> generateCableMapping() {
        EnumMap<AECableType, CableCoreType> result = new EnumMap<AECableType, CableCoreType>(AECableType.class);
        result.put(AECableType.GLASS, GLASS);
        result.put(AECableType.COVERED, COVERED);
        result.put(AECableType.SMART, COVERED);
        result.put(AECableType.DENSE_COVERED, DENSE);
        result.put(AECableType.DENSE_SMART, DENSE);
        return ImmutableMap.copyOf(result);
    }

    private CableCoreType(String textureFolder) {
        this.textureFolder = textureFolder;
    }

    public static CableCoreType fromCableType(AECableType cableType) {
        return cableMapping.get((Object)cableType);
    }

    public Material getTexture(AEColor color) {
        return new Material(TextureAtlas.LOCATION_BLOCKS, AppEng.makeId(this.textureFolder + "/" + color.name().toLowerCase(Locale.ROOT)));
    }

    static {
        cableMapping = CableCoreType.generateCableMapping();
    }
}

