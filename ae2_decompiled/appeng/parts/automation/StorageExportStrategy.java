/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.neoforge.capabilities.BlockCapability
 *  net.neoforged.neoforge.capabilities.BlockCapabilityCache
 *  net.neoforged.neoforge.capabilities.Capabilities$FluidHandler
 *  net.neoforged.neoforge.capabilities.Capabilities$ItemHandler
 *  net.neoforged.neoforge.fluids.FluidStack
 *  net.neoforged.neoforge.fluids.capability.IFluidHandler
 *  net.neoforged.neoforge.items.IItemHandler
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package appeng.parts.automation;

import appeng.api.behaviors.StackExportStrategy;
import appeng.api.behaviors.StackTransferContext;
import appeng.api.config.Actionable;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEKey;
import appeng.api.storage.StorageHelper;
import appeng.parts.automation.HandlerStrategy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageExportStrategy<T, S>
implements StackExportStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(StorageExportStrategy.class);
    private final BlockCapabilityCache<T, Direction> cache;
    private final HandlerStrategy<T, S> handlerStrategy;

    public StorageExportStrategy(BlockCapability<T, Direction> capability, HandlerStrategy<T, S> handlerStrategy, ServerLevel level, BlockPos fromPos, Direction fromSide) {
        this.handlerStrategy = handlerStrategy;
        this.cache = BlockCapabilityCache.create(capability, (ServerLevel)level, (BlockPos)fromPos, (Object)fromSide);
    }

    @Override
    public long transfer(StackTransferContext context, AEKey what, long amount) {
        if (!this.handlerStrategy.isSupported(what)) {
            return 0L;
        }
        Object adjacentStorage = this.cache.getCapability();
        if (adjacentStorage == null) {
            return 0L;
        }
        IStorageService inv = context.getInternalStorage();
        long extracted = StorageHelper.poweredExtraction(context.getEnergySource(), inv.getInventory(), what, amount, context.getActionSource(), Actionable.SIMULATE);
        long wasInserted = this.handlerStrategy.insert(adjacentStorage, what, extracted, Actionable.SIMULATE);
        if (wasInserted > 0L && (wasInserted = this.handlerStrategy.insert(adjacentStorage, what, extracted = StorageHelper.poweredExtraction(context.getEnergySource(), inv.getInventory(), what, wasInserted, context.getActionSource(), Actionable.MODULATE), Actionable.MODULATE)) < extracted) {
            long leftover = extracted - wasInserted;
            if ((leftover -= inv.getInventory().insert(what, leftover, Actionable.MODULATE, context.getActionSource())) > 0L) {
                LOG.error("Storage export: adjacent block unexpectedly refused insert, voided {}x{}", (Object)leftover, (Object)what);
            }
        }
        return wasInserted;
    }

    @Override
    public long push(AEKey what, long amount, Actionable mode) {
        if (!this.handlerStrategy.isSupported(what)) {
            return 0L;
        }
        Object adjacentStorage = this.cache.getCapability();
        if (adjacentStorage == null) {
            return 0L;
        }
        return this.handlerStrategy.insert(adjacentStorage, what, amount, mode);
    }

    public static StackExportStrategy createItem(ServerLevel level, BlockPos fromPos, Direction fromSide) {
        return new StorageExportStrategy<IItemHandler, ItemStack>(Capabilities.ItemHandler.BLOCK, HandlerStrategy.ITEMS, level, fromPos, fromSide);
    }

    public static StackExportStrategy createFluid(ServerLevel level, BlockPos fromPos, Direction fromSide) {
        return new StorageExportStrategy<IFluidHandler, FluidStack>(Capabilities.FluidHandler.BLOCK, HandlerStrategy.FLUIDS, level, fromPos, fromSide);
    }
}

