/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.block.Block
 *  net.neoforged.neoforge.registries.DeferredBlock
 */
package appeng.core.definitions;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.core.definitions.ItemDefinition;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;

public class BlockDefinition<T extends Block>
implements ItemLike {
    private final String englishName;
    private final ItemDefinition<BlockItem> item;
    private final DeferredBlock<T> block;

    public BlockDefinition(String englishName, DeferredBlock<T> block, ItemDefinition<BlockItem> item) {
        this.englishName = englishName;
        this.item = Objects.requireNonNull(item, "item");
        this.block = Objects.requireNonNull(block, "block");
    }

    public String getEnglishName() {
        return this.englishName;
    }

    public ResourceLocation id() {
        return this.block.getId();
    }

    public final T block() {
        return (T)((Block)this.block.get());
    }

    public ItemStack stack() {
        return this.item.stack();
    }

    public ItemStack stack(int stackSize) {
        return this.item.stack(stackSize);
    }

    public GenericStack genericStack(long stackSize) {
        return this.item.genericStack(stackSize);
    }

    public boolean is(ItemStack comparableStack) {
        return this.item.is(comparableStack);
    }

    public boolean is(AEKey key) {
        return this.item.is(key);
    }

    public ItemDefinition<BlockItem> item() {
        return this.item;
    }

    public Item asItem() {
        return this.item.asItem();
    }
}

