/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.world.phys.AABB
 *  net.neoforged.neoforge.client.model.data.ModelData
 *  net.neoforged.neoforge.client.model.data.ModelProperty
 */
package appeng.client.render.cablebus;

import appeng.api.parts.IPartModel;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.client.render.cablebus.CableCoreType;
import appeng.client.render.cablebus.FacadeRenderState;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

public class CableBusRenderState {
    public static final ModelProperty<CableBusRenderState> PROPERTY = new ModelProperty();
    private AECableType cableType = AECableType.NONE;
    private CableCoreType coreType;
    private AEColor cableColor = AEColor.TRANSPARENT;
    private EnumMap<Direction, AECableType> connectionTypes = new EnumMap(Direction.class);
    private EnumSet<Direction> cableBusAdjacent = EnumSet.noneOf(Direction.class);
    private EnumMap<Direction, Integer> channelsOnSide = new EnumMap(Direction.class);
    private EnumMap<Direction, IPartModel> attachments = new EnumMap(Direction.class);
    private EnumMap<Direction, Integer> attachmentConnections = new EnumMap(Direction.class);
    private EnumMap<Direction, FacadeRenderState> facades = new EnumMap(Direction.class);
    private BlockPos pos;
    private List<AABB> boundingBoxes = new ArrayList<AABB>();
    private EnumMap<Direction, ModelData> partModelData = new EnumMap(Direction.class);

    public CableCoreType getCoreType() {
        return this.coreType;
    }

    public void setCoreType(CableCoreType coreType) {
        this.coreType = coreType;
    }

    public AECableType getCableType() {
        return this.cableType;
    }

    public void setCableType(AECableType cableType) {
        this.cableType = cableType;
    }

    public AEColor getCableColor() {
        return this.cableColor;
    }

    public void setCableColor(AEColor cableColor) {
        this.cableColor = cableColor;
    }

    public EnumMap<Direction, Integer> getChannelsOnSide() {
        return this.channelsOnSide;
    }

    public EnumMap<Direction, AECableType> getConnectionTypes() {
        return this.connectionTypes;
    }

    public void setConnectionTypes(EnumMap<Direction, AECableType> connectionTypes) {
        this.connectionTypes = connectionTypes;
    }

    public void setChannelsOnSide(EnumMap<Direction, Integer> channelsOnSide) {
        this.channelsOnSide = channelsOnSide;
    }

    public EnumSet<Direction> getCableBusAdjacent() {
        return this.cableBusAdjacent;
    }

    public void setCableBusAdjacent(EnumSet<Direction> cableBusAdjacent) {
        this.cableBusAdjacent = cableBusAdjacent;
    }

    public EnumMap<Direction, IPartModel> getAttachments() {
        return this.attachments;
    }

    public EnumMap<Direction, Integer> getAttachmentConnections() {
        return this.attachmentConnections;
    }

    public EnumMap<Direction, FacadeRenderState> getFacades() {
        return this.facades;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }

    public List<AABB> getBoundingBoxes() {
        return this.boundingBoxes;
    }

    public EnumMap<Direction, ModelData> getPartModelData() {
        return this.partModelData;
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + (this.attachmentConnections == null ? 0 : this.attachmentConnections.hashCode());
        result = 31 * result + (this.cableBusAdjacent == null ? 0 : this.cableBusAdjacent.hashCode());
        result = 31 * result + (this.cableColor == null ? 0 : this.cableColor.hashCode());
        result = 31 * result + (this.cableType == null ? 0 : this.cableType.hashCode());
        result = 31 * result + (this.channelsOnSide == null ? 0 : this.channelsOnSide.hashCode());
        result = 31 * result + (this.connectionTypes == null ? 0 : this.connectionTypes.hashCode());
        result = 31 * result + (this.coreType == null ? 0 : this.coreType.hashCode());
        result = 31 * result + (this.partModelData == null ? 0 : this.partModelData.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        CableBusRenderState other = (CableBusRenderState)obj;
        return this.cableColor == other.cableColor && this.cableType == other.cableType && this.coreType == other.coreType && Objects.equals(this.attachmentConnections, other.attachmentConnections) && Objects.equals(this.cableBusAdjacent, other.cableBusAdjacent) && Objects.equals(this.channelsOnSide, other.channelsOnSide) && Objects.equals(this.connectionTypes, other.connectionTypes) && Objects.equals(this.partModelData, other.partModelData);
    }
}

