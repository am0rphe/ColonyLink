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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
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

    /**
     * getTicker : utilisé pour le premier tick de setup après chargement.
     * Le BE notifie ses voisins après que le niveau soit complètement chargé.
     */
    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> serverType)
    {
        if (level.isClientSide()) return null;
        return ColonyLinkRedirectorBlockEntity.createTicker(level, serverType);
    }

    /**
     * neighborChanged : appelé par Minecraft quand un bloc adjacent change.
     *
     * Du côté AE2, c'est le CÂBLE qui initie la connexion :
     * quand un câble est posé, il appelle neighborChanged sur ses voisins,
     * puis appelle getGridNode(direction) sur les IInWorldGridNodeHost adjacents.
     * Notre BE implémente IInWorldGridNodeHost et retourne son nœud → AE2 crée
     * automatiquement la connexion.
     *
     * Ici on met juste à jour l'état du redirector en réponse au changement de voisin.
     */
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos,
                                Block neighborBlock, BlockPos neighborPos, boolean movedByPiston)
    {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (level.isClientSide()) return;

        var be = level.getBlockEntity(pos);
        if (be instanceof ColonyLinkRedirectorBlockEntity redirector)
            redirector.updateState();
    }

    /**
     * onPlace : quand le redirector est posé, notifie les blocs voisins.
     * Cela permet aux câbles déjà présents de détecter le nouveau nœud
     * et de former une connexion en appelant getGridNode() sur ce BE.
     */
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos,
                        BlockState oldState, boolean movedByPiston)
    {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level.isClientSide()) return;
        level.updateNeighborsAt(pos, this);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
                         BlockState newState, boolean movedByPiston)
    {
        if (!state.hasBlockEntity() || state.getBlock() == newState.getBlock())
        {
            super.onRemove(state, level, pos, newState, movedByPiston);
            return;
        }
        if (!level.isClientSide())
            level.updateNeighborsAt(pos, this);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit)
    {
        if (level.isClientSide()) return InteractionResult.PASS;

        var be = level.getBlockEntity(pos);
        if (!(be instanceof ColonyLinkRedirectorBlockEntity redirector)) return InteractionResult.PASS;

        ItemStack heldItem = player.getMainHandItem();

        // Wrench AE2 → show status ou casse
        if (heldItem.getItem() instanceof NetworkToolItem)
        {
            if (player.isShiftKeyDown())
            {
                Block.dropResources(state, level, pos, be, player, heldItem);
                level.removeBlock(pos, false);
                player.sendSystemMessage(Component.literal("§aColony Link Redirector removed!"));
            }
            else
            {
                showStatus(player, redirector);
            }
            return InteractionResult.SUCCESS;
        }

        // Wand + sneak → délégué à ColonyLinkWand.useOn()
        if (heldItem.getItem() instanceof ColonyLinkWand)
            return InteractionResult.PASS;

        // Main vide → ouvre le GUI buffer
        if (heldItem.isEmpty() && !player.isShiftKeyDown())
        {
            player.openMenu(redirector, buf -> buf.writeBlockPos(pos));
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    private void showStatus(Player player, ColonyLinkRedirectorBlockEntity redirector)
    {
        redirector.updateState();

        boolean ae2Active = redirector.isAe2Active();
        player.sendSystemMessage(Component.literal(
                "§7[Redirector] AE2: " + (ae2Active ? "§aLinked" : "§cUnlinked")));

        switch (redirector.getState())
        {
            case NOT_LINKED -> player.sendSystemMessage(
                    Component.literal("§e[Redirector] No builder linked. Sneak + right-click with the Wand."));
            case STANDBY -> player.sendSystemMessage(
                    Component.literal("§6[Redirector] STANDBY - Target inventory is full!"));
            case LINKED ->
            {
                player.sendSystemMessage(Component.literal("§a[Redirector] LINKED and operational!"));
                if (redirector.getTargetInventoryPos() != null)
                    player.sendSystemMessage(Component.literal(
                            "§7Target: " + redirector.getTargetInventoryPos().toShortString()));
                if (redirector.getLinkedBuilderPos() != null)
                    player.sendSystemMessage(Component.literal(
                            "§7Builder: " + redirector.getLinkedBuilderPos().toShortString()));
            }
            default -> {}
        }
    }
}