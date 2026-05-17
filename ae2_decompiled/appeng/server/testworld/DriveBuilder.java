/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.material.Fluid
 *  org.jetbrains.annotations.Nullable
 */
package appeng.server.testworld;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import appeng.core.definitions.AEItems;
import appeng.me.cells.BasicCellInventory;
import appeng.me.helpers.BaseActionSource;
import appeng.util.ConfigInventory;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

public class DriveBuilder {
    private final List<ItemStack> cells;

    DriveBuilder(List<ItemStack> cells) {
        this.cells = cells;
    }

    public CreativeCellBuilder addCreativeCell() {
        ItemStack cell = AEItems.CREATIVE_CELL.stack();
        ConfigInventory configInv = AEItems.CREATIVE_CELL.get().getConfigInventory(cell);
        this.cells.add(cell);
        return new CreativeCellBuilder(configInv);
    }

    public ItemCellBuilder addItemCell64k() {
        ItemStack cell = AEItems.ITEM_CELL_64K.stack();
        BasicCellInventory cellInv = BasicCellInventory.createInventory(cell, null);
        this.cells.add(cell);
        return new ItemCellBuilder(this, cellInv);
    }

    public FluidCellBuilder addFluidCell64k() {
        ItemStack cell = AEItems.FLUID_CELL_64K.stack();
        BasicCellInventory cellInv = BasicCellInventory.createInventory(cell, null);
        this.cells.add(cell);
        return new FluidCellBuilder(this, cellInv);
    }

    public class CreativeCellBuilder {
        private final ConfigInventory inv;

        public CreativeCellBuilder(ConfigInventory inv) {
            this.inv = inv;
        }

        public CreativeCellBuilder add(ItemLike item) {
            return this.add(AEItemKey.of(item));
        }

        public CreativeCellBuilder add(Fluid fluid) {
            return this.add(AEFluidKey.of(fluid));
        }

        public CreativeCellBuilder add(@Nullable GenericStack stack) {
            if (stack != null) {
                this.add(stack.what());
            }
            return this;
        }

        public CreativeCellBuilder add(AEKey key) {
            this.inv.insert(key, 1L, Actionable.MODULATE, new BaseActionSource());
            return this;
        }

        public DriveBuilder and() {
            return DriveBuilder.this;
        }
    }

    public class ItemCellBuilder
    extends CellBuilder {
        public ItemCellBuilder(DriveBuilder this$0, BasicCellInventory inv) {
            super(inv);
        }

        public void add(ItemLike what, long amount) {
            this.add(AEItemKey.of(what), amount);
        }
    }

    public class FluidCellBuilder
    extends CellBuilder {
        public FluidCellBuilder(DriveBuilder this$0, BasicCellInventory inv) {
            super(inv);
        }

        public void addBuckets(Fluid fluid, double buckets) {
            this.add(AEFluidKey.of(fluid), (long)(buckets * 1000.0));
        }
    }

    public class CellBuilder {
        protected final MEStorage inv;

        public CellBuilder(MEStorage inv) {
            this.inv = inv;
        }

        public void add(GenericStack stack) {
            this.add(stack.what(), stack.amount());
        }

        public void add(AEKey what, long amount) {
            if (this.inv.insert(what, amount, Actionable.MODULATE, new BaseActionSource()) != amount) {
                throw new IllegalArgumentException("Couldn't insert " + amount + " of " + String.valueOf(what));
            }
        }

        public DriveBuilder and() {
            return DriveBuilder.this;
        }
    }
}

