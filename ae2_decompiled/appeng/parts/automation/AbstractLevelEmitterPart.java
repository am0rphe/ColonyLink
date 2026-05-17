/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.component.DataComponentMap
 *  net.minecraft.core.component.DataComponentMap$Builder
 *  net.minecraft.core.particles.DustParticleOptions
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  org.jetbrains.annotations.Nullable
 */
package appeng.parts.automation;

import appeng.api.config.RedstoneMode;
import appeng.api.config.Setting;
import appeng.api.config.Settings;
import appeng.api.ids.AEComponents;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNodeListener;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigManagerBuilder;
import appeng.parts.automation.UpgradeablePart;
import appeng.util.Platform;
import appeng.util.SettingsFrom;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractLevelEmitterPart
extends UpgradeablePart {
    private boolean prevState;
    protected long lastReportedValue;
    private long reportingValue;
    private boolean clientSideOn;

    public AbstractLevelEmitterPart(IPartItem<?> partItem) {
        super(partItem);
        this.getMainNode().setFlags(new GridFlags[0]);
    }

    @Override
    protected void registerSettings(IConfigManagerBuilder builder) {
        super.registerSettings(builder);
        builder.registerSetting(Settings.REDSTONE_EMITTER, RedstoneMode.HIGH_SIGNAL);
    }

    protected abstract void configureWatchers();

    protected abstract boolean hasDirectOutput();

    protected abstract boolean getDirectOutput();

    @Override
    protected final void onMainNodeStateChanged(IGridNodeListener.State reason) {
        super.onMainNodeStateChanged(reason);
        if (this.getMainNode().hasGridBooted()) {
            this.updateState();
        }
    }

    @Override
    public void writeToStream(RegistryFriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeBoolean(this.prevState);
    }

    @Override
    public boolean readFromStream(RegistryFriendlyByteBuf data) {
        boolean changed = super.readFromStream(data);
        boolean wasOn = this.clientSideOn;
        this.clientSideOn = data.readBoolean();
        return changed || wasOn != this.clientSideOn;
    }

    @Override
    public void writeVisualStateToNBT(CompoundTag data) {
        super.writeVisualStateToNBT(data);
        data.putBoolean("on", this.isLevelEmitterOn());
    }

    @Override
    public void readVisualStateFromNBT(CompoundTag data) {
        super.readVisualStateFromNBT(data);
        this.clientSideOn = data.getBoolean("on");
    }

    protected void updateState() {
        boolean isOn = this.isLevelEmitterOn();
        if (this.prevState != isOn) {
            this.getHost().markForUpdate();
            BlockEntity te = this.getHost().getBlockEntity();
            this.prevState = isOn;
            Platform.notifyBlocksOfNeighbors(te.getLevel(), te.getBlockPos());
            Platform.notifyBlocksOfNeighbors(te.getLevel(), te.getBlockPos().relative(this.getSide()));
        }
    }

    public final long getReportingValue() {
        return this.reportingValue;
    }

    public final void setReportingValue(long v) {
        this.reportingValue = v;
        this.onReportingValueChanged();
        this.updateState();
    }

    protected void onReportingValueChanged() {
    }

    @Override
    public final int isProvidingStrongPower() {
        return this.prevState ? 15 : 0;
    }

    @Override
    public final int isProvidingWeakPower() {
        return this.prevState ? 15 : 0;
    }

    @Override
    public final void animateTick(Level level, BlockPos pos, RandomSource r) {
        if (this.isLevelEmitterOn()) {
            Direction d = this.getSide();
            double d0 = (double)((float)d.getStepX() * 0.45f) + (double)(r.nextFloat() - 0.5f) * 0.2;
            double d1 = (double)((float)d.getStepY() * 0.45f) + (double)(r.nextFloat() - 0.5f) * 0.2;
            double d2 = (double)((float)d.getStepZ() * 0.45f) + (double)(r.nextFloat() - 0.5f) * 0.2;
            level.addParticle((ParticleOptions)DustParticleOptions.REDSTONE, 0.5 + (double)pos.getX() + d0, 0.5 + (double)pos.getY() + d1, 0.5 + (double)pos.getZ() + d2, 0.0, 0.0, 0.0);
        }
    }

    protected boolean isLevelEmitterOn() {
        boolean flipState;
        if (this.isClientSide()) {
            return this.clientSideOn;
        }
        if (!this.getMainNode().isActive()) {
            return false;
        }
        if (this.hasDirectOutput()) {
            return this.getDirectOutput();
        }
        boolean bl = flipState = this.getConfigManager().getSetting(Settings.REDSTONE_EMITTER) == RedstoneMode.LOW_SIGNAL;
        return flipState ? this.reportingValue >= this.lastReportedValue + 1L : this.reportingValue < this.lastReportedValue + 1L;
    }

    @Override
    public final boolean canConnectRedstone() {
        return true;
    }

    @Override
    public void readFromNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.readFromNBT(data, registries);
        this.lastReportedValue = data.getLong("lastReportedValue");
        this.reportingValue = data.getLong("reportingValue");
        this.prevState = data.getBoolean("prevState");
    }

    @Override
    public void writeToNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.writeToNBT(data, registries);
        data.putLong("lastReportedValue", this.lastReportedValue);
        data.putLong("reportingValue", this.reportingValue);
        data.putBoolean("prevState", this.prevState);
    }

    @Override
    public final float getCableConnectionLength(AECableType cable) {
        return 16.0f;
    }

    @Override
    public final void getBoxes(IPartCollisionHelper bch) {
        bch.addBox(7.0, 7.0, 11.0, 9.0, 9.0, 16.0);
    }

    @Override
    public final AECableType getDesiredConnectionType() {
        return AECableType.SMART;
    }

    @Override
    public final void onSettingChanged(IConfigManager manager, Setting<?> setting) {
        this.configureWatchers();
    }

    @Override
    public void importSettings(SettingsFrom mode, DataComponentMap input, @Nullable Player player) {
        super.importSettings(mode, input, player);
        Long reportingValue = (Long)input.get(AEComponents.EXPORTED_LEVEL_EMITTER_VALUE);
        if (reportingValue != null) {
            this.setReportingValue(reportingValue);
        }
    }

    @Override
    public void exportSettings(SettingsFrom mode, DataComponentMap.Builder builder) {
        super.exportSettings(mode, builder);
        if (mode == SettingsFrom.MEMORY_CARD) {
            builder.set(AEComponents.EXPORTED_LEVEL_EMITTER_VALUE, (Object)this.reportingValue);
        }
    }

    @Override
    protected boolean shouldSendPowerStateToClient() {
        return false;
    }

    @Override
    protected boolean shouldSendMissingChannelStateToClient() {
        return false;
    }
}

