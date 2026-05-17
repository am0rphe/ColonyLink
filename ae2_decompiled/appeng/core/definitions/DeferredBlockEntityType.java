/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.neoforged.neoforge.registries.DeferredHolder
 */
package appeng.core.definitions;

import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class DeferredBlockEntityType<T extends BlockEntity>
implements Supplier<BlockEntityType<T>> {
    private final Class<T> blockEntityClass;
    private final DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> holder;

    public DeferredBlockEntityType(Class<T> blockEntityClass, DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> holder) {
        this.blockEntityClass = blockEntityClass;
        this.holder = holder;
    }

    public Class<T> getBlockEntityClass() {
        return this.blockEntityClass;
    }

    @Override
    public BlockEntityType<T> get() {
        return (BlockEntityType)this.holder.get();
    }

    @Nullable
    public T getBlockEntity(BlockGetter level, BlockPos pos) {
        BlockEntity blockentity = level.getBlockEntity(pos);
        return (T)(blockentity != null && blockentity.getType() == this.holder.get() ? blockentity : null);
    }
}

