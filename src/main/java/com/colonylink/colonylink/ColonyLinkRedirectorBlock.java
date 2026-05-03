package com.colonylink.colonylink;

import appeng.items.tools.NetworkToolItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.jetbrains.annotations.Nullable;

public class ColonyLinkRedirectorBlock extends Block implements EntityBlock
{
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public ColonyLinkRedirectorBlock()
    {
        super(BlockBehaviour.Properties.of()
                .strength(2.0f)
                .destroyTime(2.0f));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new ColonyLinkRedirectorBlockEntity(pos, state);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit)
    {
        if (level.isClientSide()) return InteractionResult.PASS;

        var be = level.getBlockEntity(pos);
        if (!(be instanceof ColonyLinkRedirectorBlockEntity redirector)) return InteractionResult.PASS;

        ItemStack heldItem = player.getMainHandItem();

        if (heldItem.getItem() instanceof NetworkToolItem)
        {
            if (player.isShiftKeyDown())
            {
                level.removeBlock(pos, false);
                player.sendSystemMessage(Component.literal("§aColony Link Redirector removed!"));
            }
            else
            {
                showStatus(player, redirector);
            }
            return InteractionResult.SUCCESS;
        }

        if (player.isShiftKeyDown())
        {
            BlockPos targetPos = hit.getBlockPos().relative(hit.getDirection());
            var handler = level.getCapability(Capabilities.ItemHandler.BLOCK, targetPos, null);
            if (handler != null)
            {
                redirector.setTargetInventoryPos(targetPos);
                redirector.updateState();
                player.sendSystemMessage(Component.literal("§aInventory linked at " + targetPos.toShortString()));
            }
            else
            {
                player.sendSystemMessage(Component.literal("§cNo inventory found here!"));
            }
            return InteractionResult.SUCCESS;
        }

        showStatus(player, redirector);
        return InteractionResult.SUCCESS;
    }

    private void showStatus(Player player, ColonyLinkRedirectorBlockEntity redirector)
    {
        redirector.updateState();
        switch (redirector.getState())
        {
            case NO_CONTROLLER -> player.sendSystemMessage(Component.literal("§c[ColonyLink Redirector] Not adjacent to a ME Controller!"));
            case NOT_LINKED -> player.sendSystemMessage(Component.literal("§e[ColonyLink Redirector] No inventory linked. Sneak + right-click an inventory to link."));
            case STANDBY -> player.sendSystemMessage(Component.literal("§6[ColonyLink Redirector] STANDBY - Target inventory is full!"));
            case LINKED ->
            {
                player.sendSystemMessage(Component.literal("§a[ColonyLink Redirector] LINKED and operational!"));
                if (redirector.getTargetInventoryPos() != null)
                    player.sendSystemMessage(Component.literal("§7Target: " + redirector.getTargetInventoryPos().toShortString()));
                if (redirector.getLinkedBuilderPos() != null)
                    player.sendSystemMessage(Component.literal("§7Builder: " + redirector.getLinkedBuilderPos().toShortString()));
            }
        }
    }
}