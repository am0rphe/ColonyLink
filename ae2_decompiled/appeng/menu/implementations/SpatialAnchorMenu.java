/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Multiset
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.level.Level
 */
package appeng.menu.implementations;

import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.blockentity.spatial.SpatialAnchorBlockEntity;
import appeng.me.service.StatisticsService;
import appeng.menu.AEBaseMenu;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import com.google.common.collect.Multiset;
import java.util.HashMap;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;

public class SpatialAnchorMenu
extends AEBaseMenu {
    public static final MenuType<SpatialAnchorMenu> TYPE = MenuTypeBuilder.create(SpatialAnchorMenu::new, SpatialAnchorBlockEntity.class).build("spatialanchor");
    private static final int UPDATE_DELAY = 20;
    private int delay = 20;
    @GuiSync(value=0)
    public long powerConsumption;
    @GuiSync(value=1)
    public int loadedChunks;
    @GuiSync(value=2)
    public YesNo overlayMode = YesNo.NO;
    @GuiSync(value=10)
    public int allLoadedWorlds;
    @GuiSync(value=11)
    public int allLoadedChunks;
    @GuiSync(value=20)
    public int allWorlds;
    @GuiSync(value=21)
    public int allChunks;

    public SpatialAnchorMenu(int id, Inventory ip, SpatialAnchorBlockEntity spatialAnchor) {
        super(TYPE, id, ip, spatialAnchor);
    }

    @Override
    public void broadcastChanges() {
        if (this.isServerSide()) {
            SpatialAnchorBlockEntity anchor = (SpatialAnchorBlockEntity)this.getBlockEntity();
            this.setOverlayMode(anchor.getConfigManager().getSetting(Settings.OVERLAY_MODE));
            IGridNode gridNode = anchor.getGridNode();
            ++this.delay;
            if (this.delay > 20 && gridNode != null) {
                IGrid grid = gridNode.getGrid();
                StatisticsService statistics = grid.getService(StatisticsService.class);
                this.powerConsumption = (long)gridNode.getIdlePowerUsage();
                this.loadedChunks = anchor.countLoadedChunks();
                HashMap<Level, Integer> stats = new HashMap<Level, Integer>();
                for (SpatialAnchorBlockEntity spatialAnchorBlockEntity : grid.getMachines(SpatialAnchorBlockEntity.class)) {
                    Level level = spatialAnchorBlockEntity.getLevel();
                    stats.merge(level, spatialAnchorBlockEntity.countLoadedChunks(), Math::max);
                }
                this.allLoadedChunks = stats.values().stream().reduce(Integer::sum).orElse(0);
                this.allLoadedWorlds = stats.keySet().size();
                this.allWorlds = statistics.getChunks().size();
                this.allChunks = 0;
                for (Multiset multiset : statistics.getChunks().values()) {
                    this.allChunks += multiset.elementSet().size();
                }
                this.delay = 0;
            }
        }
        super.broadcastChanges();
    }

    public YesNo getOverlayMode() {
        return this.overlayMode;
    }

    public void setOverlayMode(YesNo mode) {
        this.overlayMode = mode;
    }
}

