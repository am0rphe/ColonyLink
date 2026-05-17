/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.client.resources.model.Material
 */
package appeng.client.render.crafting;

import appeng.block.crafting.ICraftingUnitType;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;

public abstract class AbstractCraftingUnitModelProvider<T extends ICraftingUnitType> {
    protected final T type;

    public AbstractCraftingUnitModelProvider(T type) {
        this.type = type;
    }

    public abstract List<Material> getMaterials();

    public abstract BakedModel getBakedModel(Function<Material, TextureAtlasSprite> var1);
}

