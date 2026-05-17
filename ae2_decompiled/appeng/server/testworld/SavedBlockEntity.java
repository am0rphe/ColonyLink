/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  org.jetbrains.annotations.Nullable
 */
package appeng.server.testworld;

import appeng.server.testworld.PlotTestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class SavedBlockEntity {
    private final PlotTestHelper helper;
    private BlockPos pos;
    @Nullable
    private BlockState blockState;
    private CompoundTag data;

    public SavedBlockEntity(PlotTestHelper helper) {
        this.helper = helper;
    }

    public void save(BlockPos pos) {
        this.pos = pos;
        this.blockState = this.helper.getBlockState(pos);
        BlockEntity be = this.helper.getBlockEntity(pos);
        if (be == null) {
            this.helper.fail("No BlockEntity", pos);
            return;
        }
        this.data = be.saveWithId((HolderLookup.Provider)this.helper.getLevel().registryAccess());
    }

    public void saveAndRemove(BlockPos pos) {
        this.save(pos);
        this.helper.destroyBlock(pos);
    }

    public BlockEntity restore() {
        if (this.pos == null) {
            this.helper.fail("No block entity was saved");
            return null;
        }
        this.helper.setBlock(BlockPos.ZERO, this.blockState);
        BlockEntity be = BlockEntity.loadStatic((BlockPos)this.helper.absolutePos(BlockPos.ZERO), (BlockState)this.blockState, (CompoundTag)this.data, (HolderLookup.Provider)this.helper.getLevel().registryAccess());
        if (be == null) {
            this.helper.fail("Blockentity could not be restored", this.pos);
            return null;
        }
        this.helper.getLevel().setBlockEntity(be);
        return be;
    }
}

