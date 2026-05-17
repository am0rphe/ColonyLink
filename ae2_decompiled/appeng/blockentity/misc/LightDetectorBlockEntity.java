/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 */
package appeng.blockentity.misc;

import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.CommonTickingBlockEntity;
import appeng.util.Platform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class LightDetectorBlockEntity
extends AEBaseBlockEntity
implements CommonTickingBlockEntity {
    private int lastCheck = 30;
    private int lastLight = 0;

    public LightDetectorBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    public boolean isExposedToLight() {
        return this.lastLight > 0;
    }

    @Override
    public void commonTick() {
        ++this.lastCheck;
        if (this.lastCheck > 30) {
            this.lastCheck = 0;
            this.updateLight();
        }
    }

    public void updateLight() {
        int val = this.level.getMaxLocalRawBrightness(this.worldPosition);
        if (this.lastLight != val) {
            this.lastLight = val;
            Platform.notifyBlocksOfNeighbors(this.level, this.worldPosition);
        }
    }
}

