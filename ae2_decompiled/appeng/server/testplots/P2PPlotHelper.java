/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.component.DataComponentMap
 *  net.minecraft.core.component.DataComponentMap$Builder
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 */
package appeng.server.testplots;

import appeng.api.networking.IGrid;
import appeng.api.parts.IPart;
import appeng.api.parts.PartHelper;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.core.definitions.ItemDefinition;
import appeng.items.parts.PartItem;
import appeng.me.service.P2PService;
import appeng.parts.p2p.P2PTunnelPart;
import appeng.server.testplots.PosAndSide;
import appeng.server.testworld.PlotBuilder;
import appeng.util.SettingsFrom;
import java.util.Collection;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

public final class P2PPlotHelper {
    private P2PPlotHelper() {
    }

    public static <T extends P2PTunnelPart<?>> void placeTunnel(PlotBuilder plot, ItemDefinition<PartItem<T>> tunnel) {
        BlockPos origin = BlockPos.ZERO;
        plot.creativeEnergyCell(origin.below());
        plot.cable(origin);
        plot.cable(origin.west()).part(Direction.WEST, tunnel);
        plot.cable(origin.east()).part(Direction.EAST, tunnel);
        plot.addPostInitAction((level, player, absOrigin) -> {
            CableBusBlockEntity be = (CableBusBlockEntity)level.getBlockEntity(absOrigin);
            IGrid grid = be.getCableBus().getPart(null).getGridNode().getGrid();
            P2PPlotHelper.linkTunnels(grid, ((PartItem)tunnel.get()).getPartClass(), absOrigin.west(), absOrigin.east());
        });
    }

    public static <T extends P2PTunnelPart<?>> short linkTunnels(IGrid grid, Class<T> tunnelType, BlockPos inputPos, BlockPos outputPos) {
        P2PService p2p = P2PService.get(grid);
        P2PTunnelPart inputTunnel = null;
        P2PTunnelPart outputTunnel = null;
        for (P2PTunnelPart p2pPart : grid.getMachines(tunnelType)) {
            if (p2pPart.getBlockEntity().getBlockPos().equals((Object)inputPos)) {
                inputTunnel = p2pPart;
                continue;
            }
            if (!p2pPart.getBlockEntity().getBlockPos().equals((Object)outputPos)) continue;
            outputTunnel = p2pPart;
        }
        Objects.requireNonNull(inputTunnel, "inputTunnel");
        Objects.requireNonNull(outputTunnel, "outputTunnel");
        inputTunnel.setFrequency(p2p.newFrequency());
        p2p.updateFreq(inputTunnel, inputTunnel.getFrequency());
        DataComponentMap.Builder settings = DataComponentMap.builder();
        inputTunnel.exportSettings(SettingsFrom.MEMORY_CARD, settings);
        outputTunnel.importSettings(SettingsFrom.MEMORY_CARD, settings.build(), null);
        return inputTunnel.getFrequency();
    }

    public static short linkTunnels(IGrid grid, PosAndSide inputPos, Collection<PosAndSide> outputPositions) {
        P2PService p2p = P2PService.get(grid);
        ServerLevel level = grid.getPivot().getLevel();
        P2PTunnelPart<?> inputTunnel = P2PPlotHelper.getTunnelAt((Level)level, inputPos);
        inputTunnel.setFrequency(p2p.newFrequency());
        p2p.updateFreq(inputTunnel, inputTunnel.getFrequency());
        DataComponentMap.Builder settings = DataComponentMap.builder();
        inputTunnel.exportSettings(SettingsFrom.MEMORY_CARD, settings);
        for (PosAndSide outputPos : outputPositions) {
            P2PTunnelPart<?> outputTunnel = P2PPlotHelper.getTunnelAt((Level)level, outputPos);
            outputTunnel.importSettings(SettingsFrom.MEMORY_CARD, settings.build(), null);
        }
        return inputTunnel.getFrequency();
    }

    private static P2PTunnelPart<?> getTunnelAt(Level level, PosAndSide posAndSide) {
        IPart part = PartHelper.getPart((BlockGetter)level, posAndSide.pos(), posAndSide.side());
        if (!(part instanceof P2PTunnelPart)) {
            throw new IllegalStateException("No P2P @ " + String.valueOf(posAndSide));
        }
        P2PTunnelPart p2PTunnelPart = (P2PTunnelPart)part;
        return p2PTunnelPart;
    }
}

