/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.Item$TooltipContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package appeng.items.storage;

import appeng.api.ids.AEComponents;
import appeng.api.implementations.items.ISpatialStorageCell;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.core.localization.Tooltips;
import appeng.items.AEBaseItem;
import appeng.items.storage.SpatialPlotInfo;
import appeng.spatial.SpatialStorageHelper;
import appeng.spatial.SpatialStoragePlot;
import appeng.spatial.SpatialStoragePlotManager;
import appeng.spatial.TransitionInfo;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpatialStorageCellItem
extends AEBaseItem
implements ISpatialStorageCell {
    private static final Logger LOG = LoggerFactory.getLogger(SpatialStorageCellItem.class);
    private final int maxRegion;

    public SpatialStorageCellItem(Item.Properties props, int spatialScale) {
        super(props);
        this.maxRegion = spatialScale;
    }

    @OnlyIn(value=Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> lines, TooltipFlag advancedTooltips) {
        SpatialPlotInfo plotInfo = (SpatialPlotInfo)stack.get(AEComponents.SPATIAL_PLOT_INFO);
        if (plotInfo == null) {
            lines.add((Component)Tooltips.of(GuiText.Unformatted, new Object[0]).withStyle(ChatFormatting.ITALIC));
            lines.add((Component)Tooltips.of(GuiText.SpatialCapacity, this.maxRegion, this.maxRegion, this.maxRegion));
            return;
        }
        String serialNumber = String.format(Locale.ROOT, "SP-%04d", plotInfo.id());
        BlockPos size = plotInfo.size();
        lines.add((Component)Tooltips.of(GuiText.SerialNumber, serialNumber));
        lines.add((Component)Tooltips.of(GuiText.StoredSize, size.getX(), size.getY(), size.getZ()));
    }

    @Override
    public boolean isSpatialStorage(ItemStack is) {
        return true;
    }

    @Override
    public int getMaxStoredDim(ItemStack is) {
        return this.maxRegion;
    }

    @Override
    public int getAllocatedPlotId(ItemStack stack) {
        SpatialPlotInfo plotInfo = (SpatialPlotInfo)stack.get(AEComponents.SPATIAL_PLOT_INFO);
        if (plotInfo != null) {
            try {
                if (SpatialStoragePlotManager.INSTANCE.getPlot(plotInfo.id()) == null) {
                    return -1;
                }
                return plotInfo.id();
            }
            catch (Exception e) {
                LOG.warn("Failed to retrieve spatial storage dimension for plot {}: {}", (Object)plotInfo, (Object)e);
            }
        }
        return -1;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean doSpatialTransition(ItemStack is, ServerLevel level, BlockPos min, BlockPos max, int playerId) {
        int targetX = max.getX() - min.getX() - 1;
        int targetY = max.getY() - min.getY() - 1;
        int targetZ = max.getZ() - min.getZ() - 1;
        int maxSize = this.getMaxStoredDim(is);
        if (targetX > maxSize || targetY > maxSize || targetZ > maxSize) {
            AELog.info("Failing spatial transition because the transfer area (%dx%dx%d) exceeds the cell capacity (%s).", targetX, targetY, targetZ, maxSize);
            return false;
        }
        BlockPos targetSize = new BlockPos(targetX, targetY, targetZ);
        SpatialStoragePlotManager manager = SpatialStoragePlotManager.INSTANCE;
        SpatialStoragePlot plot = SpatialStoragePlotManager.INSTANCE.getPlot(this.getAllocatedPlotId(is));
        if (plot != null) {
            if (!plot.getSize().equals((Object)targetSize)) {
                AELog.info("Failing spatial transition because the transfer area (%dx%dx%d) does not match the spatial storage plot's size (%s).", targetX, targetY, targetZ, plot.getSize());
                return false;
            }
        } else {
            plot = manager.allocatePlot(targetSize, playerId);
        }
        TransitionInfo info = new TransitionInfo(level.dimension().location(), min, max, Instant.now());
        manager.setLastTransition(plot.getId(), info);
        try {
            ServerLevel cellLevel = manager.getLevel();
            BlockPos offset = plot.getOrigin();
            this.setStoredDimension(is, plot.getId(), plot.getSize());
            SpatialStorageHelper.getInstance().swapRegions(level, min.getX() + 1, min.getY() + 1, min.getZ() + 1, cellLevel, offset.getX(), offset.getY(), offset.getZ(), targetX - 1, targetY - 1, targetZ - 1);
            boolean bl = true;
            return bl;
        }
        finally {
            if (this.getAllocatedPlotId(is) == -1) {
                manager.freePlot(plot.getId(), true);
            }
        }
    }

    public void setStoredDimension(ItemStack is, int plotId, BlockPos size) {
        is.set(AEComponents.SPATIAL_PLOT_INFO, (Object)new SpatialPlotInfo(plotId, size.immutable()));
    }
}

