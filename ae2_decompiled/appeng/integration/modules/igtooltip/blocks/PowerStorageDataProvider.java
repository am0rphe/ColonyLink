/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.block.entity.BlockEntity
 */
package appeng.integration.modules.igtooltip.blocks;

import appeng.api.integrations.igtooltip.TooltipBuilder;
import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.api.integrations.igtooltip.providers.BodyProvider;
import appeng.api.integrations.igtooltip.providers.ServerDataProvider;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.core.localization.InGameTooltip;
import appeng.util.Platform;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class PowerStorageDataProvider
implements BodyProvider<BlockEntity>,
ServerDataProvider<BlockEntity> {
    private static final String TAG_CURRENT_POWER = "currentPower";
    private static final String TAG_MAX_POWER = "maxPower";

    @Override
    public void buildTooltip(BlockEntity object, TooltipContext context, TooltipBuilder tooltip) {
        CompoundTag tag = context.serverData();
        if (tag.contains(TAG_MAX_POWER, 6)) {
            double currentPower = tag.getDouble(TAG_CURRENT_POWER);
            double maxPower = tag.getDouble(TAG_MAX_POWER);
            String formatCurrentPower = Platform.formatPower(currentPower, false);
            String formatMaxPower = Platform.formatPower(maxPower, false);
            tooltip.addLine((Component)InGameTooltip.Stored.text(formatCurrentPower, formatMaxPower));
        }
    }

    @Override
    public void provideServerData(Player player, BlockEntity object, CompoundTag serverData) {
        IAEPowerStorage storage;
        if (object instanceof IAEPowerStorage && (storage = (IAEPowerStorage)object).getAEMaxPower() > 0.0) {
            serverData.putDouble(TAG_CURRENT_POWER, storage.getAECurrentPower());
            serverData.putDouble(TAG_MAX_POWER, storage.getAEMaxPower());
        }
    }
}

