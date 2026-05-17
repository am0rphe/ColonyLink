/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 */
package appeng.api.movable;

import appeng.api.movable.DefaultBlockEntityMoveStrategy;
import appeng.api.movable.IBlockEntityMoveStrategy;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class BlockEntityMoveStrategies {
    private static final IBlockEntityMoveStrategy DEFAULT_STRATEGY = new DefaultBlockEntityMoveStrategy(){

        @Override
        public boolean canHandle(BlockEntityType<?> type) {
            return true;
        }
    };
    private static final List<IBlockEntityMoveStrategy> strategies = new ArrayList<IBlockEntityMoveStrategy>();
    private static final Map<BlockEntityType<?>, IBlockEntityMoveStrategy> valid = new IdentityHashMap();

    public static synchronized void add(IBlockEntityMoveStrategy strategy) {
        Objects.requireNonNull(strategy, "handler");
        strategies.add(strategy);
    }

    public static synchronized IBlockEntityMoveStrategy get(BlockEntity blockEntity) {
        Objects.requireNonNull(blockEntity, "blockEntity");
        IBlockEntityMoveStrategy result = valid.get(blockEntity.getType());
        if (result == null) {
            for (IBlockEntityMoveStrategy strategy : strategies) {
                if (!strategy.canHandle(blockEntity.getType())) continue;
                result = strategy;
                break;
            }
            if (result == null) {
                result = DEFAULT_STRATEGY;
            }
            valid.put(blockEntity.getType(), result);
        }
        return result;
    }

    public static IBlockEntityMoveStrategy getDefault() {
        return DEFAULT_STRATEGY;
    }
}

