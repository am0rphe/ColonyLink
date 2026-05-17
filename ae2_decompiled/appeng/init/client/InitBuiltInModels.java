/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.resources.model.UnbakedModel
 *  net.minecraft.resources.ResourceLocation
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package appeng.init.client;

import appeng.block.crafting.CraftingUnitType;
import appeng.block.paint.PaintSplotchesModel;
import appeng.block.qnb.QnbFormedModel;
import appeng.client.render.FacadeItemModel;
import appeng.client.render.cablebus.CableBusModel;
import appeng.client.render.cablebus.P2PTunnelFrequencyModel;
import appeng.client.render.crafting.CraftingCubeModel;
import appeng.client.render.crafting.CraftingUnitModelProvider;
import appeng.client.render.model.ColorApplicatorModel;
import appeng.client.render.model.DriveModel;
import appeng.client.render.model.GlassModel;
import appeng.client.render.model.MemoryCardModel;
import appeng.client.render.model.MeteoriteCompassModel;
import appeng.client.render.tesr.spatial.SpatialPylonModel;
import appeng.core.AppEng;
import appeng.hooks.BuiltInModelHooks;
import appeng.parts.automation.PlaneModel;
import java.util.function.Supplier;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(value=Dist.CLIENT)
public final class InitBuiltInModels {
    private InitBuiltInModels() {
    }

    public static void init() {
        InitBuiltInModels.addBuiltInModel("block/cable_bus", CableBusModel::new);
        InitBuiltInModels.addBuiltInModel("block/quartz_glass", GlassModel::new);
        InitBuiltInModels.addBuiltInModel("item/meteorite_compass", MeteoriteCompassModel::new);
        InitBuiltInModels.addBuiltInModel("item/memory_card", MemoryCardModel::new);
        InitBuiltInModels.addBuiltInModel("block/drive", DriveModel::new);
        InitBuiltInModels.addBuiltInModel("color_applicator", ColorApplicatorModel::new);
        InitBuiltInModels.addBuiltInModel("block/spatial_pylon", SpatialPylonModel::new);
        InitBuiltInModels.addBuiltInModel("block/paint", PaintSplotchesModel::new);
        InitBuiltInModels.addBuiltInModel("block/qnb/qnb_formed", QnbFormedModel::new);
        InitBuiltInModels.addBuiltInModel("part/p2p/p2p_tunnel_frequency", P2PTunnelFrequencyModel::new);
        InitBuiltInModels.addBuiltInModel("item/facade", FacadeItemModel::new);
        InitBuiltInModels.addPlaneModel("part/annihilation_plane", "part/annihilation_plane");
        InitBuiltInModels.addPlaneModel("part/annihilation_plane_on", "part/annihilation_plane_on");
        InitBuiltInModels.addPlaneModel("part/identity_annihilation_plane", "part/identity_annihilation_plane");
        InitBuiltInModels.addPlaneModel("part/identity_annihilation_plane_on", "part/identity_annihilation_plane_on");
        InitBuiltInModels.addPlaneModel("part/formation_plane", "part/formation_plane");
        InitBuiltInModels.addPlaneModel("part/formation_plane_on", "part/formation_plane_on");
        InitBuiltInModels.addBuiltInModel("block/crafting/1k_storage_formed", () -> new CraftingCubeModel(new CraftingUnitModelProvider(CraftingUnitType.STORAGE_1K)));
        InitBuiltInModels.addBuiltInModel("block/crafting/4k_storage_formed", () -> new CraftingCubeModel(new CraftingUnitModelProvider(CraftingUnitType.STORAGE_4K)));
        InitBuiltInModels.addBuiltInModel("block/crafting/16k_storage_formed", () -> new CraftingCubeModel(new CraftingUnitModelProvider(CraftingUnitType.STORAGE_16K)));
        InitBuiltInModels.addBuiltInModel("block/crafting/64k_storage_formed", () -> new CraftingCubeModel(new CraftingUnitModelProvider(CraftingUnitType.STORAGE_64K)));
        InitBuiltInModels.addBuiltInModel("block/crafting/256k_storage_formed", () -> new CraftingCubeModel(new CraftingUnitModelProvider(CraftingUnitType.STORAGE_256K)));
        InitBuiltInModels.addBuiltInModel("block/crafting/accelerator_formed", () -> new CraftingCubeModel(new CraftingUnitModelProvider(CraftingUnitType.ACCELERATOR)));
        InitBuiltInModels.addBuiltInModel("block/crafting/monitor_formed", () -> new CraftingCubeModel(new CraftingUnitModelProvider(CraftingUnitType.MONITOR)));
        InitBuiltInModels.addBuiltInModel("block/crafting/unit_formed", () -> new CraftingCubeModel(new CraftingUnitModelProvider(CraftingUnitType.UNIT)));
    }

    private static void addPlaneModel(String planeName, String frontTexture) {
        ResourceLocation frontTextureId = AppEng.makeId(frontTexture);
        ResourceLocation sidesTextureId = AppEng.makeId("part/plane_sides");
        ResourceLocation backTextureId = AppEng.makeId("part/transition_plane_back");
        InitBuiltInModels.addBuiltInModel(planeName, () -> new PlaneModel(frontTextureId, sidesTextureId, backTextureId));
    }

    private static <T extends UnbakedModel> void addBuiltInModel(String id, Supplier<T> modelFactory) {
        BuiltInModelHooks.addBuiltInModel(AppEng.makeId(id), (UnbakedModel)modelFactory.get());
    }
}

