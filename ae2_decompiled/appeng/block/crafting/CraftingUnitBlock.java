/*
 * Decompiled with CFR 0.152.
 */
package appeng.block.crafting;

import appeng.block.crafting.AbstractCraftingUnitBlock;
import appeng.block.crafting.ICraftingUnitType;
import appeng.blockentity.crafting.CraftingBlockEntity;

public class CraftingUnitBlock
extends AbstractCraftingUnitBlock<CraftingBlockEntity> {
    public CraftingUnitBlock(ICraftingUnitType type) {
        super(CraftingUnitBlock.metalProps(), type);
    }
}

