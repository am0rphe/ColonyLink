/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.stats.Stats
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.ItemInteractionResult
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.AbstractArrow
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Explosion
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 *  org.jetbrains.annotations.Nullable
 */
package appeng.block.misc;

import appeng.block.AEBaseBlock;
import appeng.entity.TinyTNTPrimedEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class TinyTNTBlock
extends AEBaseBlock {
    private static final VoxelShape SHAPE = Shapes.create((AABB)new AABB(0.25, 0.0, 0.25, 0.75, 0.5, 0.75));

    public TinyTNTBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 2;
    }

    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    protected ItemInteractionResult useItemOn(ItemStack heldItem, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (heldItem.is(Items.FLINT_AND_STEEL) || heldItem.is(Items.FIRE_CHARGE)) {
            this.onCaughtFire(state, level, pos, hit.getDirection(), (LivingEntity)player);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
            Item item = heldItem.getItem();
            if (!player.isCreative()) {
                if (heldItem.is(Items.FLINT_AND_STEEL)) {
                    heldItem.hurtAndBreak(1, (LivingEntity)player, LivingEntity.getSlotForHand((InteractionHand)hand));
                } else {
                    heldItem.shrink(1);
                }
            }
            player.awardStat(Stats.ITEM_USED.get((Object)item));
            return ItemInteractionResult.sidedSuccess((boolean)level.isClientSide);
        }
        return super.useItemOn(heldItem, state, level, pos, player, hand, hit);
    }

    public void onCaughtFire(BlockState state, Level level, BlockPos pos, @Nullable Direction direction, @Nullable LivingEntity igniter) {
        this.startFuse(level, pos, igniter);
    }

    public void startFuse(Level level, BlockPos pos, LivingEntity igniter) {
        if (!level.isClientSide) {
            TinyTNTPrimedEntity primedTinyTNTEntity = new TinyTNTPrimedEntity(level, (float)pos.getX() + 0.5f, pos.getY(), (float)pos.getZ() + 0.5f, igniter);
            level.addFreshEntity((Entity)primedTinyTNTEntity);
            level.playSound(null, primedTinyTNTEntity.getX(), primedTinyTNTEntity.getY(), primedTinyTNTEntity.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
    }

    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (level.getBestNeighborSignal(pos) > 0) {
            this.startFuse(level, pos, null);
            level.removeBlock(pos, false);
        }
    }

    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (level.getBestNeighborSignal(pos) > 0) {
            this.startFuse(level, pos, null);
            level.removeBlock(pos, false);
        }
    }

    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        AbstractArrow arrow;
        if (!level.isClientSide && entity instanceof AbstractArrow && (arrow = (AbstractArrow)entity).isOnFire()) {
            LivingEntity igniter = null;
            Entity shooter = arrow.getOwner();
            if (shooter instanceof LivingEntity) {
                igniter = (LivingEntity)shooter;
            }
            this.startFuse(level, pos, igniter);
            level.removeBlock(pos, false);
        }
    }

    public boolean dropFromExplosion(Explosion exp) {
        return false;
    }

    public void wasExploded(Level level, BlockPos pos, Explosion exp) {
        super.wasExploded(level, pos, exp);
        if (!level.isClientSide) {
            TinyTNTPrimedEntity primedTinyTNTEntity = new TinyTNTPrimedEntity(level, (float)pos.getX() + 0.5f, pos.getY(), (float)pos.getZ() + 0.5f, exp.getIndirectSourceEntity());
            primedTinyTNTEntity.setFuse(level.random.nextInt(primedTinyTNTEntity.getFuse() / 4) + primedTinyTNTEntity.getFuse() / 8);
            level.addFreshEntity((Entity)primedTinyTNTEntity);
        }
    }
}

