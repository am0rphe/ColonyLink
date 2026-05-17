/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.component.DataComponentMap
 *  net.minecraft.core.component.DataComponentMap$Builder
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.ComponentContents
 *  net.minecraft.network.chat.contents.PlainTextContents
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.ItemInteractionResult
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.EntityBlock
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityTicker
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.storage.loot.LootParams
 *  net.minecraft.world.level.storage.loot.LootParams$Builder
 *  net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
 *  net.minecraft.world.level.storage.loot.parameters.LootContextParams
 *  net.minecraft.world.phys.BlockHitResult
 *  org.jetbrains.annotations.Nullable
 */
package appeng.block;

import appeng.api.ids.AEComponents;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.block.AEBaseBlock;
import appeng.block.IOwnerAwareBlockEntity;
import appeng.block.networking.CableBusBlock;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.AEBaseInvBlockEntity;
import appeng.items.tools.MemoryCardItem;
import appeng.util.InteractionUtil;
import appeng.util.Platform;
import appeng.util.SettingsFrom;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public abstract class AEBaseEntityBlock<T extends AEBaseBlockEntity>
extends AEBaseBlock
implements EntityBlock {
    private Class<T> blockEntityClass;
    private BlockEntityType<T> blockEntityType;
    @Nullable
    private BlockEntityTicker<T> serverTicker;
    @Nullable
    private BlockEntityTicker<T> clientTicker;

    public AEBaseEntityBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    public void setBlockEntity(Class<T> blockEntityClass, BlockEntityType<T> blockEntityType, BlockEntityTicker<T> clientTicker, BlockEntityTicker<T> serverTicker) {
        this.blockEntityClass = blockEntityClass;
        this.blockEntityType = blockEntityType;
        this.serverTicker = serverTicker;
        this.clientTicker = clientTicker;
    }

    @Nullable
    public T getBlockEntity(BlockGetter level, int x, int y, int z) {
        return this.getBlockEntity(level, new BlockPos(x, y, z));
    }

    @Nullable
    public T getBlockEntity(BlockGetter level, BlockPos pos) {
        BlockEntity te = level.getBlockEntity(pos);
        if (this.blockEntityClass != null && this.blockEntityClass.isInstance(te)) {
            return (T)((AEBaseBlockEntity)this.blockEntityClass.cast(te));
        }
        return null;
    }

    public BlockEntityType<T> getBlockEntityType() {
        return this.blockEntityType;
    }

    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return this.blockEntityType.create(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> type) {
        return level.isClientSide() ? this.clientTicker : this.serverTicker;
    }

    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        T be;
        if (!level.isClientSide() && !newState.is(state.getBlock()) && (be = this.getBlockEntity((BlockGetter)level, pos)) != null) {
            ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
            ((AEBaseBlockEntity)be).addAdditionalDrops(level, pos, drops);
            Platform.spawnDrops(level, pos, drops);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    public boolean hasAnalogOutputSignal(BlockState state) {
        return AEBaseInvBlockEntity.class.isAssignableFrom(this.blockEntityClass);
    }

    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        AEBaseInvBlockEntity invBlockEntity;
        T te = this.getBlockEntity((BlockGetter)level, pos);
        if (te instanceof AEBaseInvBlockEntity && !(invBlockEntity = (AEBaseInvBlockEntity)te).getInternalInventory().isEmpty()) {
            return invBlockEntity.getInternalInventory().getRedstoneSignal();
        }
        return 0;
    }

    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int eventID, int eventParam) {
        super.triggerEvent(state, level, pos, eventID, eventParam);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity != null ? blockEntity.triggerEvent(eventID, eventParam) : false;
    }

    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack is) {
        Component hoverName;
        ComponentContents componentContents;
        Player player;
        T blockEntity = this.getBlockEntity((BlockGetter)level, pos);
        if (blockEntity == null) {
            return;
        }
        if (blockEntity instanceof IOwnerAwareBlockEntity) {
            IOwnerAwareBlockEntity ownerAware = (IOwnerAwareBlockEntity)blockEntity;
            if (placer instanceof Player) {
                player = (Player)placer;
                ownerAware.setOwner(player);
            }
        }
        if ((componentContents = (hoverName = is.getHoverName()).getContents()) instanceof PlainTextContents) {
            PlainTextContents text = (PlainTextContents)componentContents;
            ((AEBaseBlockEntity)blockEntity).setName(text.text());
        }
        player = null;
        if (placer instanceof Player) {
            player = (Player)placer;
        }
        ((AEBaseBlockEntity)blockEntity).importSettings(SettingsFrom.DISMANTLE_ITEM, is.getComponents(), player);
    }

    protected ItemInteractionResult useItemOn(ItemStack heldItem, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        Item item = heldItem.getItem();
        if (item instanceof IMemoryCard) {
            IMemoryCard memoryCard = (IMemoryCard)item;
            if (!(this instanceof CableBusBlock)) {
                T blockEntity = this.getBlockEntity((BlockGetter)level, pos);
                if (blockEntity == null) {
                    return ItemInteractionResult.FAIL;
                }
                if (InteractionUtil.isInAlternateUseMode(player)) {
                    DataComponentMap.Builder builder = DataComponentMap.builder();
                    ((AEBaseBlockEntity)blockEntity).exportSettings(SettingsFrom.MEMORY_CARD, builder, player);
                    DataComponentMap settings = builder.build();
                    if (!settings.isEmpty()) {
                        MemoryCardItem.clearCard(heldItem);
                        heldItem.applyComponents(settings);
                        memoryCard.notifyUser(player, MemoryCardMessages.SETTINGS_SAVED);
                    }
                } else {
                    Component savedName = (Component)heldItem.get(AEComponents.EXPORTED_SETTINGS_SOURCE);
                    if (this.getName().equals((Object)savedName)) {
                        ((AEBaseBlockEntity)blockEntity).importSettings(SettingsFrom.MEMORY_CARD, heldItem.getComponents(), player);
                        memoryCard.notifyUser(player, MemoryCardMessages.SETTINGS_LOADED);
                    } else {
                        MemoryCardItem.importGenericSettingsAndNotify(blockEntity, heldItem.getComponents(), player);
                    }
                }
                return ItemInteractionResult.sidedSuccess((boolean)level.isClientSide());
            }
        }
        return super.useItemOn(heldItem, state, level, pos, player, hand, hit);
    }

    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    public final BlockState getBlockEntityBlockState(BlockState current, BlockEntity te) {
        if (current.getBlock() != this || !this.blockEntityClass.isInstance(te)) {
            return current;
        }
        return this.updateBlockStateFromBlockEntity(current, (AEBaseBlockEntity)this.blockEntityClass.cast(te));
    }

    protected BlockState updateBlockStateFromBlockEntity(BlockState currentState, T be) {
        return currentState;
    }

    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List drops = super.getDrops(state, builder);
        for (ItemStack drop : drops) {
            BlockItem blockItem;
            Item item = drop.getItem();
            if (!(item instanceof BlockItem) || (blockItem = (BlockItem)item).getBlock() != this) continue;
            LootParams lootContext = builder.withParameter(LootContextParams.BLOCK_STATE, (Object)state).create(LootContextParamSets.BLOCK);
            BlockEntity be = (BlockEntity)lootContext.getParamOrNull(LootContextParams.BLOCK_ENTITY);
            if (!(be instanceof AEBaseBlockEntity)) break;
            AEBaseBlockEntity aeBaseBlockEntity = (AEBaseBlockEntity)be;
            Entity looter = (Entity)lootContext.getParamOrNull(LootContextParams.THIS_ENTITY);
            Player player = null;
            if (looter instanceof Player) {
                player = (Player)looter;
            }
            DataComponentMap settings = aeBaseBlockEntity.exportSettings(SettingsFrom.DISMANTLE_ITEM, player);
            drop.applyComponents(settings);
            break;
        }
        return drops;
    }
}

