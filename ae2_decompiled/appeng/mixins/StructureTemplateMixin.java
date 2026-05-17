/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package appeng.mixins;

import appeng.hooks.VisualStateSaving;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={StructureTemplate.class})
public class StructureTemplateMixin {
    @Inject(method={"fillFromWorld"}, at={@At(value="HEAD")})
    public void enableClientSideStateSaving(CallbackInfo ci) {
        VisualStateSaving.setEnabled(true);
    }

    @Inject(method={"fillFromWorld"}, at={@At(value="TAIL")})
    public void disableClientSideStateSaving(CallbackInfo ci) {
        VisualStateSaving.setEnabled(false);
    }
}

