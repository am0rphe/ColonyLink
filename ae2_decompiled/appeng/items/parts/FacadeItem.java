/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Holder
 *  net.minecraft.network.chat.Component
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.CreativeModeTab$ItemDisplayParameters
 *  net.minecraft.world.item.CreativeModeTab$Output
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.EmptyBlockGetter
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.RenderShape
 *  net.minecraft.world.level.block.SoundType
 *  net.minecraft.world.level.block.state.BlockState
 */
package appeng.items.parts;

import appeng.api.ids.AEComponents;
import appeng.api.ids.AETags;
import appeng.api.implementations.items.IFacadeItem;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.PartHelper;
import appeng.facade.FacadePart;
import appeng.items.AEBaseItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public class FacadeItem
extends AEBaseItem
implements IFacadeItem {
    public FacadeItem(Item.Properties properties) {
        super(properties);
    }

    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if (stack.getItem() != this) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        FacadePart facade = this.createPartFromItemStack(stack, context.getClickedFace());
        if (facade == null || !FacadeItem.placeFacade(facade, level, pos)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide && player != null && !player.isCreative()) {
            stack.grow(-1);
            if (stack.isEmpty()) {
                player.setItemInHand(context.getHand(), ItemStack.EMPTY);
            }
        }
        return InteractionResult.sidedSuccess((boolean)level.isClientSide);
    }

    public static boolean canPlaceFacade(IPartHost host, IFacadePart facade) {
        if (host.getPart(null) == null) {
            return false;
        }
        return host.getFacadeContainer().canAddFacade(facade);
    }

    private static boolean placeFacade(FacadePart facade, Level level, BlockPos blockPos) {
        IPartHost host = PartHelper.getPartHost(level, blockPos);
        if (host == null) {
            return false;
        }
        if (!FacadeItem.canPlaceFacade(host, facade)) {
            return false;
        }
        if (!host.getFacadeContainer().addFacade(facade)) {
            return false;
        }
        BlockState blockState = facade.getBlockState();
        SoundType soundType = blockState.getSoundType();
        level.playSound(null, blockPos, soundType.getPlaceSound(), SoundSource.BLOCKS, (soundType.getVolume() + 1.0f) / 2.0f, soundType.getPitch() * 0.8f);
        host.markForSave();
        host.markForUpdate();
        return true;
    }

    public static IFacadePart createFacade(ItemStack held, Direction side) {
        if (held.getItem() instanceof IFacadeItem) {
            return ((IFacadeItem)held.getItem()).createPartFromItemStack(held, side);
        }
        return null;
    }

    public Component getName(ItemStack is) {
        try {
            ItemStack in = this.getTextureItem(is);
            if (!in.isEmpty()) {
                return super.getName(is).copy().append(" - ").append(in.getHoverName());
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return super.getName(is);
    }

    @Override
    public void addToMainCreativeTab(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
    }

    public ItemStack createFacadeForItem(ItemStack itemStack, boolean returnItem) {
        boolean isBlockAllowed;
        Item item;
        if (itemStack.isEmpty() || !itemStack.getComponentsPatch().isEmpty() || !((item = itemStack.getItem()) instanceof BlockItem)) {
            return ItemStack.EMPTY;
        }
        BlockItem blockItem = (BlockItem)item;
        Block block = blockItem.getBlock();
        if (block == Blocks.AIR) {
            return ItemStack.EMPTY;
        }
        BlockState blockState = block.defaultBlockState();
        boolean isWhiteListed = block.builtInRegistryHolder().is(AETags.FACADE_BLOCK_WHITELIST);
        boolean isModel = blockState.getRenderShape() == RenderShape.MODEL;
        BlockState defaultState = block.defaultBlockState();
        boolean isBlockEntity = defaultState.hasBlockEntity();
        boolean isFullCube = defaultState.isCollisionShapeFullBlock((BlockGetter)EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
        boolean isBlockEntityAllowed = !isBlockEntity || isWhiteListed;
        boolean bl = isBlockAllowed = isFullCube || isWhiteListed;
        if (isModel && isBlockEntityAllowed && isBlockAllowed) {
            if (returnItem) {
                return itemStack;
            }
            return this.createFacadeForItemUnchecked(itemStack);
        }
        return ItemStack.EMPTY;
    }

    public ItemStack createFacadeForItemUnchecked(ItemStack itemStack) {
        ItemStack is = new ItemStack((ItemLike)this);
        is.set(AEComponents.FACADE_ITEM, (Object)itemStack.getItemHolder());
        return is;
    }

    @Override
    public FacadePart createPartFromItemStack(ItemStack is, Direction side) {
        ItemStack in = this.getTextureItem(is);
        if (!in.isEmpty()) {
            return new FacadePart(this.getTextureBlockState(is), side);
        }
        return null;
    }

    @Override
    public ItemStack getTextureItem(ItemStack is) {
        Holder baseItem = (Holder)is.get(AEComponents.FACADE_ITEM);
        if (baseItem == null) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(baseItem, 1);
    }

    @Override
    public BlockState getTextureBlockState(ItemStack is) {
        ItemStack baseItemStack = this.getTextureItem(is);
        if (baseItemStack.isEmpty()) {
            return Blocks.GLASS.defaultBlockState();
        }
        Block block = Block.byItem((Item)baseItemStack.getItem());
        if (block == Blocks.AIR) {
            return Blocks.GLASS.defaultBlockState();
        }
        return block.defaultBlockState();
    }
}

