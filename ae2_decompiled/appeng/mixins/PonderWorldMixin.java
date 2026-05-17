/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package appeng.mixins;

import appeng.hooks.VisualStateSaving;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets={"com.simibubi.create.foundation.ponder.PonderWorld"}, remap=false)
@Pseudo
public class PonderWorldMixin {
    @Inject(method={"restore"}, at={@At(value="HEAD")}, remap=false, require=0)
    public void enableClientSideStateSavingForRestore(CallbackInfo ci) {
        VisualStateSaving.setEnabled(true);
    }

    @Inject(method={"restore"}, at={@At(value="TAIL")}, remap=false, require=0)
    public void disableClientSideStateSavingForRestore(CallbackInfo ci) {
        VisualStateSaving.setEnabled(false);
    }

    @Inject(method={"restoreBlocks"}, at={@At(value="HEAD")}, remap=false, require=0)
    public void enableClientSideStateSavingForRestoreBlocks(CallbackInfo ci) {
        VisualStateSaving.setEnabled(true);
    }

    @Inject(method={"restoreBlocks"}, at={@At(value="TAIL")}, remap=false, require=0)
    public void disableClientSideStateSavingForRestoreBlocks(CallbackInfo ci) {
        VisualStateSaving.setEnabled(false);
    }
}

