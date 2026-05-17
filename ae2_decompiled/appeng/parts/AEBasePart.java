/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.stream.JsonWriter
 *  it.unimi.dsi.fastutil.objects.Reference2IntMap
 *  net.minecraft.CrashReportCategory
 *  net.minecraft.core.Direction
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.component.DataComponentMap
 *  net.minecraft.core.component.DataComponentMap$Builder
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.Component$Serializer
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.Nameable
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.phys.Vec3
 *  org.jetbrains.annotations.MustBeInvokedByOverriders
 *  org.jetbrains.annotations.Nullable
 */
package appeng.parts;

import appeng.api.ids.AEComponents;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.stacks.AEItemKey;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEParts;
import appeng.items.tools.MemoryCardItem;
import appeng.util.IDebugExportable;
import appeng.util.InteractionUtil;
import appeng.util.JsonStreamUtil;
import appeng.util.SettingsFrom;
import com.google.gson.stream.JsonWriter;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

public abstract class AEBasePart
implements IPart,
IActionHost,
ISegmentedInventory,
IPowerChannelState,
Nameable,
IDebugExportable {
    private final IManagedGridNode mainNode;
    private IPartItem<?> partItem;
    private BlockEntity blockEntity = null;
    private IPartHost host = null;
    @Nullable
    private Direction side;
    @Nullable
    private Component customName;
    private boolean clientSidePowered;
    private boolean clientSideMissingChannel;

    public AEBasePart(IPartItem<?> partItem) {
        this.partItem = Objects.requireNonNull(partItem, "partItem");
        this.mainNode = this.createMainNode().setVisualRepresentation(AEItemKey.of(this.partItem)).setExposedOnSides(EnumSet.noneOf(Direction.class));
    }

    protected IManagedGridNode createMainNode() {
        return GridHelper.createManagedNode(this, NodeListener.INSTANCE);
    }

    @MustBeInvokedByOverriders
    protected void onMainNodeStateChanged(IGridNodeListener.State reason) {
        if (reason != IGridNodeListener.State.GRID_BOOT) {
            this.markForUpdateIfClientFlagsChanged();
        }
    }

    public final boolean isClientSide() {
        return this.blockEntity == null || this.blockEntity.getLevel() == null || this.blockEntity.getLevel().isClientSide();
    }

    public IPartHost getHost() {
        return this.host;
    }

    protected AEColor getColor() {
        if (this.host == null) {
            return AEColor.TRANSPARENT;
        }
        return this.host.getColor();
    }

    public IManagedGridNode getMainNode() {
        return this.mainNode;
    }

    @Override
    public IGridNode getActionableNode() {
        return this.mainNode.getNode();
    }

    public final BlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    public Level getLevel() {
        return this.blockEntity.getLevel();
    }

    public Component getName() {
        return Objects.requireNonNullElse(this.customName, this.partItem.asItem().getDescription());
    }

    @Nullable
    public Component getCustomName() {
        return this.customName;
    }

    @Override
    public void addEntityCrashInfo(CrashReportCategory crashreportcategory) {
        crashreportcategory.setDetail("Part Side", (Object)this.getSide());
        BlockEntity beHost = this.getBlockEntity();
        if (beHost != null) {
            beHost.fillCrashReportCategory(crashreportcategory);
            Level level = beHost.getLevel();
            if (level != null) {
                crashreportcategory.setDetail("Level", (Object)level.dimension());
            }
        }
    }

    @Override
    public IPartItem<?> getPartItem() {
        return this.partItem;
    }

    protected void setPartItem(IPartItem<?> partItem) {
        if (partItem != this.partItem) {
            this.partItem = Objects.requireNonNull(partItem);
            this.getMainNode().setVisualRepresentation(partItem);
        }
    }

    @Override
    public void readFromNBT(CompoundTag data, HolderLookup.Provider registries) {
        this.mainNode.loadFromNBT(data);
        if (data.contains("customName")) {
            try {
                this.customName = Component.Serializer.fromJson((String)data.getString("customName"), (HolderLookup.Provider)registries);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (data.contains("visual", 10)) {
            this.readVisualStateFromNBT(data.getCompound("visual"));
        }
    }

    @Override
    public void writeToNBT(CompoundTag data, HolderLookup.Provider registries) {
        this.mainNode.saveToNBT(data);
        if (this.customName != null) {
            data.putString("customName", Component.Serializer.toJson((Component)this.customName, (HolderLookup.Provider)registries));
        }
    }

    @Override
    @MustBeInvokedByOverriders
    public void writeToStream(RegistryFriendlyByteBuf data) {
        this.clientSidePowered = this.isPowered();
        this.clientSideMissingChannel = this.isMissingChannel();
        int flags = 0;
        if (this.clientSidePowered) {
            flags |= 1;
        }
        if (this.clientSideMissingChannel) {
            flags |= 2;
        }
        data.writeByte(flags);
    }

    @Override
    @MustBeInvokedByOverriders
    public boolean readFromStream(RegistryFriendlyByteBuf data) {
        byte flags = data.readByte();
        boolean wasPowered = this.clientSidePowered;
        boolean wasMissingChannel = this.clientSideMissingChannel;
        this.clientSidePowered = (flags & 1) != 0;
        this.clientSideMissingChannel = (flags & 2) != 0;
        return this.shouldSendPowerStateToClient() && this.clientSidePowered != wasPowered || this.shouldSendMissingChannelStateToClient() && this.clientSideMissingChannel != wasMissingChannel;
    }

    @Override
    @MustBeInvokedByOverriders
    public void writeVisualStateToNBT(CompoundTag data) {
        data.putBoolean("powered", this.isPowered());
        data.putBoolean("missingChannel", this.isMissingChannel());
    }

    @Override
    @MustBeInvokedByOverriders
    public void readVisualStateFromNBT(CompoundTag data) {
        this.clientSidePowered = data.getBoolean("powered");
        this.clientSideMissingChannel = data.getBoolean("missingChannel");
    }

    @Override
    public IGridNode getGridNode() {
        return this.mainNode.getNode();
    }

    @Override
    public void removeFromWorld() {
        this.mainNode.destroy();
    }

    @Override
    public void addToWorld() {
        this.mainNode.create(this.getLevel(), this.blockEntity.getBlockPos());
    }

    @Override
    public void setPartHostInfo(Direction side, IPartHost host, BlockEntity blockEntity) {
        this.setSide(side);
        this.blockEntity = blockEntity;
        this.host = host;
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 3.0f;
    }

    @Override
    @MustBeInvokedByOverriders
    public void importSettings(SettingsFrom mode, DataComponentMap input, @Nullable Player player) {
        if (mode == SettingsFrom.DISMANTLE_ITEM) {
            this.customName = (Component)input.get(DataComponents.CUSTOM_NAME);
        } else if (mode == SettingsFrom.MEMORY_CARD) {
            this.customName = (Component)input.get(AEComponents.EXPORTED_CUSTOM_NAME);
        }
        MemoryCardItem.importGenericSettings(this, input, player);
    }

    @Override
    @MustBeInvokedByOverriders
    public void exportSettings(SettingsFrom mode, DataComponentMap.Builder builder) {
        if (mode == SettingsFrom.DISMANTLE_ITEM) {
            builder.set(DataComponents.CUSTOM_NAME, (Object)this.customName);
        } else if (mode == SettingsFrom.MEMORY_CARD) {
            builder.set(AEComponents.EXPORTED_CUSTOM_NAME, (Object)this.customName);
        }
        if (mode == SettingsFrom.MEMORY_CARD) {
            MemoryCardItem.exportGenericSettings(this, builder);
            builder.set(AEComponents.EXPORTED_SETTINGS_SOURCE, (Object)this.getPartItem().asItem().getDescription());
        }
    }

    public final DataComponentMap exportSettings(SettingsFrom mode) {
        DataComponentMap.Builder builder = DataComponentMap.builder();
        this.exportSettings(mode, builder);
        return builder.build();
    }

    public boolean useStandardMemoryCard() {
        return true;
    }

    private boolean useMemoryCard(ItemStack memCardIS, Player player) {
        Item item;
        if (!this.useStandardMemoryCard() || !((item = memCardIS.getItem()) instanceof IMemoryCard)) {
            return false;
        }
        IMemoryCard memoryCard = (IMemoryCard)item;
        Item partItem = this.getPartItem().asItem();
        if (AEParts.INTERFACE.asItem() == partItem) {
            partItem = AEBlocks.INTERFACE.asItem();
        } else if (AEParts.PATTERN_PROVIDER.asItem() == partItem) {
            partItem = AEBlocks.PATTERN_PROVIDER.asItem();
        }
        Component name = partItem.getDescription();
        if (InteractionUtil.isInAlternateUseMode(player)) {
            DataComponentMap settings = this.exportSettings(SettingsFrom.MEMORY_CARD);
            if (!settings.isEmpty()) {
                MemoryCardItem.clearCard(memCardIS);
                memCardIS.applyComponents(settings);
                memoryCard.notifyUser(player, MemoryCardMessages.SETTINGS_SAVED);
            }
        } else {
            Component storedName = (Component)memCardIS.get(AEComponents.EXPORTED_SETTINGS_SOURCE);
            if (name.equals((Object)storedName)) {
                this.importSettings(SettingsFrom.MEMORY_CARD, memCardIS.getComponents(), player);
                memoryCard.notifyUser(player, MemoryCardMessages.SETTINGS_LOADED);
            } else {
                MemoryCardItem.importGenericSettingsAndNotify(this, memCardIS.getComponents(), player);
            }
        }
        return true;
    }

    @Override
    public boolean onUseItemOn(ItemStack heldItem, Player player, InteractionHand hand, Vec3 pos) {
        if (this.useMemoryCard(heldItem, player)) {
            return true;
        }
        return IPart.super.onUseItemOn(heldItem, player, hand, pos);
    }

    @Override
    public void onPlacement(Player player) {
        this.mainNode.setOwningPlayer(player);
    }

    public Direction getSide() {
        return this.side;
    }

    private void setSide(Direction side) {
        this.side = side;
    }

    @Override
    @Nullable
    @MustBeInvokedByOverriders
    public InternalInventory getSubInventory(ResourceLocation id) {
        return null;
    }

    @Override
    public boolean isPowered() {
        if (this.isClientSide()) {
            return this.clientSidePowered;
        }
        IGridNode node = this.getGridNode();
        return node != null && node.isPowered();
    }

    public boolean isMissingChannel() {
        if (this.isClientSide()) {
            return this.clientSideMissingChannel;
        }
        IGridNode node = this.getGridNode();
        return node == null || !node.meetsChannelRequirements();
    }

    @Override
    public boolean isActive() {
        return this.isPowered() && !this.isMissingChannel();
    }

    private void markForUpdateIfClientFlagsChanged() {
        boolean changed = false;
        if (this.shouldSendPowerStateToClient() && this.isPowered() != this.clientSidePowered) {
            changed = true;
        }
        if (!changed && this.shouldSendMissingChannelStateToClient() && this.isMissingChannel() != this.clientSideMissingChannel) {
            changed = true;
        }
        if (changed) {
            this.getHost().markForUpdate();
        }
    }

    protected boolean shouldSendPowerStateToClient() {
        return true;
    }

    protected boolean shouldSendMissingChannelStateToClient() {
        return true;
    }

    @Override
    public void debugExport(JsonWriter writer, HolderLookup.Provider registries, Reference2IntMap<Object> machineIds, Reference2IntMap<IGridNode> nodeIds) throws IOException {
        int myId = machineIds.getOrDefault((Object)this, -1);
        JsonStreamUtil.writeProperties(Map.of("id", myId, "item", BuiltInRegistries.ITEM.getKey((Object)this.getPartItem().asItem()).toString(), "mainNodeId", nodeIds.getOrDefault((Object)this.mainNode.getNode(), -1)), writer);
    }

    public static class NodeListener<T extends AEBasePart>
    implements IGridNodeListener<T> {
        public static final NodeListener<AEBasePart> INSTANCE = new NodeListener();

        @Override
        public void onSaveChanges(T nodeOwner, IGridNode node) {
            ((AEBasePart)nodeOwner).getHost().markForSave();
        }

        @Override
        public void onStateChanged(T nodeOwner, IGridNode node, IGridNodeListener.State state) {
            ((AEBasePart)nodeOwner).onMainNodeStateChanged(state);
        }
    }
}

