/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.Vec3
 */
package appeng.parts.automation;

import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.energy.IEnergyWatcher;
import appeng.api.networking.energy.IEnergyWatcherNode;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.util.IConfigManagerBuilder;
import appeng.core.AppEng;
import appeng.items.parts.PartModels;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.EnergyLevelEmitterMenu;
import appeng.menu.locator.MenuLocators;
import appeng.parts.PartModel;
import appeng.parts.automation.AbstractLevelEmitterPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class EnergyLevelEmitterPart
extends AbstractLevelEmitterPart {
    @PartModels
    public static final ResourceLocation MODEL_BASE_OFF = AppEng.makeId("part/level_emitter_base_off");
    @PartModels
    public static final ResourceLocation MODEL_BASE_ON = AppEng.makeId("part/level_emitter_base_on");
    @PartModels
    public static final ResourceLocation MODEL_STATUS_OFF = AppEng.makeId("part/level_emitter_status_off");
    @PartModels
    public static final ResourceLocation MODEL_STATUS_ON = AppEng.makeId("part/level_emitter_status_on");
    @PartModels
    public static final ResourceLocation MODEL_STATUS_HAS_CHANNEL = AppEng.makeId("part/level_emitter_status_has_channel");
    public static final PartModel MODEL_OFF_OFF = new PartModel(MODEL_BASE_OFF, MODEL_STATUS_OFF);
    public static final PartModel MODEL_OFF_ON = new PartModel(MODEL_BASE_OFF, MODEL_STATUS_ON);
    public static final PartModel MODEL_OFF_HAS_CHANNEL = new PartModel(MODEL_BASE_OFF, MODEL_STATUS_HAS_CHANNEL);
    public static final PartModel MODEL_ON_OFF = new PartModel(MODEL_BASE_ON, MODEL_STATUS_OFF);
    public static final PartModel MODEL_ON_ON = new PartModel(MODEL_BASE_ON, MODEL_STATUS_ON);
    public static final PartModel MODEL_ON_HAS_CHANNEL = new PartModel(MODEL_BASE_ON, MODEL_STATUS_HAS_CHANNEL);
    private IEnergyWatcher energyWatcher;
    private final IEnergyWatcherNode energyWatcherNode = new IEnergyWatcherNode(){

        @Override
        public void updateWatcher(IEnergyWatcher newWatcher) {
            EnergyLevelEmitterPart.this.energyWatcher = newWatcher;
            EnergyLevelEmitterPart.this.configureWatchers();
        }

        @Override
        public void onThresholdPass(IEnergyService energyGrid) {
            EnergyLevelEmitterPart.this.lastReportedValue = (long)energyGrid.getStoredPower();
            EnergyLevelEmitterPart.this.updateState();
        }
    };

    public EnergyLevelEmitterPart(IPartItem<?> partItem) {
        super(partItem);
        this.getMainNode().addService(IEnergyWatcherNode.class, this.energyWatcherNode);
    }

    @Override
    protected void registerSettings(IConfigManagerBuilder builder) {
        super.registerSettings(builder);
        builder.registerSetting(Settings.REDSTONE_EMITTER, RedstoneMode.HIGH_SIGNAL);
    }

    @Override
    protected int getUpgradeSlots() {
        return 0;
    }

    @Override
    protected void configureWatchers() {
        if (this.energyWatcher != null) {
            this.energyWatcher.reset();
        }
        if (this.energyWatcher != null) {
            this.energyWatcher.add(this.getReportingValue());
        }
        this.getMainNode().ifPresent(grid -> {
            this.lastReportedValue = (long)grid.getEnergyService().getStoredPower();
            this.updateState();
        });
    }

    @Override
    protected boolean hasDirectOutput() {
        return false;
    }

    @Override
    protected boolean getDirectOutput() {
        throw new UnsupportedOperationException("hasDirectOutput is false...");
    }

    @Override
    public boolean onUseWithoutItem(Player player, Vec3 pos) {
        if (!this.isClientSide()) {
            MenuOpener.open(EnergyLevelEmitterMenu.TYPE, player, MenuLocators.forPart(this));
        }
        return true;
    }

    @Override
    public IPartModel getStaticModels() {
        if (this.isActive() && this.isPowered()) {
            return this.isLevelEmitterOn() ? MODEL_ON_HAS_CHANNEL : MODEL_OFF_HAS_CHANNEL;
        }
        if (this.isPowered()) {
            return this.isLevelEmitterOn() ? MODEL_ON_ON : MODEL_OFF_ON;
        }
        return this.isLevelEmitterOn() ? MODEL_ON_OFF : MODEL_OFF_OFF;
    }
}

