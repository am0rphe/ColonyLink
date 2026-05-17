/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Registry
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.inventory.MenuType
 */
package appeng.init;

import appeng.menu.implementations.CellWorkbenchMenu;
import appeng.menu.implementations.CondenserMenu;
import appeng.menu.implementations.DriveMenu;
import appeng.menu.implementations.EnergyLevelEmitterMenu;
import appeng.menu.implementations.FormationPlaneMenu;
import appeng.menu.implementations.IOBusMenu;
import appeng.menu.implementations.IOPortMenu;
import appeng.menu.implementations.InscriberMenu;
import appeng.menu.implementations.InterfaceMenu;
import appeng.menu.implementations.MEChestMenu;
import appeng.menu.implementations.MolecularAssemblerMenu;
import appeng.menu.implementations.PatternAccessTermMenu;
import appeng.menu.implementations.PatternProviderMenu;
import appeng.menu.implementations.PriorityMenu;
import appeng.menu.implementations.QNBMenu;
import appeng.menu.implementations.QuartzKnifeMenu;
import appeng.menu.implementations.SetStockAmountMenu;
import appeng.menu.implementations.SkyChestMenu;
import appeng.menu.implementations.SpatialAnchorMenu;
import appeng.menu.implementations.SpatialIOPortMenu;
import appeng.menu.implementations.StorageBusMenu;
import appeng.menu.implementations.StorageLevelEmitterMenu;
import appeng.menu.implementations.VibrationChamberMenu;
import appeng.menu.implementations.WirelessAccessPointMenu;
import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.me.crafting.CraftAmountMenu;
import appeng.menu.me.crafting.CraftConfirmMenu;
import appeng.menu.me.crafting.CraftingCPUMenu;
import appeng.menu.me.crafting.CraftingStatusMenu;
import appeng.menu.me.items.BasicCellChestMenu;
import appeng.menu.me.items.CraftingTermMenu;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.menu.me.items.WirelessCraftingTermMenu;
import appeng.menu.me.networktool.NetworkStatusMenu;
import appeng.menu.me.networktool.NetworkToolMenu;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;

public final class InitMenuTypes {
    private static final Map<ResourceLocation, MenuType<?>> REGISTRATION_QUEUE = new HashMap();

    private InitMenuTypes() {
    }

    public static void init(Registry<MenuType<?>> registry) {
        InitMenuTypes.registerAll(registry, BasicCellChestMenu.TYPE, CellWorkbenchMenu.TYPE, MEChestMenu.TYPE, CondenserMenu.TYPE, CraftAmountMenu.TYPE, CraftConfirmMenu.TYPE, CraftingCPUMenu.TYPE, CraftingStatusMenu.TYPE, CraftingTermMenu.TYPE, DriveMenu.TYPE, EnergyLevelEmitterMenu.TYPE, FormationPlaneMenu.TYPE, IOBusMenu.EXPORT_TYPE, IOBusMenu.IMPORT_TYPE, IOPortMenu.TYPE, InscriberMenu.TYPE, InterfaceMenu.TYPE, MEStorageMenu.TYPE, MEStorageMenu.PORTABLE_FLUID_CELL_TYPE, MEStorageMenu.PORTABLE_ITEM_CELL_TYPE, MEStorageMenu.WIRELESS_TYPE, MolecularAssemblerMenu.TYPE, NetworkStatusMenu.NETWORK_TOOL_TYPE, NetworkStatusMenu.CONTROLLER_TYPE, NetworkToolMenu.TYPE, PatternAccessTermMenu.TYPE, PatternProviderMenu.TYPE, PatternEncodingTermMenu.TYPE, PriorityMenu.TYPE, QNBMenu.TYPE, QuartzKnifeMenu.TYPE, SetStockAmountMenu.TYPE, SkyChestMenu.TYPE, SpatialAnchorMenu.TYPE, SpatialIOPortMenu.TYPE, StorageBusMenu.TYPE, StorageLevelEmitterMenu.TYPE, VibrationChamberMenu.TYPE, WirelessCraftingTermMenu.TYPE, WirelessAccessPointMenu.TYPE);
    }

    private static void registerAll(Registry<MenuType<?>> registry, MenuType<?> ... types) {
        for (Map.Entry<ResourceLocation, MenuType<?>> entry : REGISTRATION_QUEUE.entrySet()) {
            Registry.register(registry, (ResourceLocation)entry.getKey(), entry.getValue());
        }
        REGISTRATION_QUEUE.clear();
        for (MenuType<?> type : types) {
            if (!registry.getResourceKey(type).isEmpty()) continue;
            throw new IllegalStateException("Menu Type " + String.valueOf(type) + " is not registered");
        }
    }

    public static void queueRegistration(ResourceLocation id, MenuType<?> menuType) {
        if (REGISTRATION_QUEUE.put(id, menuType) != null) {
            throw new IllegalStateException("Duplicate menu id: " + String.valueOf(id));
        }
    }
}

