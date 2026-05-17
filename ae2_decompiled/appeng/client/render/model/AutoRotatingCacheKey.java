/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.block.state.BlockState
 *  net.neoforged.neoforge.client.model.data.ModelData
 */
package appeng.client.render.model;

import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

final class AutoRotatingCacheKey {
    private final BlockState blockState;
    private final ModelData modelData;

    AutoRotatingCacheKey(BlockState blockState, ModelData modelData) {
        this.blockState = blockState;
        this.modelData = modelData;
    }

    public BlockState getBlockState() {
        return this.blockState;
    }

    public ModelData getModelData() {
        return this.modelData;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        AutoRotatingCacheKey cacheKey = (AutoRotatingCacheKey)o;
        return this.blockState.equals(cacheKey.blockState) && this.modelData.equals(cacheKey.modelData);
    }

    public int hashCode() {
        int result = this.blockState.hashCode();
        result = 31 * result + this.modelData.hashCode();
        return result;
    }
}

