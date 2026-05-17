/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.neoforged.neoforge.items.IItemHandler
 */
package appeng.debug;

import appeng.blockentity.AEBaseBlockEntity;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import java.util.ArrayDeque;
import java.util.Queue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

public class ItemGenBlockEntity
extends AEBaseBlockEntity
implements InternalInventoryHost {
    private static final Queue<ItemStack> SHARED_POSSIBLE_ITEMS = new ArrayDeque<ItemStack>();
    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 16, 64);
    private Item filter = Items.AIR;
    private final Queue<ItemStack> possibleItems = new ArrayDeque<ItemStack>();

    public ItemGenBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    public void clearRemoved() {
        super.clearRemoved();
        if (SHARED_POSSIBLE_ITEMS.isEmpty()) {
            ItemGenBlockEntity.initGlobalPossibleItems();
        }
        this.scheduleInit();
    }

    @Override
    public void onReady() {
        super.onReady();
        this.refillInv();
    }

    private static synchronized void initGlobalPossibleItems() {
        if (SHARED_POSSIBLE_ITEMS.isEmpty()) {
            for (Item item : BuiltInRegistries.ITEM) {
                ItemGenBlockEntity.addPossibleItem(item, SHARED_POSSIBLE_ITEMS);
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        data.putString("filter", BuiltInRegistries.ITEM.getKey((Object)this.filter).toString());
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        if (data.contains("filter")) {
            Item item = (Item)BuiltInRegistries.ITEM.get(ResourceLocation.parse((String)data.getString("filter")));
            this.setItem(item);
        }
        super.loadTag(data, registries);
    }

    public IItemHandler getItemHandler() {
        return this.inv.toItemHandler();
    }

    public void setItem(Item item) {
        this.filter = item;
        this.possibleItems.clear();
        ItemGenBlockEntity.addPossibleItem(this.filter, this.possibleItems);
        this.refillInv();
    }

    private Queue<ItemStack> getPossibleItems() {
        return this.filter != Items.AIR ? this.possibleItems : SHARED_POSSIBLE_ITEMS;
    }

    private static void addPossibleItem(Item item, Queue<ItemStack> queue) {
        if (item == null || item == Items.AIR) {
            return;
        }
        ItemStack sampleStack = item.getDefaultInstance();
        if (sampleStack.isDamageableItem()) {
            int maxDamage = sampleStack.getMaxDamage();
            for (int dmg = 0; dmg < maxDamage; ++dmg) {
                ItemStack is = sampleStack.copy();
                is.setDamageValue(dmg);
                queue.add(is);
            }
        } else {
            queue.add(item.getDefaultInstance());
        }
    }

    @Override
    public void saveChangedInventory(AppEngInternalInventory inv) {
        this.saveChanges();
    }

    @Override
    public void onChangeInventory(AppEngInternalInventory inv, int slot) {
        if (inv.getStackInSlot(slot).isEmpty()) {
            this.refillSlot(slot);
        }
    }

    private void refillInv() {
        for (int slot = 0; slot < this.inv.size(); ++slot) {
            this.refillSlot(slot);
        }
    }

    private void refillSlot(int slot) {
        ItemStack stack = this.getPossibleItems().poll();
        if (stack != null) {
            ItemStack copy = stack.copy();
            copy.setCount(stack.getMaxStackSize());
            this.inv.setItemDirect(slot, copy);
            this.getPossibleItems().add(stack);
        }
    }
}

