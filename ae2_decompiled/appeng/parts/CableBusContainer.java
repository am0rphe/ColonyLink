/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.EntityCollisionContext
 *  net.minecraft.world.phys.shapes.VoxelShape
 *  org.jetbrains.annotations.Nullable
 */
package appeng.parts;

import appeng.api.config.YesNo;
import appeng.api.implementations.parts.ICablePart;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.parts.CableRenderMode;
import appeng.api.parts.IFacadeContainer;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartHelper;
import appeng.api.parts.SelectedPart;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalBlockPos;
import appeng.client.render.cablebus.CableBusRenderState;
import appeng.client.render.cablebus.CableCoreType;
import appeng.client.render.cablebus.FacadeRenderState;
import appeng.core.AELog;
import appeng.facade.FacadeContainer;
import appeng.helpers.AEMultiBlockEntity;
import appeng.hooks.VisualStateSaving;
import appeng.hooks.ticking.TickHandler;
import appeng.items.parts.FacadeItem;
import appeng.me.InWorldGridNode;
import appeng.parts.BusCollisionHelper;
import appeng.parts.CableBusStorage;
import appeng.parts.ICableBusContainer;
import appeng.parts.VoxelShapeCache;
import appeng.parts.networking.CablePart;
import appeng.util.Platform;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class CableBusContainer
implements AEMultiBlockEntity,
ICableBusContainer {
    private static final String[] NBT_KEY_SIDES = (String[])Arrays.stream(Platform.DIRECTIONS_WITH_NULL).map(d -> d == null ? "cable" : d.getSerializedName()).toArray(String[]::new);
    private static final ThreadLocal<Boolean> IS_LOADING = new ThreadLocal();
    private final CableBusStorage storage = new CableBusStorage();
    private YesNo hasRedstone = YesNo.UNDECIDED;
    private IPartHost tcb;
    private boolean requiresDynamicRender = false;
    private boolean inWorld = false;
    private VoxelShape cachedCollisionShapeLiving;
    private VoxelShape cachedCollisionShape;
    private VoxelShape cachedShape;
    private CableRenderMode cachedShapeCableRenderMode;

    public CableBusContainer(IPartHost host) {
        this.tcb = host;
    }

    public static boolean isLoading() {
        Boolean is = IS_LOADING.get();
        return is != null && is != false;
    }

    public void setHost(IPartHost host) {
        this.tcb.clearContainer();
        this.tcb = host;
    }

    @Override
    public IFacadeContainer getFacadeContainer() {
        return new FacadeContainer(this.storage, this::facadeChanged);
    }

    private void facadeChanged(Direction side) {
        this.invalidateShapes();
        this.updateNeighborShapeOnSide(side);
    }

    private ICablePart getCable() {
        return this.storage.getCenter();
    }

    @Override
    @Nullable
    public IPart getPart(@Nullable Direction partLocation) {
        if (partLocation == null) {
            return this.storage.getCenter();
        }
        return this.storage.getPart(partLocation);
    }

    @Override
    public boolean canAddPart(ItemStack is, Direction side) {
        if (FacadeItem.createFacade(is, side) != null) {
            return true;
        }
        Item item = is.getItem();
        if (item instanceof IPartItem) {
            IPartItem partItem = (IPartItem)item;
            Object part = partItem.createPart();
            if (part == null) {
                return false;
            }
            if (part instanceof ICablePart) {
                ICablePart cablePart = (ICablePart)part;
                return this.getCable() == null && this.arePartsCompatibleWithCable(cablePart);
            }
            if (side != null) {
                return this.getPart(side) == null && CableBusContainer.isPartCompatibleWithCable(part, this.getCable());
            }
        }
        return false;
    }

    @Override
    @Nullable
    public <T extends IPart> T addPart(IPartItem<T> partItem, Direction side, @Nullable Player player) {
        T part = partItem.createPart();
        if (part == null) {
            return null;
        }
        if (part instanceof ICablePart) {
            IGridNode cableNode;
            ICablePart cablePart = (ICablePart)part;
            if (this.getCable() != null || !this.arePartsCompatibleWithCable(cablePart)) {
                return null;
            }
            this.storage.setCenter(cablePart);
            cablePart.setPartHostInfo(null, this, this.tcb.getBlockEntity());
            if (player != null) {
                cablePart.onPlacement(player);
            }
            if (this.inWorld) {
                this.updateConnections();
                cablePart.addToWorld();
            }
            if ((cableNode = cablePart.getGridNode()) != null) {
                for (Direction partSide : Direction.values()) {
                    IGridNode existingPartNode;
                    IPart existingPart = this.getPart(partSide);
                    if (existingPart == null || (existingPartNode = existingPart.getGridNode()) == null) continue;
                    GridHelper.createConnection(cableNode, existingPartNode);
                }
            }
        } else if (side != null) {
            ICablePart cable = this.getCable();
            if (!CableBusContainer.isPartCompatibleWithCable(part, cable)) {
                return null;
            }
            this.storage.setPart(side, (IPart)part);
            part.setPartHostInfo(side, this, this.getBlockEntity());
            if (player != null) {
                part.onPlacement(player);
            }
            if (this.inWorld) {
                part.addToWorld();
            }
            if (cable != null) {
                IGridNode cableNode = cable.getGridNode();
                IGridNode partNode = part.getGridNode();
                if (cableNode != null && partNode != null) {
                    GridHelper.createConnection(cableNode, partNode);
                }
            }
        }
        this.updateAfterPartChange(side);
        return part;
    }

    private boolean arePartsCompatibleWithCable(ICablePart cable) {
        for (Direction d : Direction.values()) {
            IPart part = this.getPart(d);
            if (part == null || CableBusContainer.isPartCompatibleWithCable(part, cable)) continue;
            return false;
        }
        return true;
    }

    private static boolean isPartCompatibleWithCable(IPart part, @Nullable ICablePart cable) {
        return cable == null || part.canBePlacedOn(cable.supportsBuses());
    }

    @Override
    public <T extends IPart> T replacePart(IPartItem<T> partItem, @Nullable Direction side, Player owner, InteractionHand hand) {
        this.removePartWithoutUpdates(side);
        return this.addPart(partItem, side, owner);
    }

    @Override
    public void removePartFromSide(@Nullable Direction side) {
        this.removePartWithoutUpdates(side);
        this.updateAfterPartChange(side);
        if (this.isInWorld() && this.isEmpty()) {
            this.cleanup();
        }
    }

    @Override
    public boolean removePart(IPart part) {
        if (this.getPart(null) == part) {
            this.removePartFromSide(null);
            return true;
        }
        for (Direction side : Direction.values()) {
            if (this.getPart(side) != part) continue;
            this.removePartFromSide(side);
            return true;
        }
        return false;
    }

    private void updateAfterPartChange(Direction side) {
        this.invalidateShapes();
        this.updateDynamicRender();
        this.updateConnections();
        this.markForUpdate();
        this.markForSave();
        this.partChanged();
        this.getBlockEntity().invalidateCapabilities();
        this.updateNeighborShapeOnSide(side);
    }

    private void removePartWithoutUpdates(@Nullable Direction side) {
        if (side == null) {
            if (this.storage.getCenter() != null) {
                this.storage.getCenter().removeFromWorld();
            }
            this.storage.setCenter(null);
        } else {
            if (this.getPart(side) != null) {
                this.getPart(side).removeFromWorld();
            }
            this.storage.removePart(side);
        }
    }

    @Override
    public void markForUpdate() {
        this.tcb.markForUpdate();
    }

    @Override
    public DimensionalBlockPos getLocation() {
        return this.tcb.getLocation();
    }

    @Override
    public BlockEntity getBlockEntity() {
        return this.tcb.getBlockEntity();
    }

    @Override
    public AEColor getColor() {
        if (this.storage.getCenter() != null) {
            ICablePart c = this.storage.getCenter();
            return c.getCableColor();
        }
        return AEColor.TRANSPARENT;
    }

    @Override
    public void clearContainer() {
        throw new UnsupportedOperationException("Now that is silly!");
    }

    @Override
    public boolean isBlocked(Direction side) {
        return this.tcb.isBlocked(side);
    }

    @Override
    public SelectedPart selectPartLocal(Vec3 pos) {
        for (Direction side : Platform.DIRECTIONS_WITH_NULL) {
            IPart p = this.getPart(side);
            if (p == null) continue;
            ArrayList<AABB> boxes = new ArrayList<AABB>();
            BusCollisionHelper bch = new BusCollisionHelper(boxes, side, true);
            p.getBoxes(bch);
            for (AABB bb : boxes) {
                if (!(bb = bb.inflate(0.002, 0.002, 0.002)).contains(pos)) continue;
                return new SelectedPart(p, side);
            }
        }
        if (PartHelper.getCableRenderMode().opaqueFacades) {
            IFacadeContainer fc = this.getFacadeContainer();
            for (Direction side : Direction.values()) {
                IFacadePart p = fc.getFacade(side);
                if (p == null) continue;
                ArrayList<AABB> boxes = new ArrayList<AABB>();
                BusCollisionHelper bch = new BusCollisionHelper(boxes, side, true);
                p.getBoxes(bch, true);
                for (AABB bb : boxes) {
                    if (!(bb = bb.inflate(0.01, 0.01, 0.01)).contains(pos)) continue;
                    return new SelectedPart(p, side);
                }
            }
        }
        return new SelectedPart();
    }

    @Override
    public void markForSave() {
        this.tcb.markForSave();
    }

    @Override
    public void partChanged() {
        if (this.storage.getCenter() == null) {
            ArrayList<ItemStack> facades = new ArrayList<ItemStack>();
            IFacadeContainer fc = this.getFacadeContainer();
            for (Direction d : Direction.values()) {
                IFacadePart fp = fc.getFacade(d);
                if (fp == null) continue;
                facades.add(fp.getItemStack());
                fc.removeFacade(this.tcb, d);
            }
            if (!facades.isEmpty()) {
                BlockEntity te = this.tcb.getBlockEntity();
                Platform.spawnDrops(te.getLevel(), te.getBlockPos(), facades);
            }
        }
        for (Direction direction : Direction.values()) {
            IGridNode iGridNode;
            IPart part = this.getPart(direction);
            if (part == null || !((iGridNode = part.getExternalFacingNode()) instanceof InWorldGridNode)) continue;
            InWorldGridNode inWorldNode = (InWorldGridNode)iGridNode;
            inWorldNode.setExposedOnSides(EnumSet.of(direction));
        }
        this.tcb.partChanged();
    }

    @Override
    public boolean hasRedstone() {
        if (this.hasRedstone == YesNo.UNDECIDED) {
            this.updateRedstone();
        }
        return this.hasRedstone == YesNo.YES;
    }

    @Override
    public boolean isEmpty() {
        IFacadeContainer fc = this.getFacadeContainer();
        for (Direction s : Platform.DIRECTIONS_WITH_NULL) {
            IFacadePart fp;
            IPart part = this.getPart(s);
            if (part != null) {
                return false;
            }
            if (s == null || (fp = fc.getFacade(s)) == null) continue;
            return false;
        }
        return true;
    }

    @Override
    public void cleanup() {
        this.tcb.cleanup();
    }

    @Override
    public void notifyNeighbors() {
        this.tcb.notifyNeighbors();
    }

    @Override
    public void notifyNeighborNow(Direction side) {
        this.tcb.notifyNeighborNow(side);
    }

    @Override
    public boolean isInWorld() {
        return this.inWorld;
    }

    private void updateRedstone() {
        BlockEntity te = this.getBlockEntity();
        this.hasRedstone = te.getLevel().hasNeighborSignal(te.getBlockPos()) ? YesNo.YES : YesNo.NO;
    }

    private void updateDynamicRender() {
        this.requiresDynamicRender = false;
        for (Direction s : Direction.values()) {
            IPart p = this.getPart(s);
            if (p == null) continue;
            this.setRequiresDynamicRender(this.isRequiresDynamicRender() || p.requireDynamicRender());
        }
    }

    public void updateConnections() {
        ICablePart center = this.storage.getCenter();
        if (center != null) {
            EnumSet<Direction> sides = EnumSet.allOf(Direction.class);
            for (Direction s : Direction.values()) {
                if (this.getPart(s) == null && !this.isBlocked(s)) continue;
                sides.remove(s);
            }
            center.setExposedOnSides(sides);
        }
    }

    public void addToWorld() {
        if (this.inWorld) {
            return;
        }
        this.inWorld = true;
        IS_LOADING.set(true);
        BlockEntity te = this.getBlockEntity();
        for (int x = 6; x >= 0; --x) {
            IGridNode cn;
            IPart center;
            IGridNode sn;
            Direction s = Platform.DIRECTIONS_WITH_NULL[x];
            IPart part = this.getPart(s);
            if (part == null) continue;
            part.setPartHostInfo(s, this, te);
            part.addToWorld();
            if (s == null || (sn = part.getGridNode()) == null || (center = this.getPart(null)) == null || (cn = center.getGridNode()) == null) continue;
            GridHelper.createConnection(cn, sn);
        }
        this.partChanged();
        IS_LOADING.set(false);
    }

    public void removeFromWorld() {
        if (!this.inWorld) {
            return;
        }
        this.inWorld = false;
        for (Direction s : Platform.DIRECTIONS_WITH_NULL) {
            IPart part = this.getPart(s);
            if (part == null) continue;
            part.removeFromWorld();
        }
        this.invalidateShapes();
        this.partChanged();
    }

    @Override
    public IGridNode getGridNode(Direction side) {
        IGridNode n;
        IPart part = this.getPart(side);
        if (part != null && (n = part.getExternalFacingNode()) != null) {
            return n;
        }
        if (this.storage.getCenter() != null) {
            return this.storage.getCenter().getGridNode();
        }
        return null;
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        IPart part = this.getPart(dir);
        if (part != null) {
            return part.getExternalCableConnectionType();
        }
        if (this.storage.getCenter() != null) {
            ICablePart c = this.storage.getCenter();
            return c.getCableConnectionType();
        }
        return AECableType.NONE;
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return this.getPart(null) instanceof ICablePart ? this.getPart(null).getCableConnectionLength(cable) : -1.0f;
    }

    @Override
    public int isProvidingStrongPower(Direction side) {
        IPart part = this.getPart(side);
        return part != null ? part.isProvidingStrongPower() : 0;
    }

    @Override
    public int isProvidingWeakPower(Direction side) {
        IPart part = this.getPart(side);
        return part != null ? part.isProvidingWeakPower() : 0;
    }

    @Override
    public boolean canConnectRedstone(Direction opposite) {
        IPart part = this.getPart(opposite);
        return part != null && part.canConnectRedstone();
    }

    @Override
    public void onEntityCollision(Entity entity) {
        for (Direction s : Platform.DIRECTIONS_WITH_NULL) {
            IPart part = this.getPart(s);
            if (part == null) continue;
            part.onEntityCollision(entity);
        }
    }

    @Override
    public boolean useItemOn(ItemStack heldItem, Player player, InteractionHand hand, Vec3 localPos) {
        SelectedPart p = this.selectPartLocal(localPos);
        if (p != null && p.part != null) {
            return p.part.onUseItemOn(heldItem, player, hand, localPos);
        }
        if (p != null && p.facade != null && p.facade.onUseItemOn(heldItem, player, hand, localPos)) {
            this.markForSave();
            this.markForUpdate();
            return true;
        }
        return false;
    }

    @Override
    public boolean useWithoutItem(Player player, Vec3 localPos) {
        SelectedPart p = this.selectPartLocal(localPos);
        if (p != null && p.part != null) {
            return p.part.onUseWithoutItem(player, localPos);
        }
        return false;
    }

    @Override
    public void onNeighborChanged(BlockGetter level, BlockPos pos, BlockPos neighbor) {
        this.hasRedstone = YesNo.UNDECIDED;
        for (Direction s : Platform.DIRECTIONS_WITH_NULL) {
            IPart part = this.getPart(s);
            if (part == null) continue;
            part.onNeighborChanged(level, pos, neighbor);
        }
    }

    @Override
    public void onUpdateShape(LevelAccessor level, BlockPos pos, Direction side) {
        for (Direction s : Platform.DIRECTIONS_WITH_NULL) {
            IPart part = this.getPart(s);
            if (part == null) continue;
            part.onUpdateShape(side);
        }
        this.invalidateShapes();
    }

    @Override
    public boolean isLadder(LivingEntity entity) {
        for (Direction side : Platform.DIRECTIONS_WITH_NULL) {
            IPart p = this.getPart(side);
            if (p == null || !p.isLadder(entity)) continue;
            return true;
        }
        return false;
    }

    @Override
    public void animateTick(Level level, BlockPos pos, RandomSource r) {
        for (Direction side : Platform.DIRECTIONS_WITH_NULL) {
            IPart p = this.getPart(side);
            if (p == null) continue;
            p.animateTick(level, pos, r);
        }
    }

    @Override
    public int getLightValue() {
        int light = 0;
        for (Direction d : Platform.DIRECTIONS_WITH_NULL) {
            IPart p = this.getPart(d);
            if (p == null) continue;
            light = Math.max(p.getLightLevel(), light);
        }
        return light;
    }

    public void writeToStream(RegistryFriendlyByteBuf data) {
        IPart p;
        int x;
        int sides = 0;
        for (x = 0; x < Platform.DIRECTIONS_WITH_NULL.length; ++x) {
            p = this.getPart(Platform.DIRECTIONS_WITH_NULL[x]);
            if (p == null) continue;
            sides |= 1 << x;
        }
        data.writeByte((byte)sides);
        for (x = 0; x < Platform.DIRECTIONS_WITH_NULL.length; ++x) {
            p = this.getPart(Platform.DIRECTIONS_WITH_NULL[x]);
            if (p == null) continue;
            data.writeVarInt(IPartItem.getNetworkId(p.getPartItem()));
            p.writeToStream(data);
        }
        this.getFacadeContainer().writeToStream(data);
    }

    public boolean readFromStream(RegistryFriendlyByteBuf data) {
        byte sides = data.readByte();
        boolean updateBlock = false;
        for (int x = 0; x < Platform.DIRECTIONS_WITH_NULL.length; ++x) {
            Direction side = Platform.DIRECTIONS_WITH_NULL[x];
            if ((sides & 1 << x) == 1 << x) {
                IPart p = this.getPart(side);
                int itemId = data.readVarInt();
                IPartItem<?> partItem = IPartItem.byNetworkId(itemId);
                if (p != null && p.getPartItem() == partItem) {
                    if (!p.readFromStream(data)) continue;
                    updateBlock = true;
                    continue;
                }
                if (partItem != null) {
                    this.removePartFromSide(side);
                    p = this.addPart(partItem, side, null);
                    if (p != null) {
                        p.readFromStream(data);
                        continue;
                    }
                    throw new IllegalStateException("Invalid Stream For CableBus Container.");
                }
                throw new IllegalStateException("Invalid item from server for part: " + itemId);
            }
            if (this.getPart(side) == null) continue;
            this.removePartFromSide(side);
        }
        this.invalidateShapes();
        return updateBlock |= this.getFacadeContainer().readFromStream(data);
    }

    private static int getSideIndex(@Nullable Direction side) {
        return side == null ? 6 : side.ordinal();
    }

    public void writeToNBT(CompoundTag data, HolderLookup.Provider registries) {
        data.putInt("hasRedstone", this.hasRedstone.ordinal());
        this.getFacadeContainer().writeToNBT(data, registries);
        boolean saveVisualState = VisualStateSaving.isEnabled(this.getBlockEntity().getLevel());
        for (Direction side : Platform.DIRECTIONS_WITH_NULL) {
            IPart part = this.getPart(side);
            if (part == null) continue;
            CompoundTag partData = new CompoundTag();
            if (saveVisualState) {
                CompoundTag visualTag = new CompoundTag();
                part.writeVisualStateToNBT(visualTag);
                partData.put("visual", (Tag)visualTag);
            }
            part.writeToNBT(partData, registries);
            if (partData.contains("id")) {
                throw new IllegalStateException("Part " + String.valueOf(part) + " used the reserved 'id' field to store its data");
            }
            partData.putString("id", IPartItem.getId(part.getPartItem()).toString());
            String sideKey = NBT_KEY_SIDES[CableBusContainer.getSideIndex(side)];
            data.put(sideKey, (Tag)partData);
        }
    }

    public void readFromNBT(CompoundTag data, HolderLookup.Provider registries) {
        this.invalidateShapes();
        if (data.contains("hasRedstone")) {
            this.hasRedstone = YesNo.values()[data.getInt("hasRedstone")];
        }
        for (Direction side : Platform.DIRECTIONS_WITH_NULL) {
            CompoundTag partData;
            int sideIndex = CableBusContainer.getSideIndex(side);
            String sideKey = NBT_KEY_SIDES[sideIndex];
            Tag sideTag = data.get(sideKey);
            if (sideTag instanceof CompoundTag && this.loadPart(side, partData = (CompoundTag)sideTag, registries)) continue;
            this.removePartFromSide(side);
        }
        this.getFacadeContainer().readFromNBT(data, registries);
    }

    private boolean loadPart(Direction side, CompoundTag data, HolderLookup.Provider registries) {
        ResourceLocation itemId = ResourceLocation.parse((String)data.getString("id"));
        IPartItem<?> partItem = IPartItem.byId(itemId);
        if (partItem == null) {
            AELog.warn("Ignoring persisted part with non-part-item %s", itemId);
            return false;
        }
        IPart p = this.getPart(side);
        if (p != null && p.getPartItem() == partItem) {
            p.readFromNBT(data, registries);
        } else {
            p = this.replacePart(partItem, side, null, null);
            if (p != null) {
                p.readFromNBT(data, registries);
            } else {
                AELog.warn("Invalid NBT For CableBus Container: " + String.valueOf(itemId) + " is not a valid part; it was ignored.", new Object[0]);
            }
        }
        return true;
    }

    public List<ItemStack> addPartDrops(List<ItemStack> drops) {
        for (Direction side : Platform.DIRECTIONS_WITH_NULL) {
            IFacadePart fp;
            IPart part = this.getPart(side);
            if (part != null) {
                part.addPartDrop(drops, false);
            }
            if (side == null || (fp = this.getFacadeContainer().getFacade(side)) == null) continue;
            drops.add(fp.getItemStack());
        }
        return drops;
    }

    public void addAdditionalDrops(List<ItemStack> drops) {
        for (Direction side : Platform.DIRECTIONS_WITH_NULL) {
            IPart part = this.getPart(side);
            if (part == null) continue;
            part.addAdditionalDrops(drops, false);
        }
    }

    public void clearContent() {
        for (Direction s : Platform.DIRECTIONS_WITH_NULL) {
            IPart part = this.getPart(s);
            if (part == null) continue;
            part.clearContent();
        }
    }

    @Override
    public boolean recolourBlock(Direction side, AEColor colour, Player who) {
        IPart cable = this.getPart(null);
        if (cable != null) {
            ICablePart pc = (ICablePart)cable;
            return pc.changeColor(colour, who);
        }
        return false;
    }

    public boolean isRequiresDynamicRender() {
        return this.requiresDynamicRender;
    }

    private void setRequiresDynamicRender(boolean requiresDynamicRender) {
        this.requiresDynamicRender = requiresDynamicRender;
    }

    @Override
    public CableBusRenderState getRenderState() {
        CablePart cable = (CablePart)this.storage.getCenter();
        CableBusRenderState renderState = new CableBusRenderState();
        if (cable != null) {
            renderState.setCableColor(cable.getCableColor());
            renderState.setCableType(cable.getCableConnectionType());
            renderState.setCoreType(CableCoreType.fromCableType(cable.getCableConnectionType()));
            for (Direction side : Direction.values()) {
                if (!cable.isConnected(side)) continue;
                AECableType connectionType = cable.getCableConnectionType();
                BlockPos adjacentPos = this.getBlockEntity().getBlockPos().relative(side);
                IInWorldGridNodeHost adjacentHost = GridHelper.getNodeHost(this.getBlockEntity().getLevel(), adjacentPos);
                if (adjacentHost != null) {
                    AECableType adjacentType = adjacentHost.getCableConnectionType(side.getOpposite());
                    connectionType = AECableType.min(connectionType, adjacentType);
                }
                if (adjacentHost instanceof CableBusContainer) {
                    renderState.getCableBusAdjacent().add(side);
                }
                renderState.getConnectionTypes().put(side, connectionType);
            }
            for (Direction side : Direction.values()) {
                int channels = cable.getCableConnectionType().isSmart() ? cable.getChannelsOnSide(side) : 0;
                renderState.getChannelsOnSide().put(side, channels);
            }
        }
        for (Direction side : Direction.values()) {
            int length;
            IPart part;
            FacadeRenderState facadeState = this.getFacadeRenderState(side);
            if (facadeState != null) {
                renderState.getFacades().put(side, facadeState);
            }
            if ((part = this.getPart(side)) == null) continue;
            renderState.getPartModelData().put(side, part.getModelData());
            BusCollisionHelper bch = new BusCollisionHelper(renderState.getBoundingBoxes(), side, true);
            part.getBoxes(bch);
            AECableType desiredType = part.getDesiredConnectionType();
            if (renderState.getCoreType() == CableCoreType.GLASS && (desiredType == AECableType.SMART || desiredType == AECableType.COVERED)) {
                renderState.setCoreType(CableCoreType.COVERED);
            }
            if ((length = (int)part.getCableConnectionLength(null)) > 0 && length <= 8) {
                renderState.getAttachmentConnections().put(side, length);
            }
            renderState.getAttachments().put(side, part.getStaticModels());
        }
        return renderState;
    }

    private FacadeRenderState getFacadeRenderState(Direction side) {
        IFacadePart facade = this.storage.getFacade(side);
        if (facade != null) {
            BlockState blockState = facade.getBlockState();
            Level level = this.getBlockEntity().getLevel();
            if (blockState != null && level != null) {
                return new FacadeRenderState(blockState, !facade.getBlockState().isSolidRender((BlockGetter)level, this.getBlockEntity().getBlockPos()));
            }
        }
        return null;
    }

    public VoxelShape getShape() {
        CableRenderMode currentRenderMode = PartHelper.getCableRenderMode();
        if (this.cachedShape == null || currentRenderMode != this.cachedShapeCableRenderMode) {
            this.cachedShape = this.createShape(false, false);
            this.cachedShapeCableRenderMode = currentRenderMode;
        }
        return this.cachedShape;
    }

    @Override
    public VoxelShape getCollisionShape(CollisionContext context) {
        EntityCollisionContext entityContext;
        boolean itemEntity;
        boolean bl = itemEntity = context instanceof EntityCollisionContext && (entityContext = (EntityCollisionContext)context).getEntity() instanceof ItemEntity;
        if (itemEntity) {
            if (this.cachedCollisionShapeLiving == null) {
                this.cachedCollisionShapeLiving = this.createShape(true, true);
            }
            return this.cachedCollisionShapeLiving;
        }
        if (this.cachedCollisionShape == null) {
            this.cachedCollisionShape = this.createShape(true, false);
        }
        return this.cachedCollisionShape;
    }

    private VoxelShape createShape(boolean forCollision, boolean forItemEntity) {
        ArrayList<AABB> boxes = new ArrayList<AABB>();
        IFacadeContainer fc = this.getFacadeContainer();
        for (Direction s : Platform.DIRECTIONS_WITH_NULL) {
            IFacadePart fp;
            BusCollisionHelper bch = new BusCollisionHelper(boxes, s, !forCollision);
            IPart part = this.getPart(s);
            if (part != null) {
                part.getBoxes(bch);
            }
            if (!PartHelper.getCableRenderMode().opaqueFacades && !forCollision || s == null || (fp = fc.getFacade(s)) == null) continue;
            fp.getBoxes(bch, forItemEntity);
        }
        return VoxelShapeCache.get(boxes);
    }

    private void invalidateShapes() {
        this.cachedShape = null;
        this.cachedCollisionShape = null;
        this.cachedCollisionShapeLiving = null;
    }

    private void updateNeighborShapeOnSide(Direction side) {
        if (side == null) {
            return;
        }
        BlockEntity be = this.getBlockEntity();
        if (be != null && be.getLevel() != null && !be.getLevel().isClientSide()) {
            TickHandler.instance().addCallable((LevelAccessor)be.getLevel(), level -> {
                if (!be.isRemoved()) {
                    BlockPos ourPos = be.getBlockPos();
                    BlockPos neighborPos = ourPos.relative(side);
                    BlockState neighborState = level.getBlockState(neighborPos);
                    BlockState ourState = be.getBlockState();
                    BlockState newNeighborState = neighborState.updateShape(side.getOpposite(), ourState, (LevelAccessor)level, neighborPos, ourPos);
                    Block.updateOrDestroy((BlockState)neighborState, (BlockState)newNeighborState, (LevelAccessor)level, (BlockPos)neighborPos, (int)3, (int)512);
                }
            });
        }
    }
}

