/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  org.jetbrains.annotations.Nullable
 */
package appeng.blockentity.misc;

import appeng.api.implementations.blockentities.ICrankable;
import appeng.block.misc.CrankBlock;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.ClientTickingBlockEntity;
import appeng.blockentity.ServerTickingBlockEntity;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CrankBlockEntity
extends AEBaseBlockEntity
implements ServerTickingBlockEntity,
ClientTickingBlockEntity {
    public static final int POWER_PER_CRANK_TURN = 160;
    private final int ticksPerRotation = 18;
    private float visibleRotation = 0.0f;
    private int charge = 0;
    private int hits = 0;
    private int rotation = 0;

    public CrankBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    @Nullable
    private ICrankable getCrankable() {
        if (this.isClientSide()) {
            return null;
        }
        BlockState blockState = this.getBlockState();
        Block block = blockState.getBlock();
        if (block instanceof CrankBlock) {
            CrankBlock crankBlock = (CrankBlock)block;
            return crankBlock.getCrankable(blockState, this.level, this.getBlockPos());
        }
        return null;
    }

    @Override
    protected boolean readFromStream(RegistryFriendlyByteBuf data) {
        boolean c = super.readFromStream(data);
        this.rotation = data.readInt();
        return c;
    }

    @Override
    protected void writeToStream(RegistryFriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeInt(this.rotation);
    }

    public boolean power() {
        ICrankable crankable;
        if (this.isClientSide()) {
            return false;
        }
        if (this.rotation < 3 && (crankable = this.getCrankable()) != null) {
            if (crankable.canTurn()) {
                this.hits = 0;
                this.rotation += this.ticksPerRotation;
                this.markForUpdate();
                return true;
            }
            ++this.hits;
            if (this.hits > 10) {
                this.level.destroyBlock(this.getBlockPos(), false);
            }
        }
        return false;
    }

    public float getVisibleRotation() {
        return this.visibleRotation;
    }

    private void setVisibleRotation(float visibleRotation) {
        this.visibleRotation = visibleRotation;
    }

    @Override
    public void clientTick() {
        this.tick();
    }

    @Override
    public void serverTick() {
        this.tick();
    }

    private void tick() {
        if (this.rotation > 0) {
            float f = this.getVisibleRotation();
            Objects.requireNonNull(this);
            this.setVisibleRotation(f - 360.0f / 18.0f);
            ++this.charge;
            if (this.charge >= this.ticksPerRotation) {
                this.charge -= this.ticksPerRotation;
                ICrankable g = this.getCrankable();
                if (g != null) {
                    g.applyTurn();
                }
            }
            --this.rotation;
        }
    }
}

