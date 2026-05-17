/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.MenuType
 */
package appeng.menu.implementations;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.spatial.ISpatialService;
import appeng.blockentity.spatial.SpatialIOPortBlockEntity;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.slot.OutputSlot;
import appeng.menu.slot.RestrictedInputSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class SpatialIOPortMenu
extends AEBaseMenu {
    public static final MenuType<SpatialIOPortMenu> TYPE = MenuTypeBuilder.create(SpatialIOPortMenu::new, SpatialIOPortBlockEntity.class).build("spatialioport");
    @GuiSync(value=0)
    public long currentPower;
    @GuiSync(value=1)
    public long maxPower;
    @GuiSync(value=2)
    public long reqPower;
    @GuiSync(value=3)
    public long eff;
    private int delay = 40;
    @GuiSync(value=31)
    public int xSize;
    @GuiSync(value=32)
    public int ySize;
    @GuiSync(value=33)
    public int zSize;

    public SpatialIOPortMenu(int id, Inventory ip, SpatialIOPortBlockEntity spatialIOPort) {
        super(TYPE, id, ip, spatialIOPort);
        this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.SPATIAL_STORAGE_CELLS, spatialIOPort.getInternalInventory(), 0), SlotSemantics.MACHINE_INPUT);
        this.addSlot(new OutputSlot(spatialIOPort.getInternalInventory(), 1, RestrictedInputSlot.PlacableItemType.SPATIAL_STORAGE_CELLS_NO_SHADOW.icon), SlotSemantics.MACHINE_OUTPUT);
        this.createPlayerInventorySlots(ip);
    }

    @Override
    public void broadcastChanges() {
        if (this.isServerSide()) {
            IGrid grid;
            ++this.delay;
            IGridNode gridNode = ((SpatialIOPortBlockEntity)this.getBlockEntity()).getGridNode();
            IGrid iGrid = grid = gridNode != null ? gridNode.getGrid() : null;
            if (this.delay > 15 && grid != null) {
                this.delay = 0;
                IEnergyService eg = grid.getEnergyService();
                ISpatialService sc = grid.getSpatialService();
                this.setCurrentPower((long)(100.0 * eg.getStoredPower()));
                this.setMaxPower((long)(100.0 * eg.getMaxStoredPower()));
                this.setRequiredPower((long)(100.0 * (double)sc.requiredPower()));
                this.setEfficency((long)(100.0f * sc.currentEfficiency()));
                BlockPos min = sc.getMin();
                BlockPos max = sc.getMax();
                if (min != null && max != null && sc.isValidRegion()) {
                    this.xSize = sc.getMax().getX() - sc.getMin().getX() - 1;
                    this.ySize = sc.getMax().getY() - sc.getMin().getY() - 1;
                    this.zSize = sc.getMax().getZ() - sc.getMin().getZ() - 1;
                } else {
                    this.xSize = 0;
                    this.ySize = 0;
                    this.zSize = 0;
                }
            }
        }
        super.broadcastChanges();
    }

    public long getCurrentPower() {
        return this.currentPower;
    }

    private void setCurrentPower(long currentPower) {
        this.currentPower = currentPower;
    }

    public long getMaxPower() {
        return this.maxPower;
    }

    private void setMaxPower(long maxPower) {
        this.maxPower = maxPower;
    }

    public long getRequiredPower() {
        return this.reqPower;
    }

    private void setRequiredPower(long reqPower) {
        this.reqPower = reqPower;
    }

    public long getEfficency() {
        return this.eff;
    }

    private void setEfficency(long eff) {
        this.eff = eff;
    }
}

