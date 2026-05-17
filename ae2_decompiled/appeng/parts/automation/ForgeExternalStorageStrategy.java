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
 *  org.jetbrains.annotations.Nullable
 */
package appeng.parts.automation;

import appeng.api.behaviors.ExternalStorageStrategy;
import appeng.api.storage.MEStorage;
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
import org.jetbrains.annotations.Nullable;

public class ForgeExternalStorageStrategy<T, S>
implements ExternalStorageStrategy {
    private final BlockCapabilityCache<T, Direction> cache;
    private final HandlerStrategy<T, S> conversion;

    public ForgeExternalStorageStrategy(BlockCapability<T, Direction> capability, HandlerStrategy<T, S> conversion, ServerLevel level, BlockPos fromPos, Direction fromSide) {
        this.cache = BlockCapabilityCache.create(capability, (ServerLevel)level, (BlockPos)fromPos, (Object)fromSide);
        this.conversion = conversion;
    }

    @Override
    @Nullable
    public MEStorage createWrapper(boolean extractableOnly, Runnable injectOrExtractCallback) {
        Object storage = this.cache.getCapability();
        if (storage == null) {
            return null;
        }
        ExternalStorageFacade result = this.conversion.getFacade(storage);
        result.setChangeListener(injectOrExtractCallback);
        result.setExtractableOnly(extractableOnly);
        return result;
    }

    public static ExternalStorageStrategy createItem(ServerLevel level, BlockPos fromPos, Direction fromSide) {
        return new ForgeExternalStorageStrategy<IItemHandler, ItemStack>(Capabilities.ItemHandler.BLOCK, HandlerStrategy.ITEMS, level, fromPos, fromSide);
    }

    public static ExternalStorageStrategy createFluid(ServerLevel level, BlockPos fromPos, Direction fromSide) {
        return new ForgeExternalStorageStrategy<IFluidHandler, FluidStack>(Capabilities.FluidHandler.BLOCK, HandlerStrategy.FLUIDS, level, fromPos, fromSide);
    }
}

