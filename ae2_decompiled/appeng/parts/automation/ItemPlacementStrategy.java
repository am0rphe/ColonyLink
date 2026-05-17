/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Direction$Axis
 *  net.minecraft.core.Vec3i
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.Vec3
 *  org.jetbrains.annotations.Nullable
 */
package appeng.parts.automation;

import appeng.api.behaviors.PlacementStrategy;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.core.AEConfig;
import appeng.util.Platform;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ItemPlacementStrategy
implements PlacementStrategy {
    private static final RandomSource RANDOM_OFFSET = RandomSource.create();
    private final ServerLevel level;
    private final BlockPos pos;
    private final Direction side;
    private final BlockEntity host;
    @Nullable
    private final UUID ownerUuid;
    private boolean blocked = false;

    public ItemPlacementStrategy(ServerLevel level, BlockPos pos, Direction side, BlockEntity host, @Nullable UUID owningPlayerId) {
        this.level = level;
        this.pos = pos;
        this.side = side;
        this.host = host;
        this.ownerUuid = owningPlayerId;
    }

    @Override
    public void clearBlocked() {
        this.blocked = !this.level.getBlockState(this.pos).canBeReplaced();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final long placeInWorld(AEKey what, long amount, Actionable type, boolean placeAsEntity) {
        AEItemKey itemKey;
        block18: {
            block17: {
                if (this.blocked || !(what instanceof AEItemKey)) break block17;
                itemKey = (AEItemKey)what;
                if (amount > 0L) break block18;
            }
            return 0L;
        }
        Item i = itemKey.getItem();
        int maxStorage = (int)Math.min(amount, (long)itemKey.getMaxStackSize());
        ItemStack is = itemKey.toStack(maxStorage);
        boolean worked = false;
        Direction side = this.side.getOpposite();
        BlockPos placePos = this.pos;
        if (this.level.getBlockState(placePos).canBeReplaced()) {
            if (placeAsEntity) {
                int sum = this.countEntitesAround((Level)this.level, placePos);
                if (sum < AEConfig.instance().getFormationPlaneEntityLimit()) {
                    worked = true;
                    if (type == Actionable.MODULATE) {
                        is.setCount(maxStorage);
                        ItemPlacementStrategy.spawnItemEntity((Level)this.level, this.host, side, is);
                    }
                }
            } else {
                Player player = Platform.getFakePlayer(this.level, this.ownerUuid);
                Platform.configurePlayer(player, side, this.host);
                maxStorage = is.getCount();
                worked = true;
                if (type == Actionable.MODULATE) {
                    Direction lookDirection = side;
                    PlaneDirectionalPlaceContext context = new PlaneDirectionalPlaceContext((Level)this.level, player, placePos, lookDirection, is, lookDirection.getOpposite());
                    player.setItemInHand(InteractionHand.MAIN_HAND, is);
                    try {
                        i.useOn((UseOnContext)context);
                    }
                    finally {
                        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                    }
                    maxStorage = Math.max(0, maxStorage - is.getCount());
                } else {
                    maxStorage = 1;
                }
            }
        }
        boolean bl = this.blocked = !this.level.getBlockState(placePos).canBeReplaced();
        if (worked) {
            return maxStorage;
        }
        return 0L;
    }

    private static void spawnItemEntity(Level level, BlockEntity te, Direction side, ItemStack is) {
        double centerX = (double)te.getBlockPos().getX() + 0.5;
        double centerY = te.getBlockPos().getY();
        double centerZ = (double)te.getBlockPos().getZ() + 0.5;
        ItemEntity entity = new ItemEntity(level, centerX, centerY, centerZ, is.copy());
        double additionalYOffset = side.getStepY() == -1 ? (double)(1.0f - entity.getBbHeight()) : 0.0;
        double spawnAreaHeight = Math.max(0.0f, 1.0f - entity.getBbHeight());
        double spawnAreaWidth = Math.max(0.0f, 1.0f - entity.getBbWidth());
        double offsetX = side.getStepX() == 0 ? (double)RANDOM_OFFSET.nextFloat() * spawnAreaWidth - spawnAreaWidth / 2.0 : (double)side.getStepX() * (0.525 + (double)(entity.getBbWidth() / 2.0f));
        double offsetY = side.getStepY() == 0 ? (double)RANDOM_OFFSET.nextFloat() * spawnAreaHeight : (double)side.getStepY() + additionalYOffset;
        double offsetZ = side.getStepZ() == 0 ? (double)RANDOM_OFFSET.nextFloat() * spawnAreaWidth - spawnAreaWidth / 2.0 : (double)side.getStepZ() * (0.525 + (double)(entity.getBbWidth() / 2.0f));
        double absoluteX = centerX + offsetX;
        double absoluteY = centerY + offsetY;
        double absoluteZ = centerZ + offsetZ;
        entity.setPos(absoluteX, absoluteY, absoluteZ);
        entity.setDeltaMovement((double)side.getStepX() * 0.1, (double)side.getStepY() * 0.1, (double)side.getStepZ() * 0.1);
        level.addFreshEntity((Entity)entity);
    }

    private int countEntitesAround(Level level, BlockPos pos) {
        AABB t = new AABB(pos).inflate(8.0);
        List list = level.getEntitiesOfClass(Entity.class, t);
        return list.size();
    }

    private static class PlaneDirectionalPlaceContext
    extends BlockPlaceContext {
        private final Direction lookDirection;

        public PlaneDirectionalPlaceContext(Level level, Player player, BlockPos pos, Direction lookDirection, ItemStack itemStack, Direction facing) {
            super(level, player, InteractionHand.MAIN_HAND, itemStack, new BlockHitResult(Vec3.atBottomCenterOf((Vec3i)pos), facing, pos, false));
            this.lookDirection = lookDirection;
        }

        public BlockPos getClickedPos() {
            return this.getHitResult().getBlockPos();
        }

        public boolean canPlace() {
            return this.getLevel().getBlockState(this.getClickedPos()).canBeReplaced((BlockPlaceContext)this);
        }

        public Direction getNearestLookingDirection() {
            return Direction.DOWN;
        }

        public Direction[] getNearestLookingDirections() {
            Direction[] directionArray;
            switch (this.lookDirection) {
                default: {
                    Direction[] directionArray2 = new Direction[6];
                    directionArray2[0] = Direction.DOWN;
                    directionArray2[1] = Direction.NORTH;
                    directionArray2[2] = Direction.EAST;
                    directionArray2[3] = Direction.SOUTH;
                    directionArray2[4] = Direction.WEST;
                    directionArray = directionArray2;
                    directionArray2[5] = Direction.UP;
                    break;
                }
                case UP: {
                    Direction[] directionArray3 = new Direction[6];
                    directionArray3[0] = Direction.DOWN;
                    directionArray3[1] = Direction.UP;
                    directionArray3[2] = Direction.NORTH;
                    directionArray3[3] = Direction.EAST;
                    directionArray3[4] = Direction.SOUTH;
                    directionArray = directionArray3;
                    directionArray3[5] = Direction.WEST;
                    break;
                }
                case NORTH: {
                    Direction[] directionArray4 = new Direction[6];
                    directionArray4[0] = Direction.DOWN;
                    directionArray4[1] = Direction.NORTH;
                    directionArray4[2] = Direction.EAST;
                    directionArray4[3] = Direction.WEST;
                    directionArray4[4] = Direction.UP;
                    directionArray = directionArray4;
                    directionArray4[5] = Direction.SOUTH;
                    break;
                }
                case SOUTH: {
                    Direction[] directionArray5 = new Direction[6];
                    directionArray5[0] = Direction.DOWN;
                    directionArray5[1] = Direction.SOUTH;
                    directionArray5[2] = Direction.EAST;
                    directionArray5[3] = Direction.WEST;
                    directionArray5[4] = Direction.UP;
                    directionArray = directionArray5;
                    directionArray5[5] = Direction.NORTH;
                    break;
                }
                case WEST: {
                    Direction[] directionArray6 = new Direction[6];
                    directionArray6[0] = Direction.DOWN;
                    directionArray6[1] = Direction.WEST;
                    directionArray6[2] = Direction.SOUTH;
                    directionArray6[3] = Direction.UP;
                    directionArray6[4] = Direction.NORTH;
                    directionArray = directionArray6;
                    directionArray6[5] = Direction.EAST;
                    break;
                }
                case EAST: {
                    Direction[] directionArray7 = new Direction[6];
                    directionArray7[0] = Direction.DOWN;
                    directionArray7[1] = Direction.EAST;
                    directionArray7[2] = Direction.SOUTH;
                    directionArray7[3] = Direction.UP;
                    directionArray7[4] = Direction.NORTH;
                    directionArray = directionArray7;
                    directionArray7[5] = Direction.WEST;
                }
            }
            return directionArray;
        }

        public Direction getHorizontalDirection() {
            return this.lookDirection.getAxis() == Direction.Axis.Y ? Direction.NORTH : this.lookDirection;
        }

        public boolean isSecondaryUseActive() {
            return false;
        }

        public float getRotation() {
            return this.lookDirection.get2DDataValue() * 90;
        }
    }
}

