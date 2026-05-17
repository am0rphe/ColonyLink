/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.item.enchantment.ItemEnchantments
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  org.jetbrains.annotations.Nullable
 */
package appeng.parts.automation;

import appeng.api.behaviors.ExternalStorageStrategy;
import appeng.api.behaviors.PickupStrategy;
import appeng.api.behaviors.PlacementStrategy;
import appeng.api.behaviors.StackExportStrategy;
import appeng.api.behaviors.StackImportStrategy;
import appeng.api.stacks.AEKeyType;
import appeng.api.storage.AEKeyFilter;
import appeng.parts.automation.FluidPickupStrategy;
import appeng.parts.automation.FluidPlacementStrategy;
import appeng.parts.automation.ForgeExternalStorageStrategy;
import appeng.parts.automation.ItemPickupStrategy;
import appeng.parts.automation.ItemPlacementStrategy;
import appeng.parts.automation.PlacementStrategyFacade;
import appeng.parts.automation.StackExportFacade;
import appeng.parts.automation.StackImportFacade;
import appeng.parts.automation.StorageExportStrategy;
import appeng.parts.automation.StorageImportStrategy;
import appeng.util.CowMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public final class StackWorldBehaviors {
    private static final CowMap<AEKeyType, StackImportStrategy.Factory> importStrategies = CowMap.identityHashMap();
    private static final CowMap<AEKeyType, StackExportStrategy.Factory> exportStrategies = CowMap.identityHashMap();
    private static final CowMap<AEKeyType, ExternalStorageStrategy.Factory> externalStorageStrategies = CowMap.identityHashMap();
    private static final CowMap<AEKeyType, PlacementStrategy.Factory> placementStrategies = CowMap.identityHashMap();
    private static final CowMap<AEKeyType, PickupStrategy.Factory> pickupStrategies = CowMap.identityHashMap();

    private StackWorldBehaviors() {
    }

    public static void registerImportStrategy(AEKeyType type, StackImportStrategy.Factory factory) {
        importStrategies.putIfAbsent(type, factory);
    }

    public static void registerExportStrategy(AEKeyType type, StackExportStrategy.Factory factory) {
        exportStrategies.putIfAbsent(type, factory);
    }

    public static void registerExternalStorageStrategy(AEKeyType type, ExternalStorageStrategy.Factory factory) {
        externalStorageStrategies.putIfAbsent(type, factory);
    }

    public static void registerPlacementStrategy(AEKeyType type, PlacementStrategy.Factory factory) {
        placementStrategies.putIfAbsent(type, factory);
    }

    public static void registerPickupStrategy(AEKeyType type, PickupStrategy.Factory factory) {
        pickupStrategies.putIfAbsent(type, factory);
    }

    public static AEKeyFilter hasImportStrategyFilter() {
        return what -> importStrategies.getMap().containsKey(what.getType());
    }

    public static Predicate<AEKeyType> hasImportStrategyTypeFilter() {
        return type -> importStrategies.getMap().containsKey(type);
    }

    public static Set<AEKeyType> withImportStrategy() {
        return Collections.unmodifiableSet(importStrategies.getMap().keySet());
    }

    public static AEKeyFilter hasExportStrategyFilter() {
        return what -> exportStrategies.getMap().containsKey(what.getType());
    }

    public static Set<AEKeyType> withExportStrategy() {
        return Collections.unmodifiableSet(exportStrategies.getMap().keySet());
    }

    public static AEKeyFilter hasPlacementStrategy() {
        return what -> placementStrategies.getMap().containsKey(what.getType());
    }

    public static Set<AEKeyType> withPlacementStrategy() {
        return Collections.unmodifiableSet(placementStrategies.getMap().keySet());
    }

    public static StackImportStrategy createImportFacade(ServerLevel level, BlockPos fromPos, Direction fromSide, Predicate<AEKeyType> forTypes) {
        ArrayList<StackImportStrategy> strategies = new ArrayList<StackImportStrategy>(importStrategies.getMap().size());
        for (Map.Entry<AEKeyType, StackImportStrategy.Factory> entry : importStrategies.getMap().entrySet()) {
            if (!forTypes.test(entry.getKey())) continue;
            strategies.add(entry.getValue().create(level, fromPos, fromSide));
        }
        return new StackImportFacade(strategies);
    }

    public static StackExportStrategy createExportFacade(ServerLevel level, BlockPos fromPos, Direction fromSide) {
        ArrayList<StackExportStrategy> strategies = new ArrayList<StackExportStrategy>(exportStrategies.getMap().size());
        for (StackExportStrategy.Factory supplier : exportStrategies.getMap().values()) {
            strategies.add(supplier.create(level, fromPos, fromSide));
        }
        return new StackExportFacade(strategies);
    }

    public static Map<AEKeyType, ExternalStorageStrategy> createExternalStorageStrategies(ServerLevel level, BlockPos fromPos, Direction fromSide) {
        IdentityHashMap<AEKeyType, ExternalStorageStrategy> strategies = new IdentityHashMap<AEKeyType, ExternalStorageStrategy>(externalStorageStrategies.getMap().size());
        for (Map.Entry<AEKeyType, ExternalStorageStrategy.Factory> entry : externalStorageStrategies.getMap().entrySet()) {
            strategies.put(entry.getKey(), entry.getValue().create(level, fromPos, fromSide));
        }
        return strategies;
    }

    public static PlacementStrategy createPlacementStrategies(ServerLevel level, BlockPos fromPos, Direction fromSide, BlockEntity host, @Nullable UUID owningPlayerId) {
        IdentityHashMap<AEKeyType, PlacementStrategy> strategies = new IdentityHashMap<AEKeyType, PlacementStrategy>(placementStrategies.getMap().size());
        for (Map.Entry<AEKeyType, PlacementStrategy.Factory> entry : placementStrategies.getMap().entrySet()) {
            strategies.put(entry.getKey(), entry.getValue().create(level, fromPos, fromSide, host, owningPlayerId));
        }
        return new PlacementStrategyFacade(strategies);
    }

    public static List<PickupStrategy> createPickupStrategies(ServerLevel level, BlockPos fromPos, Direction fromSide, BlockEntity host, ItemEnchantments enchantments, @Nullable UUID owningPlayerId) {
        return pickupStrategies.getMap().values().stream().map(f -> f.create(level, fromPos, fromSide, host, enchantments, owningPlayerId)).toList();
    }

    static {
        StackWorldBehaviors.registerImportStrategy(AEKeyType.items(), StorageImportStrategy::createItem);
        StackWorldBehaviors.registerImportStrategy(AEKeyType.fluids(), StorageImportStrategy::createFluid);
        StackWorldBehaviors.registerExportStrategy(AEKeyType.items(), StorageExportStrategy::createItem);
        StackWorldBehaviors.registerExportStrategy(AEKeyType.fluids(), StorageExportStrategy::createFluid);
        StackWorldBehaviors.registerExternalStorageStrategy(AEKeyType.items(), ForgeExternalStorageStrategy::createItem);
        StackWorldBehaviors.registerExternalStorageStrategy(AEKeyType.fluids(), ForgeExternalStorageStrategy::createFluid);
        StackWorldBehaviors.registerPlacementStrategy(AEKeyType.fluids(), FluidPlacementStrategy::new);
        StackWorldBehaviors.registerPlacementStrategy(AEKeyType.items(), ItemPlacementStrategy::new);
        StackWorldBehaviors.registerPickupStrategy(AEKeyType.fluids(), (level, pos, side, host, enchantments, owningPlayerId) -> new FluidPickupStrategy(level, pos, side, host, enchantments, owningPlayerId));
        StackWorldBehaviors.registerPickupStrategy(AEKeyType.items(), (level, pos, side, host, enchantments, owningPlayerId) -> new ItemPickupStrategy(level, pos, side, host, enchantments, owningPlayerId));
    }
}

