/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 */
package appeng.blockentity.networking;

import appeng.api.implementations.IPowerChannelState;
import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNodeListener;
import appeng.api.orientation.BlockOrientation;
import appeng.api.orientation.RelativeSide;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalBlockPos;
import appeng.blockentity.grid.AENetworkedInvBlockEntity;
import appeng.core.AEConfig;
import appeng.core.definitions.AEItems;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.filter.AEItemDefinitionFilter;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class WirelessAccessPointBlockEntity
extends AENetworkedInvBlockEntity
implements IWirelessAccessPoint,
IPowerChannelState {
    public static final int POWERED_FLAG = 1;
    public static final int CHANNEL_FLAG = 2;
    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 1);
    private int clientFlags = 0;

    public WirelessAccessPointBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.inv.setFilter(new AEItemDefinitionFilter(AEItems.WIRELESS_BOOSTER));
        this.getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL);
    }

    @Override
    public Set<Direction> getGridConnectableSides(BlockOrientation orientation) {
        return EnumSet.of(orientation.getSide(RelativeSide.BACK));
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        if (reason != IGridNodeListener.State.GRID_BOOT) {
            this.markForUpdate();
        }
    }

    @Override
    protected boolean readFromStream(RegistryFriendlyByteBuf data) {
        boolean c = super.readFromStream(data);
        int old = this.getClientFlags();
        this.setClientFlags(data.readByte());
        return old != this.getClientFlags() || c;
    }

    @Override
    protected void writeToStream(RegistryFriendlyByteBuf data) {
        super.writeToStream(data);
        this.setClientFlags(0);
        this.getMainNode().ifPresent((grid, node) -> {
            if (grid.getEnergyService().isNetworkPowered()) {
                this.setClientFlags(this.getClientFlags() | 1);
            }
            if (node.meetsChannelRequirements()) {
                this.setClientFlags(this.getClientFlags() | 2);
            }
        });
        data.writeByte((byte)this.getClientFlags());
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    @Override
    public DimensionalBlockPos getLocation() {
        return new DimensionalBlockPos(this);
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.inv;
    }

    @Override
    public void onReady() {
        this.updatePower();
        super.onReady();
    }

    private void updatePower() {
        this.getMainNode().setIdlePowerUsage(AEConfig.instance().wireless_getPowerDrain(this.getBoosters()));
    }

    private int getBoosters() {
        ItemStack boosters = this.inv.getStackInSlot(0);
        return boosters == null ? 0 : boosters.getCount();
    }

    @Override
    public void saveChanges() {
        this.updatePower();
        super.saveChanges();
    }

    @Override
    public double getRange() {
        return AEConfig.instance().wireless_getMaxRange(this.getBoosters());
    }

    @Override
    public boolean isActive() {
        if (this.isClientSide()) {
            return this.isPowered() && 2 == (this.getClientFlags() & 2);
        }
        return this.getMainNode().isOnline();
    }

    @Override
    public IGrid getGrid() {
        return this.getMainNode().getGrid();
    }

    @Override
    public boolean isPowered() {
        return 1 == (this.getClientFlags() & 1);
    }

    public int getClientFlags() {
        return this.clientFlags;
    }

    private void setClientFlags(int clientFlags) {
        this.clientFlags = clientFlags;
    }
}

