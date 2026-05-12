package com.colonylink.colonylink;

import com.refinedmods.refinedstorage.common.support.network.NetworkNodeBlockEntityTicker;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Bloc Redirector RS2 — même logique que ColonyLinkRedirectorBlock (AE2).
 * Connecté au réseau RS2 via AbstractBaseNetworkNodeContainerBlockEntity.
 *
 * ACTIVE est requis par NetworkNodeBlockEntityTicker pour gérer l'état
 * actif/inactif du nœud RS2 (updateActiveness / activenessChanged).
 * Sans ce ticker, initialize() n'est jamais appelé → le nœud ne rejoint
 * jamais le réseau RS2 et les câbles ne se connectent pas.
 */
public class ColonyLinkRedirectorBlockRS extends Block implements EntityBlock
{
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    /**
     * Propriété blockstate requise par AbstractBaseNetworkNodeContainerBlockEntity.
     * RS2 l'utilise pour indiquer visuellement si le bloc est connecté au réseau.
     * Passée au NetworkNodeBlockEntityTicker pour déclencher updateActiveness().
     */
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    // Ticker RS2 standard — appelle initialize() au premier tick côté serveur,
    // puis updateActiveness() à chaque tick pour maintenir l'état ACTIVE en sync.
    private static final NetworkNodeBlockEntityTicker<
            com.refinedmods.refinedstorage.api.network.impl.node.SimpleNetworkNode,
            ColonyLinkRedirectorBlockEntityRS> TICKER =
            new NetworkNodeBlockEntityTicker<>(
                    () -> ColonyLinkRegistry.REDIRECTOR_BLOCK_ENTITY_RS.get(),
                    ACTIVE);

    public ColonyLinkRedirectorBlockRS()
    {
        super(BlockBehaviour.Properties.of()
                .strength(0.5f)
                .destroyTime(0.5f));
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING, ACTIVE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(ACTIVE, false);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new ColonyLinkRedirectorBlockEntityRS(pos, state);
    }

    /**
     * getTicker — retourne le ticker RS2 standard côté serveur uniquement.
     *
     * Ce ticker est la pièce manquante : il appelle initialize(ServerLevel)
     * sur le BE au premier tick, ce qui déclenche la connexion au réseau RS2.
     * Sans lui, le nœud reste isolé et les câbles adjacents ne le voient pas.
     */
    @Override
    @SuppressWarnings("unchecked")
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> serverType)
    {
        if (level.isClientSide()) return null;
        if (serverType != ColonyLinkRegistry.REDIRECTOR_BLOCK_ENTITY_RS.get()) return null;
        return (BlockEntityTicker<T>) TICKER;
    }

    /**
     * onPlace : notifie les blocs voisins à la pose.
     * Les câbles RS2 adjacents détectent le nouveau nœud via neighborChanged
     * → ils interrogent la capability NetworkNodeContainerProvider sur le BE
     * → la connexion réseau est établie.
     */
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos,
                        BlockState oldState, boolean movedByPiston)
    {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide())
            level.updateNeighborsAt(pos, this);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos,
                                Block neighborBlock, BlockPos neighborPos, boolean movedByPiston)
    {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (level.isClientSide()) return;
        var be = level.getBlockEntity(pos);
        if (be instanceof ColonyLinkRedirectorBlockEntityRS redirector)
            redirector.updateState();
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
        // Extraire les stacks AVANT super.onRemove() — le BE devient null après.
        java.util.List<ItemStack> toDrop = new java.util.ArrayList<>();
        if (!level.isClientSide())
        {
            var be = level.getBlockEntity(pos);
            if (be instanceof ColonyLinkRedirectorBlockEntityRS redirector)
            {
                for (int slot = 0; slot < redirector.buffer.getSlots(); slot++)
                {
                    ItemStack stack = redirector.buffer.getStackInSlot(slot);
                    if (!stack.isEmpty()) toDrop.add(stack.copy());
                }
                ItemStack card = redirector.warehouseCardSlot.getStackInSlot(0);
                if (!card.isEmpty()) toDrop.add(card.copy());
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (!level.isClientSide())
        {
            for (ItemStack stack : toDrop)
                Block.popResource(level, pos, stack);
            level.updateNeighborsAt(pos, this);
        }
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                            Player player, BlockHitResult hit)
    {
        ItemStack held = player.getMainHandItem();

        if (held.getItem() instanceof ColonyLinkWandRS)
            return InteractionResult.PASS;

        var wrenchTag = net.minecraft.tags.TagKey.create(
                net.minecraft.core.registries.Registries.ITEM,
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("c", "tools/wrench"));
        if (held.is(wrenchTag))
        {
            if (!level.isClientSide() && player.isShiftKeyDown())
            {
                Block.dropResources(state, level, pos, level.getBlockEntity(pos), player, held);
                level.removeBlock(pos, false);
                player.sendSystemMessage(Component.literal("§a[ColonyLink RS] Redirector removed!"));
            }
            else if (!level.isClientSide())
            {
                var be = level.getBlockEntity(pos);
                if (be instanceof ColonyLinkRedirectorBlockEntityRS redirector)
                {
                    redirector.updateState();
                    player.sendSystemMessage(Component.literal(
                            "§7[Redirector RS] RS2: " + (redirector.isRs2Active() ? "§aLinked" : "§cUnlinked")));
                    switch (redirector.getState())
                    {
                        case NOT_LINKED -> player.sendSystemMessage(
                                Component.literal("§e[Redirector RS] No builder linked."));
                        case STANDBY -> player.sendSystemMessage(
                                Component.literal("§6[Redirector RS] STANDBY - Target inventory is full!"));
                        case LINKED -> player.sendSystemMessage(
                                Component.literal("§a[Redirector RS] LINKED and operational!"));
                        default -> {}
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }

        if (level.isClientSide()) return InteractionResult.PASS;

        var be = level.getBlockEntity(pos);
        if (!(be instanceof ColonyLinkRedirectorBlockEntityRS redirector)) return InteractionResult.PASS;

        if (!player.isShiftKeyDown())
        {
            player.openMenu(redirector, buf -> buf.writeBlockPos(pos));
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}