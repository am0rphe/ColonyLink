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
 */
package appeng.parts.automation;

import appeng.api.behaviors.StackImportStrategy;
import appeng.api.behaviors.StackTransferContext;
import appeng.api.config.Actionable;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.GenericStack;
import appeng.core.AELog;
import appeng.me.storage.ExternalStorageFacade;
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

public class StorageImportStrategy<T, S>
implements StackImportStrategy {
    private final BlockCapabilityCache<T, Direction> cache;
    private final HandlerStrategy<T, S> conversion;

    public StorageImportStrategy(BlockCapability<T, Direction> capability, HandlerStrategy<T, S> conversion, ServerLevel level, BlockPos fromPos, Direction fromSide) {
        this.cache = BlockCapabilityCache.create(capability, (ServerLevel)level, (BlockPos)fromPos, (Object)fromSide);
        this.conversion = conversion;
    }

    @Override
    public boolean transfer(StackTransferContext context) {
        if (!context.isKeyTypeEnabled(this.conversion.getKeyType())) {
            return false;
        }
        Object adjacentHandler = this.cache.getCapability();
        if (adjacentHandler == null) {
            return false;
        }
        ExternalStorageFacade adjacentStorage = this.conversion.getFacade(adjacentHandler);
        long remainingTransferAmount = (long)context.getOperationsRemaining() * (long)this.conversion.getKeyType().getAmountPerOperation();
        IStorageService inv = context.getInternalStorage();
        for (int i = 0; i < adjacentStorage.getSlots() && remainingTransferAmount > 0L; ++i) {
            GenericStack resource = adjacentStorage.getStackInSlot(i);
            if (resource == null || context.isInFilter(resource.what()) == context.isInverted()) continue;
            long amountForThisResource = inv.getInventory().insert(resource.what(), remainingTransferAmount, Actionable.SIMULATE, context.getActionSource());
            long amount = adjacentStorage.extract(resource.what(), amountForThisResource, Actionable.MODULATE, context.getActionSource());
            if (amount <= 0L) continue;
            long inserted = inv.getInventory().insert(resource.what(), amount, Actionable.MODULATE, context.getActionSource());
            if (inserted < amount) {
                long leftover = amount - inserted;
                if ((leftover -= adjacentStorage.insert(resource.what(), leftover, Actionable.MODULATE, context.getActionSource())) > 0L) {
                    AELog.warn("Extracted %dx%s from adjacent storage and voided it because network refused insert", leftover, resource.what());
                }
            }
            long opsUsed = Math.max(1L, inserted / (long)this.conversion.getKeyType().getAmountPerOperation());
            context.reduceOperationsRemaining(opsUsed);
            remainingTransferAmount -= inserted;
        }
        return false;
    }

    public static StackImportStrategy createItem(ServerLevel level, BlockPos fromPos, Direction fromSide) {
        return new StorageImportStrategy<IItemHandler, ItemStack>(Capabilities.ItemHandler.BLOCK, HandlerStrategy.ITEMS, level, fromPos, fromSide);
    }

    public static StackImportStrategy createFluid(ServerLevel level, BlockPos fromPos, Direction fromSide) {
        return new StorageImportStrategy<IFluidHandler, FluidStack>(Capabilities.FluidHandler.BLOCK, HandlerStrategy.FLUIDS, level, fromPos, fromSide);
    }
}

