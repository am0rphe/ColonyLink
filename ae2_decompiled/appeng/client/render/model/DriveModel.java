/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.client.resources.model.Material
 *  net.minecraft.client.resources.model.ModelBaker
 *  net.minecraft.client.resources.model.ModelState
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Items
 *  org.jetbrains.annotations.Nullable
 */
package appeng.client.render.model;

import appeng.api.client.StorageCellModels;
import appeng.client.render.BasicUnbakedModel;
import appeng.client.render.model.DriveBakedModel;
import appeng.init.internal.InitStorageCells;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

public class DriveModel
implements BasicUnbakedModel {
    private static final ResourceLocation MODEL_BASE = ResourceLocation.parse((String)"ae2:block/drive/drive_base");
    private static final ResourceLocation MODEL_CELL_EMPTY = ResourceLocation.parse((String)"ae2:block/drive/drive_cell_empty");

    @Nullable
    public BakedModel bake(ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform) {
        IdentityHashMap<Item, BakedModel> cellModels = new IdentityHashMap<Item, BakedModel>();
        for (Map.Entry<Item, ResourceLocation> entry : StorageCellModels.models().entrySet()) {
            BakedModel cellModel = baker.bake(entry.getValue(), modelTransform);
            cellModels.put(entry.getKey(), cellModel);
        }
        BakedModel baseModel = baker.bake(MODEL_BASE, modelTransform);
        BakedModel defaultCell = baker.bake(StorageCellModels.getDefaultModel(), modelTransform);
        cellModels.put(Items.AIR, baker.bake(MODEL_CELL_EMPTY, modelTransform));
        return new DriveBakedModel(modelTransform.getRotation(), baseModel, cellModels, defaultCell);
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return ImmutableSet.builder().add((Object)StorageCellModels.getDefaultModel()).addAll(InitStorageCells.getModels()).addAll(StorageCellModels.models().values()).build();
    }
}

