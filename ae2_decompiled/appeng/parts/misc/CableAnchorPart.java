/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.block.entity.BlockEntity
 */
package appeng.parts.misc;

import appeng.api.networking.IGridNode;
import appeng.api.parts.BusSupport;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.util.AECableType;
import appeng.core.AppEng;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

public class CableAnchorPart
implements IPart {
    @PartModels
    public static final PartModel DEFAULT_MODELS = new PartModel(false, AppEng.makeId("part/cable_anchor"));
    @PartModels
    public static final PartModel FACADE_MODELS = new PartModel(false, AppEng.makeId("part/cable_anchor_short"));
    private final IPartItem<CableAnchorPart> partItem;
    private IPartHost host = null;
    private Direction mySide = Direction.UP;

    public CableAnchorPart(IPartItem<CableAnchorPart> partItem) {
        this.partItem = partItem;
    }

    @Override
    public IPartItem<?> getPartItem() {
        return this.partItem;
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        if (this.host != null && this.host.getFacadeContainer().getFacade(this.mySide) != null) {
            bch.addBox(7.0, 7.0, 10.0, 9.0, 9.0, 14.0);
        } else {
            bch.addBox(7.0, 7.0, 10.0, 9.0, 9.0, 16.0);
        }
    }

    @Override
    public boolean isLadder(LivingEntity entity) {
        return this.mySide.getStepY() == 0 && (entity.horizontalCollision || !entity.onGround());
    }

    @Override
    public IGridNode getGridNode() {
        return null;
    }

    @Override
    public void setPartHostInfo(Direction side, IPartHost host, BlockEntity blockEntity) {
        this.host = host;
        this.mySide = side;
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 0.0f;
    }

    @Override
    public boolean canBePlacedOn(BusSupport what) {
        return what == BusSupport.CABLE || what == BusSupport.DENSE_CABLE;
    }

    @Override
    public IPartModel getStaticModels() {
        if (this.host != null && this.host.getFacadeContainer().getFacade(this.mySide) != null) {
            return FACADE_MODELS;
        }
        return DEFAULT_MODELS;
    }
}

