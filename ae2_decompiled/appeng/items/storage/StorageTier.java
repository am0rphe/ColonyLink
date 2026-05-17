/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.world.item.Item
 */
package appeng.items.storage;

import appeng.api.ids.AEItemIds;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

public record StorageTier(int index, String namePrefix, int bytes, double idleDrain, Supplier<Item> componentSupplier) {
    public static final StorageTier SIZE_1K = new StorageTier(1, "1k", 1024, 0.5, () -> (Item)BuiltInRegistries.ITEM.get(AEItemIds.CELL_COMPONENT_1K));
    public static final StorageTier SIZE_4K = new StorageTier(2, "4k", 4096, 1.0, () -> (Item)BuiltInRegistries.ITEM.get(AEItemIds.CELL_COMPONENT_4K));
    public static final StorageTier SIZE_16K = new StorageTier(3, "16k", 16384, 1.5, () -> (Item)BuiltInRegistries.ITEM.get(AEItemIds.CELL_COMPONENT_16K));
    public static final StorageTier SIZE_64K = new StorageTier(4, "64k", 65536, 2.0, () -> (Item)BuiltInRegistries.ITEM.get(AEItemIds.CELL_COMPONENT_64K));
    public static final StorageTier SIZE_256K = new StorageTier(5, "256k", 262144, 2.5, () -> (Item)BuiltInRegistries.ITEM.get(AEItemIds.CELL_COMPONENT_256K));
}

