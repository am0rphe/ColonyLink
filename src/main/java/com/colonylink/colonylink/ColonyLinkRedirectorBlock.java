package com.colonylink.colonylink;

import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ColonyLinkRedirectorBlock extends Block implements EntityBlock
{
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public ColonyLinkRedirectorBlock()
    {
        super(BlockBehaviour.Properties.of()
                .strength(0.5f)
                .destroyTime(0.5f));
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
        {
            var be = level.getBlockEntity(pos);
            if (be instanceof ColonyLinkRedirectorBlockEntity redirector)
            {
                // Droppe le contenu du buffer
                for (int slot = 0; slot < redirector.buffer.getSlots(); slot++)
                {
                    net.minecraft.world.item.ItemStack stack = redirector.buffer.getStackInSlot(slot);
                    if (!stack.isEmpty())
                        Block.popResource(level, pos, stack);
                }
                // Droppe la Warehouse Link Card si présente
                net.minecraft.world.item.ItemStack card = redirector.warehouseCardSlot.getStackInSlot(0);
                if (!card.isEmpty())
                    Block.popResource(level, pos, card);
            }
            level.updateNeighborsAt(pos, this);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    /**
     * Drop "self" défini en code, indépendant de toute loot table JSON.
     * C'était la cause du bug : sans loot table (absente/incomplète), le chemin
     * vanilla playerDestroy() → dropResources() → getDrops() ne renvoyait rien,
     * donc le block item ne tombait jamais. On force ici le drop du Redirector.
     * Couvre TOUS les cas de destruction (pioche, explosion, destroyBlock du wrench…).
     * Le contenu du buffer + la Warehouse Link Card restent gérés par onRemove().
     */
    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params)
    {
        return List.of(new ItemStack(ColonyLinkRegistry.REDIRECTOR_BLOCK_ITEM.get()));
    }



    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit)
    {
        ItemStack held = player.getMainHandItem();

        // Wand → PASS côté client et serveur, useOn() de la wand gère déjà tout
        if (held.getItem() instanceof ColonyLinkWand)
            return InteractionResult.PASS;

        // Wrench (c:tools/wrench) → bloquer l'ouverture GUI des deux côtés
        // Le check doit être AVANT isClientSide() pour que le client ne tente
        // pas d'ouvrir le GUI de son côté
        var wrenchTag = net.minecraft.tags.TagKey.create(
                net.minecraft.core.registries.Registries.ITEM,
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("c", "tools/wrench"));
        if (held.is(wrenchTag))
        {
            // Sneak + wrench → retire le Redirector proprement.
            // destroyBlock(pos, true) : true → dropResources → getDrops() droppe le block
            // item, puis onRemove() droppe le contenu du buffer + la Warehouse Link Card.
            // Côté client (isClientSide) on ne fait rien : la suppression du bloc est
            // répliquée par le packet de mise à jour serveur habituel.
            if (!level.isClientSide() && player.isShiftKeyDown())
                level.destroyBlock(pos, true);
            return InteractionResult.SUCCESS;
        }

        if (level.isClientSide()) return InteractionResult.PASS;

        var be = level.getBlockEntity(pos);
        if (!(be instanceof ColonyLinkRedirectorBlockEntity redirector)) return InteractionResult.PASS;

        // Tout autre cas → ouvre le GUI buffer
        if (!player.isShiftKeyDown())
        {
            player.openMenu(redirector, buf -> buf.writeBlockPos(pos));
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }


}