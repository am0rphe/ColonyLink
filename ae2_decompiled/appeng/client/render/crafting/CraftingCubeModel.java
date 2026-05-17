/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.client.resources.model.Material
 *  net.minecraft.client.resources.model.ModelBaker
 *  net.minecraft.client.resources.model.ModelState
 *  net.minecraft.client.resources.model.UnbakedModel
 *  net.minecraft.resources.ResourceLocation
 *  org.jetbrains.annotations.Nullable
 */
package appeng.client.render.crafting;

import appeng.client.render.BasicUnbakedModel;
import appeng.client.render.crafting.AbstractCraftingUnitModelProvider;
import java.util.function.Function;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class CraftingCubeModel
implements BasicUnbakedModel {
    private final AbstractCraftingUnitModelProvider<?> provider;

    public CraftingCubeModel(AbstractCraftingUnitModelProvider<?> provider) {
        this.provider = provider;
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> function) {
    }

    @Nullable
    public BakedModel bake(ModelBaker loader, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState) {
        return this.provider.getBakedModel(spriteGetter);
    }
}

