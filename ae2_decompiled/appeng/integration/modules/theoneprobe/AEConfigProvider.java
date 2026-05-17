/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  mcjty.theoneprobe.api.IProbeConfig
 *  mcjty.theoneprobe.api.IProbeConfigProvider
 *  mcjty.theoneprobe.api.IProbeHitData
 *  mcjty.theoneprobe.api.IProbeHitEntityData
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.state.BlockState
 */
package appeng.integration.modules.theoneprobe;

import appeng.blockentity.AEBaseBlockEntity;
import mcjty.theoneprobe.api.IProbeConfig;
import mcjty.theoneprobe.api.IProbeConfigProvider;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeHitEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class AEConfigProvider
implements IProbeConfigProvider {
    public void getProbeConfig(IProbeConfig config, Player player, Level level, Entity entity, IProbeHitEntityData data) {
    }

    public void getProbeConfig(IProbeConfig config, Player player, Level level, BlockState blockState, IProbeHitData data) {
        if (level.getBlockEntity(data.getPos()) instanceof AEBaseBlockEntity) {
            config.setRFMode(0);
        }
    }
}

