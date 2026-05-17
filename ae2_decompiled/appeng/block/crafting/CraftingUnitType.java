/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.Item
 */
package appeng.block.crafting;

import appeng.block.crafting.AbstractCraftingUnitBlock;
import appeng.block.crafting.ICraftingUnitType;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.BlockDefinition;
import net.minecraft.world.item.Item;

public enum CraftingUnitType implements ICraftingUnitType
{
    UNIT(0),
    ACCELERATOR(0),
    STORAGE_1K(1),
    STORAGE_4K(4),
    STORAGE_16K(16),
    STORAGE_64K(64),
    STORAGE_256K(256),
    MONITOR(0);

    private final int storageKb;

    private CraftingUnitType(int storageKb) {
        this.storageKb = storageKb;
    }

    @Override
    public long getStorageBytes() {
        return 1024L * (long)this.storageKb;
    }

    @Override
    public int getAcceleratorThreads() {
        return this == ACCELERATOR ? 1 : 0;
    }

    @Override
    public Item getItemFromType() {
        BlockDefinition<AbstractCraftingUnitBlock> definition = switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> AEBlocks.CRAFTING_UNIT;
            case 1 -> AEBlocks.CRAFTING_ACCELERATOR;
            case 2 -> AEBlocks.CRAFTING_STORAGE_1K;
            case 3 -> AEBlocks.CRAFTING_STORAGE_4K;
            case 4 -> AEBlocks.CRAFTING_STORAGE_16K;
            case 5 -> AEBlocks.CRAFTING_STORAGE_64K;
            case 6 -> AEBlocks.CRAFTING_STORAGE_256K;
            case 7 -> AEBlocks.CRAFTING_MONITOR;
        };
        return definition.asItem();
    }
}

