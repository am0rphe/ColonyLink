/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.stream.JsonWriter
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  io.netty.buffer.Unpooled
 *  it.unimi.dsi.fastutil.objects.Reference2IntMap
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.GlobalPos
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.RegistryAccess
 *  net.minecraft.core.component.DataComponentMap
 *  net.minecraft.core.component.DataComponentMap$Builder
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientGamePacketListener
 *  net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
 *  net.minecraft.resources.RegistryOps
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.Clearable
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.Nameable
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.BlockHitResult
 *  net.neoforged.neoforge.client.model.data.ModelData
 *  org.jetbrains.annotations.ApiStatus$OverrideOnly
 *  org.jetbrains.annotations.MustBeInvokedByOverriders
 *  org.jetbrains.annotations.Nullable
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package appeng.blockentity;

import appeng.api.ids.AEComponents;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.orientation.BlockOrientation;
import appeng.api.orientation.RelativeSide;
import appeng.block.AEBaseEntityBlock;
import appeng.client.render.model.AEModelData;
import appeng.core.AELog;
import appeng.hooks.VisualStateSaving;
import appeng.hooks.ticking.TickHandler;
import appeng.items.tools.MemoryCardItem;
import appeng.util.IDebugExportable;
import appeng.util.JsonStreamUtil;
import appeng.util.SettingsFrom;
import appeng.util.helpers.ItemComparisonHelper;
import com.google.gson.stream.JsonWriter;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AEBaseBlockEntity
extends BlockEntity
implements Nameable,
ISegmentedInventory,
Clearable,
IDebugExportable {
    private static final Logger LOG = LoggerFactory.getLogger(AEBaseBlockEntity.class);
    private static final Map<BlockEntityType<?>, Item> REPRESENTATIVE_ITEMS = new HashMap();
    @Nullable
    private Component customName;
    private boolean setChangedQueued = false;
    private byte queuedForReady = 0;
    private byte readyInvoked = 0;

    public AEBaseBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    public static void registerBlockEntityItem(BlockEntityType<?> type, Item wat) {
        REPRESENTATIVE_ITEMS.put(type, wat);
    }

    public boolean notLoaded() {
        return !this.level.hasChunkAt(this.worldPosition);
    }

    public final GlobalPos getGlobalPos() {
        if (this.level == null) {
            throw new IllegalStateException("Block entity is not in a level");
        }
        return GlobalPos.of((ResourceKey)this.level.dimension(), (BlockPos)this.getBlockPos());
    }

    public BlockEntity getBlockEntity() {
        return this;
    }

    protected Item getItemFromBlockEntity() {
        return REPRESENTATIVE_ITEMS.getOrDefault(this.getType(), Items.AIR);
    }

    public final void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        RegistryAccess registryAccess = null;
        if (registries instanceof RegistryAccess) {
            registryAccess = (RegistryAccess)registries;
        } else if (this.level != null) {
            registryAccess = this.level.registryAccess();
        }
        if (tag.contains("#upd", 7) && tag.size() == 1) {
            byte[] updateData = tag.getByteArray("#upd");
            if (registryAccess == null) {
                LOG.warn("Ignoring  update packet for {} since no registry is available.", (Object)this);
            } else if (this.readUpdateData(new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer((byte[])updateData), registryAccess)) && this.level != null) {
                this.requestModelDataUpdate();
                this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 0);
            }
            return;
        }
        if (tag.contains("visual", 10)) {
            this.loadVisualState(tag.getCompound("visual"));
        }
        super.loadAdditional(tag, registries);
        this.loadTag(tag, registries);
    }

    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        this.customName = data.contains("customName") ? Component.literal((String)data.getString("customName")) : null;
    }

    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        if (VisualStateSaving.isEnabled(this.level)) {
            CompoundTag visualTag = new CompoundTag();
            this.saveVisualState(visualTag);
            data.put("visual", (Tag)visualTag);
        }
        super.saveAdditional(data, registries);
        if (this.customName != null) {
            data.putString("customName", this.customName.getString());
        }
    }

    @MustBeInvokedByOverriders
    public void onReady() {
        this.readyInvoked = (byte)(this.readyInvoked + 1);
    }

    protected void scheduleInit() {
        this.queuedForReady = (byte)(this.queuedForReady + 1);
        GridHelper.onFirstTick(this, AEBaseBlockEntity::onReady);
    }

    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag data = new CompoundTag();
        RegistryFriendlyByteBuf stream = new RegistryFriendlyByteBuf(Unpooled.buffer(), this.level.registryAccess());
        this.writeToStream(stream);
        stream.capacity(stream.readableBytes());
        data.putByteArray("#upd", stream.array());
        return data;
    }

    private boolean readUpdateData(RegistryFriendlyByteBuf stream) {
        boolean output = false;
        try {
            output = this.readFromStream(stream);
        }
        catch (Throwable t) {
            AELog.warn(t);
        }
        return output;
    }

    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create((BlockEntity)this);
    }

    protected boolean readFromStream(RegistryFriendlyByteBuf data) {
        return false;
    }

    protected void writeToStream(RegistryFriendlyByteBuf data) {
    }

    @MustBeInvokedByOverriders
    protected void saveVisualState(CompoundTag data) {
    }

    @MustBeInvokedByOverriders
    protected void loadVisualState(CompoundTag data) {
    }

    public void markForClientUpdate() {
        this.requestModelDataUpdate();
        if (this.level != null && !this.isRemoved() && !this.notLoaded()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 2);
        }
    }

    public void markForUpdate() {
        this.requestModelDataUpdate();
        if (this.level != null && !this.isRemoved() && !this.notLoaded()) {
            AEBaseEntityBlock block;
            BlockState newState;
            boolean alreadyUpdated = false;
            BlockState currentState = this.getBlockState();
            Block block2 = currentState.getBlock();
            if (block2 instanceof AEBaseEntityBlock && currentState != (newState = (block = (AEBaseEntityBlock)block2).getBlockEntityBlockState(currentState, this))) {
                AELog.blockUpdate(this.worldPosition, currentState, newState, this);
                this.level.setBlockAndUpdate(this.worldPosition, newState);
                alreadyUpdated = true;
            }
            if (!alreadyUpdated) {
                this.level.sendBlockUpdated(this.worldPosition, currentState, currentState, 1);
            }
        }
    }

    public final BlockOrientation getOrientation() {
        return BlockOrientation.get(this.getBlockState());
    }

    public Direction getFront() {
        return this.getOrientation().getSide(RelativeSide.FRONT);
    }

    public Direction getTop() {
        return this.getOrientation().getSide(RelativeSide.TOP);
    }

    @ApiStatus.OverrideOnly
    protected void onOrientationChanged(BlockOrientation orientation) {
        this.invalidateCapabilities();
    }

    public final DataComponentMap exportSettings(SettingsFrom mode, @Nullable Player player) {
        DataComponentMap.Builder builder = DataComponentMap.builder();
        this.exportSettings(mode, builder, player);
        return builder.build();
    }

    @MustBeInvokedByOverriders
    public void exportSettings(SettingsFrom mode, DataComponentMap.Builder builder, @Nullable Player player) {
        if (mode == SettingsFrom.DISMANTLE_ITEM) {
            builder.set(DataComponents.CUSTOM_NAME, (Object)this.customName);
        } else if (mode == SettingsFrom.MEMORY_CARD) {
            builder.set(AEComponents.EXPORTED_CUSTOM_NAME, (Object)this.customName);
        }
        if (mode == SettingsFrom.MEMORY_CARD) {
            MemoryCardItem.exportGenericSettings(this, builder);
            builder.set(AEComponents.EXPORTED_SETTINGS_SOURCE, (Object)this.getItemFromBlockEntity().getDescription());
        }
    }

    @MustBeInvokedByOverriders
    public void importSettings(SettingsFrom mode, DataComponentMap input, @Nullable Player player) {
        if (mode == SettingsFrom.DISMANTLE_ITEM) {
            this.customName = (Component)input.get(DataComponents.CUSTOM_NAME);
        } else if (mode == SettingsFrom.MEMORY_CARD) {
            this.customName = (Component)input.get(AEComponents.EXPORTED_CUSTOM_NAME);
        }
        MemoryCardItem.importGenericSettings(this, input, player);
    }

    @MustBeInvokedByOverriders
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
    }

    @MustBeInvokedByOverriders
    public void clearContent() {
    }

    public Component getName() {
        return Objects.requireNonNullElse(this.customName, this.getItemFromBlockEntity().getDescription());
    }

    @Nullable
    public Component getCustomName() {
        return this.customName;
    }

    public boolean isClientSide() {
        Level level = this.getLevel();
        return level == null || level.isClientSide();
    }

    public void saveChanges() {
        if (this.level == null) {
            return;
        }
        if (this.level.isClientSide) {
            this.setChanged();
        } else {
            this.level.blockEntityChanged(this.worldPosition);
            if (!this.setChangedQueued) {
                TickHandler.instance().addCallable(null, this::setChangedAtEndOfTick);
                this.setChangedQueued = true;
            }
        }
    }

    private Object setChangedAtEndOfTick(Level level) {
        this.setChanged();
        this.setChangedQueued = false;
        return null;
    }

    public void setName(String name) {
        this.customName = Component.literal((String)name);
    }

    @Override
    @Nullable
    @MustBeInvokedByOverriders
    public InternalInventory getSubInventory(ResourceLocation id) {
        return null;
    }

    public ModelData getModelData() {
        return AEModelData.create();
    }

    public InteractionResult disassembleWithWrench(Player player, Level level, BlockHitResult hitResult, ItemStack wrench) {
        BlockPos pos = hitResult.getBlockPos();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            List drops = Block.getDrops((BlockState)state, (ServerLevel)serverLevel, (BlockPos)pos, (BlockEntity)this, (Entity)player, (ItemStack)wrench);
            ItemStack op = new ItemStack((ItemLike)state.getBlock());
            for (ItemStack ol : drops) {
                if (!ItemComparisonHelper.isEqualItemType(ol, op)) continue;
                DataComponentMap settings = this.exportSettings(SettingsFrom.DISMANTLE_ITEM, player);
                ol.applyComponents(settings);
                break;
            }
            this.addAdditionalDrops(level, pos, drops);
            this.clearContent();
            for (ItemStack item : drops) {
                player.getInventory().placeItemBackInInventory(item);
            }
        }
        block.playerWillDestroy(level, pos, state, player);
        level.removeBlock(pos, false);
        block.destroy((LevelAccessor)level, pos, this.getBlockState());
        return InteractionResult.sidedSuccess((boolean)level.isClientSide());
    }

    public byte getQueuedForReady() {
        return this.queuedForReady;
    }

    public byte getReadyInvoked() {
        return this.readyInvoked;
    }

    public void setBlockState(BlockState state) {
        BlockOrientation previousOrientation = BlockOrientation.get(this.getBlockState());
        super.setBlockState(state);
        BlockOrientation newOrientation = BlockOrientation.get(this.getBlockState());
        if (previousOrientation != newOrientation) {
            this.onOrientationChanged(newOrientation);
        }
    }

    @Override
    public void debugExport(JsonWriter writer, HolderLookup.Provider registries, Reference2IntMap<Object> machineIds, Reference2IntMap<IGridNode> nodeIds) throws IOException {
        CompoundTag data = new CompoundTag();
        this.saveAdditional(data, registries);
        RegistryOps ops = registries.createSerializationContext((DynamicOps)JsonOps.INSTANCE);
        JsonStreamUtil.writeProperties(Map.of("blockState", BlockState.CODEC.encodeStart((DynamicOps)ops, (Object)this.getBlockState()).getOrThrow(), "level", this.level.dimension().location().toString(), "pos", this.getBlockPos(), "data", CompoundTag.CODEC.encodeStart((DynamicOps)ops, (Object)data).getOrThrow()), writer);
    }
}

