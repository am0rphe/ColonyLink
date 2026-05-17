/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 */
package appeng.blockentity.networking;

import appeng.api.networking.energy.IPassiveEnergyGenerator;
import appeng.api.orientation.BlockOrientation;
import appeng.api.orientation.RelativeSide;
import appeng.api.util.AECableType;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.core.AEConfig;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CrystalResonanceGeneratorBlockEntity
extends AENetworkedBlockEntity {
    private boolean suppressed;

    public CrystalResonanceGeneratorBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.getMainNode().setIdlePowerUsage(0.0);
        this.getMainNode().addService(IPassiveEnergyGenerator.class, new IPassiveEnergyGenerator(){

            @Override
            public double getRate() {
                return AEConfig.instance().getCrystalResonanceGeneratorRate();
            }

            @Override
            public boolean isSuppressed() {
                return CrystalResonanceGeneratorBlockEntity.this.suppressed;
            }

            @Override
            public void setSuppressed(boolean suppressed) {
                if (suppressed != CrystalResonanceGeneratorBlockEntity.this.suppressed) {
                    CrystalResonanceGeneratorBlockEntity.this.suppressed = suppressed;
                    CrystalResonanceGeneratorBlockEntity.this.markForUpdate();
                }
            }
        });
    }

    public boolean isSuppressed() {
        return this.suppressed;
    }

    @Override
    protected boolean readFromStream(RegistryFriendlyByteBuf data) {
        super.readFromStream(data);
        this.suppressed = data.readBoolean();
        return false;
    }

    @Override
    protected void writeToStream(RegistryFriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeBoolean(this.suppressed);
    }

    @Override
    protected void saveVisualState(CompoundTag data) {
        super.saveVisualState(data);
        data.putBoolean("suppressed", this.suppressed);
    }

    @Override
    protected void loadVisualState(CompoundTag data) {
        super.loadVisualState(data);
        this.suppressed = data.getBoolean("suppressed");
    }

    @Override
    public Set<Direction> getGridConnectableSides(BlockOrientation orientation) {
        return EnumSet.of(orientation.getSide(RelativeSide.BACK));
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }
}

