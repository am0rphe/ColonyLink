/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ChunkMap
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.ChunkPos
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package appeng.mixins.chunkloading;

import appeng.core.AEConfig;
import appeng.server.services.ChunkLoadingService;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ChunkMap.class})
public class ChunkMapMixin {
    @Shadow
    @Final
    ServerLevel level;

    @Inject(at={@At(value="RETURN")}, method={"anyPlayerCloseEnoughForSpawning"}, cancellable=true)
    private void spatialAnchorEnableRandomTicks(ChunkPos pos, CallbackInfoReturnable<Boolean> ci) {
        if (AEConfig.instance().isSpatialAnchorEnablesRandomTicks() && !((Boolean)ci.getReturnValue()).booleanValue() && ChunkLoadingService.getInstance().isChunkForced(this.level, pos.x, pos.z)) {
            ci.setReturnValue((Object)true);
        }
    }
}

