/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.neoforged.neoforge.items.IItemHandler
 *  org.jetbrains.annotations.Nullable
 */
package appeng.blockentity;

import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.GenericStack;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public abstract class AEBaseInvBlockEntity
extends AEBaseBlockEntity
implements InternalInventoryHost {
    public AEBaseInvBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        InternalInventory inv = this.getInternalInventory();
        if (inv != InternalInventory.empty()) {
            CompoundTag opt = data.getCompound("inv");
            for (int x = 0; x < inv.size(); ++x) {
                CompoundTag item = opt.getCompound("item" + x);
                inv.setItemDirect(x, ItemStack.parseOptional((HolderLookup.Provider)registries, (CompoundTag)item));
            }
        }
    }

    public abstract InternalInventory getInternalInventory();

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        InternalInventory inv = this.getInternalInventory();
        if (inv != InternalInventory.empty()) {
            CompoundTag opt = new CompoundTag();
            for (int x = 0; x < inv.size(); ++x) {
                ItemStack is = inv.getStackInSlot(x);
                opt.put("item" + x, is.saveOptional(registries));
            }
            data.put("inv", (Tag)opt);
        }
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        InternalInventory inv = this.getInternalInventory();
        for (ItemStack stack : inv) {
            GenericStack genericStack = GenericStack.unwrapItemStack(stack);
            if (genericStack != null) {
                genericStack.what().addDrops(genericStack.amount(), drops, level, pos);
                continue;
            }
            drops.add(stack);
        }
    }

    @Override
    public void clearContent() {
        super.clearContent();
        this.getInternalInventory().clear();
    }

    @Override
    public void saveChangedInventory(AppEngInternalInventory inv) {
        this.saveChanges();
    }

    protected InternalInventory getExposedInventoryForSide(Direction side) {
        return this.getInternalInventory();
    }

    @Nullable
    public IItemHandler getExposedItemHandler(@Nullable Direction side) {
        if (side == null) {
            return this.getInternalInventory().toItemHandler();
        }
        InternalInventory exposed = this.getExposedInventoryForSide(side);
        return exposed.size() == 0 ? null : exposed.toItemHandler();
    }
}

