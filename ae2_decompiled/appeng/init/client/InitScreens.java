/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.MenuType
 *  net.neoforged.neoforge.client.event.RegisterMenuScreensEvent
 */
package appeng.init.client;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.implementations.CellWorkbenchScreen;
import appeng.client.gui.implementations.CondenserScreen;
import appeng.client.gui.implementations.DriveScreen;
import appeng.client.gui.implementations.EnergyLevelEmitterScreen;
import appeng.client.gui.implementations.FormationPlaneScreen;
import appeng.client.gui.implementations.IOBusScreen;
import appeng.client.gui.implementations.IOPortScreen;
import appeng.client.gui.implementations.InscriberScreen;
import appeng.client.gui.implementations.InterfaceScreen;
import appeng.client.gui.implementations.MEChestScreen;
import appeng.client.gui.implementations.MolecularAssemblerScreen;
import appeng.client.gui.implementations.PatternProviderScreen;
import appeng.client.gui.implementations.PriorityScreen;
import appeng.client.gui.implementations.QNBScreen;
import appeng.client.gui.implementations.QuartzKnifeScreen;
import appeng.client.gui.implementations.SkyChestScreen;
import appeng.client.gui.implementations.SpatialAnchorScreen;
import appeng.client.gui.implementations.SpatialIOPortScreen;
import appeng.client.gui.implementations.StorageBusScreen;
import appeng.client.gui.implementations.StorageLevelEmitterScreen;
import appeng.client.gui.implementations.VibrationChamberScreen;
import appeng.client.gui.implementations.WirelessAccessPointScreen;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.me.crafting.CraftAmountScreen;
import appeng.client.gui.me.crafting.CraftConfirmScreen;
import appeng.client.gui.me.crafting.CraftingCPUScreen;
import appeng.client.gui.me.crafting.CraftingStatusScreen;
import appeng.client.gui.me.crafting.SetStockAmountScreen;
import appeng.client.gui.me.items.CraftingTermScreen;
import appeng.client.gui.me.items.PatternEncodingTermScreen;
import appeng.client.gui.me.networktool.NetworkStatusScreen;
import appeng.client.gui.me.networktool.NetworkToolScreen;
import appeng.client.gui.me.patternaccess.PatternAccessTermScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.StyleManager;
import appeng.menu.AEBaseMenu;
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
import com.google.common.annotations.VisibleForTesting;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class InitScreens {
    @VisibleForTesting
    static final Map<MenuType<?>, String> MENU_STYLES = new IdentityHashMap();

    private InitScreens() {
    }

    public static void init(RegisterMenuScreensEvent event) {
        InitScreens.register(event, QNBMenu.TYPE, QNBScreen::new, "/screens/qnb.json");
        InitScreens.register(event, SkyChestMenu.TYPE, SkyChestScreen::new, "/screens/sky_chest.json");
        InitScreens.register(event, MEChestMenu.TYPE, MEChestScreen::new, "/screens/me_chest.json");
        InitScreens.register(event, WirelessAccessPointMenu.TYPE, WirelessAccessPointScreen::new, "/screens/wireless_access_point.json");
        InitScreens.register(event, NetworkStatusMenu.NETWORK_TOOL_TYPE, NetworkStatusScreen::new, "/screens/network_status.json");
        InitScreens.register(event, NetworkStatusMenu.CONTROLLER_TYPE, NetworkStatusScreen::new, "/screens/network_status.json");
        InitScreens.register(event, CraftingCPUMenu.TYPE, CraftingCPUScreen::new, "/screens/crafting_cpu.json");
        InitScreens.register(event, NetworkToolMenu.TYPE, NetworkToolScreen::new, "/screens/network_tool.json");
        InitScreens.register(event, QuartzKnifeMenu.TYPE, QuartzKnifeScreen::new, "/screens/quartz_knife.json");
        InitScreens.register(event, DriveMenu.TYPE, DriveScreen::new, "/screens/drive.json");
        InitScreens.register(event, VibrationChamberMenu.TYPE, VibrationChamberScreen::new, "/screens/vibration_chamber.json");
        InitScreens.register(event, CondenserMenu.TYPE, CondenserScreen::new, "/screens/condenser.json");
        InitScreens.register(event, InterfaceMenu.TYPE, InterfaceScreen::new, "/screens/interface.json");
        InitScreens.register(event, IOBusMenu.EXPORT_TYPE, IOBusScreen::new, "/screens/export_bus.json");
        InitScreens.register(event, IOBusMenu.IMPORT_TYPE, IOBusScreen::new, "/screens/import_bus.json");
        InitScreens.register(event, IOPortMenu.TYPE, IOPortScreen::new, "/screens/io_port.json");
        InitScreens.register(event, StorageBusMenu.TYPE, StorageBusScreen::new, "/screens/storage_bus.json");
        InitScreens.register(event, SetStockAmountMenu.TYPE, SetStockAmountScreen::new, "/screens/set_stock_amount.json");
        InitScreens.register(event, FormationPlaneMenu.TYPE, FormationPlaneScreen::new, "/screens/formation_plane.json");
        InitScreens.register(event, PriorityMenu.TYPE, PriorityScreen::new, "/screens/priority.json");
        InitScreens.register(event, StorageLevelEmitterMenu.TYPE, StorageLevelEmitterScreen::new, "/screens/level_emitter.json");
        InitScreens.register(event, EnergyLevelEmitterMenu.TYPE, EnergyLevelEmitterScreen::new, "/screens/energy_level_emitter.json");
        InitScreens.register(event, SpatialIOPortMenu.TYPE, SpatialIOPortScreen::new, "/screens/spatial_io_port.json");
        InitScreens.register(event, InscriberMenu.TYPE, InscriberScreen::new, "/screens/inscriber.json");
        InitScreens.register(event, CellWorkbenchMenu.TYPE, CellWorkbenchScreen::new, "/screens/cell_workbench.json");
        InitScreens.register(event, PatternProviderMenu.TYPE, PatternProviderScreen::new, "/screens/pattern_provider.json");
        InitScreens.register(event, MolecularAssemblerMenu.TYPE, MolecularAssemblerScreen::new, "/screens/molecular_assembler.json");
        InitScreens.register(event, CraftAmountMenu.TYPE, CraftAmountScreen::new, "/screens/craft_amount.json");
        InitScreens.register(event, CraftConfirmMenu.TYPE, CraftConfirmScreen::new, "/screens/craft_confirm.json");
        InitScreens.register(event, CraftingStatusMenu.TYPE, CraftingStatusScreen::new, "/screens/crafting_status.json");
        InitScreens.register(event, SpatialAnchorMenu.TYPE, SpatialAnchorScreen::new, "/screens/spatial_anchor.json");
        InitScreens.register(event, MEStorageMenu.TYPE, MEStorageScreen::new, "/screens/terminals/terminal.json");
        InitScreens.register(event, BasicCellChestMenu.TYPE, MEStorageScreen::new, "/screens/terminals/terminal.json");
        InitScreens.register(event, MEStorageMenu.PORTABLE_ITEM_CELL_TYPE, MEStorageScreen::new, "/screens/terminals/portable_item_cell.json");
        InitScreens.register(event, MEStorageMenu.PORTABLE_FLUID_CELL_TYPE, MEStorageScreen::new, "/screens/terminals/portable_fluid_cell.json");
        InitScreens.register(event, MEStorageMenu.WIRELESS_TYPE, MEStorageScreen::new, "/screens/terminals/wireless_terminal.json");
        InitScreens.register(event, CraftingTermMenu.TYPE, CraftingTermScreen::new, "/screens/terminals/crafting_terminal.json");
        InitScreens.register(event, WirelessCraftingTermMenu.TYPE, CraftingTermScreen::new, "/screens/terminals/crafting_terminal.json");
        InitScreens.register(event, PatternEncodingTermMenu.TYPE, PatternEncodingTermScreen::new, "/screens/terminals/pattern_encoding_terminal.json");
        InitScreens.register(event, PatternAccessTermMenu.TYPE, PatternAccessTermScreen::new, "/screens/terminals/pattern_access_terminal.json");
    }

    public static <M extends AEBaseMenu, U extends AEBaseScreen<M>> void register(RegisterMenuScreensEvent event, MenuType<M> type, StyledScreenFactory<M, U> factory, String stylePath) {
        MENU_STYLES.put(type, stylePath);
        event.register(type, (menu, playerInv, title) -> {
            ScreenStyle style = StyleManager.loadStyleDoc(stylePath);
            return (AEBaseScreen)((Object)((Object)factory.create(menu, playerInv, title, style)));
        });
    }

    @FunctionalInterface
    public static interface StyledScreenFactory<T extends AbstractContainerMenu, U extends Screen> {
        public U create(T var1, Inventory var2, Component var3, ScreenStyle var4);
    }
}

