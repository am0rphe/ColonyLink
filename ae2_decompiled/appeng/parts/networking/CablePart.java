/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.Unpooled
 *  net.minecraft.core.Direction
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.StringTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  org.apache.commons.lang3.StringUtils
 *  org.jetbrains.annotations.Nullable
 */
package appeng.parts.networking;

import appeng.api.implementations.parts.ICablePart;
import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.pathing.ChannelMode;
import appeng.api.parts.BusSupport;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.core.definitions.AEParts;
import appeng.items.parts.ColoredPartItem;
import appeng.items.tools.powered.ColorApplicatorItem;
import appeng.parts.AEBasePart;
import io.netty.buffer.Unpooled;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

public abstract class CablePart
extends AEBasePart
implements ICablePart {
    private static final IGridNodeListener<CablePart> NODE_LISTENER = new AEBasePart.NodeListener<CablePart>(){

        @Override
        public void onInWorldConnectionChanged(CablePart nodeOwner, IGridNode node) {
            super.onInWorldConnectionChanged(nodeOwner, node);
            nodeOwner.markForUpdate();
        }
    };
    private final int[] channelsOnSide = new int[]{0, 0, 0, 0, 0, 0};
    private Set<Direction> connections = Collections.emptySet();

    public CablePart(ColoredPartItem<?> partItem) {
        super(partItem);
        this.getMainNode().setFlags(GridFlags.PREFERRED).setIdlePowerUsage(0.0).setInWorldNode(true).setExposedOnSides(EnumSet.allOf(Direction.class));
        this.getMainNode().setGridColor(partItem.getColor());
    }

    @Override
    protected IManagedGridNode createMainNode() {
        return GridHelper.createManagedNode(this, NODE_LISTENER);
    }

    @Override
    public BusSupport supportsBuses() {
        return BusSupport.CABLE;
    }

    @Override
    public AEColor getCableColor() {
        IPartItem<?> iPartItem = this.getPartItem();
        if (iPartItem instanceof ColoredPartItem) {
            ColoredPartItem coloredPartItem = (ColoredPartItem)iPartItem;
            return coloredPartItem.getColor();
        }
        return AEColor.TRANSPARENT;
    }

    @Override
    public final void getBoxes(IPartCollisionHelper bch) {
        this.getBoxes(bch, dir -> true);
    }

    public abstract void getBoxes(IPartCollisionHelper var1, Predicate<@Nullable Direction> var2);

    protected static void addConnectionBox(IPartCollisionHelper bch, Direction direction, double min, double max, double distanceFromEnd) {
        switch (direction) {
            case DOWN: {
                bch.addBox(min, distanceFromEnd, min, max, min, max);
                break;
            }
            case EAST: {
                bch.addBox(max, min, min, 16.0 - distanceFromEnd, max, max);
                break;
            }
            case NORTH: {
                bch.addBox(min, min, distanceFromEnd, max, max, min);
                break;
            }
            case SOUTH: {
                bch.addBox(min, min, max, max, max, 16.0 - distanceFromEnd);
                break;
            }
            case UP: {
                bch.addBox(min, max, min, max, 16.0 - distanceFromEnd, max);
                break;
            }
            case WEST: {
                bch.addBox(distanceFromEnd, min, min, min, max, max);
            }
        }
    }

    protected void addNonDenseBoxes(IPartCollisionHelper bch, Predicate<@Nullable Direction> filterConnections, double min, double max) {
        IPartHost ph;
        if (filterConnections.test(null)) {
            bch.addBox(min, min, min, max, max, max);
        }
        if ((ph = this.getHost()) != null) {
            for (Direction dir : Direction.values()) {
                float dist;
                IPart p;
                if (!filterConnections.test(dir) || (p = ph.getPart(dir)) == null || (dist = p.getCableConnectionLength(this.getCableConnectionType())) <= 0.0f || dist > 8.0f) continue;
                CablePart.addConnectionBox(bch, dir, min, max, dist);
            }
        }
        for (Direction of : this.getConnections()) {
            if (!filterConnections.test(of)) continue;
            CablePart.addConnectionBox(bch, of, min, max, 0.0);
        }
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        if (cable == this.getCableConnectionType()) {
            return 4.0f;
        }
        if (cable.ordinal() >= this.getCableConnectionType().ordinal()) {
            return -1.0f;
        }
        return 8.0f;
    }

    @Override
    public void onPlacement(Player player) {
        ColorApplicatorItem item;
        AEColor color;
        Item item2;
        super.onPlacement(player);
        ItemStack stack = player.getItemInHand(InteractionHand.OFF_HAND);
        if (!stack.isEmpty() && (item2 = stack.getItem()) instanceof ColorApplicatorItem && (color = (item = (ColorApplicatorItem)item2).getActiveColor(stack)) != null && color != this.getCableColor() && item.consumeColor(stack, color, true) && this.changeColor(color, player) && !player.getAbilities().instabuild) {
            item.consumeColor(stack, color, false);
        }
    }

    @Override
    public boolean changeColor(AEColor newColor, Player who) {
        if (this.getCableColor() != newColor) {
            IPartItem newPart = null;
            if (this.getCableConnectionType() == AECableType.GLASS) {
                newPart = AEParts.GLASS_CABLE.item(newColor);
            } else if (this.getCableConnectionType() == AECableType.COVERED) {
                newPart = AEParts.COVERED_CABLE.item(newColor);
            } else if (this.getCableConnectionType() == AECableType.SMART) {
                newPart = AEParts.SMART_CABLE.item(newColor);
            } else if (this.getCableConnectionType() == AECableType.DENSE_COVERED) {
                newPart = AEParts.COVERED_DENSE_CABLE.item(newColor);
            } else if (this.getCableConnectionType() == AECableType.DENSE_SMART) {
                newPart = AEParts.SMART_DENSE_CABLE.item(newColor);
            }
            if (newPart != null) {
                if (this.isClientSide()) {
                    return true;
                }
                this.setPartItem(newPart);
                this.getMainNode().setGridColor(this.getCableColor());
                this.getHost().partChanged();
                this.getHost().markForUpdate();
                this.getHost().markForSave();
                return true;
            }
        }
        return false;
    }

    @Override
    public void setExposedOnSides(EnumSet<Direction> sides) {
        this.getMainNode().setExposedOnSides(sides);
    }

    @Override
    public boolean isConnected(Direction side) {
        return this.getConnections().contains(side);
    }

    public void markForUpdate() {
        this.getHost().markForUpdate();
    }

    protected void updateConnections() {
        if (!this.isClientSide()) {
            IGridNode n = this.getGridNode();
            if (n != null) {
                this.setConnections(n.getConnectedSides());
            } else {
                this.setConnections(Collections.emptySet());
            }
        }
    }

    @Override
    public void writeToStream(RegistryFriendlyByteBuf data) {
        super.writeToStream(data);
        boolean[] writeChannels = new boolean[Direction.values().length];
        byte[] channelsPerSide = new byte[Direction.values().length];
        for (Direction thisSide : Direction.values()) {
            IPart part = this.getHost().getPart(thisSide);
            if (part == null) continue;
            int channels = 0;
            if (part.getGridNode() != null) {
                for (IGridConnection gc : part.getGridNode().getConnections()) {
                    channels = Math.max(channels, gc.getUsedChannels());
                }
            }
            channelsPerSide[thisSide.ordinal()] = this.getVisualChannels(channels);
            writeChannels[thisSide.ordinal()] = true;
        }
        int connectedSidesPacked = 0;
        IGridNode n = this.getGridNode();
        if (n != null) {
            for (Map.Entry<Direction, IGridConnection> entry : n.getInWorldConnections().entrySet()) {
                int side = entry.getKey().ordinal();
                IGridConnection connection = entry.getValue();
                channelsPerSide[side] = this.getVisualChannels(connection.getUsedChannels());
                writeChannels[side] = true;
                connectedSidesPacked |= 1 << side;
            }
        }
        data.writeByte((byte)connectedSidesPacked);
        for (int i = 0; i < writeChannels.length; ++i) {
            if (!writeChannels[i]) continue;
            data.writeByte(channelsPerSide[i]);
        }
    }

    private byte getVisualChannels(int channels) {
        byte visualMaxChannels;
        IGridNode node = this.getGridNode();
        if (node == null) {
            return 0;
        }
        switch (this.getCableConnectionType()) {
            default: {
                throw new MatchException(null, null);
            }
            case NONE: {
                byte by = 0;
                break;
            }
            case GLASS: 
            case SMART: 
            case COVERED: {
                byte by = 8;
                break;
            }
            case DENSE_COVERED: 
            case DENSE_SMART: {
                byte by = visualMaxChannels = 32;
            }
        }
        if (node.getGrid().getPathingService().getChannelMode() == ChannelMode.INFINITE) {
            return channels <= 0 ? (byte)0 : visualMaxChannels;
        }
        int gridMaxChannels = node.getMaxChannels();
        if (visualMaxChannels == 0 || gridMaxChannels == 0) {
            return 0;
        }
        byte result = (byte)Math.min(visualMaxChannels, channels * visualMaxChannels / gridMaxChannels);
        if (result == 0 && channels > 0) {
            return 1;
        }
        return result;
    }

    @Override
    public boolean readFromStream(RegistryFriendlyByteBuf data) {
        boolean changed = super.readFromStream(data);
        byte connectedSidesPacked = data.readByte();
        Set<Direction> previousConnections = this.getConnections();
        boolean channelsChanged = false;
        EnumSet<Direction> connections = EnumSet.noneOf(Direction.class);
        for (Direction d : Direction.values()) {
            boolean conOnSide;
            boolean bl = conOnSide = (connectedSidesPacked & 1 << d.ordinal()) != 0;
            if (conOnSide) {
                connections.add(d);
            }
            int ch = 0;
            if (conOnSide || this.getHost().getPart(d) != null) {
                ch = data.readByte() & 0xFF;
            }
            if (ch == this.channelsOnSide[d.ordinal()]) continue;
            channelsChanged = true;
            this.setChannelsOnSide(d.ordinal(), ch);
        }
        this.setConnections(connections);
        return changed || !previousConnections.equals(this.getConnections()) || channelsChanged;
    }

    @Override
    public void writeVisualStateToNBT(CompoundTag data) {
        super.writeVisualStateToNBT(data);
        if (!this.isClientSide()) {
            this.updateConnections();
            RegistryFriendlyByteBuf packet = new RegistryFriendlyByteBuf(Unpooled.buffer(), this.getLevel().registryAccess());
            this.writeToStream(packet);
            this.readFromStream(packet);
        }
        for (RegistryFriendlyByteBuf side : Direction.values()) {
            if (!this.connections.contains(side)) continue;
            String sideName = "channels" + StringUtils.capitalize((String)side.getSerializedName());
            data.putInt(sideName, this.channelsOnSide[side.ordinal()]);
        }
        ListTag connectionsTag = new ListTag();
        for (Direction connection : this.connections) {
            connectionsTag.add((Object)StringTag.valueOf((String)connection.getSerializedName()));
        }
        data.put("connections", (Tag)connectionsTag);
    }

    @Override
    public void readVisualStateFromNBT(CompoundTag data) {
        super.readVisualStateFromNBT(data);
        if (data.contains("channels")) {
            Arrays.fill(this.channelsOnSide, data.getInt("channels"));
        } else {
            for (Direction side : Direction.values()) {
                String sideName = "channels" + StringUtils.capitalize((String)side.getSerializedName());
                this.channelsOnSide[side.ordinal()] = data.getInt(sideName);
            }
        }
        EnumSet<Direction> connections = EnumSet.noneOf(Direction.class);
        ListTag connectionsTag = data.getList("connections", 8);
        for (Tag connectionTag : connectionsTag) {
            Direction side = Direction.byName((String)connectionTag.getAsString());
            if (side == null) continue;
            connections.add(side);
        }
        this.setConnections(connections);
    }

    public int getChannelsOnSide(Direction side) {
        if (!this.isPowered()) {
            return 0;
        }
        return this.channelsOnSide[side.ordinal()];
    }

    void setChannelsOnSide(int i, int channels) {
        this.channelsOnSide[i] = channels;
    }

    Set<Direction> getConnections() {
        return this.connections;
    }

    void setConnections(Set<Direction> connections) {
        this.connections = connections;
    }
}

