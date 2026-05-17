/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.gametest.framework.GameTestAssertException
 *  net.minecraft.gametest.framework.GameTestAssertPosException
 *  net.minecraft.gametest.framework.GameTestHelper
 *  net.minecraft.gametest.framework.GameTestInfo
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BaseContainerBlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.material.Fluid
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.neoforge.capabilities.BlockCapability
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package appeng.server.testworld;

import appeng.api.config.Actionable;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.IManagedGridNode;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.blockentity.AEBaseInvBlockEntity;
import appeng.me.helpers.BaseActionSource;
import appeng.me.helpers.IGridConnectedBlockEntity;
import appeng.parts.AEBasePart;
import appeng.util.Platform;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestAssertPosException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlotTestHelper
extends GameTestHelper {
    private final BlockPos plotTranslation;

    public PlotTestHelper(BlockPos plotTranslation, GameTestInfo gameTestInfo) {
        super(gameTestInfo);
        this.plotTranslation = plotTranslation;
    }

    public BlockPos absolutePos(BlockPos pos) {
        return super.absolutePos(pos.offset((Vec3i)this.plotTranslation).offset(0, 1, 0));
    }

    public BlockPos relativePos(BlockPos pos) {
        return super.relativePos(pos).offset(-this.plotTranslation.getX(), -this.plotTranslation.getY(), -this.plotTranslation.getZ()).offset(0, -1, 0);
    }

    public Vec3 absoluteVec(Vec3 relativeVec3) {
        return super.absoluteVec(relativeVec3).add((double)this.plotTranslation.getX(), (double)this.plotTranslation.getY(), (double)this.plotTranslation.getZ());
    }

    public <T extends AEBasePart> T getPart(BlockPos pos, @Nullable Direction side, Class<T> partClass) {
        BlockEntity be = this.getBlockEntity(pos);
        if (!(be instanceof IPartHost)) {
            this.fail("not a part host", pos);
            return null;
        }
        IPartHost partHost = (IPartHost)be;
        IPart part = partHost.getPart(side);
        if (part == null) {
            this.fail("part missing", pos);
        }
        if (!partClass.isInstance(part)) {
            this.fail("wrong part", pos);
        }
        return (T)((AEBasePart)partClass.cast(part));
    }

    @NotNull
    public IGridNode getGridNode(BlockPos pos) {
        this.checkAllInitialized();
        BlockEntity be = this.getLevel().getBlockEntity(this.absolutePos(pos));
        if (be instanceof IGridConnectedBlockEntity) {
            IGridConnectedBlockEntity gridConnectedBlockEntity = (IGridConnectedBlockEntity)be;
            IGridNode node = gridConnectedBlockEntity.getMainNode().getNode();
            this.check(node != null, "no node", pos);
            return node;
        }
        IInWorldGridNodeHost nodeHost = GridHelper.getNodeHost((Level)this.getLevel(), this.absolutePos(pos));
        if (nodeHost != null) {
            for (Direction side : Direction.values()) {
                IGridNode node = nodeHost.getGridNode(side);
                if (node == null) continue;
                return node;
            }
        }
        this.fail("no node", pos);
        return null;
    }

    @NotNull
    public IGrid getGrid(BlockPos pos) {
        IGridNode node = this.getGridNode(pos);
        return node.getGrid();
    }

    public void checkAllInitialized() {
        this.forEveryBlockInStructure(blockPos -> {
            BlockEntity be = this.getLevel().getBlockEntity(this.absolutePos((BlockPos)blockPos));
            if (be instanceof IGridConnectedBlockEntity) {
                IGridConnectedBlockEntity gridConnectedBlockEntity = (IGridConnectedBlockEntity)be;
                this.check(gridConnectedBlockEntity.getMainNode().isReady(), "BE " + String.valueOf(be) + " is not ready");
            } else if (be instanceof IPartHost) {
                IPartHost partHost = (IPartHost)be;
                for (Direction side : Platform.DIRECTIONS_WITH_NULL) {
                    IPart part = partHost.getPart(side);
                    if (!(part instanceof AEBasePart)) continue;
                    AEBasePart basePart = (AEBasePart)part;
                    IManagedGridNode mainNode = basePart.getMainNode();
                    this.check(mainNode.isReady(), "Part " + String.valueOf(part) + " is not ready");
                }
            }
        });
    }

    public void assertContains(IGrid grid, Item item) {
        MEStorage storage = grid.getStorageService().getInventory();
        this.assertContains(storage, AEItemKey.of((ItemLike)item));
    }

    public void assertContains(MEStorage storage, AEKey key) {
        long count = storage.getAvailableStacks().get(key);
        if (count <= 0L) {
            throw new GameTestAssertException("Network storage does not contain " + String.valueOf(key) + ". Available keys: " + String.valueOf(storage.getAvailableStacks().keySet()));
        }
    }

    public void assertContainsNot(MEStorage storage, AEKey key) {
        long count = storage.getAvailableStacks().get(key);
        if (count > 0L) {
            throw new GameTestAssertException("Network storage contains unexpected " + String.valueOf(key) + ".");
        }
    }

    public void assertNetworkContains(BlockPos gridPos, ItemLike item) {
        this.assertNetworkContains(gridPos, AEItemKey.of(item));
    }

    public void assertNetworkContains(BlockPos gridPos, Fluid fluid) {
        this.assertNetworkContains(gridPos, AEFluidKey.of(fluid));
    }

    public void assertNetworkContainsNot(BlockPos gridPos, ItemLike item) {
        this.assertNetworkContainsNot(gridPos, AEItemKey.of(item));
    }

    public void assertNetworkContainsNot(BlockPos gridPos, Fluid fluid) {
        this.assertNetworkContainsNot(gridPos, AEFluidKey.of(fluid));
    }

    public void assertNetworkContains(BlockPos gridPos, AEKey key) {
        IGrid grid = this.getGrid(gridPos);
        KeyCounter storage = grid.getStorageService().getInventory().getAvailableStacks();
        long count = storage.get(key);
        if (count <= 0L) {
            throw new GameTestAssertPosException("Network storage does not contain " + String.valueOf(key) + ". Available keys: " + String.valueOf(storage.keySet()), this.absolutePos(gridPos), gridPos, this.getTick());
        }
    }

    public void assertNetworkContainsNot(BlockPos gridPos, AEKey key) {
        IGrid grid = this.getGrid(gridPos);
        long count = grid.getStorageService().getInventory().getAvailableStacks().get(key);
        if (count > 0L) {
            throw new GameTestAssertPosException("Network storage contains unexpected " + String.valueOf(key) + ".", this.absolutePos(gridPos), gridPos, this.getTick());
        }
    }

    public void clearStorage(IGrid grid) {
        this.clearStorage(grid.getStorageService().getInventory());
    }

    public void clearStorage(MEStorage storage) {
        KeyCounter counter = storage.getAvailableStacks();
        for (AEKey key : counter.keySet()) {
            storage.extract(key, Long.MAX_VALUE, Actionable.MODULATE, new BaseActionSource());
        }
    }

    public <T, C> T getCapability(BlockPos ref, BlockCapability<T, C> cap, C context) {
        return (T)this.getLevel().getCapability(cap, this.absolutePos(ref), context);
    }

    public void assertEquals(BlockPos ref, Object expected, Object actual) {
        if (!Objects.equals(expected, actual)) {
            String message = String.valueOf(actual) + " was not " + String.valueOf(expected);
            this.fail(message, ref);
        }
    }

    public void check(boolean test, String errorMessage) throws GameTestAssertException {
        if (!test) {
            this.fail(errorMessage);
        }
    }

    public void check(boolean test, String errorMessage, BlockPos pos) throws GameTestAssertException {
        if (!test) {
            this.fail(errorMessage, pos);
        }
    }

    public KeyCounter countContainerContentAt(BlockPos pos) {
        KeyCounter counter = new KeyCounter();
        this.countContainerContentAt(pos, counter);
        return counter;
    }

    public void countContainerContentAt(BlockPos pos, KeyCounter counter) {
        BlockEntity be = this.getBlockEntity(pos);
        if (be instanceof BaseContainerBlockEntity) {
            BaseContainerBlockEntity container = (BaseContainerBlockEntity)be;
            for (int i = 0; i < container.getContainerSize(); ++i) {
                ItemStack item = container.getItem(i);
                if (item.isEmpty()) continue;
                counter.add(AEItemKey.of(item), item.getCount());
            }
        } else if (be instanceof AEBaseInvBlockEntity) {
            AEBaseInvBlockEntity aeBe = (AEBaseInvBlockEntity)be;
            InternalInventory internalInv = aeBe.getInternalInventory();
            for (ItemStack item : internalInv) {
                counter.add(AEItemKey.of(item), item.getCount());
            }
        } else {
            throw new RuntimeException("Unsupported BE: " + String.valueOf(be));
        }
    }
}

