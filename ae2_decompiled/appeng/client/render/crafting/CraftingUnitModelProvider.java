/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.texture.TextureAtlas
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.client.resources.model.Material
 */
package appeng.client.render.crafting;

import appeng.block.crafting.CraftingUnitType;
import appeng.client.render.crafting.AbstractCraftingUnitModelProvider;
import appeng.client.render.crafting.LightBakedModel;
import appeng.client.render.crafting.MonitorBakedModel;
import appeng.client.render.crafting.UnitBakedModel;
import appeng.core.AppEng;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;

public class CraftingUnitModelProvider
extends AbstractCraftingUnitModelProvider<CraftingUnitType> {
    private static final List<Material> MATERIALS = new ArrayList<Material>();
    protected static final Material RING_CORNER = CraftingUnitModelProvider.texture("ring_corner");
    protected static final Material RING_SIDE_HOR = CraftingUnitModelProvider.texture("ring_side_hor");
    protected static final Material RING_SIDE_VER = CraftingUnitModelProvider.texture("ring_side_ver");
    protected static final Material UNIT_BASE = CraftingUnitModelProvider.texture("unit_base");
    protected static final Material LIGHT_BASE = CraftingUnitModelProvider.texture("light_base");
    protected static final Material ACCELERATOR_LIGHT = CraftingUnitModelProvider.texture("accelerator_light");
    protected static final Material STORAGE_1K_LIGHT = CraftingUnitModelProvider.texture("1k_storage_light");
    protected static final Material STORAGE_4K_LIGHT = CraftingUnitModelProvider.texture("4k_storage_light");
    protected static final Material STORAGE_16K_LIGHT = CraftingUnitModelProvider.texture("16k_storage_light");
    protected static final Material STORAGE_64K_LIGHT = CraftingUnitModelProvider.texture("64k_storage_light");
    protected static final Material STORAGE_256K_LIGHT = CraftingUnitModelProvider.texture("256k_storage_light");
    protected static final Material MONITOR_BASE = CraftingUnitModelProvider.texture("monitor_base");
    protected static final Material MONITOR_LIGHT_DARK = CraftingUnitModelProvider.texture("monitor_light_dark");
    protected static final Material MONITOR_LIGHT_MEDIUM = CraftingUnitModelProvider.texture("monitor_light_medium");
    protected static final Material MONITOR_LIGHT_BRIGHT = CraftingUnitModelProvider.texture("monitor_light_bright");

    public CraftingUnitModelProvider(CraftingUnitType type) {
        super(type);
    }

    @Override
    public List<Material> getMaterials() {
        return Collections.unmodifiableList(MATERIALS);
    }

    public TextureAtlasSprite getLightMaterial(Function<Material, TextureAtlasSprite> textureGetter) {
        return switch ((CraftingUnitType)this.type) {
            case CraftingUnitType.ACCELERATOR -> textureGetter.apply(ACCELERATOR_LIGHT);
            case CraftingUnitType.STORAGE_1K -> textureGetter.apply(STORAGE_1K_LIGHT);
            case CraftingUnitType.STORAGE_4K -> textureGetter.apply(STORAGE_4K_LIGHT);
            case CraftingUnitType.STORAGE_16K -> textureGetter.apply(STORAGE_16K_LIGHT);
            case CraftingUnitType.STORAGE_64K -> textureGetter.apply(STORAGE_64K_LIGHT);
            case CraftingUnitType.STORAGE_256K -> textureGetter.apply(STORAGE_256K_LIGHT);
            default -> throw new IllegalArgumentException("Crafting unit type " + String.valueOf(this.type) + " does not use a light texture.");
        };
    }

    @Override
    public BakedModel getBakedModel(Function<Material, TextureAtlasSprite> spriteGetter) {
        TextureAtlasSprite ringCorner = spriteGetter.apply(RING_CORNER);
        TextureAtlasSprite ringSideHor = spriteGetter.apply(RING_SIDE_HOR);
        TextureAtlasSprite ringSideVer = spriteGetter.apply(RING_SIDE_VER);
        return switch ((CraftingUnitType)this.type) {
            default -> throw new MatchException(null, null);
            case CraftingUnitType.UNIT -> new UnitBakedModel(ringCorner, ringSideHor, ringSideVer, spriteGetter.apply(UNIT_BASE));
            case CraftingUnitType.ACCELERATOR, CraftingUnitType.STORAGE_1K, CraftingUnitType.STORAGE_4K, CraftingUnitType.STORAGE_16K, CraftingUnitType.STORAGE_64K, CraftingUnitType.STORAGE_256K -> new LightBakedModel(ringCorner, ringSideHor, ringSideVer, spriteGetter.apply(LIGHT_BASE), this.getLightMaterial(spriteGetter));
            case CraftingUnitType.MONITOR -> new MonitorBakedModel(ringCorner, ringSideHor, ringSideVer, spriteGetter.apply(UNIT_BASE), spriteGetter.apply(MONITOR_BASE), spriteGetter.apply(MONITOR_LIGHT_DARK), spriteGetter.apply(MONITOR_LIGHT_MEDIUM), spriteGetter.apply(MONITOR_LIGHT_BRIGHT));
        };
    }

    private static Material texture(String name) {
        Material mat = new Material(TextureAtlas.LOCATION_BLOCKS, AppEng.makeId("block/crafting/" + name));
        MATERIALS.add(mat);
        return mat;
    }
}

