/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.entity.player.Player
 *  org.jetbrains.annotations.ApiStatus$NonExtendable
 *  org.jetbrains.annotations.ApiStatus$OverrideOnly
 */
package appeng.api.integrations.igtooltip.providers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.ApiStatus;

@FunctionalInterface
@ApiStatus.NonExtendable
@ApiStatus.OverrideOnly
public interface ServerDataProvider<T> {
    public void provideServerData(Player var1, T var2, CompoundTag var3);
}

