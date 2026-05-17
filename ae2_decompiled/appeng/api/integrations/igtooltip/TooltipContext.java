/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.Vec3
 *  org.jetbrains.annotations.ApiStatus$Experimental
 */
package appeng.api.integrations.igtooltip;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public record TooltipContext(CompoundTag serverData, Vec3 hitLocation, Player player) {
    public HolderLookup.Provider registries() {
        return this.player.registryAccess();
    }
}

