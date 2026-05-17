/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.neoforge.common.SoundActions
 *  org.jetbrains.annotations.Nullable
 */
package appeng.util.fluid;

import appeng.api.stacks.AEFluidKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.SoundActions;
import org.jetbrains.annotations.Nullable;

public final class FluidSoundHelper {
    private FluidSoundHelper() {
    }

    public static void playFillSound(Player player, @Nullable AEFluidKey fluid) {
        if (fluid == null) {
            return;
        }
        SoundEvent fillSound = fluid.getFluid().getFluidType().getSound((Entity)player, SoundActions.BUCKET_FILL);
        if (fillSound == null) {
            return;
        }
        FluidSoundHelper.playSound(player, fillSound);
    }

    public static void playEmptySound(Player player, @Nullable AEFluidKey fluid) {
        if (fluid == null) {
            return;
        }
        SoundEvent fillSound = fluid.getFluid().getFluidType().getSound((Entity)player, SoundActions.BUCKET_EMPTY);
        if (fillSound == null) {
            return;
        }
        FluidSoundHelper.playSound(player, fillSound);
    }

    private static void playSound(Player player, SoundEvent fillSound) {
        player.playNotifySound(fillSound, SoundSource.PLAYERS, 1.0f, 1.0f);
    }
}

