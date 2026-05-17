/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.neoforged.bus.api.Event
 *  net.neoforged.fml.ModLoader
 *  net.neoforged.neoforge.capabilities.BlockCapability
 *  net.neoforged.neoforge.capabilities.Capabilities$EnergyStorage
 *  net.neoforged.neoforge.capabilities.Capabilities$FluidHandler
 *  net.neoforged.neoforge.capabilities.Capabilities$ItemHandler
 *  net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent
 *  net.neoforged.neoforge.energy.IEnergyStorage
 *  net.neoforged.neoforge.fluids.capability.IFluidHandler
 *  net.neoforged.neoforge.items.IItemHandler
 */
package appeng.init;

import appeng.api.AECapabilities;
import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.parts.RegisterPartCapabilitiesEvent;
import appeng.api.parts.RegisterPartCapabilitiesEventInternal;
import appeng.blockentity.AEBaseInvBlockEntity;
import appeng.blockentity.misc.ChargerBlockEntity;
import appeng.blockentity.misc.GrowthAcceleratorBlockEntity;
import appeng.blockentity.misc.InscriberBlockEntity;
import appeng.blockentity.powersink.AEBasePoweredBlockEntity;
import appeng.blockentity.storage.MEChestBlockEntity;
import appeng.core.definitions.AEBlockEntities;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.ItemDefinition;
import appeng.helpers.externalstorage.GenericStackFluidStorage;
import appeng.helpers.externalstorage.GenericStackItemStorage;
import appeng.items.tools.powered.powersink.PoweredItemCapabilities;
import appeng.parts.crafting.PatternProviderPart;
import appeng.parts.encoding.PatternEncodingTerminalPart;
import appeng.parts.misc.InterfacePart;
import appeng.parts.networking.EnergyAcceptorPart;
import appeng.parts.p2p.FEP2PTunnelPart;
import appeng.parts.p2p.FluidP2PTunnelPart;
import appeng.parts.p2p.ItemP2PTunnelPart;
import java.util.function.Function;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

public final class InitCapabilityProviders {
    private InitCapabilityProviders() {
    }

    public static void markProxyableCapabilities(RegisterCapabilitiesEvent event) {
        event.setProxyable(AECapabilities.ME_STORAGE);
        event.setProxyable(AECapabilities.CRAFTING_MACHINE);
        event.setProxyable(AECapabilities.GENERIC_INTERNAL_INV);
        event.setNonProxyable(AECapabilities.IN_WORLD_GRID_NODE_HOST);
        event.setNonProxyable(AECapabilities.CRANKABLE);
    }

