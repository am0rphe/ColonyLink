/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.util.Mth
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.ItemInteractionResult
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package appeng.block.misc;

import appeng.api.inventories.InternalInventory;
import appeng.api.orientation.BlockOrientation;
import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import appeng.api.orientation.RelativeSide;
import appeng.api.util.AEAxisAlignedBB;
import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.misc.ChargerBlockEntity;
import appeng.blockentity.misc.ChargerRecipes;
import appeng.client.render.effects.LightningArcParticleData;
import appeng.core.AEConfig;
import appeng.core.AppEngClient;
import appeng.util.Platform;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class ChargerBlock
extends AEBaseEntityBlock<ChargerBlockEntity> {
    public ChargerBlock() {
        super(ChargerBlock.metalProps().noOcclusion());
    }

    @Override
    public IOrientationStrategy getOrientationStrategy() {
        return OrientationStrategies.full();
    }

    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 2;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack heldItem, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ChargerBlockEntity charger;
        InternalInventory inv;
        ItemStack chargingItem;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ChargerBlockEntity && (chargingItem = (inv = (charger = (ChargerBlockEntity)blockEntity).getInternalInventory()).getStackInSlot(0)).isEmpty() && (ChargerRecipes.findRecipe(level, heldItem) != null || Platform.isChargeable(heldItem))) {
            ItemStack toInsert = heldItem.split(1);
            inv.setItemDirect(0, toInsert);
            return ItemInteractionResult.sidedSuccess((boolean)level.isClientSide);
        }
        return super.useItemOn(heldItem, state, level, pos, player, hand, hit);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        ChargerBlockEntity charger;
        InternalInventory inv;
        ItemStack chargingItem;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ChargerBlockEntity && !(chargingItem = (inv = (charger = (ChargerBlockEntity)blockEntity).getInternalInventory()).getStackInSlot(0)).isEmpty()) {
            inv.setItemDirect(0, ItemStack.EMPTY);
            Platform.spawnDrops(player.level(), charger.getBlockPos().relative(charger.getFront()), List.of(chargingItem));
            return InteractionResult.sidedSuccess((boolean)level.isClientSide);
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @OnlyIn(value=Dist.CLIENT)
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource r) {
        if (!AEConfig.instance().isEnableEffects()) {
            return;
        }
        ChargerBlockEntity blockEntity = (ChargerBlockEntity)this.getBlockEntity((BlockGetter)level, pos);
        if (blockEntity != null && blockEntity.isWorking()) {
            if ((double)r.nextFloat() < 0.5) {
                return;
            }
            BlockOrientation rotation = BlockOrientation.get(blockEntity);
            for (int bolts = 0; bolts < 3; ++bolts) {
                float xOff = Mth.randomBetween((RandomSource)r, (float)-0.15f, (float)0.15f);
                float zOff = Mth.randomBetween((RandomSource)r, (float)-0.15f, (float)0.15f);
                Vector3f center = new Vector3f((float)pos.getX() + 0.5f, (float)pos.getY() + 0.5f, (float)pos.getZ() + 0.5f);
                Vector3f origin = new Vector3f(xOff, -0.3f, zOff);
                origin.rotate((Quaternionfc)rotation.getQuaternion());
                origin.add((Vector3fc)center);
                Vector3f target = new Vector3f(xOff, 0.3f, zOff);
                target.rotate((Quaternionfc)rotation.getQuaternion());
                target.add((Vector3fc)center);
                if (r.nextBoolean()) {
                    Vector3f tmp = target;
                    target = origin;
                    origin = tmp;
                }
                if (!AppEngClient.instance().shouldAddParticles(r)) continue;
                Minecraft.getInstance().particleEngine.createParticle((ParticleOptions)new LightningArcParticleData(target), (double)origin.x(), (double)origin.y(), (double)origin.z(), 0.0, 0.0, 0.0);
            }
        }
    }

    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        BlockOrientation orientation = this.getOrientation(state);
        Direction up = orientation.getSide(RelativeSide.TOP);
        Direction forward = orientation.getSide(RelativeSide.FRONT);
        double twoPixels = 0.125;
        AEAxisAlignedBB bb = new AEAxisAlignedBB(twoPixels, twoPixels, twoPixels, 1.0 - twoPixels, 1.0 - twoPixels, 1.0 - twoPixels);
        if (up.getStepX() != 0) {
            bb.minX = 0.0;
            bb.maxX = 1.0;
        }
        if (up.getStepY() != 0) {
            bb.minY = 0.0;
            bb.maxY = 1.0;
        }
        if (up.getStepZ() != 0) {
            bb.minZ = 0.0;
            bb.maxZ = 1.0;
        }
        switch (forward) {
            case DOWN: {
                bb.maxY = 1.0;
                break;
            }
            case UP: {
                bb.minY = 0.0;
                break;
            }
            case NORTH: {
                bb.maxZ = 1.0;
                break;
            }
            case SOUTH: {
                bb.minZ = 0.0;
                break;
            }
            case EAST: {
                bb.minX = 0.0;
                break;
            }
            case WEST: {
                bb.maxX = 1.0;
                break;
            }
        }
        return Shapes.create((AABB)bb.getBoundingBox());
    }

    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.create((AABB)new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0));
    }
}

