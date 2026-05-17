/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Holder
 *  net.minecraft.core.Registry
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.tags.BlockTags
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.enchantment.EnchantmentHelper
 *  net.minecraft.world.item.enchantment.Enchantments
 *  net.minecraft.world.item.enchantment.ItemEnchantments
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  org.jetbrains.annotations.Nullable
 */
package appeng.parts.automation;

import appeng.api.behaviors.PickupSink;
import appeng.api.behaviors.PickupStrategy;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.stacks.AEItemKey;
import appeng.core.AppEng;
import appeng.core.network.clientbound.BlockTransitionEffectPacket;
import appeng.core.network.clientbound.ItemTransitionEffectPacket;
import appeng.util.Platform;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class ItemPickupStrategy
implements PickupStrategy {
    public static final ResourceLocation TAG_BLACKLIST = AppEng.makeId("blacklisted/annihilation_plane");
    private static final TagKey<Block> BLOCK_BLACKLIST = TagKey.create((ResourceKey)Registries.BLOCK, (ResourceLocation)TAG_BLACKLIST);
    private static final TagKey<Item> ITEM_BLACKLIST = TagKey.create((ResourceKey)Registries.ITEM, (ResourceLocation)TAG_BLACKLIST);
    private final ServerLevel level;
    private final BlockPos pos;
    private final Direction side;
    private final ItemEnchantments enchantments;
    @Nullable
    private final UUID ownerUuid;
    private boolean isAccepting = true;

    public ItemPickupStrategy(ServerLevel level, BlockPos pos, Direction side, BlockEntity host, ItemEnchantments enchantments, @Nullable UUID owningPlayerId) {
        this.level = level;
        this.pos = pos;
        this.side = side;
        this.enchantments = enchantments;
        this.ownerUuid = owningPlayerId;
    }

    @Override
    public void reset() {
        this.isAccepting = true;
    }

    @Override
    public boolean canPickUpEntity(Entity entity) {
        return entity instanceof ItemEntity;
    }

    @Override
    public boolean pickUpEntity(IEnergySource energySource, PickupSink sink, Entity entity) {
        if (!this.isAccepting || !(entity instanceof ItemEntity)) {
            return false;
        }
        ItemEntity itemEntity = (ItemEntity)entity;
        if (ItemPickupStrategy.isItemBlacklisted(itemEntity.getItem().getItem())) {
            return false;
        }
        boolean changed = this.storeEntityItem(sink, itemEntity);
        if (changed) {
            AppEng.instance().sendToAllNearExcept(null, this.pos.getX(), this.pos.getY(), this.pos.getZ(), 64.0, (Level)this.level, new ItemTransitionEffectPacket(entity.getX(), entity.getY(), entity.getZ(), this.side));
        }
        return true;
    }

    @Override
    public PickupStrategy.Result tryPickup(IEnergySource energySource, PickupSink sink) {
        BlockState blockState;
        if (this.isAccepting && this.canHandleBlock(this.level, this.pos, blockState = this.level.getBlockState(this.pos))) {
            List<ItemStack> items = this.obtainBlockDrops(this.level, this.pos);
            float requiredPower = this.calculateEnergyUsage(this.level, this.pos, items);
            boolean hasPower = energySource.extractAEPower(requiredPower, Actionable.SIMULATE, PowerMultiplier.CONFIG) > (double)requiredPower - 0.1;
            boolean canStore = this.canStoreItemStacks(sink, items);
            if (hasPower && canStore) {
                this.completePickup(energySource, sink, items, requiredPower, blockState);
                return PickupStrategy.Result.PICKED_UP;
            }
            return PickupStrategy.Result.CANT_STORE;
        }
        return PickupStrategy.Result.CANT_PICKUP;
    }

    private void completePickup(IEnergySource energySource, PickupSink sink, List<ItemStack> items, float requiredPower, BlockState blockState) {
        if (!this.breakBlockAndStoreExtraItems(sink, this.level, this.pos)) {
            return;
        }
        for (ItemStack item : items) {
            int inserted = this.storeItemStack(sink, item);
            if (inserted >= item.getCount()) continue;
            item.shrink(inserted);
            Platform.spawnDrops((Level)this.level, this.pos, Collections.singletonList(item));
        }
        energySource.extractAEPower(requiredPower, Actionable.MODULATE, PowerMultiplier.CONFIG);
        AppEng.instance().sendToAllNearExcept(null, this.pos.getX(), this.pos.getY(), this.pos.getZ(), 64.0, (Level)this.level, new BlockTransitionEffectPacket(this.pos, blockState, this.side, BlockTransitionEffectPacket.SoundMode.NONE));
    }

    private boolean storeEntityItem(PickupSink sink, ItemEntity entityItem) {
        if (entityItem.isAlive()) {
            int inserted = this.storeItemStack(sink, entityItem.getItem());
            return this.handleOverflow(entityItem, inserted);
        }
        return false;
    }

    private int storeItemStack(PickupSink sink, ItemStack item) {
        int amount;
        if (item.isEmpty()) {
            return 0;
        }
        AEItemKey what = AEItemKey.of(item);
        int inserted = (int)sink.insert(what, amount = item.getCount(), Actionable.MODULATE);
        this.isAccepting = inserted >= amount;
        return inserted;
    }

    private boolean handleOverflow(ItemEntity entityItem, int inserted) {
        int entityItemCount = entityItem.getItem().getCount();
        if (inserted >= entityItemCount) {
            entityItem.discard();
            return true;
        }
        int newStackSize = entityItemCount - inserted;
        boolean changed = entityItemCount != newStackSize;
        entityItem.getItem().setCount(newStackSize);
        return changed;
    }

    private boolean canHandleBlock(ServerLevel level, BlockPos pos, BlockState state) {
        if (state.isAir()) {
            return false;
        }
        if (ItemPickupStrategy.isBlockBlacklisted(state.getBlock())) {
            return false;
        }
        float hardness = state.getDestroySpeed((BlockGetter)level, pos);
        boolean ignoreAirAndFluids = state.isAir() || state.liquid();
        return !ignoreAirAndFluids && hardness >= 0.0f && level.isLoaded(pos) && level.mayInteract(Platform.getFakePlayer(level, this.ownerUuid), pos);
    }

    protected List<ItemStack> obtainBlockDrops(ServerLevel level, BlockPos pos) {
        Player fakePlayer = Platform.getFakePlayer(level, this.ownerUuid);
        BlockState state = level.getBlockState(pos);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        HarvestTool harvestTool = this.createHarvestTool(state);
        ItemStack harvestToolItem = harvestTool.item();
        if (!state.requiresCorrectToolForDrops() && harvestTool.fallback()) {
            harvestToolItem = ItemStack.EMPTY;
        }
        List drops = Block.getDrops((BlockState)state, (ServerLevel)level, (BlockPos)pos, (BlockEntity)blockEntity, (Entity)fakePlayer, (ItemStack)harvestToolItem);
        return drops.stream().filter(stack -> !stack.isEmpty()).toList();
    }

    protected float calculateEnergyUsage(ServerLevel level, BlockPos pos, List<ItemStack> items) {
        boolean useEnergy = true;
        BlockState state = level.getBlockState(pos);
        float hardness = state.getDestroySpeed((BlockGetter)level, pos);
        float requiredEnergy = 1.0f + hardness;
        for (ItemStack is : items) {
            requiredEnergy += (float)is.getCount();
        }
        if (this.enchantments != null) {
            int levelSum;
            int unbreakingLevel;
            Registry enchantmentRegistry = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
            float efficiencyFactor = 1.0f;
            int efficiencyLevel = this.enchantments.getLevel((Holder)enchantmentRegistry.getHolderOrThrow(Enchantments.EFFICIENCY));
            if (efficiencyLevel > 0) {
                efficiencyFactor = (float)((double)efficiencyFactor * Math.pow(0.85, efficiencyLevel));
            }
            if ((unbreakingLevel = this.enchantments.getLevel((Holder)enchantmentRegistry.getHolderOrThrow(Enchantments.UNBREAKING))) > 0) {
                int randomNumber = level.getRandom().nextInt(unbreakingLevel + 1);
                useEnergy = randomNumber == 0;
            }
            requiredEnergy *= (float)((levelSum = this.enchantments.entrySet().stream().map(Map.Entry::getValue).reduce(0, Integer::sum) - efficiencyLevel - unbreakingLevel) > 0 ? 8 * levelSum : 1) * efficiencyFactor;
        }
        return useEnergy ? requiredEnergy : 0.0f;
    }

    private boolean canStoreItemStacks(PickupSink sink, List<ItemStack> itemStacks) {
        boolean canStore = itemStacks.isEmpty();
        for (ItemStack itemStack : itemStacks) {
            AEItemKey itemToTest = AEItemKey.of(itemStack);
            long inserted = sink.insert(itemToTest, itemStack.getCount(), Actionable.SIMULATE);
            if (inserted != (long)itemStack.getCount()) continue;
            canStore = true;
        }
        this.isAccepting = canStore;
        return canStore;
    }

    private boolean breakBlockAndStoreExtraItems(PickupSink sink, ServerLevel level, BlockPos pos) {
        if (!level.destroyBlock(pos, false)) {
            return false;
        }
        AABB box = new AABB(pos).inflate(0.2);
        for (ItemEntity itemEntity : level.getEntitiesOfClass(ItemEntity.class, box)) {
            this.storeEntityItem(sink, itemEntity);
        }
        return true;
    }

    private HarvestTool createHarvestTool(BlockState state) {
        ItemStack tool;
        boolean fallback = false;
        if (state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            tool = new ItemStack((ItemLike)Items.DIAMOND_PICKAXE, 1);
        } else if (state.is(BlockTags.MINEABLE_WITH_AXE)) {
            tool = new ItemStack((ItemLike)Items.DIAMOND_AXE, 1);
        } else if (state.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
            tool = new ItemStack((ItemLike)Items.DIAMOND_SHOVEL, 1);
        } else if (state.is(BlockTags.MINEABLE_WITH_HOE)) {
            tool = new ItemStack((ItemLike)Items.DIAMOND_HOE, 1);
        } else {
            tool = new ItemStack((ItemLike)Items.DIAMOND_PICKAXE, 1);
            fallback = true;
        }
        if (!this.enchantments.isEmpty()) {
            EnchantmentHelper.setEnchantments((ItemStack)tool, (ItemEnchantments)this.enchantments);
            fallback = false;
        }
        return new HarvestTool(tool, fallback);
    }

    public static boolean isBlockBlacklisted(Block b) {
        return b.builtInRegistryHolder().is(BLOCK_BLACKLIST);
    }

    public static boolean isItemBlacklisted(Item i) {
        return i.builtInRegistryHolder().is(ITEM_BLACKLIST);
    }

    record HarvestTool(ItemStack item, boolean fallback) {
    }
}