    public static void register(RegisterCapabilitiesEvent event) {
        RegisterPartCapabilitiesEvent partEvent = new RegisterPartCapabilitiesEvent();
        partEvent.addHostType(AEBlockEntities.CABLE_BUS.get());
        InitCapabilityProviders.registerPartCapabilities(partEvent);
        ModLoader.postEvent((Event)partEvent);
        RegisterPartCapabilitiesEventInternal.register(partEvent, event);
        InitCapabilityProviders.initInterface(event);
        InitCapabilityProviders.initPatternProvider(event);
        InitCapabilityProviders.initCondenser(event);
        InitCapabilityProviders.initMEChest(event);
        InitCapabilityProviders.initMisc(event);
        InitCapabilityProviders.initPoweredItem(event);
        InitCapabilityProviders.initCrankable(event);
        for (BlockEntityType<AEBaseInvBlockEntity> blockEntityType : AEBlockEntities.getSubclassesOf(AEBaseInvBlockEntity.class)) {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, blockEntityType, AEBaseInvBlockEntity::getExposedItemHandler);
        }
        for (BlockEntityType<AEBaseInvBlockEntity> blockEntityType : AEBlockEntities.getSubclassesOf(AEBasePoweredBlockEntity.class)) {
            event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, blockEntityType, AEBasePoweredBlockEntity::getEnergyStorage);
        }
        for (BlockEntityType blockEntityType : AEBlockEntities.getImplementorsOf(IInWorldGridNodeHost.class)) {
            event.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST, blockEntityType, (object, context) -> (IInWorldGridNodeHost)object);
        }
    }

    public static void registerGenericAdapters(RegisterCapabilitiesEvent event) {
        for (Block block : BuiltInRegistries.BLOCK) {
            if (!event.isBlockRegistered(AECapabilities.GENERIC_INTERNAL_INV, block)) continue;
            InitCapabilityProviders.registerGenericInvAdapter(event, block, Capabilities.ItemHandler.BLOCK, GenericStackItemStorage::new);
            InitCapabilityProviders.registerGenericInvAdapter(event, block, Capabilities.FluidHandler.BLOCK, GenericStackFluidStorage::new);
        }
    }

    private static <T> void registerGenericInvAdapter(RegisterCapabilitiesEvent event, Block block, BlockCapability<T, Direction> capability, Function<GenericInternalInventory, T> adapter) {
        event.registerBlock(capability, (level, pos, state, blockEntity, context) -> {
            GenericInternalInventory genericInv = (GenericInternalInventory)level.getCapability(AECapabilities.GENERIC_INTERNAL_INV, pos, state, blockEntity, context);
            if (genericInv != null) {
                return adapter.apply(genericInv);
            }
            return null;
        }, new Block[]{block});
    }

    private static void initInterface(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(AECapabilities.GENERIC_INTERNAL_INV, AEBlockEntities.INTERFACE.get(), (be, context) -> be.getInterfaceLogic().getStorage());
        event.registerBlockEntity(AECapabilities.ME_STORAGE, AEBlockEntities.INTERFACE.get(), (blockEntity, context) -> blockEntity.getInterfaceLogic().getInventory());
    }

    private static void initPatternProvider(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(AECapabilities.GENERIC_INTERNAL_INV, AEBlockEntities.PATTERN_PROVIDER.get(), (blockEntity, context) -> blockEntity.getLogic().getReturnInv());
    }

    private static void initCondenser(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, AEBlockEntities.CONDENSER.get(), (blockEntity, context) -> blockEntity.getExternalInv().toItemHandler());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, AEBlockEntities.CONDENSER.get(), (blockEntity, context) -> blockEntity.getFluidHandler());
        event.registerBlockEntity(AECapabilities.ME_STORAGE, AEBlockEntities.CONDENSER.get(), (blockEntity, context) -> blockEntity.getMEStorage());
    }

    private static void initMEChest(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, AEBlockEntities.ME_CHEST.get(), MEChestBlockEntity::getFluidHandler);
        event.registerBlockEntity(AECapabilities.ME_STORAGE, AEBlockEntities.ME_CHEST.get(), MEChestBlockEntity::getMEStorage);
    }

    private static void initMisc(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(AECapabilities.CRAFTING_MACHINE, AEBlockEntities.MOLECULAR_ASSEMBLER.get(), (object, context) -> object);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, AEBlockEntities.DEBUG_ITEM_GEN.get(), (object, context) -> object.getItemHandler());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, AEBlockEntities.DEBUG_ENERGY_GEN.get(), (object, context) -> object);
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, AEBlockEntities.SKY_STONE_TANK.get(), (object, context) -> object.getFluidHandler());
    }

    private static void initPoweredItem(RegisterCapabilitiesEvent event) {
        InitCapabilityProviders.registerPowerStorageItem(event, AEItems.ENTROPY_MANIPULATOR);
        InitCapabilityProviders.registerPowerStorageItem(event, AEItems.CHARGED_STAFF);
        InitCapabilityProviders.registerPowerStorageItem(event, AEItems.COLOR_APPLICATOR);
        InitCapabilityProviders.registerPowerStorageItem(event, AEItems.PORTABLE_ITEM_CELL1K);
        InitCapabilityProviders.registerPowerStorageItem(event, AEItems.PORTABLE_ITEM_CELL4K);
        InitCapabilityProviders.registerPowerStorageItem(event, AEItems.PORTABLE_ITEM_CELL16K);
        InitCapabilityProviders.registerPowerStorageItem(event, AEItems.PORTABLE_ITEM_CELL64K);
        InitCapabilityProviders.registerPowerStorageItem(event, AEItems.PORTABLE_ITEM_CELL256K);
        InitCapabilityProviders.registerPowerStorageItem(event, AEItems.PORTABLE_FLUID_CELL1K);
        InitCapabilityProviders.registerPowerStorageItem(event, AEItems.PORTABLE_FLUID_CELL4K);
        InitCapabilityProviders.registerPowerStorageItem(event, AEItems.PORTABLE_FLUID_CELL16K);
        InitCapabilityProviders.registerPowerStorageItem(event, AEItems.PORTABLE_FLUID_CELL64K);
        InitCapabilityProviders.registerPowerStorageItem(event, AEItems.PORTABLE_FLUID_CELL256K);
        InitCapabilityProviders.registerPowerStorageItem(event, AEItems.MATTER_CANNON);
        InitCapabilityProviders.registerPowerStorageItem(event, AEItems.WIRELESS_TERMINAL);
        InitCapabilityProviders.registerPowerStorageItem(event, AEItems.WIRELESS_CRAFTING_TERMINAL);
    }

    private static <T extends Item> void registerPowerStorageItem(RegisterCapabilitiesEvent event, ItemDefinition<T> definition) {
        IAEItemPowerStorage powerStorage = (IAEItemPowerStorage)definition.get();
        event.registerItem(Capabilities.EnergyStorage.ITEM, (object, context) -> new PoweredItemCapabilities((ItemStack)object, powerStorage), new ItemLike[]{definition});
    }

    private static void initCrankable(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(AECapabilities.CRANKABLE, AEBlockEntities.CHARGER.get(), ChargerBlockEntity::getCrankable);
        event.registerBlockEntity(AECapabilities.CRANKABLE, AEBlockEntities.INSCRIBER.get(), InscriberBlockEntity::getCrankable);
        event.registerBlockEntity(AECapabilities.CRANKABLE, AEBlockEntities.GROWTH_ACCELERATOR.get(), GrowthAcceleratorBlockEntity::getCrankable);
    }

    private static void registerPartCapabilities(RegisterPartCapabilitiesEvent event) {
        event.register(Capabilities.ItemHandler.BLOCK, (part, direction) -> part.getLogic().getBlankPatternInv().toItemHandler(), PatternEncodingTerminalPart.class);
        event.register(AECapabilities.GENERIC_INTERNAL_INV, (part, context) -> part.getLogic().getReturnInv(), PatternProviderPart.class);
        event.register(AECapabilities.GENERIC_INTERNAL_INV, (part, context) -> part.getInterfaceLogic().getStorage(), InterfacePart.class);
        event.register(AECapabilities.ME_STORAGE, (part, context) -> part.getInterfaceLogic().getInventory(), InterfacePart.class);
        event.register(Capabilities.ItemHandler.BLOCK, (part, context) -> (IItemHandler)part.getExposedApi(), ItemP2PTunnelPart.class);
        event.register(Capabilities.EnergyStorage.BLOCK, (part, context) -> (IEnergyStorage)part.getExposedApi(), FEP2PTunnelPart.class);
        event.register(Capabilities.FluidHandler.BLOCK, (part, context) -> (IFluidHandler)part.getExposedApi(), FluidP2PTunnelPart.class);
        event.register(Capabilities.EnergyStorage.BLOCK, (part, context) -> part.getEnergyStorage(), EnergyAcceptorPart.class);
    }
}

